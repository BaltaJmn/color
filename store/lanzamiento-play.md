# Lanzamiento en Google Play, paso a paso

Todo lo que falta para publicar Chroma en Play y que Pro se pueda comprar, en el orden en que conviene
hacerlo. Las respuestas de los formularios viven en `store/formularios.md`; aquí se dice dónde se
pinchan. Los textos de la ficha viven en `store/listings/`.

Orden, de un vistazo:

1. Guardar la clave de subida (5 minutos).
2. ~~Poner la web de privacidad en `color.baltajmn.dev`~~ (hecho).
3. Crear la app en Play Console y subir el AAB a prueba interna.
4. Rellenar el contenido de la app (formularios).
5. Montar la ficha: textos, gráficos y capturas.
6. RevenueCat: producto, derecho, oferta y clave `goog_`. Con la clave, versión 2 a prueba interna.
7. Probar la compra y restaurar con una cuenta de prueba.
8. Prueba cerrada (12 testers, 14 días) y producción.

Las capturas se sacan **antes** del paso 6: el año de demostración trae Pro encendido y, en cuanto la
app tiene la clave de RevenueCat, la tienda lo apaga al arrancar.

---

## 1. La clave de subida

Ya está generada. Nada de esto entra en el repositorio (`.gitignore` lo impide).

| Qué | Dónde |
|---|---|
| Almacén de claves | `~/keys/chroma-upload.jks` (PKCS12, RSA 4096, válida hasta 2054) |
| Alias | `upload` |
| Contraseña (almacén y clave, la misma) | `keystore.properties`, en la raíz del repositorio |
| Huella SHA-256 del certificado de subida | `DD:DB:0D:53:08:C5:DB:12:EA:DC:87:EA:6E:86:0D:1F:C9:78:70:C6:AB:52:99:12:77:A5:9F:3D:48:DD:F1:F1` |

**Copia de seguridad, hoy mismo.** En tu gestor de contraseñas (1Password, Bitwarden, el de Apple),
una entrada "Chroma, clave de subida de Play" con:

- el fichero `chroma-upload.jks` como adjunto;
- la contraseña, que copias de `keystore.properties` (línea `storePassword=`);
- el alias `upload`.

Si algún día se pierde, no es el fin: con la firma de apps de Play, Google guarda la clave que firma
lo que se instala, y la de subida se puede cambiar en *Prueba y publicación > Integridad de la app >
Firma de apps > Solicitar restablecimiento de la clave de subida*. Tarda unos días; mejor no llegar ahí.

**Generar el AAB** (cada versión):

```bash
./gradlew :androidApp:bundleRelease
```

Sale en `androidApp/build/outputs/bundle/release/androidApp-release.aab`. Antes de subirlo, comprueba
que va firmado con la clave de subida y no con la de depuración (si falta `keystore.properties`, el
build firma en silencio con la de depuración y Play lo rechaza):

```bash
keytool -J-Duser.language=en -printcert -jarfile androidApp/build/outputs/bundle/release/androidApp-release.aab | grep SHA256
```

Tiene que salir la huella de la tabla. Cada subida a Play lleva un `versionCode` nuevo
(`androidApp/build.gradle.kts`, línea `versionCode = 1`): se sube de uno en uno y nunca se reutiliza,
ni siquiera si la versión anterior no llegó a publicarse.

---

## 2. La web de privacidad en color.baltajmn.dev

**Hecho el 24-09-2026.** La política (`web/index.html`, más `terms.html` y `delete.html`) se publica en
`https://color.baltajmn.dev/`, que es la URL a la que ya apunta la app (`AppInfo.kt`).

Cómo está montado, igual que Quilt:

| Pieza | Estado |
|---|---|
| Registrador | Porkbun. No se toca: los servidores de nombres de `baltajmn.dev` son los de Cloudflare |
| DNS | Cloudflare, zona `baltajmn.dev`: `CNAME color` a `baltajmn.github.io`, **con proxy** (nube naranja) |
| HTTPS | Lo pone Cloudflare en su borde y redirige `http` a `https`. GitHub no puede emitir certificado detrás del proxy, así que su casilla *Enforce HTTPS* se queda sin marcar, y está bien |
| GitHub Pages | Fuente *GitHub Actions*; dominio propio `color.baltajmn.dev`. `.github/workflows/pages.yml` publica `web/` en cada cambio |

Si Cloudflare pasara algún día a SSL "Full (strict)", estas webs darían error 526: el certificado de
GitHub no cubre el subdominio. Con el modo actual funcionan.

