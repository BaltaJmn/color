package com.baltajmn.color

import com.baltajmn.color.data.isFromToday
import com.baltajmn.color.data.parseExifDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class CaptureTest {
    @Test
    fun exifDatesParse() {
        assertEquals(LocalDateTime(2026, 9, 22, 18, 4, 11), parseExifDate("2026:09:22 18:04:11"))
        assertNull(parseExifDate("0000:00:00 00:00:00"))
        assertNull(parseExifDate(""))
        assertNull(parseExifDate(null))
    }

    @Test
    fun onlyTodaysPhotosAndUndatedOnesPass() {
        val today = LocalDate(2026, 9, 22)
        assertTrue(isFromToday(null, today))
        assertTrue(isFromToday(LocalDateTime(2026, 9, 22, 9, 0), today))
        // 01:30 of the 23rd still belongs to the 22nd.
        assertTrue(isFromToday(LocalDateTime(2026, 9, 23, 1, 30), today))
        assertFalse(isFromToday(LocalDateTime(2026, 9, 21, 12, 0), today))
    }
}
