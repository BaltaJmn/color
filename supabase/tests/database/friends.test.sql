-- Test 17 (docs/tecnico.md 10): without friendship nothing is read, a block cuts at once, the
-- limit of 50 holds, invite codes stay with their owner, and a card only shows its author's photo.
-- It also holds the doors the review closed: who may call what, the names of the photos, the
-- days a card may carry, the reports and the call they queue in pg_net, the invitations, the guards of
-- a block with the friendship row still there, and the sweep of orphan photos.
-- Run with: supabase test db
begin;
create extension if not exists pgtap with schema extensions;
select plan(90);

-- Four people. auth.users is the only table the test writes as its owner.
insert into auth.users (id, email) values
  ('00000000-0000-0000-0000-00000000000a', 'a@test'),
  ('00000000-0000-0000-0000-00000000000b', 'b@test'),
  ('00000000-0000-0000-0000-00000000000c', 'c@test'),
  ('00000000-0000-0000-0000-00000000000d', 'd@test');
insert into public.profiles (id, display_name, invite_code) values
  ('00000000-0000-0000-0000-00000000000a', 'Ana', 'aaaaaaaaaa'),
  ('00000000-0000-0000-0000-00000000000b', 'Bea', 'bbbbbbbbbb'),
  ('00000000-0000-0000-0000-00000000000c', 'Carla', 'cccccccccc'),
  ('00000000-0000-0000-0000-00000000000d', 'Dani', 'dddddddddd');

