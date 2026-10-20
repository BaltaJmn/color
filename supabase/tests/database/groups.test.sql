-- Test 20 (docs/tecnico.md 10): groups. A member reads another's today and yesterday and nothing older,
-- a stranger reads nothing, a block cuts inside a group too, the limits hold, only who is in charge
-- removes and regenerates, and the last one out takes the group with them.
-- Run with: supabase test db
begin;
create extension if not exists pgtap with schema extensions;
select plan(40);

-- Ana, Bea, Carla and Dani have a profile; Eva only signed in.
insert into auth.users (id, email) values
  ('00000000-0000-0000-0000-00000000000a', 'a@test'),
  ('00000000-0000-0000-0000-00000000000b', 'b@test'),
  ('00000000-0000-0000-0000-00000000000c', 'c@test'),
  ('00000000-0000-0000-0000-00000000000d', 'd@test'),
  ('00000000-0000-0000-0000-00000000000e', 'e@test');
insert into public.profiles (id, display_name) values
  ('00000000-0000-0000-0000-00000000000a', 'Ana'),
  ('00000000-0000-0000-0000-00000000000b', 'Bea'),
  ('00000000-0000-0000-0000-00000000000c', 'Carla'),
  ('00000000-0000-0000-0000-00000000000d', 'Dani');

-- Ana shares today with a photo, and a day from five days ago. Nobody is anybody's friend.
insert into public.shared_entries (author, day, color, name, photo_path) values
  ('00000000-0000-0000-0000-00000000000a', current_date, '#3A6EA5', 'storm_blue',
   '00000000-0000-0000-0000-00000000000a/' || to_char(current_date, 'YYYY-MM-DD') || '.jpg'),
  ('00000000-0000-0000-0000-00000000000a', current_date - 5, '#112233', 'night', null);
insert into storage.objects (bucket_id, name) values
  ('photos', '00000000-0000-0000-0000-00000000000a/' || to_char(current_date, 'YYYY-MM-DD') || '.jpg');

-- Ana creates the group.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select lives_ok($$ select public.create_group('  Piso  ') $$, 'someone with a profile creates a group');
reset role;
select set_config('test.g', (select id::text from public.groups where name = 'Piso'), true);
update public.groups set invite_code = '1111111111' where name = 'Piso';

set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select is((select admin from public.my_groups()), true, 'who creates it is in charge');
select is((select name from public.my_groups()), 'Piso', 'the name is trimmed');

set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000e", "role": "authenticated"}';
select throws_ok($$ select public.create_group('Nada') $$, 'P0001', 'no_profile', 'without a profile there is no group');

-- Bea joins with the link.
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is(public.join_group('1111111111'), 'joined', 'the link lets you in');
select is(public.join_group(' 1111111111 '), 'already', 'twice is already');
select is(public.join_group('2222222222'), 'not_found', 'a code that does not exist');
select is((select count(*) from public.shared_entries where author = '00000000-0000-0000-0000-00000000000a')::int,
  1, 'a member reads today, and not the day from five days ago');
select is((select count(*) from storage.objects where name like '00000000-0000-0000-0000-00000000000a/%')::int,
  1, 'and the photo of that day');
select is((select count(*) from public.group_people(current_setting('test.g')::uuid))::int, 2, 'the members are listed');
select is((select admin from public.my_groups()), false, 'who joins is not in charge');
select is(public.regenerate_group_code(current_setting('test.g')::uuid), null, 'only who is in charge regenerates');
select public.remove_member(current_setting('test.g')::uuid, '00000000-0000-0000-0000-00000000000a');
select is((select count(*) from public.group_people(current_setting('test.g')::uuid))::int, 2, 'nor removes anyone');
select lives_ok(
  $$ insert into public.reports (reporter, author, day) values ('00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000a', current_date) $$,
  'a member reports a card of the group'
);
reset role;
select is((select count(*) from public.invite_attempts where user_id = '00000000-0000-0000-0000-00000000000b')::int,
  1, 'the miss counted against the brake of the invitations');

