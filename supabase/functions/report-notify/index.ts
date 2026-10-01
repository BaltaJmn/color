// Database webhook on insert into reports. Apple (guideline 1.2) wants reports acted on within a
// day, so each one arrives as an email to the developer with what is needed to judge it.
import { createClient } from "jsr:@supabase/supabase-js@2";

interface Report {
  id: number;
  reporter: string;
  author: string;
  day: string;
}

Deno.serve(async (req) => {
  // The database sends this header from the Vault; nothing else may make the developer's inbox ring.
  // Without the variable there is nothing to compare with, so an empty header must not get in.
  const secret = Deno.env.get("REPORT_WEBHOOK_SECRET");
  if (!secret || req.headers.get("x-webhook-secret") !== secret) return new Response(null, { status: 401 });
  // Better a 500 that names the cause than a mail to [undefined] that Resend refuses.
  const to = Deno.env.get("REPORT_TO");
  if (!to) return new Response("REPORT_TO is not set", { status: 500 });

  const { record } = (await req.json()) as { record?: Report };
  if (!record?.id) return new Response(null, { status: 400 });
  const db = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);

  const { data: entry } = await db
    .from("shared_entries")
    .select("color, name, word, photo_path")
    .eq("author", record.author)
    .eq("day", record.day)
    .maybeSingle();
  const { data: people } = await db.from("profiles").select("id, display_name").in("id", [record.author, record.reporter]);
  const nameOf = (id: string) => people?.find((p) => p.id === id)?.display_name ?? "(deleted)";

  // A day to look at the photo, which is the time Apple gives to act.
  let photo = "none";
  if (entry?.photo_path) {
    const signed = await db.storage.from("photos").createSignedUrl(entry.photo_path, 86_400);
    photo = signed.data?.signedUrl ?? "could not sign";
  }

  const text = [
    `Report #${record.id}`,
    `Author: ${nameOf(record.author)} (${record.author})`,
    `Reported by: ${nameOf(record.reporter)} (${record.reporter})`,
    `Day: ${record.day}`,
    `Color: ${entry?.color ?? "-"} (${entry?.name ?? "-"})`,
    `Word: ${entry?.word ?? "-"}`,
    `Photo (24 h): ${photo}`,
    "",
    "Act within 24 hours, from the Supabase dashboard: delete the shared_entries row and its photo in Storage, or the account.",
    "A photo left in Storage is only removed by the nightly purge.",
  ].join("\n");

  const sent = await fetch("https://api.resend.com/emails", {
    method: "POST",
    headers: { Authorization: `Bearer ${Deno.env.get("RESEND_API_KEY")}`, "Content-Type": "application/json" },
    body: JSON.stringify({
      from: Deno.env.get("REPORT_FROM") ?? "Chroma <reports@baltajmn.dev>",
      to: [to],
      subject: `Chroma: report #${record.id}`,
      text,
    }),
  });
  // Not delivered stays without notified_at, and the hourly retry in the database sends it again.
  if (!sent.ok) return new Response(null, { status: 502 });

  const marked = await db.from("reports").update({ notified_at: new Date().toISOString() }).eq("id", record.id);
  // The mail is out already: failing here only means the retry may send it a second time.
  if (marked.error) return new Response(marked.error.message, { status: 500 });
  return new Response(null, { status: 200 });
});
