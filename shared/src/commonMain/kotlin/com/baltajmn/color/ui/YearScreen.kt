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
fun YearScreen(today: LocalDate, onOpenDay: (LocalDate) -> Unit, onPoster: ((Int) -> Unit)?) {
    val days = ChromaRepository.journal.mapValues { it.value.color }
    val years = remember(days.keys) { (days.keys.map { it.take(4).toInt() } + today.year).distinct().sorted() }
    var year by rememberSaveable { mutableStateOf(today.year) }
    var strip by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().screenInsets().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                val i = years.indexOf(year)
                if (years.size > 1) GlyphButton(Glyph.BACK, S.a11yPreviousYear, { year = years[i - 1] }, enabled = i > 0)
                Text(year.toString(), style = Styles.title, modifier = Modifier.weight(1f), textAlign = if (years.size > 1) TextAlign.Center else TextAlign.Start)
                if (years.size > 1) GlyphButton(Glyph.FORWARD, S.a11yNextYear, { year = years[i + 1] }, enabled = i < years.lastIndex)
            }
            Segmented(listOf(S.viewGrid, S.viewStrip), if (strip) 1 else 0) { strip = it == 1 }
            Spacer(Modifier.height(20.dp))

            val inYear = days.keys.any { it.startsWith("$year-") }
            when {
                !inYear -> Text(S.yearEmpty, style = Styles.muted, modifier = Modifier.padding(vertical = 24.dp))
                strip -> YearStrip(year, days)
                else -> YearGrid(year, days, today, onOpenDay)
            }
            if (inYear && onPoster != null) {
                Spacer(Modifier.height(24.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { OutlinedAction(S.poster, { onPoster(year) }) }
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
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(colors.surfaceVariant).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (on) colors.surface else colors.surfaceVariant)
                    .semantics { this.selected = on }
                    .clickable(role = Role.Tab) { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = Styles.label.copy(color = colors.onBackground, fontWeight = if (on) FontWeight.Medium else FontWeight.Normal))
            }
        }
    }
}
