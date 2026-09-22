#!/usr/bin/env python3
"""Writes the demo year the store screenshots are taken with. Never a real one: a real year of
someone's photos in a public listing is exactly what this app promises not to do.

Colors follow the seasons of a city in the north: greys and blues in winter, greens and pinks in
spring, sea and sand in summer, ochres and rust in autumn. Every choice is seeded by the date, so two
runs on the same day write the same year. Today is left empty on purpose: scene 01 is the capture.

Usage: python3 tools/demo/generar.py [--idioma es-ES] [--hoy AAAA-MM-DD]
Leaves tools/demo/salida/entries.json. Loading it: store/capturas.md 4.
"""
import argparse
import datetime
import json
import pathlib
import random
import re

ROOT = pathlib.Path(__file__).resolve().parents[2]
NAMES = ROOT / "shared/src/commonMain/kotlin/com/baltajmn/color/color/Names.kt"
OUT = pathlib.Path(__file__).resolve().parent / "salida"

SEASONS = {
    (1, 2): "fog silver slate smoke steel denim storm_blue charcoal pearl concrete stone sky glacier pine burgundy powder_blue",
    (3, 4): "powder_pink blush sage pistachio spring_green lemon sky cornflower lilac lavender grass butter peony mint",
    (5, 6): "grass leaf poppy sunflower sky cerulean turquoise lavender coral lemon lime aqua cream",
    (7, 8): "aqua turquoise lagoon cerulean sand ochre amber honey tangerine coral watermelon straw sky ocean",
    (9, 10): "mustard ochre caramel terracotta rust olive pumpkin copper clay camel sage moss dusk",
    (11, 12): "chestnut cinnamon coffee wine burgundy forest pine slate graphite fog cherry amber cream navy",
}

# A word now and then, as people add them: short, concrete, never a feeling to be scored.
WORDS = {
    "en-US": ["rain", "market", "beach", "coffee", "train", "garden", "snow", "concert", "picnic", "ferry"],
    "es-ES": ["lluvia", "mercado", "playa", "café", "tren", "jardín", "nieve", "concierto", "picnic", "ferry"],
    "pt-BR": ["chuva", "feira", "praia", "café", "trem", "jardim", "neve", "show", "piquenique", "balsa"],
    "de-DE": ["Regen", "Markt", "Strand", "Kaffee", "Zug", "Garten", "Schnee", "Konzert", "Picknick", "Fähre"],
    "fr-FR": ["pluie", "marché", "plage", "café", "train", "jardin", "neige", "concert", "pique-nique", "ferry"],
}


def table():
    text = NAMES.read_text()
    return dict(re.findall(r'n\("([a-z_]+)", "(#[0-9A-F]{6})"', text))


def pool(month):
    return next(keys.split() for months, keys in SEASONS.items() if month in months)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--idioma", default="en-US", choices=sorted(WORDS))
    parser.add_argument("--hoy", default=datetime.date.today().isoformat())
    args = parser.parse_args()
    today = datetime.date.fromisoformat(args.hoy)
    hexes = table()

    entries = {}
    day = datetime.date(today.year, 1, 1)
    while day < today:
        rng = random.Random(day.toordinal() * 7919)
        if rng.random() < 0.86:
            keys = pool(day.month)
            picked = rng.choice(keys)
            others = rng.sample([k for k in keys if k != picked], rng.randint(2, 4))
            swatches = [hexes[k] for k in [picked] + others]
            rng.shuffle(swatches)
            entry = {"color": hexes[picked], "swatches": swatches, "name": picked}
            if rng.random() < 0.12:
                entry["word"] = rng.choice(WORDS[args.idioma])
            entry["at"] = int(datetime.datetime.combine(day, datetime.time(18)).timestamp() * 1000)
            entries[day.isoformat()] = entry
        day += datetime.timedelta(days=1)

    settings = {
        # Pro so the poster and the year widget can be shown; a demo is not a purchase.
        "pro": True,
        "reminderOn": True,
        "reminderHour": 20,
        "reminderOffered": True,
        "backupNoticeDone": True,
        "lastBackup": (today - datetime.timedelta(days=3)).isoformat(),
    }
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / "entries.json").write_text(json.dumps({"version": 1, "entries": entries, "settings": settings}, ensure_ascii=False, indent=1))
    print(f"{len(entries)} days up to {today} in {OUT / 'entries.json'}")


if __name__ == "__main__":
    main()
