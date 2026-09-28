package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.i18n.S
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles
import com.baltajmn.color.ui.theme.screenInsets
import kotlinx.datetime.LocalDate

@Composable
fun YearScreen(today: LocalDate, onOpenDay: (LocalDate) -> Unit, onPoster: (Int) -> Unit, onStats: (Int) -> Unit) {
    val days = ChromaRepository.journal.mapValues { it.value.color }
    val years = remember(days.keys) { (days.keys.map { it.take(4).toInt() } + today.year).distinct().sorted() }
    var year by rememberSaveable { mutableStateOf(today.year) }
    var strip by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().screenInsets().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
            // The year as the same light numeral as the day on Today: the two screens are one instrument.
            Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                val i = years.indexOf(year)
                Column(Modifier.weight(1f)) {
                    Text(S.navYear.uppercase(), style = Styles.eyebrow)
                    Text(year.toString(), style = Styles.numeral, modifier = Modifier.semantics { heading() })
                }
                if (years.size > 1) {
                    GlyphButton(Glyph.BACK, S.a11yPreviousYear, { year = years[i - 1] }, enabled = i > 0, tint = MaterialTheme.colorScheme.onBackground)
                    GlyphButton(Glyph.FORWARD, S.a11yNextYear, { year = years[i + 1] }, enabled = i < years.lastIndex, tint = MaterialTheme.colorScheme.onBackground)
                }
            }

            val inYear = days.keys.any { it.startsWith("$year-") }
            if (inYear) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionTile(Glyph.POSTER, S.poster, { onPoster(year) }, Modifier.weight(1f))
                    // Pro, and asked for honestly: the tile is there, the phrases open after buying.
                    ActionTile(
                        Glyph.WORDS,
                        S.stats,
                        { if (ChromaRepository.settings.pro) onStats(year) else Paywall.open = true },
                        Modifier.weight(1f),
                        pro = !ChromaRepository.settings.pro,
                    )
                }
                Spacer(Modifier.height(20.dp))
                Segmented(listOf(S.viewGrid, S.viewStrip), if (strip) 1 else 0) { strip = it == 1 }
                Spacer(Modifier.height(16.dp))
            }

            when {
                !inYear -> Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).padding(20.dp),
                ) { Text(S.yearEmpty, style = Styles.muted) }
                strip -> YearStrip(year, days)
                else -> Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface)
                        .padding(start = 8.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
                ) { YearGrid(year, days, today, onOpenDay) }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Two or three options side by side, the chosen one filled. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(colors.surfaceVariant).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (on) colors.primary else colors.surfaceVariant)
                    .semantics { this.selected = on }
                    .clickable(role = Role.Tab) { onSelect(i) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = Styles.label.copy(
                        color = if (on) colors.onPrimary else colors.onSurfaceVariant,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                )
            }
        }
    }
}
