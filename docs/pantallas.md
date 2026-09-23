# Pantallas de Chroma

La interfaz pantalla a pantalla. Medidas en dp (pt en iOS). Los textos se nombran por su clave de
`Strings.kt`; `docs/textos.md` fija el tono.

---

## 1. Tokens

### Color

La interfaz es gris sin tinte, como una cabina de ver color: un gris cálido o frío cambia cómo se ve
el color que tiene encima, y este es el único producto de la app. El único color vivo lo pone el
usuario. La excepción es `error`, reservado a lo que no se deshace.

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| `background` | `#F3F3F3` | `#121212` | Fondo de pantalla |
| `surface` | `#FFFFFF` | `#1C1C1C` | Hojas, diálogos, filas, avisos |
| `surfaceVariant` | `#E5E5E5` | `#262626` | Días vacíos de la rejilla, campos |
| `onBackground` | `#111111` | `#F2F2F2` | Texto principal |
| `onMuted` | `#595959` | `#A8A8A8` | Texto secundario (contraste 6,3:1 en claro, 7,9:1 en oscuro) |
| `outline` | `#D0D0D0` | `#333333` | Separadores, bordes de los círculos de color |
| `accent` | `#111111` | `#F2F2F2` | Botón principal: relleno del color del texto, texto del color de fondo |
| `error` | `#B3261E` | `#F2B8B5` | Avisos de error y el botón que confirma algo que no se deshace |

Sobre la tarjeta, la tinta sale de `inkFor(color)` (`tecnico.md` 6.5), nunca del tema, y siempre
plena: la jerarquía es de tamaño y peso, no de transparencia.

### Tipografía

La del sistema (Roboto, SF Pro). Sin fuente propia: el color es el protagonista.

| Estilo | Tamaño / peso | Uso |
|---|---|---|
| `display` | 34 / Medium | Nombre del color en la tarjeta de pantalla completa, la pregunta de Hoy vacío |
| `title` | 22 / Medium | Títulos de pantalla |
| `body` | 17 / Regular | Texto normal, el de los botones (el cuerpo de iOS; 16 se queda corto para leer sin gafas) |
| `label` | 14 / Medium | Hex, fechas, subtítulos |
| `caption` | 13 / Regular | Notas pequeñas; nada baja de 13 |

Todo escala con el tamaño de letra del sistema. Por eso ninguna fila con texto tiene alto fijo, solo
mínimo: con letra grande, la fila crece en vez de cortar. La excepción son las etiquetas dibujadas
dentro de la rejilla del año, que no escalan porque la rejilla tampoco.

### Forma y espacio

Rejilla de 4. Márgenes laterales 20. Radio de tarjeta 28, de hoja 28 arriba, de botón 24 (píldora),
de miniatura 14. Tocables de 48 como mínimo, sin excepción de estilo: el botón principal mide 52, el
de borde, el de texto y cada opción de un segmentado, 48. La única excepción es la celda de la
rejilla del año (unos 22), porque el año entero tiene que caber de un vistazo; cada día se puede
abrir igual con el lector de pantalla.

### Reglas de uso

- **Un botón relleno por pantalla**, como mucho: la acción que la pantalla existe para hacer. Si hay
  dos opciones iguales (Apple y Google), las dos van con borde.
- **Un aviso a la vez.** Si coinciden varios, se ve el más urgente: fallo al guardar, fichero
  ilegible, aviso pasajero, y solo después las ofertas (recordatorio, copia).
- **Lo que no se deshace se marca en rojo** (`error`): el botón que confirma borrar un día, borrar la
  cuenta, eliminar o bloquear a un amigo y cambiar el enlace de invitación. Y la entrada del menú que
  lleva a borrar. Nada más usa rojo.
- **Lo que abre el paywall lo dice antes**: el botón lleva `proTag` detrás de su texto. Un botón que
  dice Compartir y abre una compra parece un fallo.
