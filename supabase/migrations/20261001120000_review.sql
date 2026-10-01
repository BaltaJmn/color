-- Chroma v1.1, review before launch. A new file and not an edit of the earlier ones: a database that
-- already applied them gets these too.
--
-- One more secret in the Vault, set once by hand (store/servidor.md), next to project_url and
-- service_role_key, which the nightly purge already reads:
--   select vault.create_secret('<same value as REPORT_WEBHOOK_SECRET>', 'report_webhook_secret');

-- --- 1. nobody asks about a pair of uuids ------------------------------------------------------

-- is_friend(x, y) and is_blocked(x, y) answered for any pair to anyone signed in, so a blocked person
-- could ask whether they were. They come back without x: they compare with the caller. is_blocked
-- stays symmetric (a block cuts both ways), and for that reason nobody signed in runs it: only the
-- functions below use it, as their owner.
drop policy profiles_read on public.profiles;
drop policy entries_read on public.shared_entries;
drop policy reports_insert on public.reports;
drop policy photos_read on storage.objects;
drop policy photos_insert on storage.objects;
drop policy photos_update on storage.objects;
-- photos_read looks at the rows now and not at the folder, so can_see_folder has no caller left.
drop function public.can_see_folder(text);
drop function public.is_friend(uuid, uuid);
drop function public.is_blocked(uuid, uuid);

create function public.is_blocked(y uuid) returns boolean
language sql stable security definer set search_path = '' as $$
  select exists (
    select 1 from public.blocks k
    where (k.blocker = auth.uid() and k.blocked = y) or (k.blocker = y and k.blocked = auth.uid())
  );
$$;

-- Friends: an accepted friendship and no block either way. A false says nothing about which of
-- the two it was, so this one is safe to hand to the caller.
create function public.is_friend(y uuid) returns boolean
language sql stable security definer set search_path = '' as $$
  select exists (
    select 1 from public.friendships f
    where f.a = least(auth.uid(), y) and f.b = greatest(auth.uid(), y) and f.status = 'accepted'
  ) and not public.is_blocked(y);
$$;

-- Whoever has a request open with me and no block between us: accepting needs to know who asks.
-- Safe for the same reason: a block deletes the request, and a false is just a false.
create function public.has_open_request(y uuid) returns boolean
language sql stable security definer set search_path = '' as $$
  select exists (
    select 1 from public.friendships f
    where f.a = least(auth.uid(), y) and f.b = greatest(auth.uid(), y) and f.status = 'pending'
  ) and not public.is_blocked(y);
$$;

create policy profiles_read on public.profiles for select to authenticated using (
  id = auth.uid() or public.is_friend(id) or public.has_open_request(id)
);
create policy entries_read on public.shared_entries for select to authenticated
  using (author = auth.uid() or public.is_friend(author));
-- Only about a friend's card, and never readable back: a report is for the developer, not a log.
create policy reports_insert on public.reports for insert to authenticated
  with check (reporter = auth.uid() and public.is_friend(author));

-- --- 2. invitations -------------------------------------------------------------------------------

-- A code is 40 bits, so what keeps it from being walked is how many misses one account gets.
create table public.invite_attempts (
  user_id uuid not null references auth.users on delete cascade,
  created_at timestamptz not null default now()
);
create index invite_attempts_user on public.invite_attempts (user_id, created_at);
alter table public.invite_attempts enable row level security;
revoke all on public.invite_attempts from anon, authenticated;

-- 'sent', 'accepted' (the other had already asked), 'already', 'self', 'blocked' (I blocked them),
-- 'not_found', 'limit' (50 friends, or 100 requests waiting for the other), 'too_many'.
create or replace function public.request_friend(code text) returns text
language plpgsql security definer set search_path = '' as $$
declare
  me uuid := auth.uid();
  other uuid;
  existing public.friendships;
