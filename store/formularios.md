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

## 0. Para enviar por la mañana (20-10-2026)

Lo dejó preparado la sesión de la noche del 19 al 20. En orden, cada paso con quién lo hace y lo que
se pega. Estado al acabar la noche: Amigos y grupos probados de punta a punta con dos
emuladores Android y dos cuentas (`store/servidor.md` 6), con los dos fallos que salieron ya
arreglados; la 1.2 (19) etiquetada `v1.2`, camino de Play alfa y TestFlight.

**Recomendación: no envíes la 1.1. Convierte esa misma versión de App Store Connect en la 1.2 y envía
la 1.2.** Por qué:

- Una revisión de Apple en vez de dos. Para el revisor, Amigos y grupos son una misma cosa (contenido
  de usuarios: términos, reportar, bloquear) y piden lo mismo: cuenta de prueba, Privacidad de la app y
  la primera suscripción. Enviar la 1.1 y luego la 1.2 es hacer eso dos veces y esperar dos revisiones.
- Grupos no añaden ningún tipo de dato (1b y 5b): los formularios son idénticos.
- La 1.2 está probada de punta a punta con dos dispositivos y está en Play alfa y TestFlight (build 19).
- Las fichas del repositorio ya cuentan los grupos. Con la 1.1 habría que volver a la ficha anterior.

Lo que arriesgas: si Apple pusiera pegas a algo de grupos, también retrasaría Amigos. Si prefieres no
arriesgar, salta el paso 2 y envía la 1.1 como está; la 1.2 irá después en otra versión
(`appstore.py nueva com.baltajmn.color 1.2`).

### Paso 1. Cuenta de prueba (hecho el 20-10)

Una cuenta de Google de prueba pide SMS o un segundo correo en un iPhone que Google no conoce, así que
el revisor entra por email: `review@baltajmn.dev`, creada a mano en Supabase (la contraseña es tuya y no
está en el repositorio). Es la única alta por email que el servidor admite (`docs/tecnico.md` 8), y la
build 20 trae el enlace *Sign in with email* bajo Apple y Google en Amigos. Sirve también para FlowTime.

Ya montada por SQL: perfil `Alex`, amistad aceptada con Ana (`baltax.75`), grupo `Chroma` con las dos
(Alex de admin). Ana tiene fotos del 19 y el 20, y la tarea `review-demo` de `pg_cron` le comparte un
color cada día a las 00:07 UTC para que el feed nunca se quede vacío durante una revisión (se quita con
`select cron.unschedule('review-demo')`). Si quieres fotos además de colores, comparte una desde Ana
en el emulador el día del envío.

Ojo: en *Información de inicio de sesión* está todavía `baltajmn@gmail.com` con una contraseña puesta.
Se cambia en el paso 3.2.

### Paso 2. Pasar la 1.1 a 1.2 en App Store Connect (Claude, con tu sí, 2 minutos)

**Comprobado el 20-10 por la mañana: App Store Connect ya tiene la versión 1.2 con la build 19, la ficha, las novedades y las notas de este fichero subidas.** Antes de repetir nada, `appstore.py estado com.baltajmn.color`. Dos cosas a mirar: el borrador de envío tiene 1 elemento, y la publicación de la 1.2 está en *Después de la aprobación* (automática), no manual: cámbiala a manual en la página de la versión si quieres salir el mismo día que Play.

Dile a Claude "pasa la 1.1 de Chroma a 1.2 en App Store Connect". Lo hace por la API y lo comprueba con
`appstore.py estado com.baltajmn.color`:

1. Saca la 1.1 del borrador de envío que ya existe (dos elementos, uno es la versión; los dos en
   `READY_FOR_REVIEW`), para que vuelva a *Preparar para el envío*: `appstore.py` no toca una versión
   añadida a revisión. En la web es *Revisión de apps* > el borrador > *Eliminar* en cada fila.
