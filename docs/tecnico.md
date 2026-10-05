# Contrato técnico de Chroma

Todo lo que el código tiene que respetar. El porqué está en `SPEC.md`; la interfaz, en
`docs/pantallas.md`; los textos, en `docs/textos.md`. Si el código necesita algo que aquí no está, se
decide, se escribe aquí en el mismo cambio y se sigue.

La plantilla es **Purl** (`../line`): casi todo lo que no es color ni amigos sale de allí. La sección
3 dice de qué fichero sale cada uno.

---

## 1. Identificadores

| Qué | Valor |
|---|---|
| Nombre bajo el icono | `Chroma` (los cinco idiomas) |
| Nombre de ficha | `Chroma: diario de color` y su versión en cada idioma (`store/listings/`): "diario" es lo que se busca, y separa a Chroma de Razer Chroma o Chroma DB |
| `applicationId` y `namespace` de `androidApp` | `com.baltajmn.color` |
| `namespace` de `shared` | `com.baltajmn.color.shared` |
| Paquete Kotlin | `com.baltajmn.color` |
| Bundle id de la app iOS | `com.baltajmn.color` (`APP_BUNDLE_ID` en `Config.xcconfig`) |
| Bundle id del widget iOS | `com.baltajmn.color.widget` |
| Dispositivos iOS | Solo iPhone (`TARGETED_DEVICE_FAMILY = 1`, app y widget). El iPad se puede añadir en una actualización, pero App Store Connect no deja quitarlo una vez publicado. En un iPad se instala en modo compatibilidad |
| Target y producto del widget | `ChromaWidget`, `ChromaWidgetExtension` |
| App Group | `group.com.baltajmn.color` |
| Kinds de WidgetKit | `ChromaTodayWidget`, `ChromaYearWidget` |
| Esquema de URL | `com.baltajmn.color` (`://today`, `://year`, `://pro`, `://friends`) |
| Enlaces universales | `https://color.baltajmn.dev/i/<code>` |
| Canal de notificación Android | `color-daily` |
| Entitlement RevenueCat | `pro` (producto `pro_lifetime`, offering `default`) |
| Política de privacidad | `https://color.baltajmn.dev/` |
| Borrado de cuenta (web) | `https://color.baltajmn.dev/delete` |
| Nombre del zip de copia | `chroma-AAAA-MM-DD.zip` |
| Proyecto de Supabase | `chroma`, región `eu-central-1` |
| `versionCode` / `versionName` inicial | `1` / `1.0`; iOS `MARKETING_VERSION = 1.0`, `CURRENT_PROJECT_VERSION = 1` |

---

## 2. Versiones y dependencias

Las de Purl (`../line/gradle/libs.versions.toml`): AGP 9.0.1, SDK 36, Kotlin 2.4.10, Compose
Multiplatform 1.11.1, material3 1.11.0-alpha07, kotlinx-datetime 0.8.0, kotlinx-serialization
1.11.0, Glance 1.1.1, purchases-kmp 3.2.1, biometric 1.1.0.

**minSdk 26, no 24 como Purl** (1.0.6): kotlinx-datetime envuelve `java.time`, que no existe antes de
Android 8.0, y sin desugaring la app no abría en Android 7. Subirlo quita también `NotificationChannel`
sin guarda y una plataforma que nadie ha probado; Android 7 es una fracción mínima de los móviles
activos. Purl y las hermanas tienen el mismo fallo.

En v1.1 se añaden:

| Librería | Versión | Para qué |
|---|---|---|
| `io.github.jan-tennert.supabase:bom` | 3.8.0 | Auth, Postgrest, Storage, Functions |
| `io.github.jan-tennert.supabase:compose-auth` | la del bom | Google nativo (Credential Manager) y Apple nativo |
| `io.ktor:ktor-client-okhttp` / `ktor-client-darwin` | 3.5.1, la que pide supabase-kt 3.8.0 | Motor HTTP de cada plataforma |

Nada más. No hay librería de imágenes: las fotos se decodifican con el sistema (`BitmapFactory`,
`UIImage`) y se cachean como en Purl. No hay librería de QR: el QR se genera en común (6.10).

---

## 3. Árbol de ficheros

### `shared/src/commonMain/kotlin/com/baltajmn/color`

| Fichero | Qué | Sale de |
|---|---|---|
| `App.kt` | `enum Screen { Today, Year, Friends, Settings }`, capas encima (día abierto, compartir, paywall, bloqueo) | Purl `App.kt` |
| `model/Entry.kt` | `ChromaEntry`, `Share`, `Settings`, `JournalFile`, `ExportFile`, `JournalJson` | Purl, reescrito |
| `model/DayClock.kt` | `logicalDate`, `isoKey` | Purl, sin años anteriores |
| `color/Lab.kt` | `Lab`, sRGB <-> Lab, `deltaE`, `chroma`, hex | Nuevo |
| `color/Extract.kt` | `extractSwatches(pixels): List<Swatch>` | Nuevo |
| `color/Names.kt` | Tabla de nombres y `nearestName` | Nuevo |
| `color/Contrast.kt` | Luminancia relativa y `inkFor(color)` | Nuevo |
| `color/Week.kt` | Color de la semana (v1.2) | Nuevo |
| `data/ChromaRepository.kt` | Fuente única: cargar, editar, guardar, fotos, importar | Purl `LineRepository.kt` |
| `data/Storage.kt` | `expect object Storage` | Purl tal cual |
| `data/Photos.kt` | Caché en memoria de fotos decodificadas | Purl |
| `data/Capture.kt` | `expect object Capture`: cámara, galería, fecha y píxeles | Nuevo |
| `data/Zip.kt`, `data/Export.kt`, `data/Merge.kt` | Copia en zip | Purl, adaptados al modelo |
| `data/Reminder.kt`, `data/ReminderPlan.kt` | Recordatorio | Purl tal cual |
| `data/WidgetState.kt`, `data/Widgets.kt` | `widget.json` | Purl, otro contenido |
| `data/Lock.kt`, `ui/LockScreen.kt` | Biometría (v1.2): el bloqueo del teléfono, nunca un PIN propio | Purl, sin el efecto en el recordatorio (el de Chroma nunca cita nada) |
| `data/FilePicker.kt`, `data/AppInfo.kt`, `data/Route.kt` | Selector de ficheros, versión y enlaces | Purl |
| `share/ShareCard.kt`, `share/Sharing.kt` | Tarjeta y póster a PNG, hoja de compartir | Purl, otro dibujo |
| `social/Social.kt` | Cliente de Supabase y sesión | Nuevo (v1.1) |
| `social/Outbox.kt` | Cola de subida | Nuevo (v1.1) |
| `social/Friends.kt` | Amigos, solicitudes, feed, mosaicos | Nuevo (v1.1) |
| `social/Qr.kt` | Codificador QR | Nuevo (v1.1) |
| `social/Friends.kt` | Amigos, solicitudes, enlaces de invitación | Nuevo (v1.1) |
| `ui/InviteScreen.kt` | El enlace, su QR, compartir y regenerar | Nuevo (v1.1) |
| `color/Week.kt`, `color/Stats.kt` | Color de la semana y estadísticas | Nuevo (v1.2) |
| `ui/StatsScreen.kt` | El año en frases | Nuevo (v1.2) |
| `ui/FriendYear.kt` | Lista de amigos, el año de uno, uno de sus días, y su menú (reportar, bloquear, quitar) | Nuevo (v1.1) |
| `ui/FriendsBlocked.kt` | El diálogo de Ajustes > Amigos > Bloqueados: quién bloqueé y cómo deshacerlo | Nuevo (v1.1) |
| `ui/Card.kt` | La tarjeta | Nuevo |
| `ui/TodayScreen.kt`, `ui/YearScreen.kt`, `ui/SettingsScreen.kt`, `ui/FriendsScreen.kt` | Pantallas | Purl y nuevo |
| `ui/DaySheet.kt`, `ui/ShareScreen.kt`, `ui/Pro.kt`, `ui/LockScreen.kt`, `ui/Icons.kt` | Capas y dibujos | Purl |
| `ui/theme/Theme.kt` | Tema neutro | Purl, otra paleta |
| `billing/Billing.kt` | RevenueCat | Purl tal cual |
| `i18n/Strings.kt` | Todos los textos | Purl, otros textos |

### `androidMain` e `iosMain`

