# El servidor de Amigos, paso a paso

Todo lo que hay que hacer a mano una vez para que Amigos (v1.1) funcione. El esquema, las reglas y
las funciones están en `supabase/`; aquí va lo que no se puede versionar: el proyecto, las claves y
los proveedores de inicio de sesión. Contrato: `docs/tecnico.md` 8 y 9.

**Ningún secreto entra en el repositorio.** La clave `service_role`, la de Resend y los secretos de
Apple y Google viven solo en el proyecto de Supabase. En la app va la URL del proyecto y la clave
pública (`anon`), en `social/SupabaseConfig.kt`.

## 1. El proyecto

1. Crear el proyecto `chroma` en la región **eu-central-1** (Frankfurt), plan Free.
2. Instalar la CLI (`brew install supabase/tap/supabase`), `supabase login` y enlazar:

   ```bash
   supabase link --project-ref <ref>
   supabase db push
   ```

3. Poner en `shared/src/commonMain/kotlin/com/baltajmn/color/social/SupabaseConfig.kt` la URL
   (`https://<ref>.supabase.co`) y la clave `anon`. Son públicas: ya viajarían en el binario.

## 2. La tarea de cada noche

`purge-photos` la llama `pg_cron` a las 03:17 UTC. Necesita dos secretos en el Vault, desde el
editor SQL:

```sql
select vault.create_secret('https://<ref>.supabase.co', 'project_url');
select vault.create_secret('<service role key>', 'service_role_key');
```

## 3. Las funciones

```bash
supabase functions deploy purge-photos
supabase functions deploy report-notify
supabase functions deploy delete-account
supabase secrets set RESEND_API_KEY=<clave> REPORT_TO=baltajmn@gmail.com \
  REPORT_FROM="Chroma <reports@baltajmn.dev>" REPORT_WEBHOOK_SECRET=<aleatorio>
```

`REPORT_FROM` tiene que ser de un dominio verificado en Resend (`baltajmn.dev`, registros en
Cloudflare).

Webhook de base de datos (*Database > Webhooks*): tabla `reports`, evento `INSERT`, tipo *Supabase
Edge Functions*, función `report-notify`, cabecera `x-webhook-secret` con el mismo valor que
`REPORT_WEBHOOK_SECRET`.

## 4. Iniciar sesión con Apple y Google

*Authentication > Sign In / Providers*. Correo, teléfono y anónimo, apagados.

- **Apple**: un Services ID (`com.baltajmn.color.signin`) con *Sign in with Apple* y la vuelta
  `https://<ref>.supabase.co/auth/v1/callback`; una clave de Sign in with Apple para generar el
  secreto. En *Client IDs*, el Services ID y el bundle `com.baltajmn.color` (el nativo de iOS entra
  con el bundle). El target de iOS necesita la capacidad *Sign in with Apple*.
- **Google**: en Google Cloud, un cliente OAuth web (su ID y secreto van a Supabase), uno Android con
  el SHA-1 de la firma de Play y el de depuración, y uno iOS. En *Client IDs* de Supabase, los tres
  separados por comas. El ID del cliente web va también en `SupabaseConfig.kt` (`googleServerClientId`).
- *URL Configuration*: `Site URL` `https://color.baltajmn.dev` y, en *Redirect URLs*,
  `com.baltajmn.color://login`.

## 5. Comprobarlo

```bash
supabase test db        # supabase/tests: test 17 de docs/tecnico.md 10
```

Y a mano, con dos móviles: invitar, aceptar, compartir un color, verlo en el feed del otro,
bloquear y ver que desaparece.
