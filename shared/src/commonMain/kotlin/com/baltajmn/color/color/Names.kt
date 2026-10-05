package com.baltajmn.color.color

/**
 * A named color. The name is the one each language would use for it, not a word by word
 * translation (docs/textos.md 3). Lower case except German nouns; the interface capitalizes.
 * Japanese and Korean have no case: their names are what a paint chart there would print.
 */
class ColorName(
    val key: String,
    val hex: String,
    val en: String,
    val es: String,
    val pt: String,
    val de: String,
    val fr: String,
    val it: String,
    val nl: String,
    val pl: String,
    val ru: String,
    val tr: String,
    val id: String,
    val ja: String,
    val ko: String,
) {
    val lab: Lab by lazy { labOf(hex) }

    fun label(lang: String): String = when (lang) {
        "es" -> es
        "pt" -> pt
        "de" -> de
        "fr" -> fr
        "it" -> it
        "nl" -> nl
        "pl" -> pl
        "ru" -> ru
        "tr" -> tr
        "id" -> id
        "ja" -> ja
        "ko" -> ko
        else -> en
    }
}

private fun n(
    key: String, hex: String, en: String, es: String, pt: String, de: String, fr: String,
    it: String, nl: String, pl: String, ru: String, tr: String, id: String, ja: String, ko: String,
) = ColorName(key, hex, en, es, pt, de, fr, it, nl, pl, ru, tr, id, ja, ko)

/**
 * The only copy of the names. Spread over the hue circle in light, mid and dark, with warm and cool
 * neutrals, so no grey ever comes out named after a vivid color.
 */
