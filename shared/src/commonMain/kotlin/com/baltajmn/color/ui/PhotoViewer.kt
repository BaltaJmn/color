package com.baltajmn.color.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.baltajmn.color.i18n.S

/** The photo behind a card, whole, on black. */
@Composable
fun PhotoViewer(image: ImageBitmap, onClose: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Image(image, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().safeDrawingPadding())
        GlyphButton(
            Glyph.CLOSE,
            S.a11yClose,
            onClose,
            Modifier.align(Alignment.TopStart).safeDrawingPadding(),
            tint = Color.White,
        )
    }
}
