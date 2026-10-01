package com.baltajmn.color.data

import java.io.File
import java.io.FileOutputStream
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

actual object Storage {
    /** Host tests point this at a temporary folder; the app always uses filesDir. */
    internal var rootOverride: File? = null

    private val dir: File get() = rootOverride ?: AndroidContext.value.filesDir
    private val file get() = File(dir, "entries.json")
    private val backup get() = File(dir, "entries.bak.json")
    private val temp get() = File(dir, "entries.tmp.json")
    private val photos get() = File(dir, "photos").apply { mkdirs() }
    private val cached get() = File(rootOverride ?: AndroidContext.value.cacheDir, "friends").apply { mkdirs() }

    actual fun read(): String? = file.textOrNull()

    actual fun readPrevious(): String? = backup.textOrNull()

    actual fun write(text: String) {
        writeTemp(text)
        if (file.exists() && !file.renameTo(backup)) error("could not rotate the backup")
        if (!temp.renameTo(file)) error("could not move the new file into place")
    }

    actual fun restoreMain(text: String) {
        writeTemp(text)
        if (!temp.renameTo(file)) error("could not restore the main file")
    }

    private val corrupt get() = File(dir, "corrupt").apply { mkdirs() }

    @OptIn(ExperimentalTime::class)
    private fun stamp(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "%04d%02d%02d-%02d%02d%02d".format(now.year, now.month.ordinal + 1, now.day, now.hour, now.minute, now.second)
    }

    actual fun quarantine() {
        val stamp = stamp()
        file.takeIf { it.exists() }?.renameTo(File(corrupt, "entries-$stamp.json"))
        backup.takeIf { it.exists() }?.renameTo(File(corrupt, "entries-$stamp.bak.json"))
    }

    actual fun quarantineMain() {
        if (file.exists() && !file.renameTo(File(corrupt, "entries-${stamp()}.json"))) error("could not move the main file aside")
    }

    actual fun quarantinedCount(): Int = corrupt.list()?.count { it.endsWith(".json") } ?: 0

    actual fun readPending(): String? = temp.textOrNull()

    actual fun promotePending() {
        if (!temp.renameTo(file)) error("could not move the pending write into place")
    }

    // Written aside and renamed, like the journal: a full disk or a power cut leaves no half photo
    // under a name the journal may already point at, and the space goes back at once.
    actual fun writePhoto(name: String, bytes: ByteArray) {
        val part = File(photos, "$name.part")
        try {
            FileOutputStream(part).use {
                it.write(bytes)
                it.fd.sync()
            }
            if (!part.renameTo(File(photos, name))) error("could not move the photo into place")
        } catch (e: Exception) {
            part.delete()
            throw e
        }
    }

    actual fun readPhoto(name: String): ByteArray? =
        File(photos, name).takeIf { it.exists() }?.readBytes()

    actual fun deletePhoto(name: String) {
        File(photos, name).delete()
    }

    actual fun listPhotos(): List<String> = photos.list()?.toList().orEmpty()

    actual fun importDir(): String {
        val import = File(dir, "import")
        import.deleteRecursively()
        import.mkdirs()
        return import.path
    }

    private val importFolder get() = File(dir, "import").apply { mkdirs() }

    actual fun writeImport(name: String, bytes: ByteArray) {
        File(importFolder, name).writeBytes(bytes)
    }

    actual fun adoptImport(name: String, asName: String) {
        val from = File(importFolder, name)
        if (from.exists() && !from.renameTo(File(photos, asName))) from.copyTo(File(photos, asName), overwrite = true)
    }

    /** Flushed to the disk before any rename, so a power cut cannot leave a renamed empty file. */
    actual fun readCached(name: String): ByteArray? = File(cached, name).takeIf { it.exists() }?.readBytes()

    actual fun writeCached(name: String, bytes: ByteArray) {
        File(cached, name).writeBytes(bytes)
    }

    actual fun keepCached(names: Set<String>) {
        cached.listFiles()?.filter { it.name !in names }?.forEach { it.delete() }
    }

    private fun writeTemp(text: String) {
        FileOutputStream(temp).use {
            it.write(text.encodeToByteArray())
            it.fd.sync()
        }
    }

    /** Like iOS: a file that exists but cannot be read is quarantined, not a crash on every start. */
    private fun File.textOrNull(): String? = if (exists()) runCatching { readText() }.getOrDefault("") else null
}
