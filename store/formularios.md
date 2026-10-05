# Formularios de las dos tiendas, respuesta a respuesta

Todo lo que las consolas preguntan y no tiene API, con la respuesta cerrada y el hecho del código que
la sostiene. Se pegan a mano. Si el código cambia algo de lo que aquí se afirma (un permiso, un SDK,
un dato que sale del teléfono), se cambian en el mismo cambio este fichero, el `index.html` de `BaltaJmn/chroma-privacy` y
`iosApp/iosApp/PrivacyInfo.xcprivacy`.

Las secciones 1 a 6 son v1.0, sin cuentas. Lo que cambia con Amigos (v1.1) está en 1b, 5b y 6b, y
se rellena al publicar la primera versión con `SupabaseConfig` puesto.

Hechos de partida, todos de `docs/tecnico.md`:

- Los días (fecha, color, candidatos, clave del nombre, palabra, foto) viven en `filesDir` en Android
  y en `Application Support` en iOS. No hay servidor, ni cuenta, ni analítica, ni publicidad, ni
  informes de fallos.
- La foto se recodifica a JPEG desde los píxeles (`tecnico.md` 7): no guarda EXIF, así que tampoco
  ubicación. De la foto solo se lee la fecha, para rechazar una de otro día.
- Lo único que sale del teléfono es lo de **RevenueCat**: un identificador anónimo de instalación
  (se configura sin `appUserID`), el historial de compras y datos técnicos del dispositivo.
- Los widgets leen `widget.json`: el color de hoy, su nombre, con Pro los colores del año y, con
  Amigos (v1.2), los colores de hoy de los amigos. Nunca fotos, palabras ni nombres (`tecnico.md` 4.2).
- La notificación tiene siempre el mismo texto (`reminderTitle`, `reminderText`): no lleva nada del
  usuario.
- Android no tiene el permiso `CAMERA`: la foto la hace la cámara del sistema con `TakePicture`, y la
  galería va por `PickVisualMedia`, que no pide permiso.

---

## 1. Play: seguridad de los datos

*Política > Contenido de la aplicación > Seguridad de los datos.*

| Pregunta | Respuesta |
|---|---|
| ¿Tu app recoge o comparte alguno de los tipos de datos obligatorios? | Sí |
| ¿Se cifran en tránsito todos los datos recogidos? | Sí (HTTPS del SDK de RevenueCat) |
| ¿Ofreces una forma de pedir que se borren los datos? | Sí. URL `https://color.baltajmn.dev/delete`: su sección de compras explica cómo borrar lo de RevenueCat por correo con el número de pedido |
| ¿Pueden iniciar sesión con cuentas creadas fuera? | No |
| ¿Permite la app crear una cuenta? | No (v1.0). Por eso no hace falta URL de borrado de cuenta |

Tipos de datos, los únicos dos que se marcan:

| Tipo | Recogido | Compartido | Efímero | Obligatorio | Finalidad |
|---|---|---|---|---|---|
| Información financiera > Historial de compras | Sí | No | No | Sí | Funcionalidad de la app |
| Identificadores de dispositivo u otros identificadores | Sí | No | No | Sí | Funcionalidad de la app |

Lo que **no** se marca, y por qué:

| Tipo | Por qué no |
|---|---|
| Fotos | La foto se copia, reducida y sin metadatos, al almacenamiento privado de la app y no sale de ahí |
| Otro contenido generado por el usuario (colores, palabras) | No sale del dispositivo |
| Ubicación | La app no la pide, y la copia de la foto no guarda el EXIF |
| Mensajes, contactos, salud, actividad | La app no los toca |
| Registros de fallos, diagnóstico | No hay SDK que los mande |

**La copia automática del sistema.** Android puede subir `entries.json` al Drive del usuario con su
copia de seguridad, solo **cifrado de extremo a extremo** (`data_extraction_rules.xml` con
`disableIfNoEncryptionCapabilities="true"`, `backup_rules.xml` con `clientSideEncryption`). Las fotos
solo van en el traspaso directo entre dispositivos. Google excluye de "recogido" lo que va cifrado de
extremo a extremo; es una **inferencia**, porque la ayuda no nombra la copia del sistema, la misma que
en Purl.

