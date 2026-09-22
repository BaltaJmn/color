package com.baltajmn.color.social

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.color.data.Route
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

const val INVITE_BASE = "https://color.baltajmn.dev/i/"

fun inviteLink(code: String): String = INVITE_BASE + code

private val INVITE = Regex("""https?://color\.baltajmn\.dev/i/([0-9a-fA-F]{10})/?(?:[?#].*)?""")

/** The code of an invite link, or null for any other link. */
fun inviteCodeOf(url: String): String? = INVITE.matchEntire(url.trim())?.groupValues?.get(1)?.lowercase()

/** What request_friend answers. Blocked reads as Invalid: nobody learns from a link that they were blocked. */
enum class InviteResult { Sent, Accepted, Already, Self, Invalid, Limit }

@Serializable
private data class FriendshipRow(
    val a: String,
    val b: String,
    @SerialName("requested_by") val requestedBy: String,
    val status: String,
)

/**
 * Who is a friend and who asked to be (docs/tecnico.md 9.4). Only incoming requests are listed: one
 * sent and ignored stays pending forever and nobody is told, which is the point.
 */
object Friends {

    var friends by mutableStateOf<List<Profile>>(emptyList())
        private set

    var requests by mutableStateOf<List<Profile>>(emptyList())
        private set

    /** A link opened before there was a session or a name. Sent as soon as there is. */
    var pendingCode by mutableStateOf<String?>(null)

    var inviteOpen by mutableStateOf(false)

    fun opened(code: String) {
        pendingCode = code
        Route.pending = "friends"
    }

    suspend fun refresh() {
        val me = Social.userId() ?: return
        val rows = Social.client.from("friendships").select().decodeList<FriendshipRow>()
        val others = rows.map { if (it.a == me) it.b else it.a }
        val people = if (others.isEmpty()) {
            emptyMap()
        } else {
            Social.client.from("profiles")
                .select(Columns.list("id", "display_name")) { filter { isIn("id", others) } }
                .decodeList<Profile>()
                .associateBy { it.id }
        }
        friends = rows.filter { it.status == "accepted" }
            .mapNotNull { people[if (it.a == me) it.b else it.a] }
            .sortedBy { it.displayName.lowercase() }
        requests = rows.filter { it.status == "pending" && it.requestedBy != me }.mapNotNull { people[it.requestedBy] }
    }

    suspend fun request(code: String): InviteResult {
        val answer = Social.client.postgrest.rpc("request_friend", buildJsonObject { put("code", code) }).decodeAs<String>()
        refresh()
        return when (answer) {
            "sent" -> InviteResult.Sent
            "accepted" -> InviteResult.Accepted
            "already" -> InviteResult.Already
            "self" -> InviteResult.Self
            "limit" -> InviteResult.Limit
            else -> InviteResult.Invalid
        }
    }

    /** False when either of the two is already at 50: the trigger says no, and nothing changes. */
    suspend fun accept(id: String): Boolean {
        val ok = try {
            Social.client.postgrest.rpc("accept_friend", buildJsonObject { put("other", id) })
            true
        } catch (e: RestException) {
            if (e.message?.contains("friend_limit") != true) throw e
            false
        }
        refresh()
        return ok
    }

    suspend fun decline(id: String) {
        Social.client.postgrest.rpc("decline_friend", buildJsonObject { put("other", id) })
        refresh()
    }

    /** The old link stops working at once; requests it already made stay. */
    suspend fun regenerate() {
        Social.client.postgrest.rpc("regenerate_code")
        Social.loadMe()
    }

    internal fun forget() {
        friends = emptyList()
        requests = emptyList()
        pendingCode = null
    }
}
