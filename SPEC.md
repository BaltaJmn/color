# Chroma: color del día, spec de producto

App Compose Multiplatform (Android + iOS) en la que cada día haces una foto, la app te ofrece los
colores que hay en ella y tú eliges uno. Ese color es tu día. Al final del año tienes tu vida pintada
en una rejilla de 365 colores. Si quieres, y solo si quieres, tus amigos ven tu color del día y tú
el suyo.

Cuarta de la familia. Hermana de **Quilt** (`../HabitTracker`, `com.baltajmn.habit`), de
**MoodTraker** (`../MoodTraker`, `com.baltajmn.mood`) y de **Purl** (`../line`, `com.baltajmn.line`):
misma arquitectura, mismo almacén local, pago único. Nombre de tienda **Chroma: color del día**,
nombre bajo el icono **Chroma**. Identificador en las dos tiendas: `com.baltajmn.color`. El
repositorio y el código se llaman `color`.

Es la primera de la familia con parte social, y eso cambia una promesa. Las hermanas dicen "tus datos
no salen del teléfono". Chroma dice: **lo que no compartes no sale del teléfono**.

La promesa en una frase: *un segundo al día para mirar alrededor; un año después, tu vida en colores*.

## Documentos

Este SPEC es el porqué del producto: qué hace la app, qué no hace y por qué. Lo que se programa está
escrito aparte, como en Purl.

| Dónde | Qué contiene |
|---|---|
| `docs/tecnico.md` | El contrato de implementación: árbol de código, modelo, `entries.json`, `widget.json`, extracción de color, esquema del servidor, reglas RLS y tests |
| `docs/pantallas.md` | La interfaz pantalla a pantalla, con tokens, medidas y estados |
| `docs/textos.md` | Tono y vocabulario. Los textos viven en `Strings.kt` y los nombres de color en `Names.kt` |
| Issues del repo | `gh issue list -R BaltaJmn/color`: #1 a #44, una por pieza, en los hitos v1.0, v1.1 y v1.2 |

Todo lo que dice este SPEC está decidido. Si un detalle de implementación de este SPEC no coincide con
`docs/tecnico.md`, manda `docs/tecnico.md` y el SPEC se corrige en el mismo cambio.

---

## 1. Benchmark: qué copiar y dónde atacar

| App | Qué es | Lo que hace bien | Lo que le duele |
|---|---|---|---|
| **BeReal** | Una foto al día a una hora que marca la app, para amigos | Demostró que "una foto al día entre amigos" engancha a millones | Aviso a hora impuesta, cuenta atrás de dos minutos, etiqueta de "tarde" y reacciones con la cara: presión por diseño |
| **Locket** | Las fotos de tus amigos aparecen en tu widget | La pantalla de inicio como canal íntimo, sin feed | Se ha ido llenando de rachas, mensajes y funciones de pago |
| **1 Second Everyday** | Un segundo de vídeo al día, montado en una película | La recompensa diferida: el montaje del año emociona | Solo para uno, y el vídeo pesa y cuesta de revisar |
| **Pixels / Year in Pixels de Daylio** | Un color por día según el ánimo | La rejilla del año como objeto que da gusto mirar | El color es una escala de ánimo, no algo que ves |
| **Adobe Capture, Coolors** | Sacar paletas de una foto con la cámara | La extracción de color funciona y gusta | Herramientas de diseño: nada de diario ni de hábito |
| **Instagram** | Fotos para seguidores | Todo el mundo sabe hacer una foto y compartirla | Likes, seguidores, algoritmo, scroll infinito: justo lo que no queremos |

Nadie une las tres piezas: la foto diaria, el color como resumen y un círculo pequeño de amigos sin
métricas.

### Nuestro ataque, en una línea cada uno

1. **El color es el contenido; la foto, el contexto.** Compartir un color no da vergüenza ni pide
   pose. Es lo contrario a Instagram.
2. **Social sin métricas.** Ni likes, ni comentarios, ni contadores, ni seguidores, ni algoritmo. El
   feed se acaba.
3. **Sin hora impuesta ni cuenta atrás.** Tienes todo el día para encontrar tu color.
4. **Funciona entera sin cuenta.** Amigos es una capa opcional; quien no la quiere tiene la app
   completa.
5. **Pago único y lo social gratis.** Nunca se cobra por ver a tus amigos.
6. **Las fotos compartidas se borran del servidor a los 7 días.** Solo quedan los colores.