Para la web de otra app, lo mismo cambiando el nombre:

```bash
gh api -X POST repos/BaltaJmn/<repo>/pages -f build_type=workflow
gh workflow run pages.yml --repo BaltaJmn/<repo>
# CNAME <app> a baltajmn.github.io, con proxy, en Cloudflare
gh api -X PUT repos/BaltaJmn/<repo>/pages -f cname=<app>.baltajmn.dev
```

Si antes de poner el dominio alguien pidió la URL, algunos nodos del CDN de GitHub se quedan con su
404 ("Site not found") y `/` alterna 404 y 200 durante mucho más de los 10 minutos de caché. Un
despliegue nuevo lo purga: `gh workflow run pages.yml --repo BaltaJmn/<repo>`.

Comprobación:

```bash
for p in "" terms.html delete.html .well-known/assetlinks.json; do curl -s -o /dev/null -w "%{http_code} /$p\n" https://color.baltajmn.dev/$p; done
```

`/.well-known/assetlinks.json` lleva todavía un marcador: solo lo usarán los enlaces de invitación de
Amigos (v1.1), y se rellena entonces (sección 9).

---

## 3. Crear la app y la primera subida

### Crear la app

Play Console, *Inicio > Crear aplicación*:

| Campo | Valor |
|---|---|
| Nombre de la aplicación | `Chroma: Daily Color Diary` |
| Idioma predeterminado | **Inglés (Estados Unidos), en-US** |
| Aplicación o juego | Aplicación |
| Gratuita o de pago | **Gratuita** (Pro es una compra dentro de la app) |
| Declaraciones | Marca las dos (políticas del programa y leyes de exportación de EE. UU.) |

El idioma predeterminado es el que ve cualquiera cuyo idioma no tenga ficha propia: el inglés cubre más
países que el español. "Gratuita" no se puede cambiar luego a "de pago", y es lo que se quiere.

### Subir el AAB a prueba interna

La prueba interna es la vía rápida (sin revisión larga) y además es requisito para crear el producto
de Pro: Play no deja crear productos hasta que hay un AAB subido con el permiso de facturación, y el
de Chroma ya lo lleva.

1. *Prueba y publicación > Pruebas > Prueba interna > Testers*: crea una lista de correo (por ejemplo
   "Chroma interna") con tu cuenta de Google, guárdala y copia el **enlace para unirse**.
2. *Crear versión*.
3. **Firma de apps de Play**: la primera vez te pregunta; deja la opción por defecto, que Google genere
   y guarde la clave de firma de la app. Tu `.jks` queda como clave de subida.
4. Sube `androidApp-release.aab` (el del paso 1, `versionCode` 1).
5. Nombre de la versión: `1 (1.0)`. Notas: pega las de `store/whatsnew/whatsnew-<idioma>` en cada
   idioma, o déjalas vacías en prueba interna.
6. *Siguiente > Guardar y publicar* (o *Iniciar lanzamiento en prueba interna*).
7. Abre en el móvil el enlace del punto 1, acepta y descarga Chroma desde Play.

---

## 4. Contenido de la app (formularios)

Todo en *Política > Contenido de la aplicación* (en algunas cuentas, *Supervisar y mejorar > Política
y programas > Contenido de la aplicación*). Las respuestas completas, con su porqué, en
`store/formularios.md`.

| Declaración | Qué poner |
|---|---|
| Política de privacidad | `https://color.baltajmn.dev/` |
| Anuncios | No contiene anuncios |
| Acceso a la aplicación | Toda la funcionalidad está disponible sin acceso especial |
| Clasificación del contenido | Cuestionario de `formularios.md` sección 2. Categoría: "Todas las demás aplicaciones" (utilidad, productividad, comunicación u otras). No a todo lo de violencia, sexo, drogas, apuestas; sí a "compra de bienes digitales"; no a interacción entre usuarios y a compartir ubicación. Resultado esperado: PEGI 3 |
| Público objetivo | 13-15, 16-17 y 18 o más. No atrae a menores de 13 |
| Aplicación de noticias | No |
| Seguridad de los datos | `formularios.md` sección 1, detallado abajo |
| ID de publicidad | No usa el ID de publicidad |
| Apps gubernamentales, funciones financieras, salud | No / Ninguna |

**Seguridad de los datos**, pantalla por pantalla:

