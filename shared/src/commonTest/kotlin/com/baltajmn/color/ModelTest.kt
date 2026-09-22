package com.baltajmn.color

import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.JournalFile
import com.baltajmn.color.model.JournalJson
import com.baltajmn.color.model.Share
import com.baltajmn.color.model.WORD_MAX
import com.baltajmn.color.model.clampCodePoints
import com.baltajmn.color.model.codePointCount
import com.baltajmn.color.model.limitEdit
import com.baltajmn.color.model.logicalDate
import com.baltajmn.color.model.withPick
import com.baltajmn.color.model.withWord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

class ModelTest {

    // Test 1: the logical day.
    @Test
    fun dayEndsAtThree() {
        assertEquals(LocalDate(2026, 9, 21), logicalDate(LocalDateTime(2026, 9, 22, 2, 59)))
        assertEquals(LocalDate(2026, 9, 22), logicalDate(LocalDateTime(2026, 9, 22, 3, 0)))
        assertEquals(LocalDate(2026, 9, 21), logicalDate(LocalDateTime(2026, 9, 22, 0, 0)))
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun cutoffIgnoresDaylightSaving() {
        val madrid = TimeZone.of("Europe/Madrid")
        // 2026-10-25: clocks go back at 03:00, so 02:30 happens twice. Both are still the 24th.
        assertEquals(LocalDate(2026, 10, 24), logicalDate(Instant.parse("2026-10-25T00:30:00Z"), madrid))
        assertEquals(LocalDate(2026, 10, 24), logicalDate(Instant.parse("2026-10-25T01:30:00Z"), madrid))
        assertEquals(LocalDate(2026, 10, 25), logicalDate(Instant.parse("2026-10-25T02:00:00Z"), madrid))
    }

    // Test 9: the word counts an emoji as one and never splits a pair.
    @Test
    fun wordLimitCountsCodePoints() {
        val smile = "🙂"
        val long = smile.repeat(WORD_MAX + 3)
        assertEquals(WORD_MAX, long.clampCodePoints(WORD_MAX).codePointCount())
        val edit = limitEdit("", long, long.length)
        assertEquals(WORD_MAX, edit.text.codePointCount())
        assertEquals(0, edit.text.length % 2)
    }

    @Test
    fun onlyTodayIsWritten() {
        val j = emptyMap<String, ChromaEntry>()
        assertNull(j.withPick("2026-09-21", "2026-09-22", "#112233", listOf("#112233"), "x", 1, Share.Private))
        val next = j.withPick("2026-09-22", "2026-09-22", "#112233", listOf("#112233"), "x", 1, Share.Color)!!
        assertEquals(Share.Color, next.getValue("2026-09-22").share)
        // Changing the pick keeps what the user chose to share.
        val again = next.withPick("2026-09-22", "2026-09-22", "#445566", listOf("#445566"), "y", 2, Share.Private)!!
        assertEquals(Share.Color, again.getValue("2026-09-22").share)
        assertEquals("#445566", again.getValue("2026-09-22").color)
    }

    @Test
    fun blankWordIsRemoved() {
        val j = mapOf("2026-09-22" to ChromaEntry(color = "#112233", name = "x", word = "rain"))
        assertNull(j.withWord("2026-09-22", "2026-09-22", "  ", 1)!!.getValue("2026-09-22").word)
        assertNull(j.withWord("2026-09-21", "2026-09-22", "sun", 1))
    }

    @Test
    fun fileRoundTrips() {
        val f = JournalFile(entries = mapOf("2026-09-22" to ChromaEntry(color = "#3A6EA5", name = "storm_blue", share = Share.Photo)))
        val text = JournalJson.encodeToString(JournalFile.serializer(), f)
        assertEquals(f, JournalJson.decodeFromString(JournalFile.serializer(), text))
        assert("\"share\":\"photo\"" in text)
    }
}
