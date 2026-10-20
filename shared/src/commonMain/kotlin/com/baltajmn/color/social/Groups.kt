package com.baltajmn.color.social

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.color.data.Route
import com.baltajmn.color.data.Storage
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Under /i/ like a friend's link, so the app links of both platforms already cover it (docs/tecnico.md 9.5). */
fun groupLink(code: String): String = INVITE_BASE + "g" + code

private val GROUP_INVITE = Regex("""https?://color\.baltajmn\.dev/i/g([0-9a-fA-F]{10})/?(?:[?#].*)?""")

/** The code of a group link, or null for any other link. */
fun groupCodeOf(url: String): String? = GROUP_INVITE.matchEntire(url.trim())?.groupValues?.get(1)?.lowercase()

/** What join_group answers. Invalid is also a group that removed me: nobody learns that from a link. */
enum class JoinResult { Joined, Already, Invalid, Full, TooMany }

internal fun joinResultOf(answer: String): JoinResult = when (answer) {
    "joined" -> JoinResult.Joined
    "already" -> JoinResult.Already
    "limit" -> JoinResult.Full
    "too_many" -> JoinResult.TooMany
    else -> JoinResult.Invalid
}

@Serializable
data class Group(
    val id: String,
    val name: String,
    @SerialName("invite_code") val inviteCode: String,
    val admin: Boolean,
)

@Serializable
data class Member(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val admin: Boolean,
)

/** group_people answers nothing to someone outside the group, and always lists me to a member. */
internal fun isMember(people: List<Member>, me: String): Boolean = people.any { it.id == me }

/** A group's cards: its members' days but mine, without the ones I reported, the last changed first. */
internal fun groupFeed(rows: List<FeedRow>, members: List<Member>, me: String, hidden: List<String>): List<FeedRow> {
    val ids = members.map { it.id }.toSet() - me
    return rows.filter { it.author in ids && it.key !in hidden }
}

/**
 * Groups of friends (docs/tecnico.md 9.5): a feed of today and yesterday shared by a few people. The
 * server only lets a member who is not a friend read those two days, so nothing here keeps more.
 */
object Groups {

    var groups by mutableStateOf<List<Group>>(emptyList())
        private set

    /** The group on screen, or null for the friends' feed. */
    var selected by mutableStateOf<String?>(null)

    var members by mutableStateOf<List<Member>>(emptyList())
        private set

    var feed by mutableStateOf<List<FeedRow>>(emptyList())
        private set

    /** A group link opened before there was a session or a name; asked about once there is. */
    var pendingCode by mutableStateOf<String?>(null)

    var inviteOpen by mutableStateOf(false)

    /** The naming dialog of a new group, and the quiet answer of the last thing done here. */
    var creating by mutableStateOf(false)
    var notice by mutableStateOf<String?>(null)

    val current: Group? get() = groups.firstOrNull { it.id == selected }

    fun opened(code: String) {
        pendingCode = code
        Route.pending = "friends"
    }

    suspend fun refresh() {
        groups = Social.client.postgrest.rpc("my_groups").decodeList<Group>()
        if (current == null) selected = null
    }

    /** False when this person is already in five groups. */
    suspend fun create(name: String): Boolean {
        val id = try {
            Social.client.postgrest.rpc("create_group", buildJsonObject { put("name", name.trim()) }).decodeAs<String>()
        } catch (e: RestException) {
            if (e.message?.contains("group_limit") != true) throw e
            return false
        }
        selected = id
        runCatching { refresh() }
        return true
    }

    /** The answer is the rpc's; the list catching up after it may fail without losing it. */
    suspend fun join(code: String): JoinResult {
        val answer = joinResultOf(Social.client.postgrest.rpc("join_group", buildJsonObject { put("code", code) }).decodeAs<String>())
        if (pendingCode == code) pendingCode = null
        runCatching { refresh() }
        if (answer == JoinResult.Joined || answer == JoinResult.Already) {
            groups.firstOrNull { it.inviteCode == code }?.let { selected = it.id }
        }
        return answer
    }

    suspend fun leave(id: String) {
        Social.client.postgrest.rpc("leave_group", buildJsonObject { put("g", id) })
        groups = groups.filter { it.id != id }
        if (selected == id) select(null)
    }

    suspend fun remove(id: String, member: String) {
        Social.client.postgrest.rpc("remove_member", buildJsonObject { put("g", id); put("other", member) })
        forgetPerson(member)
    }

    /** The old link stops working at once; nobody leaves the group for it. */
    suspend fun regenerate(id: String) {
        Social.client.postgrest.rpc("regenerate_group_code", buildJsonObject { put("g", id) })
        refresh()
    }

    fun select(id: String?) {
        if (id == selected) return
        selected = id
        members = emptyList()
        feed = emptyList()
    }

    /** docs/tecnico.md 9.5. No polling: on opening the group and on pulling down. */
    suspend fun refreshFeed(today: LocalDate) {
        val group = selected ?: return
        val me = Social.userId() ?: return
        val people = Social.client.postgrest.rpc("group_people", buildJsonObject { put("g", group) }).decodeList<Member>()
        // Removed from the group, or the group is gone: the capsule goes too, not just the cards.
        if (!isMember(people, me)) {
            refresh()
            return
        }
        val ids = people.map { it.id }.filter { it != me }
        val rows = if (ids.isEmpty()) {
            emptyList()
        } else {
            val days = listOf(today, today.minus(1, DateTimeUnit.DAY)).map { it.toString() }
            Social.client.from("shared_entries").select {
                filter {
                    isIn("author", ids)
                    isIn("day", days)
                }
                order("updated_at", Order.DESCENDING)
            }.decodeList<FeedRow>().readable()
        }
        // Another group may have been opened while this one was loading.
        if (selected != group) return
        members = people
        feed = rows
        keepPhotos()
    }

    /** Someone blocked or removed leaves the group's screen at once, before any reload. */
    fun forgetPerson(id: String) {
        members = members.filter { it.id != id }
        feed = feed.filter { it.author != id }
    }

    internal suspend fun keepPhotos() =
        withContext(Dispatchers.IO) { Storage.keepCached((Friends.feed + feed).map { it.cacheName }.toSet()) }

    internal fun forget() {
        groups = emptyList()
        selected = null
        members = emptyList()
        feed = emptyList()
        pendingCode = null
        inviteOpen = false
        creating = false
        notice = null
    }
}