Los `actual` de `Storage`, `Photos`, `Capture`, `Reminder`, `Widgets`, `Lock`, `FilePicker`,
`AppInfo`, `Sharing`, `Billing` y `systemLanguage`, más los widgets de Glance (`widget/`) y
`MainViewController.kt`. Salen de Purl salvo `Capture`.

### Resto

`androidApp`, `iosApp` (con `ChromaWidget/`), `supabase/` (v1.1: `migrations/` y `functions/`),
`store/`, `tools/`, `.github/workflows/`. La web (política, invitación y borrado de cuenta) está en el
repositorio público `BaltaJmn/chroma-privacy`.

---

## 4. Formatos de datos

### 4.1 `entries.json`

```json
{
  "version": 1,
  "entries": {
    "2026-09-22": {
      "color": "#3A6EA5",
      "swatches": ["#3A6EA5", "#D9C7A7", "#2E2A24", "#8FA37A", "#C24E3A"],
      "name": "storm_blue",
      "word": "lluvia",
      "photo": "p-3f9a1c2e.jpg",
      "share": "private",
      "at": 1790000000000
    }
  },
  "settings": { "reminderOn": false, "defaultShare": "private", "pro": false }
}
```

```kotlin
@Serializable enum class Share { @SerialName("private") Private, @SerialName("color") Color, @SerialName("photo") Photo }

@Serializable
data class ChromaEntry(
    val color: String,                 // "#RRGGBB", mayúsculas
    val swatches: List<String>,        // los candidatos de la foto, en el orden en que se ofrecieron
    val name: String,                  // clave de la tabla de nombres (6.3)
    val word: String? = null,          // hasta WORD_MAX puntos de código
    val photo: String? = null,         // fichero en photos/
    val share: Share = Share.Private,
    val at: Long = 0,                  // epoch ms de la última edición: orden del feed
)
```

- La clave es la fecha **local** del día lógico. Solo se crea o edita la entrada de hoy (6.1).
- `Settings`: `reminderOn`, `reminderHour`, `reminderMinute`, `reminderOffered`, `lockOn`,
  `lastBackup`, `backupNoticeDone`, `pro` (como Purl), más `defaultShare: Share = Private`,
  `shareAsked: Boolean`, `weekColorOn: Boolean = true`, `watermark: Boolean = true`,
  `hiddenCards: List<String>` (`<author>/<day>` de cada día reportado: oculto para siempre, porque
  los reportes no se pueden leer desde la app), `quarantineSeen: Int` (ficheros de `corrupt/` de los
  que el usuario ya fue avisado).
- `JournalJson`: `ignoreUnknownKeys`, sin valores por defecto ni nulos explícitos. Igual que Purl.
- `entries.bak.json`, cuarentena en `corrupt/` y un único escritor con rebote de 800 ms: Purl 6.12,
  con cuatro diferencias (1.0.6 y 1.0.7):
  - Si `entries.json` no se lee y la `.bak` sí, el ilegible va a `corrupt/` antes de restaurar la
    `.bak` en su sitio; Purl lo pisaba. Si no se puede apartar, tampoco se restaura.
  - En Android, sin `entries.json` y con `entries.tmp.json` legible (un corte entre los dos renombres
    de la escritura), se toma ese, que es la última escritura completa, y se renombra a su sitio sin
    reescribirlo.
  - El aviso `noticeCorrupt` sale mientras `corrupt/` tenga más ficheros que `quarantineSeen`: se lee
    del disco, porque puede haberlo encontrado un receiver en un proceso que muere antes de que el
    usuario abra la app. Si `quarantineSeen` es mayor que lo que había en `corrupt/` (un diario
    restaurado en otro móvil, sin su `corrupt/`), se baja antes de comparar.
  - El barrido de fotos huérfanas solo corre con el diario leído del principal y nada en `corrupt/`:
    si no, esas fotos pueden ser de los días del fichero que no se leyó. Los borrados normales los
    hace la escritura, no el barrido.
- Las fotos se escriben aparte (`.part`) y se renombran, como el diario: un disco lleno no deja media
  foto. Si no cabe, una primera elección guarda el color sin foto, rehacer la foto deja el día como
  estaba, y Hoy lo avisa (`photoNotSaved`).
- `Photos` guarda decodificadas las 12 últimas fotos dibujadas, no todas: cada una son unos 3,5 MB.

### 4.2 `widget.json`

```json
{ "date": "2026-09-22", "color": "#3A6EA5", "name": "Storm blue", "pro": false,
  "year": 2026, "days": { "2026-01-01": "#AABBCC" }, "friends": ["#112233"] }
```

