-- App Review needs a login that works on an unknown device: a Google or Apple test account asks for
-- an SMS or a second email there. So the Email provider is on, but this Before User Created hook
-- (Authentication > Auth Hooks) lets email create only the reviewer account, which is made by hand in
-- the dashboard. Apple and Google sign-ups pass untouched. Shared by Chroma and FlowTime
-- (docs/tecnico.md 8).
create function public.hook_before_user_created(event jsonb) returns jsonb
language sql immutable set search_path = '' as $$
  select case
    when event->'user'->'app_metadata'->>'provider' = 'email'
     and coalesce(lower(event->'user'->>'email'), '') <> 'review@baltajmn.dev'
    then jsonb_build_object('error', jsonb_build_object(
      'message', 'Sign in with Apple or Google.', 'http_code', 403))
    else '{}'::jsonb
  end;
$$;

revoke execute on function public.hook_before_user_created(jsonb) from public, anon, authenticated;
grant execute on function public.hook_before_user_created(jsonb) to supabase_auth_admin;
