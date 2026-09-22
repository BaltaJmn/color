# Capturas de las fichas

Seis escenas, las mismas en las dos tiendas, en **en-US** y **es-ES**. Portugués, alemán y francés
heredan las de en-US, que es lo que hacen Play y App Store cuando un idioma no trae las suyas. Se
sacan con el año de demostración, nunca con uno real.

Herramientas:

- `tools/demo/generar.py`: escribe el año de demostración.
- `tools/store/capturas.py`: el marco de Purl, con las seis escenas de abajo.
- `tools/store/fichas.py`: comprueba los topes de las dos fichas y sube la de Play (`--subir`).

## 1. Tamaños

| Destino | Dispositivo | Captura cruda | Imagen final |
|---|---|---|---|
| `play` | Emulador Pixel 8, API 36 | 1080x2400 | 1200x2100 PNG |
| `iphone` | Simulador iPhone 17 Pro Max | 1320x2868 | 1320x2868 PNG (6,9") |
| `ipad` | Simulador iPad Pro 13" | 2064x2752 | 2064x2752 PNG (13") |

```bash
python3 tools/store/capturas.py <carpeta-de-crudas> <idioma> <destino>
```

Espera dentro `01_hoy.png` a `06_poster.png` y deja el resultado en
`store/screenshots/<destino>/<idioma>/`.

## 2. Las seis escenas

| Fichero | Pantalla | Qué tiene que verse |
|---|---|---|
| `01_hoy` | Hoy, con color | La tarjeta de hoy con la foto en la esquina y la fila de candidatos debajo. La foto, una propia de hoy elegida desde la galería (el simulador no tiene cámara) |
| `02_ano` | Mi año, rejilla | La rejilla llena hasta hoy, con huecos sueltos |
| `03_tira` | Mi año, tira | La tira del año, del gris del invierno al verano |
| `04_tarjeta` | Compartir | La tarjeta de 1080x1350 en la vista previa con "Solo el color" a la vista |
| `05_widgets` | Pantalla de inicio | El widget de hoy (2x2) y el del año (4x2) sobre un fondo liso del sistema |
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
demo no es una compra; las claves de RevenueCat son `null` y nada lo apaga). `tools/demo/salida/`
va en `.gitignore`.

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
