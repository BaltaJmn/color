#!/usr/bin/env python3
"""Monta las capturas crudas del emulador y del simulador en las que piden las tiendas.

El marco es el de Purl: fondo claro, titular arriba y el telefono con las esquinas redondeadas
debajo. Play rechaza una captura cuyo lado largo pase del doble del corto, asi que la suya va en
1200x2100; las de Apple conservan el tamano del dispositivo, que es lo que exige App Store Connect, y
el mismo marco se escala.

Uso: python3 tools/store/capturas.py <directorio-de-capturas-crudas> <idioma> <destino>
     python3 tools/store/capturas.py --recaptionar <idioma> <destino>
Espera dentro 01_hoy.png a 06_poster.png. Detalle: store/capturas.md. `--recaptionar` rehace solo
el titular de las seis que ya hay en store/screenshots/<destino>/<idioma>/ (las de dispositivo, de
las que no se guardo la cruda); los destinos son play, iphone y play-dibujado (Play con las crudas
de iphone.py, sin la escena de widgets, que alli son los de iOS).
"""
import base64
import pathlib
import subprocess
import sys

TINTA = "#111111"
PAPEL = "#F3F3F3"
# The app's outline token: the screens share the paper color, so only this edge tells them apart.
BORDE = "#D0D0D0"

# Neutral like the app itself: the color in the strip comes from the screenshots, never the frame.
FONDOS = [PAPEL] * 6

# Orden de la ficha: primero la rejilla, que es lo que busca quien escribe "year in pixels" (los
# primeros resultados de las tiendas son todos rejillas), y despues el color de hoy.
CAPTURAS = ["02_ano", "01_hoy", "03_tira", "04_tarjeta", "05_widgets", "06_poster"]
ORDEN_ANTIGUO = ["01_hoy", "02_ano", "03_tira", "04_tarjeta", "05_widgets", "06_poster"]

# Beneficio primero, dos lineas, sin "suscripcion": Pro tambien se paga al mes.
TITULARES = {
    "en-US": [
        ["Your year in pixels,", "made from your photos"],
        ["One photo a day.", "One color you choose."],
        ["The whole year", "in one gradient"],
        ["Share the card,", "or just the color"],
        ["On your home screen,", "never your photo"],
        ["Your year as a poster.", "Pay once, keep it."],
    ],
    "es-ES": [
        ["Tu año en píxeles,", "hecho con tus fotos"],
        ["Una foto al día.", "Un color que eliges tú."],
        ["El año entero", "en un degradado"],
        ["Comparte la tarjeta,", "o solo el color"],
        ["En tu pantalla de inicio,", "sin tu foto"],
        ["Tu año en un póster.", "Pagas una vez, es tuyo."],
    ],
    "de-DE": [
        ["Dein Jahr in Pixeln,", "aus deinen Fotos"],
        ["Ein Foto am Tag.", "Eine Farbe, die du wählst."],
        ["Das ganze Jahr", "in einem Farbverlauf"],
        ["Teile die Karte", "oder nur die Farbe"],
        ["Auf deinem Homescreen,", "nie dein Foto"],
        ["Dein Jahr als Poster.", "Einmal zahlen, für immer."],
    ],
    "fr-FR": [
        ["Ton année en pixels,", "faite de tes photos"],
        ["Une photo par jour.", "Une couleur que tu choisis."],
        ["Toute l'année", "en un seul dégradé"],
        ["Partage la carte,", "ou juste la couleur"],
        ["Sur ton écran d'accueil,", "jamais ta photo"],
        ["Ton année en poster.", "Un seul paiement, à vie."],
    ],
    "pt-BR": [
        ["Seu ano em pixels,", "feito das suas fotos"],
        ["Uma foto por dia.", "Uma cor que você escolhe."],
        ["O ano inteiro", "em um degradê"],
        ["Compartilhe o cartão,", "ou só a cor"],
        ["Na sua tela inicial,", "nunca a sua foto"],
        ["Seu ano em um pôster.", "Pague uma vez, é seu."],
    ],
    "it-IT": [
        ["Il tuo anno in pixel,", "fatto delle tue foto"],
        ["Una foto al giorno.", "Un colore che scegli tu."],
        ["Tutto l'anno", "in un'unica sfumatura"],
        ["Condividi la carta,", "o solo il colore"],
        ["Sulla tua schermata Home,", "mai la tua foto"],
        ["Il tuo anno in un poster.", "Paghi una volta, è tuo."],
    ],
    "nl-NL": [
        ["Jouw jaar in pixels,", "gemaakt van je foto's"],
        ["Eén foto per dag.", "Eén kleur die jij kiest."],
        ["Het hele jaar", "in één kleurverloop"],
        ["Deel de kaart,", "of alleen de kleur"],
        ["Op je beginscherm,", "nooit je foto"],
        ["Je jaar als poster.", "Eén keer betalen."],
    ],
    "pl-PL": [
        ["Twój rok w pikselach,", "ze Twoich zdjęć"],
        ["Jedno zdjęcie dziennie.", "Kolor, który wybierasz."],
        ["Cały rok", "w jednym gradiencie"],
        ["Udostępnij kartę", "albo sam kolor"],
        ["Na ekranie głównym,", "nigdy Twoje zdjęcie"],
        ["Twój rok jako plakat.", "Płacisz raz."],
    ],
    "ru-RU": [
        ["Ваш год в пикселях", "из ваших фотографий"],
        ["Одно фото в день.", "Цвет выбираете вы."],
        ["Весь год", "в одном градиенте"],
        ["Поделитесь карточкой", "или только цветом"],
        ["На главном экране", "никогда ваше фото"],
        ["Ваш год в виде постера.", "Платите один раз."],
    ],
    "tr-TR": [
        ["Yılın piksel piksel,", "kendi fotoğraflarından"],
        ["Günde bir fotoğraf.", "Seçtiğin bir renk."],
        ["Koca yıl", "tek bir gradyanda"],
        ["Kartı paylaş", "ya da sadece rengi"],
        ["Ana ekranında renk,", "fotoğrafın asla"],
        ["Yılın bir poster olarak.", "Bir kez öde."],
    ],
    "id": [
        ["Tahunmu dalam piksel,", "dari foto-fotomu"],
        ["Satu foto sehari.", "Satu warna pilihanmu."],
        ["Satu tahun penuh", "dalam satu gradasi"],
        ["Bagikan kartunya,", "atau warnanya saja"],
        ["Di layar utama,", "tanpa fotomu"],
        ["Tahunmu jadi poster.", "Bayar sekali saja."],
    ],
    "ja-JP": [
        ["1年をピクセルで、", "あなたの写真から"],
        ["1日1枚の写真。", "選ぶのは、あなたの色。"],
        ["1年が、", "ひとつのグラデーションに"],
        ["カードを共有、", "色だけでもOK"],
        ["ホーム画面には色だけ。", "写真は映りません"],
        ["1年をポスターに。", "支払いは一度だけ"],
    ],
    "ko-KR": [
        ["한 해를 픽셀로,", "내 사진으로 완성"],
        ["하루에 사진 한 장.", "색은 내가 고릅니다."],
        ["한 해 전체가", "하나의 그라데이션으로"],
        ["카드를 공유하거나", "색만 공유하세요"],
        ["홈 화면에는 색만,", "사진은 보이지 않아요"],
        ["한 해를 포스터로.", "한 번 결제로 평생"],
    ],
}