-- Four files in the bucket, written as the owner of the storage tables. The first is the one Ana's card
-- will point at; the second has no card and is two days old; the third has none and is new; the
-- fourth will have a card (Carla's) and is nine days old.
insert into storage.objects (bucket_id, name, created_at) values
  ('photos', '00000000-0000-0000-0000-00000000000a/2026-01-01.jpg', now()),
  ('photos', '00000000-0000-0000-0000-00000000000a/2026-01-02.jpg', now() - interval '2 days'),
  ('photos', '00000000-0000-0000-0000-00000000000b/2026-01-03.jpg', now()),
  ('photos', '00000000-0000-0000-0000-00000000000c/2026-01-04.jpg', now() - interval '9 days');

-- Ana shares today.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
insert into public.shared_entries (author, day, color, name) values
  ('00000000-0000-0000-0000-00000000000a', current_date, '#3A6EA5', 'storm_blue');
select is((select invite_code from public.my_profile()), 'aaaaaaaaaa', 'the owner reads their own code');
select lives_ok(
  $$ update public.shared_entries set photo_path = '00000000-0000-0000-0000-00000000000a/2026-01-01.jpg' where author = auth.uid() $$,
  'a card may point at its own photo'
);
select throws_ok(
  $$ update public.shared_entries set photo_path = '00000000-0000-0000-0000-00000000000b/2026-01-01.jpg' where author = auth.uid() $$,
  '23514', null, 'a card cannot point at someone else''s photo'
);
select lives_ok(
  format($$ insert into storage.objects (bucket_id, name) values ('photos', '00000000-0000-0000-0000-00000000000a/%s.jpg') $$, to_char(current_date, 'YYYY-MM-DD')),
  'a photo goes up under the name of a day, in its own folder'
);
select throws_ok(
  format($$ insert into storage.objects (bucket_id, name) values ('photos', '00000000-0000-0000-0000-00000000000b/%s.jpg') $$, to_char(current_date, 'YYYY-MM-DD')),
  '42501', null, 'nobody uploads into another''s folder'
);
select throws_ok(
  $$ insert into storage.objects (bucket_id, name) values ('photos', '00000000-0000-0000-0000-00000000000a/photo.jpg') $$,
  '42501', null, 'a name that is not a day is refused'
);
select throws_ok(
  $$ insert into storage.objects (bucket_id, name) values ('photos', '00000000-0000-0000-0000-00000000000a/2099-01-01.jpg') $$,
  '42501', null, 'nor a day the purge would never reach'
);
select throws_ok(
  format($$ update storage.objects set name = '00000000-0000-0000-0000-00000000000a/photo.jpg' where name = '00000000-0000-0000-0000-00000000000a/%s.jpg' $$, to_char(current_date, 'YYYY-MM-DD')),
  '42501', null, 'a file cannot be renamed out of the pattern'
);

-- Bea is nobody to Ana yet.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is((select count(*) from public.shared_entries)::int, 0, 'without friendship nothing is read');
select is((select count(*) from public.profiles where display_name = 'Ana')::int, 0, 'nor the profile');
select throws_ok(
  $$ insert into public.shared_entries (author, day, color, name) values ('00000000-0000-0000-0000-00000000000a', current_date, '#000000', 'night') $$,
  '42501', null, 'nobody writes in another''s name'
);
select throws_ok(
  $$ insert into public.friendships (a, b, requested_by, status) values ('00000000-0000-0000-0000-00000000000a', '00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000b', 'accepted') $$,
  '42501', null, 'friendships only change through the functions'
);

-- Bea opens Ana's link: a request, still nothing to read.
select is(public.request_friend('aaaaaaaaaa'), 'sent', 'a link makes a request');
select is((select count(*) from public.shared_entries)::int, 0, 'a request reads nothing');
select throws_ok($$ select invite_code from public.profiles $$, '42501', null, 'nobody reads another''s invite code');
select is(
  (select count(*) from storage.objects where bucket_id = 'photos' and name like '00000000-0000-0000-0000-00000000000a/%')::int,
  0, 'a request reads no photo either'
);

-- Ana sees the request with the name already joined, and accepts.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select is(
  (select display_name || ':' || status from public.my_friendships() where id = '00000000-0000-0000-0000-00000000000b'),
  'Bea:pending', 'a request comes with the name of who asks'
);
select is(
  (select display_name from public.profiles where id = '00000000-0000-0000-0000-00000000000b'),
  'Bea', 'and their profile can be read while the request is open'
);
select public.accept_friend('00000000-0000-0000-0000-00000000000b');
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is((select count(*) from public.shared_entries)::int, 1, 'a friend reads the day');
-- Of Ana's three files, only the one her card points at: not the one without a card, nor the one just uploaded.
select is(
  (select count(*) from storage.objects where bucket_id = 'photos' and name like '00000000-0000-0000-0000-00000000000a/%')::int,
  1, 'a friend reads the photo a card points at, and no other'
);
select is(
  (select status from public.my_friendships() where id = '00000000-0000-0000-0000-00000000000a'),
  'accepted', 'a friendship is listed once accepted'
);

-- A friend reports a card: once, never twice, and not the column that says the mail went. First with
-- the Vault still empty, as in a project that forgot its secrets: the report is kept all the same and
-- nothing is queued; the hourly retry picks it up once they are there.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select lives_ok(
  $$ insert into public.reports (reporter, author, day) values ('00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000a', current_date - 45) $$,
  'a report is kept even when the Vault has nothing to ring with'
);
reset role;
select is(
  (select count(*) from net.http_request_queue where url like '%/report-notify')::int,
  0, 'and nothing is queued'
);
-- Then the Vault holds what notify_report needs to ring report-notify, as in a deployed project, so
-- the call that the trigger queues in pg_net can be read back.
do $$
begin
  perform vault.create_secret('https://x.supabase.co', 'project_url');
  perform vault.create_secret('s3cret', 'report_webhook_secret');
end;
$$;
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select lives_ok(
  $$ insert into public.reports (reporter, author, day) values ('00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000a', current_date) $$,
  'a friend can report a card'
);
reset role;
select is(
  (select count(*) from net.http_request_queue where url = 'https://x.supabase.co/functions/v1/report-notify')::int,
  1, 'a report queues one call to report-notify'
);
select is(
  (select headers ->> 'x-webhook-secret' from net.http_request_queue
   where url = 'https://x.supabase.co/functions/v1/report-notify' order by id limit 1),
  's3cret', 'with the secret of the Vault in the header'
);
select is(
  (select convert_from(body, 'UTF8')::jsonb #>> '{record,id}' from net.http_request_queue
   where url = 'https://x.supabase.co/functions/v1/report-notify' order by id limit 1),
  (select id::text from public.reports
   where reporter = '00000000-0000-0000-0000-00000000000b' and author = '00000000-0000-0000-0000-00000000000a' and day = current_date),
  'and the report in the body'
);
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select throws_ok(
  $$ insert into public.reports (reporter, author, day) values ('00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000a', current_date) $$,
  '23505', null, 'the same card is reported once'
);
select throws_ok(
  $$ insert into public.reports (reporter, author, day) values ('00000000-0000-0000-0000-00000000000c', '00000000-0000-0000-0000-00000000000a', current_date - 50) $$,
  '42501', null, 'nobody reports in the name of another'
);
select throws_ok(
  $$ insert into public.reports (reporter, author, day, notified_at) values ('00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000a', current_date - 40, now()) $$,
  '42501', null, 'nobody marks their own report as sent'
);
-- Twenty in all: the one with the empty Vault, the first with it full, and these are the other eighteen.
do $$
begin
  for i in 1..18 loop
    insert into public.reports (reporter, author, day)
    values (auth.uid(), '00000000-0000-0000-0000-00000000000a'::uuid, current_date - i);
  end loop;
end;
$$;
select throws_ok(
  $$ insert into public.reports (reporter, author, day) values ('00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000a', current_date - 30) $$,
  'P0001', 'report_limit', 'twenty reports a day is the most'
);

-- The hourly retry runs the command that is scheduled. Of the twenty reports, none marked: one is
-- eleven minutes old, and goes out again; another is twelve minutes old but marked as sent, and does
-- not. Nineteen were queued above (the report with the empty Vault queued nothing), so this is the 20th.
reset role;
update public.reports set created_at = now() - interval '11 minutes'
where reporter = '00000000-0000-0000-0000-00000000000b' and author = '00000000-0000-0000-0000-00000000000a' and day = current_date;
update public.reports set created_at = now() - interval '12 minutes', notified_at = now()
where reporter = '00000000-0000-0000-0000-00000000000b' and author = '00000000-0000-0000-0000-00000000000a' and day = current_date - 1;
do $$
begin
  execute (select command from cron.job where jobname = 'report-retry');
end;
$$;
select is(
  (select count(*) from net.http_request_queue where url = 'https://x.supabase.co/functions/v1/report-notify')::int,
  20, 'the retry sends again only a report that is ten minutes old and not marked'
);
-- Twenty in a day and not in an hour: two hours later the next one is refused still.
update public.reports set created_at = now() - interval '2 hours' where reporter = '00000000-0000-0000-0000-00000000000b';
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select throws_ok(
  $$ insert into public.reports (reporter, author, day) values ('00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000a', current_date - 31) $$,
  'P0001', 'report_limit', 'the twenty are counted over a day, not an hour'
);

-- Carla, still a stranger, reads nothing even with the path of the photo.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000c", "role": "authenticated"}';
select is(
  (select count(*) from storage.objects where bucket_id = 'photos' and name like '00000000-0000-0000-0000-00000000000a/%')::int,
  0, 'a stranger cannot read the photos'
);
select throws_ok(
  $$ insert into public.reports (reporter, author, day) values ('00000000-0000-0000-0000-00000000000c', '00000000-0000-0000-0000-00000000000a', current_date) $$,
  '42501', null, 'nor report a card that is not a friend''s'
);
-- The days a card may carry.
select throws_ok(
  $$ insert into public.shared_entries (author, day, color, name) values ('00000000-0000-0000-0000-00000000000c', current_date + 3, '#7A8B3C', 'moss') $$,
  'P0001', 'day_out_of_range', 'a card cannot be from the future'
);
select throws_ok(
  $$ insert into public.shared_entries (author, day, color, name) values ('00000000-0000-0000-0000-00000000000c', '2025-12-31', '#7A8B3C', 'moss') $$,
  'P0001', 'day_out_of_range', 'nor from before the app'
);
select lives_ok(
  $$ insert into public.shared_entries (author, day, color, name) values ('00000000-0000-0000-0000-00000000000c', current_date + 2, '#7A8B3C', 'moss') $$,
  'but two days ahead is accepted: east of UTC it is tomorrow'
);
select lives_ok(
  $$ insert into public.shared_entries (author, day, color, name, photo_path) values ('00000000-0000-0000-0000-00000000000c', current_date - 9, '#7A8B3C', 'moss', '00000000-0000-0000-0000-00000000000c/2026-01-05.jpg') $$,
  'a day older than a week is accepted'
);
select is(
  (select photo_path from public.shared_entries where author = '00000000-0000-0000-0000-00000000000c' and day = current_date - 9),
  null::text, 'but only its color: the photo is dropped'
);
-- One night of slack: current_date is UTC, and west of it the app is still on the seventh day.
select lives_ok(
  $$ insert into public.shared_entries (author, day, color, name, photo_path) values ('00000000-0000-0000-0000-00000000000c', current_date - 8, '#7A8B3C', 'moss', '00000000-0000-0000-0000-00000000000c/2026-01-06.jpg') $$,
  'a day eight days ago is accepted too'
);
select is(
  (select photo_path from public.shared_entries where author = '00000000-0000-0000-0000-00000000000c' and day = current_date - 8),
  '00000000-0000-0000-0000-00000000000c/2026-01-06.jpg', 'and keeps its photo for the night'
);
-- The photo rule holds on every write, not only when the day is written: an update that leaves the
-- day alone and points an old card at a file still gets its path dropped.
select lives_ok(
  $$ update public.shared_entries set photo_path = '00000000-0000-0000-0000-00000000000c/2026-01-05.jpg' where author = '00000000-0000-0000-0000-00000000000c' and day = current_date - 9 $$,
  'an old card can be updated'
);
select is(
  (select photo_path from public.shared_entries where author = '00000000-0000-0000-0000-00000000000c' and day = current_date - 9),
  null::text, 'and its photo is dropped again, whatever the update'
);
-- The range is for a day that is written: moving a card out of it is refused.
select throws_ok(
  $$ update public.shared_entries set day = '2025-12-31' where author = '00000000-0000-0000-0000-00000000000c' and day = current_date - 8 $$,
  'P0001', 'day_out_of_range', 'a card cannot be moved to a day before the app'
);
-- A card for today that points at the old file, so that only its age is what sweeps it.
insert into public.shared_entries (author, day, color, name, photo_path) values
  ('00000000-0000-0000-0000-00000000000c', current_date, '#7A8B3C', 'moss', '00000000-0000-0000-0000-00000000000c/2026-01-04.jpg');

-- Ana blocks Bea: it cuts at once, Bea cannot ask again, and is told what a dead link is told.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select public.block_user('00000000-0000-0000-0000-00000000000b');
select is(
  (select display_name from public.my_blocks() where id = '00000000-0000-0000-0000-00000000000b'),
  'Bea', 'the one who blocked lists who by name'
);
select is(public.request_friend('bbbbbbbbbb'), 'blocked', 'and is told so when they open the link of the one they blocked');
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is((select count(*) from public.shared_entries)::int, 0, 'a block cuts at once');
select is(
  (select count(*) from storage.objects where bucket_id = 'photos' and name like '00000000-0000-0000-0000-00000000000a/%')::int,
  0, 'the photos too'
);
select is((select count(*) from public.my_friendships())::int, 0, 'a blocked person is out of the list');
select is((select count(*) from public.my_blocks())::int, 0, 'and lists nobody they blocked');
-- The blocked one tries to lift the block by naming themselves.
select lives_ok(
  $$ select public.unblock_user('00000000-0000-0000-0000-00000000000b') $$,
  'nobody lifts a block they did not make'
);
select is(public.request_friend('aaaaaaaaaa'), 'not_found', 'a blocked person cannot ask again, and cannot tell why');
select throws_ok(
  $$ select public.is_blocked('00000000-0000-0000-0000-00000000000a') $$,
  '42501', null, 'nor ask whether they were blocked'
);

-- Ana unblocks Bea: the friendship does not come back, but they can ask again.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select public.unblock_user('00000000-0000-0000-0000-00000000000b');
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is((select count(*) from public.shared_entries)::int, 0, 'unblocking does not bring the friendship back');
select is(public.request_friend('aaaaaaaaaa'), 'sent', 'but they can ask again');
-- Whoever asked cannot accept their own request.
select public.accept_friend('00000000-0000-0000-0000-00000000000a');
select is(
  (select status from public.my_friendships() where id = '00000000-0000-0000-0000-00000000000a'),
  'pending', 'a request is not accepted by whoever made it'
);

-- The limits: Carla with 50 friends cannot accept one more, and Bea with 100 requests waiting takes no more.
reset role;
insert into auth.users (id, email)
  select ('00000000-0000-0000-0000-' || lpad(to_hex(1000 + i), 12, '0'))::uuid, 'f' || i || '@test' from generate_series(1, 50) i;
insert into public.profiles (id, display_name)
  select ('00000000-0000-0000-0000-' || lpad(to_hex(1000 + i), 12, '0'))::uuid, 'F' || i from generate_series(1, 50) i;
insert into public.friendships (a, b, requested_by, status)
  select '00000000-0000-0000-0000-00000000000c', ('00000000-0000-0000-0000-' || lpad(to_hex(1000 + i), 12, '0'))::uuid,
         '00000000-0000-0000-0000-00000000000c', 'accepted'
  from generate_series(1, 50) i;
insert into auth.users (id, email)
  select ('00000000-0000-0000-0000-' || lpad(to_hex(3000 + i), 12, '0'))::uuid, 'g' || i || '@test' from generate_series(1, 100) i;
insert into public.profiles (id, display_name)
  select ('00000000-0000-0000-0000-' || lpad(to_hex(3000 + i), 12, '0'))::uuid, 'G' || i from generate_series(1, 100) i;
insert into public.friendships (a, b, requested_by, status)
  select '00000000-0000-0000-0000-00000000000b', ('00000000-0000-0000-0000-' || lpad(to_hex(3000 + i), 12, '0'))::uuid,
         ('00000000-0000-0000-0000-' || lpad(to_hex(3000 + i), 12, '0'))::uuid, 'pending'
  from generate_series(1, 100) i;
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select is(public.request_friend('aaaaaaaaaa'), 'self', 'a link to oneself is not a request');
select is(public.request_friend('cccccccccc'), 'sent', 'asking someone who is full is still a request');
select is(public.request_friend('dddddddddd'), 'sent', 'a link makes a request');

-- Dani asks back before Ana's request is answered: that is accepting it. Then Dani walks the codes.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000d", "role": "authenticated"}';
select is(public.request_friend('aaaaaaaaaa'), 'accepted', 'asking back is accepting');
select is((select count(*) from public.my_friendships())::int, 1, 'the list is of the friendships one is in, and no others');
-- Bea's twenty reports above do not count against Dani: the cap is per person.
select lives_ok(
  $$ insert into public.reports (reporter, author, day) values ('00000000-0000-0000-0000-00000000000d', '00000000-0000-0000-0000-00000000000a', current_date) $$,
  'the twenty reports of one person do not stop another''s'
);
select is(public.request_friend('bbbbbbbbbb'), 'limit', 'someone with 100 requests waiting takes no more');
select is(
  (select count(*) from generate_series(1, 20) where public.request_friend('0000000000') = 'not_found')::int,
  20, 'a link that does not exist is not found'
);
select is(public.request_friend('0000000000'), 'too_many', 'twenty misses in an hour and the next one waits');
select is(public.request_friend('aaaaaaaaaa'), 'too_many', 'even for a link that is good');

set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000c", "role": "authenticated"}';
select throws_ok($$ select public.accept_friend('00000000-0000-0000-0000-00000000000a') $$, 'P0001', 'friend_limit', 'the 51st friend is refused');
-- Dani has nothing to do with Carla: what stops this request is Carla's own 50.
select is(public.request_friend('dddddddddd'), 'limit', 'someone with 50 friends cannot ask for one more');
-- Ana asked Carla earlier: asking back would be accepting, and it is the same limit, said and not thrown.
select is(public.request_friend('aaaaaaaaaa'), 'limit', 'nor ask back to accept one more');

-- A row from before the day trigger existed, out of its range, written with the trigger off. What
-- purge-photos does to it every night is to clear its path, and that must not trip over the day.
-- The author is the first of the hundred who asked Bea above (3001 is bb9 in hex): a card needs a profile.
reset role;
alter table public.shared_entries disable trigger entry_day;
insert into public.shared_entries (author, day, color, name, photo_path) values
  ('00000000-0000-0000-0000-000000000bb9', '2025-12-30', '#7A8B3C', 'moss', '00000000-0000-0000-0000-000000000bb9/2025-12-30.jpg');
alter table public.shared_entries enable trigger entry_day;
set local role service_role;
select lives_ok(
  $$ update public.shared_entries set photo_path = null where author = '00000000-0000-0000-0000-000000000bb9' and day = '2025-12-30' $$,
  'the purge can clear the path of a row whose day is out of range'
);
reset role;
select is(
  (select photo_path from public.shared_entries where author = '00000000-0000-0000-0000-000000000bb9' and day = '2025-12-30'),
  null::text, 'and the path is gone'
);

-- The sweep sees what no card points at after a day, and what is older than eight days whatever points at it.
-- A third file, three days old, with a card of its author pointing at it: neither clause takes it.
insert into storage.objects (bucket_id, name, created_at) values
  ('photos', '00000000-0000-0000-0000-00000000000a/2026-01-06.jpg', now() - interval '3 days');
insert into public.shared_entries (author, day, color, name, photo_path) values
  ('00000000-0000-0000-0000-00000000000a', current_date - 3, '#3A6EA5', 'storm_blue', '00000000-0000-0000-0000-00000000000a/2026-01-06.jpg');
select is(
  (select array_agg(x order by x) from public.orphan_photos() as t(x)),
  array['00000000-0000-0000-0000-00000000000a/2026-01-02.jpg', '00000000-0000-0000-0000-00000000000c/2026-01-04.jpg'],
  'the sweep finds the old file with no card and the file past eight days'
);
select is(
  (select count(*) from public.orphan_photos() as t(x) where x = '00000000-0000-0000-0000-00000000000a/2026-01-06.jpg')::int,
  0, 'but not an old file that a card still points at'
);
select is(has_function_privilege('service_role', 'public.orphan_photos()', 'execute'), true, 'the service role runs the sweep');
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select throws_ok($$ select * from public.orphan_photos() $$, '42501', null, 'and nobody signed in does');

-- The guards of a block, with the row of the friendship still there. block_user deletes it, so the
-- tests above never reach them; a request that raced a block leaves exactly this, and so does anything
-- that writes the tables without the functions. Ana blocks Bea, who is her friend, and Dani, who has
-- a request open with her. Dani and not Carla: with 50 friends, her accepting would trip friend_limit
-- and hide whether the block stopped it.
reset role;
delete from public.friendships
where a = '00000000-0000-0000-0000-00000000000a' and b in ('00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000d');
delete from public.blocks where blocker = '00000000-0000-0000-0000-00000000000a';
insert into public.friendships (a, b, requested_by, status) values
  ('00000000-0000-0000-0000-00000000000a', '00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000b', 'accepted'),
  ('00000000-0000-0000-0000-00000000000a', '00000000-0000-0000-0000-00000000000d', '00000000-0000-0000-0000-00000000000d', 'pending');
insert into public.blocks (blocker, blocked) values
  ('00000000-0000-0000-0000-00000000000a', '00000000-0000-0000-0000-00000000000b'),
  ('00000000-0000-0000-0000-00000000000a', '00000000-0000-0000-0000-00000000000d');
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is(
  (select count(*) from public.shared_entries where author = '00000000-0000-0000-0000-00000000000a')::int,
  0, 'a friend who was blocked reads no day'
);
select is(
  (select count(*) from storage.objects where bucket_id = 'photos' and name like '00000000-0000-0000-0000-00000000000a/%')::int,
  0, 'nor a photo'
);
-- Bea has the 100 requests of the limits above: it is only Ana who must be missing.
select is(
  (select count(*) from public.my_friendships() where id = '00000000-0000-0000-0000-00000000000a')::int,
  0, 'nor is the friendship listed'
);
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000d", "role": "authenticated"}';
select is(
  (select count(*) from public.profiles where id = '00000000-0000-0000-0000-00000000000a')::int,
  0, 'whoever asked and was blocked cannot read the profile of who blocked them'
);
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select public.accept_friend('00000000-0000-0000-0000-00000000000d');
reset role;
select is(
  (select status from public.friendships
   where a = '00000000-0000-0000-0000-00000000000a' and b = '00000000-0000-0000-0000-00000000000d'),
  'pending', 'a request from someone blocked is not accepted'
);

-- Who may call what, and who may touch what.
reset role;
select is(
  (select count(*) from pg_proc p join pg_namespace n on n.oid = p.pronamespace
   where n.nspname = 'public' and has_function_privilege('anon', p.oid, 'execute')
     and not exists (select 1 from pg_depend d where d.objid = p.oid and d.deptype = 'e'))::int,
  0, 'anon runs no function'
);
select is(
  (select array_agg(p.proname || '/' || p.pronargs order by p.proname collate "C", p.pronargs)
   from pg_proc p join pg_namespace n on n.oid = p.pronamespace
   where n.nspname = 'public' and has_function_privilege('authenticated', p.oid, 'execute')
     and not exists (select 1 from pg_depend d where d.objid = p.oid and d.deptype = 'e')),
  array['accept_friend/1', 'block_user/1', 'decline_friend/1', 'has_open_request/1', 'is_friend/1', 'my_blocks/0',
        'my_friendships/0', 'my_profile/0', 'regenerate_code/0', 'remove_friend/1', 'request_friend/1', 'unblock_user/1'],
  'signed in, exactly the functions the app and the policies call'
);
select is(
  (select count(*) from pg_class c join pg_namespace n on n.oid = c.relnamespace
   where n.nspname = 'public' and c.relkind in ('r', 'p', 'v', 'm', 'f')
     and has_table_privilege('anon', c.oid, 'select,insert,update,delete'))::int,
  0, 'anon touches no table'
);
select is(
  (select count(*) from pg_proc p join pg_namespace n on n.oid = p.pronamespace
   where n.nspname = 'public' and p.proname in ('is_friend', 'is_blocked') and p.pronargs = 2)::int,
  0, 'nobody picks the first uuid of is_friend or is_blocked'
);
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select throws_ok($$ select * from public.invite_attempts $$, '42501', null, 'the attempts are not for the person who made them');
select throws_ok($$ select * from public.reports $$, '42501', null, 'nobody reads the reports');

-- What runs by itself.
reset role;
select has_trigger('public', 'reports', 'report_notify', 'a report rings the developer from the database');
select is(
  (select count(*) from cron.job where jobname in ('purge-photos', 'report-retry', 'invite-attempts-trim'))::int,
  3, 'the purge, the retry of reports and the trim of attempts are scheduled'
);
select ok(
  position('120000' in (select command from cron.job where jobname = 'purge-photos')) > 0,
  'the purge has two minutes to answer'
);

select * from finish();
rollback;
