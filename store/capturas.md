# Capturas de las fichas

Seis escenas, las mismas en las dos tiendas y en los trece idiomas. **en-US y es-ES** salen de
dispositivo (Play, emulador; iPhone, simulador); los otros once, dibujadas con la interfaz de la
version (seccion 7), cada una con su idioma en la interfaz y en el titular. Se sacan con el año de
demostracion, nunca con uno real.

**Orden de la ficha (21-10-2026):** primero la rejilla de Mi año, con "year in pixels" en el
titular, porque es lo que busca quien llega por esa frase y lo primero que ve en los resultados;
despues Hoy, la tira, la tarjeta, los widgets y el poster. Los nombres de las crudas no cambian
(`01_hoy` a `06_poster`); el orden lo da `CAPTURAS` en `tools/store/capturas.py`.

**Una ficha, un tipo de captura.** Las seis de un dispositivo salen todas del mismo sitio, y si una
cambia se rehacen todas: Play, del emulador (sección 1); iPhone, del simulador (sección 6) o, sin
Mac, las seis dibujadas (sección 5). Nunca unas dibujadas y otras de dispositivo: la letra, la barra
y los datos no casan, y se nota (07-10-2026).

Herramientas:

- `tools/demo/generar.py`: escribe el año de demostración.
- `tools/store/capturas.py`: el marco de Purl, con las seis escenas de abajo.
- `tools/store/widgets.py`: la escena de los widgets, limpia.
- Topes y subida de las dos fichas: `listings.yml`, con `tienda/` de `BaltaJmn/ci` (`store/ci.md`).
- `tools/store/iphone.py`: las seis de iPhone sin Mac, de principio a fin (sección 5).
- `tools/demo/atardecer.py`: la foto de la escena 01.

## 1. Tamaños