- **Siempre hay salida.** Cada paso tiene su Cancelar o su cerrar; ninguno obliga a elegir.
- Una fila con interruptor se cambia tocando cualquier parte de la fila, como en los ajustes del
  sistema.
- Lo que solo es forma lleva descripción para el lector de pantalla (el QR, la tira de amigos, el
  widget del año) o se calla si es de adorno (la tira de ejemplo).

---

## 2. Navegación

Barra inferior con tres destinos: **Hoy**, **Mi año** y **Amigos** (Amigos aparece en v1.1). Ajustes
es un icono arriba a la derecha de Hoy.

El icono de Hoy es un punto. Con el color del día elegido, el punto es ese color (14, con borde
`outline` de 1 para que un blanco no desaparezca): es el único sitio donde el marco de la app toma
color, y dice de un vistazo, desde cualquier pestaña, si hoy ya tiene el suyo. Atrás, en Android, cierra la capa abierta y después vuelve a
Hoy.

Capas sobre cualquier pantalla: el día abierto (hoja), la foto a pantalla completa, compartir, el
paywall y el bloqueo.

---

## 3. Hoy

### Sin entrada

- Arriba, la fecha larga (`longDate`) en `title` y el icono de Ajustes.
- A 64 del encabezado, `todayPrompt` ("¿De qué color es hoy?") en `display`, centrado. Sin
  ilustración: el círculo con cámara que hubo aquí era un segundo botón sin texto que hacía lo mismo
  que el de debajo.
- Botón principal a lo ancho: `takePhoto`. Debajo, botón de texto: `fromGallery`.
- Primera sesión: bajo el prompt, `firstHelp` en `onMuted`.
- Con el color de la semana encendido (v1.2): un punto de 12 con ese color y `weekHint(nombre)` en
  `caption`. Una pista, no una tarea. Nada más.

### Eligiendo color

Tras la foto, en la misma pantalla:

- La foto ocupa el ancho con radio 28 y proporción 4:5.
- Debajo, los candidatos: círculos de 56 con borde `outline` de 1, separados 12, centrados. El
  elegido lleva un anillo de 3 en `onBackground` a 4 de distancia.
- Tocar un candidato lo elige y guarda al momento. No hay botón de confirmar.
- Debajo, siempre, `cancel`: vuelve a lo que había antes de la foto, también con la primera del día.
  Nada se guarda hasta tocar un color.
- Mientras se analiza la foto, los círculos se pintan en `surfaceVariant` (menos de 100 ms, casi
  nunca se ve).
- Si la foto de galería no es de hoy: aviso `galleryNotToday` y se vuelve al estado sin entrada.

### Con entrada

- La tarjeta (sección 6) ocupa el ancho, proporción 4:5.
- Debajo, la fila de candidatos, más pequeña (círculos de 40), para cambiar de color durante el día.
- Debajo, `addWord` como botón de texto; tocarlo abre un campo de una línea con tope visible (`n/24`).
- En v1.1, el control de compartir (sección 8.5).
- Menú de la tarjeta (tres puntos): `retakePhoto`, `share`, `deleteDay` (este en `error`).

---

## 4. Mi año

- Título: el año, con flechas a los lados si hay más de uno con entradas.
- Selector de vista segmentado: `viewGrid` y `viewStrip`. Solo con entradas: en un año vacío no hay
  nada que ver de otra forma.
- Justo debajo, `poster` y `stats` (v1.2) lado a lado. Van antes de la rejilla, que mide unos 800 y
  los dejaba fuera de la pantalla. Sin Pro, `stats` lleva `proTag` y abre el paywall.
- **Rejilla**: una columna por mes y una fila por día (12 x 31), como Purl: en un móvil da celdas de
  unos 24, que se tocan bien; girada saldrían de 10. Celdas cuadradas con 3 de separación y radio 3;
  inicial del mes arriba y los días 1, 10, 20 y 30 a la izquierda, en `caption`. Días sin color en
  `surfaceVariant` (los futuros, más claros); días que no existen (31 de febrero), vacíos. Hoy lleva
  un borde de 1,5 en `onBackground`.