2. Después, desde el repositorio:

```
python3 ~/keys/appstore.py version com.baltajmn.color 1.2
python3 ~/keys/appstore.py build com.baltajmn.color 19
python3 ~/keys/appstore.py ficha com.baltajmn.color ~/AndroidStudioProjects/color
python3 ~/keys/appstore.py novedades com.baltajmn.color ~/AndroidStudioProjects/color
python3 ~/keys/appstore.py notas com.baltajmn.color ~/AndroidStudioProjects/color
```

`build` necesita la 19 ya procesada (`estado` la lista como `('19', 'VALID')`). `ficha` sube la
descripción que cuenta los grupos; `novedades`, las de `store/whatsnew` (Amigos, grupos, fotos 2 días,
Pro mensual: quien viene de la 1.0.14 no ha visto la 1.1); `notas`, las de la sección 6, que ya son las
de la 1.2 (3881 caracteres, tope 4000). `novedades` y `notas` ya están en `BaltaJmn/ci` (commit 2fb0926).

### Paso 3. App Store Connect en la web (tú, 10 minutos)

1. **Privacidad de la app** (menú de la izquierda): *Tipos de datos > Editar*. Deja *Historial de
   compras* como está y marca además:
   - Información de contacto > **Nombre**
   - Información de contacto > **Correo electrónico**
   - Contenido del usuario > **Fotos o vídeos**
   - Contenido del usuario > **Otro contenido del usuario**
   - Identificadores > **ID de usuario**

   *Siguiente* o *Guardar*. Cada uno de los cinco sale con su configuración pendiente; en todos, la
   misma respuesta:
   - Finalidad: solo **Funcionalidad de la app**.
   - ¿Están vinculados a la identidad del usuario?: **Sí**.
   - ¿Se usan para rastreo?: **No**.

   *Historial de compras* sigue con funcionalidad y análisis, no vinculado y sin rastreo. Arriba a la
   derecha, **Publicar**. Es la tabla de 5b; grupos no añade nada.
2. Página de la versión, *Información para la revisión de la app* > **Información de inicio de
   sesión**, con *Se requiere inicio de sesión* marcado. Sustituye lo que hay:
   - Nombre de usuario: `review@baltajmn.dev`
   - Contraseña: la que pusiste en Supabase

   *Guardar*. Las notas no llevan la cuenta: dicen "The email account in Sign-In Information".
3. En la misma página, comprueba: build **20** (la primera con *Sign in with email*), *Notas* empiezan por "What is
   new in 1.2", el vídeo sigue adjunto, y *Novedades de esta versión* en inglés tiene la línea "New:
   groups". Si no hiciste el paso 2 con Claude, pega las notas de la sección 6 y las novedades de
   `store/whatsnew/whatsnew-<idioma>` a mano.
4. **La suscripción** va en el mismo envío por ser la primera de la app. En la página de la versión,
   sección *Compras dentro de la app y suscripciones* > *Seleccionar compras dentro de la app o
   suscripciones* > marca **Chroma Pro monthly** (`com.baltajmn.color.pro_monthly`) > *Listo*. Si no
   la deja marcar porque dice *Faltan metadatos*, ábrela en *Suscripciones > Pro*: la web marca en rojo
   lo que falta. Por la API tiene los 13 idiomas, el grupo con sus 13, precio en 174 países, la captura
   para la revisión y disponibilidad, y aun así la API dice `MISSING_METADATA` en las cuatro apps de la
   familia.
5. Arriba a la derecha, **Añadir a revisión**. En el borrador deben salir al menos la versión y la
   suscripción. **Enviar a revisión**.

### Paso 4. Google Play (tú, 5 minutos)

0. **Clasificación de contenido (IARC): ya enviada.** La sesión de privacidad la vio enviada en Play el 19-10-2026 a las 18:16, en el navegador. No la repitas; si quieres comprobarlo, *Política y programas > Contenido de la aplicación > Clasificación de contenido*.

