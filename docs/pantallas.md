# Pantallas de Chroma

La interfaz pantalla a pantalla. Medidas en dp (pt en iOS). Los textos se nombran por su clave de
`Strings.kt`; `docs/textos.md` fija el tono.

---

## 1. Tokens

### Color

La interfaz es gris sin tinte, como una cabina de ver color: un gris cálido o frío cambia cómo se ve
el color que tiene encima, y este es el único producto de la app. El único color vivo lo pone el
usuario. La excepción es `error`, reservado a lo que no se deshace. Las imágenes que salen de la app
(la tarjeta y el póster, `tecnico.md` 6.9) van siempre sobre el papel claro, sea cual sea el tema: se
imprimen, se ponen de fondo o se ven en el móvil de otro.

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| `background` | `#F1F1F1` | `#0E0E0E` | Fondo de pantalla |
| `surface` | `#FFFFFF` | `#1A1A1A` | Tarjetas de ajustes, avisos, diálogos, la cápsula de navegación |
| `surfaceVariant` | `#E6E6E6` | `#252525` | Días vacíos de la rejilla, campos, la ficha en blanco, las teselas de icono |
| `onBackground` | `#111111` | `#F2F2F2` | Texto principal |
| `onMuted` | `#5E5E5E` | `#A3A3A3` | Texto secundario (contraste 6:1 en claro, 7,5:1 en oscuro) |
| `outline` | `#D6D6D6` | `#303030` | Bordes de los círculos de color y de los botones con borde |
| `outlineVariant` | `#EAEAEA` | `#222222` | Filetes entre filas de una tarjeta de ajustes |
| `accent` | `#111111` | `#F2F2F2` | Botón principal: relleno del color del texto, texto del color de fondo |
| `error` | `#B3261E` | `#F2B8B5` | Avisos de error y el botón que confirma algo que no se deshace |

Sobre la tarjeta, la tinta sale de `inkFor(color)` (`tecnico.md` 6.5), nunca del tema, y siempre
plena: la jerarquía es de tamaño y peso, no de transparencia.

### Tipografía

La del sistema (Roboto, SF Pro). Sin fuente propia: el color es el protagonista. La voz sale del
peso y del espaciado: los números, enormes y finos como la lectura de un instrumento de medir color;
las etiquetas, en versalitas espaciadas; los códigos, con cifras tabulares como el de una muestra de
pintura.

| Estilo | Tamaño / peso | Uso |
|---|---|---|
| `numeral` | 64 / ExtraLight, espaciado -2,5 | El día en la cabecera de Hoy y del día abierto, el año en Mi año |
| `display` | 34 / Medium, espaciado -0,6 | Nombre del color en la tarjeta (40 SemiBold), la pregunta de la ficha en blanco, el título de Ajustes |
| `title` | 22 / SemiBold, espaciado -0,3 | Títulos de capa (compartir, póster, En palabras) |
| `body` | 17 / Regular | Texto normal, el de los botones en SemiBold (el cuerpo de iOS; 16 se queda corto para leer sin gafas) |
| `label` | 14 / Medium | Subtítulos, la cápsula de navegación |
| `eyebrow` | 12 / SemiBold, espaciado 1,4, en mayúsculas | Nombres de sección, el día de la semana, `pickColor`, la fecha en la tarjeta |
| `code` | 14 / Medium, espaciado 1, cifras tabulares | El hex, en la tarjeta y en la ficha en blanco (`#------`) |
| `caption` | 13 / Regular | Notas pequeñas; nada baja de 13 (salvo `eyebrow`, que va en mayúsculas y seminegrita) |

Todo escala con el tamaño de letra del sistema. Por eso ninguna fila con texto tiene alto fijo, solo
mínimo: con letra grande, la fila crece en vez de cortar. La excepción son las etiquetas dibujadas
dentro de la rejilla del año, que no escalan porque la rejilla tampoco.

### Forma y espacio

