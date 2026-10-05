package com.baltajmn.color

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ApplicationProvider
import com.baltajmn.color.billing.Billing
import com.baltajmn.color.color.blendsInto
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.extractSwatches
import com.baltajmn.color.color.inkColorFor
import com.baltajmn.color.color.labOf
import com.baltajmn.color.color.nearestName
import com.baltajmn.color.data.AndroidContext
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.Storage
import com.baltajmn.color.data.WidgetState
import com.baltajmn.color.data.decodeImage
import com.baltajmn.color.data.samplePixels
import com.baltajmn.color.data.today
import com.baltajmn.color.data.widgetState
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.JournalFile
import com.baltajmn.color.model.JournalJson
import com.baltajmn.color.model.isoKey
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Not a test: the raw iPhone captures of store/capturas.md, drawn by the same Compose code the iOS
 * app runs. 440 x 860 pt at 3x is the iPhone 17 Pro Max (6.9") without its status bar and home
 * indicator; tools/store/iphone.py puts those margins back before framing. Only runs with
 * -Pcapturas=<folder>, which holds entries.json and photos/ and receives the PNGs.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w440dp-h860dp-xxhdpi")
class StoreScreenshots {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val out = File(System.getProperty("capturas") ?: ".")

    @Test
    fun scenes() {
        System.getProperty("capturas") ?: return
        S.lang = System.getProperty("capturas.idioma") ?: "en"
        AndroidContext.init(ApplicationProvider.getApplicationContext())
        val work = createTempDirectory("chroma-shots").toFile()
        File(out, "entries.json").copyTo(File(work, "entries.json"))
        File(out, "photos").copyRecursively(File(work, "photos"))
        Storage.rootOverride = work
        addToday(work)
        // There is no store in a JVM: Billing stays quiet, and the demo's Pro is never switched off.
        Billing::class.java.getDeclaredField("configured").apply { isAccessible = true }.set(Billing, true)

        compose.setContent { App() }
        shot("01_hoy")

        compose.onAllNodesWithText(S.navYear).onFirst().performClick()
        shot("02_ano")
        compose.onAllNodesWithText(S.viewStrip).onFirst().performClick()
        shot("03_tira")
        compose.onAllNodesWithText(S.viewGrid).onFirst().performClick()

        compose.onAllNodesWithText(S.poster).onFirst().performClick()
        shot("06_poster")
        compose.onNodeWithContentDescription(S.a11yClose).performClick()

        compose.onAllNodesWithText(S.navToday).onFirst().performClick()
        compose.onNodeWithContentDescription(S.a11yMore).performClick()
        compose.onAllNodesWithText(S.share).onFirst().performClick()
        compose.onAllNodesWithText(S.includePhoto).onFirst().performClick()
        shot("04_tarjeta")
    }

    /**
     * Today as it is a moment after the photo: the real extraction over the demo photo, and the
     * warmest light candidate picked, the one a person would stop at along that row.
     */
    private fun addToday(work: File) {
        val photo = File(work, "photos/p-demo.jpg")
        val image = decodeImage(photo.readBytes()) ?: error("no demo photo")
        val swatches = extractSwatches(samplePixels(image)).map { it.color }
        val picked = swatches.filter { labOf(it).l > 55 }.maxByOrNull { labOf(it).chroma } ?: swatches.first()
        val file = File(work, "entries.json")
        val journal = JournalJson.decodeFromString(JournalFile.serializer(), file.readText())
        val entry = ChromaEntry(color = picked, swatches = swatches, name = nearestName(picked).key, photo = photo.name, at = 1)
        file.writeText(JournalJson.encodeToString(JournalFile.serializer(), journal.copy(entries = journal.entries + (today().isoKey() to entry))))
    }

    /**
     * Scene 05 for the App Store. WidgetKit cannot run here, so this is ChromaWidget.swift and
     * ChromaYearWidget.swift drawn again with their own numbers (sizes of the 6.9" iPhone, 16 pt
     * content margins, the same fonts, grays and grid), fed with the same widget.json the app writes.
     */
    @Test
    fun widgets() {
        System.getProperty("capturas") ?: return
        S.lang = System.getProperty("capturas.idioma") ?: "en"
        AndroidContext.init(ApplicationProvider.getApplicationContext())
        val work = createTempDirectory("chroma-widgets").toFile()
        File(out, "entries.json").copyTo(File(work, "entries.json"))
        File(out, "photos").copyRecursively(File(work, "photos"))
        Storage.rootOverride = work
        addToday(work)
        ChromaRepository.ensureLoaded()
        val day = today()
        val state = widgetState(ChromaRepository.journal, ChromaRepository.settings, day, S::colorName)

        compose.setContent {
            Box(Modifier.fillMaxSize().background(Color(0xFFC4C4C4))) {
                Column(Modifier.offset(x = 38.dp, y = 150.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                    TodayWidget(state, day)
                    YearWidget(state, day)
                }
            }
        }
        shot("05_widgets")
    }

    // captureToImage waits for a hardware frame Robolectric never produces; a software draw of the
    // window is the same picture.
    private fun shot(name: String) {
        compose.waitForIdle()
        compose.runOnUiThread {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            File(out, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}

private val WidgetShape = RoundedCornerShape(23.dp)
private val Paper = Color(0xFFF1F1F1)
private val Empty = Color(0xFFE6E6E6)

@androidx.compose.runtime.Composable
private fun TodayWidget(state: WidgetState, day: LocalDate) {
    val hex = state.color ?: return
    val ink = inkColorFor(hex)
    Column(Modifier.size(170.dp).clip(WidgetShape).background(colorOf(hex)).padding(16.dp)) {
        Text(day.day.toString(), style = TextStyle(color = ink, fontSize = 30.sp, fontWeight = FontWeight.Light))
        Spacer(Modifier.weight(1f))
        Text(state.name.orEmpty(), maxLines = 2, style = TextStyle(color = ink, fontSize = 17.sp, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(2.dp))
        Text(hex, style = TextStyle(color = ink, fontSize = 13.sp, fontWeight = FontWeight.Medium))
    }
}

@androidx.compose.runtime.Composable
private fun YearWidget(state: WidgetState, day: LocalDate) {
    Column(Modifier.size(364.dp, 170.dp).clip(WidgetShape).background(Paper).padding(16.dp)) {
        Text(day.year.toString(), style = TextStyle(color = Color(0xFF111111), fontSize = 20.sp, fontWeight = FontWeight.Light))
        Spacer(Modifier.height(8.dp))
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
            val gap = 2.dp.toPx()
            val side = minOf((size.width - 30 * gap) / 31, (size.height - 11 * gap) / 12)
            val step = side + gap
            val left = (size.width - (step * 31 - gap)) / 2
            val top = (size.height - (step * 12 - gap)) / 2
            val radius = CornerRadius(side / 5)
            for (month in 1..12) for (d in 1..31) {
                val date = runCatching { LocalDate(day.year, month, d) }.getOrNull() ?: continue
                val at = Offset(left + step * (d - 1), top + step * (month - 1))
                val hex = state.days[date.isoKey()]
                if (hex != null) {
                    drawRoundRect(colorOf(hex), at, Size(side, side), radius)
                    if (blendsInto(colorOf(hex), Paper) || blendsInto(colorOf(hex), Color(0xFF0E0E0E))) {
                        drawRoundRect(Color(0x33808080), at, Size(side, side), radius, style = Stroke(1f))
                    }
                } else {
                    drawRoundRect(Empty.copy(alpha = if (date > day) 0.45f else 1f), at, Size(side, side), radius)
                }
            }
        }
    }
}
