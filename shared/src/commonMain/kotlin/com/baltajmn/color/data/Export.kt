package com.baltajmn.color.data

import com.baltajmn.color.model.BACKUP_VERSION
import com.baltajmn.color.model.ExportFile
import com.baltajmn.color.model.Journal
import com.baltajmn.color.model.JournalFile
import com.baltajmn.color.model.JournalJson
import com.baltajmn.color.model.Share
import com.baltajmn.color.model.WORD_MAX
import com.baltajmn.color.model.clampCodePoints
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject

const val EXPORT_PREFIX = "chroma"
const val ENTRIES_NAME = "entries.json"
const val PHOTOS_DIR = "photos/"

private val HEX = Regex("^#[0-9A-Fa-f]{6}$")

fun exportName(today: LocalDate) = "$EXPORT_PREFIX-$today.zip"

/**
 * Asks the system where to put the backup and writes it there. The date of the last backup only
 * moves when the system says the file is written: a cancelled picker has saved nothing.
 */
fun startExport(today: LocalDate, onResult: (PickResult) -> Unit) {
    ChromaRepository.saveNow()
    val snapshot = ChromaRepository.file
    FilePicker.exportZip(exportName(today), { sink -> exportZip(snapshot, sink) }) { result ->
        if (result == PickResult.Done) {
            ChromaRepository.updateSettings { it.copy(lastBackup = today.toString(), backupNoticeDone = true) }
        }
        onResult(result)
    }
}

/**
 * Writes the backup into [sink] entry by entry. Each photo is read whole to get its CRC before its
 * header, so at most one photo is in memory at a time however long the journal is.
 */
fun exportZip(file: JournalFile, sink: (ByteArray) -> Unit) {
    val zip = ZipWriter(sink)
    // Only the days travel: the settings of this phone are not part of anyone's year.
    val sorted = file.entries.keys.sorted().associateWith { file.entries.getValue(it) }
    zip.add(ENTRIES_NAME, JournalJson.encodeToString(ExportFile.serializer(), ExportFile(entries = sorted)).encodeToByteArray())
    sorted.values.mapNotNull { it.photo }.distinct()
        .forEach { name -> Storage.readPhoto(name)?.let { zip.add(PHOTOS_DIR + name, it) } }
    zip.finish()
}

/** Why a file was refused. The key is the text the user reads. */
enum class ImportProblem { NotBackup, Damaged, TooNew, Empty }

class ImportFailed(val problem: ImportProblem) : Exception(problem.name)

/** What a backup brought: the days, and the photos already parked in import/. */
data class Backup(val journal: Journal, val photos: Set<String>)

/**
 * Reads a backup, zip or bare entries.json, leaving the journal untouched if anything is off.
 *
 * ignoreUnknownKeys means any JSON at all would decode into an empty journal, and an empty journal
 * imported over a real one reads as "nothing to merge". So the shape is checked by hand first.
 */
fun readBackup(source: (Int) -> ByteArray?): Backup {
    val head = readAtLeast(source, 4) ?: throw ImportFailed(ImportProblem.NotBackup)
    val isZip = head.size >= 4 && head[0] == 0x50.toByte() && head[1] == 0x4B.toByte() &&
        head[2] == 0x03.toByte() && head[3] == 0x04.toByte()

    var json: String? = null
    val photos = mutableSetOf<String>()
    if (isZip) {
        // Emptied before parking anything, and again on any refusal or cancel.
        Storage.importDir()
        try {
            ZipReader(replay(head, source)).forEach { name, bytes ->
                when {
                    name == ENTRIES_NAME -> json = bytes.decodeToString()
                    isPhotoName(name) -> {
                        // "photos/.." would land on the folder above import/, so the inner name is
                        // checked too and not only the shape of the entry.
                        val photo = name.substringAfter(PHOTOS_DIR)
                        if (!isSafePhotoName(photo)) throw ImportFailed(ImportProblem.Damaged)
                        Storage.writeImport(photo, bytes)
                        photos += photo
                    }
                }
            }
        } catch (e: ZipDamaged) {
            throw ImportFailed(ImportProblem.Damaged)
        }
        if (json == null) throw ImportFailed(ImportProblem.NotBackup)
    } else {
        if (head[0] != '{'.code.toByte()) throw ImportFailed(ImportProblem.NotBackup)
        json = drain(replay(head, source)).decodeToString()
    }

    return Backup(journalOf(json!!), photos)
}

/** Clears whatever a refused or cancelled import parked in import/. */
fun abandonImport() {
    Storage.importDir()
}

private fun journalOf(text: String): Journal {
    val root = runCatching { JournalJson.parseToJsonElement(text).jsonObject }.getOrNull()
        ?: throw ImportFailed(ImportProblem.NotBackup)
    // as? and not jsonPrimitive: a "version" that is an object has to read as not ours, not crash.
    val version = (root["version"] as? JsonPrimitive)?.intOrNull ?: throw ImportFailed(ImportProblem.NotBackup)
    if (version > BACKUP_VERSION) throw ImportFailed(ImportProblem.TooNew)
    val entries = (root["entries"] as? JsonObject) ?: throw ImportFailed(ImportProblem.NotBackup)
    if (entries.isEmpty()) throw ImportFailed(ImportProblem.Empty)
    // A Purl backup has the same shape and days without a color: it is a backup, just not ours.
    if (entries.values.any { ((it as? JsonObject)?.get("color") as? JsonPrimitive)?.contentOrNull == null }) {
        throw ImportFailed(ImportProblem.NotBackup)
    }
    entries.keys.forEach { key ->
        if (runCatching { LocalDate.parse(key) }.isFailure) throw ImportFailed(ImportProblem.Damaged)
    }

    val file = runCatching { JournalJson.decodeFromString(ExportFile.serializer(), text) }.getOrNull()
        ?: throw ImportFailed(ImportProblem.Damaged)
    // A color that is not "#RRGGBB" would crash the first screen that paints it.
    val bad = file.entries.values.any { e ->
        !HEX.matches(e.color) || e.swatches.any { !HEX.matches(it) } || e.photo?.let { !isSafePhotoName(it) } == true
    }
    if (bad) throw ImportFailed(ImportProblem.Damaged)
    // Restoring a backup never publishes a day: whatever it was, it arrives private.
    return file.entries.mapValues { (_, e) ->
        e.copy(
            color = e.color.uppercase(),
            swatches = e.swatches.map { it.uppercase() },
            word = e.word?.clampCodePoints(WORD_MAX),
            share = Share.Private,
        )
    }
}

// --- reading the picked file ---------------------------------------------------------------------

private fun readAtLeast(source: (Int) -> ByteArray?, n: Int): ByteArray? {
    var out = ByteArray(0)
    while (out.size < n) {
        val chunk = source(n - out.size) ?: return out.takeIf { it.isNotEmpty() }
        if (chunk.isEmpty()) return out.takeIf { it.isNotEmpty() }
        out += chunk
    }
    return out
}

/** Hands back what was already read before the rest of the file. */
private fun replay(head: ByteArray, source: (Int) -> ByteArray?): (Int) -> ByteArray? {
    var at = 0
    return { n ->
        if (at < head.size) {
            val take = minOf(n, head.size - at)
            head.copyOfRange(at, at + take).also { at += take }
        } else {
            source(n)
        }
    }
}

private fun drain(source: (Int) -> ByteArray?): ByteArray {
    var out = ByteArray(0)
    while (true) {
        val chunk = source(64 * 1024) ?: return out
        if (chunk.isEmpty()) return out
        out += chunk
    }
}