val COLOR_NAMES: List<ColorName> = listOf(
    // Neutrals, light to dark.
    n("snow", "#FAFAF7", "snow", "nieve", "neve", "Schnee", "neige",
        "neve", "sneeuw", "śnieg", "снег", "kar", "salju", "雪白", "설백"),
    n("paper", "#EFE8DA", "paper white", "blanco papel", "branco papel", "Papierweiß", "blanc papier",
        "bianco carta", "papierwit", "papierowa biel", "бумажно-белый", "kâğıt beyazı", "putih kertas", "生成り", "미색"),
    n("bone", "#E3DACA", "bone", "hueso", "osso", "Knochenweiß", "os",
        "osso", "ivoor", "ecru", "слоновая кость", "kemik beyazı", "putih tulang", "アイボリー", "아이보리"),
    n("pearl", "#D6D6D2", "pearl grey", "gris perla", "cinza pérola", "Perlgrau", "gris perle",
        "grigio perla", "parelgrijs", "perłowa szarość", "жемчужно-серый", "inci grisi", "abu-abu mutiara", "パールグレー", "펄 그레이"),
    n("fog", "#C3C8CD", "fog", "niebla", "névoa", "Nebel", "brouillard",
        "nebbia", "mist", "mgła", "туман", "sis", "kabut", "霧", "안개"),
    n("silver", "#ABAEB1", "silver", "plata", "prata", "Silber", "argent",
        "argento", "zilver", "srebro", "серебро", "gümüş", "perak", "銀色", "은색"),
    n("pebble", "#A89F92", "pebble", "guijarro", "seixo", "Kiesel", "galet",
        "ciottolo", "kiezel", "kamyk", "галька", "çakıl", "kerikil", "小石色", "조약돌"),
    n("concrete", "#96968F", "concrete", "hormigón", "concreto", "Beton", "béton",
        "cemento", "beton", "beton", "бетон", "beton", "beton", "コンクリート", "콘크리트"),
    n("stone", "#857C71", "stone", "piedra", "pedra", "Stein", "pierre",
        "pietra", "steen", "kamień", "камень", "taş", "batu", "石色", "돌색"),
    n("smoke", "#6D6D69", "smoke", "humo", "fumaça", "Rauch", "fumée",
        "fumo", "rook", "dym", "дым", "duman", "asap", "スモーク", "스모크"),
    n("slate", "#5B6570", "slate", "pizarra", "ardósia", "Schiefer", "ardoise",
        "ardesia", "leisteen", "łupek", "сланец", "arduvaz", "batu tulis", "スレート", "슬레이트"),
    n("graphite", "#474747", "graphite", "grafito", "grafite", "Graphit", "graphite",
        "grafite", "grafiet", "grafit", "графит", "grafit", "grafit", "グラファイト", "그라파이트"),
    n("charcoal", "#32302E", "charcoal", "carbón", "carvão", "Kohle", "charbon",
        "carbone", "houtskool", "węgiel", "уголь", "kömür", "arang", "チャコール", "차콜"),
    n("ink", "#1D2129", "ink", "tinta", "tinta", "Tinte", "encre",
        "inchiostro", "inkt", "atrament", "чернила", "mürekkep", "tinta", "墨色", "먹색"),
    n("night", "#0D0D0F", "night", "noche", "noite", "Nacht", "nuit",
        "notte", "nacht", "noc", "ночь", "gece", "malam", "夜", "밤"),

    // Reds and pinks.
    n("powder_pink", "#F3D6DC", "powder pink", "rosa empolvado", "rosa-claro", "Puderrosa", "rose poudre",
        "rosa cipria", "poederroze", "pudrowy róż", "пудрово-розовый", "pudra pembe", "merah muda pastel", "パウダーピンク", "파우더 핑크"),
    n("blush", "#F2C4BD", "blush", "rubor", "rubor", "Zartrosa", "rose tendre",
        "rosa tenue", "zachtroze", "bladoróżowy", "румянец", "allık", "merah pipi", "桜色", "벚꽃색"),
    n("rose", "#E8A0A8", "rose", "rosa", "rosa", "Rosa", "rose",
        "rosa", "roze", "róż", "розовый", "gül", "mawar", "ローズ", "장미색"),
    n("peony", "#E68AB8", "peony", "peonía", "peônia", "Pfingstrose", "pivoine",
        "peonia", "pioen", "piwonia", "пион", "şakayık", "peoni", "牡丹色", "모란색"),
    n("dusty_rose", "#C08A86", "dusty rose", "rosa palo", "rosa antigo", "Altrosa", "vieux rose",
        "rosa antico", "oudroze", "zgaszony róż", "пыльная роза", "gül kurusu", "mawar pudar", "灰桜", "더스티 로즈"),
    n("salmon", "#F29B80", "salmon", "salmón", "salmão", "Lachs", "saumon",
        "salmone", "zalm", "łososiowy", "лососевый", "somon", "salem", "サーモン", "연어색"),
    n("coral", "#F07A68", "coral", "coral", "coral", "Koralle", "corail",
        "corallo", "koraal", "koralowy", "коралловый", "mercan", "koral", "コーラル", "코랄"),
    n("watermelon", "#F25F6E", "watermelon", "sandía", "melancia", "Wassermelone", "pastèque",
        "anguria", "watermeloen", "arbuzowy", "арбузный", "karpuz", "semangka", "スイカ色", "수박색"),
    n("tomato", "#E0452F", "tomato", "tomate", "tomate", "Tomate", "tomate",
        "pomodoro", "tomaat", "pomidorowy", "томатный", "domates", "tomat", "トマト", "토마토"),
    n("poppy", "#D7263D", "poppy red", "rojo amapola", "vermelho papoula", "Mohnrot", "rouge coquelicot",
        "rosso papavero", "klaprozenrood", "czerwień maku", "маковый", "gelincik kırmızısı", "merah poppy", "ポピーレッド", "양귀비색"),
    n("raspberry", "#C7365F", "raspberry", "frambuesa", "framboesa", "Himbeere", "framboise",
        "lampone", "framboos", "malinowy", "малиновый", "ahududu", "frambos", "ラズベリー", "라즈베리"),
    n("cherry", "#A81C35", "cherry", "cereza", "cereja", "Kirschrot", "cerise",
        "ciliegia", "kers", "wiśniowy", "вишнёвый", "kiraz", "ceri", "チェリー", "체리"),
    n("brick", "#A3452E", "brick", "ladrillo", "tijolo", "Ziegelrot", "brique",
        "mattone", "baksteen", "ceglasty", "кирпичный", "tuğla", "bata merah", "煉瓦色", "벽돌색"),
    n("wine", "#6E1E2B", "wine", "vino", "vinho", "Weinrot", "lie-de-vin",
        "vino", "wijnrood", "winny", "винный", "şarap", "merah anggur", "ワイン", "와인"),
    n("burgundy", "#471520", "burgundy", "burdeos", "bordô", "Bordeaux", "bordeaux",
        "bordeaux", "bordeaux", "burgund", "бордовый", "bordo", "marun", "バーガンディ", "버건디"),

    // Oranges and browns.
    n("peach", "#F8C4A2", "peach", "melocotón", "pêssego", "Pfirsich", "pêche",
        "pesca", "perzik", "brzoskwiniowy", "персиковый", "şeftali", "persik", "ピーチ", "복숭아색"),
    n("apricot", "#F4A261", "apricot", "albaricoque", "damasco", "Aprikose", "abricot",
        "albicocca", "abrikoos", "morelowy", "абрикосовый", "kayısı", "aprikot", "アプリコット", "살구색"),
    n("tangerine", "#F28C28", "tangerine", "mandarina", "tangerina", "Mandarine", "mandarine",
        "mandarino", "mandarijn", "mandarynkowy", "мандариновый", "mandalina", "jeruk keprok", "蜜柑色", "귤색"),
    n("orange", "#EC6E0E", "orange", "naranja", "laranja", "Orange", "orange",
        "arancione", "oranje", "pomarańczowy", "оранжевый", "turuncu", "oranye", "オレンジ", "주황"),
    n("pumpkin", "#CF5E17", "pumpkin", "calabaza", "abóbora", "Kürbis", "citrouille",
        "zucca", "pompoen", "dyniowy", "тыквенный", "balkabağı", "labu", "かぼちゃ色", "펌킨"),
    n("terracotta", "#C46A4A", "terracotta", "terracota", "terracota", "Terrakotta", "terre cuite",
        "terracotta", "terracotta", "terakota", "терракотовый", "kiremit", "terakota", "テラコッタ", "테라코타"),
    n("rust", "#A84A25", "rust", "óxido", "ferrugem", "Rost", "rouille",
        "ruggine", "roest", "rdzawy", "ржавый", "pas", "karat", "錆色", "러스트"),
    n("copper", "#B8733B", "copper", "cobre", "cobre", "Kupfer", "cuivre",
        "rame", "koper", "miedziany", "медный", "bakır", "tembaga", "銅色", "구리색"),
    n("caramel", "#C98C4E", "caramel", "caramelo", "caramelo", "Karamell", "caramel",
        "caramello", "karamel", "karmelowy", "карамельный", "karamel", "karamel", "キャラメル", "캐러멜"),
    n("camel", "#B79468", "camel", "camel", "camelo", "Kamel", "camel",
        "cammello", "camel", "camel", "кэмел", "deve tüyü", "cokelat unta", "キャメル", "카멜"),
    n("clay", "#AE7C63", "clay", "arcilla", "argila", "Ton", "argile",
        "argilla", "klei", "glina", "глина", "kil", "tanah liat", "クレイ", "점토색"),
    n("cinnamon", "#94552F", "cinnamon", "canela", "canela", "Zimt", "cannelle",
        "cannella", "kaneel", "cynamon", "корица", "tarçın", "kayu manis", "シナモン", "시나몬"),
    n("mocha", "#8A6955", "mocha", "moca", "moca", "Mokka", "moka",
        "moka", "mokka", "mokka", "мокко", "moka", "moka", "モカ", "모카"),
    n("chestnut", "#733A22", "chestnut", "castaña", "castanha", "Kastanie", "châtaigne",
        "castagna", "kastanje", "kasztanowy", "каштановый", "kestane", "kastanye", "栗色", "밤색"),
    n("coffee", "#5E4334", "coffee", "café", "café", "Kaffee", "café",
        "caffè", "koffie", "kawa", "кофейный", "kahve", "kopi", "コーヒー", "커피색"),
    n("umber", "#5A4A2E", "umber", "tierra de sombra", "sombra queimada", "Umbra", "terre d'ombre",
        "terra d'ombra", "omber", "umbra", "умбра", "umbra", "umber", "焦茶", "엄버"),
    n("chocolate", "#3F2419", "chocolate", "chocolate", "chocolate", "Schokolade", "chocolat",
        "cioccolato", "chocolade", "czekoladowy", "шоколадный", "çikolata", "cokelat", "チョコレート", "초콜릿"),

    // Yellows.
    n("cream", "#F6ECCB", "cream", "crema", "creme", "Creme", "crème",
        "crema", "crème", "kremowy", "кремовый", "krem", "krem", "クリーム", "크림"),
    n("butter", "#F7E08A", "butter", "mantequilla", "manteiga", "Butter", "beurre",
        "burro", "boter", "maślany", "сливочный", "tereyağı", "mentega", "バター", "버터"),
    n("lemon", "#F2E03F", "lemon", "limón", "limão", "Zitrone", "citron",
        "limone", "citroen", "cytrynowy", "лимонный", "limon", "lemon", "レモン", "레몬"),
    n("sunflower", "#F6C324", "sunflower", "girasol", "girassol", "Sonnenblume", "tournesol",
        "girasole", "zonnebloem", "słonecznikowy", "подсолнух", "ayçiçeği", "bunga matahari", "ひまわり", "해바라기"),
    n("amber", "#F0A81A", "amber", "ámbar", "âmbar", "Bernstein", "ambre",
        "ambra", "amber", "bursztynowy", "янтарный", "kehribar", "amber", "琥珀色", "호박색"),
    n("honey", "#D9A441", "honey", "miel", "mel", "Honig", "miel",
        "miele", "honing", "miodowy", "медовый", "bal", "madu", "蜂蜜色", "꿀색"),
    n("mustard", "#C9A42E", "mustard", "mostaza", "mostarda", "Senf", "moutarde",
        "senape", "mosterd", "musztardowy", "горчичный", "hardal", "mustard", "マスタード", "머스터드"),
    n("ochre", "#B98A2C", "ochre", "ocre", "ocre", "Ocker", "ocre",
        "ocra", "oker", "ochra", "охра", "aşı boyası", "oker", "黄土色", "황토색"),
    n("straw", "#E3D08C", "straw", "paja", "palha", "Stroh", "paille",
        "paglia", "stro", "słomkowy", "соломенный", "saman", "jerami", "麦わら色", "밀짚색"),
    n("sand", "#DCC6A0", "sand", "arena", "areia", "Sand", "sable",
        "sabbia", "zand", "piaskowy", "песочный", "kum", "pasir", "砂色", "모래색"),
    n("khaki", "#B3A672", "khaki", "caqui", "cáqui", "Khaki", "kaki",
        "kaki", "kaki", "khaki", "хаки", "haki", "khaki", "カーキ", "카키"),
    n("olive", "#7C772E", "olive", "oliva", "oliva", "Oliv", "olive",
        "oliva", "olijf", "oliwkowy", "оливковый", "zeytin yeşili", "zaitun", "オリーブ", "올리브"),

    // Greens.
    n("pistachio", "#C8DCA2", "pistachio", "pistacho", "pistache", "Pistazie", "pistache",
        "pistacchio", "pistache", "pistacjowy", "фисташковый", "fıstık yeşili", "pistasio", "ピスタチオ", "피스타치오"),
    n("lime", "#A6D12A", "lime", "lima", "lima", "Limette", "citron vert",
        "lime", "limoen", "limonkowy", "лаймовый", "misket limonu", "limau", "ライム", "라임"),
    n("spring_green", "#7CC46B", "spring green", "verde primavera", "verde primavera", "Frühlingsgrün", "vert printemps",
        "verde primavera", "lentegroen", "wiosenna zieleń", "весенняя зелень", "bahar yeşili", "hijau musim semi", "若草色", "연두색"),
    n("grass", "#4AA548", "grass", "hierba", "grama", "Grasgrün", "herbe",
        "erba", "gras", "trawiasty", "травяной", "çimen", "rumput", "草色", "풀색"),
    n("leaf", "#2E7A31", "leaf green", "verde hoja", "verde folha", "Blattgrün", "vert feuille",
        "verde foglia", "bladgroen", "zieleń liścia", "лиственный", "yaprak yeşili", "hijau daun", "リーフグリーン", "나뭇잎색"),
    n("fern", "#5A8A4A", "fern", "helecho", "samambaia", "Farn", "fougère",
        "felce", "varen", "paproć", "папоротник", "eğrelti otu", "pakis", "シダ色", "고사리색"),
    n("moss", "#687838", "moss", "musgo", "musgo", "Moos", "mousse",
        "muschio", "mos", "mech", "мох", "yosun", "lumut", "苔色", "이끼색"),
    n("avocado", "#525F28", "avocado", "aguacate", "abacate", "Avocado", "avocat",
        "avocado", "avocado", "awokado", "авокадо", "avokado", "alpukat", "アボカド", "아보카도"),
    n("sage", "#9CAF88", "sage", "salvia", "sálvia", "Salbei", "sauge",
        "salvia", "salie", "szałwia", "шалфей", "adaçayı", "hijau sage", "セージ", "세이지"),
    n("eucalyptus", "#7FA69A", "eucalyptus", "eucalipto", "eucalipto", "Eukalyptus", "eucalyptus",
        "eucalipto", "eucalyptus", "eukaliptus", "эвкалипт", "okaliptüs", "kayu putih", "ユーカリ", "유칼립투스"),
    n("mint", "#A8E0C4", "mint", "menta", "menta", "Minze", "menthe",
        "menta", "mint", "miętowy", "мятный", "nane", "mint", "ミント", "민트"),
    n("jade", "#2FA67A", "jade", "jade", "jade", "Jade", "jade",
        "giada", "jade", "jadeit", "нефритовый", "yeşim", "giok", "翡翠色", "비취색"),
    n("emerald", "#1B8A56", "emerald", "esmeralda", "esmeralda", "Smaragd", "émeraude",
        "smeraldo", "smaragd", "szmaragdowy", "изумрудный", "zümrüt", "zamrud", "エメラルド", "에메랄드"),
    n("forest", "#214D2E", "forest", "bosque", "floresta", "Waldgrün", "vert forêt",
        "verde bosco", "woudgroen", "leśna zieleń", "лесная зелень", "orman yeşili", "hijau hutan", "深緑", "숲색"),
    n("pine", "#1D3A31", "pine", "pino", "pinheiro", "Kiefer", "pin",
        "pino", "den", "sosnowy", "сосна", "çam", "pinus", "松葉色", "솔잎색"),

    // Teals and cyans.
    n("glacier", "#CFE8EC", "glacier", "glaciar", "geleira", "Gletscher", "glacier",
        "ghiacciaio", "gletsjer", "lodowiec", "ледник", "buzul", "gletser", "氷色", "빙하색"),
    n("aqua", "#7FD8D4", "aquamarine", "aguamarina", "água-marinha", "Aquamarin", "aigue-marine",
        "acquamarina", "aquamarijn", "akwamaryna", "аквамарин", "akuamarin", "aquamarin", "アクアマリン", "아쿠아마린"),
    n("turquoise", "#2BB5B0", "turquoise", "turquesa", "turquesa", "Türkis", "turquoise",
        "turchese", "turquoise", "turkusowy", "бирюзовый", "turkuaz", "toska", "ターコイズ", "터쿼이즈"),
    n("lagoon", "#39A6C3", "lagoon", "laguna", "lagoa", "Lagune", "lagon",
        "laguna", "lagune", "laguna", "лагуна", "lagün", "laguna", "ラグーン", "라군"),
    n("teal", "#1A7F80", "teal", "verde azulado", "verde-azulado", "Blaugrün", "bleu canard",
        "ottanio", "groenblauw", "morski", "сине-зелёный", "deniz yeşili", "hijau kebiruan", "青緑", "청록"),
    n("petrol", "#1E4F5C", "petrol blue", "azul petróleo", "azul petróleo", "Petrol", "bleu pétrole",
        "blu petrolio", "petrolblauw", "petrol", "петроль", "petrol mavisi", "biru petrol", "ペトロールブルー", "페트롤 블루"),

    // Blues.
    n("powder_blue", "#B7CCE0", "powder blue", "azul empolvado", "azul pó", "Puderblau", "bleu poudre",
        "azzurro polvere", "poederblauw", "pudrowy błękit", "пудрово-голубой", "pudra mavisi", "biru pastel", "パウダーブルー", "파우더 블루"),
    n("sky", "#95C6EA", "sky", "cielo", "céu", "Himmelblau", "ciel",
        "cielo", "hemelsblauw", "błękitny", "небесный", "gök mavisi", "biru langit", "空色", "하늘색"),
    n("cornflower", "#6F95D9", "cornflower", "aciano", "centáurea", "Kornblume", "bleuet",
        "fiordaliso", "korenbloem", "chabrowy", "васильковый", "peygamber çiçeği", "biru bunga jagung", "コーンフラワー", "수레국화색"),
    n("cerulean", "#2A87C8", "cerulean", "cerúleo", "cerúleo", "Coelinblau", "céruléen",
        "ceruleo", "azuur", "lazurowy", "лазурный", "masmavi", "biru serulean", "セルリアン", "세룰리안"),
    n("storm_blue", "#3A6EA5", "storm blue", "azul tormenta", "azul tempestade", "Sturmblau", "bleu orage",
        "blu tempesta", "stormblauw", "burzowy błękit", "грозовой синий", "fırtına mavisi", "biru badai", "ストームブルー", "스톰 블루"),
    n("steel", "#6E8499", "steel", "acero", "aço", "Stahlblau", "acier",
        "acciaio", "staalblauw", "stalowy", "стальной", "çelik", "baja", "鋼色", "강철색"),
    n("dusk", "#7A84A6", "dusk", "anochecer", "entardecer", "Dämmerung", "crépuscule",
        "crepuscolo", "schemering", "zmierzch", "сумерки", "alacakaranlık", "senja", "夕暮れ", "해질녘"),
    n("denim", "#475F85", "denim", "vaquero", "jeans", "Jeansblau", "denim",
        "denim", "denim", "dżinsowy", "джинсовый", "kot mavisi", "denim", "デニム", "데님"),
    n("ocean", "#1D5A94", "ocean", "océano", "oceano", "Ozean", "océan",
        "oceano", "oceaan", "oceaniczny", "океан", "okyanus", "samudra", "オーシャン", "바다색"),
    n("cobalt", "#1F47B8", "cobalt", "cobalto", "cobalto", "Kobalt", "cobalt",
        "cobalto", "kobalt", "kobaltowy", "кобальт", "kobalt", "kobalt", "コバルト", "코발트"),
    n("ultramarine", "#2A2FA6", "ultramarine", "ultramar", "ultramar", "Ultramarin", "outremer",
        "oltremare", "ultramarijn", "ultramaryna", "ультрамарин", "ultramarin", "ultramarin", "群青", "군청"),
    n("navy", "#1B2A4E", "navy", "azul marino", "azul-marinho", "Marineblau", "bleu marine",
        "blu navy", "marineblauw", "granatowy", "тёмно-синий", "lacivert", "biru dongker", "紺色", "남색"),
    n("midnight", "#111933", "midnight", "medianoche", "meia-noite", "Mitternacht", "minuit",
        "mezzanotte", "middernacht", "północ", "полночь", "gece yarısı", "tengah malam", "真夜中", "한밤중"),

    // Purples.
    n("periwinkle", "#A3A8E6", "periwinkle", "vincapervinca", "pervinca", "Immergrün", "pervenche",
        "pervinca", "maagdenpalm", "barwinkowy", "барвинок", "cezayir menekşesi", "biru periwinkle", "ペリウィンクル", "페리윙클"),
    n("lavender", "#C7B6E6", "lavender", "lavanda", "lavanda", "Lavendel", "lavande",
        "lavanda", "lavendel", "lawendowy", "лавандовый", "lavanta", "lavender", "ラベンダー", "라벤더"),
    n("lilac", "#B598D6", "lilac", "lila", "lilás", "Flieder", "lilas",
        "lilla", "lila", "liliowy", "сиреневый", "leylak", "ungu lila", "ライラック", "라일락"),
    n("orchid", "#C77DC4", "orchid", "orquídea", "orquídea", "Orchidee", "orchidée",
        "orchidea", "orchidee", "orchidea", "орхидея", "orkide", "anggrek", "オーキッド", "난초색"),
    n("mauve", "#9E7A92", "mauve", "malva", "malva", "Malve", "mauve",
        "malva", "mauve", "malwowy", "мальвовый", "eflatun", "ungu mauve", "モーブ", "모브"),
    n("amethyst", "#8E5AA8", "amethyst", "amatista", "ametista", "Amethyst", "améthyste",
        "ametista", "amethist", "ametystowy", "аметистовый", "ametist", "kecubung", "アメジスト", "자수정색"),
    n("violet", "#7A4FC4", "violet", "violeta", "violeta", "Veilchenblau", "violet",
        "viola", "violet", "fioletowy", "фиолетовый", "menekşe", "violet", "菫色", "보라"),
    n("iris", "#5A4FB0", "iris", "iris", "íris", "Iris", "iris",
        "iris", "iris", "irys", "ирис", "süsen", "iris", "菖蒲色", "붓꽃색"),
    n("grape", "#4E2A6B", "grape", "uva", "uva", "Traube", "raisin",
        "uva", "druif", "winogronowy", "виноградный", "üzüm", "ungu anggur", "葡萄色", "포도색"),
    n("plum", "#6B2F5E", "plum", "ciruela", "ameixa", "Pflaume", "prune",
        "prugna", "pruim", "śliwkowy", "сливовый", "erik", "plum", "プラム", "자두색"),
    n("aubergine", "#3B1E38", "aubergine", "berenjena", "berinjela", "Aubergine", "aubergine",
        "melanzana", "aubergine", "bakłażanowy", "баклажановый", "patlıcan", "terung", "茄子紺", "가지색"),

    // Magentas.
    n("fuchsia", "#D63AA0", "fuchsia", "fucsia", "fúcsia", "Fuchsia", "fuchsia",
        "fucsia", "fuchsia", "fuksja", "фуксия", "fuşya", "fuchsia", "フクシア", "푸시아"),
    n("magenta", "#B3127A", "magenta", "magenta", "magenta", "Magenta", "magenta",
        "magenta", "magenta", "magenta", "пурпурный", "macenta", "magenta", "マゼンタ", "마젠타"),
)

internal val BY_KEY: Map<String, ColorName> by lazy { COLOR_NAMES.associateBy { it.key } }

/** The closest named color, by deltaE. */
fun nearestName(hex: String): ColorName {
    val lab = labOf(hex)
    return COLOR_NAMES.minBy { deltaE(it.lab, lab) }
}

/** The name to show for a stored key, first letter up. An unknown key (a newer backup) shows as is. */
fun colorLabel(key: String, lang: String): String =
    (BY_KEY[key]?.label(lang) ?: key).replaceFirstChar { if (lang == "tr" && it == 'i') 'İ' else it.uppercaseChar() }