Rejilla de 4. Márgenes laterales 20. Radio de tarjeta 28, de hoja 28 arriba, de botón 24 (píldora),
de tarjeta de ajustes y de tesela 20, de miniatura 14, de tesela de icono 11 (36 de lado, glifo de 18). Tocables de 48 como mínimo, sin excepción de estilo: el botón principal mide 52, el
de borde, el de texto y cada opción de un segmentado, 48. La única excepción es la celda de la
rejilla del año (unos 22), porque el año entero tiene que caber de un vistazo; cada día se puede
abrir igual con el lector de pantalla.

### Movimiento y tacto

Un solo momento se mueve de verdad: **al elegir el color del día, el color brota de la miniatura de la
foto** y se extiende hasta cubrir la tarjeta (700 ms, frenando al final), porque de ahí ha salido. El
nombre, el hex y la fecha llegan al final, nunca sobre el fondo vacío. Solo justo tras elegir: en Mi
año, en el feed o al volver a Hoy, la tarjeta está sin más.

Cambiar el color durante el día funde el viejo en el nuevo (400 ms). Nada más se anima: una app que
se mueve por todas partes deja de enseñar el color. Con "Reducir movimiento" (iOS) o las animaciones
apagadas (Android) todo esto se salta solo, porque Compose lo respeta.

El tacto, igual de escaso: un toque de hecho al guardar el color del día y al aceptar a un amigo, que
pasan una vez; un tic ligero al cambiar el color después. Nada más vibra.

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
- El teclado nunca tapa lo que se escribe, y el hueco que deja no se descuenta dos veces sobre la
  barra inferior, que el teclado ya cubre.
- Lo que solo es forma lleva descripción para el lector de pantalla (el QR, la tira de amigos, el
  widget del año) o se calla si es de adorno (la tira de ejemplo).

---

## 2. Navegación

Una cápsula flotante abajo, centrada, en `surface` con sombra suave: dos o tres sitios no piden una
banda de marco de lado a lado. Destinos: **Hoy**, **Mi año** y **Amigos** (Amigos aparece en v1.1),
cada uno con icono y nombre; el elegido va relleno de tinta, como el botón principal. Ajustes es un
icono arriba a la derecha de Hoy.

El icono de Hoy es un punto. Con el color del día elegido, el punto es ese color (14, con borde
`outline` de 1 para que un blanco no desaparezca): es el único sitio donde el marco de la app toma
color, y dice de un vistazo, desde cualquier pestaña, si hoy ya tiene el suyo. Atrás, en Android, cierra la capa abierta y después vuelve a
Hoy.

Capas sobre cualquier pantalla: el día abierto (hoja), la foto a pantalla completa, compartir, el
paywall y el bloqueo.

---

## 3. Hoy

### Sin entrada

- Arriba, la cabecera de las pantallas con fecha: el día en `numeral` y, a su lado, el día de la
  semana en `eyebrow` sobre el mes en `title` sin negrita. Al final de la fila, el icono de Ajustes.
- Debajo, **la ficha en blanco**: la tarjeta antes de tener color, del mismo tamaño y forma (4:5,
  radio 28) en `surfaceVariant`, con `todayPrompt` en `display` donde irá el nombre y `#------` en
  `code` donde irá el hex. Sin ilustración ni icono dentro: el círculo con cámara que hubo aquí era un
  segundo botón sin texto que hacía lo mismo que el de debajo.
- Primera sesión: bajo la ficha, `firstHelp` en `onMuted`.
- Botón principal a lo ancho con el icono de cámara: `takePhoto`. Debajo, botón de texto:
  `fromGallery`.
- Con el color de la semana encendido (v1.2): arriba a la izquierda de la ficha, una píldora en
  `surface` con un punto de 12 de ese color y `weekHint(nombre)` en `caption`. Una pista, no una
  tarea. Nada más.

### Eligiendo color

Tras la foto, en la misma pantalla:

- La foto ocupa el ancho con radio 28 y proporción 4:5.
- Debajo, los candidatos: círculos de 56 con borde `outline` de 1, separados 12, centrados. El
  elegido lleva un anillo de 3 en `onBackground` a 4 de distancia. Si no caben (cinco piden unos 400
  y un móvil deja unos 320), encogen todos por igual y siguen redondos.