- `name` ya traducido: el widget no lleva la tabla.
- `days` solo para el widget del año, y solo con Pro. `friends`, desde v1.2 (#42) y gratis: los
  colores de hoy de los amigos, en orden de hora, los mismos de la paleta del círculo. Salen de
  `JournalFile.friendsToday` (fecha y colores), que se reescribe al cargar el feed y se borra al cerrar
  sesión. Vive en `entries.json` para que cualquier otra escritura del widget lo conserve, pero no va
  en la copia: `ExportFile` solo lleva los días.
- Nunca fotos, nombres de amigos ni palabras.

### 4.3 La copia: `chroma-AAAA-MM-DD.zip`

Zip STORED con CRC32 (Purl 6.9): `entries.json` con `ExportFile(version, entries)` y `photos/`.
Importar **fusiona**: un día que solo está en la copia entra; un día que está en los dos se queda
con el del teléfono. Nunca se borra nada. `share` se importa siempre como `private`: un día no se
publica por restaurar una copia.

Una excepción, solo para la foto: un día del teléfono cuyo fichero de foto no existe toma la foto de
la copia, sin tocar color, palabra ni `share`. Es el móvil restaurado desde la copia en la nube de
Android, que lleva los días y no las fotos; sin esto, el zip no las devolvía nunca. El resumen y el
aviso final cuentan las fotos recuperadas.

Los días posteriores a mañana se descartan al leer la copia: son un reloj mal puesto donde se hizo,
o un fichero escrito a mano, y abrirían un año que no existe y callarían el aviso de ese día.

Antes de fusionar se comprueba la forma a mano: `version` numérica y no mayor que la nuestra,
`entries` no vacío, fechas válidas y colores `#RRGGBB` (un color mal escrito rompería la primera
pantalla que lo pinta). Un día sin `color` es una copia de Purl: se rechaza como "no es una copia de
Chroma", no como dañada.

Al mes del primer día, si nunca se hizo copia, Hoy lo ofrece una vez (`noticeBackup`).

---

## 5. Constantes

| Constante | Valor | Dónde |
|---|---|---|
| `DAY_CUTOFF_HOUR` | 3 | `DayClock.kt` |
| `MAX_SWATCHES` | 5 | `Extract.kt` |
| `SAMPLE_SIDE` | 64 | `Capture` (lado de la miniatura que se analiza) |
| `KMEANS_K` | 8 | `Extract.kt` |
| `KMEANS_ITERATIONS` | 12 | `Extract.kt` |
| `KMEANS_SEED` | 7 | `Extract.kt` |
| `MERGE_DELTA_E` | 10.0 | `Extract.kt` |
| `MIN_SHARE` | 0.01 | `Extract.kt` (grupos con menos del 1 % de la foto se ignoran) |
| `VIVID_CHROMA` | 20.0 | `Extract.kt` (croma mínima para contar como "el más saturado") |
| `WORD_MAX` | 24 | `Entry.kt` |
| `PHOTO_SIDE` | 1080 | `ChromaRepository.kt` |
| `UPLOAD_SIDE` | 720 | `Outbox.kt` |
| `UPLOAD_QUALITY` | 80 | `Outbox.kt` |
| `SYNC_DELTA_E` | 5.0 | `Friends.kt` |
| `WEEK_DELTA_E` | 15.0 | `Week.kt` |
| `MAX_FRIENDS` | 50 | Servidor (trigger) y cliente (mensaje) |
| `PHOTO_TTL_DAYS` | 7 | Servidor (`purge-photos`; el límite duro, 8 días de fichero, en `orphan_photos`) |
| `PHOTO_DAYS` | 7 | `Outbox.kt` (un día de más de esos días sube sin foto) |
| `MAX_PENDING_REQUESTS` | 100 | Servidor (`request_friend`, escrito en el valor): solicitudes pendientes que puede tener una persona |
| `INVITE_MISSES_PER_HOUR` | 20 | Servidor (`request_friend`, escrito en el valor): enlaces que no valen antes de `too_many` |
| `MAX_REPORTS_PER_DAY` | 20 | Servidor (trigger `report_cap`, escrito en el valor) |
| `SAVE_DEBOUNCE_MS` | 800 | `ChromaRepository.kt` |
| `BACKUP_NOTICE_AFTER_DAYS` | 30 | `ChromaRepository.kt` |

---

## 6. Algoritmos

### 6.1 Día lógico y edición

`logicalDate` de Purl: la hora local se compara con 03:00, nunca un instante menos tres horas. Solo
existe una entrada editable, la de `today()`. Un día pasado se ve pero no se cambia; se puede borrar
entero.

Las escrituras (`pick`, `setWord`, `setShare`) reciben el día que la pantalla enseña y solo escriben
si sigue siendo `today()`: con la app abierta al pasar las 03:00, tocar un color escribía en el día
siguiente, que la pantalla no enseñaba. `App` mira cada minuto si el día cambió y pasa al nuevo. Una
foto que vuelve de la cámara después de las 03:00 es del día que acabó y se rechaza como las de la
galería (`galleryNotToday`), y lo mismo una que espera su color cuando cambia el día.

En Android, si el proceso muere con la cámara abierta (o la Activity se rehace), nadie espera la
foto: `shot.jpg` se queda en `capture/` y `Capture.leftover()` se la da al siguiente Hoy si tiene
menos de 15 minutos. Cancelar la cámara sí lo borra: algunas lo dejan escrito aunque se cancele.

### 6.2 Color: sRGB, Lab y distancia

- sRGB a lineal: `c <= 0.04045 ? c / 12.92 : ((c + 0.055) / 1.055)^2.4`.
- Lineal a XYZ con la matriz D65 de sRGB; XYZ a Lab con blanco D65 (0.95047, 1.0, 1.08883) y
  `f(t) = t > (6/29)^3 ? cbrt(t) : t / (3 (6/29)^2) + 4/29`.
- La vuelta es la inversa exacta, con recorte a [0, 255].
- `deltaE` es CIE76: distancia euclídea en Lab. `chroma = sqrt(a^2 + b^2)`.
- Hex en mayúsculas, `#RRGGBB`.

### 6.3 Extracción de candidatos

Entrada: `IntArray` ARGB de como mucho 64x64. Salida: de 1 a 5 `Swatch(color, share)`.

1. Se ignoran los píxeles con alfa < 128. Cada píxel pasa a Lab.
2. K-means con k = 8. Inicio k-means++ con `Random(KMEANS_SEED)`; 12 iteraciones como mucho, o antes
   si ningún píxel cambia de grupo. Un grupo vacío se descarta.
3. Mientras el par de centros más cercano esté a menos de `MERGE_DELTA_E`, se fusionan, con la media
   ponderada por píxeles.
4. Se descartan los grupos con menos de `MIN_SHARE` de la foto.
5. Selección: si el grupo de mayor croma tiene croma >= `VIVID_CHROMA`, entra primero. Después, por
   peso descendente, entra cada grupo que esté a >= `MERGE_DELTA_E` de todos los ya elegidos, hasta
   5.
6. Se devuelven ordenados por peso descendente.

Determinista: misma entrada, misma salida, en las dos plataformas. Sin `Float` dependiente de
plataforma en las comparaciones: todo en `Double`.

### 6.4 Nombres

`Names.kt` tiene la tabla: clave, color de referencia en hex y nombre en los cinco idiomas. Es la
**única fuente** de los nombres (no se copia en `docs/textos.md`, que solo fija el criterio). El
nombre de un color es la entrada de la tabla con menor `deltaE`. La tabla cubre el círculo cromático
en claros, medios y oscuros, más neutros cálidos y fríos, para que ningún gris salga con nombre de
color vivo.

### 6.5 Tinta sobre el color

Luminancia relativa WCAG. La tinta es blanca (`#FFFFFF`) o negra (`#000000`), la de mayor contraste.
Con esas dos, el peor caso del espacio sRGB da 4,58:1, así que todo el texto de la tarjeta cumple AA.
**Todo el texto va en tinta plena**: la jerarquía la marcan el tamaño y el peso, nunca la
transparencia, que en los colores medios bajaría de 4,5 (test 7). Solo el borde de la miniatura usa
tinta al 24 %, porque no es texto.

### 6.6 Recordatorio

Purl 6.10 tal cual: Android programa el siguiente disparo y se calla si hoy ya hay entrada; iOS
programa una ventana de 60 avisos sueltos que se rehace al guardar. Desde 1.0.8, además:

- `sync` quita el aviso que siga en la bandeja si hoy ya tiene color o si se entregó antes de las
  03:00 de hoy (`dayStart`): pedía algo ya hecho, o hablaba de un día que acabó (Android `cancel`,
  iOS `removeDeliveredNotificationsWithIdentifiers` de los `reminder-*`).
- Tocar el aviso abre Hoy, se hubiera dejado la app donde fuera (extra `screen` en Android, el
  delegado de `UNUserNotificationCenter` en iOS).
- `Reminder.blocked`: el sistema no va a entregar el aviso. En Android, notificaciones de la app
  apagadas o el canal `color-daily` desactivado (se puede hacer desde el propio aviso). En iOS,
  denegado, o nunca preguntado con el recordatorio encendido (un diario restaurado en un iPhone
  nuevo trae el recordatorio y no el permiso). Se lee en cada `sync`, también al volver a primer
  plano y en cuanto se contesta la pregunta. Ajustes lo enseña bajo la fila (`reminderBlocked`), y
  Hoy también, una vez, si el sí venía de su oferta. `Reminder.unblock` lleva a los ajustes de
  notificaciones de la app o, en iOS sin preguntar, hace la pregunta. El recordatorio sigue
  encendido: es lo que el usuario pidió.
- `BootReceiver` está exportado, porque los avisos del sistema lo piden, y solo atiende a esas
  cuatro acciones.

### 6.7 Widgets

`syncWidgets` escribe `widget.json` en el App Group (iOS) o en `filesDir` (Android) y pide repintar,
como Purl 6.11. Se llama al cargar, al guardar y al volver a primer plano.

En Android nada más repinta a las 03:00 (el `updatePeriodMillis` mínimo es media hora y no cae en
la hora), así que `Reminder.sync` reserva siempre, haya recordatorio o no, una alarma inexacta al
próximo 03:00 (`nextDayStart`) cuyo `DayReceiver` sincroniza los widgets y vuelve a reservarla.
`BootReceiver` hace lo mismo tras un reinicio, una actualización o un cambio de hora o de zona. Los
widgets de Android siguen el tema del sistema con `ColorProvider(día, noche)` (desde Android 12 sin
repintar; en 8 a 11 el color se elige al dibujar y se pone al día en el siguiente repintado), y los días vacíos de
la rejilla del año son un gris translúcido que vale sobre los dos fondos.

### 6.8 Compras

Purl 6.15: RevenueCat KMP, `pro_lifetime`, derecho `pro`. Sin clave, la app funciona como gratis. Un
fallo de red no quita el Pro guardado. Paywall al chocar: exportar el póster, tocar el widget del año
bloqueado, o la fila de Ajustes.

Desde 1.0.9 la compra distingue más que bien o mal, porque cada respuesta pide algo distinto al
usuario:

- `Success` solo si el derecho queda activo: una compra que la tienda acepta sin activar `pro` es un
  error de configuración, no un éxito.
- `Pending`, pago diferido (operadora, un padre que aprueba): `buyPending` y el botón de compra se
  desactiva, porque reintentar solo diría que ya es suyo.
- `Offline`, `NetworkError`: en Android llega cuando Play ya ha cobrado y el recibo no ha llegado a
  RevenueCat, que lo reenvía solo. `buyOffline` lo dice así, con Restaurar como salida.
- `Unreachable`, `StoreProblemError`: la tienda no responde antes de cobrar, `storeUnavailable`.
- `ProductAlreadyPurchasedError` restaura y, si aparece, es un `Success`.
- `Cancelled`, nada; el resto, `Failed`.

Un `PurchasesDelegate` escucha cada `CustomerInfo` nueva: un pago diferido aprobado o un reembolso
cambian Pro sin esperar a que nadie pregunte, y el paywall abierto se cierra solo si llega Pro.
Restaurar sin llegar a la tienda dice `storeUnavailable`, no `restoreNothing`: esa respuesta asusta a
quien sí pagó.

Valoración en la tienda: una vez, con 7 días o más, al volver a la app con el color de hoy ya
elegido. Se decide en `ON_START` (sin bloqueo y sin volver de un viaje propio: cámara, galería,
compartir, ajustes del sistema) y se pide en el `ON_RESUME` siguiente, en Hoy, sin capas, sin
paywall y sin una ruta de widget pendiente: iOS descarta la petición de una escena que aún no está
activa, y un diálogo de permiso pausa sin volver. No desde el guardado: gastaba la única petición al
importar una copia o con la app parada. `reviewRequested` se marca solo cuando la tienda ha recibido
la petición (Android, al terminar `launchReviewFlow` con la Activity delante; iOS, con una escena
activa), y no hay dos peticiones en vuelo.

### 6.9 Tarjeta y póster a imagen

- Tarjeta: 1080x1350 (`share/ShareCard.kt`). Fondo del color; márgenes de 72 px; nombre a 96 px con
  su base a 168 px del borde; hex a 40 px; palabra a 56 px en cursiva; fecha con año a 40 px abajo a
  la izquierda; miniatura cuadrada de 320 px con radio del 10 % abajo a la derecha. Con `watermark`,
  "Chroma" a 32 px en la última línea y la fecha sube 56 px.
- Póster, siempre sobre papel claro (`#F3F3F3`, tinta `#111111`) sea cual sea el tema, porque es
  para imprimir:
  - Rejilla, 1080x1350: el año a 72 px arriba; 12 columnas de meses y 31 filas de días, como Mi
    año, con celdas rectangulares de radio 6 e iniciales de mes encima. Días sin color en
    `#E5E5E5`; los que no existen, papel.
  - Tira, 1080x1350: el año arriba y debajo una columna fina por día con color, en orden, sin los
    días vacíos, dentro de un rectángulo de radio 28. Con menos de 30 días (`STRIP_MIN_DAYS`), cada
    columna mide 1/30 del rectángulo y el resto queda en `#E5E5E5`, con una línea de tinta al 13 % si
    el último día se confunde con él.
  - Fondo de pantalla, 1170x2532: el año en bandas horizontales de arriba abajo, a sangre y sin
    texto, para que el reloj vaya encima.
  - Con `watermark`, "Chroma" abajo a la izquierda en la rejilla y la tira; nunca en el fondo.
- Se pinta con `Canvas` sobre un `ImageBitmap` en común y se codifica a PNG en cada plataforma, como
  la tarjeta de Purl.

### 6.10 QR

Codificador QR propio en `social/Qr.kt`: modo byte, corrección de errores M, versiones 1 a 6 (una URL
de invitación cabe en la 3), máscara elegida por penalización. Sin dependencias. Test con vectores
conocidos (test 15).

### 6.11 Sintonía

Para cada tarjeta del feed: `deltaE(miColor, suColor) < SYNC_DELTA_E`, con mi entrada del mismo día
que la suya (`inTune` en `Friends.kt`). Se calcula al pintar el feed. No se guarda.

Solo en el feed (hoy y ayer), nunca en el año de un amigo: ahí se convertiría en un historial de
sintonías, que es justo lo que no puede existir. Mi día no necesita estar compartido para verla; el
otro solo la ve si se lo compartí.

### 6.12 Color de la semana (v1.2)

`WEEK_KEYS`: 52 claves de la tabla de nombres, elegidas vivas y variadas. Semana ISO `w` (1 a 53):
`WEEK_KEYS[(w - 1) % 52]`. Hay acierto si `deltaE(colorDelDía, colorDeLaSemana) < WEEK_DELTA_E`.

- `color/Week.kt`. El orden recorre el círculo cromático y, a grandes rasgos, las estaciones (azules
  fríos en enero, verdes en primavera, cálidos en verano, tierras en otoño). Cambiar el orden cambia el
  color de todos a la vez: solo entre versiones, nunca a mitad de semana.
- La semana ISO se calcula a mano (`isoWeek`): kotlinx-datetime no la trae.
- La marca sale en Hoy, en el día abierto desde Mi año y en las tarjetas del feed. No va en la
  imagen que se comparte: fuera de la app no significa nada.
- `weekColorOn` apaga la pista de Hoy y todas las marcas.

### 6.13 Estadísticas (v1.2, Pro)

`color/Stats.kt`, función pura `yearStats(año, diario)`. Cada dato es nulo si no hay días para
decirlo con honradez:

- **Calidez** de un color: su croma proyectada sobre el tono cálido de Lab (50 grados, entre rojo y
  naranja): `C * cos(h - 50)`. Un gris da casi cero, sea claro u oscuro; un azul, negativo.
- **Mes más cálido y más frío**: media de calidez de los meses con `STATS_MIN_DAYS` (3) días o más,
  al menos dos meses así, y entre el más cálido y el más frío `STATS_ALIKE` (3) o más: en un año
  gris los dos salen por ruido, y dos meses iguales serían a la vez el cálido y el frío.
- **Color que más volvió**: la clave de nombre más repetida, con dos días como mínimo. Empates, al
  primero del año.
- **Estación más gris**: la de menor croma media entre las de 3 días o más (al menos dos, y con
  `STATS_GREY_SPREAD` (3) de croma o más entre la más gris y la más viva). Estaciones
  meteorológicas por meses (diciembre a febrero, etc.), nombradas por sus meses y no por verano o
  invierno, que dependen del hemisferio.
- **Comparación**: con `STATS_MIN_YEAR_DAYS` (20) días en los dos años, la diferencia de calidez
  media; por debajo de `STATS_ALIKE` (3) es "se pareció mucho". El año anterior se corta en el mismo
  día del año que el actual ha alcanzado: un enero contra un año entero es invierno contra el año.

Se enseña en frases (`StatsScreen`), sin números ni gráficas.

---

## 7. Captura

```kotlin
expect object Capture {
    /** Abre la cámara del sistema. Devuelve el JPEG o null si se cancela. */
    suspend fun camera(): Picked?
    /** Abre el selector de fotos del sistema, sin permiso. */
    suspend fun gallery(): Picked?
}
class Picked(val jpeg: ByteArray, val takenOn: LocalDateTime?)
```

- Android: `ActivityResultContracts.TakePicture` con un `FileProvider` en `cacheDir`, y
  `PickVisualMedia(ImageOnly)`. La fecha sale de `ExifInterface` (`DateTimeOriginal`, después
  `DateTime`).
- iOS: `UIImagePickerController` con fuente cámara, y `PHPickerViewController` sin acceso a la
  fototeca. La fecha sale de `CGImageSourceCopyPropertiesAtIndex` (`{Exif}.DateTimeOriginal`).
- Las dos: se aplica la orientación EXIF, se reduce a `PHOTO_SIDE` y se recodifica a JPEG 85 (así
  se van los metadatos también en local). La miniatura de `SAMPLE_SIDE` en ARGB sale en común de la
  foto ya decodificada (`samplePixels`, que llama a `sampleOf`): el recorte 4:5 central, lo que Hoy
  enseña mientras se elige, promediado por bloques enteros con sumas de enteros. Antes se escalaba la
  foto entera con el escalador de cada plataforma, que muestrea puntos sueltos: un color que no se
  veía en el recorte podía salir de candidato, y una línea fina salía o no según dónde cayera.
- Regla de fecha: si `takenOn` existe y su `logicalDate` no es hoy, se rechaza con el aviso
  `galleryNotToday`. Sin fecha, se acepta.
- Sin nadie que conteste (1.0.6): en Android `cameraAvailable` exige además que alguien resuelva
  `ACTION_IMAGE_CAPTURE` (de ahí el `<queries>` del manifiesto), mirado una vez por proceso. Si el
  lanzamiento falla igualmente, `launchFailed`, aviso `captureFailed` y el botón de cámara se va.
- Permiso de cámara en iOS (1.0.6): la primera vez lo pide `camera()` con
  `AVCaptureDevice.requestAccessForMediaType`, no el selector, que tras un no se queda en negro.
  Denegado da `cameraDenied` y el aviso lleva a Ajustes (`AppInfo.openSettings`); restringido
  (Tiempo de uso, móvil gestionado) cuenta como sin cámara, porque en Ajustes no hay nada que tocar.
- La Activity no se rehace al girar ni con el modo oscuro, el tamaño de letra, la negrita, la
  densidad o un teclado físico (`configChanges`): una foto que vuelve de la cámara se perdería. Solo
  el idioma la rehace. Como nada más redibuja los widgets con el tema o el idioma nuevos,
  `ChromaApp.onConfigurationChanged` los sincroniza cuando cambia cualquiera de los dos.

---

## 8. Servidor (v1.1)

Todo en `supabase/migrations/` y `supabase/functions/`. La seguridad está en RLS y en funciones
`security definer`; el cliente no tiene permisos directos sobre amistades ni bloqueos. Nada depende de
que la app se porte bien: un cliente modificado recibe lo mismo que el oficial.

### 8.1 Tablas

Las migraciones son la fuente, en este orden. Una ya aplicada no se edita nunca: un cambio es un
fichero nuevo, porque una base que aplicó las anteriores tiene que recibirlo también.

| Migración | Qué trae |
|---|---|
| `20260923120000_friends.sql` | Las tablas, el tope de 50, las funciones de amistad y bloqueo, RLS y el bucket `photos` |
| `20260923120100_purge.sql` | `pg_cron` y `pg_net`, y la purga de cada noche |
| `20260923130000_hardening.sql` | El código de invitación solo lo lee su dueño (`my_profile`), y `photo_path` solo puede ser un fichero propio |
| `20261001120000_review.sql` | La revisión antes del lanzamiento: lo que cambia en 8.2 a 8.4 |

El esquema que dejan las cuatro:

```sql
create table profiles (
  id uuid primary key references auth.users on delete cascade,
  display_name text not null check (char_length(display_name) between 1 and 30),
  invite_code text not null unique default encode(extensions.gen_random_bytes(5), 'hex'),
  created_at timestamptz not null default now()
);

create table friendships (
  a uuid not null references profiles on delete cascade,
  b uuid not null references profiles on delete cascade,
  requested_by uuid not null,
  status text not null check (status in ('pending', 'accepted')),
  created_at timestamptz not null default now(),
  primary key (a, b),
  check (a < b),                          -- una fila por pareja, pida quien pida
  check (requested_by in (a, b))
);

create table shared_entries (
  author uuid not null references profiles on delete cascade,
  day date not null,
  color text not null check (color ~ '^#[0-9A-F]{6}$'),
  name text not null check (char_length(name) between 1 and 40),   -- la clave de la tabla de nombres
  word text check (char_length(word) <= 24),
  -- Solo un fichero de su propia carpeta: <author>/<día>.jpg
  photo_path text check (
    photo_path is null
    or (photo_path like author::text || '/%'
        and photo_path ~ '^[0-9a-f-]{36}/[0-9]{4}-[0-9]{2}-[0-9]{2}\.jpg$')
  ),
  updated_at timestamptz not null default now(),   -- lo mueve el trigger touch, no el cliente
  primary key (author, day)
);

create table blocks (
  blocker uuid not null references profiles on delete cascade,
  blocked uuid not null references profiles on delete cascade,
  primary key (blocker, blocked)
);

create table reports (
  id bigint generated always as identity primary key,
  reporter uuid not null references profiles on delete cascade,
  author uuid not null,                            -- sin clave: el autor puede irse antes de que se lea
  day date not null,
  created_at timestamptz not null default now(),
  notified_at timestamptz,                         -- lo rellena report-notify cuando el correo salió
  constraint reports_once unique (reporter, author, day)
);

create table invite_attempts (                     -- los enlaces que no valen; sin permisos para nadie
  user_id uuid not null references auth.users on delete cascade,
  created_at timestamptz not null default now()
);
```

Permisos de tabla: `anon` ninguno. `authenticated` lee `profiles` solo en `id` y `display_name`,
inserta `id` y `display_name`, y actualiza `display_name`; lee `friendships` y `blocks`; lee, inserta,
actualiza y borra `shared_entries`; inserta `reporter`, `author` y `day` en `reports`. Sobre
`invite_attempts` no tiene nada.

### 8.2 Funciones

Supabase da `execute` sobre cada función nueva de `public` a `anon` y a `authenticated`, así que
cada migración dice de la suya quién la ejecuta. `anon` no ejecuta ninguna. `authenticated` ejecuta
solo estas doce: `is_friend`, `has_open_request`, `request_friend`, `accept_friend`,
`decline_friend`, `remove_friend`, `block_user`, `unblock_user`, `regenerate_code`, `my_profile`,
`my_friendships` y `my_blocks`. `orphan_photos` es de `service_role`, y nadie con sesión ejecuta el
resto (`is_blocked`, `accepted_count`, los triggers y `notify_report`). `supabase/tests` comprueba el
conjunto entero.

- `is_friend(y uuid) returns boolean`: hay amistad `accepted` entre `auth.uid()` e `y`, y ningún
  bloqueo en ningún sentido. No lleva el primer uuid: con él, cualquiera con sesión podía preguntar
  por cualquier pareja de uuids. Un `false` no dice cuál de las dos cosas falló, así que la ejecuta
  `authenticated` (las políticas la llaman con sus permisos).
- `is_blocked(y uuid)`: lo mismo para el bloqueo, simétrica (un bloqueo corta en los dos sentidos).
  Por eso **no** la ejecuta nadie con sesión: un bloqueado podría preguntar si lo está. Solo la llaman
  las funciones `security definer`.
- `has_open_request(y uuid)`: hay solicitud `pending` con `y` y ningún bloqueo. Es lo que
  `profiles_read` necesita para que aceptar sepa quién pide. La ejecuta `authenticated`.
- `request_friend(code text) returns text`: `'sent'`, `'accepted'`, `'already'`, `'self'`,
  `'blocked'`, `'not_found'`, `'limit'`, `'too_many'`.
  - `'accepted'`: el otro ya lo había pedido. También si los dos piden a la vez: el segundo `insert`
    choca con la clave, y la función relee la fila y sigue por la rama de la que ya existe.
  - `'not_found'`: un código que no existe, o cuyo dueño me bloqueó. Se contestan igual y cuentan
    igual como intento fallido: la respuesta no dice a nadie que lo bloquearon. `'blocked'` solo lo ve
    quien bloqueó.
  - `'limit'`: yo con 50 amigos, o la otra persona con 100 solicitudes pendientes recibidas. Al aceptar
    una solicitud ya hecha, cualquiera de los dos con 50 (trigger `friend_limit`).
  - `'too_many'`: 20 intentos fallidos en la última hora (`invite_attempts`). Un código son 40 bits,
    y lo que impide recorrerlos es cuántos fallos tiene una cuenta. `'self'` y `'blocked'` no cuentan.
- `accept_friend(other uuid)`, `decline_friend(other uuid)`, `remove_friend(other uuid)`: los dos
  últimos borran la fila sin avisar. Aceptar no hace nada si hay un bloqueo: una carrera entre
  invitar y bloquear puede dejar una fila pendiente, y no debe volverse amistad.
- `block_user(other uuid)`: borra la amistad o la solicitud y crea el bloqueo.
- `unblock_user(other uuid)`: borra el bloqueo, sin avisar. No devuelve la amistad (la fila se fue con
  el bloqueo): solo deja que las solicitudes entre los dos vuelvan a funcionar.
- `regenerate_code() returns text`.
- `my_profile() returns table (id, display_name, invite_code)`: la única forma de leer un código de
  invitación, y solo el propio. En `profiles` el cliente solo puede leer `id` y `display_name`: con la
  tabla entera, un amigo o alguien con una solicitud abierta podría leer tu código y repartirlo, que
  es justo lo que regenerarlo tiene que cortar.
- `my_friendships() returns table (id, display_name, status, requested_by)`: mis amistades, pendientes
  y aceptadas, con el nombre ya unido y sin las de personas bloqueadas en ningún sentido. `id` es el
  de la otra persona. Una sola llamada: pedir los nombres con `id in (...)` en la URL deja de caber
  con unos cientos de solicitudes.
- `my_blocks() returns table (id, display_name)`: las personas que yo bloqueé, con su nombre, que
  `profiles_read` no deja leer.
- `orphan_photos() returns setof text`: los nombres del bucket `photos` que hay que borrar, de más de
  1 día y sin fila en `shared_entries` con ese `photo_path`, o de más de 8 días en cualquier caso. Como
  mucho 1000 por llamada. La ejecuta solo `service_role` (la purga, 8.4).

Triggers:

| Trigger | En | Qué |
|---|---|---|
| `friend_limit` | `friendships` | Al pasar a `accepted`, si cualquiera de los dos ya tiene `MAX_FRIENDS` aceptados, lanza `friend_limit` |
| `touch` | `shared_entries` | Mueve `updated_at` en cada cambio |
| `entry_day` | `shared_entries` | Un `day` fuera de `[2026-01-01, current_date + 2]` se rechaza (`day_out_of_range`), pero solo al insertar o al cambiar el `day`: una fila anterior al trigger y fuera de rango deja que la purga le ponga `photo_path` a null. Con `day < current_date - 8`, `photo_path` se guarda a null, siempre: la foto de un día así no se conserva. El margen de una noche sobre los 7 días es porque `current_date` es UTC y la app, al oeste de UTC, sigue en su séptimo día |
| `report_cap` | `reports` | Más de 20 reportes del mismo reportero en 24 h lanza `report_limit` |
| `report_notify` | `reports` | Después de insertar, `notify_report(id)`: avisa por `pg_net` a `report-notify` (8.4) |

Un segundo reporte de la misma tarjeta por la misma persona choca con `reports_once` (`23505`), y
la app lo toma por hecho.

### 8.3 RLS

| Tabla | select | insert / update / delete |
|---|---|---|
| `profiles` | yo, mis amigos (`is_friend`) y quien tenga una solicitud abierta conmigo (`has_open_request`), solo `id` y `display_name` | solo mi fila, solo `display_name` |
| `friendships` | filas donde estoy | solo por funciones |
| `shared_entries` | `author = auth.uid() or is_friend(author)` | `author = auth.uid()`, con los límites de `entry_day` |
| `blocks` | las mías | solo por funciones |
| `reports` | nadie | insert: `reporter = auth.uid() and is_friend(author)`, uno por tarjeta y 20 al día |
| `invite_attempts` | nadie | solo por `request_friend` |

Bucket `photos`, privado, 1 MB, solo `image/jpeg`.

- **Nombre**: obligatoriamente `<uid>/<AAAA-MM-DD>.jpg`, tanto al crear como al actualizar, y con un
  día entre hace 10 días y dentro de 2. Así una cuenta tiene como mucho 13 nombres posibles: no puede
  llenar el bucket, y no existe un nombre que la purga no alcance (un día de 2099).
- **Escribir**: la primera carpeta es `auth.uid()`. **Borrar**: igual.
- **Leer**: la propia carpeta, siempre (la foto sube antes que la fila que la señala), y la de un
  amigo solo si existe una fila visible suya con ese `photo_path` (`is_friend`). Un fichero sin fila,
  como el que queda al quitar una tarjeta desde el panel, no lo lee nadie más que su dueño.
- Se sirve con **descarga autenticada con la sesión** (`downloadAuthenticated`); la única URL firmada
  es la de 24 h del correo de reporte.

### 8.4 Edge Functions y tareas

| Función | Cuándo | Qué |
|---|---|---|
| `purge-photos` | Cada noche, por `pg_cron` y `pg_net` (con dos minutos para contestar) | Primero por filas: borra del bucket las fotos con `day < current_date - 7` y pone `photo_path` a null. Después el barrido de huérfanos: pide los nombres a `orphan_photos()` y los borra por la API de Storage, en lotes de 100. Las dos fases son independientes: un error en una (guarda el primero) no impide que corra la otra, y contesta 500 al final si alguna falló. Cada fase tiene un tope de 100 rondas por noche; si un lote no borra nada, esa fase falla en vez de girar. Responde `{purged, orphans}`, y con un fallo, 500 con `{purged, orphans, error}` |
| `report-notify` | La base la llama al insertar en `reports` (trigger y `pg_net`) y cada hora para reintentar | Correo al autor de la app por Resend (`RESEND_API_KEY`, `REPORT_TO`, `REPORT_FROM`), con una URL firmada de 24 h para ver la foto. Al enviarlo rellena `reports.notified_at` |
| `delete-account` | Desde la app y desde la web | Con la clave de servicio: vacía la carpeta del bucket, borra los reportes sobre esa persona y el usuario (el resto cae en cascada) |

- **`purge-photos`**: la fecha de corte es UTC y el día de la foto es el local del autor, así que una
  foto vive entre unos 6,5 y 8,7 días según el huso. El límite duro lo pone `orphan_photos`: ninguna
  pasa de 8 días de fichero más la noche en que se barre. La app no sube foto de un día de más de 7
  días (9.2) y el trigger `entry_day` no deja guardar su ruta con `day < current_date - 8` (una noche
  de margen por el huso), así que una foto que entra es una que este corte alcanza. Si una fila no se
  puede actualizar tras borrar el fichero, esa fase se detiene y la respuesta final es 500. El barrido
  de huérfanos corre igualmente: es el único límite duro, y una fila que no se deja purgar no puede
  dejarlo sin ejecutar noche tras noche.
- **`report-notify`** no lleva JWT (`verify_jwt = false` en `supabase/config.toml`, que es lo que deja
  entrar a `pg_net`): la protege la cabecera `x-webhook-secret`, que tiene que ser igual a
  `REPORT_WEBHOOK_SECRET`, y esa variable tiene que existir: si falta, 401 siempre, también con la
  cabecera vacía. Sin `REPORT_TO`, 500, y sin `record.id` en el cuerpo, 400. Si Resend falla (502) no
  marca nada, y el reintento de cada hora lo vuelve a mandar. Si el marcado falla (500), el correo ya
  salió y el reintento puede mandarlo dos veces.
- **`delete-account`**: si una pasada no borra nada (una subcarpeta, o un fichero que la API no borra),
  contesta 500 en vez de girar hasta el límite de tiempo. Una subida tardía entre el último listado y el
  borrado del usuario queda sin dueño: la recoge el barrido de huérfanos. Los reportes sobre la persona
  (`reports.author` no tiene clave foránea) se borran antes que el usuario y, si falla, contesta 500
  sin borrarlo: con el usuario ya borrado el reintento no podría autenticarse y los reportes se
  quedarían para siempre.

Tareas de `pg_cron` (todas en UTC):

| Tarea | Cuándo | Qué |
|---|---|---|
| `purge-photos` | 03:17 | Llama a la función del mismo nombre, con la clave de servicio del Vault |
| `invite-attempts-trim` | 03:31 | Borra los intentos de `invite_attempts` de más de 1 día |
| `report-retry` | Minuto 41 de cada hora | `notify_report` para los reportes sin `notified_at` y de más de 10 minutos, 50 como mucho |

El aviso de un reporte sale de la base y no de un webhook del panel, para que un proyecto nuevo no
pueda olvidarlo. `notify_report` lee del Vault `project_url` y `report_webhook_secret`; si falta uno,
o `pg_net` falla, solo escribe un `warning` y el reporte se guarda igual: el reintento de cada hora lo
manda cuando se arregla. Quien reporta nunca pierde su reporte por un fallo del correo.

Los secretos viven en Supabase, nunca en el repositorio. En el Vault: `project_url`,
`service_role_key` (la purga) y `report_webhook_secret` (el mismo valor que `REPORT_WEBHOOK_SECRET`
de la función), a mano una vez (`store/servidor.md` 2). La app solo lleva la URL del proyecto y la
clave pública (`anon`), que ya viajan en el binario, en `social/SupabaseConfig.kt`.

---

## 9. Cliente social (v1.1)

### 9.1 Sesión

`supabase-kt` con Auth, Postgrest, Storage y Functions (`social/Social.kt`). La sesión la guarda el
gestor por defecto del SDK: preferencias privadas de la app en Android y `NSUserDefaults` en iOS,
que por eso figura en `PrivacyInfo.xcprivacy` (razón `CA92.1`). Flujo PKCE; la vuelta del flujo web
es `com.baltajmn.color://login`.

Apple y Google con `compose-auth`: nativos en su plataforma, y el otro por el flujo web (Apple en
Android, Google en iOS). Al crear la cuenta: nombre visible de 1 a 30 caracteres y casilla de 16
años o más, obligatoria. Sin proyecto (`SupabaseConfig.url` nulo), la pestaña Amigos no existe.

La sesión tiene cuatro estados y la pantalla Amigos los reparte uno a uno. `NotAuthenticated` enseña la
presentación. `Initializing` (el cliente lee la sesión guardada en otra corrutina al crearse) enseña
`working`. `RefreshFailure` (sesión guardada que no se renueva sin red) enseña `friendsOffline` sin
botón: el cliente reintenta solo y la pantalla vuelve sola al conectar. `Authenticated` abre Amigos.
`Social.loadMe` lanza si no hay una sesión viva, para que salga ese aviso y no una espera sin fin.

`hasAccount()` (sesión viva, o `RefreshFailure`) es lo que decide qué se enseña en Ajustes (la sección
Amigos, con cerrar sesión y borrar cuenta) y en Hoy (el interruptor de compartir). No depende de que
el perfil (`Social.me`) esté cargado: antes solo se cargaba al abrir Amigos con red, y tras un
arranque en frío faltaban justo los controles de privacidad. Lo que sí necesita el perfil o las
listas (el nombre, `friendsRow`, `inviteFriend`) sale desactivado hasta que cargan, y Ajustes los
pide cada vez que la sesión pasa a viva (`hasLiveSession()`, que es `Authenticated` y no `hasAccount()`):
con el arranque en frío sin red la cuenta ya está pero no está viva, y el perfil se pide cuando el
cliente renueva el token, no antes. Sin perfil (sesión iniciada y nunca llamada con nombre,
`Social.needsName`), la sección enseña solo `signOut` y `deleteAccount`: son lo único que no necesita
perfil, y Apple 5.1.1(v) pide poder borrar la cuenta desde la app en cualquier momento.

Cerrar sesión deja la cuenta donde está: al volver a entrar vuelven los amigos. Sin red, el teléfono
olvida la sesión igual (`clearSession`). Sin cuenta nada se comparte: `defaultShare` vuelve a `Private`
y `shareAsked` a `false`, de modo que un día guardado ahora no entra en la cola, y la pregunta del
valor por defecto sale otra vez con el primer amigo.

Ese olvido (`Social.forget`) también se hace cuando el cliente suelta la sesión por su cuenta: el
refresh token revocado, o el usuario borrado desde el panel. `Social.watchSession`, que arranca `App`
una vez por proceso, lo ejecuta en la transición de `Authenticated` o `RefreshFailure` a
`NotAuthenticated` (`sessionLost`), y no con el `NotAuthenticated` con el que arranca un teléfono sin
cuenta. Ese lleva `isSignOut` a false; el cliente marca con true todo lo que borra (`clearSession`),
también la sesión guardada que el servidor rechaza al arrancar, que pasa de `Initializing` directo a
`NotAuthenticated` sin un `Authenticated` antes, y por eso también cuenta. Se ejecuta también tras
`signOut` y `deleteAccount`, que ya habían olvidado: es idempotente. Sin esto, el nombre, los amigos y
el feed de la cuenta anterior se le enseñarían a la siguiente, y sus fotos en caché y la tira del widget
se quedarían en el teléfono.

### 9.2 Subida

`Outbox` (`social/Outbox.kt`). La cola es el campo `outbox` de `entries.json` y no un fichero
aparte: un cambio y su sitio en la cola se escriben juntos o no se escriben. `ChromaRepository.edit`
añade cada día cuyo contenido cambia y que está o estaba compartido (`sharedChanges`); un día
privado no entra nunca.

La cola y las marcas de "compartido" son de una cuenta: `JournalFile.outboxOwner` guarda su uid. La
primera cuenta que entra la toma, con lo que ya hubiera; otra distinta la descarta (todos los días
quedan privados y la cola vacía), porque lo que se encoló para una no es de la otra. Las filas que la
primera ya tenía en el servidor se quedan allí, en su cuenta: este teléfono no las toca. Si esa cuenta
vuelve a entrar después de otra, se descarta de nuevo (el dueño de la cola es ya la otra): los días
quedan privados en el teléfono aunque el servidor conserve sus filas viejas. Cerrar sesión conserva
cola y dueño; borrar la cuenta vacía la cola, borra el dueño y deja todos los días privados, porque
ahí sí el servidor ya no tiene copia que alcanzar.

Se vacía en orden al arrancar, después de cada guardado, al abrir Amigos y cada vez que la sesión pasa
a `Authenticated`: en un arranque en frío no hay uid hasta que el cliente lee la sesión, y con el
token caducado hasta que la renueva por red. Cada día se manda como
está ahora, no como estaba al entrar en la cola: privado o borrado quita la fila y después la foto;
compartido sube primero la foto y después la fila, para que nadie reciba nunca una ruta sin fichero.
Un fallo corta la ronda y deja el resto para la siguiente. Un día solo sale de la cola si no cambió
mientras se mandaba (`synced`).

La foto se sube solo con `share == Photo`: reducida a `UPLOAD_SIDE`, JPEG `UPLOAD_QUALITY`,
recodificada (`reencodeJpeg`, sin metadatos), con `upsert` a `<uid>/<day>.jpg`: el nombre que exige la
política del bucket (8.3). Con `Color`, la foto del servidor se borra.

Lo que el servidor no admite no se manda, o la cola se atascaría para siempre:

- Un día de más de `PHOTO_DAYS` (7) días sube solo el color, aunque esté en `Photo` (una cola que
  tarda en salir): el servidor dejaría su `photo_path` a null y el fichero quedaría huérfano. El
  servidor guarda una noche más de margen que la app para los husos (su disparador anula la foto con
  `day < current_date - 8`, y su fecha es UTC mientras el día es el local del autor). `photoFits`
  sigue en 7: la app no gasta ese margen, que es para que un teléfono al oeste de UTC no vea anulada
  una foto que, para él, aún está dentro de su semana.
- Un día fuera de `[2026-01-01, hoy + 2]` (`inServerRange`) no se manda y sale de la cola.

El interruptor de cada día (privado, solo el color, con la foto) está en Hoy bajo la palabra, solo
con cuenta; el valor para los días nuevos, en Ajustes > Amigos.

### 9.3 Feed

- `select * from shared_entries where day in (hoy, ayer) and author <> me order by updated_at desc`.
  `hoy` y `ayer` son los de quien mira.
- Amigos y solicitudes: `rpc my_friendships()`, una llamada con el nombre ya unido. Los amigos son los
  `accepted`; las solicitudes, los `pending` que no pedí yo. Una fila de alguien que ya no es amigo no
  se enseña aunque llegue.
- Quitar o bloquear a alguien lo saca primero de las listas, del feed y de la tira del widget, y solo
  después vuelve a pedir las listas: el `rpc` ya funcionó, y si ese refresco falla (sin red) no deja su
  color en el widget ni dice "sin conexión" de algo que ya está hecho.
- Fotos: `downloadAuthenticated` con la sesión (las políticas del bucket deciden, y es una llamada),
  una vez, y caché en disco en la caché del sistema, `friends/<author>-<day>-<updated_at>.jpg`. El
  `updated_at` en el nombre hace que una foto cambiada nunca salga de la caché vieja. Tras cada carga se
  borra de la caché todo lo que el feed ya no enseña, y al cerrar sesión, todo.
- La caché es solo una comodidad: se escribe a un `.part` y se renombra, y nada que falle (disco lleno,
  un fichero que la limpieza borra mientras se lee, una copia que no decodifica, que se vuelve a bajar
  y se sobrescribe) cuesta la foto ni cierra la app. Una tarjeta cuya foto no se pudo bajar la vuelve a
  pedir en cada carga del feed (`Friends.refreshes`, un contador interno que no se enseña), así que tirar
  hacia abajo también la recupera.
- El año de un amigo se pide de uno en uno: `Friends.year(id, año)` con rango y orden por día, y
  `Friends.firstYear(id)` para el primer año con algo compartido; las flechas van de ese a este. El
  servidor corta en 1000 filas empezando por las más antiguas, y pedirlo todo perdería justo el año que
  se ve. Un día que no se puede leer (un cliente modificado puede guardar uno que no parsea) se descarta
  al recibirlo.
- La tarjeta se decodifica al entrar en pantalla (`LazyColumn`): 50 amigos por dos días no caben
  decodificados a la vez.
- Se refresca al abrir Amigos y al tirar hacia abajo. Sin sondeo en segundo plano.

### 9.4 Invitación

Enlace `https://color.baltajmn.dev/i/<code>`. Android: `intent-filter` con `autoVerify` y
`.well-known/assetlinks.json` de `chroma-privacy`. iOS: `applinks:color.baltajmn.dev` y su
`.well-known/apple-app-site-association`. La web (su `404.html`, que GitHub Pages sirve para
cualquier ruta) lee el código y enseña los botones de las tiendas.

- El código son 10 caracteres hexadecimales; `inviteCodeOf` solo acepta ese host y esa forma.
- Un enlace abierto sin sesión, o antes de elegir nombre, se guarda en memoria (`Friends.pendingCode`)
  y se manda al llegar a Amigos con cuenta. No se guarda en disco: tras instalar, el enlace se vuelve a
  abrir (SPEC 5). Se borra en cuanto el servidor contesta, no cuando acaba el refresco de listas.
- `request`, `accept` y `decline` devuelven lo que contestó el `rpc`. El refresco de las listas que
  viene detrás es solo ponerlas al día: si falla (la red cae justo después), no se convierte en
  "sin conexión" ni pierde la respuesta; las listas se corrigen en la siguiente carga.
- `request_friend` contesta `not_found` a quien está bloqueado, igual que ante un código que no existe, y
  la app pinta `blocked` igual ("este enlace ya no vale"): nadie averigua por un enlace que le han
  bloqueado. `too_many` (20 enlaces que no valen en una hora) dice `inviteTooMany`. `limit` dice
  `inviteLimit`: puede ser el tope de amigos de uno o el de solicitudes en espera del otro, y no se sabe
  cuál.
- Solo se listan las solicitudes recibidas. La enviada e ignorada se queda pendiente sin avisar. Cada
  solicitud se puede aceptar, ignorar o bloquear, y bloquear abre directamente la confirmación
  (`blockText`): ignorar no impide que quien tenga el enlace mande otra.
- Aceptar con alguno de los dos en 50 falla en el disparador (`friend_limit`) y la app lo dice con
  `friendLimit`, sin sugerir a quién quitar.
- Bloqueados: Ajustes > Amigos > `blockedRow` abre un diálogo con los nombres de `my_blocks()` y
  `unblock` en cada uno, con confirmación (`unblockText`). Desbloquear no avisa y no devuelve la
  amistad.
- Reportar inserta en `reports`. Un `23505` (la misma tarjeta otra vez) es éxito, y la tarjeta queda
  oculta igual. El tope de 20 al día (`report_limit`) sí es un fallo, y la tarjeta vuelve.
- Los marcadores de `assetlinks.json`, `apple-app-site-association` y `404.html` se rellenan como
  dice `store/servidor.md` 5.

---

## 10. Tests

Comunes (`commonTest`) salvo que se diga.

1. Día lógico: 02:59 es ayer, 03:00 es hoy, con cambio de horario.
2. Lab: ida y vuelta de 1.000 colores al azar con semilla fija, error <= 1 por canal.
3. `deltaE` y `chroma` con valores de referencia.
4. Extracción determinista: dos llamadas, mismo resultado.
5. Extracción de una imagen sintética de tres bandas: devuelve esos tres colores (deltaE < 3).
6. Extracción de una imagen gris con un 3 % de rojo: el rojo entra.
7. Tinta: contraste >= 4,5 en una barrida de 32^3 colores.
8. Nombres: cada color de la tabla devuelve su clave; un gris neutro devuelve una clave de neutro.
9. Palabra: tope de 24 contando un emoji como uno, sin partir parejas suplentes.
10. Almacén: escritura atómica, `.bak`, cuarentena (Purl, `androidHostTest`).
11. Fusión: el día del teléfono gana; `share` llega como privado.
12. Zip: ida y vuelta con fotos (Purl).
13. Recordatorio: el plan de iOS se calla si hoy hay entrada (Purl).
14. Sintonía: justo por debajo y justo por encima del umbral.
15. QR: la matriz de un texto conocido coincide con la de referencia.
16. Color de la semana: semana 1 y semana 53.
17. Servidor (`supabase/tests/`, pgTAP): sin amistad no se lee; un bloqueo corta y se deshace; tope de 50
    y de 100 solicitudes; nadie lee el código de invitación de otro; una tarjeta no puede apuntar a la
    foto de otra persona. Y lo que cierra la revisión: fotos (lee un amigo solo con fila, no un
    extraño ni un bloqueado; escribe solo con nombre `<uid>/<día>.jpg` y un día dentro de la ventana),
    días de una tarjeta (hasta hoy + 2, el margen de una noche, mover el `day` fuera de rango, la fila
    antigua a la que la purga le quita la ruta, y que una actualización que no toca el `day` también
    quita la ruta de un día viejo), reportes (solo de un amigo y a nombre propio, duplicado,
    `notified_at`, tope de 20 por persona en un día y no en una hora, uno que se guarda aunque el Vault
    esté vacío y no deja nada en la cola, la llamada que `notify_report` deja en la cola de `pg_net`
    con su URL, la cabecera `x-webhook-secret` y el reporte, y el reintento horario, que solo repite el
    de más de 10 minutos y sin marcar), freno de códigos, cruce de solicitudes, `self`, el tope de 50
    amigos de quien pide (también al pedir de vuelta una solicitud ya hecha), que quien pidió no
    acepta su propia solicitud, que `my_friendships` y `my_blocks` son solo lo mío y que un bloqueado
    no levanta el bloqueo, los guardas de un bloqueo con la fila de amistad todavía puesta (no lee
    días ni fotos, no sale en la lista, no deja leer el perfil ni aceptar), el barrido de huérfanos
    (también el fichero viejo al que sí apunta una tarjeta), que `anon` no ejecuta ni toca nada, el
    conjunto exacto de funciones que ejecuta `authenticated`, y que el trigger y las tres tareas de
    `pg_cron` existen.
    `.github/workflows/db.yml` lo ejecuta (`supabase start` y `supabase test db`) en cada cambio de
    `supabase/`.
18. Estadísticas: calidez ordenada, mes más cálido y más frío, color repetido, estación más gris, pocos
    días no dicen nada, y la comparación con el año anterior en los tres sentidos.
19. Amigos, lógica del cliente (`FriendsLogicTest`): amigos por nombre sin distinguir mayúsculas y solo
    las solicitudes que me hicieron a mí; cada respuesta de `request_friend` tiene su resultado; un día
    que no parsea se descarta; el servidor admite de 2026-01-01 a hoy + 2; una foto de más de 7 días
    sube solo con el color; otra cuenta empieza sin nada compartido ni en cola; el dueño de la cola
    sobrevive al fichero y los ficheros viejos no lo tienen; la sesión que el cliente suelta
    (`sessionLost`) se distingue del `NotAuthenticated` con el que arranca un teléfono sin cuenta.

`ExtractPreview` (`androidHostTest`) no es un test: con `CHROMA_PHOTOS=<carpeta>` escribe
`shared/build/extract-preview.png`, una hoja con cada foto y sus candidatos, para juzgar la extracción a ojo.
Sin la variable no hace nada.

---

## 11. Qué gobierna cada issue

| Issue | Secciones |
|---|---|
| #5 Andamiaje | 1, 2, 3 |
| #6 Modelo | 4.1, 5, 6.1; test 1 y 9 |
| #7 Almacén | 4.1, 4.2; test 10 |
| #8 Extracción | 6.2, 6.3; tests 2 a 6 |
| #9 Nombres | 6.4; test 8 |
| #10 Captura | 7 |
| #11 Hoy | `pantallas.md` Hoy |
| #12 Tarjeta | 6.5, 6.9; test 7 |
| #13 Mi año | `pantallas.md` Mi año |
| #14 Tema | `pantallas.md` 1 |
| #15 Textos | `textos.md` |
| #16 Ajustes | `pantallas.md` Ajustes |
| #17 Recordatorio | 6.6; test 13 |
| #18 Copia | 4.3; tests 11 y 12 |
| #19 Compartir | 6.9 |
| #20 Póster | 6.9 |
| #21, #22 Widgets | 4.2, 6.7 |
| #23 Compras | 6.8 |
| #28 a #30 Servidor | 8; test 17 |
| #31 Subida | 9.2 |
| #32 Invitación | 6.10, 9.4; test 15 |
| #33 a #35 Feed y mosaicos | 9.3 |
| #36, #37 Seguridad y cuenta | 8.2, 8.4 |
| Revisión de Amigos (v1.1) | 8, 9.1 a 9.4; tests 17 y 19; `pantallas.md` 7 y 8 |
| #39 Sintonía | 6.11; test 14 |
| #40 Color de la semana | 6.12; test 16 |
| #41 Estadísticas | 6.13; test 18 |
