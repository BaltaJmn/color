package com.baltajmn.color

import com.baltajmn.color.data.shouldOfferPro
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// D9: Pro is offered once, at a week of colors, to someone free.
class ProOfferTest {
    @Test
    fun offeredAtAWeekOfColors() {
        assertTrue(shouldOfferPro(totalDays = 7, pro = false, offered = false))
        assertTrue(shouldOfferPro(totalDays = 40, pro = false, offered = false))
    }

    @Test
    fun notBeforeTheSeventhDay() {
        assertFalse(shouldOfferPro(totalDays = 6, pro = false, offered = false))
    }

    @Test
    fun onlyOnce() {
        assertFalse(shouldOfferPro(totalDays = 7, pro = false, offered = true))
        assertFalse(shouldOfferPro(totalDays = 365, pro = false, offered = true))
    }

    @Test
    fun neverToWhoAlreadyHasPro() {
        assertFalse(shouldOfferPro(totalDays = 30, pro = true, offered = false))
    }
}