- Tocar un candidato lo elige y guarda al momento. No hay botón de confirmar.
- Debajo, siempre, `cancel`: vuelve a lo que había antes de la foto, también con la primera del día.
  Nada se guarda hasta tocar un color.
- Sobre los círculos, `pickColor` en `eyebrow`: la primera vez no es obvio que hay que tocar uno.
- Mientras se analiza la foto (menos de 100 ms), los botones se desactivan y sale `working`.
- Si la foto de galería no es de hoy: aviso `galleryNotToday` y se vuelve al estado sin entrada.

### Con entrada

- La tarjeta (sección 6) ocupa el ancho, proporción 4:5.
- Debajo, la fila de candidatos, más pequeña (círculos de 40), para cambiar de color durante el día.
- Debajo, `addWord` como botón de texto; tocarlo abre un campo de una línea con tope visible (`n/24`)
  y el teclado a la vez: un toque, no dos. El campo entero sube por encima del teclado, no solo la
  línea del cursor. Una palabra ya escrita no abre el teclado al volver a Hoy.
- En v1.1, el control de compartir (sección 8.5).
- Menú de la tarjeta (tres puntos): `retakePhoto`, `share`, `deleteDay` (este en `error`).

---

## 4. Mi año

- Cabecera: `navYear` en `eyebrow` y el año en `numeral`, la misma lectura que el día en Hoy. Con
  más de un año con entradas, las flechas al final de la fila. Empieza 20 dp bajo la barra de estado,
  no 8 como Hoy: aquí lo primero es el `eyebrow`, pequeño y sin el aire propio del `numeral`, y a 8 dp
  quedaba pegado a la barra (y el recorte de las capturas de la ficha se lo comía).
- Debajo, `poster` y `stats` (v1.2) como dos teselas lado a lado (en `surface`, radio 20, icono y
  nombre). Van antes de la rejilla, que mide unos 800 y las dejaba fuera de la pantalla. Sin Pro,
  `stats` lleva `proTag` y abre el paywall.
- Después, el segmentado de vista: `viewGrid` y `viewStrip`, con el elegido relleno de tinta. Solo con
  entradas: en un año vacío no hay nada que ver de otra forma.
- La rejilla va sobre una tarjeta en `surface`, radio 24.
- **Rejilla**: una columna por mes y una fila por día (12 x 31), como Purl: en un móvil da celdas de
  unos 24, que se tocan bien; girada saldrían de 10. Celdas cuadradas con 3 de separación y radio 3;
  inicial del mes arriba y los días 1, 10, 20 y 30 a la izquierda, en `caption`. Días sin color en
  `surfaceVariant` (los futuros, más claros); días que no existen (31 de febrero), vacíos. Hoy lleva
  un borde de 1,5 en `onBackground`. Las celdas no pasan de 24: en un móvil ancho sobra sitio y el bloque
  (números de día incluidos) va centrado, no pegado a la izquierda.
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

Hoja modal con la tarjeta a lo ancho. Arriba, cerrar; debajo, la misma cabecera que Hoy (el día en
`numeral`, día de la semana y mes), con el año tras el mes solo si no es el actual. Acciones, al
final de la cabecera: `share` y `deleteDay` (con confirmación). Un día
pasado no se edita.

Tocar la miniatura abre la foto a pantalla completa, sobre negro, con cerrar arriba a la izquierda
sobre un círculo negro al 40 %: sin él, una foto de cielo o de nieve se comía la única salida.

### Compartir

