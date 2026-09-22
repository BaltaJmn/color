package com.baltajmn.color

import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.Share
import com.baltajmn.color.model.sharedChanges
import kotlin.test.Test
import kotlin.test.assertEquals

// docs/tecnico.md 9.2: what the server has to hear about, and what it never does.
class OutboxTest {

    private fun day(share: Share, color: String = "#3A6EA5") = ChromaEntry(color = color, name = "x", share = share)

    @Test
    fun aPrivateDayNeverReachesTheQueue() {
        assertEquals(emptyList(), sharedChanges(emptyMap(), mapOf("2027-01-17" to day(Share.Private))))
        assertEquals(
            emptyList(),
            sharedChanges(mapOf("2027-01-17" to day(Share.Private)), mapOf("2027-01-17" to day(Share.Private, "#112233"))),
        )
    }

    @Test
    fun sharingChangingUnsharingAndDeletingAreAllQueued() {
        val private = mapOf("2027-01-17" to day(Share.Private))
        val color = mapOf("2027-01-17" to day(Share.Color))
        val recolored = mapOf("2027-01-17" to day(Share.Color, "#112233"))
        assertEquals(listOf("2027-01-17"), sharedChanges(private, color))
        assertEquals(listOf("2027-01-17"), sharedChanges(color, recolored))
        assertEquals(listOf("2027-01-17"), sharedChanges(color, private))
        assertEquals(listOf("2027-01-17"), sharedChanges(color, emptyMap()))
        assertEquals(emptyList(), sharedChanges(color, color))
    }
}
