package com.baltajmn.color.share

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.inkColorFor
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.model.isoKey
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

const val CARD_W = 1080
const val CARD_H = 1350
private const val MARGIN = 72f
private const val THUMB = 320f

/**
 * The day card as a picture, 1080x1350 (docs/tecnico.md 6.9). Laid out in pixels at density 1, so a
 * size in sp is a size in px. The screen card and this one share the layout, not the code: one is
 * measured by Compose, the other is drawn at a fixed size for a timeline.
 */
fun renderDayCard(
    entry: ChromaEntry,
    date: LocalDate,
    photo: ImageBitmap?,
    watermark: Boolean,
    measurer: TextMeasurer,
): ImageBitmap = picture(CARD_W, CARD_H) {
    val ink = inkColorFor(entry.color)
    drawRect(colorOf(entry.color))
    var y = text(measurer, S.colorName(entry.name), TextStyle(fontSize = 96.sp, fontWeight = FontWeight.Medium, color = ink), MARGIN, 72f + 96f, maxWidth = (CARD_W - 2 * MARGIN).toInt())
    y = text(measurer, entry.color, TextStyle(fontSize = 40.sp, color = ink), MARGIN, y.bottom + 52f)
    entry.word?.let {
        text(measurer, it, TextStyle(fontSize = 56.sp, fontStyle = FontStyle.Italic, color = ink), MARGIN, y.bottom + 90f, maxWidth = (CARD_W - 2 * MARGIN).toInt())
    }
    val dateBaseline = if (watermark) CARD_H - MARGIN - 56f else CARD_H - MARGIN
    text(measurer, S.shortDate(date) + ", " + date.year, TextStyle(fontSize = 40.sp, color = ink), MARGIN, dateBaseline)
    if (watermark) text(measurer, "Chroma", TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium, color = ink), MARGIN, CARD_H - MARGIN)
    photo?.let { thumbnail(it, Offset(CARD_W - MARGIN - THUMB, CARD_H - MARGIN - THUMB), THUMB, ink) }
}

// --- the poster (Pro to export, free to look at) --------------------------------------------------

enum class PosterStyle { Grid, Strip, Wallpaper }

/** A tall phone screen: iOS and Android both fit it to theirs by cropping the sides. */
const val WALL_W = 1170
const val WALL_H = 2532

// Always the light paper, whatever the theme: a poster is for printing and a wall.
private val PAPER = Color(0xFFF6F4F1)
private val PAPER_INK = Color(0xFF1C1B1A)
private val PAPER_EMPTY = Color(0xFFECE9E4)

fun renderPoster(style: PosterStyle, year: Int, days: Map<String, String>, watermark: Boolean, measurer: TextMeasurer): ImageBitmap {
    val colors = days.filterKeys { it.startsWith("$year-") }.entries.sortedBy { it.key }.map { colorOf(it.value) }
    return when (style) {
        PosterStyle.Grid -> gridPoster(year, days, watermark, measurer)
        PosterStyle.Strip -> stripPoster(year, colors, watermark, measurer)
        PosterStyle.Wallpaper -> wallpaper(colors)
    }
}

/** Twelve columns of months, a row per day, as in My year. Days that do not exist stay paper. */
private fun gridPoster(year: Int, days: Map<String, String>, watermark: Boolean, measurer: TextMeasurer) = picture(CARD_W, CARD_H) {
    drawRect(PAPER)
    val title = posterTitle(year, measurer)
    val top = title.bottom + 72f
    val bottom = CARD_H - MARGIN - if (watermark) 56f else 0f
    val gap = 8f
    val rowGap = 6f
    val cellW = (CARD_W - 2 * MARGIN - 11 * gap) / 12
    val cellH = (bottom - top - 30 * rowGap) / 31
    val radius = CornerRadius(6f)
    val label = TextStyle(fontSize = 28.sp, color = PAPER_INK)
    S.monthInitials().forEachIndexed { i, initial ->
        val month = i + 1
        val x = MARGIN + i * (cellW + gap)
        val laid = measurer.measure(initial, label)
        drawText(laid, topLeft = Offset(x + (cellW - laid.size.width) / 2, top - 16f - laid.size.height))
        val last = LocalDate(year, month, 1).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).day
        for (day in 1..last) {
            val fill = days[LocalDate(year, month, day).isoKey()]?.let(::colorOf) ?: PAPER_EMPTY
            drawRoundRect(fill, Offset(x, top + (day - 1) * (cellH + rowGap)), Size(cellW, cellH), radius)
        }
    }
    if (watermark) posterMark(measurer)
}