Capa a pantalla completa desde el menú de Hoy o desde el día abierto. Arriba, cerrar y el título
(`share`, o `poster` en el póster); a 8, como en el resto de capas, la imagen, sobre un escenario en
`surfaceVariant` de radio 28 y con una sombra suave, como una copia que se mira antes de llevársela. El lector de pantalla la lee como el color, su hex y la fecha (el póster,
como el año y su estilo): es todo lo que hay en la pantalla. En medio, la
tarjeta de 1080x1350 (`tecnico.md` 6.9) a 320 de ancho como mucho, con radio 12.
Debajo, si el día tiene foto, una fila con interruptor en su tarjeta, `includePhoto` (empieza encendido): apagarlo
quita la miniatura de la imagen, no del día. No usa las palabras de compartir con amigos
(`shareColorOnly`, `shareWithPhoto`) porque son dos ajustes distintos y con las mismas palabras
parecían uno. Después, lado a lado, `share` como botón principal con su icono (hoja del sistema) y
`saveToPhotos` con borde (en Android solo desde la 10, que no pide permiso). Sin Pro, en el póster,
los dos van con borde y `proTag`. La marca "Chroma" la
decide el ajuste `watermarkRow`, no esta pantalla.

---

## 6. La tarjeta

Componente `ChromaCard(entry, date, author?, compact, reveal)`. Proporción 4:5, radio 28. En `compact`,
margen 16 y miniatura a 12 del borde (rejilla de 4).

| Elemento | Posición | Estilo |
|---|---|---|
| Fondo | Todo | El color |
| Nombre del color | Arriba izquierda, margen 24 | `display` a 40 SemiBold (`title` si `compact`), tinta |
| Hex | Bajo el nombre, a 2 | `code` (se lee como el código de una muestra de pintura), tinta |
| Palabra | Bajo el hex, a 12 | `body` en cursiva, tinta |
| Autor (feed) | Abajo izquierda, sobre la fecha | `body` Medium, tinta; tocable de 48 de alto |
| Fecha | Abajo izquierda, margen 24 | `eyebrow`, en mayúsculas, tinta |
| Miniatura | Abajo derecha, margen 20, lado 30 % del ancho | Radio 14, borde de 2 en tinta al 24 % |
| Marca de sintonía (v1.1) | Arriba derecha | Dos aros solapados de 10 (16 de ancho, trazo 1,5), tinta; se lee `inTune` |
| Marca de la semana (v1.2) | Arriba derecha, a la izquierda de la sintonía | Un rombo de 10 relleno, tinta; se lee `weekColorRow` |

Sin foto (día compartido solo con color, o foto caducada en el servidor), no hay miniatura.

---

## 7. Ajustes

Cerrar arriba y el título `settingsTitle` en `display`. Sin Pro, justo debajo, **la tarjeta de Pro**:
`proRow` en `title` con su icono, `proSubtitle`, y, con dos días o más este año, una tira de 28 de alto
con los colores del usuario: el póster se hace con sus días, no con una foto de catálogo. Toda la
tarjeta abre el paywall.

Después, las secciones: cada una con su nombre en `eyebrow` y sus filas juntas en una tarjeta en
`surface` de radio 20, separadas por un filete `outlineVariant` que empieza donde empieza el texto.
Cada fila, de 60 como mínimo, lleva delante una tesela con su icono (campana, candado, documento,
sello, rombo, exportar, importar, restaurar, apps) y al final su interruptor o, si lleva a otro sitio,
una flecha. Una lista de opciones se lee como unas pocas cosas que decidir, no como un párrafo:

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
   - Lo de debajo sigue vivo (una foto a medio elegir no se pierde), pero la capa se traga los toques,
     el lector de pantalla no lo lee y los diálogos y menús, que son ventanas propias, esperan al
     desbloqueo (`LocalLocked`).
   - El tiempo fuera se mide con `elapsedMillis` (en Android `elapsedRealtime`, que cuenta el sueño y
     no se puede cambiar; en iOS el reloj de pared). Un reloj que va hacia atrás bloquea. Volver de un
     viaje propio (cámara, galería, selector de ficheros, hoja de compartir) espera `TRIP_GRACE`
     (10 min) en vez de un minuto. El viaje se marca con la hora y solo cuenta si la app sale de
     pantalla en los 10 s siguientes: una hoja que no la saca (iOS) o un lanzamiento que falla no
     regalan los 10 min a la siguiente salida.
   - En iOS las hojas del sistema (compartir, galería, ficheros) se presentan sobre Compose y quedan
     por encima de la capa si se dejan abiertas al salir. Se acepta: lo que enseñan es lo que el dueño
     acababa de elegir compartir o abrir.
   - Si el teléfono se queda sin bloqueo de pantalla (o la copia llega a uno sin él), el de Chroma se
     apaga solo en cuanto la capa lo intenta, al abrir o con `unlock`: nadie podría contestar al
     diálogo, y quien tiene un móvil sin bloqueo ya puede abrir todo lo que hay en él. En Android se
     pregunta a `KeyguardManager.isDeviceSecure`, no a `canAuthenticate`, que en algunos móviles dice
     que no con un código puesto. El interruptor sigue activo con el bloqueo encendido aunque falte el
     del sistema, para poder apagarlo.
   - Los widgets siguen enseñando colores: están en la pantalla de inicio porque el usuario los puso,
     y nunca llevan fotos ni palabras.
