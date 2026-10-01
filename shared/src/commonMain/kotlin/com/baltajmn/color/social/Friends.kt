package com.baltajmn.color.social

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.baltajmn.color.color.deltaE
import com.baltajmn.color.color.labOf
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.Route
import com.baltajmn.color.data.Storage
import com.baltajmn.color.data.decodeImage
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.FriendsToday
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
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

/**
 * What request_friend answers. Blocked reads as Invalid: nobody learns from a link that they were
 * blocked. TooMany is the server's brake on guessing codes: a few too many that never existed.
 */
enum class InviteResult { Sent, Accepted, Already, Self, Invalid, Limit, TooMany }

internal fun inviteResultOf(answer: String): InviteResult = when (answer) {
    "sent" -> InviteResult.Sent
    "accepted" -> InviteResult.Accepted
    "already" -> InviteResult.Already
    "self" -> InviteResult.Self
    "limit" -> InviteResult.Limit
    "too_many" -> InviteResult.TooMany
    else -> InviteResult.Invalid
}

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
    /** Null for a day that does not parse: the server hands back whatever a modified client stored. */
    val dateOrNull: LocalDate? get() = runCatching { LocalDate.parse(day) }.getOrNull()

    /** Only for rows that went through [readable]. */
    val date: LocalDate get() = LocalDate.parse(day)

    fun entry() = ChromaEntry(color = color, name = name, word = word)

    /** How a report remembers this day: `Settings.hiddenCards`. */
    val key: String get() = "$author/$day"

    /** Changes with every edit, so a replaced photo is never served from the cache. */
    val cacheName: String get() = "$author-$day-${updatedAt.filter(Char::isDigit)}.jpg"
}

/**
 * The menu of a friend, or of one of their days, open over everything. A [request] is someone who
 * is not a friend yet: there is only one thing to do about them besides answering, and it opens at once.
 */
data class Acting(val person: Profile, val day: FeedRow? = null, val request: Boolean = false)

/** Rows a day the server stored in a form that does not parse are dropped: they would crash the screen that shows them. */
internal fun List<FeedRow>.readable(): List<FeedRow> = filter { it.dateOrNull != null }

@Serializable
private data class NewReport(val reporter: String, val author: String, val day: String)

@Serializable
private data class DayRow(val day: String)

/** One line of my_friendships: [id] is the other person, and the name comes already joined. */
@Serializable
internal data class FriendshipRow(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val status: String,
    @SerialName("requested_by") val requestedBy: String,
)

/** Friends by name, and the requests made to [me]: one sent and ignored stays out of both. */
internal fun splitFriendships(me: String, rows: List<FriendshipRow>): Pair<List<Profile>, List<Profile>> {
    fun people(of: List<FriendshipRow>) = of.map { Profile(it.id, it.displayName) }.sortedBy { it.displayName.lowercase() }
    return people(rows.filter { it.status == "accepted" }) to
        people(rows.filter { it.status == "pending" && it.requestedBy != me })
}

/** The server has no day before this one (the same bound as its trigger). */
private const val FIRST_DAY = "2026-01-01"

