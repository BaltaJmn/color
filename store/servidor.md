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

## 2. Las tareas programadas y el Vault

`pg_cron` lanza tres tareas, todas en UTC: `purge-photos` a las 03:17 (la función del mismo nombre),
`invite-attempts-trim` a las 03:31 (borra los intentos de enlace de más de un día) y `report-retry` en el
minuto 41 de cada hora (reenvía los reportes cuyo correo no salió). Las migraciones las crean; lo que
no pueden crear son los secretos que leen, que se ponen a mano desde el editor SQL, una vez:

```sql
select vault.create_secret('https://<ref>.supabase.co', 'project_url');
select vault.create_secret('<service role key>', 'service_role_key');
select vault.create_secret('<el mismo valor que REPORT_WEBHOOK_SECRET, sección 3>', 'report_webhook_secret');
```

`project_url` y `service_role_key` son para la purga; `project_url` y `report_webhook_secret`, para el
aviso de un reporte. Mejor ponerlos antes de que nadie pueda reportar: si falta alguno, el reporte se
guarda igual y la base solo escribe un aviso en su registro, y el reintento de cada hora lo manda
cuando existan. Para saber si alguno se quedó sin salir:

```sql
select id, created_at from reports where notified_at is null order by id;
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

`REPORT_WEBHOOK_SECRET` tiene que existir y valer lo mismo que `report_webhook_secret` en el Vault
(sección 2): `report-notify` contesta 401 a todo si la variable falta o si la cabecera `x-webhook-secret`
no coincide.

**No hace falta un webhook en el panel** (*Database > Webhooks*): el aviso lo lanza un trigger de la base
sobre `reports`, con `pg_net`, y lo reintenta `report-retry`. Si de una versión anterior queda uno sobre
`reports`, **hay que borrarlo**, o cada reporte mandaría dos correos.

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

## 5. Los enlaces de invitación

`https://color.baltajmn.dev/i/<code>` abre la app si está instalada; si no, GitHub Pages sirve
el `404.html` de la web (repositorio público `BaltaJmn/chroma-privacy`), que lleva a las tiendas. Para
que el sistema confíe en el dominio, la web lleva tres datos de las cuentas:

- `.well-known/assetlinks.json`: el SHA-256 de la clave de firma de apps de Play, **ya puesto** el
  01-10-2026 (leído de la API, `generatedApks`; también en *Play Console > Prueba y publicación >
  Integridad de la app*). Si se quiere probar una build firmada con la clave de subida, se añade
  también la suya a la lista.
- `.well-known/apple-app-site-association`: `TEAM_ID` por el Team ID de `Config.xcconfig`. El
  App ID necesita la capacidad *Associated Domains*. En v1.0 se quitaron de `iosApp.entitlements`
  `associated-domains` (`applinks:color.baltajmn.dev`) y `applesignin` (`Default`): un equipo
  personal de Xcode no firma con ellas. Volver a ponerlas al activar Amigos.
- `404.html`: `APP_STORE_ID` por el identificador numérico de la app en App Store Connect. Vacío,
  la página solo enseña Google Play.

Comprobación: `adb shell pm verify-app-links --re-verify com.baltajmn.color` y después
`adb shell pm get-app-links com.baltajmn.color` dice `verified`. En iOS, el enlace pegado en Notas y
pulsado abre la app.

Riesgo conocido: GitHub Pages sirve `apple-app-site-association` sin extensión como
`application/octet-stream`. La CDN de Apple lo acepta hoy, pero si algún día deja de hacerlo, el
arreglo es servir ese fichero desde otro sitio o poner delante una regla de Cloudflare que fije
`application/json`.

## 6. Comprobarlo

```bash
supabase test db        # supabase/tests: test 17 de docs/tecnico.md 10
```

La misma prueba la ejecuta `.github/workflows/db.yml` (`supabase start` y `supabase test db`) en cada
cambio bajo `supabase/`, así que una migración que abra una política se nota antes de llegar aquí.

Y a mano, con dos móviles: invitar, aceptar, compartir un color, verlo en el feed del otro,
bloquear y ver que desaparece, y desbloquear desde Ajustes > Amigos > Bloqueados. Con un reporte: que
llega el correo (uno solo) y que `notified_at` se rellena.

## 7. Actuar sobre un reporte

El correo trae lo necesario para juzgar (autor, quién reporta, día, color, palabra y una URL de la foto
que vale 24 h) y Apple da 24 h para actuar. Quitar el contenido es, desde el panel:

1. Borrar la fila de `shared_entries` de esa tarjeta (*Table Editor*).
2. Borrar su foto en *Storage > photos*, en la carpeta del autor.
3. Si es la cuenta, vaciar antes su carpeta en *Storage*, borrarla en *Authentication > Users* (el
   resto cae en cascada) y borrar a mano los reportes sobre ella (`reports.author` no tiene clave
   ajena). `delete-account` sigue esos mismos pasos, pero solo la propia persona puede lanzarla.

Un fichero que quede en Storage sin su fila no lo lee nadie más que su dueño, y la purga de esa noche
lo borra (pasado un día sin fila, o a los 8 días en cualquier caso). Pero la foto reportada no tiene
por qué esperar: se borra en el momento.
