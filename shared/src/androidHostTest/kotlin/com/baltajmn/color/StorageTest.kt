package com.baltajmn.color

import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.Storage
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.JournalFile
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Test 10: atomic writes, and a file that cannot be read is never overwritten.
class StorageTest {
    private lateinit var dir: File

    private val good = """{"version":1,"entries":{"2026-09-22":{"color":"#3A6EA5","name":"storm_blue"}}}"""

    @BeforeTest
    fun setUp() {
        dir = createTempDirectory("chroma").toFile()
        Storage.rootOverride = dir
    }

    @AfterTest
    fun tearDown() {
        Storage.rootOverride = null
        dir.deleteRecursively()
    }

    @Test
    fun halfWrittenTempAndUnreadableMainFallBackToTheBackup() {
        File(dir, "entries.tmp.json").writeText("{\"version\":1,\"entr")
        File(dir, "entries.json").writeText("not json")
        File(dir, "entries.bak.json").writeText(good)

        ChromaRepository.load()

        assertEquals("#3A6EA5", ChromaRepository.journal["2026-09-22"]?.color)
        assertFalse(ChromaRepository.corrupt)
        assertEquals(good, File(dir, "entries.json").readText())
        assertEquals(good, File(dir, "entries.bak.json").readText())
    }

    @Test
    fun twoUnreadableFilesAreQuarantinedUntouched() {
        File(dir, "entries.json").writeText("garbage one")
        File(dir, "entries.bak.json").writeText("garbage two")

        ChromaRepository.load()

        assertTrue(ChromaRepository.corrupt)
        assertTrue(ChromaRepository.journal.isEmpty())
        assertFalse(File(dir, "entries.json").exists())
        val moved = File(dir, "corrupt").listFiles().orEmpty()
        assertEquals(setOf("garbage one", "garbage two"), moved.map { it.readText() }.toSet())
    }

    @Test
    fun noFilesIsAFreshJournal() {
        ChromaRepository.load()
        assertFalse(ChromaRepository.corrupt)
        assertTrue(ChromaRepository.journal.isEmpty())
        assertFalse(File(dir, "entries.json").exists())
    }

    @Test
    fun writeRotatesTheBackupAndLeavesNoTemp() {
        Storage.write("first")
        Storage.write("second")
        assertEquals("second", Storage.read())
        assertEquals("first", Storage.readPrevious())
        assertFalse(File(dir, "entries.tmp.json").exists())
    }

    @Test
    fun orphanPhotosAreSweptOnLoad() {
        File(dir, "entries.json").writeText(
            """{"version":1,"entries":{"2026-09-22":{"color":"#3A6EA5","name":"x","photo":"p-00000001.jpg"}}}""",
        )
        Storage.writePhoto("p-00000001.jpg", byteArrayOf(1))
        Storage.writePhoto("p-deadbeef.jpg", byteArrayOf(2))

        ChromaRepository.load()

        assertEquals(listOf("p-00000001.jpg"), Storage.listPhotos())
    }

    @Test
    fun widgetStateNeverCarriesThePhotoOrTheWord() {
        File(dir, "entries.json").writeText(
            """{"version":1,"entries":{"2026-09-22":{"color":"#3A6EA5","name":"x","word":"secret","photo":"p-1.jpg"}}}""",
        )
        ChromaRepository.load()
        val widget = File(dir, "widget.json").readText()
        assertFalse("secret" in widget)
        assertFalse("p-1.jpg" in widget)
    }

    @Test
    fun theFileIsWrittenInDateOrder() {
        val text = ChromaRepository.encode(
            JournalFile(
                entries = mapOf(
                    "2027-02-01" to ChromaEntry(color = "#000000", name = "b"),
                    "2026-01-01" to ChromaEntry(color = "#FFFFFF", name = "a"),
                ),
            ),
        )
        assertTrue(text.indexOf("2026-01-01") < text.indexOf("2027-02-01"))
    }
}
