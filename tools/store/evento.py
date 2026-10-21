#!/usr/bin/env python3
"""Draws the art of the In-App Events of store/events/<nombre>/: card.png (1920x1080) and details.png
(2160x3840, portrait), with no text, because App Store Connect puts the name and the badge over the card and a
sentence of ours would end up under them. The art is Mi año in the app's own look: a year of
squares, January filled with the winter colors of the demo year and the rest still to paint, with the
numerals of the year, which are the same in every language.

Usage: python3 tools/store/evento.py
Needs rsvg-convert. Detail: store/capturas.md 8.
"""
import pathlib
import random
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools" / "demo"))
import generar  # the seasons of the demo year: the colors of the screenshots

PAPEL, TINTA = "#F3F3F3", "#111111"
# Folder name: (year, days of January painted, the rest of the year as it will look or still to paint).
# The first event shows the year already painted, what a person is shown a goal of; the second is a
# year with two weeks in and the rest waiting, in the same colors but pale.
EVENTOS = {"start-2027": ("2027", 31, False), "no-es-tarde-2027": ("2027", 14, True)}


# Apple's specs: the card is 16:9 (1920x1080 up to 3840x2160) and the details page 9:16 (1080x1920 up
# to 2160x3840). Per layout: canvas, numerals (x, y, size), cell (w, h), pitch (x, y) and grid origin.
DISPOSICION = {
    "card": ((1920, 1080), (110, 440, 330), (62, 22), (70, 30), (1000, 78)),
    "details": ((1080, 1920), (80, 330, 300), (68, 25), (76, 33), (84, 520)),
}


def svg(year, pintados, pastel, vista):
    (W, H), (nx, ny, tam), (ancho, alto), (paso_x, paso_y), (x0, y0) = DISPOSICION[vista]
    hexes, rng = generar.table(), random.Random(2027)
    celdas = []
    for col in range(12):
        pool = [hexes[k] for k in generar.pool(col + 1)]
        for fila in range(31):
            # February has 28 days and four months 30: the short months leave the end empty.
            if (col, fila) in {(1, 28), (1, 29), (1, 30)} | {(m, 30) for m in (3, 5, 8, 10)}:
                continue
            x, y = x0 + col * paso_x, y0 + fila * paso_y
            color = rng.choice(pool)
            if pastel and col == 0 and fila == pintados:
                relleno = f'fill="{PAPEL}" stroke="{TINTA}" stroke-width="3"'
            else:
                pintada = not pastel or (col == 0 and fila < pintados)
                relleno = f'fill="{color}" fill-opacity="{1 if pintada else 0.33}"'
            celdas.append(f'<rect x="{x}" y="{y}" width="{ancho}" height="{alto}" rx="5" {relleno}/>')
    return f"""<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
  <rect width="{W}" height="{H}" fill="{PAPEL}"/>
  <text x="{nx}" y="{ny}" font-family="Helvetica Neue, Helvetica, Arial, sans-serif" font-size="{tam}" font-weight="200" fill="{TINTA}" letter-spacing="-6">{year}</text>
  {''.join(celdas)}
</svg>"""


def main():
    for nombre, (year, pintados, pastel) in EVENTOS.items():
        carpeta = ROOT / "store" / "events" / nombre
        carpeta.mkdir(parents=True, exist_ok=True)
        tmp = carpeta / "arte.svg"
        for vista, fichero, escala in (("card", "card.png", 1), ("details", "details.png", 2)):
            (W, H) = DISPOSICION[vista][0]
            tmp.write_text(svg(year, pintados, pastel, vista), encoding="utf-8")
            subprocess.run(["rsvg-convert", "-w", str(W * escala), "-h", str(H * escala), str(tmp), "-o", str(carpeta / fichero)], check=True)
        tmp.unlink()
        print(carpeta / "card.png")


if __name__ == "__main__":
    main()
