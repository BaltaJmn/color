package com.baltajmn.color.data

import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.Journal
import com.baltajmn.color.model.Settings
import com.baltajmn.color.model.logicalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

/** iOS allows 64 pending requests, and one a day makes the window worth 60 days of margin. */
const val REMINDER_WINDOW = 60

/** The same local time on another day: after a daylight saving change the reminder is still at 20:00. */
private fun LocalDateTime.plusDays(n: Int) = LocalDateTime(date.plus(n, DateTimeUnit.DAY), time)

/** The first firing strictly after [now] whose logical day has no color yet. */
fun nextFire(now: LocalDateTime, hour: Int, minute: Int, done: (LocalDate) -> Boolean): LocalDateTime {
    var c = LocalDateTime(now.date, LocalTime(hour, minute))
    if (c <= now) c = c.plusDays(1)
    while (done(logicalDate(c))) c = c.plusDays(1)
    return c
}

data class Planned(val id: String, val at: LocalDateTime, val title: String, val body: String)

/**
 * The whole window at once, one request per day instead of a repeating one: a repeating reminder
 * cannot be told that today already has its color, and an app that nags after you did it is an
 * app you silence.
 */
fun reminderPlan(j: Journal, s: Settings, now: LocalDateTime): List<Planned> {
    if (!s.reminderOn) return emptyList()
    val first = nextFire(now, s.reminderHour, s.reminderMinute) { it.toString() in j }
    return (0 until REMINDER_WINDOW).map { i ->
        val at = first.plusDays(i)
        Planned(id = "reminder-${logicalDate(at)}", at = at, title = S.reminderTitle, body = S.reminderText)
    }
}
