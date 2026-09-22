package com.baltajmn.color

import com.baltajmn.color.data.ImportFailed
import com.baltajmn.color.data.ImportProblem
import com.baltajmn.color.data.ZipDamaged
import com.baltajmn.color.data.ZipReader
import com.baltajmn.color.data.ZipWriter
import com.baltajmn.color.data.crc32
import com.baltajmn.color.data.merge
import com.baltajmn.color.data.readBackup
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.Share
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BackupTest {

    private fun entry(color: String, photo: String? = null, share: Share = Share.Private) =
        ChromaEntry(color = color, name = "x", photo = photo, share = share)

    // 11. Merge. docs/tecnico.md 4.3
    @Test
    fun theDayOfThisPhoneWinsAndOnlyNewDaysBringPhotos() {
        val device = mapOf("2027-01-17" to entry("#3A6EA5", photo = "p-1.jpg"))
        val incoming = mapOf(
            "2027-01-17" to entry("#EFE8DA", photo = "p-2.jpg"),
            "2027-01-18" to entry("#112233", photo = "p-3.jpg"),
        )
        val r = merge(device, incoming)
        assertEquals("#3A6EA5", r.journal.getValue("2027-01-17").color)
        assertEquals("#112233", r.journal.getValue("2027-01-18").color)
        assertEquals(1, r.added)
        assertEquals(1, r.kept)
        assertEquals(setOf("p-3.jpg"), r.photosFromIncoming)
    }

    @Test
    fun anImportedDayArrivesPrivateAndUpperCase() {
        val backup = readBackup(
            sourceOf("""{"version":1,"entries":{"2027-01-17":{"color":"#3a6ea5","swatches":["#3a6ea5"],"name":"storm_blue","share":"photo"}}}"""),
        )
        val day = backup.journal.getValue("2027-01-17")
        assertEquals(Share.Private, day.share)
        assertEquals("#3A6EA5", day.color)
        assertEquals(listOf("#3A6EA5"), day.swatches)
    }

    @Test
    fun anyJsonIsNotABackup() {
        assertEquals(ImportProblem.NotBackup, refusal("""{"hello":"world"}"""))
        assertEquals(ImportProblem.NotBackup, refusal("""{"version":1}"""))
        assertEquals(ImportProblem.NotBackup, refusal("""{"version":[1],"entries":{}}"""))
        assertEquals(ImportProblem.NotBackup, refusal("not a file at all"))
        // A Purl backup: same shape, days without a color.
        assertEquals(ImportProblem.NotBackup, refusal("""{"version":1,"entries":{"2027-01-17":{"text":"Cafe"}}}"""))
    }

    @Test
    fun aBackupThatCannotBePaintedIsRefusedWhole() {
        assertEquals(ImportProblem.TooNew, refusal("""{"version":99,"entries":{"2027-01-17":{"color":"#112233","name":"x"}}}"""))
        assertEquals(ImportProblem.Empty, refusal("""{"version":1,"entries":{}}"""))
        assertEquals(ImportProblem.Damaged, refusal("""{"version":1,"entries":{"manana":{"color":"#112233","name":"x"}}}"""))
        assertEquals(ImportProblem.Damaged, refusal("""{"version":1,"entries":{"2027-01-17":{"color":"red","name":"x"}}}"""))
        assertEquals(
            ImportProblem.Damaged,
            refusal("""{"version":1,"entries":{"2027-01-17":{"color":"#112233","name":"x","photo":"../entries.json"}}}"""),
        )
    }

    // 12. The zip. docs/tecnico.md 4.3
    @Test
    fun crc32MatchesTheCheckValueOfTheStandard() {
        assertEquals(0xCBF43926.toInt(), crc32("123456789".encodeToByteArray()))
    }

    @Test
    fun aZipWeWroteComesBackWithEveryByte() {
        val photo = ByteArray(300) { (it * 7).toByte() }
        val other = byteArrayOf(0, -1, 127, -128, 10, 13)
        val zip = zipOf(
            "entries.json" to """{"version":1}""".encodeToByteArray(),
            "photos/p-1.jpg" to photo,
            "photos/p-2.jpg" to other,
        )
        val read = mutableListOf<Pair<String, ByteArray>>()
        reader(zip).forEach { name, bytes -> read += name to bytes }
        assertEquals(listOf("entries.json", "photos/p-1.jpg", "photos/p-2.jpg"), read.map { it.first })
        assertContentEquals(photo, read[1].second)
        assertContentEquals(other, read[2].second)
    }

    @Test
    fun aZipCutInHalfOrWithABrokenCrcIsRefused() {
        val long = zipOf("entries.json" to ByteArray(500) { 42 })
        assertFailsWith<ZipDamaged> { reader(long.copyOfRange(0, long.size / 2)).forEach { _, _ -> } }

        val zip = zipOf("entries.json" to "hola".encodeToByteArray())
        // The payload starts right after the 30 byte local header and the name.
        val payload = 30 + "entries.json".length
        zip[payload] = (zip[payload] + 1).toByte()
        assertFailsWith<ZipDamaged> { reader(zip).forEach { _, _ -> } }
    }

    private fun zipOf(vararg files: Pair<String, ByteArray>): ByteArray {
        val out = mutableListOf<Byte>()
        val writer = ZipWriter({ bytes -> bytes.forEach { out += it } })
        files.forEach { (name, bytes) -> writer.add(name, bytes) }
        writer.finish()
        return out.toByteArray()
    }

    private fun reader(data: ByteArray): ZipReader = ZipReader(sourceOf(data))

    private fun refusal(text: String): ImportProblem? =
        runCatching { readBackup(sourceOf(text)) }.exceptionOrNull().let { (it as? ImportFailed)?.problem }

    private fun sourceOf(text: String) = sourceOf(text.encodeToByteArray())

    private fun sourceOf(data: ByteArray): (Int) -> ByteArray? {
        var pos = 0
        return { n -> if (pos >= data.size) null else data.copyOfRange(pos, minOf(pos + n, data.size)).also { pos += it.size } }
    }
}