---

## 2. Funcionalidad esencial

### v1.0: Chroma en solitario

- [ ] Hacer la foto del día con la cámara del sistema, o elegir de la galería una foto **de hoy**.
- [ ] La app extrae hasta 5 colores de la foto y **el usuario elige uno**. No hay cuentagotas libre:
      elegir entre pocos es rápido y siempre sale un color que representa la foto.
- [ ] Nombre del color en el idioma del usuario ("azul tormenta") y, opcionalmente, una palabra.
- [ ] La tarjeta: el color a sangre y la foto pequeña abajo a la derecha.
- [ ] Una entrada por día, editable (otro color, otra foto) hasta que acaba el día lógico. No se
      pueden rellenar días pasados: el color tiene que ser de un día que has vivido mirando.
- [ ] Mi año: rejilla con un cuadro por día y la tira degradada. Tocar un día abre su tarjeta.
- [ ] Compartir la tarjeta como imagen.
- [ ] Póster del año (Pro).
- [ ] Widget de hoy (gratis) y widget del año (Pro).
- [ ] Recordatorio a la hora que elijas, que se calla si ya tienes color.
- [ ] Exportar e importar en zip.
- [ ] Cinco idiomas: en, es, pt, de, fr.

### v1.1: Amigos

- [ ] Cuenta opcional con Apple o Google, pedida solo al abrir Amigos.
- [ ] Añadir amigos por enlace o QR. Amistad mutua, tope de 50.
- [ ] Compartir cada día como privado, solo color, o color y foto.
- [ ] Feed de hoy y ayer, en orden de hora, que se acaba.
- [ ] Paleta del círculo: la tira con los colores de hoy de tus amigos.
- [ ] Mosaico de cada amigo con los días que compartió.
- [ ] Sintonía: marca sutil cuando tu color y el de un amigo casi coinciden.
- [ ] Quitar amigo en silencio, bloquear y reportar.
- [ ] Borrar la cuenta desde la app y desde la web.

### v1.2: extras

- [ ] Color de la semana, opcional y sin puntos.
- [ ] Estadísticas del año (Pro).
- [ ] Paleta de amigos en el widget de hoy.
- [ ] Bloqueo con biometría.

### Descartado a propósito

