package com.baltajmn.color

import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.JournalFile
import com.baltajmn.color.model.JournalJson
import com.baltajmn.color.model.Share
import com.baltajmn.color.model.withNothingShared
import com.baltajmn.color.social.FeedRow
import com.baltajmn.color.social.FriendshipRow
import com.baltajmn.color.social.InviteResult
import com.baltajmn.color.social.PHOTO_DAYS
import com.baltajmn.color.social.inServerRange
import com.baltajmn.color.social.inviteResultOf
import com.baltajmn.color.social.photoFits
import com.baltajmn.color.social.readable
import com.baltajmn.color.social.sessionLost
import com.baltajmn.color.social.splitFriendships
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The pure parts of Friends and the queue (docs/tecnico.md 8 and 9): no session, no network.
class FriendsLogicTest {

    private val me = "me"

    private fun line(id: String, name: String, status: String, by: String) = FriendshipRow(id, name, status, by)

    @Test
    fun friendsComeSortedByNameWithoutCaseAndRequestsOnlyThoseMadeToMe() {
        val (friends, requests) = splitFriendships(
            me,
            listOf(
                line("b", "bea", "accepted", "b"),
                line("a", "Ana", "accepted", me),
                line("c", "Carla", "pending", "c"),
                // Sent by me and never answered: listed nowhere, nobody is told.
                line("d", "Dario", "pending", me),
            ),
        )
        assertEquals(listOf("a", "b"), friends.map { it.id })
        assertEquals(listOf("Ana", "bea"), friends.map { it.displayName })
        assertEquals(listOf("c"), requests.map { it.id })
    }

    @Test
    fun noFriendshipsIsNoFriendsAndNoRequests() {
        val (friends, requests) = splitFriendships(me, emptyList())
        assertTrue(friends.isEmpty() && requests.isEmpty())
    }

    @Test
    fun everyAnswerOfRequestFriendHasItsResult() {
        assertEquals(InviteResult.Sent, inviteResultOf("sent"))
        assertEquals(InviteResult.Accepted, inviteResultOf("accepted"))
        assertEquals(InviteResult.Already, inviteResultOf("already"))
        assertEquals(InviteResult.Self, inviteResultOf("self"))
        assertEquals(InviteResult.Limit, inviteResultOf("limit"))
        assertEquals(InviteResult.TooMany, inviteResultOf("too_many"))
        // A block and a code that never existed answer alike: nobody learns they were blocked.
        assertEquals(InviteResult.Invalid, inviteResultOf("not_found"))
        assertEquals(InviteResult.Invalid, inviteResultOf("blocked"))
    }

    private fun row(day: String) = FeedRow("x", day, "#112233", "azul", null, null, "2027-01-01T00:00:00Z")

    @Test
    fun aDayThatDoesNotParseIsDroppedInsteadOfBreakingTheScreen() {
        val rows = listOf(row("2027-03-15"), row("0044-03-15 BC"), row("10000-01-01"), row("nonsense"))
        assertNull(row("nonsense").dateOrNull)
        assertEquals(listOf("2027-03-15"), rows.readable().map { it.day })
    }

    private val today = LocalDate(2027, 6, 20)

    @Test
    fun theServerTakesFromItsFirstDayToTwoDaysAhead() {
        assertTrue(inServerRange(LocalDate(2026, 1, 1), today))
        assertFalse(inServerRange(LocalDate(2025, 12, 31), today))
        assertTrue(inServerRange(today, today))
        assertTrue(inServerRange(today.plus(2, DateTimeUnit.DAY), today))
        assertFalse(inServerRange(today.plus(3, DateTimeUnit.DAY), today))
    }

    @Test
    fun aPhotoOlderThanItsWeekGoesUpAsColorOnly() {
        assertTrue(photoFits(today, today))
        assertTrue(photoFits(today.minus(PHOTO_DAYS, DateTimeUnit.DAY), today))
        assertFalse(photoFits(today.minus(PHOTO_DAYS + 1, DateTimeUnit.DAY), today))
    }

    private fun entry(share: Share) = ChromaEntry(color = "#3A6EA5", name = "x", share = share, at = 5)

    @Test
    fun anotherAccountStartsWithNothingSharedAndNothingQueued() {
        val file = JournalFile(
            entries = mapOf("2027-06-18" to entry(Share.Photo), "2027-06-19" to entry(Share.Private), "2027-06-20" to entry(Share.Color)),
            outbox = listOf("2027-06-19", "2027-06-20"),
            outboxOwner = "ana",
        )
        val next = file.withNothingShared("bea")
        assertEquals("bea", next.outboxOwner)
        assertEquals(emptyList(), next.outbox)
        assertTrue(next.entries.values.all { it.share == Share.Private })
        // The days themselves are untouched: only who sees them changes.
        assertEquals(file.entries.mapValues { it.value.color to it.value.at }, next.entries.mapValues { it.value.color to it.value.at })
    }

    @Test
    fun theOwnerOfTheQueueSurvivesTheFileAndOldFilesHaveNone() {
        val file = JournalFile(outbox = listOf("2027-06-20"), outboxOwner = "ana")
        val back = JournalJson.decodeFromString(JournalFile.serializer(), JournalJson.encodeToString(JournalFile.serializer(), file))
        assertEquals("ana", back.outboxOwner)
        assertNull(JournalJson.decodeFromString(JournalFile.serializer(), """{"version":1,"entries":{}}""").outboxOwner)
    }

    private val live = SessionStatus.Authenticated(UserSession("access", "refresh", expiresIn = 3600, tokenType = "bearer"))
    private val offline = SessionStatus.RefreshFailure(RefreshFailureCause.NetworkError(Exception("offline")))

    @Test
    fun aSessionTheClientDropsIsLostButTheStartOfAPhoneWithoutAccountIsNot() {
        // Revoked refresh token or user deleted from the dashboard: the phone had one and has none now.
        assertTrue(sessionLost(live, SessionStatus.NotAuthenticated(isSignOut = true)))
        assertTrue(sessionLost(live, SessionStatus.NotAuthenticated()))
        // Back from a cold start offline, and rejected once the network came: it had a session too.
        assertTrue(sessionLost(offline, SessionStatus.NotAuthenticated()))
        // A stored session the server rejects at start never shows an Authenticated before it.
        assertTrue(sessionLost(SessionStatus.Initializing, SessionStatus.NotAuthenticated(isSignOut = true)))
        assertTrue(sessionLost(null, SessionStatus.NotAuthenticated(isSignOut = true)))
        // A phone that never had an account starts at NotAuthenticated, with nothing to forget.
        assertFalse(sessionLost(null, SessionStatus.NotAuthenticated()))
        assertFalse(sessionLost(SessionStatus.Initializing, SessionStatus.NotAuthenticated()))
        // Signing in, renewing and falling offline are not losses.
        assertFalse(sessionLost(SessionStatus.NotAuthenticated(), live))
        assertFalse(sessionLost(live, live))
        assertFalse(sessionLost(live, offline))
        assertFalse(sessionLost(offline, live))
        assertFalse(sessionLost(SessionStatus.Initializing, live))
    }
}
