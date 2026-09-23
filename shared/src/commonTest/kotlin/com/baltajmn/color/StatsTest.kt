package com.baltajmn.color

import com.baltajmn.color.color.Season
import com.baltajmn.color.color.Versus
import com.baltajmn.color.color.labOf
import com.baltajmn.color.color.warmth
import com.baltajmn.color.color.yearStats
import com.baltajmn.color.model.ChromaEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// 18. docs/tecnico.md 6.13.
class StatsTest {

    private fun day(color: String, name: String = color) = ChromaEntry(color = color, name = name)

    private fun month(year: Int, month: Int, color: String, n: Int = 3, name: String = color) =
        (1..n).associate { "$year-${month.toString().padStart(2, '0')}-${it.toString().padStart(2, '0')}" to day(color, name) }

    @Test
    fun warmthOrdersOrangeOverGreyOverBlue() {
        assertTrue(warmth(labOf("#EC6E0E")) > warmth(labOf("#96968F")))
        assertTrue(warmth(labOf("#96968F")) > warmth(labOf("#1F47B8")))
    }

    @Test
    fun warmestColdestRepeatedAndGreyest() {
        val journal = month(2026, 1, "#96968F", name = "concrete") +
            month(2026, 4, "#4AA548", name = "grass") +
            month(2026, 7, "#EC6E0E", n = 4, name = "orange") +
            month(2026, 10, "#1F47B8", name = "cobalt")
        val stats = yearStats(2026, journal)
        assertEquals(7, stats.warmest)
        assertEquals(10, stats.coldest)
        assertEquals("orange", stats.repeated)
        assertEquals(Season.DecFeb, stats.greyest)
        assertNull(stats.versus)
    }

    @Test
    fun tooFewDaysSayNothing() {
        // Two days a month and every name different: nothing reaches a minimum.
        val journal = mapOf(
            "2026-03-01" to day("#EC6E0E", "orange"),
            "2026-03-02" to day("#F28C28", "tangerine"),
            "2026-08-01" to day("#1F47B8", "cobalt"),
            "2026-08-02" to day("#1D5A94", "ocean"),
        )
        val stats = yearStats(2026, journal)
        assertNull(stats.warmest)
        assertNull(stats.greyest)
        assertTrue(stats.isEmpty)
    }

    @Test
    fun aWarmerYearThanTheOneBefore() {
        val before = (1..7).map { month(2025, it, "#1F47B8") }.reduce { a, b -> a + b }
        val now = (1..7).map { month(2026, it, "#EC6E0E") }.reduce { a, b -> a + b }
        assertEquals(Versus.Warmer, yearStats(2026, before + now).versus)
        assertEquals(Versus.Cooler, yearStats(2025, before + now + (1..7).map { month(2024, it, "#EC6E0E") }.reduce { a, b -> a + b }).versus)
        assertEquals(Versus.Alike, yearStats(2026, now + (1..7).map { month(2025, it, "#EC6E0E") }.reduce { a, b -> a + b }).versus)
    }
}