/** Postgres' unique_violation: a report already made. */
private const val UNIQUE_VIOLATION = "23505"

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

    /**
     * Moves on every feed load, and nothing shows it: a card whose photo could not be fetched asks
     * again with it, so pulling down is also how a picture that failed comes back.
     */
    var refreshes by mutableStateOf(0)
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

    /** One call, joined on the server: names by id in a URL would stop fitting with a few hundred requests. */
    suspend fun refresh() {
        val me = Social.userId() ?: return
        val (accepted, asked) = splitFriendships(me, Social.client.postgrest.rpc("my_friendships").decodeList<FriendshipRow>())
        friends = accepted
        requests = asked
    }

    /** docs/tecnico.md 9.3. No polling: this runs on opening Friends and on pulling down. */
    suspend fun refreshFeed(today: LocalDate) {
        val me = Social.userId() ?: return
        val days = listOf(today, today.minus(1, DateTimeUnit.DAY)).map { it.toString() }
        // Before the cards are replaced, so one that is still without its photo asks again in the same pass.
        refreshes++
        feed = Social.client.from("shared_entries").select {
            filter {
                isIn("day", days)
                neq("author", me)
            }
            order("updated_at", Order.DESCENDING)
        }.decodeList<FeedRow>().readable()
        withContext(Dispatchers.IO) { Storage.keepCached(feed.map { it.cacheName }.toSet()) }
        syncWidgetStrip(today.toString())
    }

    /**
     * Downloaded once and kept in the cache; null without a photo or when it cannot be fetched. The
     * cache is only a convenience, so nothing it does wrong (a full disk, a file the cleanup removed
     * under a read, one left half written) costs the picture or closes the app: a copy that does not
     * decode is fetched again and written over.
     */
    suspend fun photo(row: FeedRow): ImageBitmap? = withContext(Dispatchers.IO) {
        val path = row.photoPath ?: return@withContext null
        runCatching { Storage.readCached(row.cacheName)?.let(::decodeImage) }.getOrNull()?.let { return@withContext it }
        runCatching {
            val bytes = Social.client.storage.from("photos").downloadAuthenticated(path)
            val image = decodeImage(bytes) ?: return@runCatching null
            runCatching { Storage.writeCached(row.cacheName, bytes) }
            image
        }.getOrNull()
    }

    /**
     * One friend's days of [year], in order. A year at a time: the server answers 1000 rows at most
     * and starts from the oldest, so a long history would lose the very year that is on screen.
     */
    suspend fun year(id: String, year: Int): List<FeedRow> =
        Social.client.from("shared_entries").select {
            filter {
                eq("author", id)
                gte("day", "${year.toString().padStart(4, '0')}-01-01")
                lt("day", "${(year + 1).toString().padStart(4, '0')}-01-01")
            }
            order("day", Order.ASCENDING)
        }.decodeList<FeedRow>().readable()

    /** The first year this friend shared anything, or null when there is nothing yet. */
    suspend fun firstYear(id: String): Int? =
        Social.client.from("shared_entries").select(Columns.list("day")) {
            filter {
                eq("author", id)
                gte("day", FIRST_DAY)
            }
            order("day", Order.ASCENDING)
            limit(1)
        }.decodeList<DayRow>().firstOrNull()?.day?.take(4)?.toIntOrNull()

    /**
     * The answer is the rpc's. What follows it is only the lists catching up: a network that drops
     * in between must not turn a request that went out into "no connection" and lose its answer.
     */
    suspend fun request(code: String): InviteResult {
        val answer = Social.client.postgrest.rpc("request_friend", buildJsonObject { put("code", code) }).decodeAs<String>()
        // Spent from here on, whatever the refresh does; a newer link opened meanwhile is not.
        if (pendingCode == code) pendingCode = null
        runCatching { refresh() }
        return inviteResultOf(answer)
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
        // Answered here too, so a refresh lost to the network does not leave the request to tap again.
        if (ok) {
            val person = requests.firstOrNull { it.id == id }
            requests = requests.filter { it.id != id }
            if (person != null) friends = (friends + person).sortedBy { it.displayName.lowercase() }
        }
        runCatching { refresh() }
        return ok
    }

    suspend fun decline(id: String) {
        Social.client.postgrest.rpc("decline_friend", buildJsonObject { put("other", id) })
        requests = requests.filter { it.id != id }
        runCatching { refresh() }
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

    /** Who this person has blocked, by name: the server joins it, profiles of strangers are not readable. */
    suspend fun blocked(): List<Profile> =
        Social.client.postgrest.rpc("my_blocks").decodeList<Profile>().sortedBy { it.displayName.lowercase() }

    /** Nobody is told. It only lets requests between the two work again; they are not friends again. */
    suspend fun unblock(id: String) {
        Social.client.postgrest.rpc("unblock_user", buildJsonObject { put("other", id) })
    }

    /**
     * The report-notify function mails it on insert; the reader only ever inserts. Reporting the same
     * day twice is not a failure: the server keeps one, and the card is hidden either way.
     */
    suspend fun report(row: FeedRow) {
        val me = Social.userId() ?: return
        try {
            Social.client.from("reports").insert(NewReport(me, row.author, row.day))
        } catch (e: PostgrestRestException) {
            if (e.code != UNIQUE_VIOLATION) throw e
        }
    }

    /**
     * The person goes from the screen, the strip and the lists before the network is asked again: the
     * rpc has already worked, and a refresh that fails after it must not leave their color on the
     * widget or call the whole thing an error.
     */
    private suspend fun forgetPerson(id: String) {
        feed = feed.filter { it.author != id }
        friends = friends.filter { it.id != id }
        requests = requests.filter { it.id != id }
        if (viewing?.id == id) {
            viewing = null
            viewingDay = null
        }
        resyncWidgetStrip()
        runCatching { refresh() }
    }

    /**
     * The widget's strip: the same colors as the circle's palette, earliest first, and no names.
     * Someone removed, blocked or reported leaves it at once, not at the next load.
     */
    private fun syncWidgetStrip(day: String) {
        val shown = friends.map { it.id }.toSet()
        val hidden = ChromaRepository.settings.hiddenCards
        val colors = feed.filter { it.day == day && it.author in shown && it.key !in hidden }.asReversed().map { it.color }
        ChromaRepository.updateFriendsToday(FriendsToday(day, colors))
    }

    fun resyncWidgetStrip() {
        ChromaRepository.file.friendsToday?.let { syncWidgetStrip(it.date) }
    }

    /** The old link stops working at once; requests it already made stay. */
    suspend fun regenerate() {
        Social.client.postgrest.rpc("regenerate_code")
        Social.loadMe()
    }

    internal suspend fun forget() {
        friends = emptyList()
        requests = emptyList()
        feed = emptyList()
        pendingCode = null
        listOpen = false
        viewing = null
        viewingDay = null
        acting = null
        withContext(Dispatchers.IO) { Storage.keepCached(emptySet()) }
        ChromaRepository.updateFriendsToday(null)
    }
}