## 2. Play: clasificación de contenido (IARC)

| Pregunta | Respuesta |
|---|---|
| Categoría | Utilidad, productividad, comunicación u otras |
| Violencia, sexo, lenguaje soez, drogas, apuestas, miedo | No a todo |
| ¿Los usuarios pueden interactuar o intercambiar contenido? | No en v1.0. La tarjeta se comparte con la hoja del sistema, fuera de la app |
| ¿Comparte la ubicación del usuario? | No |
| ¿Permite comprar bienes digitales? | Sí |
| ¿Contiene anuncios? | No |
| ¿Acceso sin restricciones a internet? | No |

Resultado (enviado el 24-09-2026): PEGI 3, ESRB Everyone, USK 0, IARC 3+; Brasil (ClassInd) 14+ por
las compras. El cuestionario de 2026 pregunta además por cajas de botín, recompensas en metálico o
NFT, navegador y app informativa: no a todo. Correo de IARC: `baltajmn@gmail.com`.

## 3. Play: público objetivo y declaraciones

| Campo | Valor |
|---|---|
| Grupos de edad | 13-15, 16-17, 18 y más |
| ¿Atrae a menores de 13? | No |
| Anuncios | No contiene anuncios |
| Datos de inicio de sesión (antes "Acceso a la app") | **Sí**: la redacción de 2026 cuenta como restringido cualquier pago ("productos únicos") y la autenticación biométrica, y Google no compra con cuentas personales. Pro necesita un código promocional de `pro_lifetime` para el revisor; el bloqueo viene apagado. Play no deja empezar Público objetivo sin esta sección |
| App de noticias, salud, finanzas, gobierno | No |

Con Amigos (v1.1), al publicar la primera versión con servidor:

| Campo | Valor v1.1 |
|---|---|
| Grupos de edad | 16-17, 18 y más. La cuenta exige 16 declarados (`SPEC.md`), y Play mira la app entera |
| Acceso a la app | Parte de la funcionalidad necesita iniciar sesión: una cuenta de Google de prueba, ya con nombre y un amigo aceptado, y los pasos para llegar a Amigos |

