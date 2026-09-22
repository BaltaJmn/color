package com.baltajmn.color.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The glyphs the app needs, drawn rather than typed: iOS renders characters like a gear or an
 * angle quote as colour emoji and Android as plain text, so typed glyphs ship two icon sets.
 * Coordinates are fractions of the side (docs/pantallas.md 2).
 */
enum class Glyph { BACK, FORWARD, SHARE, SETTINGS, YEAR, CLOSE, PHOTO, CAMERA, TODAY, FRIENDS, MORE, TRASH, CHECK }

/** A 48dp tap target with no background: the glyph is the whole control. */
@Composable
fun GlyphButton(glyph: Glyph, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(48.dp)
            .clip(CircleShape)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { GlyphIcon(glyph) }
}

@Composable
fun GlyphIcon(glyph: Glyph, size: Dp = 20.dp, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Canvas(Modifier.size(size)) {
        val side = this.size.width
        val stroke = Stroke(width = side * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun at(x: Float, y: Float) = Offset(x * side, y * side)
        fun line(vararg points: Pair<Float, Float>) = drawPath(
            Path().apply {
                points.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * side, y * side) else lineTo(x * side, y * side) }
            },
            tint,
            style = stroke,
        )
        // Filled: a stroked ring this small is just the line crossing itself.
        fun dot(x: Float, y: Float, r: Float) = drawCircle(tint, radius = r * side, center = at(x, y))

        when (glyph) {
            Glyph.BACK -> line(0.62f to 0.18f, 0.34f to 0.50f, 0.62f to 0.82f)
            Glyph.FORWARD -> line(0.40f to 0.18f, 0.68f to 0.50f, 0.40f to 0.82f)
            Glyph.SHARE -> {
                line(0.50f to 0.88f, 0.50f to 0.16f)
                line(0.26f to 0.40f, 0.50f to 0.16f, 0.74f to 0.40f)
            }
            // Two sliders, not a gear: a gear at 20dp is a smudge.
            Glyph.SETTINGS -> listOf(0.34f to 0.66f, 0.62f to 0.38f).forEach { (y, knob) ->
                line(0.14f to y, 0.86f to y)
                dot(knob, y, 0.11f)
            }
            Glyph.YEAR -> listOf(0.25f, 0.50f, 0.75f).forEach { y ->
                listOf(0.25f, 0.50f, 0.75f).forEach { x -> dot(x, y, 0.07f) }
            }
            Glyph.CLOSE -> {
                line(0.24f to 0.24f, 0.76f to 0.76f)
                line(0.76f to 0.24f, 0.24f to 0.76f)
            }
            Glyph.PHOTO -> {
                drawRoundRect(
                    tint,
                    topLeft = at(0.14f, 0.24f),
                    size = Size(0.72f * side, 0.56f * side),
                    cornerRadius = CornerRadius(0.08f * side),
                    style = stroke,
                )
                dot(0.36f, 0.43f, 0.07f)
                line(0.14f to 0.72f, 0.40f to 0.52f, 0.58f to 0.66f, 0.70f to 0.57f, 0.86f to 0.70f)
            }
            Glyph.TRASH -> {
                line(0.18f to 0.28f, 0.82f to 0.28f)
                line(0.40f to 0.28f, 0.40f to 0.18f, 0.60f to 0.18f, 0.60f to 0.28f)
                line(0.26f to 0.28f, 0.31f to 0.84f, 0.69f to 0.84f, 0.74f to 0.28f)
            }
            Glyph.CAMERA -> {
                drawRoundRect(
                    tint,
                    topLeft = at(0.12f, 0.30f),
                    size = Size(0.76f * side, 0.52f * side),
                    cornerRadius = CornerRadius(0.10f * side),
                    style = stroke,
                )
                line(0.36f to 0.30f, 0.42f to 0.20f, 0.58f to 0.20f, 0.64f to 0.30f)
                drawCircle(tint, radius = 0.13f * side, center = at(0.50f, 0.56f), style = stroke)
            }
            // A filled circle: today is one color.
            Glyph.TODAY -> dot(0.50f, 0.50f, 0.30f)
            // Two overlapping circles, the same mark as being in tune.
            Glyph.FRIENDS -> {
                drawCircle(tint, radius = 0.22f * side, center = at(0.37f, 0.50f), style = stroke)
                drawCircle(tint, radius = 0.22f * side, center = at(0.63f, 0.50f), style = stroke)
            }
            Glyph.MORE -> listOf(0.22f, 0.50f, 0.78f).forEach { x -> dot(x, 0.50f, 0.075f) }
            Glyph.CHECK -> line(0.22f to 0.52f, 0.42f to 0.72f, 0.78f to 0.30f)
        }
    }
}