| Destino | Dispositivo | Captura cruda | Imagen final |
|---|---|---|---|
| `play` | Emulador Pixel 8, API 36 | 1080x2400 | 1200x2100 PNG |
| `iphone` | Simulador iPhone 17 Pro Max (sección 6); sin Mac, `tools/store/iphone.py` (sección 5) | 1320x2868 | 1320x2868 PNG (6,9") |

Sin iPad: la v1.0 es solo iPhone (`docs/tecnico.md` 1), y App Store Connect no pide capturas de un
dispositivo que la app no declara.

```bash
python3 tools/store/capturas.py <carpeta-de-crudas> <idioma> <destino>
```

Espera dentro `01_hoy.png` a `06_poster.png` y deja el resultado en
`store/screenshots/<destino>/<idioma>/`.

## 2. Las seis escenas

| Fichero | Pantalla | Qué tiene que verse |
|---|---|---|
| `01_hoy` | Hoy, con color | La tarjeta de hoy con la foto en la esquina y la fila de candidatos debajo. La foto, elegida desde la galería (el simulador no tiene cámara) y sin personas ni sitios reconocibles: la de 2026-09 es un atardecer generado con PIL, en `tools/demo/fotos/` (fuera del repo) |
| `02_ano` | Mi año, rejilla | La rejilla llena hasta hoy, con huecos sueltos |
| `03_tira` | Mi año, tira | La tira del año, del gris del invierno al verano |
| `04_tarjeta` | Compartir | La tarjeta de 1080x1350 en la vista previa con "Incluir la foto" apagado |
| `05_widgets` | Pantalla de inicio | El widget de hoy (2x2) y el del año (4x2), recortados de la pantalla de inicio y puestos sobre gris liso con `tools/store/widgets.py` (usa el volcado de `uiautomator` para encontrarlos). El launcher del emulador no deja quitar su relleno (Calendario, reloj, carpeta de Google, dock) por adb, y los widgets son los de verdad. En iOS, sección 6 |
| `06_poster` | Póster | La rejilla del póster en la vista previa |

## 3. Titulares

Dos lineas por escena, en `TITULARES` de `tools/store/capturas.py`, para los trece idiomas. Es lo unico
que se lee en la tira de la ficha, asi que va en negrita y lo mas grande que cabe: 88 px en la lamina
de Play (1200 de ancho), menos si la linea mas larga del idioma no cabe (un solo tamaño para las seis
de un idioma, `letra()`). Cuentan el beneficio, no la funcion, y no dicen "sin suscripcion": Pro
tambien se paga al mes.

| Escena | en-US | es-ES |
|---|---|---|
| 02 (primera) | Your year in pixels, / made from your photos | Tu año en píxeles, / hecho con tus fotos |
| 01 (segunda) | One photo a day. / One color you choose. | Una foto al día. / Un color que eliges tú. |
| 03 | The whole year / in one gradient | El año entero / en un degradado |
| 04 | Share the card, / or just the color | Comparte la tarjeta, / o solo el color |
| 05 | On your home screen, / never your photo | En tu pantalla de inicio, / sin tu foto |
| 06 | Your year as a poster. / Pay once, keep it. | Tu año en un póster. / Pagas una vez, es tuyo. |

Para cambiar solo los titulares de una ficha de dispositivo, de la que no se guardo la cruda:
`python3 tools/store/capturas.py --recaptionar en-US play` (o `iphone`). Tapa el hueco de arriba, que
es de fondo liso, y escribe el titular nuevo; las seis salen en el orden de `CAPTURAS`.

## 4. El año de demostración

```bash
python3 tools/demo/generar.py --idioma es-ES [--hoy AAAA-MM-DD]
```

Del 1 de enero hasta ayer, un 86 % de días con color, por estaciones, y una palabra suelta de vez en
cuando en el idioma pedido. Hoy queda vacío: la escena 01 es hacer la foto. Deja
`tools/demo/salida/entries.json`, con Pro encendido para enseñar el póster y el widget del año (una
demo no es una compra). En un móvil o simulador con las claves de RevenueCat puestas, la tienda
contesta que no hay compra y apaga Pro al abrir: para las escenas 05 y 06 hay que quitar la clave de
la build de depuración o usar la sección 5, donde no hay tienda. `tools/demo/salida/` va en
`.gitignore`.

Cargarlo con la app cerrada:

```bash
# iOS
cp tools/demo/salida/entries.json "$(xcrun simctl get_app_container booted com.baltajmn.color data)/Library/Application Support/entries.json"
```

```bash
# Android (build de depuración)
adb push tools/demo/salida/entries.json /data/local/tmp/entries.json
adb shell run-as com.baltajmn.color cp /data/local/tmp/entries.json files/entries.json
```

## 5. iPhone sin Mac

```bash
python3 tools/store/iphone.py en-US es-ES
```

La interfaz es Compose común: la misma que pinta iOS. `StoreScreenshots.kt` (en `androidHostTest`)
la dibuja en la JVM con Robolectric a 440x860 pt a 3x, que es el área segura del iPhone 17 Pro Max,
sobre el año de demostración y la foto de `atardecer.py`, y recorre las escenas pulsando como lo haría
una persona: Mi año, Tira, Póster, y Compartir con la foto apagada. Hoy lleva la extracción de verdad
sobre esa foto y el candidato claro más saturado elegido. `iphone.py` vuelve a poner arriba y abajo los
62 y 34 pt de la barra de estado y del indicador de inicio, y enmarca con `capturas.py iphone`.

Solo corre si se pide (`-Pcapturas=<carpeta>`): `./gradlew :shared:testAndroidHostTest` lo excluye.
La primera vez Robolectric baja su Android (`android-all-instrumented`, unos 200 MB).

Lo que no es igual que en el simulador:

- La letra es Roboto, la del sistema de Android; en iOS es SF Pro. Medidas, colores y textos son
  los mismos.
- La escena 05 no es una captura: WidgetKit no corre fuera de un Mac. Son `ChromaWidget.swift` y
  `ChromaYearWidget.swift` dibujados otra vez con sus medidas (170x170 y 364x170 pt, márgenes de 16,
  las mismas letras, grises y rejilla) a partir del mismo `widget.json`. Si cambia un widget de iOS,
  cambia también aquí.

Con un Mac, mejor el simulador: sección 6.

## 6. iPhone con Mac: el simulador

Las de `store/screenshots/iphone/` salen de aquí, con la letra SF Pro y los widgets de WidgetKit de
verdad.

1. Un simulador propio, para no pisar los de otras sesiones:
   `xcrun simctl create "Chroma capturas" com.apple.CoreSimulator.SimDeviceType.iPhone-17-Pro-Max <runtime>`.
   Recién creado, `simctl addmedia` se queda colgado hasta que se abre Fotos una vez.
2. Build de depuración sin la clave `appl_` de `Billing.ios.kt`, que se devuelve en cuanto acaba
   (con ella la tienda apaga Pro y no hay póster ni widget del año), firmada ad hoc para que el App
   Group llegue a los widgets:
   `xcodebuild ... -destination 'platform=iOS Simulator,id=<udid>' CODE_SIGN_STYLE=Manual CODE_SIGN_IDENTITY=- DEVELOPMENT_TEAM= build`.
3. El año de `generar.py` cargado como en la sección 4, y la foto de `atardecer.py` con
   `xcrun simctl addmedia`: no lleva fecha, así que la app la toma por de hoy.
4. El idioma, el del simulador y no el de la app, para que los widgets cambien con ella:
   `xcrun simctl spawn <udid> defaults write -g AppleLanguages -array <idioma>`, lo mismo con
   `AppleLocale`, y reiniciar el simulador.
5. Escena 05: los dos widgets en una página vacía de la pantalla de inicio, y `widgets.py` con sus
   cajas en píxeles, 6 px por dentro del borde: la esquina de un widget de iOS mide unos 90 px y
   `widgets.py` redondea a 75, así que con la caja justa asoma el fondo. En el 17 Pro Max con iOS 27
   quedan `99,288,1221,806` (año) y `99,940,617,1458` (hoy).

La captura para la revisión de la compra (`revision-compra.png`, cruda y sin marco) sale de la misma
build con Pro apagado en `entries.json` y el botón de compra forzado con un precio a mano: sin tienda,
`Pro.kt` lo esconde. Ese cambio solo vive en esa build y no se sube nunca.

## 7. Los otros once idiomas

```bash
python3 tools/store/iphone.py de-DE fr-FR pt-BR it-IT nl-NL pl-PL ru-RU tr-TR id ja-JP ko-KR
```

Una pasada por idioma (~20 s): `generar.py` con sus palabras, el atardecer, `StoreScreenshots.kt` en
ese idioma, y el marco dos veces, `iphone` (las seis) y `play-dibujado` (cinco: la de widgets es de
iOS y en Android no se parece, asi que Play no la lleva). Cada ficha tiene un solo tipo de captura:
las de estos idiomas, todas dibujadas. En la interfaz dibujada se ve la pestaña Amigos (la 1.2);
las de en-US y es-ES, de dispositivo, son de la 1.0.
Las once dibujadas se rehicieron el 22-10-2026 con la 1.2.3: el póster (06) enseña el selector de cuatro
estilos en dos filas y la tarjeta (04) y el póster llevan la firma "Chroma: Color Year in Pixels". Las de
dispositivo (en-US y es-ES, iPhone y Play) siguen con la tarjeta y el póster de antes; rehacerlas pide el
set de seis entero en el simulador (sección 6) y en el emulador (sección 1), por la regla de arriba.

`StoreScreenshots.kt` pone en `Social` un cliente de Supabase sobre memoria (`quietSocial`): el real
no se puede construir en una JVM sin ajustes de Android, y Hoy pregunta si hay cuenta. Sin eso el
test falla desde que entro Amigos.

## 8. Eventos y escaparate

`store/events/<nombre>/` lleva `event.json` (textos en los 13 idiomas), `card.png` (1920x1080) y
`details.png` (2160x3840, vertical: Apple pide 9:16, no 16:9). `tools/store/evento.py` dibuja las dos
imagenes: un año de cuadros con el 2027 en grande y sin una sola frase, porque Apple pone el nombre
y la insignia encima de la tarjeta. Un evento dura 31 dias como maximo, asi que 15-12 al 31-01 son dos:
"Start 2027 in color" (15-12 al 14-01, enlace `com.baltajmn.color://year`) e "It's not too late for
2027" (15-01 al 31-01, `://today`). Apple exige un enlace profundo para enviarlos.

Las imagenes de evento (y las capturas de una prueba de Product Page Optimization) las rechaza
la API de App Store Connect siempre, con `INVALID_ASSET_FILE_FORMAT`, incluso una captura que ya vale
en la ficha. La web las acepta: se suben desde ella (Claude in Chrome, `file_upload`), y lo ya subido
se coloca por API en el resto de idiomas con `ci/tienda/biblioteca.py colocar_evento`. La prueba
de PPO admite capturas de 6,1" o 6,3" (1206x2622), no las de 6,9" de la ficha.

La nominacion de featuring es `tools/store/nominacion.py` sobre `store/events/nominacion.json`. Apple solo
admite en ella eventos ya aprobados: se envia sin ellos y se repite con `--enviar` al aprobarse.
