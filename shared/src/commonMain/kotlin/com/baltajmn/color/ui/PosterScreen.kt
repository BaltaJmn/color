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
    val styleLabels = listOf(S.posterGrid, S.posterStrip, S.posterStory, S.posterWallpaper)

    PictureScreen(S.poster, picture, "$year, ${styleLabels[style.ordinal]}", onClose, locked = !settings.pro, onLocked = { Paywall.open = true }) {
        // Two rows of two: four labels in one row put "Hintergrund" or "Achtergrond" over the edge at 360 dp.
        // Each row is a Segmented of its own, and a selection outside it leaves it with none lit.
        Segmented(styleLabels.take(2), style.ordinal) { style = PosterStyle.entries[it] }
        Spacer(Modifier.height(8.dp))
        Segmented(styleLabels.drop(2), style.ordinal - 2) { style = PosterStyle.entries[it + 2] }
        Spacer(Modifier.height(16.dp))
    }
}
