# Pantallas de Chroma

La interfaz pantalla a pantalla. Medidas en dp (pt en iOS). Los textos se nombran por su clave de
`Strings.kt`; `docs/textos.md` fija el tono.

---

## 1. Tokens

### Color

La interfaz es neutra: el único color vivo lo pone el usuario.

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| `background` | `#F6F4F1` | `#141312` | Fondo de pantalla |
| `surface` | `#FFFFFF` | `#1E1D1B` | Hojas, diálogos, filas |
| `surfaceVariant` | `#ECE9E4` | `#2A2826` | Días vacíos de la rejilla, campos |
| `onBackground` | `#1C1B1A` | `#EDEAE6` | Texto principal |
| `onMuted` | `#6E6A64` | `#A29D96` | Texto secundario |
| `outline` | `#D9D5CF` | `#3A3734` | Separadores, bordes de los círculos de color |
| `accent` | `#1C1B1A` | `#EDEAE6` | Botón principal: relleno del color del texto, texto del color de fondo |
| `error` | `#B3261E` | `#F2B8B5` | Avisos de error |

Sobre la tarjeta, la tinta sale de `inkFor(color)` (`tecnico.md` 6.5), nunca del tema, y siempre
plena: la jerarquía es de tamaño y peso, no de transparencia.

### Tipografía

La del sistema (Roboto, SF Pro). Sin fuente propia: el color es el protagonista.

| Estilo | Tamaño / peso | Uso |
|---|---|---|
| `display` | 34 / Medium | Nombre del color en la tarjeta de pantalla completa |
| `title` | 22 / Medium | Títulos de pantalla |
| `body` | 16 / Regular | Texto normal |
| `label` | 13 / Medium | Hex, fechas, subtítulos |
| `caption` | 12 / Regular | Notas pequeñas |

### Forma y espacio

Rejilla de 4. Márgenes laterales 20. Radio de tarjeta 28, de hoja 28 arriba, de botón 24 (píldora),
de miniatura 14. Tocables de 48 como mínimo.

---

## 2. Navegación

Barra inferior con tres destinos: **Hoy**, **Mi año** y **Amigos** (Amigos aparece en v1.1). Ajustes
es un icono arriba a la derecha de Hoy. Atrás, en Android, cierra la capa abierta y después vuelve a
Hoy.

Capas sobre cualquier pantalla: el día abierto (hoja), la foto a pantalla completa, compartir, el
paywall y el bloqueo.

---

## 3. Hoy

### Sin entrada

- Arriba, la fecha larga (`longDate`) en `title` y el icono de Ajustes.
- Centro: un círculo de 160 con borde `outline` de 2 y un icono de cámara; debajo, `todayPrompt`
  ("¿De qué color es hoy?").
- Botón principal a lo ancho: `takePhoto`. Debajo, botón de texto: `fromGallery`.
- Primera sesión: bajo el prompt, `firstHelp` en `onMuted`. Nada más.

### Eligiendo color

Tras la foto, en la misma pantalla:

- La foto ocupa el ancho con radio 28 y proporción 4:5.
- Debajo, los candidatos: círculos de 56 con borde `outline` de 1, separados 12, centrados. El
  elegido lleva un anillo de 3 en `onBackground` a 4 de distancia.
- Tocar un candidato lo elige y guarda al momento. No hay botón de confirmar.
- Mientras se analiza la foto, los círculos se pintan en `surfaceVariant` (menos de 100 ms, casi
  nunca se ve).
- Si la foto de galería no es de hoy: aviso `galleryNotToday` y se vuelve al estado sin entrada.

### Con entrada

- La tarjeta (sección 6) ocupa el ancho, proporción 4:5.
- Debajo, la fila de candidatos, más pequeña (círculos de 40), para cambiar de color durante el día.
- Debajo, `addWord` como botón de texto; tocarlo abre un campo de una línea con tope visible (`n/24`).
- En v1.1, el control de compartir (sección 8.5).
- Menú de la tarjeta (tres puntos): `retakePhoto`, `share`, `deleteDay`.

---

## 4. Mi año

- Título: el año, con flechas a los lados si hay más de uno con entradas.
- Selector de vista segmentado: `viewGrid` y `viewStrip`.
- **Rejilla**: una columna por mes y una fila por día (12 x 31), como Purl: en un móvil da celdas de
  unos 24, que se tocan bien; girada saldrían de 10. Celdas cuadradas con 3 de separación y radio 3;
  inicial del mes arriba y los días 1, 10, 20 y 30 a la izquierda, en `caption`. Días sin color en
  `surfaceVariant` (los futuros, más claros); días que no existen (31 de febrero), vacíos. Hoy lleva
  un borde de 1,5 en `onBackground`.
- **Tira**: el año como columnas de 1 día, sin separación, a toda la altura disponible (proporción
  4:5). Los días vacíos no se pintan: la tira se comprime.
- Tocar un día con entrada abre el día (sección 5).
- Abajo: botón `poster`. Abre la misma capa que compartir (sección 5) con el póster del año y un
  segmentado de tres: `posterGrid`, `posterStrip`, `posterWallpaper`. Mirarlo es gratis; sin Pro,
  compartir y guardar abren el paywall.
- Año sin entradas: `yearEmpty`.

---

## 5. El día abierto

