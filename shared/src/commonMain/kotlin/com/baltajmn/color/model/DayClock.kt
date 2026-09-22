package com.baltajmn.color.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/** A photo of Saturday's dinner at 01:30 still belongs to Saturday. */
const val DAY_CUTOFF_HOUR = 3

@OptIn(ExperimentalTime::class)
fun logicalDate(now: Instant, tz: TimeZone): LocalDate = logicalDate(now.toLocalDateTime(tz))

// The local wall-clock hour is compared, never an instant minus three hours: a DST change does not
// move the cutoff.
fun logicalDate(local: LocalDateTime): LocalDate =
    if (local.hour < DAY_CUTOFF_HOUR) local.date.minus(1, DateTimeUnit.DAY) else local.date

fun LocalDate.isoKey(): String = toString()
