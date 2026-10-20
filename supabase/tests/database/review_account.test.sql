-- Email creates only the reviewer account; Apple and Google sign-ups are never touched.
-- Run with: supabase test db
begin;
create extension if not exists pgtap with schema extensions;
select plan(5);

select is(public.hook_before_user_created(
  '{"user": {"email": "Review@baltajmn.dev", "app_metadata": {"provider": "email"}}}'),
  '{}'::jsonb, 'the reviewer account can be created');
select is(public.hook_before_user_created(
  '{"user": {"email": "someone@gmail.com", "app_metadata": {"provider": "email"}}}')->'error'->>'http_code',
  '403', 'any other email sign-up is refused');
select is(public.hook_before_user_created(
  '{"user": {"app_metadata": {"provider": "email"}}}')->'error'->>'http_code',
  '403', 'an email sign-up without an address is refused');
select is(public.hook_before_user_created(
  '{"user": {"email": "someone@gmail.com", "app_metadata": {"provider": "google"}}}'),
  '{}'::jsonb, 'Google sign-ups pass');
select is(public.hook_before_user_created(
  '{"user": {"email": "abc@privaterelay.appleid.com", "app_metadata": {"provider": "apple"}}}'),
  '{}'::jsonb, 'Apple sign-ups pass');

select * from finish();
rollback;
