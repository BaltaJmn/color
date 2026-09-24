package com.baltajmn.color.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.FriendsToday
import com.baltajmn.color.model.Journal
import com.baltajmn.color.model.JournalFile
import com.baltajmn.color.model.JournalJson
import com.baltajmn.color.model.Settings
import com.baltajmn.color.model.Share
import com.baltajmn.color.review.Review
import com.baltajmn.color.model.isoKey
import com.baltajmn.color.model.logicalDate
import com.baltajmn.color.model.sharedChanges
import com.baltajmn.color.model.withPick
import com.baltajmn.color.model.withWord
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

const val SAVE_DEBOUNCE_MS = 800L
const val BACKUP_NOTICE_AFTER_DAYS = 30

/** "Today" everywhere in the app: the logical day, which ends at 03:00 local time. */
@OptIn(ExperimentalTime::class)
fun today(): LocalDate = logicalDate(Clock.System.now(), TimeZone.currentSystemDefault())

/** Wall clock, which is what the reminder is booked against. */
@OptIn(ExperimentalTime::class)
fun nowLocal(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

@OptIn(ExperimentalTime::class)
fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

/**
 * Single source of truth. The whole journal is one JSON file; state changes at once on the main
 * thread and a single writer persists the latest snapshot off it, so two saves never cross and
 * never rotate the backup twice. Straight from Purl.
 */
object ChromaRepository {

    var file by mutableStateOf(JournalFile())
        private set

    val journal: Journal get() = file.entries
    val settings: Settings get() = file.settings

    /** The last write failed. Today shows it; the next change retries. */
    var saveFailed by mutableStateOf(false)
        private set

    /** Neither file could be read. Both were moved aside and the journal starts empty. */
    var corrupt by mutableStateOf(false)
        private set

    /** Runs after every successful write. The outbox hangs off it. */
    val afterSave = mutableListOf<suspend (JournalFile) -> Unit>()

    private val scope by lazy { MainScope() }
    private val writeLock = Mutex()
    private var saveJob: Job? = null
    private var written: JournalFile? = null

    /** Reads the journal, falling back to the backup, and never writes over a file it could not read. */
    fun load() {
        val main = Storage.read()
        var loaded = decode(main)
        var previous: String? = null
        if (loaded == null) {
            previous = Storage.readPrevious()
            loaded = decode(previous)
            if (loaded != null) runCatching { Storage.restoreMain(previous!!) }
        }
        corrupt = false
        when {
            loaded != null -> {
                file = loaded
                sweep(loaded)
            }
            main == null && previous == null -> file = JournalFile()
            else -> {
                Storage.quarantine()
                file = JournalFile()
                corrupt = true
            }
        }
        written = file
        syncWidgets(file)
    }

    fun dismissCorrupt() {
        corrupt = false
    }

    fun edit(change: (JournalFile) -> JournalFile) {
        val before = file
        val next = change(before)
        val touched = sharedChanges(before.entries, next.entries)
        file = if (touched.isEmpty()) next else next.copy(outbox = (next.outbox + touched).distinct())
        saveJob?.cancel()
        // Cancelling only ever stops the wait: a write that has started always finishes.
        saveJob = scope.launch {
            delay(SAVE_DEBOUNCE_MS)
            withContext(NonCancellable) { persist() }
        }
    }

    /** Writes the latest state now. Going to the background and leaving an editor call it. */
    suspend fun flush() {
        saveJob?.cancel()
        withContext(NonCancellable) { persist() }
    }

    fun saveNow() {
        scope.launch { flush() }
    }

    fun updateSettings(change: (Settings) -> Settings) = edit { it.copy(settings = change(it.settings)) }

    /** The friends strip of the today widget; null forgets it. Written only when it changed. */
    fun updateFriendsToday(today: FriendsToday?) {
        if (today != file.friendsToday) edit { it.copy(friendsToday = today) }
    }

    /** What the store says, kept on disk so the app knows it offline and the widgets can read it. */
    fun updatePro(active: Boolean) {
        if (active != settings.pro) updateSettings { it.copy(pro = active) }
    }

    // --- entries ----------------------------------------------------------------------------

    fun entryOn(date: LocalDate): ChromaEntry? = journal[date.isoKey()]

    /**
     * Today's pick. With [jpeg], a new photo replaces today's: it is stored under a name nobody used
     * before, so a cached image is never served for a photo that was replaced, and the old file goes
     * when the save lands. Without it, only the color changes.
     */
    fun pick(color: String, swatches: List<String>, name: String, jpeg: ByteArray? = null) {
        val day = today()
        val key = day.isoKey()
        if (jpeg == null) {
            val next = journal.withPick(key, key, color, swatches, name, nowMillis(), settings.defaultShare) ?: return
            edit { it.copy(entries = next) }
            return
        }
        scope.launch {
            val taken = journal.values.mapNotNull { it.photo }.toSet()
            val photo = withContext(Dispatchers.IO) {
                val free = freePhotoName(taken + Storage.listPhotos())
                if (runCatching { Storage.writePhoto(free, jpeg) }.isSuccess) free else null
            }
            if (photo == null) {
                saveFailed = true
                return@launch
            }
            edit { f ->
                val picked = f.entries.withPick(key, key, color, swatches, name, nowMillis(), f.settings.defaultShare)
                    ?: f.entries
                val entry = picked[key] ?: return@edit f
                entry.photo?.let(Photos::forget)
                f.copy(entries = picked + (key to entry.copy(photo = photo)))
            }
        }
    }

    fun setWord(word: String) {
        val key = today().isoKey()
        val next = journal.withWord(key, key, word, nowMillis()) ?: return
        edit { it.copy(entries = next) }
    }

    fun setShare(share: Share) {
        val key = today().isoKey()
        val entry = journal[key] ?: return
        if (entry.share == share) return
        edit { it.copy(entries = it.entries + (key to entry.copy(share = share, at = nowMillis()))) }
    }

    fun delete(date: LocalDate) {
        val key = date.isoKey()
        journal[key]?.photo?.let(Photos::forget)
        if (key in journal) edit { it.copy(entries = it.entries - key) }
    }

    /** Asked once, a month in, and never again after a backup or after being waved away. */
    fun needsBackupNotice(today: LocalDate): Boolean {
        if (settings.backupNoticeDone || settings.lastBackup != null) return false
        val first = journal.keys.minOrNull()?.let(LocalDate::parse) ?: return false
        return first.daysUntil(today) >= BACKUP_NOTICE_AFTER_DAYS
    }

    /**
     * Takes in a merge: the photos the backup brought move out of import/ first, under a free name
     * if theirs was taken, and only then does the journal change. Nothing of this phone is touched.
     */
    fun applyImport(result: MergeResult, delivered: Set<String>, onDone: () -> Unit = {}) {
        val before = journal
        scope.launch {
            // Only what the backup actually carried is adopted, never a leftover of an older import.
            val adoptable = result.photosFromIncoming.filter { it in delivered && isSafePhotoName(it) }
            val renamed = withContext(Dispatchers.IO) {
                val taken = (Storage.listPhotos() + before.values.mapNotNull { it.photo }).toMutableSet()
                adoptable.associateWith { name ->
                    val target = if (name in taken) freePhotoName(taken) else name
                    taken += target
                    Storage.adoptImport(name, target)
                    target
                }
            }
            val merged = result.journal.mapValues { (key, entry) ->
                val photo = entry.photo
                // Named but not delivered: the color is kept, the photo that does not exist is not.
                if (key in before || photo == null) entry else entry.copy(photo = renamed[photo])
            }
            edit { it.copy(entries = merged) }
            flush()
            withContext(Dispatchers.IO) { Storage.importDir() }
            onDone()
        }
    }

    /**
     * The server has [sent] for [day]. Only then does the day leave the queue: if it changed again
     * while it was being sent, it stays for the next round.
     */
    fun synced(day: String, sent: ChromaEntry?) = edit { f ->
        if (f.entries[day] == sent) f.copy(outbox = f.outbox - day) else f
    }

    fun syncWidgets(f: JournalFile = file) {
        val day = today()
        val friends = f.friendsToday?.takeIf { it.date == day.toString() }?.colors.orEmpty()
        syncWidgets(widgetState(f.entries, f.settings, day, nameOf = S::colorName, friends = friends))
    }

    // --- internals ----------------------------------------------------------------------------

    private suspend fun persist() = writeLock.withLock {
        val snapshot = file
        val previous = written
        if (snapshot === previous) return@withLock
        val ok = withContext(Dispatchers.IO) {
            runCatching { Storage.write(encode(snapshot)) }.isSuccess
        }
        if (ok) {
            written = snapshot
            saveFailed = false
            withContext(Dispatchers.IO) { syncWidgets(snapshot) }
            // A new color today moves the next nudge to tomorrow; iOS has to be told.
            Reminder.sync(askPermission = false)
            afterSave.forEach { runCatching { it(snapshot) } }
            maybeRequestReview(snapshot)
            val gone = photosOf(previous) - photosOf(snapshot)
            if (gone.isNotEmpty()) withContext(Dispatchers.IO) { gone.forEach(Storage::deletePhoto) }
        } else {
            saveFailed = true
        }
    }

    /**
     * Once ever, the moment the journal reaches a week of entries. Only runs from a real save, so it
     * never fires on first launch (nothing saved yet) and never mid purchase (that flow does not
     * touch the journal).
     */
    private fun maybeRequestReview(f: JournalFile) {
        if (!reachedReviewDayCount(f.entries.size, f.settings.reviewRequested)) return
        updateSettings { it.copy(reviewRequested = true) }
        Review.request()
    }

    /** Photos nobody references any more, and whatever an import left half done. */
    private fun sweep(f: JournalFile) {
        val referenced = photosOf(f)
        Storage.listPhotos().filter { it !in referenced }.forEach(Storage::deletePhoto)
        Storage.importDir()
    }

    private fun photosOf(f: JournalFile?): Set<String> =
        f?.entries?.values?.mapNotNull { it.photo }?.toSet().orEmpty()

    private fun decode(text: String?): JournalFile? =
        text?.let { runCatching { JournalJson.decodeFromString(JournalFile.serializer(), it) }.getOrNull() }

    /** Sorted by date, so the file reads in order and backups diff sensibly. */
    internal fun encode(f: JournalFile): String =
        JournalJson.encodeToString(JournalFile.serializer(), f.copy(entries = f.entries.toSortedMap()))

    private fun Map<String, ChromaEntry>.toSortedMap() = entries.sortedBy { it.key }.associate { it.toPair() }
}

/** Whether a just-persisted journal should trigger the once-ever review prompt. */
internal fun reachedReviewDayCount(totalDays: Int, alreadyRequested: Boolean): Boolean =
    !alreadyRequested && totalDays >= 7
