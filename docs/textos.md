# Textos de Chroma

**Fuente única de los textos: `shared/src/commonMain/kotlin/com/baltajmn/color/i18n/Strings.kt`.** En
Purl los textos se escribían aquí y se copiaban al código; en Chroma se decidió no duplicarlos, porque
dos copias acaban divergiendo. Este documento fija el tono, el vocabulario y los textos que no viven
en `Strings.kt`.

Trece idiomas: en, es, pt (de Brasil), de, fr, it, nl, pl, ru, tr, id, ja, ko, los mismos que las fichas
de Quilt. Cualquier otro idioma cae a inglés. Android da el indonesio como `in`, su código antiguo:
`normalizeLanguage` lo lee como `id`, y sus recursos van en `values-in`.

---

## 1. Tono

- **Mirar, no rendir.** La app invita a fijarse en el día, nunca exige. Nada de "no rompas la
  racha", "llevas X días" ni "tus amigos te esperan".
- **Corto.** Una frase donde cabe una frase. Sin signos de exclamación salvo en un saludo.
- **Tuteo** en español, portugués, alemán (`du`) y francés (`tu`), como las hermanas, y también en
  italiano (`tu`), neerlandés (`je`), polaco (`ty`), ruso (`ты`), turco (`sen`) e indonesio (`kamu`).
  Japonés en です・ます y coreano en 해요체: el registro amable de cualquier app de allí, sin
  honoríficos.
- **Fechas y números** a mano, en `Strings.kt`: japonés y coreano ponen el año delante y el día de la
  semana detrás (`2026年9月22日 火曜日`), el turco el día de la semana detrás, y polaco y ruso
  declinan el mes dentro de una fecha (`22 września`, `с января по март`). Ruso y polaco tienen tres
  formas de plural (`slavic`). Las mayúsculas pasan por `S.caps`, porque la i del turco lleva punto.
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

| Concepto | it | nl | pl | ru | tr | id | ja | ko |
|---|---|---|---|---|---|---|---|---|
| El color del día | il colore di oggi | de kleur van vandaag | dzisiejszy kolor | цвет дня | bugünün rengi | warna hari ini | 今日の色 | 오늘의 색 |
| La tarjeta | card | kaart | karta | карточка | kart | kartu | カード | 카드 |
| Mi año | Il mio anno | Mijn jaar | Mój rok | Мой год | Yılım | Tahunku | わたしの1年 | 나의 한 해 |
| Amigos | Amici | Vrienden | Znajomi | Друзья | Arkadaşlar | Teman | 友だち | 친구 |
| Solo el color | solo il colore | alleen de kleur | tylko kolor | только цвет | yalnızca renk | hanya warna | 色だけ | 색만 |
| Privado | privato | privé | prywatne | только для меня | gizli | pribadi | 非公開 | 비공개 |
| Sintonía | in sintonia | in harmonie | zgrani | в унисон | uyum içinde | sehati | おそろい | 닮은 색 |
| Color de la semana | colore della settimana | kleur van de week | kolor tygodnia | цвет недели | haftanın rengi | warna minggu ini | 今週の色 | 이번 주의 색 |
| Póster | poster | poster | plakat | постер | poster | poster | ポスター | 포스터 |
| Copia | copia | back-up | kopia zapasowa | резервная копия | yedek | cadangan | バックアップ | 백업 |

## 3. Nombres de color

Viven en `color/Names.kt`, con su color de referencia. Criterio:

- Dos palabras como mucho: un tono y un matiz ("azul tormenta", "verde salvia", "arena").
- Evocadores pero reconocibles: que alguien que no ve la pantalla entienda el color.
- Cada idioma elige su nombre natural, no una traducción literal. "Terracota" puede ser `terracotta`,
  `terracota`, `terracota`, `Terrakotta`, `terre cuite`.
- En minúscula salvo el alemán, que pone mayúscula a los sustantivos. La interfaz capitaliza la
  primera letra al pintar.
- Revisión nativa de cada idioma antes de publicar (#9).
- Japonés y coreano no tienen mayúsculas: sus nombres son los de una carta de colores de allí
  (`群青`, `먹색`), en kanji o en katakana, y no una transliteración del inglés cuando hay uno propio.
- Los ocho idiomas que llegaron en octubre de 2026 (it, nl, pl, ru, tr, id, ja, ko) no han pasado aún
  esa revisión nativa.

## 4. Textos del sistema

Viven en sus ficheros de plataforma y se escriben en los trece idiomas.

| Clave | Dónde | es |
|---|---|---|
| `NSCameraUsageDescription` | `iosApp/iosApp/<lang>.lproj/InfoPlist.strings` | Para hacer la foto de la que sale el color de tu día. |
| `CFBundleDisplayName` | igual | Chroma |
| `NSPhotoLibraryAddUsageDescription` | igual | Para guardar tus tarjetas y pósteres en tus fotos. |
| Nombre y descripción de los widgets | `iosApp/ChromaWidget/<lang>.lproj/Localizable.strings`, `androidApp/src/main/res/values-<lang>/strings.xml` | Hoy: "El color de hoy". Año: "Tu año en colores". |
| Canal de notificación | `Strings.kt`, `reminderChannel` | Recordatorio diario |

## 5. Ficha de tienda

Viven en `store/listings/<idioma>/` (Play) y `store/app-store/<idioma>/` (App Store), en los trece
idiomas; `tienda/comprobar.py` de `BaltaJmn/ci` comprueba los topes. Las mismas reglas de tono, más dos:

- Lo gratis se dice con la misma fuerza que lo de pago, y el pago se dice como es: uno, una vez.
- La lista de lo que no hay (rachas, filtros, likes, anuncios, analítica) cierra siempre la ficha.