- **Tira**: el año como columnas de 1 día, sin separación, a toda la altura disponible (proporción
  4:5). Los días vacíos no se pintan: la tira se comprime.
- Tocar un día con entrada abre el día (sección 5). El lector de pantalla recorre los días en orden
  de fecha.
- `poster` abre la misma capa que compartir (sección 5) con el póster del año y un
  segmentado de tres: `posterGrid`, `posterStrip`, `posterWallpaper`. Mirarlo es gratis; sin Pro,
  compartir y guardar llevan `proTag` y abren el paywall.
- Año sin entradas: `yearEmpty`.
- **En palabras** (`statsTitle`): pantalla completa con frases cortas, una por línea en `body`
  (`statsWarmest`, `statsColdest`, `statsRepeated` con un punto de 14 de ese color, `statsGreyest`,
  `statsWarmer`/`statsCooler`/`statsAlike`). Sin gráficas ni números más allá del año. Con pocos días,
  solo `statsEmpty`.

---

## 5. El día abierto

Hoja modal con la tarjeta a lo ancho. Acciones: `share` y `deleteDay` (con confirmación). Un día
pasado no se edita.

Tocar la miniatura abre la foto a pantalla completa, sobre negro, con cerrar arriba a la izquierda
sobre un círculo negro al 40 %: sin él, una foto de cielo o de nieve se comía la única salida.

### Compartir

Capa a pantalla completa desde el menú de Hoy o desde el día abierto. Arriba, cerrar. En medio, la
tarjeta de 1080x1350 (`tecnico.md` 6.9) a 320 de ancho como mucho, con radio 14 y borde `outline`.
Debajo, si el día tiene foto, una fila con interruptor, `includePhoto` (empieza encendido): apagarlo
quita la miniatura de la imagen, no del día. No usa las palabras de compartir con amigos
(`shareColorOnly`, `shareWithPhoto`) porque son dos ajustes distintos y con las mismas palabras
parecían uno. Después, `share` (hoja del
sistema) y `saveToPhotos` (en Android solo desde la 10, que no pide permiso). La marca "Chroma" la
decide el ajuste `watermarkRow`, no esta pantalla.

---

## 6. La tarjeta

Componente `ChromaCard(entry, date, author?, compact)`. Proporción 4:5, radio 28.

| Elemento | Posición | Estilo |
|---|---|---|
| Fondo | Todo | El color |
| Nombre del color | Arriba izquierda, margen 24 | `display` (`title` si `compact`), tinta |
| Hex | Bajo el nombre | `label` en peso normal, tinta |
| Palabra | Bajo el hex, a 12 | `body` en cursiva, tinta |
| Autor (feed) | Abajo izquierda, sobre la fecha | `body` Medium, tinta |
| Fecha | Abajo izquierda, margen 24 | `label` en peso normal, tinta |
| Miniatura | Abajo derecha, margen 20, lado 30 % del ancho | Radio 14, borde de 2 en tinta al 24 % |
| Marca de sintonía (v1.1) | Arriba derecha | Dos aros solapados de 10 (16 de ancho, trazo 1,5), tinta; se lee `inTune` |
| Marca de la semana (v1.2) | Arriba derecha, a la izquierda de la sintonía | Un rombo de 10 relleno, tinta; se lee `weekColorRow` |

Sin foto (día compartido solo con color, o foto caducada en el servidor), no hay miniatura.

---

## 7. Ajustes

Lista de secciones, como Purl:

1. **Recordatorio**: interruptor y hora (`reminderRow`).
2. **Amigos** (v1.1): cuenta y nombre visible, `defaultShareRow`, `friendsRow`, `inviteFriend` (el
   mismo nombre que la pantalla que abre), `signOut`,
   `deleteAccount` (confirmación `deleteAccountText`; mientras borra, `working`; sin conexión, el aviso
   de siempre y nada cambia).