Hoja modal con la tarjeta a lo ancho. Acciones: `share` y `deleteDay` (con confirmación). Un día
pasado no se edita.

Tocar la miniatura abre la foto a pantalla completa, sobre negro, con cerrar arriba a la izquierda.

### Compartir

Capa a pantalla completa desde el menú de Hoy o desde el día abierto. Arriba, cerrar. En medio, la
tarjeta de 1080x1350 (`tecnico.md` 6.9) a 320 de ancho como mucho, con radio 14 y borde `outline`.
Debajo, si el día tiene foto, un segmentado `shareColorOnly` / `shareWithPhoto` (empieza con la
foto): solo el color quita la miniatura de la imagen, no del día. Después, `share` (hoja del
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
| Marca de sintonía (v1.1) | Arriba derecha | Dos círculos solapados de 10, tinta |
| Marca de la semana (v1.2) | Arriba derecha, a la izquierda de la sintonía | Un rombo de 10, tinta |

Sin foto (día compartido solo con color, o foto caducada en el servidor), no hay miniatura.

---

## 7. Ajustes

Lista de secciones, como Purl:

1. **Recordatorio**: interruptor y hora (`reminderRow`).
2. **Amigos** (v1.1): cuenta y nombre visible, `defaultShareRow`, `inviteRow`, `signOut`,
   `deleteAccount`.
3. **Privacidad**: bloqueo (v1.2), política.
4. **Tarjeta**: `watermarkRow`, `weekColorRow` (v1.2).
5. **Copia**: exportar (con fecha de la última) e importar.
6. **Chroma Pro**: comprar o "ya lo tienes", restaurar.
7. **Más apps**: una fila por hermana publicada en esa tienda.
8. **Acerca de**: versión.

---

## 8. Amigos (v1.1)

### 8.1 Sin cuenta

Pantalla de presentación: tres líneas (`friendsIntro1` a `friendsIntro3`) sobre una tira de colores
de ejemplo. Botones: Sign in with Apple y Google (los oficiales de cada uno). Tras iniciar sesión, si
es la primera vez: nombre visible, casilla `age16` y `continue`.

### 8.2 Con cuenta y sin amigos

`friendsEmpty` y el botón principal `inviteFriend`.

### 8.3 Feed

- Arriba, **la paleta del círculo**: una tira de 24 de alto, radio 12, con un segmento por amigo con
  color hoy, en orden de hora (el primero del día a la izquierda). Sin nombres. Si nadie tiene color
  hoy, no hay tira.
- Solicitudes recibidas, si las hay: bajo la etiqueta `requestsTitle`, una fila por persona con su
  nombre, `ignore` y `accept`. Sin número. Ignorar no avisa a nadie.
- Las tarjetas (`compact`) de hoy y de ayer, separadas por los títulos `feedToday` y
  `feedYesterday`, en orden de hora: la última cambiada, arriba. Cada una con el nombre y la fecha de
  su autor, que puede no ser la tuya cerca de medianoche.
- Al final, `caughtUp`. Nada debajo. Por eso, con amigos, `inviteFriend` va arriba a la derecha,
  junto al título, y no al final.
- Tirar hacia abajo refresca.
- Tocar el nombre del autor abre su mosaico. Mantener pulsada la tarjeta, o su menú, da `report`,
  `block` y `removeFriend`.

### 8.4 Mosaico de un amigo

La rejilla de Mi año con los días que compartió. Título: su nombre. Menú: `removeFriend` y `block`.
Sin contadores.

### 8.5 Compartir el día

En Hoy, bajo la tarjeta, un segmentado de tres: `sharePrivate`, `shareColor`, `sharePhoto`. Solo se
ve con cuenta. La primera vez que se acepta un amigo, un diálogo pregunta el valor por defecto
(`askDefaultShare`).

### 8.6 Invitar

Pantalla completa (como la de compartir) con `inviteFriend`, `inviteText`, el QR, el enlace y dos
botones: `shareLink` y `regenerateLink` (con confirmación `regenerateText`: el enlace viejo deja de
valer). Se abre desde Amigos y desde `inviteRow` en Ajustes.

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
| Hoy | Pequeño (2x2) | Con entrada: el color a sangre y su nombre en tinta. Sin entrada: `surfaceVariant`, `widgetEmpty`. v1.2: tira de amigos de 8 de alto abajo |
| Año | Mediano (4x2) | El año arriba y la rejilla tumbada: 12 filas de meses por 31 columnas, porque el widget es más ancho que alto. Días futuros más tenues. Sin Pro: la rejilla vacía con `proTitle` y `widgetUnlock` encima |

Tocar abre Hoy o Mi año; el del año sin Pro abre el paywall, que explica más que una rejilla vacía.

---

## 11. Icono

Sobre la tinta de la app (`#1C1B1A`), un bloque de radio 72 (sobre 1024) con cinco franjas
verticales de cálido a frío y una franja estrecha de papel: el día que falta por pintar. Los cinco
colores son los de una tarde junto al mar (`#E07A5F`, `#F2CC8F`, `#81B29A`, `#5B8DB8`, `#3D5A80`) y
no un espectro, que se leería como una bandera. La capa monocroma de Android y el icono de
notificación son la misma silueta en un solo color, con huecos entre franjas para que no quede una
losa lisa. Todo lo genera `tools/icon.py` (necesita `rsvg-convert`).
