package com.baltajmn.color.color

/**
 * A named color. The name is the one each language would use for it, not a word by word
 * translation (docs/textos.md 3). Lower case except German nouns; the interface capitalizes.
 */
class ColorName(
    val key: String,
    val hex: String,
    val en: String,
    val es: String,
    val pt: String,
    val de: String,
    val fr: String,
) {
    val lab: Lab by lazy { labOf(hex) }

    fun label(lang: String): String = when (lang) {
        "es" -> es
        "pt" -> pt
        "de" -> de
        "fr" -> fr
        else -> en
    }
}

private fun n(key: String, hex: String, en: String, es: String, pt: String, de: String, fr: String) =
    ColorName(key, hex, en, es, pt, de, fr)

/**
 * The only copy of the names. Spread over the hue circle in light, mid and dark, with warm and cool
 * neutrals, so no grey ever comes out named after a vivid color.
 */
val COLOR_NAMES: List<ColorName> = listOf(
    // Neutrals, light to dark.
    n("snow", "#FAFAF7", "snow", "nieve", "neve", "Schnee", "neige"),
    n("paper", "#EFE8DA", "paper white", "blanco papel", "branco papel", "Papierweiß", "blanc papier"),
    n("bone", "#E3DACA", "bone", "hueso", "osso", "Knochenweiß", "os"),
    n("pearl", "#D6D6D2", "pearl grey", "gris perla", "cinza pérola", "Perlgrau", "gris perle"),
    n("fog", "#C3C8CD", "fog", "niebla", "névoa", "Nebel", "brouillard"),
    n("silver", "#ABAEB1", "silver", "plata", "prata", "Silber", "argent"),
    n("pebble", "#A89F92", "pebble", "guijarro", "seixo", "Kiesel", "galet"),
    n("concrete", "#96968F", "concrete", "hormigón", "concreto", "Beton", "béton"),
    n("stone", "#857C71", "stone", "piedra", "pedra", "Stein", "pierre"),
    n("smoke", "#6D6D69", "smoke", "humo", "fumaça", "Rauch", "fumée"),
    n("slate", "#5B6570", "slate", "pizarra", "ardósia", "Schiefer", "ardoise"),
    n("graphite", "#474747", "graphite", "grafito", "grafite", "Graphit", "graphite"),
    n("charcoal", "#32302E", "charcoal", "carbón", "carvão", "Kohle", "charbon"),
    n("ink", "#1D2129", "ink", "tinta", "tinta", "Tinte", "encre"),
    n("night", "#0D0D0F", "night", "noche", "noite", "Nacht", "nuit"),

    // Reds and pinks.
    n("powder_pink", "#F3D6DC", "powder pink", "rosa empolvado", "rosa-claro", "Puderrosa", "rose poudre"),
    n("blush", "#F2C4BD", "blush", "rubor", "rubor", "Zartrosa", "rose tendre"),
    n("rose", "#E8A0A8", "rose", "rosa", "rosa", "Rosa", "rose"),
    n("peony", "#E68AB8", "peony", "peonía", "peônia", "Pfingstrose", "pivoine"),
    n("dusty_rose", "#C08A86", "dusty rose", "rosa palo", "rosa antigo", "Altrosa", "vieux rose"),
    n("salmon", "#F29B80", "salmon", "salmón", "salmão", "Lachs", "saumon"),
    n("coral", "#F07A68", "coral", "coral", "coral", "Koralle", "corail"),
    n("watermelon", "#F25F6E", "watermelon", "sandía", "melancia", "Wassermelone", "pastèque"),
    n("tomato", "#E0452F", "tomato", "tomate", "tomate", "Tomate", "tomate"),
    n("poppy", "#D7263D", "poppy red", "rojo amapola", "vermelho papoula", "Mohnrot", "rouge coquelicot"),
    n("raspberry", "#C7365F", "raspberry", "frambuesa", "framboesa", "Himbeere", "framboise"),
    n("cherry", "#A81C35", "cherry", "cereza", "cereja", "Kirschrot", "cerise"),
    n("brick", "#A3452E", "brick", "ladrillo", "tijolo", "Ziegelrot", "brique"),
    n("wine", "#6E1E2B", "wine", "vino", "vinho", "Weinrot", "lie-de-vin"),
    n("burgundy", "#471520", "burgundy", "burdeos", "bordô", "Bordeaux", "bordeaux"),

    // Oranges and browns.
    n("peach", "#F8C4A2", "peach", "melocotón", "pêssego", "Pfirsich", "pêche"),
    n("apricot", "#F4A261", "apricot", "albaricoque", "damasco", "Aprikose", "abricot"),
    n("tangerine", "#F28C28", "tangerine", "mandarina", "tangerina", "Mandarine", "mandarine"),
    n("orange", "#EC6E0E", "orange", "naranja", "laranja", "Orange", "orange"),
    n("pumpkin", "#CF5E17", "pumpkin", "calabaza", "abóbora", "Kürbis", "citrouille"),
    n("terracotta", "#C46A4A", "terracotta", "terracota", "terracota", "Terrakotta", "terre cuite"),
    n("rust", "#A84A25", "rust", "óxido", "ferrugem", "Rost", "rouille"),
    n("copper", "#B8733B", "copper", "cobre", "cobre", "Kupfer", "cuivre"),
    n("caramel", "#C98C4E", "caramel", "caramelo", "caramelo", "Karamell", "caramel"),
    n("camel", "#B79468", "camel", "camel", "camelo", "Kamel", "camel"),
    n("clay", "#AE7C63", "clay", "arcilla", "argila", "Ton", "argile"),
    n("cinnamon", "#94552F", "cinnamon", "canela", "canela", "Zimt", "cannelle"),
    n("mocha", "#8A6955", "mocha", "moca", "moca", "Mokka", "moka"),
    n("chestnut", "#733A22", "chestnut", "castaña", "castanha", "Kastanie", "châtaigne"),
    n("coffee", "#5E4334", "coffee", "café", "café", "Kaffee", "café"),
    n("umber", "#5A4A2E", "umber", "tierra de sombra", "sombra queimada", "Umbra", "terre d'ombre"),
    n("chocolate", "#3F2419", "chocolate", "chocolate", "chocolate", "Schokolade", "chocolat"),

    // Yellows.
    n("cream", "#F6ECCB", "cream", "crema", "creme", "Creme", "crème"),
    n("butter", "#F7E08A", "butter", "mantequilla", "manteiga", "Butter", "beurre"),
    n("lemon", "#F2E03F", "lemon", "limón", "limão", "Zitrone", "citron"),
    n("sunflower", "#F6C324", "sunflower", "girasol", "girassol", "Sonnenblume", "tournesol"),
    n("amber", "#F0A81A", "amber", "ámbar", "âmbar", "Bernstein", "ambre"),
    n("honey", "#D9A441", "honey", "miel", "mel", "Honig", "miel"),
    n("mustard", "#C9A42E", "mustard", "mostaza", "mostarda", "Senf", "moutarde"),
    n("ochre", "#B98A2C", "ochre", "ocre", "ocre", "Ocker", "ocre"),
    n("straw", "#E3D08C", "straw", "paja", "palha", "Stroh", "paille"),
    n("sand", "#DCC6A0", "sand", "arena", "areia", "Sand", "sable"),
    n("khaki", "#B3A672", "khaki", "caqui", "cáqui", "Khaki", "kaki"),
    n("olive", "#7C772E", "olive", "oliva", "oliva", "Oliv", "olive"),

    // Greens.
    n("pistachio", "#C8DCA2", "pistachio", "pistacho", "pistache", "Pistazie", "pistache"),
    n("lime", "#A6D12A", "lime", "lima", "lima", "Limette", "citron vert"),
    n("spring_green", "#7CC46B", "spring green", "verde primavera", "verde primavera", "Frühlingsgrün", "vert printemps"),
    n("grass", "#4AA548", "grass", "hierba", "grama", "Grasgrün", "herbe"),
    n("leaf", "#2E7A31", "leaf green", "verde hoja", "verde folha", "Blattgrün", "vert feuille"),
    n("fern", "#5A8A4A", "fern", "helecho", "samambaia", "Farn", "fougère"),
    n("moss", "#687838", "moss", "musgo", "musgo", "Moos", "mousse"),
    n("avocado", "#525F28", "avocado", "aguacate", "abacate", "Avocado", "avocat"),
    n("sage", "#9CAF88", "sage", "salvia", "sálvia", "Salbei", "sauge"),
    n("eucalyptus", "#7FA69A", "eucalyptus", "eucalipto", "eucalipto", "Eukalyptus", "eucalyptus"),
    n("mint", "#A8E0C4", "mint", "menta", "menta", "Minze", "menthe"),
    n("jade", "#2FA67A", "jade", "jade", "jade", "Jade", "jade"),
    n("emerald", "#1B8A56", "emerald", "esmeralda", "esmeralda", "Smaragd", "émeraude"),
    n("forest", "#214D2E", "forest", "bosque", "floresta", "Waldgrün", "vert forêt"),
    n("pine", "#1D3A31", "pine", "pino", "pinheiro", "Kiefer", "pin"),

    // Teals and cyans.
    n("glacier", "#CFE8EC", "glacier", "glaciar", "geleira", "Gletscher", "glacier"),
    n("aqua", "#7FD8D4", "aquamarine", "aguamarina", "água-marinha", "Aquamarin", "aigue-marine"),
    n("turquoise", "#2BB5B0", "turquoise", "turquesa", "turquesa", "Türkis", "turquoise"),
    n("lagoon", "#39A6C3", "lagoon", "laguna", "lagoa", "Lagune", "lagon"),
    n("teal", "#1A7F80", "teal", "verde azulado", "verde-azulado", "Blaugrün", "bleu canard"),
    n("petrol", "#1E4F5C", "petrol blue", "azul petróleo", "azul petróleo", "Petrol", "bleu pétrole"),

    // Blues.
    n("powder_blue", "#B7CCE0", "powder blue", "azul empolvado", "azul pó", "Puderblau", "bleu poudre"),
    n("sky", "#95C6EA", "sky", "cielo", "céu", "Himmelblau", "ciel"),
    n("cornflower", "#6F95D9", "cornflower", "aciano", "centáurea", "Kornblume", "bleuet"),
    n("cerulean", "#2A87C8", "cerulean", "cerúleo", "cerúleo", "Coelinblau", "céruléen"),
    n("storm_blue", "#3A6EA5", "storm blue", "azul tormenta", "azul tempestade", "Sturmblau", "bleu orage"),
    n("steel", "#6E8499", "steel", "acero", "aço", "Stahlblau", "acier"),
    n("dusk", "#7A84A6", "dusk", "anochecer", "entardecer", "Dämmerung", "crépuscule"),
    n("denim", "#475F85", "denim", "vaquero", "jeans", "Jeansblau", "denim"),
    n("ocean", "#1D5A94", "ocean", "océano", "oceano", "Ozean", "océan"),
    n("cobalt", "#1F47B8", "cobalt", "cobalto", "cobalto", "Kobalt", "cobalt"),
    n("ultramarine", "#2A2FA6", "ultramarine", "ultramar", "ultramar", "Ultramarin", "outremer"),
    n("navy", "#1B2A4E", "navy", "azul marino", "azul-marinho", "Marineblau", "bleu marine"),
    n("midnight", "#111933", "midnight", "medianoche", "meia-noite", "Mitternacht", "minuit"),

    // Purples.
    n("periwinkle", "#A3A8E6", "periwinkle", "vincapervinca", "pervinca", "Immergrün", "pervenche"),
    n("lavender", "#C7B6E6", "lavender", "lavanda", "lavanda", "Lavendel", "lavande"),
    n("lilac", "#B598D6", "lilac", "lila", "lilás", "Flieder", "lilas"),
    n("orchid", "#C77DC4", "orchid", "orquídea", "orquídea", "Orchidee", "orchidée"),
    n("mauve", "#9E7A92", "mauve", "malva", "malva", "Malve", "mauve"),
    n("amethyst", "#8E5AA8", "amethyst", "amatista", "ametista", "Amethyst", "améthyste"),
    n("violet", "#7A4FC4", "violet", "violeta", "violeta", "Veilchenblau", "violet"),
    n("iris", "#5A4FB0", "iris", "iris", "íris", "Iris", "iris"),
    n("grape", "#4E2A6B", "grape", "uva", "uva", "Traube", "raisin"),
    n("plum", "#6B2F5E", "plum", "ciruela", "ameixa", "Pflaume", "prune"),
    n("aubergine", "#3B1E38", "aubergine", "berenjena", "berinjela", "Aubergine", "aubergine"),

    // Magentas.
    n("fuchsia", "#D63AA0", "fuchsia", "fucsia", "fúcsia", "Fuchsia", "fuchsia"),
    n("magenta", "#B3127A", "magenta", "magenta", "magenta", "Magenta", "magenta"),
)

internal val BY_KEY: Map<String, ColorName> by lazy { COLOR_NAMES.associateBy { it.key } }

/** The closest named color, by deltaE. */
fun nearestName(hex: String): ColorName {
    val lab = labOf(hex)
    return COLOR_NAMES.minBy { deltaE(it.lab, lab) }
}

/** The name to show for a stored key, first letter up. An unknown key (a newer backup) shows as is. */
fun colorLabel(key: String, lang: String): String =
    (BY_KEY[key]?.label(lang) ?: key).replaceFirstChar { it.uppercase() }
