package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
    val months = remember { S.monthNames() }

    Overlay(S.statsTitle(year), onClose) {
        Spacer(Modifier.height(8.dp))
        if (stats.isEmpty) Line(S.statsEmpty)
        stats.warmest?.let { Line(S.statsWarmest(months[it - 1])) }
        stats.coldest?.let { Line(S.statsColdest(months[it - 1])) }
        stats.repeated?.let { Line(S.statsRepeated(S.colorName(it)), BY_KEY[it]?.hex) }
        stats.greyest?.let { Line(S.statsGreyest(months[it.months.first() - 1], months[it.months.last() - 1])) }
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

@Composable
private fun Line(text: String, swatch: String? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        swatch?.let {
            Box(Modifier.size(14.dp).clip(CircleShape).background(colorOf(it)))
            Spacer(Modifier.width(12.dp))
        }
        Text(text, style = Styles.body)
    }
}
