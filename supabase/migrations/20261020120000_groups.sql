-- Chroma v1.3, groups (docs/tecnico.md 8.5). No table of days or photos of their own: a group sees the
-- same rows of shared_entries and the same files of the bucket that friends see. What changes is who
-- may read them, and a member who is not a friend only ever reads today and yesterday.

-- --- 1. tables -----------------------------------------------------------------------------------

create table public.groups (
  id uuid primary key default gen_random_uuid(),
  name text not null check (char_length(btrim(name)) between 1 and 30),
  invite_code text not null unique default encode(extensions.gen_random_bytes(5), 'hex'),
  created_at timestamptz not null default now()
);

-- joined_at decides who is in charge: the oldest member. clock_timestamp and not now(), so two joins
-- in one transaction still have an order.
create table public.group_members (
  group_id uuid not null references public.groups on delete cascade,
  member uuid not null references public.profiles on delete cascade,
  joined_at timestamptz not null default clock_timestamp(),
  primary key (group_id, member)
);
create index group_members_member on public.group_members (member);

-- Whoever was removed does not come back with the link.
create table public.group_bans (
  group_id uuid not null references public.groups on delete cascade,
  member uuid not null references public.profiles on delete cascade,
  primary key (group_id, member)
);

-- Like friendships: only through the functions below.
alter table public.groups enable row level security;
alter table public.group_members enable row level security;
alter table public.group_bans enable row level security;
revoke all on public.groups, public.group_members, public.group_bans from anon, authenticated;

-- --- 2. who is in charge, and who sees whom ------------------------------------------------------

-- No owner column: the member who has been in longest. When they leave or delete their account the
-- next one is, without a row to update and without a group left with nobody able to remove anyone.
create function public.group_admin(g uuid) returns uuid
language sql stable security definer set search_path = '' as $$
  select member from public.group_members where group_id = g order by joined_at, member limit 1;
$$;

create function public.is_member(g uuid) returns boolean
language sql stable security definer set search_path = '' as $$
  select exists (select 1 from public.group_members where group_id = g and member = auth.uid());
$$;

-- In a group with me, not me, and no block either way. A false does not say which of the three it
-- was, so the caller may have it, like is_friend: the policies run as the caller.
create function public.shares_group(y uuid) returns boolean
language sql stable security definer set search_path = '' as $$
  select y <> auth.uid() and exists (
    select 1 from public.group_members m
    join public.group_members o on o.group_id = m.group_id
    where m.member = auth.uid() and o.member = y
  ) and not public.is_blocked(y);
$$;

-- current_date - 2 and not - 1: the server's date is UTC and the card's day is the author's, so the
-- reader's yesterday falls inside in any time zone.
drop policy entries_read on public.shared_entries;
create policy entries_read on public.shared_entries for select to authenticated using (
  author = auth.uid() or public.is_friend(author)
  or (day >= current_date - 2 and public.shares_group(author))
);

drop policy reports_insert on public.reports;
create policy reports_insert on public.reports for insert to authenticated
  with check (reporter = auth.uid() and (public.is_friend(author) or public.shares_group(author)));

drop policy photos_read on storage.objects;
create policy photos_read on storage.objects for select to authenticated using (
  bucket_id = 'photos' and (
    (storage.foldername(name))[1] = auth.uid()::text
    or exists (
      select 1 from public.shared_entries e
      where e.photo_path = objects.name
        and (public.is_friend(e.author) or (e.day >= current_date - 2 and public.shares_group(e.author)))
    )
  )
);

-- --- 3. limits -------------------------------------------------------------------------------------

-- 5 groups a person, 20 people a group. The group's row is locked first, so two joins at the same
-- moment cannot both see 19.
create function public.check_group_limits() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
  perform 1 from public.groups where id = new.group_id for update;
  if (select count(*) from public.group_members where group_id = new.group_id) >= 20 then
    raise exception 'group_full';
  end if;
  if (select count(*) from public.group_members where member = new.member) >= 5 then
    raise exception 'group_limit';
  end if;
  return new;
end;
$$;

create trigger group_limits before insert on public.group_members
for each row execute function public.check_group_limits();

-- The last one out takes the group with them, also when that last one deletes their account.
create function public.drop_empty_group() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
  if not exists (select 1 from public.group_members where group_id = old.group_id) then
    delete from public.groups where id = old.group_id;
  end if;
  return null;
end;
$$;

create trigger group_empty after delete on public.group_members
for each row execute function public.drop_empty_group();

