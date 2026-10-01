package com.baltajmn.color.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import com.baltajmn.color.i18n.S
import kotlin.math.max
import kotlin.math.min

/**
 * The photo behind a card, whole, on black. Pinch to look closer at what gave the color (up to 4x),
 * drag to move around it, double tap to zoom in or back out.
 */
@Composable
fun PhotoViewer(image: ImageBitmap, onClose: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    // Never dragged past the edge of the zoomed picture into the black. Fitted, the picture is smaller
    // than the screen on one side, so the room to move is its own size, not the screen's. [box] is read
    // on every gesture: a rotation changes it under a pointerInput that keeps running.
    fun bounded(o: Offset, s: Float, box: IntSize): Offset {
        val fit = min(box.width.toFloat() / image.width, box.height.toFloat() / image.height)
        val x = max(0f, image.width * fit * s - box.width) / 2
        val y = max(0f, image.height * fit * s - box.height) / 2
        return Offset(o.x.coerceIn(-x, x), o.y.coerceIn(-y, y))
    }
    // Zooms keeping the point under the fingers (or the tap) where it is: a pinch over a corner looks at
    // that corner, not at the middle. [at] is relative to the centre, the layer's pivot.
    fun zoomed(at: Offset, next: Float, pan: Offset, box: IntSize) = bounded((offset - at) * (next / scale) + at + pan, next, box)
    Box(Modifier.fillMaxSize().background(Color.Black).blockTouches()) {
        Image(
            image,
            null,
            contentScale = ContentScale.Fit,
            // Edge to edge, under the system bars: inside them, a picture dragged to its edge stopped at
            // the bar and left a band of black past it.
            modifier = Modifier.fillMaxSize()
                .pointerInput(image) {
                    detectTapGestures(onDoubleTap = { tap ->
                        val next = if (scale > 1f) 1f else 2.5f
                        offset = zoomed(tap - size.center.toOffset(), next, Offset.Zero, size)
                        scale = next
                    })
                }
                .pointerInput(image) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val next = (scale * zoom).coerceIn(1f, 4f)
                        offset = zoomed(centroid - size.center.toOffset(), next, pan, size)
                        scale = next
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
        // A scrim behind the glyph: plain white vanishes over a light photo.
        Box(
            Modifier.align(Alignment.TopStart)
                .safeDrawingPadding()
                .size(48.dp)
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f)),
        )
        GlyphButton(
            Glyph.CLOSE,
            S.a11yClose,
            onClose,
            Modifier.align(Alignment.TopStart).safeDrawingPadding(),
            tint = Color.White,
        )
    }
}
