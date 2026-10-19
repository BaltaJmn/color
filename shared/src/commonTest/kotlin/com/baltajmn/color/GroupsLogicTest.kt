package com.baltajmn.color

import com.baltajmn.color.social.FeedRow
import com.baltajmn.color.social.JoinResult
import com.baltajmn.color.social.Member
import com.baltajmn.color.social.groupCodeOf
import com.baltajmn.color.social.groupFeed
import com.baltajmn.color.social.groupLink
import com.baltajmn.color.social.inviteCodeOf
import com.baltajmn.color.social.joinResultOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Test 21 (docs/tecnico.md 10). */
class GroupsLogicTest {

    @Test
    fun groupLinksAreTheirOwn() {
        assertEquals("0a1b2c3d4e", groupCodeOf(groupLink("0a1b2c3d4e")))
        assertEquals("0a1b2c3d4e", groupCodeOf("https://color.baltajmn.dev/i/g0A1B2C3D4E/?utm=x"))
        assertNull(groupCodeOf("https://color.baltajmn.dev/i/g0a1b2c3d"))
        assertNull(groupCodeOf("https://evil.dev/i/g0a1b2c3d4e"))
        assertNull(groupCodeOf("https://color.baltajmn.dev/i/0a1b2c3d4e"))
        // A friend's link is not read as a group's, nor the other way round.
        assertNull(inviteCodeOf(groupLink("0a1b2c3d4e")))
    }

    @Test
    fun everyAnswerHasItsResult() {
        assertEquals(JoinResult.Joined, joinResultOf("joined"))
        assertEquals(JoinResult.Already, joinResultOf("already"))
        assertEquals(JoinResult.Full, joinResultOf("limit"))
        assertEquals(JoinResult.TooMany, joinResultOf("too_many"))
        assertEquals(JoinResult.Invalid, joinResultOf("not_found"))
        assertEquals(JoinResult.Invalid, joinResultOf("anything else"))
    }

    @Test
    fun aGroupShowsItsMembersButMe() {
        fun row(author: String) = FeedRow(author, "2026-10-19", "#112233", "night", updatedAt = "2026-10-19T10:00:00Z")
        val members = listOf(Member("me", "Me", true), Member("ana", "Ana", false), Member("bea", "Bea", false))
        val rows = listOf(row("me"), row("ana"), row("bea"), row("stranger"))
        assertEquals(listOf("ana"), groupFeed(rows, members, "me", hidden = listOf("bea/2026-10-19")).map { it.author })
    }
}
