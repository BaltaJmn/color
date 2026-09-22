// Every night, from pg_cron (migrations/20260923120100_purge.sql): shared photos live seven days.
// The colors stay; the feed only ever shows today and yesterday, and a friend's mosaic is colors.
import { createClient } from "jsr:@supabase/supabase-js@2";

const PHOTO_TTL_DAYS = 7;
const BATCH = 100;

Deno.serve(async (req) => {
  const key = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
  // Only the schedule calls it. Anyone else gets nothing, not even a count.
  if (req.headers.get("Authorization") !== `Bearer ${key}`) return new Response(null, { status: 401 });

  const db = createClient(Deno.env.get("SUPABASE_URL")!, key);
  const cutoff = new Date(Date.now() - PHOTO_TTL_DAYS * 86_400_000).toISOString().slice(0, 10);
  let purged = 0;

  while (true) {
    const { data, error } = await db
      .from("shared_entries")
      .select("author, day, photo_path")
      .not("photo_path", "is", null)
      .lt("day", cutoff)
      .limit(BATCH);
    if (error) return new Response(error.message, { status: 500 });
    if (!data.length) break;

    // The file first: a row without a path and a file left behind would never be found again.
    const removed = await db.storage.from("photos").remove(data.map((r) => r.photo_path!));
    if (removed.error) return new Response(removed.error.message, { status: 500 });
    for (const r of data) {
      await db.from("shared_entries").update({ photo_path: null }).eq("author", r.author).eq("day", r.day);
    }
    purged += data.length;
  }

  return Response.json({ purged });
});
