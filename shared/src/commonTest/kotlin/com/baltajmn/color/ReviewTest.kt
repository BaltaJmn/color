package com.baltajmn.color

import com.baltajmn.color.data.reachedReviewDayCount
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The once-ever in-app review prompt: fires exactly when the journal first reaches a week. */
class ReviewTriggerTest {

    @Test
    fun seven_days_logged_asks_for_a_review() {
        assertTrue(reachedReviewDayCount(totalDays = 7, alreadyRequested = false))
    }

    @Test
    fun more_than_seven_still_asks_if_it_never_fired() {
        assertTrue(reachedReviewDayCount(totalDays = 12, alreadyRequested = false))
    }

    @Test
    fun fewer_than_seven_does_not_ask() {
        assertFalse(reachedReviewDayCount(totalDays = 6, alreadyRequested = false))
    }

    @Test
    fun it_never_fires_twice() {
        assertFalse(reachedReviewDayCount(totalDays = 30, alreadyRequested = true))
    }
}
