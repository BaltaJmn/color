-- The project is shared with FlowTime (its own schema), and so is auth.users: one Apple or Google
-- sign-in may be the account of both apps. Deleting the Chroma account deletes Chroma's rows, and the
-- auth user only when no other app still has data for it. Each app that joins the project redefines
-- this function to add its own check (docs/tecnico.md 8).
create function public.used_by_other_app(uid uuid) returns boolean
language sql stable security definer set search_path = '' as $$
  select false;
$$;

revoke execute on function public.used_by_other_app(uuid) from public, anon, authenticated;
grant execute on function public.used_by_other_app(uuid) to service_role;
