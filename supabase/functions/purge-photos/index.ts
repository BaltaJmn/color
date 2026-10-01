// Every night, from pg_cron (migrations/20260923120100_purge.sql): shared photos live seven days.
// The colors stay; the feed only ever shows today and yesterday, and a friend's mosaic is colors.
import { createClient, type SupabaseClient } from "jsr:@supabase/supabase-js@2";

const PHOTO_TTL_DAYS = 7;
const BATCH = 100;
// One night's work has a ceiling: whatever a pass leaves, the next night finds again.
const MAX_ROUNDS = 100;

Deno.serve(async (req) => {
  const key = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
  // Only the schedule calls it. Anyone else gets nothing, not even a count.
  if (req.headers.get("Authorization") !== `Bearer ${key}`) return new Response(null, { status: 401 });

  const db = createClient(Deno.env.get("SUPABASE_URL")!, key);
  // The two phases do not depend on each other: the sweep of orphans is the only hard bound on how
  // long a file lives, so a row that cannot be purged must not keep it from running. The first error
  // is the one reported, and the answer is a 500 only after both have had their turn.
  const rows = await phase(() => purgeRows(db));
  const orphans = await phase(() => sweepOrphans(db));
  const error = rows.error ?? orphans.error;
  const body = { purged: rows.count, orphans: orphans.count };
  if (error) return Response.json({ ...body, error }, { status: 500 });
  return Response.json(body);
});

interface Phase {
  count: number;
  error?: string;
}

// A throw counts as the phase's error too, so it cannot skip the other phase either.
async function phase(run: () => Promise<Phase>): Promise<Phase> {
  try {
    return await run();
  } catch (e) {
    return { count: 0, error: String(e) };
  }
}

// The trigger of shared_entries refuses a path only for a day older than current_date - 8, one night
// past this cut, so a photo that gets in is one this cut will reach. The author's day is local and
// this one is UTC, so a photo lives between about 6.5 and 8.7 days; the hard limit by age of the file
// is the sweep below.
async function purgeRows(db: SupabaseClient): Promise<Phase> {
  const cutoff = new Date(Date.now() - PHOTO_TTL_DAYS * 86_400_000).toISOString().slice(0, 10);
  let count = 0;

  for (let round = 0; round < MAX_ROUNDS; round++) {
    const { data, error } = await db
      .from("shared_entries")
      .select("author, day, photo_path")
      .not("photo_path", "is", null)
      .lt("day", cutoff)
      .limit(BATCH);
    if (error) return { count, error: error.message };
    if (!data.length) break;

    // The file first, so a path is never cleared while its bytes are still there to be read.
    const removed = await db.storage.from("photos").remove(data.map((r) => r.photo_path!));
    if (removed.error) return { count, error: removed.error.message };
    // A failed update stops the phase: the row would match the select again, and the failure would be
    // a silent loop instead of a 500 in the function logs.
    for (const r of data) {
      const cleared = await db.from("shared_entries").update({ photo_path: null }).eq("author", r.author).eq("day", r.day);
      if (cleared.error) return { count, error: cleared.error.message };
    }
    count += data.length;
  }
  return { count };
}

// Files no row points to (a row deleted from the dashboard, an upload whose row never came, an
// account gone with a photo in flight) and anything older than the hard limit. The database says
// which; only the Storage API deletes the bytes.
async function sweepOrphans(db: SupabaseClient): Promise<Phase> {
  let count = 0;

  for (let round = 0; round < MAX_ROUNDS; round++) {
    const { data, error } = await db.rpc("orphan_photos").limit(BATCH);
    if (error) return { count, error: error.message };
    if (!data.length) break;

    const removed = await db.storage.from("photos").remove(data);
    if (removed.error) return { count, error: removed.error.message };
    // Nothing removed means the same names come back on the next call.
    if (!removed.data.length) return { count, error: "orphan photos could not be removed" };
    count += removed.data.length;
  }
  return { count };
}