1. *Recogida y seguridad de los datos*: ¿recoge o comparte datos de los tipos obligatorios? **Sí**.
   ¿Se cifran en tránsito? **Sí**. Cuentas: **la app no permite crear cuentas**. ¿Forma de pedir el
   borrado? **Sí** (por correo, lo explica la política).
2. *Tipos de datos*: marca solo **Información financiera > Historial de compras** y
   **Identificadores del dispositivo u otros identificadores**. Nada más: las fotos, los colores y las
   palabras no salen del teléfono.
3. Para cada uno de los dos: recogido **sí**, compartido **no**, tratado de forma efímera **no**,
   **obligatorio**, finalidad **funcionalidad de la aplicación**.
4. Vista previa y *Guardar*.

Los dos datos son los de RevenueCat al comprar Pro. Si algún día cambia lo que sale del teléfono, se
cambia en el mismo commit `web/index.html`, `formularios.md` y `PrivacyInfo.xcprivacy`.

Si la consola pregunta por servicios en primer plano, el permiso lo trae WorkManager (a través de los
widgets), no la app. Pásame el texto exacto de la pregunta y lo resolvemos.

---

## 5. La ficha

### Ajustes de la ficha

*Crecer > Presencia en Play Store > Configuración de la ficha* (en algunas consolas, *Ajustes de la
tienda*):

| Campo | Valor |
|---|---|
| Categoría | **Fotografía** |
| Etiquetas (hasta 5) | Busca en la lista: Diario, Fotografía, Personalización, Minimalista, Bienestar o Mindfulness. Usa las que existan |
| Correo | `baltajmn@gmail.com` (el mismo de la política; es el que recibe las peticiones de borrado) |
| Sitio web | `https://color.baltajmn.dev/` |
| Teléfono | Vacío |

### Textos, en cinco idiomas

*Crecer > Presencia en Play Store > Ficha principal*. Rellena en-US y después *Gestionar traducciones >
Añadir tus propias traducciones*: es-ES, pt-BR, de-DE, fr-FR. Nunca la traducción automática.

Cada campo sale de un fichero:

| Campo | Fichero | Tope |
|---|---|---|
| Nombre de la aplicación | `store/listings/<idioma>/title.txt` | 30 |
| Descripción breve | `store/listings/<idioma>/short.txt` | 80 |
| Descripción completa | `store/listings/<idioma>/full.txt` | 4000 |
| Novedades (en cada versión) | `store/whatsnew/whatsnew-<idioma>` | 500 |

Los títulos:

| Idioma | Título |
|---|---|
| en-US | Chroma: Daily Color Diary |
| es-ES | Chroma: diario de color |
| pt-BR | Chroma: diário de cores |
| de-DE | Chroma: Farbtagebuch |
| fr-FR | Chroma : journal de couleurs |

"Chroma" a secas es un nombre muy ocupado (Razer Chroma, Chroma DB); el apellido es lo que trae
búsquedas, y "diario" es lo que la gente escribe para encontrar este tipo de app. Bajo el icono sigue
poniendo **Chroma**. Los cinco títulos, cortos y largos están comprobados contra los topes:

```bash
python3 tools/store/fichas.py
```