1. *Política y programas > Contenido de la aplicación > Acceso a la aplicación > Gestionar*: "Todas o
   algunas funciones están restringidas" > *Añadir instrucciones*. Nombre `Cuenta de prueba`, usuario
   `review@baltajmn.dev` y su contraseña, y en *Otra información* (ya puesto en el diálogo el 20-10):

```
Friends tab > Sign in with email (below Continue with Google) with this email and password. The account has a display name (Alex), an accepted friend (Ana) and a group (Chroma), and Ana shares a color every day. Report and block: the three-dots button on any card. Unblock: Settings > Friends > Blocked.
```

   Guárdalo con la build 20 ya en alfa: la 19 no tiene el enlace de email.

   *Guardar*, y si lo pide, *Resumen de publicación > Enviar cambios a revisión*.
2. Producción, cuando quieras (la 1.2 ya está en alfa): primero la ficha nueva, que dice grupos, con
   `gh workflow run listings.yml -f target=play` (o dile a Claude "sube la ficha de Chroma a Play");
   después *Prueba y publicación > Pruebas > Prueba cerrada (alfa) > Versiones > 1.2 (19) >
   Promocionar versión > Producción*, todos los países, *Guardar* y *Enviar a revisión*. La 1.1 de
   alfa no hace falta en producción: la 1.2 la lleva entera.

### Si prefieres enviar la 1.1 ahora

Pasos 1, 3 y 4.1 tal cual, sin el 2. En App Store, las notas y novedades de la 1.1 ya están puestas.
En Play no promociones la 1.1 a producción con la ficha del repositorio: habla de grupos. Después de
aprobada, la 1.2: `appstore.py nueva com.baltajmn.color 1.2` y los comandos del paso 2 desde
`version`.

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

Con grupos (v1.1, `20261020120000_groups.sql`) esta tabla **no cambia**: el nombre del grupo, quién
está dentro y desde cuándo, y los expulsados son contenido y IDs de usuario ya declarados, y los
miembros de un grupo ven lo que el usuario comparte a propósito (hoy y ayer), igual que los amigos, así
que sigue sin contar como compartir. Al guardar, no hay que marcar nada nuevo.

