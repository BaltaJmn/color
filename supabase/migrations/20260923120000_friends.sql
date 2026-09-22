-- Chroma v1.1: friends. docs/tecnico.md 8.
-- The security lives here, in RLS and security definer functions: the app is only a client, and a
-- modified client must not be able to read one color more than the official one.

-- --- tables ------------------------------------------------------------------------------------

create table public.profiles (
  id uuid primary key references auth.users on delete cascade,
  display_name text not null check (char_length(display_name) between 1 and 30),
  invite_code text not null unique default encode(extensions.gen_random_bytes(5), 'hex'),
  created_at timestamptz not null default now()
);

create table public.friendships (
  a uuid not null references public.profiles on delete cascade,
  b uuid not null references public.profiles on delete cascade,
  requested_by uuid not null,
  status text not null check (status in ('pending', 'accepted')),
  created_at timestamptz not null default now(),
  primary key (a, b),
  -- One row per pair, whoever asked: the pair is stored in order.
  check (a < b),
  check (requested_by in (a, b))
);
create index friendships_b on public.friendships (b);

create table public.shared_entries (
  author uuid not null references public.profiles on delete cascade,
  day date not null,
  color text not null check (color ~ '^#[0-9A-F]{6}$'),
  -- The key of the name table, not the name: each friend reads it in their own language.
  name text not null check (char_length(name) between 1 and 40),
  word text check (char_length(word) <= 24),
  photo_path text,
  updated_at timestamptz not null default now(),
  primary key (author, day)
);
create index shared_entries_day on public.shared_entries (day);

-- The feed orders by this, so it moves on every change and not only when the client says so.
create function public.touch_updated_at() returns trigger
language plpgsql set search_path = '' as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

create trigger touch before insert or update on public.shared_entries
for each row execute function public.touch_updated_at();

create table public.blocks (
  blocker uuid not null references public.profiles on delete cascade,
  blocked uuid not null references public.profiles on delete cascade,
  primary key (blocker, blocked)
);
create index blocks_blocked on public.blocks (blocked);

create table public.reports (
  id bigint generated always as identity primary key,
  reporter uuid not null references public.profiles on delete cascade,
  author uuid not null,
  day date not null,
  created_at timestamptz not null default now()
);

-- --- the one question everything asks ---------------------------------------------------------

-- Friends: an accepted friendship and no block either way. A block cuts at once, whatever the row
-- in friendships says.
create function public.is_friend(x uuid, y uuid) returns boolean
language sql stable security definer set search_path = '' as $$
  select exists (
    select 1 from public.friendships f
    where f.a = least(x, y) and f.b = greatest(x, y) and f.status = 'accepted'
  ) and not exists (
    select 1 from public.blocks k
    where (k.blocker = x and k.blocked = y) or (k.blocker = y and k.blocked = x)
  );
$$;

create function public.is_blocked(x uuid, y uuid) returns boolean
language sql stable security definer set search_path = '' as $$
  select exists (
    select 1 from public.blocks k
    where (k.blocker = x and k.blocked = y) or (k.blocker = y and k.blocked = x)
  );
$$;

-- --- the friend limit -------------------------------------------------------------------------

create function public.accepted_count(u uuid) returns integer
language sql stable security definer set search_path = '' as $$
  select count(*)::integer from public.friendships f where (f.a = u or f.b = u) and f.status = 'accepted';
$$;

-- MAX_FRIENDS = 50 (docs/tecnico.md 5). Checked when a friendship becomes accepted, for both sides.
create function public.enforce_friend_limit() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
  if new.status = 'accepted' and (tg_op = 'INSERT' or old.status <> 'accepted') then
    if public.accepted_count(new.a) >= 50 or public.accepted_count(new.b) >= 50 then
      raise exception 'friend_limit';
    end if;
  end if;
  return new;
end;
$$;

create trigger friend_limit before insert or update on public.friendships
for each row execute function public.enforce_friend_limit();

-- --- what the app may call --------------------------------------------------------------------

-- 'sent', 'accepted' (the other had already asked), 'already', 'self', 'blocked', 'not_found', 'limit'.
create function public.request_friend(code text) returns text
language plpgsql security definer set search_path = '' as $$
declare
  me uuid := auth.uid();
  other uuid;
  existing public.friendships;
begin
  if me is null then raise exception 'not_signed_in'; end if;
  select id into other from public.profiles where invite_code = lower(trim(code));
  if other is null then return 'not_found'; end if;
  if other = me then return 'self'; end if;
  -- Said the same way whoever blocked whom: the answer must not tell someone they were blocked.
  if public.is_blocked(me, other) then return 'blocked'; end if;

  select * into existing from public.friendships where a = least(me, other) and b = greatest(me, other);
  if found then
    if existing.status = 'accepted' then return 'already'; end if;
    if existing.requested_by = me then return 'sent'; end if;
    begin
      update public.friendships set status = 'accepted' where a = existing.a and b = existing.b;
    exception when raise_exception then
      return 'limit';
    end;
    return 'accepted';
  end if;

  if public.accepted_count(me) >= 50 then return 'limit'; end if;
  insert into public.friendships (a, b, requested_by, status)
  values (least(me, other), greatest(me, other), me, 'pending');
  return 'sent';
end;
$$;