4. **Tarjeta**: `watermarkRow`, `weekColorRow` (v1.2, interruptor con el nombre del color de esta
   semana debajo, precedido de un punto de 12 de ese color: el nombre solo no dice qué violeta es).
5. **Copia**: exportar (con fecha de la última) e importar.
6. **Chroma Pro**: "ya lo tienes" con una marca (sin Pro lo vende la tarjeta de arriba), restaurar.
7. **Más apps**: una fila por hermana publicada en esa tienda.

Al pie, fuera de las tarjetas y centrado, "Chroma" y la versión: es un dato de la app, no un ajuste.

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

Un diálogo propio en `surface`, radio 28: `proTitle` con su icono; con dos días o más este año, una
tira de 56 de alto con los colores del usuario; lo que incluye, una fila por cosa con su icono
(`proPoster`, `proYearWidget`, y en v1.2 `proStats`); `proOnce` en `caption` (con `proFriendsFree`
solo cuando Amigos existe, v1.1); el botón principal a lo ancho con el precio leído de la tienda
(`buy`), y debajo `restore` y `notNow` como botones de texto en gris.

---

## 10. Widgets

| Widget | Tamaño | Contenido |
|---|---|---|
| Hoy | Pequeño (2x2) | La misma lectura que la app: el día grande y fino arriba. Con entrada: el color a sangre, abajo su nombre en negrita y el hex, en tinta. Sin entrada: `surfaceVariant`, `widgetEmpty` y `#------`, como la ficha en blanco. v1.2: tira de amigos de 8 de alto y radio 4 abajo, con o sin entrada, en orden de hora y sin nombres; sin amigos, no hay tira |
| Año | Mediano (4x2) | El año arriba y la rejilla tumbada: 12 filas de meses por 31 columnas, porque el widget es más ancho que alto. Días futuros más tenues. Sin Pro: la rejilla vacía con `proTitle` y `widgetUnlock` encima |

Tocar abre Hoy o Mi año; el del año sin Pro abre el paywall, que explica más que una rejilla vacía.
La rejilla del año es una imagen: el lector de pantalla la anuncia como `a11yYearWidget`, y la tira de
amigos como `a11yFriendsToday`. Margen interior 14. El año del widget grande va a 20, fino. En los widgets rige el mismo suelo de 13 y el gris
secundario es el token (`WidgetMuted` en iOS, `widget_muted` en Android), no el del sistema.

---

## 11. Icono

Sobre `#1C1B1A` (la tinta de la primera paleta; el icono no cambió con el gris neutro porque a ese
tamaño no se distingue y el de iOS es una imagen generada), un bloque de radio 72 (sobre 1024) con cinco franjas
verticales de cálido a frío y una franja estrecha de papel: el día que falta por pintar. Los cinco
colores son los de una tarde junto al mar (`#E07A5F`, `#F2CC8F`, `#81B29A`, `#5B8DB8`, `#3D5A80`) y
no un espectro, que se leería como una bandera. La capa monocroma de Android y el icono de
notificación son la misma silueta en un solo color, con huecos entre franjas para que no quede una
losa lisa. Todo lo genera `tools/icon.py` (necesita `rsvg-convert`).
