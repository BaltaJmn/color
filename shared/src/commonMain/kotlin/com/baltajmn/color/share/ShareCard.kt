package com.baltajmn.color.share

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
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
import kotlinx.datetime.LocalDate

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