begin
  if me is null then raise exception 'not_signed_in'; end if;
  if (select count(*) from public.invite_attempts where user_id = me and created_at > now() - interval '1 hour') >= 20 then
    return 'too_many';
  end if;

  select id into other from public.profiles where invite_code = lower(trim(code));
  -- A code that does not exist and a code whose owner blocked me answer the same and count the same:
  -- the answer must not tell someone they were blocked.
  if other is null or exists (select 1 from public.blocks where blocker = other and blocked = me) then
    insert into public.invite_attempts (user_id) values (me);
    return 'not_found';
  end if;
  if other = me then return 'self'; end if;
  if exists (select 1 from public.blocks where blocker = me and blocked = other) then return 'blocked'; end if;

  select * into existing from public.friendships where a = least(me, other) and b = greatest(me, other);
  if not found then
    if public.accepted_count(me) >= 50 then return 'limit'; end if;
    if (
      select count(*) from public.friendships
      where (a = other or b = other) and status = 'pending' and requested_by <> other
    ) >= 100 then
      return 'limit';
    end if;
    begin
      insert into public.friendships (a, b, requested_by, status)
      values (least(me, other), greatest(me, other), me, 'pending');
      return 'sent';
    exception when unique_violation then
      -- The other asked at the same moment: go on as if the row had been there to begin with.
      select * into existing from public.friendships where a = least(me, other) and b = greatest(me, other);
      if not found then return 'not_found'; end if;
    end;
  end if;

  if existing.status = 'accepted' then return 'already'; end if;
  if existing.requested_by = me then return 'sent'; end if;
  begin
    update public.friendships set status = 'accepted' where a = existing.a and b = existing.b;
  exception when raise_exception then
    return 'limit';
  end;
  return 'accepted';
end;
$$;

-- A block that raced a request leaves a pending row behind: it must not become a friendship.
create or replace function public.accept_friend(other uuid) returns void
language plpgsql security definer set search_path = '' as $$
begin
  update public.friendships set status = 'accepted'
  where a = least(auth.uid(), other) and b = greatest(auth.uid(), other)
    and requested_by = other and status = 'pending'
    and not public.is_blocked(other);
end;
$$;

-- --- 3. lists and blocks --------------------------------------------------------------------------

-- Friends and requests, pending and accepted, with the name already joined: the app used to ask for the
-- names with an id list in the URL, which does not survive a few hundred requests. Anyone blocked,
-- whichever way, is not here.
create function public.my_friendships()
returns table (id uuid, display_name text, status text, requested_by uuid)
language sql stable security definer set search_path = '' as $$
  select p.id, p.display_name, f.status, f.requested_by
  from public.friendships f
  join public.profiles p on p.id = case when f.a = auth.uid() then f.b else f.a end
  where auth.uid() in (f.a, f.b) and not public.is_blocked(p.id);
$$;

-- Only the people I blocked: a name has no policy of its own to read it through.
create function public.my_blocks()
returns table (id uuid, display_name text)
language sql stable security definer set search_path = '' as $$
  select p.id, p.display_name
  from public.blocks k
  join public.profiles p on p.id = k.blocked
  where k.blocker = auth.uid();
$$;

-- Undoing a block does not bring the friendship back: the row went when the block came.
create function public.unblock_user(other uuid) returns void
language sql security definer set search_path = '' as $$
  delete from public.blocks where blocker = auth.uid() and blocked = other;
$$;

-- --- 4. shared days ----------------------------------------------------------------------------

-- No day before the app existed or beyond what a time zone can explain, and no photo for a day the
-- purge would already have emptied.
-- The range is checked when a day is written, not on every update: a row that was already out of it
-- (before this trigger existed) must still let purge-photos clear its photo_path.
-- The photo rule has one night of slack: current_date is UTC, and the app uploads for days up to 7 old
-- on the local logical day, which west of UTC is one date behind.
create function public.check_entry_day() returns trigger
language plpgsql set search_path = '' as $$
begin
  if tg_op = 'INSERT' or new.day is distinct from old.day then
    if new.day < date '2026-01-01' or new.day > current_date + 2 then
      raise exception 'day_out_of_range';
    end if;
  end if;
  if new.day < current_date - 8 then
    new.photo_path := null;
  end if;
  return new;
