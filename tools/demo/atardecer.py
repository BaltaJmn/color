#!/usr/bin/env python3
"""Draws the photo of scene 01: a sunset over the sea, with no person and no place anyone could
recognise. Generated and not taken, for the same reason as the demo year (generar.py).

Usage: python3 tools/demo/atardecer.py [salida.jpg]
Default output: tools/demo/salida/photos/p-demo.jpg, the name the screenshot run expects.
Needs Pillow.
"""
import math
import pathlib
import random
import sys

from PIL import Image, ImageDraw, ImageFilter

W, H = 1080, 1350
HORIZON = 0.58
SHORE = 0.86

SKY = [(0.0, (122, 114, 150)), (0.45, (214, 150, 140)), (0.8, (242, 184, 140)), (1.0, (248, 206, 160))]
SEA_TOP, SEA_BOTTOM = (52, 92, 122), (28, 58, 88)
SAND_TOP, SAND_BOTTOM = (214, 190, 150), (196, 170, 128)


def mix(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def ramp(stops, t):
    for (t0, c0), (t1, c1) in zip(stops, stops[1:]):
        if t <= t1:
            return mix(c0, c1, (t - t0) / (t1 - t0))
    return stops[-1][1]


def main():
    out = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else \
        pathlib.Path(__file__).resolve().parent / "salida" / "photos" / "p-demo.jpg"
    rng = random.Random(2026)
    img = Image.new("RGB", (W, H))
    px = img.load()
    horizon, shore = int(H * HORIZON), int(H * SHORE)
    sun_x = W * 0.55

    for y in range(horizon):
        for x in range(W):
            t = y / horizon
            c = ramp(SKY, t)
            # The glow of a sun just under the horizon.
            d = math.hypot((x - sun_x) / W, (y - horizon) / H * 2.2)
            glow = max(0.0, 1 - d * 2.6) ** 2
            px[x, y] = mix(c, (255, 222, 170), glow * 0.8)
    for y in range(horizon, shore):
        t = (y - horizon) / (shore - horizon)
        base = mix(SEA_TOP, SEA_BOTTOM, t)
        for x in range(W):
            reflection = max(0.0, 1 - abs(x - sun_x) / (W * (0.05 + t * 0.25))) * (1 - t) * 0.55
            px[x, y] = mix(base, (240, 190, 150), reflection)
    for y in range(shore, H):
        t = (y - shore) / (H - shore)
        for x in range(W):
            px[x, y] = mix(SAND_TOP, SAND_BOTTOM, t)

    draw = ImageDraw.Draw(img)
    # Ripples: short light strokes, longer and further apart near the shore.
    for _ in range(2600):
        y = rng.uniform(horizon + 2, shore - 4)
        t = (y - horizon) / (shore - horizon)
        x = rng.uniform(0, W)
        length = 6 + t * 40
        shade = mix(SEA_TOP, (150, 170, 190), rng.uniform(0.2, 0.6))
        draw.line([(x, y), (x + length, y)], fill=shade, width=1 + int(t * 2))
    # The foam line where the sea meets the sand.
    for x in range(0, W, 3):
        y = shore + math.sin(x / 37) * 3
        draw.line([(x, y - 2), (x + 3, y)], fill=(236, 230, 218), width=3)

    img = img.filter(ImageFilter.GaussianBlur(0.6))
    out.parent.mkdir(parents=True, exist_ok=True)
    img.save(out, "JPEG", quality=88)
    print(out)


if __name__ == "__main__":
    main()
