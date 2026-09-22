package com.baltajmn.color.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val BACKUP_VERSION = 1
const val REMINDER_DEFAULT_HOUR = 20
const val REMINDER_DEFAULT_MINUTE = 0

/** Who sees a day. Nothing leaves the phone unless the user picks one of the last two. */
@Serializable
enum class Share {
    @SerialName("private") Private,
    @SerialName("color") Color,
    @SerialName("photo") Photo,
}

@Serializable
data class ChromaEntry(
    /** "#RRGGBB", upper case. */
    val color: String,
    /** The candidates the photo offered, in the order they were offered, so the pick can change all day. */
    val swatches: List<String> = listOf(color),
    /** Key into the name table, not the name: switching language renames every day at once. */
    val name: String,
    val word: String? = null,
    /** File name inside photos/, extension included: "p-3f9a1c2e.jpg". */
    val photo: String? = null,
    val share: Share = Share.Private,
    /** Epoch millis of the last edit. Orders the friends feed. */
    val at: Long = 0,
)

@Serializable
data class Settings(
    val reminderOn: Boolean = false,
    val reminderHour: Int = REMINDER_DEFAULT_HOUR,
    val reminderMinute: Int = REMINDER_DEFAULT_MINUTE,
    val reminderOffered: Boolean = false,
    val lockOn: Boolean = false,
    /** Local ISO date of the last export the system confirmed. */
    val lastBackup: String? = null,
    val backupNoticeDone: Boolean = false,
    /** RevenueCat's last answer, so Pro survives an offline start. */
    val pro: Boolean = false,
    val defaultShare: Share = Share.Private,
    /** The default was asked for once, when the first friend arrived. */
    val shareAsked: Boolean = false,
    val weekColorOn: Boolean = true,
    val watermark: Boolean = true,
    /** Friends' days this person reported, "<author>/<day>": hidden from them for good. */
    val hiddenCards: List<String> = emptyList(),
)

/** Local ISO date ("2026-09-22") of the logical day to its entry. */
typealias Journal = Map<String, ChromaEntry>

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class JournalFile(
    @EncodeDefault val version: Int = BACKUP_VERSION,
    @EncodeDefault val entries: Map<String, ChromaEntry> = emptyMap(),
    val settings: Settings = Settings(),
    /**
     * Days whose shared copy on the server is behind this phone, oldest first. It lives in the same
     * file as the days so a change and its place in the queue are written together or not at all.
     */
    val outbox: List<String> = emptyList(),
)

/** Days whose change the server has to hear about: anything that is or was shared. */
fun sharedChanges(before: Journal, after: Journal): List<String> =
    (before.keys + after.keys).filter { day ->
        val a = before[day]
        val b = after[day]
        a != b && (a.isShared() || b.isShared())
    }.sorted()

private fun ChromaEntry?.isShared() = this != null && share != Share.Private

/** What goes into a backup: the days without the settings of this phone. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ExportFile(
    @EncodeDefault val version: Int = BACKUP_VERSION,
    @EncodeDefault val entries: Map<String, ChromaEntry> = emptyMap(),
)

@OptIn(ExperimentalSerializationApi::class)
val JournalJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
    explicitNulls = false
}

/**
 * The journal after picking [color] among [swatches] for [today], or null when nothing changes.
 * Only today is ever written: a color is of a day you lived looking around.
 */
fun Journal.withPick(
    date: String,
    today: String,
    color: String,
    swatches: List<String>,
    name: String,
    now: Long,
    defaultShare: Share,
): Journal? {
    if (date != today) return null
    val old = this[date]
    val entry = old?.copy(color = color, swatches = swatches, name = name, at = now)
        ?: ChromaEntry(color = color, swatches = swatches, name = name, share = defaultShare, at = now)
    return if (entry == old) null else this + (date to entry)
}

/** The journal with [word] on today's entry. Blank removes it; the limit is enforced by the field. */
fun Journal.withWord(date: String, today: String, word: String, now: Long): Journal? {
    if (date != today) return null
    val old = this[date] ?: return null
    val next = old.copy(word = word.trim().ifEmpty { null }?.clampCodePoints(WORD_MAX), at = now)
    return if (next.word == old.word) null else this + (date to next)
}
