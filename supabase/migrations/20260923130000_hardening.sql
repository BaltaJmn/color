-- Found in review after 20260923120000 was pushed. A new file and not an edit, so a database that
-- already applied the first one gets these too.

-- 1. The invite code is its owner's to hand out. A select on the whole table let a friend, or anyone
--    with a request open, read it and pass it on, which is what regenerating it is meant to stop.
--    Other people's rows show their id and name; the owner reads their own code through my_profile().
revoke select on public.profiles from authenticated;
grant select (id, display_name) on public.profiles to authenticated;

create function public.my_profile()
returns table (id uuid, display_name text, invite_code text)
language sql stable security definer set search_path = '' as $$
  select p.id, p.display_name, p.invite_code from public.profiles p where p.id = auth.uid();
$$;
revoke execute on function public.my_profile() from public, anon;
grant execute on function public.my_profile() to authenticated;

-- 2. A shared day's photo can only be the author's own file: a modified client must not be able to
--    dress its card with a photo from someone else's folder.
alter table public.shared_entries add constraint shared_entries_own_photo check (
  photo_path is null
  or (
    photo_path like author::text || '/%'
    and photo_path ~ '^[0-9a-f-]{36}/[0-9]{4}-[0-9]{2}-[0-9]{2}\.jpg$'
  )
);