3. **Privacidad**: bloqueo (v1.2), política, y `termsRow` si hay servidor.
   - `lockRow` con `lockSubtitle`, o `lockUnavailable` y desactivado si el teléfono no tiene bloqueo
     de pantalla. Apagado por defecto. Encenderlo pide antes la cara, la huella o el código: quien
     tenga el móvil en la mano no puede dejar fuera a su dueño.
   - Con el bloqueo puesto: al abrir, y al volver tras `RELOCK_AFTER` (60 s) fuera, una capa con
     "Chroma" en `display` y `unlock`, por encima de todo, diálogos incluidos. Atrás no hace nada. El
     diálogo del sistema sale solo. La multitarea queda tapada (Android 13+ sin captura de recientes;
     iOS con una capa del color del papel pintada desde `iOSApp.swift`).
   - Los widgets siguen enseñando colores: están en la pantalla de inicio porque el usuario los puso,
     y nunca llevan fotos ni palabras.
4. **Tarjeta**: `watermarkRow`, `weekColorRow` (v1.2, interruptor con el nombre del color de esta
   semana debajo).
5. **Copia**: exportar (con fecha de la última) e importar.
6. **Chroma Pro**: comprar o "ya lo tienes", restaurar.
7. **Más apps**: una fila por hermana publicada en esa tienda.
8. **Acerca de**: versión.

---

## 8. Amigos (v1.1)

### 8.1 Sin cuenta

Pantalla de presentación: tres líneas (`friendsIntro1` a `friendsIntro3`) sobre una tira de colores
de ejemplo, callada para el lector de pantalla. Botones: Apple y Google, iguales y con borde, Apple
primero (App Store 4.8: igual de visibles). Tras iniciar sesión, si
es la primera vez: nombre visible (con tope visible `n/30`, como la palabra del día), casilla
`age16`, `termsAgree` con el enlace `termsRow`, y `continue`. Los términos se aceptan ahí, antes de ver nada de nadie (Apple 1.2).

### 8.2 Con cuenta y sin amigos

`friendsEmpty` y el botón principal `inviteFriend`.

### 8.3 Feed

- Arriba, **la paleta del círculo**: una tira de 24 de alto, radio 12, con un segmento por amigo con
  color hoy, en orden de hora (el primero del día a la izquierda). Sin nombres. Si nadie tiene color
  hoy, no hay tira. El lector de pantalla la anuncia como `a11yFriendsToday`, sin número.
- Solicitudes recibidas, si las hay: bajo la etiqueta `requestsTitle`, una fila por persona con su
  nombre, `ignore` y `accept`. Sin número. Ignorar no avisa a nadie.
- Las tarjetas (`compact`) de hoy y de ayer, separadas por los títulos `feedToday` y
  `feedYesterday`, en orden de hora: la última cambiada, arriba. Cada una con el nombre y la fecha de
  su autor, que puede no ser la tuya cerca de medianoche.
- Al final, `caughtUp`. Nada debajo. Por eso, con amigos, `inviteFriend` va arriba a la derecha,
  junto al título, y no al final.
- Tirar hacia abajo refresca.
- Tocar el nombre del autor abre su mosaico. Tres puntos arriba a la derecha de la tarjeta, en su
  tinta, o mantener pulsada la tarjeta, abren un diálogo con su nombre y `report`, `block` y
  `removeFriend`. Tres puntos y no un icono propio: es el signo de "más opciones" en las dos
  plataformas y no se lee como una reacción. Tienen que verse: solo con pulsación larga nadie
  encontraba cómo reportar (App Store 1.2 pide que se encuentre).
- Cada una pide confirmación (`reportText`, `blockText`, `removeFriendText`). Reportar oculta la
  tarjeta al momento, en el feed y en el año; si el reporte no llega a salir, vuelve y se dice.

