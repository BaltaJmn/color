package com.baltajmn.color

import com.baltajmn.color.data.TRIP_WINDOW_MS
import com.baltajmn.color.data.tookTrip
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** When coming back to the app asks for the lock again (docs/pantallas.md 7). */
class LockTest {

    @Test
    fun aFirstStartIsNotAReturn() = assertFalse(relocks(away = null, trip = false))

    @Test
    fun aMinuteAwayLocks() {
        assertFalse(relocks(away = 59_999, trip = false))
        assertTrue(relocks(away = 60_000, trip = false))
    }

    @Test
    fun aTripOfOurOwnWaitsTenMinutes() {
        assertFalse(relocks(away = 5 * 60_000L, trip = true))
        assertTrue(relocks(away = 10 * 60_000L, trip = true))
    }

    @Test
    fun aClockThatWentBackLocks() = assertTrue(relocks(away = -1, trip = true))

    @Test
    fun onlyAFreshTripMarkCounts() {
        assertFalse(tookTrip(startedAt = null, leftAt = 1_000))
        assertTrue(tookTrip(startedAt = 1_000, leftAt = 1_500))
        // A share sheet on iOS never took the app off screen: the next exit, much later, is a real one.
        assertFalse(tookTrip(startedAt = 1_000, leftAt = 1_000 + TRIP_WINDOW_MS + 1))
    }
}
