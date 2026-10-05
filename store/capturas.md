# Capturas de las fichas

Seis escenas, las mismas en las dos tiendas, en **en-US** y **es-ES**. Los otros once idiomas
heredan las de en-US, que es lo que hacen Play y App Store cuando un idioma no trae las suyas. Se
sacan con el año de demostración, nunca con uno real.

Herramientas:

- `tools/demo/generar.py`: escribe el año de demostración.
- `tools/store/capturas.py`: el marco de Purl, con las seis escenas de abajo.
- `tools/store/widgets.py`: la escena de los widgets, limpia.
- `tools/store/fichas.py`: comprueba los topes de las dos fichas y sube la de Play (`--subir`).
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

Dos líneas por escena, en `tools/store/capturas.py`. Es lo único que se lee en la tira de la ficha.

| Escena | en-US | es-ES |
|---|---|---|
| 01 | One photo a day. / One color you choose. | Una foto al día. / Un color que eliges tú. |
| 02 | Your year, / painted day by day | Tu año, / pintado día a día |
| 03 | The whole year / in one gradient | El año entero / en un degradado |
| 04 | Share the card, / or just the color | Comparte la tarjeta, / o solo el color |
| 05 | On your home screen, / never your photo | En tu pantalla de inicio, / sin tu foto |
| 06 | Your year as a poster. / One payment, no subscription. | Tu año en un póster. / Un pago, sin suscripción. |

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