**Atajo: subir los cinco idiomas de golpe.** En vez de pegar quince campos a mano, el mismo script los
sube por la API en una sola edición (entran todos o ninguno). Necesita que la app exista y tenga un AAB
subido, y una cuenta de servicio con permiso de ficha (la del paso 6 sirve si le das además "Gestionar
presencia en Play Store"):

```bash
pip3 install google-api-python-client google-auth
PLAY_SERVICE_ACCOUNT_JSON="$(< ~/keys/play-service-account.json)" python3 tools/store/fichas.py --subir
```

Opcional y barato: Play trata el español de España y el de Latinoamérica como fichas distintas. Si
quieres cubrir `es-419` y `pt-PT`, copia las carpetas `es-ES` y `pt-BR` con esos nombres antes de
subir.

### Gráficos

| Recurso | Fichero | Requisito de Play |
|---|---|---|
| Icono | `store/play/icon-512.png` | 512x512, PNG, máx. 1 MB |
| Gráfico de funciones | `store/play/feature-1024x500.png` | 1024x500, PNG o JPG sin transparencia |
| Capturas de teléfono | `store/screenshots/play/<idioma>/01.png` a `06.png` | De 2 a 8, 1200x2100 |

Los dos primeros ya están hechos y cumplen (sin canal alfa). Las capturas en-US van en la ficha
principal; las es-ES en la traducción al español. pt, de y fr heredan las inglesas.

Vídeo, capturas de tablet y de Chromebook: se dejan vacíos.

---

## 5b. Las capturas

Se sacan en el emulador Pixel 8 con API 36, con el año de demostración, y antes de poner la clave de
RevenueCat. El proceso completo está en `store/capturas.md`; en corto:

```bash
python3 tools/demo/generar.py --idioma en-US
./gradlew :androidApp:installDebug
adb push tools/demo/salida/entries.json /data/local/tmp/entries.json
adb shell run-as com.baltajmn.color cp /data/local/tmp/entries.json files/entries.json
```

Con la app cerrada al copiar. Luego las seis escenas de `store/capturas.md` sección 2, cada una con
`adb exec-out screencap -p > crudas/01_hoy.png` (y así hasta `06_poster.png`), y el marco:

```bash
python3 tools/store/capturas.py crudas en-US play
```

Se repite con `--idioma es-ES` y el móvil en español. Si prefieres, las saco yo.

---

## 6. RevenueCat y Pro

Qué espera el código, que no hay que tocar:

| Pieza | Valor | Dónde lo usa |
|---|---|---|
| Derecho (entitlement) | `pro`, exacto | `Billing.kt`: Pro está activo si este derecho está activo |
| Oferta (offering) | La marcada como **actual** (current); se llamará `default` | `Billing.kt`: pide la actual, no un nombre |
| Paquete | El primero de la oferta actual. Tiene que haber **uno solo** | `Billing.kt` |
| Producto | `pro_lifetime`, compra única, 4,99 EUR | Solo en Play y RevenueCat; el precio que ve el usuario lo da la tienda |
| Clave pública Android | `goog_...` | `shared/src/androidMain/kotlin/com/baltajmn/color/billing/Billing.android.kt`, línea 4 |

Mientras la clave sea `null`, la app funciona gratis y el panel de Pro dice que la tienda no está
disponible. No se rompe nada.

**Hecho el 24-09-2026 por la API de RevenueCat:** proyecto Chroma con la app de Play
(`com.baltajmn.color`) y el JSON de la cuenta de servicio, producto `pro_lifetime`, derecho `pro`,
oferta `default` actual con el paquete `$rc_lifetime`, y la clave `goog_` en `Billing.android.kt`
desde el `versionCode` 2. Queda el producto en Play Console (6.1). Los pasos de abajo sirven para
comprobar o rehacer.

### 6.1 El producto en Play Console

Requisitos: el AAB del paso 3 ya subido y un perfil de pagos (*Configuración > Perfil de pagos*; si ya
cobras con Quilt, está).

1. *Monetizar con Play > Productos > Productos únicos* (antes se llamaba *Productos de compra en
   aplicaciones*) > *Crear producto*.
2. **ID del producto**: `pro_lifetime`. Se escribe una vez y no se puede cambiar ni reutilizar.
3. Nombre: `Chroma Pro`. Descripción: `Year poster, year widget and your year in words. One payment.`
   (y su versión en cada idioma si la consola lo pide; en español: `Póster del año, widget del año y tu
   año en palabras. Pago único.`).
4. Categoría fiscal: la que proponga para apps (servicios digitales).
5. **Opción de compra**: tipo *Comprar*, ID `buy`, y márcala **compatible con versiones anteriores**
   (backwards compatible). Es la que ven las versiones de la librería de facturación como la de Chroma.
6. **Precio**: *Establecer precio* > `4,99 EUR`. Deja que Play calcule el resto de países (precios
   regionales, es la decisión del issue 3).
7. *Guardar* y **Activar**. Un producto inactivo no aparece en RevenueCat ni en la app.

Si tu consola aún muestra el modelo antiguo (*Productos de compra en aplicaciones*), es lo mismo sin el
paso 5: ID, nombre, descripción, precio, guardar, activar.

### 6.2 La cuenta de servicio para RevenueCat

RevenueCat necesita leer las compras en Play para validarlas. Se hace con una cuenta de servicio de
Google Cloud.

1. `https://console.cloud.google.com`, crea un proyecto (por ejemplo `chroma-play`) o usa uno que ya
   tengas para tus apps.
2. *APIs y servicios > Biblioteca*: habilita **Google Play Android Developer API** y **Google Play
   Developer Reporting API**.
3. *IAM y administración > Cuentas de servicio > Crear cuenta de servicio*: nombre `revenuecat`.
   Roles: **Pub/Sub Editor** y **Monitoring Viewer** (el primero es para las notificaciones del
   paso 6.6). *Listo*.
4. Entra en la cuenta, pestaña *Claves > Agregar clave > Crear clave nueva > JSON*. Se descarga un
   `.json`. **Es un secreto**: guárdalo en `~/keys/` y en el gestor de contraseñas, nunca en el
   repositorio.
5. Copia el correo de la cuenta (`revenuecat@<proyecto>.iam.gserviceaccount.com`).
6. Play Console, *Usuarios y permisos > Invitar a nuevos usuarios*: pega ese correo. En *Permisos de la
   cuenta* marca:
   - Ver información de la aplicación y descargar informes masivos (solo lectura)
   - Ver datos financieros, pedidos y respuestas a la encuesta de cancelación
   - Gestionar pedidos y suscripciones

   *Invitar usuario*. (Para el atajo de `fichas.py --subir`, añade también "Gestionar presencia en
   Play Store".)

Si ya tienes una cuenta de servicio así para Quilt o MoodTraker en RevenueCat, reutiliza el mismo
JSON: los permisos de cuenta cubren todas tus apps.

### 6.3 El proyecto en RevenueCat

1. `https://app.revenuecat.com` > *Create new project*: `Chroma`.
2. *Apps & providers > Add app > Google Play Store*:
   - App name: `Chroma (Play)`
   - Google Play package: `com.baltajmn.color`
   - Service account credentials JSON: sube el `.json` del paso 6.2.
   - *Save*.
3. Las credenciales pueden salir como "Credentials need attention" durante horas (hasta 36): Google
   tarda en propagar los permisos. Truco que suele acelerarlo: en Play Console, edita la descripción
   del producto `pro_lifetime`, guarda, y vuelve a dejarla como estaba. Sigue con los pasos siguientes
   mientras tanto.

### 6.4 Producto, derecho y oferta

En el proyecto Chroma, menú *Product catalog*:

1. **Products** > *New* (o *Import* si ya ve los de Play): tienda Play, identificador `pro_lifetime`,
   tipo compra única (non-consumable / one-time). *Save*.
2. **Entitlements** > *New*: identificador **`pro`** (exacto, en minúscula), descripción `Chroma Pro`.
   *Add*. Dentro, *Attach* y elige `pro_lifetime`.
3. **Offerings** > *New*: identificador `default`, descripción `Pro de por vida`. Dentro, *New
   package*: tipo **Lifetime** (`$rc_lifetime`), y en la columna de Play, `pro_lifetime`. *Save*.
4. En la lista de ofertas, `default` tiene que tener la marca **Current**. Si no la tiene, menú de la
   fila > *Make current*.

Un solo paquete en la oferta: el código compra el primero que encuentre.

### 6.5 La clave pública y la versión 2

1. RevenueCat, *Project settings > API keys* (o dentro de la app de Play): copia la **Public app-specific
   API key**, la que empieza por `goog_`. Es pública por diseño y va dentro del binario.
2. **Nunca** la que empieza por `sk_`: esa es secreta y no entra en el repositorio.
3. Pásamela y la pongo en `Billing.android.kt`, subo el `versionCode` a 2 y genero el AAB. Si lo haces
   tú: sustituye `null` por `"goog_..."` en la línea 4, `versionCode = 2` en
   `androidApp/build.gradle.kts`, y `./gradlew :androidApp:bundleRelease`.
4. Sube ese AAB a **prueba interna** (paso 3, *Crear versión*).

### 6.6 (Opcional, recomendado) Notificaciones en tiempo real

Sin ellas, RevenueCat se entera de un reembolso tarde. Con una compra única es poco frecuente, pero se
configura en dos minutos:

1. RevenueCat, en la app de Play > *Google developer notifications* > *Connect to Google*: elige el
   proyecto de Cloud; RevenueCat crea el tema de Pub/Sub (por eso el rol Pub/Sub Editor). Copia el
   nombre del tema (`projects/.../topics/...`).
2. Play Console, *Monetizar con Play > Configuración de la monetización > Notificaciones para
   desarrolladores en tiempo real*: pega el tema, tipo "Todas las notificaciones", *Guardar* y *Enviar
   notificación de prueba*. En RevenueCat tiene que aparecer como recibida.

---

## 7. Probar la compra

1. Play Console, *Configuración > Prueba de licencias*: añade tu cuenta de Google (y la de quien vaya a
   probar). Respuesta de licencia: `LICENSED`. *Guardar*.
2. En el móvil, con esa cuenta, instala la versión 2 **desde Play** (enlace de prueba interna). Una app
   instalada con `adb` no puede comprar: tiene que venir de Play con su firma.
3. Abre *Ajustes > Chroma Pro* (o toca cualquier etiqueta Pro). Tiene que salir el precio en tu moneda, leído de la
   tienda.
4. *Comprar*: Play ofrece tarjetas de prueba; elige **"Tarjeta de prueba, siempre se aprueba"**. No se
   cobra nada.
5. Pro se enciende: póster, widget del año y el año en palabras sin la etiqueta Pro.
6. En RevenueCat, *Customers*, activa **View sandbox data**: aparece la compra con el derecho `pro`.
7. **Restaurar**: desinstala, vuelve a instalar desde Play, y en *Ajustes > Restaurar compra* (o *Restaurar*
   en el panel de Pro). Pro vuelve sin pagar.
8. Para repetir la prueba desde cero: Play Console, *Monetizar con Play > Gestión de pedidos*, busca el
   pedido y reembólsalo con *Revocar*.

Si el botón de comprar no aparece:

| Síntoma | Causa |
|---|---|
| "La tienda no está disponible" | La app no lleva la clave (versión 1), o la instalación no viene de Play |
| Sale el panel pero sin precio | Oferta sin marca *Current*, paquete sin `pro_lifetime`, o producto sin activar en Play |
| Error al comprar tras aceptar | Credenciales de RevenueCat todavía en "need attention" (paso 6.3) |
| La compra entra pero Pro no se enciende | El derecho no se llama `pro` o no tiene `pro_lifetime` adjunto |

---

## 8. Prueba cerrada y producción

Las cuentas personales creadas después de noviembre de 2023 no pueden publicar en producción sin una
prueba cerrada previa: **12 testers** con la prueba aceptada durante **14 días seguidos**. Si en el
*Panel* de la app te aparece la tarea "Solicitar acceso a producción", te afecta.

1. *Prueba y publicación > Pruebas > Prueba cerrada > Gestionar canal* (el que viene, *Alpha*, sirve).
2. *Testers*: lista de correos con al menos 12 cuentas de Google (amigos, familia; sirven las de
   Gmail). Guarda y copia el enlace para unirse.
3. *Países y regiones*: añade todos, o al menos los de los testers.
4. *Crear versión* > *Añadir de la biblioteca* > la versión 2 > notas de `store/whatsnew/` > *Guardar y
   publicar*. Esta sí pasa revisión (de horas a unos días).
5. Manda el enlace a los 12. Cada uno tiene que abrirlo, pulsar *Convertirme en tester* e instalar desde
   Play. Cuentan desde que aceptan; que no la desinstalen en esos 14 días.
6. Durante la prueba, si arreglas algo, versión nueva con `versionCode` siguiente al mismo canal. No
   reinicia el contador.
7. Pasados los 14 días: *Panel > Solicitar acceso a producción*. Pregunta cómo reclutaste a los
   testers, qué te dijeron y qué cambiaste. Respuestas cortas y sinceras. La revisión tarda hasta 7
   días.
8. Con el acceso concedido: *Producción > Países y regiones* (todos), *Crear versión > Añadir de la
   biblioteca* > la última versión > *Guardar y publicar*. Puedes lanzar por fases (por ejemplo un 20 %)
   y subir al 100 % cuando veas que no hay fallos en *Android vitals*.

Si tu cuenta es de organización o anterior a noviembre de 2023, te saltas los puntos 1 a 7 y vas
directo al 8 cuando termines las secciones 4 y 5 (la consola te lo dirá en el *Panel*).

---

## 9. Después del lanzamiento

- **Amigos (v1.1)**: los enlaces de invitación abren la app gracias a
  `web/.well-known/assetlinks.json`. Necesita la huella SHA-256 de la **clave de firma de la app** (no
  la de subida), que está en *Prueba y publicación > Integridad de la app > Firma de apps*. Se pone en
  lugar de `PLAY_APP_SIGNING_SHA256` cuando llegue v1.1, junto con los formularios 1b de
  `formularios.md`.
- **iOS**: App Store Connect, la misma idea con `appl_` en `Billing.ios.kt`, el producto `pro_lifetime`
  como *No consumible* y `TEAM_ID` y `APP_STORE_ID` en `web/`.
- Cada versión nueva: `versionCode` + 1, `whatsnew` actualizado, `bundleRelease`, comprobar la huella,
  subir.