Hechos del código que lo sostienen: el esquema de `supabase/migrations`, la subida de `Outbox.kt`
(foto a 720, recodificada), el borrado de fotos (`purge-photos`: a los 2 días y, además, el barrido de
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
| Contenido del usuario > Fotos o vídeos | Las fotos compartidas, 2 días |
| Contenido del usuario > Otro contenido del usuario | Colores, nombres de color, palabras, amistades y reportes |
| Identificadores > ID de usuario | Pasa a **vinculado**: el de la cuenta de Amigos |

El servidor guarda además, de uno a dos días, el ID de usuario y la hora de cada enlace de invitación
que no valía (`invite_attempts`), solo para frenar a quien prueba códigos al azar. Cae en lo ya
declarado: ID de usuario, para la funcionalidad de la app (Apple incluye ahí la seguridad y evitar el
fraude), y no añade ningún tipo.

Grupos no añaden ningún tipo: el nombre del grupo y la pertenencia caen en *Otro contenido del
usuario* (y en *ID de usuario* para quién está dentro). Al describir *Otro contenido del usuario* no hace
falta texto libre en la consola, pero si se pide, decir "colores, palabras, amistades, grupos y reportes".

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
| Versión | La de Play: `MARKETING_VERSION` en `Config.xcconfig` va igual que `versionName` |
| Precio y disponibilidad | Gratis; Pro va aparte, como compra integrada. Todos los países menos China continental, que pide un número de registro ICP |
| URL de la política de privacidad | `https://color.baltajmn.dev/` (Privacidad de la app) |
| Textos de la ficha | `store/app-store/<idioma>/`, trece idiomas: nombre, subtítulo, descripción, palabras clave y texto promocional. `listings.yml` comprueba los topes |
| Capturas | `store/screenshots/iphone/en-US` y `es-ES`, iPhone de 6,9" (`store/capturas.md`). Los otros once idiomas heredan las de en-US. Sin iPad |
| Estado de comerciante (UE) | Lo declara el titular de la cuenta. Vendiendo una compra integrada lo normal es declararse comerciante, y entonces la dirección, el teléfono y el correo que se den salen en la ficha europea |
| Contacto para la revisión | Nombre, teléfono y correo del titular, solo los ve Apple |

Cómo quedó en App Store Connect el 05-10-2026, al enviar la 1.0.12 (15) a revisión. Lo que estaba
mal o faltaba, por si vuelve a pasar en otra app de la cuenta:

- El idioma principal era español: se pasó a inglés, que es de donde heredan las capturas.
- China continental estaba disponible: se quitó (`territoryAvailabilities`).
- La versión en preparación era la 1.0.11: tiene que ser la `versionName` de la build.
- La ficha estaba en cinco idiomas y la compra integrada también, sin captura para la revisión. Los
  ocho idiomas que faltaban salen de `store/app-store/` con `~/keys/appstore.py ficha`, y la compra,
  de la tabla de abajo.
- **La primera compra no consumible va en el mismo envío que la versión.** La API no la añade
  (`FIRST_NON_CONSUMABLE_MUST_BE_SUBMITTED_ON_VERSION`): en la web, *Añadir a revisión* en la versión
  y después en la compra, y se envían juntas. El primer envío salió sin ella: se canceló y a las
  23:10 se reenvió con las dos (versión y compra en `WAITING_FOR_REVIEW`, publicación manual).
- TestFlight: grupo interno `Equipo` con el titular, para instalar cada build en el iPhone.
- El estado de comerciante de la UE se declaró el 05-10-2026 y queda en revisión en Apple: hasta que lo
  aprueben, los 27 países de la UE salen como `TRADER_STATUS_NOT_PROVIDED`.

`~/keys/appstore.py estado com.baltajmn.color` dice qué falta antes de enviar.

Usos declarados en el `Info.plist`, en los trece idiomas (`<lang>.lproj/InfoPlist.strings`):
`NSCameraUsageDescription` (la foto del día) y `NSPhotoLibraryAddUsageDescription` (guardar una
tarjeta o un póster, solo añadir).

Notas para el revisor, en inglés. El 06-10-2026 Apple paró el primer envío con la 2.1 *Information
Needed*, la que manda a las cuentas con poco historial de revisiones: pidió un vídeo grabado en un
iPhone de verdad con la última versión de iOS, desde que se abre la app y pasando por la compra, y
siete respuestas que desde entonces van en las notas de cada envío, con su numeración. El vídeo va
como archivo adjunto de la información para la revisión. Estas son las de la 1.2 (Amigos con grupos y
el Pro mensual); `appstore.py notas` sube este bloque, que Apple corta en 4000 caracteres. La cuenta
de prueba no va aquí sino en *Información de inicio de sesión*, para que ningún hueco sin rellenar
llegue a Apple.

```
What is new in 1.2: Friends, an optional private social feature, now with small groups, and a monthly option for Chroma Pro.

1. Screen recording
The attached recording (physical iPhone, latest iOS) shows the core flow and a sandbox purchase of Chroma Pro; Friends and groups are in point 3.

2. Purpose and audience
A one-photo-a-day color diary: the app finds up to five colors in your photo and you choose one; My year paints every day as a grid. The diary needs no account and stays on the device. Friends is optional: the color of the day of friends you invite yourself, and of small groups you join.

3. How to use it
- Today: "Take a photo" or "Choose from today's photos" (only photos taken today), then pick a color.
- Friends tab: sign in with Apple or Google, enter a display name and confirm you are 16 or older. The Terms of use (zero tolerance for objectionable content) are accepted there, before seeing anything from anyone. Invite friends with a link; nothing is visible until both sides accept. Friends see each other's shared color of the day, and the photo only if it was shared. Shared photos are deleted from the server after 2 days. No likes, comments, counters or public profiles.
- Groups: in the Friends tab, the row of buttons under the title ("Friends", one per group, "+ New group"). Create one with a name; others join with its link (up to 20 members). A member sees only what the other members share for today and yesterday; joining does not make people friends. The three-dots button next to the group name opens Members and Leave group. The creator is the admin: in Members, Remove takes someone out, and that person cannot rejoin with the link.
- Report: the three-dots button on any card (or long-press the card), in the friends feed or in a group, then Report. The card is hidden at once and the report reaches us by email; we act within 24 hours (remove the content and, if needed, the account).
- Block: in the same menu, and on every incoming friend request. A blocked person disappears from your friends and from every group you share. Unblock in Settings > Friends > Blocked.
- Delete account: Settings > Friends > Delete account. It deletes everything on the server (profile, shared days and photos, friendships, groups, sign-in account); the local diary stays on the device.
- Terms: https://color.baltajmn.dev/terms.html, also in Settings > Privacy.

4. Demo account
The email account in Sign-In Information: in the Friends tab, tap "Sign in with email" below the Apple and Google buttons. Email sign-in exists only for this review account; everyone else uses Apple or Google. It has a display name (Alex), an accepted friend (Ana) and a group (Chroma) with her; Ana shares a color every day, so there are cards to report and block.

5. External services
- Apple In-App Purchase and RevenueCat (revenuecat.com), to validate purchases. RevenueCat receives the purchase and an anonymous ID.
- Supabase (supabase.com), our server for Friends only, in the EU (Frankfurt): account, display name, friendships, groups and what the user chooses to share.
- Resend (resend.com), to email us content reports.
No analytics, no ads, no AI services. Colors are extracted on the device.

6. Regions and other
Same everywhere; all countries but mainland China; 13 languages. No regulated industry or third-party material.

7. In-App Purchase
Chroma Pro, two ways, same unlock: a non-consumable lifetime purchase (com.baltajmn.color.pro_lifetime, 1.99 EUR, highlighted) and an auto-renewing monthly subscription (com.baltajmn.color.pro_monthly, 0.99 EUR per month, cancel any time in App Store settings). The dialog shows both prices, renewal and cancellation terms, and links to the terms of use and privacy policy. Pro unlocks the year poster, the year widget and the year in short sentences; everything else, including all of Friends and groups, is free. It opens from Settings > Chroma Pro and My year > Poster; Restore is in Settings and in the dialog.
```

### Compra integrada

| Campo | Valor |
|---|---|
| Tipo | No consumible |
| Nombre de referencia | `Chroma Pro` |
| ID de producto | `com.baltajmn.color.pro_lifetime`. En Apple un ID no se repite entre apps de la misma cuenta: va el bundle delante y tras el último punto el de Play. Irreversible |
| Precio | 1,99 EUR de base en España (con IVA), igual en las dos tiendas con `compra.py` de `BaltaJmn/ci` (las dos desde el 06-10-2026) |
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

Grupos (20-10-2026): la política, los términos y la página de borrado de `chroma-privacy` ya dicen que
los miembros de un grupo ven hoy y ayer, que se guarda nombre, pertenencia y expulsados, y que borrar la
cuenta quita de los grupos (commit `533fd2c`, publicado por Pages). `PrivacyInfo.xcprivacy` cubre grupos
sin cambios. Para la consola no hay nada nuevo que marcar en Seguridad de los datos ni en Privacidad de la
app. La ficha de los 13 idiomas cuenta los grupos desde el 20-10-2026 y sale con la 1.2: ya no se
puede subir con la 1.1, que no los tiene.

Estado el 19-10-2026, con Amigos ya en `main`:

- Play, *Seguridad de los datos*: guardada con la tabla de 1b (cuenta por OAuth, URL de borrado de la
  cuenta y borrado parcial: sí). Está en *Resumen de publicación*, sin enviar a revisión: se envía con la
  primera versión con Amigos.
- Play, *Público objetivo*: 16-17 y 18 y más, guardado igual, sin enviar.
- Play, IARC: cuestionario nuevo rellenado y sin guardar (guardarlo pide clasificación nueva). Respuestas:
  "Todos los demás tipos"; contenido descargado sujeto a clasificación: no; interactúan o intercambian
  contenido: **sí**; el contenido de usuarios es el origen principal: no; desnudos o violencia gráfica en
  público: no; bloquear: sí; reportar: sí; moderación de chat: no; limitado a amigos invitados: sí;
  contenido en línea: no; productos con restricción de edad: no; ubicación con otros: no; compras
  digitales: sí; cajas de botín, recompensas en metálico, navegador, informativa: no.
- Play, *Datos de inicio de sesión*: falta una cuenta de Google de prueba, con su contraseña, que ya tenga
  nombre y un amigo aceptado. Google no crea cuentas ni usa las personales.
- App Store, *Privacidad de la app*: se rellena (tabla de 5b) al enviar la v1.1, no antes. Los cambios se
  publican al momento y Apple pide que reflejen la versión que está a la venta, que hoy no tiene Amigos.
  La clasificación por edad y las notas al revisor van también con esa versión.

Estado el 19-10-2026 por la noche, con la 1.1 (18) subida a Play alpha y TestFlight (`v1.1`):

- App Store, versión 1.1 creada por la API (`AFTER_APPROVAL`), con la build 18, las novedades de
  `store/whatsnew`, la ficha nueva de los trece idiomas (`appstore.py ficha`) y el vídeo de la 1.0 como
  adjunto de la revisión.
- App Store, edad (por la API, en la ficha de la 1.1): contenido generado por usuarios **sí**, red social
  **sí** (Amigos es un feed de lo que comparten otros; Apple lo pide desde septiembre de 2026 y da 13+),
  restringida por edad **no** (eso exige la API de rango de edad declarado), mensajería **no**. El resto,
  como en 6.
- App Store, notas al revisor: las de 6 reescritas para la 1.1 (Amigos, cuenta de prueba, reportar,
  bloquear, borrar la cuenta, Supabase y Resend como servicios), 3918 caracteres, en la ficha de la 1.1.
  Falta la cuenta de prueba en *Información de inicio de sesión*.
- App Store, suscripción `pro_monthly`: tenía `MISSING_METADATA` por la captura para la revisión. Nueva
  `revision-compra.png` (el diálogo de la 1.1 con vitalicio y mensual, precios puestos a mano solo en esa
  build) subida el 19-10-2026; en la web queda *En preparación para el envío*. Es la primera suscripción:
  va en el mismo envío que la 1.1 (*Añadir a revisión* en la suscripción, del titular).
- App Store, *Privacidad de la app*: *Tipos de datos > Editar > Siguiente*, marcar Nombre, Correo
  electrónico, Fotos o vídeos, Otro contenido del usuario e ID de usuario, además de Compras. Ese diálogo
  solo tiene *Publicar*, que es del titular (se marcaron, pero la pestaña se cerró sin publicar). Después, cada tipo nuevo pide su configuración: finalidad
  **Funcionalidad de la app**, **vinculado** a la identidad, **sin** rastreo. Compras sigue como estaba.
- Play, IARC: enviado el 19-10-2026 a las 18:16 (visto en el navegador por la sesión de privacidad). Las
  respuestas de arriba son las que se usaron.
- Play, ficha (`store/listings`): actualizada en el repositorio, sin subir. Sale a Play con la 1.1 a
  producción, no antes: la ficha de producción describe la 1.0.14, que no tiene Amigos.

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
