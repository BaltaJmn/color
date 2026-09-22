package com.baltajmn.color.data

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSDataWritingAtomic
import platform.Foundation.NSDataWritingFileProtectionCompleteUntilFirstUserAuthentication
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionCompleteUntilFirstUserAuthentication
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

// Not Complete: with Complete, the save that fires right after the phone locks fails and the day
// is lost. UntilFirstUserAuthentication keeps the files encrypted until the first unlock after boot.
private val protection = mapOf<Any?, Any?>(NSFileProtectionKey to NSFileProtectionCompleteUntilFirstUserAuthentication)
private val WRITE_OPTIONS = NSDataWritingAtomic or NSDataWritingFileProtectionCompleteUntilFirstUserAuthentication

@OptIn(ExperimentalForeignApi::class)
actual object Storage {
    private val fm get() = NSFileManager.defaultManager

    /** Application Support, never the App Group container. */
    private val root: String by lazy {
        val base = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
            .first() as String
        fm.createDirectoryAtPath(base, true, protection, null)
        base
    }

    private val path get() = "$root/entries.json"
    private val backupPath get() = "$root/entries.bak.json"
    private val photosDir get() = "$root/photos".also { fm.createDirectoryAtPath(it, true, protection, null) }
    private val importFolder get() = "$root/import".also { fm.createDirectoryAtPath(it, true, protection, null) }

    actual fun read(): String? = textAt(path)

    actual fun readPrevious(): String? = textAt(backupPath)

    actual fun write(text: String) {
        if (fm.fileExistsAtPath(path)) {
            fm.removeItemAtPath(backupPath, null)
            if (!fm.copyItemAtPath(path, backupPath, null)) error("could not keep the backup")
        }
        writeAtomically(path, text.encodeToByteArray())
    }

    actual fun restoreMain(text: String) = writeAtomically(path, text.encodeToByteArray())

    @OptIn(ExperimentalTime::class)
    actual fun quarantine() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        fun two(n: Int) = n.toString().padStart(2, '0')
        val stamp = "${now.year}${two(now.month.ordinal + 1)}${two(now.day)}-" +
            "${two(now.hour)}${two(now.minute)}${two(now.second)}"
        val corrupt = "$root/corrupt".also { fm.createDirectoryAtPath(it, true, protection, null) }
        if (fm.fileExistsAtPath(path)) fm.moveItemAtPath(path, "$corrupt/entries-$stamp.json", null)
        if (fm.fileExistsAtPath(backupPath)) fm.moveItemAtPath(backupPath, "$corrupt/entries-$stamp.bak.json", null)
    }

    actual fun writePhoto(name: String, bytes: ByteArray) = writeAtomically("$photosDir/$name", bytes)

    actual fun readPhoto(name: String): ByteArray? = NSData.dataWithContentsOfFile("$photosDir/$name")?.toByteArray()

    actual fun deletePhoto(name: String) {
        fm.removeItemAtPath("$photosDir/$name", null)
    }

    actual fun listPhotos(): List<String> =
        fm.contentsOfDirectoryAtPath(photosDir, null)?.filterIsInstance<String>().orEmpty()

    actual fun importDir(): String {
        val import = "$root/import"
        fm.removeItemAtPath(import, null)
        fm.createDirectoryAtPath(import, true, protection, null)
        return import
    }

    actual fun writeImport(name: String, bytes: ByteArray) = writeAtomically("$importFolder/$name", bytes)

    actual fun adoptImport(name: String, asName: String) {
        fm.removeItemAtPath("$photosDir/$asName", null)
        fm.moveItemAtPath("$importFolder/$name", "$photosDir/$asName", null)
    }

    private fun writeAtomically(target: String, bytes: ByteArray) {
        if (!bytes.toNSData().writeToFile(target, WRITE_OPTIONS, null)) error("could not write $target")
    }

    /**
     * Null only when the file does not exist. A file that exists but cannot be read comes back as
     * text that will not decode, so it is quarantined instead of being taken for a fresh journal and
     * overwritten.
     */
    private fun textAt(filePath: String): String? {
        if (!fm.fileExistsAtPath(filePath)) return null
        return NSData.dataWithContentsOfFile(filePath)?.toByteArray()?.decodeToString() ?: ""
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply { usePinned { memcpy(it.addressOf(0), bytes, length) } }
}