end;
$$;

create trigger entry_day before insert or update on public.shared_entries
for each row execute function public.check_entry_day();

create index shared_entries_photo_path on public.shared_entries (photo_path) where photo_path is not null;

-- --- 5. photos --------------------------------------------------------------------------------

-- Read: my own folder, always (the file goes up before the row that points at it), and a friend's
-- only for a photo some row of theirs points at. A file without a row, such as one left by a card
-- that was removed, is nobody's to read.
-- objects.name and not name inside the subquery: shared_entries has a name column too.
create policy photos_read on storage.objects for select to authenticated using (
  bucket_id = 'photos' and (
    (storage.foldername(name))[1] = auth.uid()::text
    or exists (
      select 1 from public.shared_entries e
      where e.photo_path = objects.name and public.is_friend(e.author)
    )
  )
);

-- Write: <uid>/<YYYY-MM-DD>.jpg, with a day from ten days ago to two ahead. Few enough names that
-- an account cannot fill the bucket, and the ones the purge cannot reach by row (a day in 2099)
-- are not allowed at all. The dates are compared as text: all of them have the same shape.
create policy photos_insert on storage.objects for insert to authenticated with check (
  bucket_id = 'photos'
  and name ~ ('^' || auth.uid()::text || '/[0-9]{4}-[0-9]{2}-[0-9]{2}\.jpg$')
  and substring(name from '/([0-9]{4}-[0-9]{2}-[0-9]{2})\.jpg$')
      between to_char(current_date - 10, 'YYYY-MM-DD') and to_char(current_date + 2, 'YYYY-MM-DD')
);
create policy photos_update on storage.objects for update to authenticated
  using (bucket_id = 'photos' and (storage.foldername(name))[1] = auth.uid()::text)
  with check (
    bucket_id = 'photos'
    and name ~ ('^' || auth.uid()::text || '/[0-9]{4}-[0-9]{2}-[0-9]{2}\.jpg$')
    and substring(name from '/([0-9]{4}-[0-9]{2}-[0-9]{2})\.jpg$')
        between to_char(current_date - 10, 'YYYY-MM-DD') and to_char(current_date + 2, 'YYYY-MM-DD')
  );

-- What purge-photos sweeps besides the photos of rows: files no row points at after a day (a card
-- removed from the dashboard, an account deleted, a failed upload), and anything past eight days
-- whatever points at it. Called with the service key, which Storage listing needs and PostgREST
-- does not expose for storage. At most 1000 names a call: ask again until it is empty.
create function public.orphan_photos() returns setof text
language sql stable security definer set search_path = '' as $$
  select o.name
  from storage.objects o
  where o.bucket_id = 'photos'
    and (
      o.created_at < now() - interval '8 days'
      or (
        o.created_at < now() - interval '1 day'
        and not exists (select 1 from public.shared_entries e where e.photo_path = o.name)
      )
    )
  order by o.created_at
  limit 1000;
$$;

-- --- 6. reports -------------------------------------------------------------------------------

alter table public.reports add column notified_at timestamptz;
-- Whatever was reported before this file went out by the webhook of the dashboard: not again.
update public.reports set notified_at = created_at;
delete from public.reports a using public.reports b
where a.reporter = b.reporter and a.author = b.author and a.day = b.day and a.id > b.id;
-- One report per person and card. The app takes a 23505 as a report already made.
alter table public.reports add constraint reports_once unique (reporter, author, day);

-- At most 20 a day per person: with the unique key above, a friend cannot ring the inbox 500 times.
-- Security definer because reports are not readable by anybody signed in.
create function public.limit_reports() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
  if (select count(*) from public.reports r where r.reporter = new.reporter and r.created_at > now() - interval '1 day') >= 20 then
    raise exception 'report_limit';
  end if;
  return new;
