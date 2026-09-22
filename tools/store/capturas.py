#!/usr/bin/env python3
"""Monta las capturas crudas del emulador y del simulador en las que piden las tiendas.

El marco es el de Purl: fondo claro, titular arriba y el telefono con las esquinas redondeadas
debajo. Play rechaza una captura cuyo lado largo pase del doble del corto, asi que la suya va en
1200x2100; las de Apple conservan el tamano del dispositivo, que es lo que exige App Store Connect, y
el mismo marco se escala.

Uso: python3 tools/store/capturas.py <directorio-de-capturas-crudas> <idioma> <destino>
Espera dentro 01_hoy.png a 06_poster.png. Detalle: store/capturas.md.
"""
import base64
import pathlib
import subprocess
import sys

TINTA = "#1C1B1A"
PAPEL = "#F6F4F1"

# Los colores del icono, aclarados hasta que el titular se lea encima; el ultimo, el papel.
FONDOS = ["#FBE9E4", "#FCF4E6", "#E8F2EC", "#E7EFF7", "#E5E9F0", "#F6F4F1"]

CAPTURAS = ["01_hoy", "02_ano", "03_tira", "04_tarjeta", "05_widgets", "06_poster"]

TITULARES = {
    "en-US": [
        ["One photo a day.", "One color you choose."],
        ["Your year,", "painted day by day"],
        ["The whole year", "in one gradient"],
        ["Share the card,", "or just the color"],
        ["On your home screen,", "never your photo"],
        ["Your year as a poster.", "One payment, no subscription."],
    ],
    "es-ES": [
        ["Una foto al día.", "Un color que eliges tú."],
        ["Tu año,", "pintado día a día"],
        ["El año entero", "en un degradado"],
        ["Comparte la tarjeta,", "o solo el color"],
        ["En tu pantalla de inicio,", "sin tu foto"],
        ["Tu año en un póster.", "Un pago, sin suscripción."],
    ],
}

# Por destino: el tamano final, el de la captura cruda, y lo que se le quita arriba (barra de
# estado) y abajo (barra de gestos o indicador de inicio). Ninguna de las dos dice nada de la app.
DESTINOS = {
    "play": {"final": (1200, 2100), "crudo": (1080, 2400), "recorte": (90, 60)},
    "iphone": {"final": (1320, 2868), "crudo": (1320, 2868), "recorte": (170, 70)},
    "ipad": {"final": (2064, 2752), "crudo": (2064, 2752), "recorte": (60, 40)},
}

# La ley del marco, en la proporcion de la lamina de Play, que es donde se ajusto a ojo.
BASE_W, BASE_H = 1200, 2100


def incrustar(png):
    """rsvg no abre ficheros fuera del directorio del SVG, asi que la captura viaja dentro."""
    return "data:image/png;base64," + base64.b64encode(png.read_bytes()).decode("ascii")


def marco(png, lineas, fondo, salida, destino):
    ancho, alto = destino["final"]
    crudo_w, crudo_h = destino["crudo"]
    arriba, abajo = destino["recorte"]
    k = alto / BASE_H

    alto_util = crudo_h - arriba - abajo
    alto_movil = round(1650 * k)
    ancho_movil = round(crudo_w * alto_movil / alto_util)
    x = (ancho - ancho_movil) // 2
    y = alto - alto_movil - round(90 * k)
    escala = alto_movil / alto_util

    texto = "".join(
        '<text x="%d" y="%d" font-family="Helvetica Neue, Helvetica, Arial, sans-serif" '
        'font-size="%d" font-weight="600" fill="%s">%s</text>'
        % (round(96 * ancho / BASE_W), round((170 + i * 82) * k), round(62 * k), TINTA,
           linea.replace("&", "&amp;").replace("<", "&lt;"))
        for i, linea in enumerate(lineas)
    )
    svg = """<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
  width="{W}" height="{H}" viewBox="0 0 {W} {H}">
  <rect width="{W}" height="{H}" fill="{fondo}"/>
  {texto}
  <defs>
    <clipPath id="r"><rect x="{x}" y="{y}" width="{aw}" height="{ah}" rx="{rx}" ry="{rx}"/></clipPath>
  </defs>
  <rect x="{x}" y="{y}" width="{aw}" height="{ah}" rx="{rx}" ry="{rx}" fill="{papel}"/>
  <g clip-path="url(#r)">
    <image xlink:href="{png}" x="{ix}" y="{iy}" width="{iw}" height="{ih}"/>
  </g>
</svg>""".format(
        W=ancho, H=alto, fondo=fondo, papel=PAPEL, texto=texto, png=incrustar(png),
        x=x, y=y, aw=ancho_movil, ah=alto_movil, rx=round(46 * k),
        ix=x, iy=round(y - arriba * escala),
        iw=ancho_movil, ih=round(crudo_h * escala),
    )
    tmp = salida.with_suffix(".svg")
    tmp.write_text(svg, encoding="utf-8")
    subprocess.run(["rsvg-convert", "-w", str(ancho), "-h", str(alto), str(tmp), "-o", str(salida)],
                   check=True)
    tmp.unlink()


def main():
    if len(sys.argv) != 4 or sys.argv[3] not in DESTINOS:
        sys.exit(__doc__)
    crudas = pathlib.Path(sys.argv[1])
    idioma, nombre_destino = sys.argv[2], sys.argv[3]
    destino = DESTINOS[nombre_destino]
    salida = pathlib.Path(__file__).resolve().parents[2] / "store" / "screenshots" / nombre_destino / idioma
    salida.mkdir(parents=True, exist_ok=True)
    for i, nombre in enumerate(CAPTURAS):
        final = salida / ("%02d.png" % (i + 1))
        marco(crudas / (nombre + ".png"), TITULARES[idioma][i], FONDOS[i], final, destino)
        print(final)


if __name__ == "__main__":
    main()
