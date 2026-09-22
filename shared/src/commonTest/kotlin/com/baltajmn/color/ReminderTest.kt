package com.baltajmn.color

import com.baltajmn.color.data.REMINDER_WINDOW
import com.baltajmn.color.data.nextFire
import com.baltajmn.color.data.reminderPlan
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.Settings
import com.baltajmn.color.model.logicalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

// 13. The reminder. docs/tecnico.md 6.6
class ReminderTest {

    @Test
    fun theAlarmSkipsADayThatAlreadyHasItsColor() {
        val done = setOf("2027-01-17")
        fun fire(at: String) = nextFire(LocalDateTime.parse(at), 20, 0) { it.toString() in done }
        assertEquals(LocalDateTime.parse("2027-01-16T20:00"), fire("2027-01-16T19:59"))
        assertEquals(LocalDateTime.parse("2027-01-18T20:00"), fire("2027-01-17T19:59"))
        assertEquals(LocalDateTime.parse("2027-01-18T20:00"), fire("2027-01-16T20:00"))
    }

    @Test
    fun theWindowStartsTodayOnlyWhileTodayIsBlank() {
        val on = Settings(reminderOn = true)
        val blank = reminderPlan(emptyMap(), on, LocalDateTime.parse("2027-01-17T10:00"))
        assertEquals(REMINDER_WINDOW, blank.size)
        assertEquals(LocalDateTime.parse("2027-01-17T20:00"), blank.first().at)
        assertEquals(LocalDateTime.parse("2027-03-17T20:00"), blank.last().at)

        val done = mapOf("2027-01-17" to ChromaEntry(color = "#3A6EA5", name = "storm_blue"))
        assertEquals(
            LocalDateTime.parse("2027-01-18T20:00"),
            reminderPlan(done, on, LocalDateTime.parse("2027-01-17T10:00")).first().at,
        )
        assertTrue(reminderPlan(emptyMap(), Settings(), LocalDateTime.parse("2027-01-17T10:00")).isEmpty())
    }

    @Test
    fun anEarlyReminderBelongsToTheDayThatIsEnding() {
        val plan = reminderPlan(
            emptyMap(),
            Settings(reminderOn = true, reminderHour = 1, reminderMinute = 30),
            LocalDateTime.parse("2027-01-17T10:00"),
        )
        assertEquals(LocalDateTime.parse("2027-01-18T01:30"), plan.first().at)
        assertEquals(LocalDate.parse("2027-01-17"), logicalDate(plan.first().at))
        assertEquals("reminder-2027-01-17", plan.first().id)
    }
}
