# ASO de Chroma: palabras clave por idioma

Investigacion para las fichas de Google Play y App Store (`com.baltajmn.color`). Objetivo: mas
descargas desde la busqueda. Metodo: WebSearch sobre el nicho "year in pixels / mood tracker / diario
de color" en cada idioma, mirando que titulos y descripciones usan los competidores (Daylio, Year in
Pixels - Mood Tracker, Pixa, PixelDiary, Moodflow, Color My Day, Moodee...), y el 21-10-2026 los datos
de la propia App Store (abajo).

## Estado a 21-10-2026

- Play: los 13 idiomas tienen `title`, `short` y `full` con la palabra principal en la primera linea
  y 2 a 4 veces en total (la lista de "pendientes de traducir" de versiones anteriores de este fichero
  ya estaba hecha). Desde el 21-10 el `title` es `Chroma: Color Year in Pixels` en los 13.
- App Store: `name` igual que Play en los 13; `subtitle` con la palabra local de la categoria y el
  beneficio; `keywords` sin repetir ninguna palabra del nombre ni del subtitulo, con el tope de
  100 BYTES (una letra con acento son dos, una letra japonesa o coreana, tres: `comprobar.py` lo
  vigila). Lo escribe `store/app-store/<idioma>/*.txt`.

## Lo que dicen los datos de la App Store (21-10-2026)

Medido con `itunes.apple.com/search` y los hints de busqueda (`MZSearchHints`) de cada tienda.

- **"year in pixels" es una busqueda sugerida en las 13 tiendas** (es, de, fr, pt, it, nl, pl, ru, tr,
  id, ja, ko y en), siempre en ingles. Las palabras locales ("diario de color", "Farbtagebuch", "color
  diary") **no salen como sugerencia en ninguna**: lo que se busca de verdad es la frase inglesa.
- En el top 10 de "year in pixels" de EE. UU., Alemania y Japon casi todas las apps llevan la frase
  en el nombre, y salvo Pixa (1.938 valoraciones) casi ninguna pasa de 50: hay hueco.
- Chroma no aparecia en "year in pixels" en us, es, de, fr ni br (solo ponia "year,pixels" por
  separado o su traduccion), y en jp y kr, con la frase en las palabras clave pero no en el nombre,
  salia en el 53 y el 67 de unos 65 y 88 resultados. Con la frase en el nombre se espera top 10.
- "year in color" es la segunda sugerencia en todas las tiendas: `Color Year in Pixels` cubre las dos y
  encaja con el evento "Start 2027 in color".
- "mood tracker" sale como sugerencia en EE. UU.; `mood,tracker` va en las palabras clave, no como
  funcion propia (Chroma no pide elegir un animo).

Por eso el nombre es el mismo en todos los idiomas y la palabra local pasa al subtitulo (App Store) y a
la descripcion corta (Play).

Hallazgo principal: la frase en ingles **"year in pixels"** se queda sin traducir en casi todos los
mercados (de, fr, pt, it, pl, tr, ru, nl, id, ja, ko): los competidores locales la usan tal cual en su
propio titulo, incluso en tiendas no inglesas. Es el termino de categoria mas buscado, y Chroma
produce de verdad esa rejilla (Mi año), asi que se puede usar con acierto sin inventar nada. La
segunda familia de terminos es "diario"/"diary"/"journal" + "color"/"foto", que es donde Chroma se
diferencia de la ficha de un mood tracker (Chroma no pide elegir un animo de una lista: el color sale
de una foto real). Esa frase de contraste ("a diferencia de un mood tracker...") es la que se ha
metido en los `full.txt` ya reescritos de es-ES, en-US, de-DE, fr-FR y pt-BR: mete la keyword sin
prometer una funcion que no existe.

Formato de titulo de la casa: `Nombre: palabra que se busca` (30 caracteres de tope). Hasta el
21-10-2026 solo en-US llevaba "Color Year in Pixels" y el resto la palabra local ("diario de color");
con los datos de arriba pasa a ser `Chroma: Color Year in Pixels` en los 13 idiomas de las dos tiendas, y
la palabra local vive en el subtitulo de App Store y en la primera linea de la ficha de Play.

Las listas de abajo son la investigacion de partida de cada idioma: la keyword principal y 5-8 palabras clave objetivo. No son traducciones literales del es-ES: son las palabras que de verdad se buscan en cada
tienda, segun la competencia local encontrada.

## es-ES

- Keywords: diario de color, year in pixels, diario de fotos, un color al dia, rejilla del año, diario
  sin palabras, mood tracker, foto del dia

## en-US

- Keywords: color diary, year in pixels, photo diary, daily photo, mood tracker, color of the day,
  pixel grid, journal

## de-DE

- Keywords: Farbtagebuch, Year in Pixels, Fototagebuch, Stimmungstagebuch, Tag als Farbe, Jahr in
  Pixeln, Pixel-Raster, Tagebuch ohne Worte

## fr-FR

- Keywords: journal de couleurs, year in pixels, journal photo, suivi d'humeur, annee en pixels,
  carnet sans mots, couleur du jour, journal intime

## pt-BR

- Keywords: diario de cores, year in pixels, ano em pixels, diario de fotos, rastreador de humor, cor
  do dia, diario sem palavras, diario intimo

## it-IT

- Keywords: diario dei colori, year in pixels, diario fotografico, anno in pixel, diario dell'umore,
  traccia umore, un colore al giorno, diario senza parole

## ja-JP

- Keywords: カラー日記, year in pixels, 一年をピクセルで, 写真日記, 気分日記, 今日の色, 言葉のいらない日記

## ko-KR

- Keywords: 컬러 일기, year in pixels, 한 해를 픽셀로, 사진 일기, 기분 일기, 오늘의 색, 말 없는 일기

## pl-PL

- Keywords: dziennik kolorow, year in pixels, rok w pikselach, dziennik zdjec, dziennik nastroju,
  kolor dnia, dziennik bez slow

## tr-TR

- Keywords: renk gunlugu, year in pixels, piksellerle yil, fotograf gunlugu, ruh hali gunlugu, gunun
  rengi, sozsuz gunluk

## id

- Keywords: buku harian warna, year in pixels, jurnal foto harian, jurnal suasana hati, warna hari
  ini, tahun dalam piksel, jurnal tanpa kata

## ru-RU

- Keywords: cvetnoy dnevnik (цветной дневник), year in pixels, god v pikselyah (год в пикселях),
  fotodnevnik (фотодневник), dnevnik nastroeniya (дневник настроения), cvet dnya (цвет дня)

## nl-NL

- Keywords: kleurendagboek, year in pixels, jaar in pixels, fotodagboek, stemmingsdagboek, kleur van
  de dag, dagboek zonder woorden

## Notas para quien traduzca despues

- Los topes de Play son title 30, short 80, full 4000 y los de App Store name 30, subtitle 30,
  keywords 100 BYTES, promo 170 (`tienda/comprobar.py` de `BaltaJmn/ci`).
- No usar "mood tracker" ni sus traducciones como funcion propia: es solo el termino de contraste
  ("a diferencia de un mood tracker...") que ya usan los `full.txt` reescritos. Chroma no pide elegir
  un animo de una lista.
- Repetir la keyword principal (el "diario de color" de cada idioma) 3-5 veces en el `full.txt`, la
  primera vez en la primera linea junto al beneficio.
- Mantener "year in pixels" sin traducir: es el termino que usa la competencia local en casi todos los
  mercados investigados.