end;
$$;

create trigger report_cap before insert on public.reports
for each row execute function public.limit_reports();

-- The mail to the developer is sent from here and not from a webhook set up in the dashboard, so a
-- new project cannot forget it. report-notify answers 200 once the mail is out and fills
-- notified_at; a report still without it after ten minutes is sent again every hour. A failure here
-- never costs the person their report: it is logged, and the hourly retry picks it up.
create function public.notify_report(report_id bigint) returns void
language plpgsql security definer set search_path = '' as $$
declare
  rec public.reports;
  project_url text;
  hook_secret text;
begin
  select * into rec from public.reports where id = report_id;
  if not found then return; end if;
  select decrypted_secret into project_url from vault.decrypted_secrets where name = 'project_url';
  select decrypted_secret into hook_secret from vault.decrypted_secrets where name = 'report_webhook_secret';
  if project_url is null or hook_secret is null then
    raise warning 'notify_report %: the Vault needs project_url and report_webhook_secret', report_id;
    return;
  end if;
  perform net.http_post(
    url => project_url || '/functions/v1/report-notify',
    headers => jsonb_build_object('Content-Type', 'application/json', 'x-webhook-secret', hook_secret),
    body => jsonb_build_object('type', 'INSERT', 'table', 'reports', 'schema', 'public', 'record', to_jsonb(rec)),
    timeout_milliseconds => 10000
  );
exception when others then
  raise warning 'notify_report %: %', report_id, sqlerrm;
end;
$$;

create function public.on_report_insert() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
  perform public.notify_report(new.id);
  return null;
end;
$$;

create trigger report_notify after insert on public.reports
for each row execute function public.on_report_insert();

-- --- 7. schedule ------------------------------------------------------------------------------

select cron.schedule(
  'report-retry',
  '41 * * * *',
  $$
  select public.notify_report(id) from public.reports
  where notified_at is null and created_at < now() - interval '10 minutes'
  order by id limit 50;
  $$
);

select cron.schedule(
  'invite-attempts-trim',
  '31 3 * * *',
  $$ delete from public.invite_attempts where created_at < now() - interval '1 day'; $$
);

-- The purge again, with two minutes to answer instead of the five seconds pg_net gives by default:
-- the function lists and deletes in batches.
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
    body := '{}'::jsonb,
    timeout_milliseconds := 120000
  );
  $$
);

-- --- 8. who may call what -----------------------------------------------------------------------

-- Supabase grants execute on every new function in public to anon and authenticated, so each one
-- says its own: nobody for what only triggers, the schedule and other functions use, the service
-- role for the sweep, and signed in people for what the app and the policies call.
revoke execute on function
  public.is_blocked(uuid), public.accepted_count(uuid), public.enforce_friend_limit(), public.touch_updated_at(),
  public.check_entry_day(), public.limit_reports(), public.on_report_insert(), public.notify_report(bigint)
from public, anon, authenticated;

revoke execute on function public.orphan_photos() from public, anon, authenticated;
grant execute on function public.orphan_photos() to service_role;

revoke execute on function
  public.request_friend(text), public.accept_friend(uuid), public.decline_friend(uuid), public.remove_friend(uuid),
  public.block_user(uuid), public.regenerate_code(), public.my_profile(), public.is_friend(uuid),
  public.has_open_request(uuid), public.my_friendships(), public.my_blocks(), public.unblock_user(uuid)
from public, anon;
grant execute on function
  public.request_friend(text), public.accept_friend(uuid), public.decline_friend(uuid), public.remove_friend(uuid),
  public.block_user(uuid), public.regenerate_code(), public.my_profile(), public.is_friend(uuid),
  public.has_open_request(uuid), public.my_friendships(), public.my_blocks(), public.unblock_user(uuid)
to authenticated;
