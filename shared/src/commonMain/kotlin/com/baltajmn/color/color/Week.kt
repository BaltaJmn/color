package com.baltajmn.color.color

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

/** docs/tecnico.md 6.12: generous on purpose, a reason to look around rather than a target. */
const val WEEK_DELTA_E = 15.0

/**
 * One lively key of the name table per ISO week, the same for everyone with no server. The order
 * walks the hue circle and loosely the seasons: cold blues in January, greens in spring, warm in
 * summer, earth in autumn.
 */
val WEEK_KEYS = listOf(
    "glacier", "cobalt", "cherry", "periwinkle", "teal", "raspberry", "sky", "plum",
    "lemon", "mint", "powder_pink", "spring_green", "lilac", "lagoon", "peony", "grass",
    "apricot", "cornflower", "lime", "rose", "jade", "sunflower", "aqua", "coral",
    "pistachio", "turquoise", "tangerine", "cerulean", "watermelon", "butter", "emerald", "salmon",
    "ocean", "amber", "orchid", "sage", "mustard", "terracotta", "violet", "ochre",
    "pumpkin", "moss", "rust", "amethyst", "caramel", "wine", "olive", "copper",
    "petrol", "poppy", "pine", "ultramarine",
)

/** ISO 8601 week number: weeks start on Monday, and week 1 is the one with the year's first Thursday. */
fun isoWeek(date: LocalDate): Int {
    val week = (date.dayOfYear - date.dayOfWeek.isoDayNumber + 10) / 7
    return when {
        week < 1 -> weeksIn(date.year - 1)
        week > weeksIn(date.year) -> 1
        else -> week
    }
}

private fun weeksIn(year: Int): Int {
    val jan1 = LocalDate(year, 1, 1).dayOfWeek
    val leap = LocalDate(year, 12, 31).dayOfYear == 366
    return if (jan1 == DayOfWeek.THURSDAY || (leap && jan1 == DayOfWeek.WEDNESDAY)) 53 else 52
}

/** Week 53 wraps round to the first key: it is short, and the next year starts over anyway. */
fun weekColor(date: LocalDate): ColorName = BY_KEY.getValue(WEEK_KEYS[(isoWeek(date) - 1) % WEEK_KEYS.size])

fun weekHit(color: String, date: LocalDate): Boolean = deltaE(labOf(color), weekColor(date).lab) < WEEK_DELTA_E