-- --- 4. what the app calls ---------------------------------------------------------------------

create function public.create_group(name text) returns uuid
language plpgsql security definer set search_path = '' as $$
declare
  g uuid;
begin
  if not exists (select 1 from public.profiles where id = auth.uid()) then raise exception 'no_profile'; end if;
  insert into public.groups (name) values (btrim(create_group.name)) returning id into g;
  insert into public.group_members (group_id, member) values (g, auth.uid());
  return g;
end;
$$;

-- 'joined', 'already', 'not_found' (no such code, or I was removed), 'limit' (the group is full, or I
-- am in 5), 'too_many'. A group code is 40 bits like a friend's, so misses count against the same
-- brake of 20 an hour.
create function public.join_group(code text) returns text
language plpgsql security definer set search_path = '' as $$
declare
  me uuid := auth.uid();
  g uuid;
begin
  if me is null then raise exception 'not_signed_in'; end if;
  if not exists (select 1 from public.profiles where id = me) then raise exception 'no_profile'; end if;
  if (select count(*) from public.invite_attempts where user_id = me and created_at > now() - interval '1 hour') >= 20 then
    return 'too_many';
  end if;
  select id into g from public.groups where invite_code = lower(trim(code));
  -- A ban answers like a code that does not exist: nobody learns from a link that they were removed.
  if g is null or exists (select 1 from public.group_bans where group_id = g and member = me) then
    insert into public.invite_attempts (user_id) values (me);
    return 'not_found';
  end if;
  if exists (select 1 from public.group_members where group_id = g and member = me) then return 'already'; end if;
  begin
    insert into public.group_members (group_id, member) values (g, me);
  exception
    when unique_violation then return 'already';
    when raise_exception then return 'limit';
  end;
  return 'joined';
end;
$$;

create function public.leave_group(g uuid) returns void
language sql security definer set search_path = '' as $$
  delete from public.group_members where group_id = g and member = auth.uid();
$$;

-- Only who is in charge, never themselves. Nobody is told, and the link no longer lets them back.
create function public.remove_member(g uuid, other uuid) returns void
language plpgsql security definer set search_path = '' as $$
begin
  if other = auth.uid() or public.group_admin(g) is distinct from auth.uid() then return; end if;
  delete from public.group_members where group_id = g and member = other;
  insert into public.group_bans (group_id, member) values (g, other) on conflict do nothing;
end;
$$;

-- Null to anyone but who is in charge. The old link stops working; nobody leaves for it.
create function public.regenerate_group_code(g uuid) returns text
language plpgsql security definer set search_path = '' as $$
declare
  fresh text;
begin
  if public.group_admin(g) is distinct from auth.uid() then return null; end if;
  update public.groups set invite_code = encode(extensions.gen_random_bytes(5), 'hex')
  where id = g returning invite_code into fresh;
  return fresh;
end;
$$;

-- Any member reads the code: anyone in a group may invite to it.
create function public.my_groups()
returns table (id uuid, name text, invite_code text, admin boolean)
language sql stable security definer set search_path = '' as $$
  select g.id, g.name, g.invite_code, public.group_admin(g.id) = auth.uid()
  from public.group_members m
  join public.groups g on g.id = m.group_id
  where m.member = auth.uid()
  order by m.joined_at;
$$;

-- The members by order of arrival, without anyone blocked either way. Empty unless I am one.
create function public.group_people(g uuid)
returns table (id uuid, display_name text, admin boolean)
language sql stable security definer set search_path = '' as $$
  select p.id, p.display_name, p.id = public.group_admin(g)
  from public.group_members m
  join public.profiles p on p.id = m.member
  where m.group_id = g and public.is_member(g)
    and (p.id = auth.uid() or not public.is_blocked(p.id))
  order by m.joined_at, m.member;
$$;

-- --- 5. who may call what -----------------------------------------------------------------------

revoke execute on function
  public.group_admin(uuid), public.is_member(uuid), public.check_group_limits(), public.drop_empty_group()
from public, anon, authenticated;

revoke execute on function
  public.shares_group(uuid), public.create_group(text), public.join_group(text), public.leave_group(uuid),
  public.remove_member(uuid, uuid), public.regenerate_group_code(uuid), public.my_groups(), public.group_people(uuid)
from public, anon;
grant execute on function
  public.shares_group(uuid), public.create_group(text), public.join_group(text), public.leave_group(uuid),
  public.remove_member(uuid, uuid), public.regenerate_group_code(uuid), public.my_groups(), public.group_people(uuid)
to authenticated;
