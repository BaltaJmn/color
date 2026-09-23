package com.baltajmn.color.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.i18n.S
import com.baltajmn.color.share.PosterStyle
import com.baltajmn.color.share.renderPoster

/** The year as a picture. Looking is free, so the wall is met with the poster already in view. */
@Composable
fun PosterScreen(year: Int, onClose: () -> Unit) {
    val days = ChromaRepository.journal.mapValues { it.value.color }
    val settings = ChromaRepository.settings
    var style by remember { mutableStateOf(PosterStyle.Grid) }
    val measurer = rememberPixelMeasurer()
    val picture = remember(style, year, days, settings.watermark) { renderPoster(style, year, days, settings.watermark, measurer) }
    val styleLabels = listOf(S.posterGrid, S.posterStrip, S.posterWallpaper)

    PictureScreen(picture, "$year, ${styleLabels[style.ordinal]}", onClose, locked = !settings.pro, onLocked = { Paywall.open = true }) {
        Segmented(styleLabels, style.ordinal) { style = PosterStyle.entries[it] }
        Spacer(Modifier.height(16.dp))
    }
}
