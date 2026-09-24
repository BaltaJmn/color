#!/usr/bin/env python3
"""Rebuilds the widgets scene: the app's real widgets, cut from a home screen capture and laid on
flat grey. The emulator's Pixel launcher cannot be cleaned over adb (Calendar, clock, Google folder,
dock stay), so the capture is taken as it is and only the app's widgets are kept.

Usage:
  adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml
  python3 tools/store/widgets.py <home.png> <ui.xml> <app name> <out.png>

The widgets are the launcher host views whose content-desc is the app name, stacked top to bottom
in the order they sit on the home screen, left edges aligned and the block centered.
"""
import re
import sys

from PIL import Image, ImageDraw

GREY = (196, 196, 196)
# The Pixel launcher's widget corner, measured on the Pixel 8 emulator (420 dpi): 75 px.
RADIUS = 75
GAP = 40
TOP = 560


def bounds(xml, name):
    found = []
    for node in re.findall(r"<node [^>]*>", xml):
        if "AppWidgetHostView" in node and 'content-desc="%s"' % name in node:
            x0, y0, x1, y1 = map(int, re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', node).groups())
            found.append((x0, y0, x1, y1))
    return sorted(found, key=lambda b: (b[1], b[0]))


def rounded(card):
    s = 4  # supersampled mask, for smooth corners
    mask = Image.new("L", (card.width * s, card.height * s), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, card.width * s - 1, card.height * s - 1), RADIUS * s, fill=255)
    return mask.resize(card.size, Image.LANCZOS)


def main():
    if len(sys.argv) != 5:
        sys.exit(__doc__)
    home, xml, name, out = sys.argv[1:]
    boxes = bounds(open(xml, encoding="utf-8").read(), name)
    if not boxes:
        sys.exit("No widget of %s in %s" % (name, xml))
    img = Image.open(home).convert("RGB")
    screen = Image.new("RGB", img.size, GREY)
    cards = [img.crop(b) for b in boxes]
    left = (img.width - max(c.width for c in cards)) // 2
    y = TOP
    for card in cards:
        screen.paste(card, (left, y), rounded(card))
        y += card.height + GAP
    screen.save(out)
    print(out, len(cards), "widgets")


if __name__ == "__main__":
    main()