create function public.accept_friend(other uuid) returns void
language plpgsql security definer set search_path = '' as $$
begin
  update public.friendships set status = 'accepted'
  where a = least(auth.uid(), other) and b = greatest(auth.uid(), other)
    and requested_by = other and status = 'pending';
end;
$$;

-- Declining and removing delete the row and tell nobody.
create function public.decline_friend(other uuid) returns void
language sql security definer set search_path = '' as $$
  delete from public.friendships
  where a = least(auth.uid(), other) and b = greatest(auth.uid(), other)
    and requested_by = other and status = 'pending';
$$;

create function public.remove_friend(other uuid) returns void
language sql security definer set search_path = '' as $$
  delete from public.friendships where a = least(auth.uid(), other) and b = greatest(auth.uid(), other);
$$;

create function public.block_user(other uuid) returns void
language plpgsql security definer set search_path = '' as $$
begin
  if auth.uid() is null or other = auth.uid() then return; end if;
  delete from public.friendships where a = least(auth.uid(), other) and b = greatest(auth.uid(), other);
  insert into public.blocks (blocker, blocked) values (auth.uid(), other) on conflict do nothing;
end;
$$;

-- The old link stops working the moment a new one exists.
create function public.regenerate_code() returns text
language sql security definer set search_path = '' as $$
  update public.profiles set invite_code = encode(extensions.gen_random_bytes(5), 'hex')
  where id = auth.uid() returning invite_code;
$$;

revoke execute on all functions in schema public from public, anon;
grant execute on function public.request_friend(text), public.accept_friend(uuid), public.decline_friend(uuid),
  public.remove_friend(uuid), public.block_user(uuid), public.regenerate_code() to authenticated;
-- Used by the policies below, which run as the caller.
grant execute on function public.is_friend(uuid, uuid), public.is_blocked(uuid, uuid) to authenticated;

-- --- row level security -----------------------------------------------------------------------

alter table public.profiles enable row level security;
alter table public.friendships enable row level security;
alter table public.shared_entries enable row level security;
alter table public.blocks enable row level security;
alter table public.reports enable row level security;

-- Nothing for anon, anywhere. Signed in, only what each policy allows, and only these columns.
revoke all on public.profiles, public.friendships, public.shared_entries, public.blocks, public.reports from anon, authenticated;
grant select on public.profiles, public.friendships, public.shared_entries, public.blocks to authenticated;
grant insert (id, display_name), update (display_name) on public.profiles to authenticated;
grant insert, update, delete on public.shared_entries to authenticated;
grant insert (reporter, author, day) on public.reports to authenticated;

-- Me, my friends, and whoever has a request open with me: accepting needs to know who asks.
create policy profiles_read on public.profiles for select to authenticated using (
  id = auth.uid()
  or public.is_friend(auth.uid(), id)
  or (
    exists (
      select 1 from public.friendships f
      where f.a = least(auth.uid(), profiles.id) and f.b = greatest(auth.uid(), profiles.id) and f.status = 'pending'
    )
    and not public.is_blocked(auth.uid(), id)
  )
);
create policy profiles_insert on public.profiles for insert to authenticated with check (id = auth.uid());
create policy profiles_update on public.profiles for update to authenticated
  using (id = auth.uid()) with check (id = auth.uid());

create policy friendships_read on public.friendships for select to authenticated
  using (auth.uid() in (a, b));

create policy entries_read on public.shared_entries for select to authenticated
  using (author = auth.uid() or public.is_friend(auth.uid(), author));
create policy entries_insert on public.shared_entries for insert to authenticated
  with check (author = auth.uid());
create policy entries_update on public.shared_entries for update to authenticated
  using (author = auth.uid()) with check (author = auth.uid());
create policy entries_delete on public.shared_entries for delete to authenticated
  using (author = auth.uid());

create policy blocks_read on public.blocks for select to authenticated using (blocker = auth.uid());

-- Only about a friend's card, and never readable back: a report is for the developer, not a log.
create policy reports_insert on public.reports for insert to authenticated
  with check (reporter = auth.uid() and public.is_friend(auth.uid(), author));

-- --- photos -----------------------------------------------------------------------------------

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('photos', 'photos', false, 1048576, array['image/jpeg']);

-- The first folder of a path is its author: <author>/<day>.jpg.
create function public.can_see_folder(folder text) returns boolean
language sql stable security definer set search_path = '' as $$
  -- case and not "and": only a case promises the cast never runs on a folder that is not a uuid.
  select case
    when folder = auth.uid()::text then true
    when folder ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$' then public.is_friend(auth.uid(), folder::uuid)
    else false
  end;
$$;
grant execute on function public.can_see_folder(text) to authenticated;

create policy photos_read on storage.objects for select to authenticated
  using (bucket_id = 'photos' and public.can_see_folder((storage.foldername(name))[1]));
create policy photos_insert on storage.objects for insert to authenticated
  with check (bucket_id = 'photos' and (storage.foldername(name))[1] = auth.uid()::text);
create policy photos_update on storage.objects for update to authenticated
  using (bucket_id = 'photos' and (storage.foldername(name))[1] = auth.uid()::text);
create policy photos_delete on storage.objects for delete to authenticated
  using (bucket_id = 'photos' and (storage.foldername(name))[1] = auth.uid()::text);
