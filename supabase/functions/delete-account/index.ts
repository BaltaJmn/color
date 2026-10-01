// Deleting an account from the app (Apple 5.1.1(v), Google Play). Everything shared goes: the
// photos in the bucket by hand, and the rest in cascade from auth.users. The journal on the phone
// is not the server's to touch.
import { createClient } from "jsr:@supabase/supabase-js@2";

Deno.serve(async (req) => {
  const jwt = req.headers.get("Authorization")?.replace("Bearer ", "");
  if (!jwt) return new Response(null, { status: 401 });

  const admin = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);
  // Whoever the token says, and only them: the body carries nothing to trust.
  const { data: user, error } = await admin.auth.getUser(jwt);
  if (error || !user.user) return new Response(null, { status: 401 });
  const id = user.user.id;

  while (true) {
    const { data: files, error: listed } = await admin.storage.from("photos").list(id, { limit: 100 });
    // A failed listing is not an empty folder: saying the account is gone with its photos still in
    // the bucket would break the promise of the deletion page.
    if (listed) return new Response(listed.message, { status: 500 });
    if (!files?.length) break;
    const removed = await admin.storage.from("photos").remove(files.map((f) => `${id}/${f.name}`));
    if (removed.error) return new Response(removed.error.message, { status: 500 });
    // A folder entry, or a file the API will not delete, would be listed again on every pass. Each
    // pass that removes something shrinks the folder, so this is the only guard the loop needs.
    if (!removed.data.length) return new Response("could not empty the photo folder", { status: 500 });
  }
  // Reports about this person have no foreign key (the author may be gone before the report is
  // read), so they go by hand too, and before the user: once the user is gone the retry cannot
  // authenticate, and the reports would stay for good.
  const reports = await admin.from("reports").delete().eq("author", id);
  if (reports.error) return new Response(reports.error.message, { status: 500 });

  // A photo that another device uploads between the last listing and this call has no row and no
  // owner left; the orphan sweep of purge-photos removes it.
  const deleted = await admin.auth.admin.deleteUser(id);
  if (deleted.error) return new Response(deleted.error.message, { status: 500 });
  return new Response(null, { status: 204 });
});
