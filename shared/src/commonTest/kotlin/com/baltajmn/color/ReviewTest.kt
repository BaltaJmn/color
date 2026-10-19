package com.baltajmn.color

import com.baltajmn.color.data.reachedReviewDayCount
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The in-app review prompt: a week of colors, then at most once every 120 days. */
class ReviewTriggerTest {
    private val day = 86_400_000L
    private val now = 1_000 * day

    @Test
    fun seven_days_logged_asks_for_a_review() {
        assertTrue(reachedReviewDayCount(totalDays = 7, askedAt = 0, now = now))
    }

    @Test
    fun more_than_seven_still_asks_if_it_never_fired() {
        assertTrue(reachedReviewDayCount(totalDays = 12, askedAt = 0, now = now))
    }

    @Test
    fun fewer_than_seven_does_not_ask() {
        assertFalse(reachedReviewDayCount(totalDays = 6, askedAt = 0, now = now))
    }

    @Test
    fun it_does_not_ask_again_within_120_days() {
        assertFalse(reachedReviewDayCount(totalDays = 30, askedAt = now - 119 * day, now = now))
    }

    @Test
    fun it_asks_again_after_120_days() {
        assertTrue(reachedReviewDayCount(totalDays = 30, askedAt = now - 120 * day, now = now))
    }
}
