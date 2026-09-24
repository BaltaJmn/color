# ASO de Chroma: palabras clave por idioma

Investigacion para la ficha de Google Play (`com.baltajmn.color`). Objetivo: mas descargas desde la
busqueda de Play. Metodo: WebSearch sobre el nicho "year in pixels / mood tracker / diario de color"
en cada idioma, mirando que titulos y descripciones usan los competidores (Daylio, Year in Pixels -
Mood Tracker, Pixa, PixelDiary, Moodflow, Color My Day, Moodee...).

Hallazgo principal: la frase en ingles **"year in pixels"** se queda sin traducir en casi todos los
mercados (de, fr, pt, it, pl, tr, ru, nl, id, ja, ko): los competidores locales la usan tal cual en su
propio titulo, incluso en tiendas no inglesas. Es el termino de categoria mas buscado, y Chroma
produce de verdad esa rejilla (Mi año), asi que se puede usar con acierto sin inventar nada. La
segunda familia de terminos es "diario"/"diary"/"journal" + "color"/"foto", que es donde Chroma se
diferencia de la ficha de un mood tracker (Chroma no pide elegir un animo de una lista: el color sale
de una foto real). Esa frase de contraste ("a diferencia de un mood tracker...") es la que se ha
metido en los `full.txt` ya reescritos de es-ES, en-US, de-DE, fr-FR y pt-BR: mete la keyword sin
prometer una funcion que no existe.

Formato de titulo de la casa: `Nombre: palabra que se busca` (30 caracteres de tope). Se ha mantenido
en es, de, fr, pt porque "diario de color" y sus traducciones ya son la keyword mas fiel a lo que hace
la app; se ha cambiado en en-US a "Color Year in Pixels" porque ahi la evidencia de mayor busqueda es
mas clara (apps enteras se llaman asi, con mucha escala) y cabe sin perder la palabra "Color".

Para los 8 idiomas sin ficha todavia (it, ja, ko, pl, tr, id, ru, nl) esto es la lista de partida para
quien traduzca: la keyword principal, 5-8 palabras clave objetivo y el titulo propuesto (<=30
caracteres). No son traducciones literales del es-ES: son las palabras que de verdad se buscan en cada
tienda, segun la competencia local encontrada.

## es-ES (ficha ya reescrita)

- Keywords: diario de color, year in pixels, diario de fotos, un color al dia, rejilla del año, diario
  sin palabras, mood tracker, foto del dia
- Titulo: `Chroma: diario de color` (24)

## en-US (ficha ya reescrita)

- Keywords: color diary, year in pixels, photo diary, daily photo, mood tracker, color of the day,
  pixel grid, journal
- Titulo: `Chroma: Color Year in Pixels` (28)

## de-DE (ficha ya reescrita)

- Keywords: Farbtagebuch, Year in Pixels, Fototagebuch, Stimmungstagebuch, Tag als Farbe, Jahr in
  Pixeln, Pixel-Raster, Tagebuch ohne Worte
- Titulo: `Chroma: Farbtagebuch` (20)

## fr-FR (ficha ya reescrita)

- Keywords: journal de couleurs, year in pixels, journal photo, suivi d'humeur, annee en pixels,
  carnet sans mots, couleur du jour, journal intime
- Titulo: `Chroma : journal de couleurs` (28)

## pt-BR (ficha ya reescrita)

- Keywords: diario de cores, year in pixels, ano em pixels, diario de fotos, rastreador de humor, cor
  do dia, diario sem palavras, diario intimo
- Titulo: `Chroma: diario de cores` (23)

## it-IT (pendiente de traducir)

- Keywords: diario dei colori, year in pixels, diario fotografico, anno in pixel, diario dell'umore,
  traccia umore, un colore al giorno, diario senza parole
- Titulo propuesto: `Chroma: diario dei colori` (26)

## ja-JP (pendiente de traducir)

- Keywords: カラー日記, year in pixels, 一年をピクセルで, 写真日記, 気分日記, 今日の色, 言葉のいらない日記
- Titulo propuesto: `Chroma: カラー日記` (13)

## ko-KR (pendiente de traducir)

- Keywords: 컬러 일기, year in pixels, 한 해를 픽셀로, 사진 일기, 기분 일기, 오늘의 색, 말 없는 일기
- Titulo propuesto: `Chroma: 컬러 일기` (12)

## pl-PL (pendiente de traducir)

- Keywords: dziennik kolorow, year in pixels, rok w pikselach, dziennik zdjec, dziennik nastroju,
  kolor dnia, dziennik bez slow
- Titulo propuesto: `Chroma: dziennik kolorow` (25)

## tr-TR (pendiente de traducir)

- Keywords: renk gunlugu, year in pixels, piksellerle yil, fotograf gunlugu, ruh hali gunlugu, gunun
  rengi, sozsuz gunluk
- Titulo propuesto: `Chroma: renk gunlugu` (20)

## id (pendiente de traducir)

- Keywords: buku harian warna, year in pixels, jurnal foto harian, jurnal suasana hati, warna hari
  ini, tahun dalam piksel, jurnal tanpa kata
- Titulo propuesto: `Chroma: buku harian warna` (26)

## ru-RU (pendiente de traducir)

- Keywords: cvetnoy dnevnik (цветной дневник), year in pixels, god v pikselyah (год в пикселях),
  fotodnevnik (фотодневник), dnevnik nastroeniya (дневник настроения), cvet dnya (цвет дня)
- Titulo propuesto: `Chroma: цветной дневник` (23)

## nl-NL (pendiente de traducir)

- Keywords: kleurendagboek, year in pixels, jaar in pixels, fotodagboek, stemmingsdagboek, kleur van
  de dag, dagboek zonder woorden
- Titulo propuesto: `Chroma: kleurendagboek` (22)

## Notas para quien traduzca despues

- Los topes de Play son title 30, short 80, full 4000 (`tools/store/fichas.py`, dict `TOPES`).
- No usar "mood tracker" ni sus traducciones como funcion propia: es solo el termino de contraste
  ("a diferencia de un mood tracker...") que ya usan los `full.txt` reescritos. Chroma no pide elegir
  un animo de una lista.
- Repetir la keyword principal (el "diario de color" de cada idioma) 3-5 veces en el `full.txt`, la
  primera vez en la primera linea junto al beneficio.
- Mantener "year in pixels" sin traducir: es el termino que usa la competencia local en casi todos los
  mercados investigados.