| Qué | Por qué no |
|---|---|
| Likes, reacciones, emojis | Convierten compartir en esperar aprobación. Es la regla número uno de la app |
| Comentarios y mensajes | Si quieres hablar con tu amigo de su foto, ya tenéis un chat. Chroma no compite con él |
| Seguidores, perfiles públicos, buscador de usuarios | Solo se añade a quien ya conoces. Sin descubrimiento no hay audiencia que perseguir |
| Contadores de cualquier tipo | Ni número de amigos, ni de días, ni de "vistas". Un número invita a compararse |
| "Visto por" | Saber quién ha mirado crea obligación de mirar y de ser mirado |
| Rachas, sociales o no | La rejilla del año ya enseña la constancia sin castigar el día que falta |
| Avisos de "X ha publicado" | Una notificación social abre la app por ansiedad, no por ganas |
| Algoritmo de orden | Hoy y ayer, por hora. Nada más |
| Scroll infinito | El feed acaba en "Ya estás al día" |
| Filtros de foto | El color ya es el filtro |
| Cuentagotas libre sobre la foto | Más lento y peor: acaba en colores que no representan nada |
| Rellenar días pasados | El color de un día que no miraste no significa nada |
| Hora impuesta y cuenta atrás (estilo BeReal) | Presión por diseño |
| Mosaicos de grupo | Complejidad (grupos, permisos, fechas) que nadie ha pedido. Se reabre si sale en reseñas (#44) |
| Push de resumen diario | El widget informa sin interrumpir (#42) |

### Explícitamente fuera de alcance

Vídeo, varias fotos por día, web, iPad con diseño propio, modo tableta en Android, anuncios,
analítica e informes de fallos de terceros.

---

## 3. Decisiones que se toman aquí, no en el código

| Decisión | Valor | Por qué |
|---|---|---|
| Fin del día lógico | **03:00** locales | Igual que Purl: la foto de la cena de un sábado cuenta como sábado |
| Colores que se ofrecen | Hasta **5** | Los suficientes para elegir, los pocos para decidir en un segundo |
| Espacio de color | CIELAB, distancia **deltaE CIE76** en todas partes | Una sola fórmula, suficiente para esta precisión y fácil de probar |
| Foto de galería | Solo si su fecha es de hoy. **Sin fecha legible se acepta** | Sin puntos no hay nada que ganar haciendo trampa; rechazarla castigaría al honesto |
| Palabra opcional | Hasta **24** puntos de código | Una palabra o dos, no un diario: para eso está Purl |
| Foto local | JPEG de **1080 px** de lado largo | La tarjeta exportada es de 1080x1350 |
| Foto subida | JPEG de **720 px**, calidad 80, sin metadatos | La miniatura de la tarjeta y la vista completa de un móvil no piden más |
| Vida de la foto en el servidor | **7 días** | El feed solo enseña hoy y ayer. Acota coste y exposición |
| Vida del color en el servidor | Mientras exista la cuenta | Son bytes, y sostienen el mosaico de cada amigo |
| Tope de amigos | **50** (`MAX_FRIENDS`) | Un círculo, no una audiencia |
| Umbral de sintonía | deltaE < **5** (`SYNC_DELTA_E`) | Tiene que parecer casualidad, no pasar cada día |
| Compartir por defecto | Privado; se pregunta una vez al aceptar el primer amigo | Nada sale sin una decisión consciente |
| Edad mínima de Amigos | **16** años declarados | La edad de consentimiento digital más alta de la UE: vale en todos los países |
| Servidor | Supabase en **Frankfurt** | Ya conectado, SDK KMP (`supabase-kt`), datos en la UE |
| Rachas | **Ninguna** | La rejilla enseña la constancia sin castigo |

---

## 4. Diseño

### La tarjeta

Es la pieza que se ve en todas partes: Hoy, el día abierto desde Mi año, el feed, el mosaico de un
amigo y la imagen para compartir. Un único componente.

- El color elegido, a sangre.
- Arriba a la izquierda, el nombre del color en grande y el hex debajo en pequeño. La palabra del
  usuario, si la hay, bajo el nombre.
- Abajo a la izquierda, la fecha. En el feed, también el nombre del amigo.
- **Abajo a la derecha, la foto original pequeña con esquinas redondeadas.** Tocarla la abre a
  pantalla completa. Si el día se compartió solo con color, no hay miniatura.
- El texto va en blanco o en casi negro según la luminancia del color, con contraste AA en todo el
  espacio de color.

### Tema neutro

Las hermanas tienen paleta propia. Chroma no: el contenido es el color, así que la interfaz va en
neutros cálidos (claro y oscuro, según el sistema) y **el único color vivo de la pantalla lo pone el
usuario**. Los días vacíos de la rejilla usan el tono neutro del tema.

### Mi año

- Rejilla: 53 columnas por 7 filas, como un calendario de contribuciones, o 12 filas por 31 columnas
  en vertical. La que quepa mejor se decide en `docs/pantallas.md`.
- Tira: el año como columnas finas de color, un "código de barras" de tu vida. Es la vista que se
  exporta como póster.

---

## 5. Social sin toxicidad

El principio que decide cada duda: **ver a tus amigos, nunca que te midan.**

### Cómo se llega a tener amigos

- Solo por enlace (`color.baltajmn.dev/i/<code>`) o QR del mismo enlace. No hay buscador, ni
  sugerencias, ni acceso a la agenda.
- Abrir el enlace crea una solicitud. La otra persona la acepta o la ignora; ignorarla no avisa a
  nadie.
- Si quien abre el enlace no tiene la app, la página web le lleva a su tienda y, tras instalar, el
  enlace se puede volver a abrir.
- El código es fijo y se puede regenerar. Un enlace filtrado no da nada: toda amistad pasa por
  aceptar.
- Tope de 50. Al llegar, un mensaje tranquilo, sin sugerencias de a quién quitar.

### Qué ves

- **Feed de hoy y ayer**, por hora, sin algoritmo, que termina en "Ya estás al día".
- Arriba, **la paleta del círculo**: una tira con los colores de hoy de tus amigos. Es lo primero
  que ves y basta para saber cómo va el día de tu gente.
- **Se ve sin haber compartido nada.** Exigir publicar para mirar (como BeReal) es presión.
- Tocar el nombre de un amigo abre su año, con solo los días que compartió. Los días de más de 7
  días se ven como color: su foto ya no está en el servidor.
- Nombre visible sin avatar. Tu cara no está en ningún sitio; tu color, sí.

### Qué no pasa nunca

Ni reacciones, ni comentarios, ni contadores, ni "visto por", ni avisos de publicación, ni rachas
compartidas. Nadie sabe si has mirado su tarjeta. Quitar a un amigo no le avisa.

### Sintonía

Si tu color y el de un amigo están a menos de deltaE 5 el mismo día, aparece una marca discreta que
solo veis los dos. Nadie la pulsa ni la cuenta: ocurre sola. Se calcula en el móvil con los colores
que ya trae el feed.

### Seguridad

Bloquear (deja de verte y no puede volver a invitarte) y reportar una tarjeta (se te oculta al
momento y el autor de la app recibe un correo para actuar en menos de 24 horas). Están en un menú
discreto. No son reacciones: Apple y Google los exigen y protegen a quien lo necesita.

---

## 6. Enganche y retención

### El recordatorio

Apagado por defecto y ofrecido tras la primera foto. Hora elegida por el usuario. Se calla si hoy ya
hay color. El texto invita a mirar ("¿de qué color es hoy?"), nunca culpa.

### La recompensa diferida

Igual que el segundo año de Purl, aquí la recompensa es la rejilla: al mes ya se ve un patrón, a los
tres meses cambia la estación. El póster del año es el final natural.

### Color de la semana (v1.2)

Cada semana, un color sugerido, el mismo para todos, sacado de una lista fija de 52 por número de
semana ISO. Si tu color del día está a menos de deltaE 15, aparece una marca en la tarjeta. Sin
puntos, sin ranking, desactivable. Es una excusa para mirar alrededor.

### Widgets

- Hoy (gratis): el color de hoy a sangre y su nombre, o una invitación neutra si aún no hay. Sin
  foto en la pantalla de inicio. En v1.2, una tira fina con los colores de hoy de tus amigos, sin
  nombres.
- Año (Pro): la rejilla.

---

## 7. Monetización

### El dato que decide el modelo

Las hermanas cuestan 0 EUR al mes por usuario. Chroma no: Amigos tiene servidor. Pero el coste está
**acotado por diseño**:

- Solo sube lo compartido.
- Las fotos se borran a los 7 días; los colores pesan bytes.
- Estimación con 1.000 activos diarios, la mitad compartiendo foto y 8 amigos de media: unos 280 MB
  de fotos vivas y unos 10 GB al mes de tráfico. Cabe en el plan Free de Supabase hasta unos
  200-500 activos, y en el Pro (25 USD al mes) con mucho margen después.

Un coste fijo y pequeño no justifica suscripción. **Decisión: pago único, como toda la familia, y lo
social gratis.**

### Qué es Pro

| Gratis para siempre | Pro en v1.0 | Pro desde v1.2 |
|---|---|---|
| Foto y color de cada día | Póster del año y sus estilos | Estadísticas del año |
| Mi año completo | Widget del año | |
| Compartir la tarjeta como imagen | | |
| Widget de hoy | | |
| Recordatorio | | |
| Exportar e importar | | |
| Todo lo de Amigos | | |

Por qué lo social no se cobra: si ver a tus amigos cuesta dinero, la app gana cuando más amigos
tienes y más miras. Es el incentivo que queremos evitar.

### Precio

**4,99 EUR**, como Quilt, sin descuento de lanzamiento, con precios regionales en las dos tiendas. El
paquete de v1.0 es fino (póster y widget), así que no puede costar como Purl. Se revisa al llegar
las estadísticas; quien compró conserva Pro.

```
4,99 EUR escaparate
/ 1,21 (IVA 21%)  = 4,12 EUR
- 15 % comisión   = 3,50 EUR netos
```

El plan Pro de Supabase se paga con unas 7 ventas al mes.

### Reglas

Las de la familia: el plan gratis es la prueba, el paywall aparece al chocar (exportar el póster,
tocar el widget del año sin Pro) o desde la fila de Ajustes, nunca al arrancar; restaurar compra en
Ajustes; sin anuncios; RevenueCat KMP con un producto no consumible; Small Business Program de Apple
desde el primer día; sección "Más apps" discreta en Ajustes.

---

## 8. Identidad y ficha

### El nombre

"Chroma" es corto, internacional y dice color en todos los idiomas de la ficha. Es también una palabra
muy usada (Razer Chroma, Chroma DB), así que la ficha lleva apellido: **Chroma: color del día**,
traducido en cada idioma. Reservas, en orden: **Chromaday** y **Hueday** (#1).

### El icono

Un cuadrado redondeado partido en franjas de color, como la tira del año. Lo genera un script en
`tools/`, igual que en las hermanas (#24).

### ASO

Palabras que la gente busca: diario de fotos, foto diaria, paleta de colores, color del día, año en
píxeles, diario visual. Las capturas se hacen con un año de demostración: una rejilla llena vende la
app sola.

---

## 9. Cumplimiento de tienda

| Requisito | Dónde | Cómo lo cumplimos |
|---|---|---|
| Permiso de cámara con motivo | iOS `NSCameraUsageDescription` | Texto en los cinco idiomas. La galería usa el selector del sistema, sin permiso de fotos |
| Contenido generado por usuarios | Apple 1.2, política de Play | Términos con tolerancia cero, reportar, bloquear y actuar en 24 horas (#36) |
| Borrar la cuenta | Apple 5.1.1(v), formulario de Play | Desde Ajustes y desde una web (#37) |
| Sign in with Apple | Apple 4.8 | Se ofrece junto a Google (#29) |
| Declaración de datos | App Privacy, Data Safety, `PrivacyInfo.xcprivacy` | v1.0: nada sale salvo RevenueCat. v1.1: fotos, colores, nombre e identificador de lo que compartes (#25, #38) |
| Edad | Cuestionarios de las dos tiendas | v1.1 declara interacción entre usuarios. Amigos exige 16 años declarados |

---

## 10. Arquitectura prevista

El detalle irá en `docs/tecnico.md`. Aquí solo lo que hace falta para entender las decisiones.

### Árbol de `shared/src/commonMain/kotlin/com/baltajmn/color`

```
App.kt                 enum Screen { Today, Year, Friends, Settings }
model/Entry.kt         ChromaEntry, Journal, Share
model/DayClock.kt      día lógico con corte a las 03:00
color/Lab.kt           sRGB <-> CIELAB, deltaE CIE76
color/Extract.kt       k-means y selección de candidatos
color/Names.kt         tabla de nombres y el más cercano
data/Store.kt          entries.json atómico (expect/actual para rutas)
data/Photos.kt         guardar, reducir, borrar (expect/actual)
data/Backup.kt         zip de exportar e importar
social/                cliente de Supabase, cola de subida, feed (v1.1)
ui/                    Today, Year, Friends, Settings, Card, Poster, Pro
widget/                WidgetState y widget.json
billing/               RevenueCat
Strings.kt             cinco idiomas por firma de función
```

### `entries.json`

```json
{
  "version": 1,
  "entries": {
    "2026-09-22": {
      "color": "#3A6EA5",
      "swatches": ["#3A6EA5", "#D9C7A7", "#2E2A24", "#8FA37A", "#C24E3A"],
      "name": "blue_storm",
      "word": "lluvia",
      "photo": "p_20260922_7f3a.jpg",
      "share": "private"
    }
  }
}
```

- La clave es la fecha **local** del día lógico, nunca UTC.
- `name` es la clave de la tabla de nombres, no el texto: si cambias de idioma, cambian todos.
- `share` es `private`, `color` o `photo`.
- Se escribe de forma atómica con `entries.bak.json`, en almacenamiento privado, nunca en el App
  Group. Un fichero ilegible va a cuarentena. Importar fusiona y nunca borra.
- **El móvil manda.** El servidor guarda una copia de lo compartido; si discrepan, gana el móvil y
  se vuelve a subir.

### Extracción de color

1. La plataforma decodifica la foto reducida a 64x64 y entrega los píxeles. Lo demás es común.
2. Conversión a CIELAB.
3. K-means con k = 8, semilla fija e iteraciones acotadas: misma foto, mismos colores.
4. Se fusionan los grupos a menos de deltaE 10.
5. Se eligen hasta 5: los que más foto ocupan, con dos reglas. Siempre entra **el más saturado**, para
   que una foto de interior beige no ofrezca cinco beiges. Y ninguno puede estar a menos de deltaE 10
   de otro ya elegido.
6. Una foto casi monocroma ofrece menos de 5. No se inventan colores.

### Nombres de color

Una tabla de unos 100 colores, cada uno con su clave, su color de referencia y su nombre en los cinco
idiomas. Se asigna el más cercano. La tabla vive solo en `Names.kt`: dos copias acaban divergiendo.

### Servidor (v1.1)

Supabase en Frankfurt. La seguridad está en las reglas RLS, no en la app.

```
profiles        (id, display_name, invite_code, created_at)
friendships     (a, b, status, requested_by, created_at)   a < b, status pending | accepted
shared_entries  (author, day, color, name, word, photo_path, updated_at)   clave (author, day)
blocks          (blocker, blocked)
reports         (id, reporter, author, day, created_at)
```

- Una fila de `shared_entries` la leen su autor y sus amigos aceptados, salvo que haya un bloqueo en
  cualquier sentido.
- Un trigger aplica el tope de 50 al aceptar.
- El bucket de fotos es privado y usa la misma regla. Las fotos se sirven con URL firmadas de vida
  corta.
- Una Edge Function diaria borra las fotos con más de 7 días y pone `photo_path` a null.
- Otra Edge Function avisa por correo (Resend) de cada reporte.
- Borrar la cuenta borra perfil, amistades, entradas compartidas y fotos. El diario local no se toca.
- Las migraciones viven en el repositorio.

### La subida

Local primero. Guardar un día compartido lo mete en una cola persistente que se vacía cuando hay red.
Cambiar el color o la foto durante el día actualiza la fila; pasar a privado la borra del servidor.
La foto se recodifica antes de subir, y con eso se van el GPS y el resto de metadatos.

### Tests mínimos

- Día lógico: corte a las 03:00, medianoche y cambio de huso.
- Extracción: determinismo, imagen sintética de bandas conocidas, foto monocroma, siempre entra el
  más saturado.
- Nombres: cada color de la tabla se devuelve a sí mismo.
- Tarjeta: contraste AA en una barrida del espacio de color.
- Almacén: escritura atómica, cuarentena, fusión al importar.
- Palabra: tope de 24 con emoji.
- Servidor: sin amistad no se lee nada; un bloqueo corta la lectura al momento; el tope de 50.
- Sintonía: umbral en el borde.

---

## 11. Plan de ataque

El orden de las issues:

**v1.0**
1. Lo del autor, en paralelo con todo: nombre (#1), precio (#3), cuentas (#4).
2. Documentos: #2.
3. Base: andamiaje (#5), modelo (#6), almacén (#7).
4. El corazón: extracción (#8) y nombres (#9). Se prueba con fotos reales antes de pintar ninguna
   pantalla: si los colores no convencen, no hay app.
5. Interfaz: tema (#14), textos (#15), tarjeta (#12), captura (#10), Hoy (#11), Mi año (#13),
   Ajustes (#16).
6. Alrededor: recordatorio (#17), copia (#18), compartir (#19), widget de hoy (#21).
7. Pago: RevenueCat (#23), póster (#20), widget del año (#22).
8. Tienda: icono (#24), privacidad (#25), ficha (#26), beta y revisión (#27).

**v1.1**
Servidor (#28), esquema (#30), cuentas (#29), subida (#31), invitaciones (#32), feed (#33), solo color
(#35), mosaico de amigo (#34), sintonía (#39), seguridad (#36), borrar cuenta (#37), formularios (#38).

**v1.2**
Color de la semana (#40), estadísticas (#41), paleta de amigos en el widget (#42), biometría (#43).

---

## 12. Riesgos abiertos

1. **El nombre.** "Chroma" puede estar tomado en alguna tienda o registro. Mitigación: reservas
   decididas (#1).
2. **Colores aburridos.** Las fotos de interior tiran a beige y gris. Mitigación: el más saturado
   siempre entra entre los candidatos. Se valida con un lote de fotos reales antes de construir
   pantallas.
3. **Arranque en frío de lo social.** Un feed vacío no engancha. Mitigación: la app es completa sin
   amigos, y el enlace de invitación funciona aunque el otro no tenga la app.
4. **Coste si crece.** Acotado por los 7 días de las fotos y la subida a 720 px. Si aun así se
   dispara, la perilla es bajar la retención o el tamaño, nunca cobrar por lo social.
5. **Revisión de Apple con contenido de usuarios.** Si falta reportar, bloquear o los términos, se
   rechaza. Por eso #36 y #38 son v1.1 entera y no un añadido posterior.
6. **Paquete Pro fino en v1.0.** Póster y widget pueden vender poco. Mitigación: precio bajo, y las
   estadísticas en v1.2.
7. **Fecha de las fotos de galería.** Algunas vías de importar borran el EXIF. Por eso una foto sin
   fecha se acepta.
