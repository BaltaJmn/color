#!/usr/bin/env python3
"""The store screenshots without a phone: the shared Compose UI drawn on the JVM by Robolectric
(StoreScreenshots.kt), then framed with capturas.py.

Usage: python3 tools/store/escenas.py <en-US|es-ES> [...]
For each language: writes the demo year (tools/demo/generar.py) and the sunset photo
(tools/demo/atardecer.py) and draws the scenes twice:
- iPhone 6.9": six scenes at 1320x2580 (the 440x860 pt safe area at 3x), with the status bar and
  home indicator margins (62 and 34 pt) put back so they are 1320x2868 like a simulator capture,
  into store/screenshots/iphone/<idioma>/.
- Play (Pixel 8): five scenes at 1080x2250, with the 90 and 60 px of status and gesture bar put back
  (1080x2400), into store/screenshots/play/<idioma>/. Not 05: the Android widgets are not drawn here,
  so the 05.png already there stays.
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
SCENES = ["01_hoy", "02_ano", "03_tira", "04_tarjeta", "05_widgets", "06_poster"]
# Per destination: the folder the renderer writes, the margins put back (px) and the scenes it has.
DEVICES = {
    "iphone": ("", 186, 102, SCENES),
    "play": ("play", 90, 60, [s for s in SCENES if s != "05_widgets"]),
}


def run(*cmd):
    subprocess.run(cmd, check=True, cwd=ROOT)


def pad(raw, out, top, bottom):
    img = Image.open(raw).convert("RGB")
    full = Image.new("RGB", (img.width, img.height + top + bottom), img.getpixel((img.width // 2, 2)))
    full.paste(img, (0, top))
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
        for device, (folder, top, bottom, scenes) in DEVICES.items():
            raw = DATA / ("crudas-" + device)
            raw.mkdir()
            for scene in scenes:
                pad(DATA / folder / (scene + ".png"), raw / (scene + ".png"), top, bottom)
            run(sys.executable, str(ROOT / "tools" / "store" / "capturas.py"), str(raw), lang, device)


if __name__ == "__main__":
    main()
