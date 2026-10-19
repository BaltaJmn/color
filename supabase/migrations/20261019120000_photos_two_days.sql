-- PHOTO_TTL_DAYS goes from 7 to 2: the feed only shows today and yesterday, so a shared photo has no
-- reason to outlive them. The one night of slack stays (current_date is UTC, and west of it the app is
-- still on its second day), and the hard limit of the sweep follows: three days of file.

create or replace function public.check_entry_day() returns trigger
language plpgsql set search_path = '' as $$
begin
  if tg_op = 'INSERT' or new.day is distinct from old.day then
    if new.day < date '2026-01-01' or new.day > current_date + 2 then
      raise exception 'day_out_of_range';
    end if;
  end if;
  if new.day < current_date - 3 then
    new.photo_path := null;
  end if;
  return new;
end;
$$;

create or replace function public.orphan_photos() returns setof text
language sql stable security definer set search_path = '' as $$
  select o.name
  from storage.objects o
  where o.bucket_id = 'photos'
    and (
      o.created_at < now() - interval '3 days'
      or (
        o.created_at < now() - interval '1 day'
        and not exists (select 1 from public.shared_entries e where e.photo_path = o.name)
      )
    )
  order by o.created_at
  limit 1000;
$$;
