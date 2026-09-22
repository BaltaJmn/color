# Formularios de las dos tiendas, respuesta a respuesta

Todo lo que las consolas preguntan y no tiene API, con la respuesta cerrada y el hecho del código que
la sostiene. Se pegan a mano. Si el código cambia algo de lo que aquí se afirma (un permiso, un SDK,
un dato que sale del teléfono), se cambian en el mismo commit este fichero, `web/index.html` y
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
- Los widgets leen `widget.json`: el color de hoy, su nombre y, con Pro, los colores del año. Nunca
  fotos ni palabras (`tecnico.md` 4.2).
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
| ¿Ofreces una forma de pedir que se borren los datos? | Sí: por correo, lo explica la política |
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

Resultado esperado: PEGI 3, ESRB Everyone, USK 0.

## 3. Play: público objetivo y declaraciones

| Campo | Valor |
|---|---|
| Grupos de edad | 13-15, 16-17, 18 y más |
| ¿Atrae a menores de 13? | No |
| Anuncios | No contiene anuncios |
| Acceso a la app | Toda la funcionalidad disponible sin restricciones ni inicio de sesión |
| App de noticias, salud, finanzas, gobierno | No |

Con Amigos (v1.1), al publicar la primera versión con servidor:

| Campo | Valor v1.1 |
|---|---|
| Grupos de edad | 16-17, 18 y más. La cuenta exige 16 declarados (`SPEC.md`), y Play mira la app entera |
| Acceso a la app | Parte de la funcionalidad necesita iniciar sesión: una cuenta de Google de prueba, ya con nombre y un amigo aceptado, y los pasos para llegar a Amigos |

Permisos del manifiesto fusionado: `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, y del SDK de
RevenueCat `INTERNET`, `ACCESS_NETWORK_STATE` y `com.android.vending.BILLING`. Ninguno pide
declaración. No se usa `SCHEDULE_EXACT_ALARM` (el recordatorio va con `setAndAllowWhileIdle`), ni
`CAMERA`, ni `READ_MEDIA_IMAGES`. `FOREGROUND_SERVICE` y `WAKE_LOCK` los trae WorkManager a través de
Glance, igual que en Purl. Comprobar la lista sobre el AAB:

```bash
grep -oE '<uses-permission[^>]*android:name="[^"]*"' \
  androidApp/build/intermediates/merged_manifest/release/*/AndroidManifest.xml | sort -u
```

`com.android.vending.BILLING` está en el binario desde la primera subida, así que "¿Tiene compras en
la aplicación?" es **sí** aunque las claves de RevenueCat sean `null`.

## 4. Play: ficha, categoría y contacto

| Campo | Valor |
|---|---|
| Tipo | Aplicación |
| Categoría | **Fotografía** |
| Correo de contacto | `baltajmn@gmail.com`, el mismo de la política |
| Sitio web | `https://color.baltajmn.dev/` |
| Teléfono | Vacío |
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
(foto a 720, recodificada), el borrado de fotos a los 7 días (`purge-photos`) y el de la cuenta
(`delete-account`).

## 5. App Store: privacidad de la app

*App Store Connect > Chroma > Privacidad de la app.*

| Pregunta | Respuesta |
|---|---|
| ¿Recoges datos de esta app? | Sí |
| Compras > Historial de compras | Recogido. Finalidad: funcionalidad de la app. **No** vinculado a la identidad. **No** usado para rastreo |
| Identificadores > ID de usuario | Recogido. Funcionalidad de la app. No vinculado. No rastreo |
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

Usos declarados en el `Info.plist`, en los cinco idiomas (`<lang>.lproj/InfoPlist.strings`):
`NSCameraUsageDescription` (la foto del día) y `NSPhotoLibraryAddUsageDescription` (guardar una
tarjeta o un póster, solo añadir).

Notas para el revisor, en inglés:

```
Chroma has no account and no server. Everything is stored on the device, so no demo account is needed.

Take a photo (or pick one taken today from the gallery) and choose one of the colors it offers. That color becomes the day; My year shows every day as a grid. The simulator has no camera: use "From the gallery" with a photo taken today.

Chroma Pro is a one-time non-consumable purchase (pro_lifetime). It opens when exporting the year poster (My year > Poster), from the locked year widget, and from Settings > Chroma Pro. Restore Purchase is in Settings and in the purchase dialog.
```

## 6b. v1.1: contenido de usuarios

Amigos convierte a Chroma en una app con contenido de usuarios, aunque solo lo vean amigos aceptados.
Lo que cambia al publicar la v1.1 (los datos que salen, en #38):

| Dónde | Cambio |
|---|---|
| Play, IARC | "¿Los usuarios pueden interactuar?": **sí**, con moderación (reportar y bloquear) |
| Play, política de contenido generado por usuarios | Términos con tolerancia cero, reportar dentro de la app, bloquear, y actuación en 24 horas |
| App Store, edad | Contenido generado por usuarios: **sí**, y mensajería o chat: no. El resultado sube, previsiblemente a 12+ |
| Play, público objetivo y acceso | 16 y más, y cuenta de prueba (tabla de 3) |
| App Store, notas al revisor | Una cuenta de prueba con un amigo ya aceptado y días compartidos, y dónde están reportar (mantener pulsada una tarjeta), bloquear y los términos |
| Play, seguridad de los datos | "¿Los usuarios pueden pedir que se borren sus datos?": **sí**. URL de borrado de la cuenta: `https://color.baltajmn.dev/delete` (`web/delete.html`) |
| App Store | Borrar la cuenta desde la app (5.1.1(v)): Ajustes > Amigos > Borrar cuenta |
| Términos | `https://color.baltajmn.dev/terms.html` (`web/terms.html`). Se aceptan al crear el nombre, antes de ver nada de nadie, y están en Ajustes > Privacidad |

Apple 1.2 pide los cuatro a la vez: términos aceptados, filtro o reporte de contenido, bloquear, y
actuar en 24 horas. Los reportes llegan por correo (`report-notify`, `store/servidor.md` 3).

## 7. La política: dónde se publica

`web/index.html` se publica en **https://color.baltajmn.dev/** con GitHub Pages desde este mismo
repositorio (`.github/workflows/pages.yml` sube la carpeta `web/`). La misma URL va en la Play Console,
en App Store Connect (política y soporte), en la ficha de Play (sitio web) y en la app (`PRIVACY_URL`).

Pasos a mano, una vez:

1. *Settings > Pages > Build and deployment > Source*: **GitHub Actions**. Lanzar el flujo
   (`gh workflow run pages.yml`) y comprobar que queda en `https://baltajmn.github.io/color/`.
2. En **Cloudflare**, zona `baltajmn.dev`: registro `CNAME`, nombre `color`, destino
   `baltajmn.github.io`, **sin proxy**, para que GitHub pueda emitir el certificado.
3. Con `dig +short color.baltajmn.dev` resolviendo, dominio propio y HTTPS:

   ```bash
   gh api -X PUT repos/BaltaJmn/color/pages -f cname=color.baltajmn.dev
   gh api -X PUT repos/BaltaJmn/color/pages -F https_enforced=true
   ```

El orden importa: con el dominio puesto antes de que el DNS resuelva, Pages redirige a un dominio que
todavía no existe. Comprobación final: `curl -sI https://color.baltajmn.dev/` devuelve 200.
