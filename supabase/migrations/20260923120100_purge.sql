-- PHOTO_TTL_DAYS = 7: every night the Edge Function purge-photos deletes the shared photos older
-- than a week and leaves their colors. It is called through pg_net because only the Storage API
-- deletes the file itself; a delete on storage.objects would leave the bytes behind.
--
-- Needs two secrets in the Vault, set once by hand (store/servidor.md):
--   select vault.create_secret('https://<ref>.supabase.co', 'project_url');
--   select vault.create_secret('<service role key>', 'service_role_key');

create extension if not exists pg_cron;
create extension if not exists pg_net;

select cron.schedule(
  'purge-photos',
  '17 3 * * *',
  $$
  select net.http_post(
    url := (select decrypted_secret from vault.decrypted_secrets where name = 'project_url') || '/functions/v1/purge-photos',
    headers := jsonb_build_object(
      'Authorization', 'Bearer ' || (select decrypted_secret from vault.decrypted_secrets where name = 'service_role_key'),
      'Content-Type', 'application/json'
    ),
    body := '{}'::jsonb
  );
  $$
);
