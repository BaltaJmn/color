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
| Nombre de ficha | `Chroma: color del día` y su traducción (`store/listings/`) |
| `applicationId` y `namespace` de `androidApp` | `com.baltajmn.color` |
| `namespace` de `shared` | `com.baltajmn.color.shared` |
| Paquete Kotlin | `com.baltajmn.color` |
| Bundle id de la app iOS | `com.baltajmn.color` (`APP_BUNDLE_ID` en `Config.xcconfig`) |
| Bundle id del widget iOS | `com.baltajmn.color.widget` |
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

Las de Purl sin tocar (`../line/gradle/libs.versions.toml`): AGP 9.0.1, SDK 36, minSdk 24, Kotlin
2.4.10, Compose Multiplatform 1.11.1, material3 1.11.0-alpha07, kotlinx-datetime 0.8.0,
kotlinx-serialization 1.11.0, Glance 1.1.1, purchases-kmp 3.2.1, biometric 1.1.0.

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
`web/` (política, invitación y borrado de cuenta), `store/`, `tools/`, `.github/workflows/`.

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
  los reportes no se pueden leer desde la app).
- `JournalJson`: `ignoreUnknownKeys`, sin valores por defecto ni nulos explícitos. Igual que Purl.
- `entries.bak.json`, cuarentena en `corrupt/` y un único escritor con rebote de 800 ms: Purl 6.12.

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
| `PHOTO_TTL_DAYS` | 7 | Servidor (`purge-photos`) |
| `SAVE_DEBOUNCE_MS` | 800 | `ChromaRepository.kt` |
| `BACKUP_NOTICE_AFTER_DAYS` | 30 | `ChromaRepository.kt` |

---

## 6. Algoritmos

### 6.1 Día lógico y edición

`logicalDate` de Purl: la hora local se compara con 03:00, nunca un instante menos tres horas. Solo
existe una entrada editable, la de `today()`. Un día pasado se ve pero no se cambia; se puede borrar
entero.

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
programa una ventana de 60 avisos sueltos que se rehace al guardar.

### 6.7 Widgets

`syncWidgets` escribe `widget.json` en el App Group (iOS) o en `filesDir` (Android) y pide repintar,
como Purl 6.11. Se llama al cargar, al guardar y al volver a primer plano.

### 6.8 Compras

Purl 6.15: RevenueCat KMP, `pro_lifetime`, derecho `pro`. Sin clave, la app funciona como gratis. Un
fallo de red no quita el Pro guardado. Paywall al chocar: exportar el póster, tocar el widget del año
bloqueado, o la fila de Ajustes.

### 6.9 Tarjeta y póster a imagen

- Tarjeta: 1080x1350 (`share/ShareCard.kt`). Fondo del color; márgenes de 72 px; nombre a 96 px con
  su base a 168 px del borde; hex a 40 px; palabra a 56 px en cursiva; fecha con año a 40 px abajo a
  la izquierda; miniatura cuadrada de 320 px con radio del 10 % abajo a la derecha. Con `watermark`,
  "Chroma" a 32 px en la última línea y la fecha sube 56 px.
- Póster, siempre sobre papel claro (`#F6F4F1`, tinta `#1C1B1A`) sea cual sea el tema, porque es
  para imprimir:
  - Rejilla, 1080x1350: el año a 72 px arriba; 12 columnas de meses y 31 filas de días, como Mi
    año, con celdas rectangulares de radio 6 e iniciales de mes encima. Días sin color en
    `#ECE9E4`; los que no existen, papel.
  - Tira, 1080x1350: el año arriba y debajo una columna fina por día con color, en orden, sin los
    días vacíos, dentro de un rectángulo de radio 28.
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
  y al menos dos meses así.
- **Color que más volvió**: la clave de nombre más repetida, con dos días como mínimo. Empates, al
  primero del año.
- **Estación más gris**: la de menor croma media entre las de 3 días o más (al menos dos). Estaciones
  meteorológicas por meses (diciembre a febrero, etc.), nombradas por sus meses y no por verano o
  invierno, que dependen del hemisferio.
