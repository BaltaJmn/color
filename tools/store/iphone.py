#!/usr/bin/env python3
"""The App Store screenshots of the 6.9" iPhone, without a Mac: the shared Compose UI drawn on the
JVM by Robolectric (StoreScreenshots.kt), then framed like the Play ones.

Usage: python3 tools/store/iphone.py <en-US|es-ES> [...]
For each language: writes the demo year (tools/demo/generar.py) and the sunset photo
(tools/demo/atardecer.py), draws the six raw scenes at 1320x2580 (the 440x860 pt safe area at 3x),
puts back the status bar and home indicator margins (62 and 34 pt) so they are 1320x2868 like a
simulator capture, and frames them with capturas.py into store/screenshots/iphone/<idioma>/.
Needs Pillow and rsvg-convert. Detail: store/capturas.md.
"""
import pathlib
import shutil
import subprocess
import sys

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parents[2]
DEMO = ROOT / "tools" / "demo"
DATA = DEMO / "salida"
TOP, BOTTOM = 186, 102
SCENES = ["01_hoy", "02_ano", "03_tira", "04_tarjeta", "05_widgets", "06_poster"]


def run(*cmd):
    subprocess.run(cmd, check=True, cwd=ROOT)


def pad(raw, out):
    img = Image.open(raw).convert("RGB")
    full = Image.new("RGB", (img.width, img.height + TOP + BOTTOM), img.getpixel((img.width // 2, 2)))
    full.paste(img, (0, TOP))
    full.save(out)


def main():
    languages = sys.argv[1:]
    if not languages or any(l not in ("en-US", "es-ES") for l in languages):
        sys.exit(__doc__)
    for lang in languages:
        if DATA.exists():
            shutil.rmtree(DATA)
        run(sys.executable, str(DEMO / "generar.py"), "--idioma", lang)
        run(sys.executable, str(DEMO / "atardecer.py"))
        run("./gradlew", ":shared:testAndroidHostTest", "-Pcapturas=%s" % DATA, "-Pcapturas.idioma=%s" % lang[:2], "--console=plain", "-q")
        raw = DATA / "crudas"
        raw.mkdir()
        for scene in SCENES:
            pad(DATA / (scene + ".png"), raw / (scene + ".png"))
        run(sys.executable, str(ROOT / "tools" / "store" / "capturas.py"), str(raw), lang, "iphone")


if __name__ == "__main__":
    main()