-- Carla is nobody to either of them.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000c", "role": "authenticated"}';
select is((select count(*) from public.shared_entries)::int, 0, 'a stranger reads no day');
select is((select count(*) from storage.objects where name like '00000000-0000-0000-0000-00000000000a/%')::int, 0, 'nor a photo');
select is((select count(*) from public.group_people(current_setting('test.g')::uuid))::int, 0, 'nor the members');
select throws_ok(
  $$ insert into public.reports (reporter, author, day) values ('00000000-0000-0000-0000-00000000000c', '00000000-0000-0000-0000-00000000000a', current_date) $$,
  '42501', null, 'nor reports a card'
);

-- A block inside the group cuts both ways, and comes undone.
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select public.block_user('00000000-0000-0000-0000-00000000000b');
select is((select count(*) from public.group_people(current_setting('test.g')::uuid))::int, 1, 'who blocked does not see the other in the list');
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is((select count(*) from public.shared_entries where author = '00000000-0000-0000-0000-00000000000a')::int, 0, 'a block cuts the days');
select is((select count(*) from storage.objects where name like '00000000-0000-0000-0000-00000000000a/%')::int, 0, 'and the photos');
select is((select count(*) from public.group_people(current_setting('test.g')::uuid))::int, 1, 'and the list, the other way too');
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select public.unblock_user('00000000-0000-0000-0000-00000000000b');
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is((select count(*) from public.shared_entries where author = '00000000-0000-0000-0000-00000000000a')::int, 1, 'unblocking brings the days back');

-- Ana removes Bea, who cannot come back with the link.
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select public.remove_member(current_setting('test.g')::uuid, '00000000-0000-0000-0000-00000000000b');
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is((select count(*) from public.my_groups())::int, 0, 'who is removed is out');
select is((select count(*) from public.group_people(current_setting('test.g')::uuid))::int, 0, 'and sees nobody in it, not even themselves');
select is(public.join_group('1111111111'), 'not_found', 'and the link answers as if it did not exist');

-- Carla joins, Ana leaves: Carla is in charge, and regenerates.
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000c", "role": "authenticated"}';
select public.join_group('1111111111');
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select public.leave_group(current_setting('test.g')::uuid);
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000c", "role": "authenticated"}';
select is((select admin from public.my_groups()), true, 'when who is in charge leaves, the next one is');
select isnt(public.regenerate_group_code(current_setting('test.g')::uuid), '1111111111', 'and gets a new link');

-- Twenty a group: Carla and nineteen more.
reset role;
insert into auth.users (id, email) select gen_random_uuid(), 'f' || i || '@test' from generate_series(1, 19) i;
insert into public.profiles (id, display_name) select id, 'F' from auth.users where email like 'f%@test';
insert into public.group_members (group_id, member)
  select current_setting('test.g')::uuid, id from auth.users where email like 'f%@test';
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000d", "role": "authenticated"}';
select is(public.join_group('1111111111'), 'not_found', 'the old link is gone');
reset role;
select set_config('test.code', (select invite_code from public.groups where id = current_setting('test.g')::uuid), true);
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000d", "role": "authenticated"}';
select is(public.join_group(current_setting('test.code')), 'limit', 'a full group lets nobody else in');

-- Five groups a person, and the last one out takes the group with them.
select lives_ok($$ select public.create_group('D' || i) from generate_series(1, 5) i $$, 'five groups');
select throws_ok($$ select public.create_group('D6') $$, 'P0001', 'group_limit', 'not six');
select public.leave_group((select id from public.my_groups() where name = 'D1'));
reset role;
select is((select count(*) from public.groups where name = 'D1')::int, 0, 'the last one out deletes the group');
delete from public.profiles where id = '00000000-0000-0000-0000-00000000000d';
select is((select count(*) from public.groups where name like 'D%')::int, 0, 'and so does deleting the last account');

-- The brake of 20 misses an hour holds here too.
insert into public.invite_attempts (user_id) select '00000000-0000-0000-0000-00000000000b' from generate_series(1, 18);
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is(public.join_group('3333333333'), 'too_many', 'too many misses stop the guessing');

-- Nothing of groups is touched without the functions.
select throws_ok($$ select * from public.groups $$, '42501', null, 'nobody reads the groups table');
select throws_ok(
  format($$ insert into public.group_members (group_id, member) values (%L, auth.uid()) $$, current_setting('test.g')),
  '42501', null, 'nobody joins without the link'
);

reset role;
select has_trigger('public', 'group_members', 'group_limits', 'the limits are a trigger');
select has_trigger('public', 'group_members', 'group_empty', 'and so is the cleanup');

select * from finish();
rollback;
