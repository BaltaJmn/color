#!/usr/bin/env python3
"""Regenerates every Chroma icon from one geometry. Needs rsvg-convert (brew install librsvg).

A palette seen from above: five vertical stripes from warm to cold and a narrow paper stripe, the
day that is still to be painted. On the app's own ink, so the colors are the only loud thing.
Geometry: docs/pantallas.md 11.

Run from anywhere:  python3 tools/icon.py
"""
import math
import os
import pathlib
import subprocess

S = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(S)

INK = "#1C1B1A"
PAPER = "#F6F4F1"
# Picked the way the app picks: the colors of one evening by the sea, not a spectrum.
STRIPES = ["#E07A5F", "#F2CC8F", "#81B29A", "#5B8DB8", "#3D5A80"]  # coral, sand, sea, sky, deep

CANVAS = 1024
STRIPE_W, PAPER_W = 104, 44
BLOCK_W = len(STRIPES) * STRIPE_W + PAPER_W   # 564
BLOCK_H = 620
BLOCK_R = 72
LEFT = (CANVAS - BLOCK_W) / 2
TOP = (CANVAS - BLOCK_H) / 2
# The one-color layers separate the stripes with a gap, or the silhouette would be a plain tile.
MONO_GAP = 18


def bars(mono=False):
    """Every stripe as (x, width, colour), left to right, the paper one last."""
    for i, colour in enumerate(STRIPES):
        yield LEFT + i * STRIPE_W, STRIPE_W - (MONO_GAP if mono else 0), colour
    yield LEFT + len(STRIPES) * STRIPE_W, PAPER_W, PAPER


def rounded_rect(x, y, w, h, r):
    return (
        f"M{x+r:.2f},{y:.2f} H{x+w-r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x+w:.2f},{y+r:.2f} "
        f"V{y+h-r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x+w-r:.2f},{y+h:.2f} "
        f"H{x+r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x:.2f},{y+h-r:.2f} "
        f"V{y+r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x+r:.2f},{y:.2f} Z"
    )


def svg(size, shape, scale=1.0):
    if shape == "circle":
        plate = f'<circle cx="{CANVAS/2}" cy="{CANVAS/2}" r="{CANVAS/2}" fill="{INK}"/>'
    elif shape == "rounded":
        plate = f'<rect width="{CANVAS}" height="{CANVAS}" rx="{CANVAS*0.22}" fill="{INK}"/>'
    else:
        plate = f'<rect width="{CANVAS}" height="{CANVAS}" fill="{INK}"/>'
    # One pixel of overlap per stripe, so the antialiased edges never show the ink between them.
    stripes = "".join(
        f'<rect x="{x}" y="{TOP}" width="{w + 1}" height="{BLOCK_H}" fill="{c}"/>' for x, w, c in bars()
    )
    inner = f'<g clip-path="url(#block)">{stripes}</g>'
    if scale != 1.0:
        m = CANVAS / 2
        inner = f'<g transform="translate({m} {m}) scale({scale}) translate({-m} {-m})">{inner}</g>'
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {CANVAS} {CANVAS}">'
        f'<defs><clipPath id="block"><path d="{rounded_rect(LEFT, TOP, BLOCK_W, BLOCK_H, BLOCK_R)}"/></clipPath></defs>'
        f"{plate}{inner}</svg>"
    )


def png(svg_text, out, size):
    src = f"{S}/_tmp.svg"
    pathlib.Path(src).write_text(svg_text)
    pathlib.Path(out).parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["rsvg-convert", "-w", str(size), "-h", str(size), src, "-o", out], check=True)


