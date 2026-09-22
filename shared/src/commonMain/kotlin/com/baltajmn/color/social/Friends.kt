package com.baltajmn.color.social

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.baltajmn.color.color.deltaE
import com.baltajmn.color.color.labOf
import com.baltajmn.color.data.Route
import com.baltajmn.color.data.Storage
import com.baltajmn.color.data.decodeImage
import com.baltajmn.color.model.ChromaEntry
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
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

const val INVITE_BASE = "https://color.baltajmn.dev/i/"

/** A knob (docs/tecnico.md 6.11): low enough that being in tune feels like chance, not a daily thing. */
const val SYNC_DELTA_E = 5.0

/** Two people picked almost the same color on the same day. Worked out on the phone, never stored. */
fun inTune(mine: String, theirs: String): Boolean = deltaE(labOf(mine), labOf(theirs)) < SYNC_DELTA_E

fun inviteLink(code: String): String = INVITE_BASE + code

private val INVITE = Regex("""https?://color\.baltajmn\.dev/i/([0-9a-fA-F]{10})/?(?:[?#].*)?""")

/** The code of an invite link, or null for any other link. */
fun inviteCodeOf(url: String): String? = INVITE.matchEntire(url.trim())?.groupValues?.get(1)?.lowercase()

/** What request_friend answers. Blocked reads as Invalid: nobody learns from a link that they were blocked. */
enum class InviteResult { Sent, Accepted, Already, Self, Invalid, Limit }

/** A friend's day as the server has it. `day` is the author's logical day, not the reader's. */
@Serializable
data class FeedRow(
    val author: String,
    val day: String,
    val color: String,
    val name: String,
    val word: String? = null,
    @SerialName("photo_path") val photoPath: String? = null,
    @SerialName("updated_at") val updatedAt: String,
) {
    val date: LocalDate get() = LocalDate.parse(day)

    fun entry() = ChromaEntry(color = color, name = name, word = word)

    /** How a report remembers this day: `Settings.hiddenCards`. */
    val key: String get() = "$author/$day"

    /** Changes with every edit, so a replaced photo is never served from the cache. */
    val cacheName: String get() = "$author-$day-${updatedAt.filter(Char::isDigit)}.jpg"
}

/** The menu of a friend, or of one of their days, open over everything. */
data class Acting(val person: Profile, val day: FeedRow? = null)

@Serializable
private data class NewReport(val reporter: String, val author: String, val day: String)

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

    /** Friends' days that fall on the reader's today or yesterday, the last changed first. */
    var feed by mutableStateOf<List<FeedRow>>(emptyList())
        private set

    var inviteOpen by mutableStateOf(false)
    var listOpen by mutableStateOf(false)

    /** Whose year is open, and which of their days. */
    var viewing by mutableStateOf<Profile?>(null)
    var viewingDay by mutableStateOf<FeedRow?>(null)

    /** Kept here and drawn by App, so closing the screen under it never cancels a report halfway. */
    var acting by mutableStateOf<Acting?>(null)

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

    /** docs/tecnico.md 9.3. No polling: this runs on opening Friends and on pulling down. */
    suspend fun refreshFeed(today: LocalDate) {
        val me = Social.userId() ?: return
        val days = listOf(today, today.minus(1, DateTimeUnit.DAY)).map { it.toString() }
        feed = Social.client.from("shared_entries").select {
            filter {
                isIn("day", days)
                neq("author", me)
            }
            order("updated_at", Order.DESCENDING)
        }.decodeList<FeedRow>()
        withContext(Dispatchers.IO) { Storage.keepCached(feed.map { it.cacheName }.toSet()) }
    }

    /** Downloaded once and kept in the cache; null without a photo or when it cannot be fetched. */
    suspend fun photo(row: FeedRow): ImageBitmap? = withContext(Dispatchers.IO) {
        val path = row.photoPath ?: return@withContext null
        val bytes = Storage.readCached(row.cacheName)
            ?: runCatching { Social.client.storage.from("photos").downloadAuthenticated(path) }.getOrNull()
                ?.also { Storage.writeCached(row.cacheName, it) }
        bytes?.let(::decodeImage)
    }

    /** Everything one friend has shared, for their year. */
    suspend fun year(id: String): List<FeedRow> =
        Social.client.from("shared_entries").select { filter { eq("author", id) } }.decodeList<FeedRow>()

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

    /** Nobody is told: their days just stop arriving, and so do yours to them. */
    suspend fun remove(id: String) {
        Social.client.postgrest.rpc("remove_friend", buildJsonObject { put("other", id) })
        forgetPerson(id)
    }

    /** Removes too, and the server refuses any request between the two from then on. */
    suspend fun block(id: String) {
        Social.client.postgrest.rpc("block_user", buildJsonObject { put("other", id) })
        forgetPerson(id)
    }

    /** The report-notify function mails it on insert; the reader only ever inserts. */
    suspend fun report(row: FeedRow) {
        val me = Social.userId() ?: return
        Social.client.from("reports").insert(NewReport(me, row.author, row.day))
    }

    private suspend fun forgetPerson(id: String) {
        feed = feed.filter { it.author != id }
        if (viewing?.id == id) {
            viewing = null
            viewingDay = null
        }
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
        feed = emptyList()
        pendingCode = null
        listOpen = false
        viewing = null
        viewingDay = null
        acting = null
        Storage.keepCached(emptySet())
    }
}
