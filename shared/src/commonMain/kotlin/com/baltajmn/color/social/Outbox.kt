package com.baltajmn.color.social

import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.Storage
import com.baltajmn.color.data.reencodeJpeg
import com.baltajmn.color.data.today
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.Share
import com.baltajmn.color.model.withNothingShared
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** docs/tecnico.md 5: small enough for a feed card, and a fresh encode that carries no metadata. */
const val UPLOAD_SIDE = 720
const val UPLOAD_QUALITY = 80

/** How long the server keeps a photo; for an older day only the color is sent. */
const val PHOTO_DAYS = 7

/** The server refuses a shared day before this one (its trigger, supabase/migrations). */
val FIRST_SHARED_DAY = LocalDate(2026, 1, 1)

/** Past two days ahead of its own today the server refuses it too: room for time zones, no more. */
private const val DAYS_AHEAD = 2

/** Whether the server will take [day] at all. One it refuses is never sent, or it would block the queue for good. */
internal fun inServerRange(day: LocalDate, today: LocalDate): Boolean =
    day >= FIRST_SHARED_DAY && day <= today.plus(DAYS_AHEAD, DateTimeUnit.DAY)

/**
 * Whether the photo of [day] may go up: the server would null it at once, and the file would be
 * orphaned. The server keeps one more night of slack than this for time zones (docs/tecnico.md 9.2).
 */
internal fun photoFits(day: LocalDate, today: LocalDate): Boolean = day.daysUntil(today) <= PHOTO_DAYS

@Serializable
private data class SharedRow(
    val author: String,
    val day: String,
    val color: String,
    val name: String,
    val word: String?,
    @SerialName("photo_path") val photoPath: String?,
)

/**
 * The phone first, the server after (docs/tecnico.md 9.2). A day is saved locally and only then
 * queued; the queue is sent in order, and whatever fails stays for the next start, save or visit to
 * Friends. Each day is sent as it is now, never as it was when it was queued, so the server ends up
 * a copy of the phone however many changes piled up.
 */
object Outbox {

    private val scope by lazy { MainScope() }
    private val lock = Mutex()
    private var watching = false

    /** Fire and forget: nobody waits on the network to save a day. */
    fun kick() {
        if (!Social.available) return
        watch()
        scope.launch { flush() }
    }

    /**
     * On a cold start the session is read in another coroutine, and an old token needs the network to
     * come back: there is no uid to send under yet. The queue goes out the moment there is one.
     */
    private fun watch() {
        if (watching) return
        watching = true
        scope.launch {
            Social.client.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) runCatching { flush() }
            }
        }
    }

    suspend fun flush() = lock.withLock {
        val uid = Social.userId() ?: return@withLock
        adopt(uid)
        val today = today()
        for (day in ChromaRepository.file.outbox) {
            val entry = ChromaRepository.journal[day]
            val date = runCatching { LocalDate.parse(day) }.getOrNull()
            if (date != null && inServerRange(date, today)) {
                // ponytail: the photo is sent again on every change of the day, a word included; a
                // hash of the last upload would save it if bandwidth ever matters.
                if (runCatching { send(uid, day, date, today, entry) }.isFailure) return@withLock
            }
            ChromaRepository.synced(day, entry)
        }
    }

    /** The queue is its owner's: the first account to sign in takes it, any other starts with nothing. */
    private fun adopt(uid: String) {
        when (ChromaRepository.file.outboxOwner) {
            uid -> Unit
            null -> ChromaRepository.edit { it.copy(outboxOwner = uid) }
            else -> releaseAll(uid)
        }
    }

    /**
     * None of this phone's days is shared by [owner] and none waits to be. For a deleted account the
     * server has nothing left either; for another account signing in, the rows of the earlier one stay
     * on the server, in its own account, and this phone no longer touches them.
     */
    internal fun releaseAll(owner: String?) {
        ChromaRepository.edit { it.withNothingShared(owner) }
        // edit queues every day whose share it changed, and there is no copy for those to catch up with.
        ChromaRepository.edit { it.copy(outbox = emptyList()) }
    }

    private suspend fun send(uid: String, day: String, date: LocalDate, today: LocalDate, entry: ChromaEntry?) {
        // The name the storage policy asks for: <uid>/<YYYY-MM-DD>.jpg.
        val path = "$uid/$day.jpg"
        val photos = Social.client.storage.from("photos")
        val rows = Social.client.from("shared_entries")

        if (entry == null || entry.share == Share.Private) {
            // The row first: a friend must never be handed a path to a photo that is already gone.
            rows.delete { filter { eq("author", uid); eq("day", day) } }
            photos.delete(path)
            return
        }

        val jpeg = entry.photo.takeIf { entry.share == Share.Photo && photoFits(date, today) }
            ?.let { withContext(Dispatchers.IO) { Storage.readPhoto(it) } }
            ?.let { withContext(Dispatchers.Default) { reencodeJpeg(it, UPLOAD_SIDE, UPLOAD_QUALITY) } }
        // And the other way round here: the file is in place before any row points at it.
        if (jpeg != null) photos.upload(path, jpeg) { upsert = true } else photos.delete(path)
        rows.upsert(SharedRow(uid, day, entry.color, entry.name, entry.word, path.takeIf { jpeg != null }))
    }
}