# Por destino: el tamano final, el de la captura cruda, y lo que se le quita arriba (barra de
# estado) y abajo (barra de gestos o indicador de inicio). Ninguna de las dos dice nada de la app.
# play-dibujado: las crudas de iphone.py (1320x2868 con los margenes puestos) en la lamina de Play.
DESTINOS = {
    "play": {"final": (1200, 2100), "crudo": (1080, 2400), "recorte": (90, 60)},
    "iphone": {"final": (1320, 2868), "crudo": (1320, 2868), "recorte": (170, 70)},
    "play-dibujado": {"final": (1200, 2100), "crudo": (1320, 2868), "recorte": (170, 70)},
}

# La ley del marco, en la proporcion de la lamina de Play, que es donde se ajusto a ojo.
BASE_W, BASE_H = 1200, 2100
LETRA = 88


def em(c):
    """Ancho aproximado de un caracter en em: lo justo para que ninguna linea se salga."""
    o = ord(c)
    if o >= 0x2E80 or 0x1100 <= o < 0x1200:
        return 1.0
    if 0x400 <= o < 0x500:
        return 0.66
    return 0.3 if c in " .,:;'!" else (0.68 if c.isupper() else 0.57)


def letra(idioma, ancho, k):
    """Un solo tamano para las seis de un idioma: el mayor con el que cabe la linea mas larga."""
    base = LETRA * min(k, ancho / BASE_W)
    util = ancho - 2 * round(96 * ancho / BASE_W)
    larga = max(sum(em(c) for c in linea) for par in TITULARES[idioma] for linea in par)
    return min(base, util / larga)


def geometria(destino):
    ancho, alto = destino["final"]
    crudo_w, crudo_h = destino["crudo"]
    arriba, abajo = destino["recorte"]
    k = alto / BASE_H
    alto_util = crudo_h - arriba - abajo
    alto_movil = round(1650 * k)
    ancho_movil = round(crudo_w * alto_movil / alto_util)
    return k, alto_util, alto_movil, ancho_movil, (ancho - ancho_movil) // 2, alto - alto_movil - round(90 * k)


