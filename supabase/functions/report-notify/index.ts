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
  // The webhook is configured to send this header; nothing else may make the developer's inbox ring.
  if (req.headers.get("x-webhook-secret") !== Deno.env.get("REPORT_WEBHOOK_SECRET")) {
    return new Response(null, { status: 401 });
  }
  const { record } = (await req.json()) as { record: Report };
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
    "Act within 24 hours: remove the content or the account from the Supabase dashboard.",
  ].join("\n");

  const sent = await fetch("https://api.resend.com/emails", {
    method: "POST",
    headers: { Authorization: `Bearer ${Deno.env.get("RESEND_API_KEY")}`, "Content-Type": "application/json" },
    body: JSON.stringify({
      from: Deno.env.get("REPORT_FROM") ?? "Chroma <reports@baltajmn.dev>",
      to: [Deno.env.get("REPORT_TO")],
      subject: `Chroma: report #${record.id}`,
      text,
    }),
  });
  return new Response(null, { status: sent.ok ? 200 : 502 });
});