/** Every day with a color as a thin column, in order, the blank days left out so the year runs on. */
private fun stripPoster(year: Int, colors: List<Color>, watermark: Boolean, measurer: TextMeasurer) = picture(CARD_W, CARD_H) {
    drawRect(PAPER)
    val title = posterTitle(year, measurer)
    val area = Rect(MARGIN, title.bottom + 40f, CARD_W - MARGIN, CARD_H - MARGIN - if (watermark) 56f else 0f)
    clipPath(Path().apply { addRoundRect(RoundRect(area, CornerRadius(28f))) }) { bands(colors, area, vertical = true) }
    if (watermark) posterMark(measurer)
}

/** The year top to bottom in bands, edge to edge and without a word: the clock goes on top. */
private fun wallpaper(colors: List<Color>) = picture(WALL_W, WALL_H) {
    drawRect(PAPER)
    bands(colors, Rect(0f, 0f, WALL_W.toFloat(), WALL_H.toFloat()), vertical = false)
}

/** One band per color. Each one overlaps the next by a pixel so no seam of background shows through. */
private fun DrawScope.bands(colors: List<Color>, area: Rect, vertical: Boolean) {
    if (colors.isEmpty()) return
    val step = (if (vertical) area.width else area.height) / colors.size
    colors.forEachIndexed { i, c ->
        if (vertical) {
            drawRect(c, Offset(area.left + i * step, area.top), Size(step + 1f, area.height))
        } else {
            drawRect(c, Offset(area.left, area.top + i * step), Size(area.width, step + 1f))
        }
    }
}

private fun DrawScope.posterTitle(year: Int, measurer: TextMeasurer) =
    text(measurer, year.toString(), TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Medium, color = PAPER_INK), MARGIN, MARGIN + 72f)

private fun DrawScope.posterMark(measurer: TextMeasurer) =
    text(measurer, "Chroma", TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium, color = PAPER_INK), MARGIN, CARD_H - MARGIN)

/** Draws [image] cropped to a centered square with rounded corners, and a faint ring of [ink]. */
internal fun DrawScope.thumbnail(image: ImageBitmap, at: Offset, side: Float, ink: Color) {
    val crop = minOf(image.width, image.height)
    val src = IntOffset((image.width - crop) / 2, (image.height - crop) / 2)
    val radius = CornerRadius(side * 0.1f)
    val clip = Path().apply { addRoundRect(RoundRect(at.x, at.y, at.x + side, at.y + side, radius)) }
    clipPath(clip) {
        drawImage(image, src, IntSize(crop, crop), IntOffset(at.x.toInt(), at.y.toInt()), IntSize(side.toInt(), side.toInt()))
    }
    drawRoundRect(ink.copy(alpha = 0.24f), at, Size(side, side), radius, style = Stroke(width = 6f))
}

internal fun picture(width: Int, height: Int, draw: DrawScope.() -> Unit): ImageBitmap {
    val bitmap = ImageBitmap(width, height)
    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(bitmap),
        size = Size(width.toFloat(), height.toFloat()),
        block = draw,
    )
    return bitmap
}

/** Positions are baselines, which is where the eye lines text up. */
internal fun DrawScope.text(
    measurer: TextMeasurer,
    value: String,
    style: TextStyle,
    x: Float,
    baseline: Float,
    maxWidth: Int = Constraints.Infinity,
): Placed {
    val laid = measurer.measure(value, style, constraints = Constraints(maxWidth = maxWidth))
    val top = baseline - laid.firstBaseline
    drawText(laid, topLeft = Offset(x, top))
    return Placed(laid, top + laid.size.height)
}

internal class Placed(val layout: TextLayoutResult, val bottom: Float)