### 8.4 Mosaico de un amigo

La rejilla de Mi año con los días que compartió. Título: su nombre. Menú: `removeFriend` y `block`.
Sin contadores. Con más de un año, flechas de año bajo el título. Un año sin nada dice
`friendYearEmpty`.

Tocar un día abre su tarjeta a tamaño completo, con su nombre y su fecha. Pasada la semana de la
foto en el servidor, la tarjeta es solo el color.

Se llega tocando el nombre en una tarjeta del feed, o desde `friendsRow` en Ajustes > Amigos: la
lista de nombres, por orden alfabético y sin número. Es la forma de llegar a un amigo que no ha
compartido nada hoy ni ayer.

### 8.5 Compartir el día

En Hoy, bajo la tarjeta, un segmentado de tres: `sharePrivate`, `shareColor`, `sharePhoto`. Solo se
ve con cuenta. La primera vez que se acepta un amigo, un diálogo pregunta el valor por defecto
(`askDefaultShare`).

### 8.6 Invitar

Capa a pantalla completa con el encabezado de todas (cerrar y `inviteFriend` en la misma fila),
`inviteText`, el QR (`a11yInviteQr` para el lector), el enlace y dos botones: `shareLink` y
`regenerateLink` (con confirmación `regenerateText`, en `error`: el enlace viejo deja de valer). Se
abre desde Amigos y desde `inviteFriend` en Ajustes.

El QR mide 240 de lado, radio 20, con cuatro módulos de margen, y es **siempre tinta sobre blanco**
(`onBackground` y `surface` del tema claro) aunque la app esté en oscuro: no todas las cámaras leen un
QR invertido. Cada módulo ocupa píxeles enteros para que no queden líneas entre módulos.

Abrir un enlace de otra persona lleva a Amigos y deja un aviso tranquilo con el resultado:
`inviteSent`, `inviteAccepted`, `inviteAlready`, `inviteSelf`, `inviteInvalid` o `friendLimit`.

---

## 9. Paywall

El `ProDialog` de Purl: título `proTitle`, lo que incluye (`proPoster`, `proYearWidget`, y en v1.2
`proStats`), `proOnce`, el precio leído de la tienda, comprar, restaurar y cerrar.

---

## 10. Widgets

| Widget | Tamaño | Contenido |
|---|---|---|
| Hoy | Pequeño (2x2) | Con entrada: el color a sangre y su nombre en tinta. Sin entrada: `surfaceVariant`, `widgetEmpty`. v1.2: tira de amigos de 8 de alto y radio 4 abajo, con o sin entrada, en orden de hora y sin nombres; sin amigos, no hay tira |
| Año | Mediano (4x2) | El año arriba y la rejilla tumbada: 12 filas de meses por 31 columnas, porque el widget es más ancho que alto. Días futuros más tenues. Sin Pro: la rejilla vacía con `proTitle` y `widgetUnlock` encima |

Tocar abre Hoy o Mi año; el del año sin Pro abre el paywall, que explica más que una rejilla vacía.
La rejilla del año es una imagen: el lector de pantalla la anuncia como `a11yYearWidget`.

---

## 11. Icono

Sobre `#1C1B1A` (la tinta de la primera paleta; el icono no cambió con el gris neutro porque a ese
tamaño no se distingue y el de iOS es una imagen generada), un bloque de radio 72 (sobre 1024) con cinco franjas
verticales de cálido a frío y una franja estrecha de papel: el día que falta por pintar. Los cinco
colores son los de una tarde junto al mar (`#E07A5F`, `#F2CC8F`, `#81B29A`, `#5B8DB8`, `#3D5A80`) y
no un espectro, que se leería como una bandera. La capa monocroma de Android y el icono de
notificación son la misma silueta en un solo color, con huecos entre franjas para que no quede una
losa lisa. Todo lo genera `tools/icon.py` (necesita `rsvg-convert`).