Permisos del manifiesto fusionado: `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, del SDK de
RevenueCat `INTERNET`, `ACCESS_NETWORK_STATE` y `com.android.vending.BILLING`, y de androidx.biometric
`USE_BIOMETRIC` (el bloqueo de v1.2; la comprobación la hace el sistema). Ninguno pide
declaración. No se usa `SCHEDULE_EXACT_ALARM` (el recordatorio va con `setAndAllowWhileIdle`), ni
`CAMERA`, ni `READ_MEDIA_IMAGES`. `FOREGROUND_SERVICE` y `WAKE_LOCK` los trae WorkManager a través de
Glance, igual que en Purl. Comprobar la lista sobre el AAB:

```bash
grep -oE '<uses-permission[^>]*android:name="[^"]*"' \
  androidApp/build/intermediates/merged_manifest/release/*/AndroidManifest.xml | sort -u
```

`com.android.vending.BILLING` está en el binario desde la primera subida, así que "¿Tiene compras en
la aplicación?" es **sí** aunque las claves de RevenueCat sean `null`.

## 3b. Play: prueba cerrada y revisor

| Campo | Valor |
|---|---|
| Canal | Prueba cerrada - Alpha, todos los países (177) |
| Testers | Grupo de Google `chroma-testers@googlegroups.com` (excluye las listas de correo) |
| Comentarios | `baltajmn@gmail.com` |
| Enlace de opt-in | `https://play.google.com/apps/testing/com.baltajmn.color`, activo cuando se publique la versión |
| Pro para el revisor | Promoción "Revision de Google Play" (id 131156526): 5 códigos de `pro_lifetime`, del 24-09-2026 al 24-03-2027. Un código va en *Datos de inicio de sesión* |

## 4. Play: ficha, categoría y contacto

| Campo | Valor |
|---|---|
| Tipo | Aplicación |
| Categoría | **Fotografía** |
| Correo de contacto | `baltajmn@gmail.com`, el mismo de la política |
| Sitio web | `https://color.baltajmn.dev/` |
| Teléfono | Vacío |
| Etiquetas | Fotografía, Estilo de vida, Arte y diseño, Personalización (no existen Diario ni Minimalista) |
| Política de privacidad | `https://color.baltajmn.dev/` |

Fotografía y no Estilo de vida como Purl: lo que se hace cada día es una foto, y es donde se busca.

## 1b. Play: seguridad de los datos con Amigos (v1.1)

Todo lo de Amigos es **opcional** (solo con cuenta) y **no compartido**: Supabase y Resend son
encargados del tratamiento, y lo que ven los amigos lo comparte el usuario a propósito, que Play no
cuenta como compartir. Se añade a la tabla de 1:

| Tipo | Recogido | Compartido | Efímero | Obligatorio | Finalidad |
|---|---|---|---|---|---|
| Información personal > Nombre | Sí | No | No | No | Funcionalidad de la app, gestión de la cuenta |
| Información personal > Dirección de correo | Sí | No | No | No | Gestión de la cuenta |
| Información personal > IDs de usuario | Sí | No | No | No | Funcionalidad de la app, gestión de la cuenta |
| Fotos y vídeos > Fotos | Sí | No | No | No | Funcionalidad de la app |
| Actividad en apps > Otro contenido generado por el usuario | Sí | No | No | No | Funcionalidad de la app |

| Pregunta | Respuesta v1.1 |
|---|---|
| ¿Se cifran en tránsito? | Sí: RevenueCat y Supabase, los dos por HTTPS |
| ¿Permite la app crear una cuenta? | Sí, con inicio de sesión de terceros (Apple y Google) |
| URL para borrar la cuenta | `https://color.baltajmn.dev/delete` |
| ¿Se pueden borrar los datos sin borrar la cuenta? | Sí: cualquier día se hace privado y su fila y su foto se borran |

Hechos del código que lo sostienen: el esquema de `supabase/migrations`, la subida de `Outbox.kt`
(foto a 720, recodificada), el borrado de fotos (`purge-photos`: a los 7 días y, además, el barrido de
los ficheros que ninguna tarjeta señala, para que ninguna se quede atrás) y el de la cuenta
(`delete-account`).

## 5. App Store: privacidad de la app

*App Store Connect > Chroma > Privacidad de la app.*

| Pregunta | Respuesta |
|---|---|
| ¿Recoges datos de esta app? | Sí |
| Compras > Historial de compras | Recogido. Finalidades: funcionalidad de la app y análisis de datos, las dos que pide la documentación de RevenueCat. **No** vinculado a la identidad. **No** usado para rastreo. Rellenado en App Store Connect el 05-10-2026 |
| Identificadores | No se marcan. RevenueCat pide *ID de usuario* solo con IDs propios e *ID de dispositivo* solo con integraciones que usen el IDFA, y la app usa su ID anónimo. Así quedaron las cuatro apps de la familia. `PrivacyInfo.xcprivacy` declara además `UserID` y los datos de Amigos (nombre, correo, fotos, contenido): de más, no de menos, hasta que Amigos se habilite y se rellene el apartado 5b |
| El resto de tipos | No recogidos |

Si el informe de privacidad de Xcode sobre el primer archivo añade algo, se añade en los dos sitios.

## 5b. App Store: privacidad con Amigos (v1.1)

Se añade a 5, todo **vinculado** a la identidad (hay cuenta), para funcionalidad de la app y **sin**
rastreo:

| Tipo | Qué es |
|---|---|
| Información de contacto > Nombre | El nombre visible |
| Información de contacto > Correo electrónico | El de Apple o Google, solo para encontrar la cuenta al borrarla |
| Contenido del usuario > Fotos o vídeos | Las fotos compartidas, 7 días |
| Contenido del usuario > Otro contenido del usuario | Colores, nombres de color, palabras, amistades y reportes |
| Identificadores > ID de usuario | Pasa a **vinculado**: el de la cuenta de Amigos |

El servidor guarda además, de uno a dos días, el ID de usuario y la hora de cada enlace de invitación
que no valía (`invite_attempts`), solo para frenar a quien prueba códigos al azar. Cae en lo ya
declarado: ID de usuario, para la funcionalidad de la app (Apple incluye ahí la seguridad y evitar el
fraude), y no añade ningún tipo.

`PrivacyInfo.xcprivacy` ya declara esto. Si se publica una versión sin servidor (`SupabaseConfig`
vacío), el formulario correcto es el de 5 y el manifiesto declara de más, cosa que Apple no rechaza; lo
que no puede pasar nunca es lo contrario.

## 6. App Store: el resto de la ficha

| Campo | Valor |
|---|---|
| Categoría principal | **Fotografía y vídeo** |
| Categoría secundaria | Estilo de vida |
| Clasificación por edad | Cuestionario: **ninguno** en todos los contenidos; **no** en contenido generado por usuarios, mensajería, publicidad, acceso web sin restricciones, temas médicos, concursos y apuestas. Resultado esperado **4+** |
| Derechos de contenido | No contiene ni accede a contenido de terceros |
| Cumplimiento de exportación | No pregunta: `ITSAppUsesNonExemptEncryption = false` en el `Info.plist` |
| Copyright | `2026 Baltasar Jiménez` |
| URL de soporte | `https://color.baltajmn.dev/` (lleva el correo de contacto) |
| Inicio de sesión para la revisión | No hace falta en v1.0 |
| Publicación | Manual, para salir el mismo día que Play |
| Idioma principal | Inglés (EE. UU.): los idiomas sin capturas propias toman las suyas |
| Versión | `1.0.11`, la de Play (`MARKETING_VERSION` en `Config.xcconfig`) |
| Precio y disponibilidad | Gratis; Pro va aparte, como compra integrada. Todos los países menos China continental, que pide un número de registro ICP |
| URL de la política de privacidad | `https://color.baltajmn.dev/` (Privacidad de la app) |
| Textos de la ficha | `store/app-store/<idioma>/`, trece idiomas: nombre, subtítulo, descripción, palabras clave y texto promocional. `tools/store/fichas.py` comprueba los topes |
| Capturas | `store/screenshots/iphone/en-US` y `es-ES`, iPhone de 6,9" (`store/capturas.md`). Los otros once idiomas heredan las de en-US. Sin iPad |
| Estado de comerciante (UE) | Lo declara el titular de la cuenta. Vendiendo una compra integrada lo normal es declararse comerciante, y entonces la dirección, el teléfono y el correo que se den salen en la ficha europea |
| Contacto para la revisión | Nombre, teléfono y correo del titular, solo los ve Apple |

Usos declarados en el `Info.plist`, en los trece idiomas (`<lang>.lproj/InfoPlist.strings`):
`NSCameraUsageDescription` (la foto del día) y `NSPhotoLibraryAddUsageDescription` (guardar una
tarjeta o un póster, solo añadir).

Notas para el revisor, en inglés:

```
Chroma has no account and no server. Everything is stored on the device, so no demo account is needed.

Take a photo (or pick one taken today from the gallery) and choose one of the colors it offers. That color becomes the day; My year shows every day as a grid. The simulator has no camera: use "Choose from today's photos" with a photo taken today.

Chroma Pro is a one-time non-consumable purchase (com.baltajmn.color.pro_lifetime). It opens when exporting the year poster (My year > Poster), from the locked year widget, and from Settings > Chroma Pro. Restore Purchase is in Settings and in the purchase dialog.
```

### Compra integrada

| Campo | Valor |
|---|---|
| Tipo | No consumible |
| Nombre de referencia | `Chroma Pro` |
| ID de producto | `com.baltajmn.color.pro_lifetime`. En Apple un ID no se repite entre apps de la misma cuenta: va el bundle delante y tras el último punto el de Play. Irreversible |
| Precio | 2,99 EUR de base en España (en Apple ya lleva IVA), el escaparate de Play |
| En RevenueCat | App "Chroma (App Store)", mismo derecho `pro` y mismo paquete `$rc_lifetime` que Android; su `appl_` en `Billing.ios.kt` |
| Captura para la revisión | `store/screenshots/iphone/revision-compra.png`: el diálogo de Pro con el botón de compra, sacado del simulador con el precio puesto a mano porque la tienda aún no lo da |

Nombre visible `Chroma Pro` en todos; descripción (tope 45), de `proSubtitle` en `Strings.kt`. Las
ocho últimas salen recortando la misma frase y, como el resto de esos idiomas, no han pasado una
revisión nativa (`docs/textos.md` 3):

| Idioma | Descripción |
|---|---|
| en-US | Year poster, year widget, year in words. |
| es-ES | Póster, widget del año y tu año en palabras. |
| pt-BR | Pôster, widget do ano e seu ano em palavras. |
| de-DE | Jahresposter, Jahres-Widget, Jahr in Worten. |
| fr-FR | Affiche, widget de l'année, année en mots. |
| it | Poster, widget dell'anno, anno in parole. |
| nl-NL | Jaarposter, jaarwidget en je jaar in woorden. |
| pl | Plakat, widżet roku i twój rok w słowach. |
| ru | Постер, виджет года и твой год в словах. |
| tr | Yıl posteri, yıl widget'ı, kelimelerle yılın. |
| id | Poster dan widget tahun, tahunmu dalam kata. |
| ja | 1年のポスター、1年のウィジェット、ことばで振り返る1年。 |
| ko | 한 해 포스터, 한 해 위젯, 글로 보는 한 해. |

## 6b. v1.1: contenido de usuarios

Amigos convierte a Chroma en una app con contenido de usuarios, aunque solo lo vean amigos aceptados.
Lo que cambia al publicar la v1.1 (los datos que salen, en #38):

| Dónde | Cambio |
|---|---|
| Play, IARC | "¿Los usuarios pueden interactuar?": **sí**, con moderación (reportar y bloquear) |
| Play, política de contenido generado por usuarios | Términos con tolerancia cero, reportar dentro de la app, bloquear, y actuación en 24 horas |
| App Store, edad | Contenido generado por usuarios: **sí**, y mensajería o chat: no. El resultado sube, previsiblemente a 12+ |
| Play, público objetivo y acceso | 16 y más, y cuenta de prueba (tabla de 3) |
| App Store, notas al revisor | Una cuenta de prueba con un amigo ya aceptado y días compartidos, y dónde están reportar (mantener pulsada una tarjeta), bloquear (también en cada solicitud), desbloquear (Ajustes > Amigos > Bloqueados) y los términos |
| Play, seguridad de los datos | "¿Los usuarios pueden pedir que se borren sus datos?": **sí**. URL de borrado de la cuenta: `https://color.baltajmn.dev/delete` (`delete.html` de `chroma-privacy`) |
| App Store | Borrar la cuenta desde la app (5.1.1(v)): Ajustes > Amigos > Borrar cuenta |
| Términos | `https://color.baltajmn.dev/terms.html` (`terms.html` de `chroma-privacy`). Se aceptan al crear el nombre, antes de ver nada de nadie, y están en Ajustes > Privacidad |

Apple 1.2 pide los cuatro a la vez: términos aceptados, filtro o reporte de contenido, bloquear, y
actuar en 24 horas. Los reportes llegan por correo (`report-notify`, `store/servidor.md` 3), lanzado desde
la base y reintentado cada hora hasta que sale, y cómo actuar sobre uno está en `store/servidor.md` 7.

## 7. La política: dónde se publica

La política es el `index.html` del repositorio público `BaltaJmn/chroma-privacy`, y se publica en
**https://color.baltajmn.dev/** con GitHub Pages desde su rama `main`, sin flujo de Actions. La misma
URL va en la Play Console, en App Store Connect (política y soporte), en la ficha de Play (sitio web)
y en la app (`PRIVACY_URL`).

Hasta el 01-10-2026 se publicaba desde la carpeta `web/` de este repositorio; al pasar a privado,
Pages dejó de servirla (el plan gratuito no publica desde repositorios privados) y la web se fue a su
repositorio, como las de Quilt y MoodTraker. Pages en modo rama, con `.nojekyll` para que Jekyll no se
coma `.well-known`, dominio propio, y en
Cloudflare (que es quien sirve el DNS de `baltajmn.dev`; Porkbun solo es el registrador) un `CNAME
color` a `baltajmn.github.io` **con proxy**. El HTTPS lo pone Cloudflare; *Enforce HTTPS* de GitHub
se queda sin marcar porque detrás del proxy GitHub no puede emitir certificado. Los pasos para otra
web, en `store/lanzamiento-play.md` sección 2.