- **Comparación**: con `STATS_MIN_YEAR_DAYS` (20) días en los dos años, la diferencia de calidez
  media; por debajo de `STATS_ALIKE` (3) es "se pareció mucho".

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
  se van los metadatos también en local). La miniatura de `SAMPLE_SIDE` en ARGB sale en común del
  JPEG ya decodificado (`samplePixels`), así las dos plataformas analizan los mismos píxeles.
- Regla de fecha: si `takenOn` existe y su `logicalDate` no es hoy, se rechaza con el aviso
  `galleryNotToday`. Sin fecha, se acepta.

---

## 8. Servidor (v1.1)

Todo en `supabase/migrations/` y `supabase/functions/`. La seguridad está en RLS y en funciones
`security definer`; el cliente no tiene permisos directos sobre amistades ni bloqueos.

### 8.1 Tablas

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
  check (a < b)
);

create table shared_entries (
  author uuid not null references profiles on delete cascade,
  day date not null,
  color text not null check (color ~ '^#[0-9A-F]{6}$'),
  name text not null,
  word text check (char_length(word) <= 24),
  -- Solo un fichero de su propia carpeta: <author>/<día>.jpg (20260923130000_hardening.sql).
  photo_path text check (photo_path is null or photo_path like author::text || '/%'),
  updated_at timestamptz not null default now(),
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
  author uuid not null,
  day date not null,
  created_at timestamptz not null default now()
);
```

### 8.2 Funciones

- `is_friend(x uuid, y uuid) returns boolean`: hay amistad `accepted` y ningún bloqueo en ningún
  sentido.
- `request_friend(code text) returns text`: `'sent'`, `'accepted'` (si el otro ya te lo había
  pedido), `'already'`, `'self'`, `'blocked'`, `'not_found'`, `'limit'`.
- `accept_friend(other uuid)`, `decline_friend(other uuid)`, `remove_friend(other uuid)`: los dos
  últimos borran la fila sin avisar.
- `block_user(other uuid)`: borra la amistad o la solicitud y crea el bloqueo.
- `regenerate_code() returns text`.
- `my_profile() returns table (id, display_name, invite_code)`: la única forma de leer un código de
  invitación, y solo el propio. En `profiles` el cliente solo puede leer `id` y `display_name`: con la
  tabla entera, un amigo o alguien con una solicitud abierta podría leer tu código y repartirlo, que
  es justo lo que regenerarlo tiene que cortar.
- Trigger en `friendships`: al pasar a `accepted`, si cualquiera de los dos ya tiene
  `MAX_FRIENDS` aceptados, lanza `friend_limit`.

### 8.3 RLS

| Tabla | select | insert / update / delete |
|---|---|---|
| `profiles` | yo, mis amigos y quien tenga una solicitud conmigo | solo mi fila, solo `display_name` |
| `friendships` | filas donde estoy | solo por funciones |
| `shared_entries` | `author = auth.uid() or is_friend(auth.uid(), author)` | `author = auth.uid()` |
| `blocks` | las mías | solo por funciones |
| `reports` | nadie | `reporter = auth.uid() and is_friend(auth.uid(), author)` |

Bucket `photos`, privado. Ruta `<author>/<day>.jpg`. Escribir y borrar: la primera carpeta es
`auth.uid()`. Leer: la primera carpeta es mía o de un amigo (`is_friend`). Se sirve con URL firmadas
de 1 hora.

### 8.4 Edge Functions

| Función | Cuándo | Qué |
|---|---|---|
| `purge-photos` | Diaria, por `pg_cron` y `pg_net` | Borra del bucket las fotos con `day < current_date - 7` y pone `photo_path` a null |
| `report-notify` | Webhook de base de datos al insertar en `reports` | Correo al autor de la app por Resend (`RESEND_API_KEY`, `REPORT_TO`) |
| `delete-account` | Desde la app y desde la web | Con la clave de servicio: borra la carpeta del bucket y el usuario (el resto cae en cascada) |

Los secretos viven en Supabase, nunca en el repositorio. La app solo lleva la URL del proyecto y la
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

### 9.2 Subida

`Outbox` (`social/Outbox.kt`). La cola es el campo `outbox` de `entries.json` y no un fichero
aparte: un cambio y su sitio en la cola se escriben juntos o no se escriben. `ChromaRepository.edit`
añade cada día cuyo contenido cambia y que está o estaba compartido (`sharedChanges`); un día
privado no entra nunca.

Se vacía en orden al arrancar, después de cada guardado y al abrir Amigos. Cada día se manda como
está ahora, no como estaba al entrar en la cola: privado o borrado quita la fila y después la foto;
compartido sube primero la foto y después la fila, para que nadie reciba nunca una ruta sin fichero.
Un fallo corta la ronda y deja el resto para la siguiente. Un día solo sale de la cola si no cambió
mientras se mandaba (`synced`).

La foto se sube solo con `share == Photo`: reducida a `UPLOAD_SIDE`, JPEG `UPLOAD_QUALITY`,
recodificada (`reencodeJpeg`, sin metadatos), con `upsert` a `<uid>/<day>.jpg`. Con `Color`, la
foto del servidor se borra.

El interruptor de cada día (privado, solo el color, con la foto) está en Hoy bajo la palabra, solo
con cuenta; el valor para los días nuevos, en Ajustes > Amigos.

### 9.3 Feed

- `select * from shared_entries where day in (hoy, ayer) and author <> me order by updated_at desc`.
  `hoy` y `ayer` son los de quien mira.
- Nombres: los de la lista de amigos, que se carga a la vez. Una fila de alguien que ya no es amigo
  no se enseña aunque llegue.
- Fotos: `downloadAuthenticated` con la sesión (las políticas del bucket deciden, igual que con una
  URL firmada, y es una llamada en vez de dos), una vez, y caché en disco en la caché del sistema,
  `friends/<author>-<day>-<updated_at>.jpg`. El `updated_at` en el nombre hace que una foto cambiada
  nunca salga de la caché vieja. Tras cada carga se borra de la caché todo lo que el feed ya no
  enseña, y al cerrar sesión, todo.
- La tarjeta se decodifica al entrar en pantalla (`LazyColumn`): 50 amigos por dos días no caben
  decodificados a la vez.
- Se refresca al abrir Amigos y al tirar hacia abajo. Sin sondeo en segundo plano.

### 9.4 Invitación

Enlace `https://color.baltajmn.dev/i/<code>`. Android: `intent-filter` con `autoVerify` y
`web/.well-known/assetlinks.json`. iOS: `applinks:color.baltajmn.dev` y
`web/.well-known/apple-app-site-association`. La web (`web/404.html`, que GitHub Pages sirve para
cualquier ruta) lee el código y enseña los botones de las tiendas.

