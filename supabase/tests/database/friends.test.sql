-- Test 17 (docs/tecnico.md 10): without friendship nothing is read, a block cuts at once, the
-- limit of 50 holds, invite codes stay with their owner, and a card only shows its author's photo.
-- Run with: supabase test db
begin;
create extension if not exists pgtap with schema extensions;
select plan(17);

-- Four people. auth.users is the only table the test writes as its owner.
insert into auth.users (id, email) values
  ('00000000-0000-0000-0000-00000000000a', 'a@test'),
  ('00000000-0000-0000-0000-00000000000b', 'b@test'),
  ('00000000-0000-0000-0000-00000000000c', 'c@test');
insert into public.profiles (id, display_name, invite_code) values
  ('00000000-0000-0000-0000-00000000000a', 'Ana', 'aaaaaaaaaa'),
  ('00000000-0000-0000-0000-00000000000b', 'Bea', 'bbbbbbbbbb'),
  ('00000000-0000-0000-0000-00000000000c', 'Carla', 'cccccccccc');

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

-- Ana accepts.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select public.accept_friend('00000000-0000-0000-0000-00000000000b');
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is((select count(*) from public.shared_entries)::int, 1, 'a friend reads the day');

-- Carla, still a stranger, reads nothing even with the path of the photo.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000c", "role": "authenticated"}';
select is(public.can_see_folder('00000000-0000-0000-0000-00000000000a'), false, 'a stranger cannot see the photos');
select is(public.can_see_folder('not-a-uuid'), false, 'a folder that is not a person is nobody''s');

-- Ana blocks Bea: it cuts at once, and Bea cannot ask again.
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select public.block_user('00000000-0000-0000-0000-00000000000b');
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000b", "role": "authenticated"}';
select is((select count(*) from public.shared_entries)::int, 0, 'a block cuts at once');
select is(public.request_friend('aaaaaaaaaa'), 'blocked', 'a blocked person cannot ask again');

-- The limit: Carla with 50 friends cannot accept one more.
reset role;
insert into auth.users (id, email)
  select ('00000000-0000-0000-0000-' || lpad(to_hex(1000 + i), 12, '0'))::uuid, 'f' || i || '@test' from generate_series(1, 50) i;
insert into public.profiles (id, display_name)
  select ('00000000-0000-0000-0000-' || lpad(to_hex(1000 + i), 12, '0'))::uuid, 'F' || i from generate_series(1, 50) i;
insert into public.friendships (a, b, requested_by, status)
  select '00000000-0000-0000-0000-00000000000c', ('00000000-0000-0000-0000-' || lpad(to_hex(1000 + i), 12, '0'))::uuid,
         '00000000-0000-0000-0000-00000000000c', 'accepted'
  from generate_series(1, 50) i;
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000a", "role": "authenticated"}';
select is(public.request_friend('cccccccccc'), 'sent', 'asking someone who is full is still a request');
set local role authenticated;
set local request.jwt.claims to '{"sub": "00000000-0000-0000-0000-00000000000c", "role": "authenticated"}';
select throws_ok($$ select public.accept_friend('00000000-0000-0000-0000-00000000000a') $$, 'P0001', 'friend_limit', 'the 51st friend is refused');

select * from finish();
rollback;
