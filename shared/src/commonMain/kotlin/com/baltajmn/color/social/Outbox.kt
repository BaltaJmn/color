package com.baltajmn.color.social

import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.Storage
import com.baltajmn.color.data.reencodeJpeg
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.Share
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** docs/tecnico.md 5: small enough for a feed card, and a fresh encode that carries no metadata. */
const val UPLOAD_SIDE = 720
const val UPLOAD_QUALITY = 80

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

    /** Fire and forget: nobody waits on the network to save a day. */
    fun kick() {
        if (Social.available) scope.launch { flush() }
    }

    suspend fun flush() = lock.withLock {
        val uid = Social.userId() ?: return@withLock
        for (day in ChromaRepository.file.outbox) {
            val entry = ChromaRepository.journal[day]
            // ponytail: the photo is sent again on every change of the day, a word included; a
            // hash of the last upload would save it if bandwidth ever matters.
            if (runCatching { send(uid, day, entry) }.isFailure) return@withLock
            ChromaRepository.synced(day, entry)
        }
    }

    private suspend fun send(uid: String, day: String, entry: ChromaEntry?) {
        val path = "$uid/$day.jpg"
        val photos = Social.client.storage.from("photos")
        val rows = Social.client.from("shared_entries")

        if (entry == null || entry.share == Share.Private) {
            // The row first: a friend must never be handed a path to a photo that is already gone.
            rows.delete { filter { eq("author", uid); eq("day", day) } }
            photos.delete(path)
            return
        }

        val jpeg = entry.photo.takeIf { entry.share == Share.Photo }
            ?.let { withContext(Dispatchers.IO) { Storage.readPhoto(it) } }
            ?.let { withContext(Dispatchers.Default) { reencodeJpeg(it, UPLOAD_SIDE, UPLOAD_QUALITY) } }
        // And the other way round here: the file is in place before any row points at it.
        if (jpeg != null) photos.upload(path, jpeg) { upsert = true } else photos.delete(path)
        rows.upsert(SharedRow(uid, day, entry.color, entry.name, entry.word, path.takeIf { jpeg != null }))
    }
}
