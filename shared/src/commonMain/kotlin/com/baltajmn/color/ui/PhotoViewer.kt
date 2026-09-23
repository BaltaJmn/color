package com.baltajmn.color.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.baltajmn.color.i18n.S

/** The photo behind a card, whole, on black. */
@Composable
fun PhotoViewer(image: ImageBitmap, onClose: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Image(image, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().safeDrawingPadding())
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
