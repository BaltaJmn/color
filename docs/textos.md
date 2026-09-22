# Textos de Chroma

**Fuente única de los textos: `shared/src/commonMain/kotlin/com/baltajmn/color/i18n/Strings.kt`.** En
Purl los textos se escribían aquí y se copiaban al código; en Chroma se decidió no duplicarlos, porque
dos copias acaban divergiendo. Este documento fija el tono, el vocabulario y los textos que no viven
en `Strings.kt`.

Cinco idiomas: en, es, pt (de Brasil), de, fr. Cualquier otro idioma cae a inglés.

---

## 1. Tono

- **Mirar, no rendir.** La app invita a fijarse en el día, nunca exige. Nada de "no rompas la
  racha", "llevas X días" ni "tus amigos te esperan".
- **Corto.** Una frase donde cabe una frase. Sin signos de exclamación salvo en un saludo.
- **Tuteo** en español, portugués, alemán (`du`) y francés (`tu`), como las hermanas.
- **Sin métricas en la voz.** Ningún texto cuenta días, amigos ni vistas.
- Sin em dash ni emoji.

## 2. Vocabulario fijo

| Concepto | en | es | pt | de | fr |
|---|---|---|---|---|---|
| El color del día | today's color | el color de hoy | a cor de hoje | die Farbe von heute | la couleur du jour |
| La tarjeta | card | tarjeta | cartão | Karte | carte |
| Mi año | My year | Mi año | Meu ano | Mein Jahr | Mon année |
| Amigos | Friends | Amigos | Amigos | Freunde | Amis |
| Compartir con amigos | share with friends | compartir con amigos | compartilhar com amigos | mit Freunden teilen | partager avec tes amis |
| Solo el color | color only | solo el color | só a cor | nur die Farbe | la couleur seule |
| Privado | private | privado | privado | privat | privé |
| Sintonía | in tune | en sintonía | em sintonia | im Einklang | en accord |
| Color de la semana | color of the week | color de la semana | cor da semana | Farbe der Woche | couleur de la semaine |
| Póster | poster | póster | pôster | Poster | affiche |

## 3. Nombres de color

Viven en `color/Names.kt`, con su color de referencia. Criterio:

- Dos palabras como mucho: un tono y un matiz ("azul tormenta", "verde salvia", "arena").
- Evocadores pero reconocibles: que alguien que no ve la pantalla entienda el color.
- Cada idioma elige su nombre natural, no una traducción literal. "Terracota" puede ser `terracotta`,
  `terracota`, `terracota`, `Terrakotta`, `terre cuite`.
- En minúscula salvo el alemán, que pone mayúscula a los sustantivos. La interfaz capitaliza la
  primera letra al pintar.
- Revisión nativa de cada idioma antes de publicar (#9).

## 4. Textos del sistema

Viven en sus ficheros de plataforma y se escriben en los cinco idiomas.

| Clave | Dónde | es |
|---|---|---|
| `NSCameraUsageDescription` | `iosApp/iosApp/<lang>.lproj/InfoPlist.strings` | Para hacer la foto de la que sale el color de tu día. |
| `CFBundleDisplayName` | igual | Chroma |
| `NSPhotoLibraryAddUsageDescription` | igual | Para guardar tus tarjetas y pósteres en tus fotos. |
| Nombre y descripción de los widgets | `iosApp/ChromaWidget/<lang>.lproj/Localizable.strings`, `androidApp/src/main/res/values-<lang>/strings.xml` | Hoy: "El color de hoy". Año: "Tu año en colores". |
| Canal de notificación | `Strings.kt`, `reminderChannel` | Recordatorio diario |

## 5. Ficha de tienda

`store/listings/<lang>/`: título, subtítulo, descripción corta y larga. Se escriben en #26.
