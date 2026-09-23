package com.baltajmn.color

import com.baltajmn.color.color.COLOR_NAMES
import com.baltajmn.color.color.WEEK_KEYS
import com.baltajmn.color.color.isoWeek
import com.baltajmn.color.color.weekColor
import com.baltajmn.color.color.weekHit
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// 16. docs/tecnico.md 6.12.
class WeekTest {

    @Test
    fun weekOneAndWeekFiftyThree() {
        // 2026 starts on a Thursday, so it has 53 weeks; 2021 started on a Friday, still in 2020's 53rd.
        assertEquals(1, isoWeek(LocalDate(2026, 1, 1)))
        assertEquals(53, isoWeek(LocalDate(2026, 12, 31)))
        assertEquals(53, isoWeek(LocalDate(2021, 1, 3)))
        assertEquals(1, isoWeek(LocalDate(2021, 1, 4)))
        // A Monday 29 December already belongs to the next year's week 1.
        assertEquals(1, isoWeek(LocalDate(2025, 12, 29)))
        // Week 53 wraps to the first color, the same as week 1.
        assertEquals(WEEK_KEYS.first(), weekColor(LocalDate(2026, 12, 31)).key)
        assertEquals(WEEK_KEYS.first(), weekColor(LocalDate(2026, 1, 1)).key)
        assertEquals(WEEK_KEYS[51], weekColor(LocalDate(2026, 12, 24)).key)
    }

    @Test
    fun fiftyTwoDistinctKeysFromTheTable() {
        val keys = COLOR_NAMES.map { it.key }.toSet()
        assertEquals(52, WEEK_KEYS.size)
        assertEquals(52, WEEK_KEYS.toSet().size)
        assertTrue(WEEK_KEYS.all { it in keys })
    }

    @Test
    fun theWeekColorItselfIsAHitAndItsOppositeIsNot() {
        val day = LocalDate(2026, 1, 5)
        assertTrue(weekHit(weekColor(day).hex, day))
        assertFalse(weekHit("#F6C324", day))
    }
}
