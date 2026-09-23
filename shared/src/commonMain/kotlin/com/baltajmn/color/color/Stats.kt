package com.baltajmn.color.color

import com.baltajmn.color.model.ChromaEntry
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos

/** Days a month or a season needs before it can be the warmest, the coldest or the greyest. */
const val STATS_MIN_DAYS = 3

/** Days each of two years needs before they are compared. */
const val STATS_MIN_YEAR_DAYS = 20

/** A difference in mean warmth smaller than this reads as "about the same year". */
const val STATS_ALIKE = 3.0

/** The Lab hue that counts as warmest, between red and orange; its opposite, a blue, is coldest. */
private const val WARM_HUE = 50.0 * PI / 180.0

/**
 * How warm a color looks: its chroma projected on the warm hue. Greys are near zero whatever their
 * lightness, so a grey month is neither warm nor cold, which is what it looks like.
 */
fun warmth(lab: Lab): Double = lab.chroma * cos(atan2(lab.b, lab.a) - WARM_HUE)

/** Meteorological seasons by month; the interface names them by their months, not by hemisphere. */
enum class Season(val months: List<Int>) {
    DecFeb(listOf(12, 1, 2)),
    MarMay(listOf(3, 4, 5)),
    JunAug(listOf(6, 7, 8)),
    SepNov(listOf(9, 10, 11)),
}

enum class Versus { Warmer, Cooler, Alike }

/** Each null when there are too few days to say it honestly. */
data class YearStats(
    val warmest: Int? = null,
    val coldest: Int? = null,
    val repeated: String? = null,
    val greyest: Season? = null,
    val versus: Versus? = null,
) {
    val isEmpty: Boolean get() = this == YearStats()
}

/** docs/tecnico.md 6.13. Deterministic: ties go to the earliest day. */
fun yearStats(year: Int, journal: Map<String, ChromaEntry>): YearStats {
    fun daysOf(y: Int) = journal.entries.filter { it.key.startsWith("$y-") }.sortedBy { it.key }
    fun meanWarmth(days: List<Map.Entry<String, ChromaEntry>>) = days.map { warmth(labOf(it.value.color)) }.average()
    val days = daysOf(year)

    val months = days.groupBy { it.key.substring(5, 7).toInt() }
        .filterValues { it.size >= STATS_MIN_DAYS }
        .mapValues { meanWarmth(it.value) }
        .takeIf { it.size >= 2 }

    val repeated = days.groupingBy { it.value.name }.eachCount()
        .filterValues { it >= 2 }
        .maxByOrNull { it.value }?.key

    val greyest = days.groupBy { day -> Season.entries.first { day.key.substring(5, 7).toInt() in it.months } }
        .filterValues { it.size >= STATS_MIN_DAYS }
        .mapValues { season -> season.value.map { labOf(it.value.color).chroma }.average() }
        .takeIf { it.size >= 2 }
        ?.minByOrNull { it.value }?.key

    val before = daysOf(year - 1)
    val versus = if (days.size >= STATS_MIN_YEAR_DAYS && before.size >= STATS_MIN_YEAR_DAYS) {
        val difference = meanWarmth(days) - meanWarmth(before)
        when {
            difference >= STATS_ALIKE -> Versus.Warmer
            difference <= -STATS_ALIKE -> Versus.Cooler
            else -> Versus.Alike
        }
    } else {
        null
    }

    return YearStats(
        warmest = months?.maxByOrNull { it.value }?.key,
        coldest = months?.minByOrNull { it.value }?.key,
        repeated = repeated,
        greyest = greyest,
        versus = versus,
    )
}