- El código son 10 caracteres hexadecimales; `inviteCodeOf` solo acepta ese host y esa forma.
- Un enlace abierto sin sesión, o antes de elegir nombre, se guarda en memoria (`Friends.pendingCode`)
  y se manda al llegar a Amigos con cuenta. No se guarda en disco: tras instalar, el enlace se vuelve a
  abrir (SPEC 5).
- `request_friend` contesta `blocked` igual que `not_found` en la app ("este enlace ya no vale"):
  nadie averigua por un enlace que le han bloqueado.
- Solo se listan las solicitudes recibidas. La enviada e ignorada se queda pendiente sin avisar.
- Aceptar con alguno de los dos en 50 falla en el disparador (`friend_limit`) y la app lo dice con
  `friendLimit`, sin sugerir a quién quitar.
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
17. Servidor (`supabase/tests/`, pgTAP): sin amistad no se lee; un bloqueo corta; tope de 50; nadie
    lee el código de invitación de otro; una tarjeta no puede apuntar a la foto de otra persona.
18. Estadísticas: calidez ordenada, mes más cálido y más frío, color repetido, estación más gris, pocos
    días no dicen nada, y la comparación con el año anterior en los tres sentidos.

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
| #39 Sintonía | 6.11; test 14 |
| #40 Color de la semana | 6.12; test 16 |
| #41 Estadísticas | 6.13; test 18 |