def vector(size_dp, viewport, block, stripes, scale=1.0):
    """An Android vector: the stripes clipped to the rounded block, scaled around the centre."""
    m = viewport / 2
    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        f'    android:width="{size_dp}dp" android:height="{size_dp}dp"',
        f'    android:viewportWidth="{viewport}" android:viewportHeight="{viewport}">',
        f'    <group android:pivotX="{m}" android:pivotY="{m}" android:scaleX="{scale:.4f}" android:scaleY="{scale:.4f}">',
        f'        <clip-path android:pathData="{block}"/>',
    ]
    for d, colour in stripes:
        lines.append(f'        <path android:fillColor="{colour}" android:pathData="{d}"/>')
    lines += ["    </group>", "</vector>"]
    return "\n".join(lines) + "\n"


def rect(x, y, w, h):
    return f"M{x:.2f},{y:.2f}h{w:.2f}v{h:.2f}h{-w:.2f}z"


# --- iOS: full bleed, the system applies its own mask ---
png(svg(CANVAS, "square"), f"{ROOT}/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png", 1024)

# --- Android legacy launcher icons (API 24 and 25 have no adaptive icons) ---
res = f"{ROOT}/androidApp/src/main/res"
for folder, size in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]:
    png(svg(CANVAS, "rounded", scale=0.9), f"{res}/mipmap-{folder}/ic_launcher.png", size)
    png(svg(CANVAS, "circle", scale=0.82), f"{res}/mipmap-{folder}/ic_launcher_round.png", size)

# --- Android adaptive icon. The 108dp canvas keeps its content inside a 66dp circle. ---
SAFE = 66 / 108
BLOCK_SCALE = SAFE * CANVAS / math.hypot(BLOCK_W, BLOCK_H)
block = rounded_rect(LEFT, TOP, BLOCK_W, BLOCK_H, BLOCK_R)
pathlib.Path(f"{res}/drawable/ic_launcher_background.xml").write_text(
    '<?xml version="1.0" encoding="utf-8"?>\n'
    '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
    '    android:width="108dp" android:height="108dp"\n'
    '    android:viewportWidth="108" android:viewportHeight="108">\n'
    f'    <path android:fillColor="{INK}" android:pathData="M0,0h108v108h-108z"/>\n'
    "</vector>\n"
)
pathlib.Path(f"{res}/drawable-v24").mkdir(parents=True, exist_ok=True)
pathlib.Path(f"{res}/drawable-v24/ic_launcher_foreground.xml").write_text(
    vector(108, CANVAS, block, [(rect(x, TOP, w + 1, BLOCK_H), c) for x, w, c in bars()], BLOCK_SCALE)
)
# One colour: the system paints this layer itself, so the gaps are what keeps it a palette.
pathlib.Path(f"{res}/drawable/ic_launcher_monochrome.xml").write_text(
    vector(108, CANVAS, block, [(rect(x, TOP, w, BLOCK_H), "#FFFFFFFF") for x, w, _ in bars(mono=True)], BLOCK_SCALE)
)
pathlib.Path(f"{res}/mipmap-anydpi-v26").mkdir(parents=True, exist_ok=True)
for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
    pathlib.Path(f"{res}/mipmap-anydpi-v26/{name}").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@drawable/ic_launcher_background" />\n'
        '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
        '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
        "</adaptive-icon>\n"
    )

# --- Notification icon: Android tints it white, so it is the one-colour palette at 24dp ---
k = 18 / BLOCK_H   # the block 18dp tall, centred
notification = vector(
    24, 24,
    rounded_rect(12 - BLOCK_W * k / 2, 3, BLOCK_W * k, BLOCK_H * k, BLOCK_R * k),
    [(rect(12 + (x - CANVAS / 2) * k, 3, w * k, BLOCK_H * k), "#FFFFFFFF") for x, w, _ in bars(mono=True)],
)
pathlib.Path(f"{ROOT}/shared/src/androidMain/res/drawable/ic_notification.xml").write_text(notification)

pathlib.Path(f"{S}/_tmp.svg").unlink()
pathlib.Path(f"{S}/icon-master.svg").write_text(svg(CANVAS, "square"))
print("assets written")