def titular(lineas, idioma, destino):
    """El texto en SVG, centrado en el hueco que deja el movil debajo."""
    ancho, _ = destino["final"]
    k, _, _, _, _, y_movil = geometria(destino)
    t = letra(idioma, ancho, k)
    paso = t * 1.18
    y0 = y_movil / 2 - paso / 2 + t * 0.35
    return "".join(
        '<text x="%d" y="%d" font-family="Helvetica Neue, Helvetica, Arial, sans-serif" '
        'font-size="%d" font-weight="700" fill="%s">%s</text>'
        % (round(96 * ancho / BASE_W), round(y0 + i * paso), round(t), TINTA,
           linea.replace("&", "&amp;").replace("<", "&lt;"))
        for i, linea in enumerate(lineas)
    )


def incrustar(png):
    """rsvg no abre ficheros fuera del directorio del SVG, asi que la captura viaja dentro."""
    return "data:image/png;base64," + base64.b64encode(png.read_bytes()).decode("ascii")


def a_png(svg, salida, destino):
    ancho, alto = destino["final"]
    tmp = salida.with_suffix(".svg")
    tmp.write_text(svg, encoding="utf-8")
    subprocess.run(["rsvg-convert", "-w", str(ancho), "-h", str(alto), str(tmp), "-o", str(salida)],
                   check=True)
    tmp.unlink()


def marco(png, lineas, idioma, fondo, salida, destino):
    ancho, alto = destino["final"]
    crudo_w, crudo_h = destino["crudo"]
    arriba, _ = destino["recorte"]
    k, alto_util, alto_movil, ancho_movil, x, y = geometria(destino)
    escala = alto_movil / alto_util
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
  <rect x="{x}" y="{y}" width="{aw}" height="{ah}" rx="{rx}" ry="{rx}" fill="none" stroke="{borde}" stroke-width="2"/>
</svg>""".format(
        W=ancho, H=alto, fondo=fondo, papel=PAPEL, borde=BORDE, texto=titular(lineas, idioma, destino),
        png=incrustar(png), x=x, y=y, aw=ancho_movil, ah=alto_movil, rx=round(46 * k),
        ix=x, iy=round(y - arriba * escala), iw=ancho_movil, ih=round(crudo_h * escala),
    )
    a_png(svg, salida, destino)


def recaptionar(viejo, lineas, idioma, salida, destino):
    """Misma lamina con otro titular: el fondo es liso, asi que basta tapar el hueco de arriba."""
    ancho, alto = destino["final"]
    _, _, _, _, _, y = geometria(destino)
    svg = """<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
  width="{W}" height="{H}" viewBox="0 0 {W} {H}">
  <image xlink:href="{png}" x="0" y="0" width="{W}" height="{H}"/>
  <rect width="{W}" height="{y}" fill="{papel}"/>
  {texto}
</svg>""".format(W=ancho, H=alto, y=y - 4, papel=PAPEL, png=incrustar(viejo),
                 texto=titular(lineas, idioma, destino))
    a_png(svg, salida, destino)


def salida_de(nombre_destino, idioma):
    carpeta = pathlib.Path(__file__).resolve().parents[2] / "store" / "screenshots" / nombre_destino.split("-")[0] / idioma
    carpeta.mkdir(parents=True, exist_ok=True)
    return carpeta


def escenas(nombre_destino):
    """Los nombres de escena de una ficha, en orden; Play dibujado no lleva widgets de iOS."""
    return [n for n in CAPTURAS if not (nombre_destino == "play-dibujado" and n == "05_widgets")]


def main():
    args = sys.argv[1:]
    if args[:1] == ["--recaptionar"] and len(args) == 3 and args[2] in DESTINOS:
        idioma, nombre = args[1], args[2]
        destino, salida = DESTINOS[nombre], salida_de(nombre, args[1])
        viejas = {n: (salida / ("%02d.png" % (i + 1))).read_bytes() for i, n in enumerate(ORDEN_ANTIGUO)}
        tmp = salida / "_viejo.png"
        for i, n in enumerate(CAPTURAS):
            tmp.write_bytes(viejas[n])
            recaptionar(tmp, TITULARES[idioma][i], idioma, salida / ("%02d.png" % (i + 1)), destino)
            print(salida / ("%02d.png" % (i + 1)))
        tmp.unlink()
        return
    if len(args) != 3 or args[2] not in DESTINOS:
        sys.exit(__doc__)
    crudas = pathlib.Path(args[0])
    idioma, nombre = args[1], args[2]
    destino, salida = DESTINOS[nombre], salida_de(nombre, idioma)
    for i, nombre_escena in enumerate(escenas(nombre)):
        final = salida / ("%02d.png" % (i + 1))
        if not (crudas / (nombre_escena + ".png")).exists():
            print("falta", nombre_escena)
            continue
        marco(crudas / (nombre_escena + ".png"), TITULARES[idioma][CAPTURAS.index(nombre_escena)], idioma,
              FONDOS[i], final, destino)
        print(final)
    # Play dibujado tiene una escena menos: sobran los ficheros de mas de una vuelta anterior.
    for sobra in salida.glob("*.png"):
        if int(sobra.stem) > len(escenas(nombre)):
            sobra.unlink()


if __name__ == "__main__":
    main()
