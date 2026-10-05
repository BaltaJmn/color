package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.baltajmn.color.color.BY_KEY
import com.baltajmn.color.color.Versus
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.yearStats
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.i18n.S
import com.baltajmn.color.ui.theme.Styles

/**
 * The year in a few short sentences (Pro, #41). No charts and no numbers: a dashboard would turn a
 * year of looking into a year of scoring.
 */
@Composable
fun StatsScreen(year: Int, onClose: () -> Unit) {
    val journal = ChromaRepository.journal
    val stats = remember(year, journal) { yearStats(year, journal) }

    Overlay(S.statsTitle(year), onClose) {
        Spacer(Modifier.height(8.dp))
        if (stats.isEmpty) Line(S.statsEmpty)
        stats.warmest?.let { Line(S.statsWarmest(it)) }
        stats.coldest?.let { Line(S.statsColdest(it)) }
        stats.repeated?.let { Line(S.statsRepeated(S.colorName(it)), BY_KEY[it]?.hex) }
        stats.greyest?.let { Line(S.statsGreyest(it.months.first(), it.months.last())) }
        stats.versus?.let {
            Line(
                when (it) {
                    Versus.Warmer -> S.statsWarmer(year, year - 1)
                    Versus.Cooler -> S.statsCooler(year, year - 1)
                    Versus.Alike -> S.statsAlike(year, year - 1)
                },
            )
        }
    }
}

/** One sentence to a card: read one at a time, like a short list of findings, not a paragraph. */
@Composable
private fun Line(text: String, swatch: String? = null) {
    Row(
        Modifier.fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        swatch?.let {
            Box(Modifier.size(22.dp).clip(CircleShape).background(colorOf(it)).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape))
            Spacer(Modifier.width(14.dp))
        }
        Text(text, style = Styles.body)
    }
}
