package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
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
    var chosen by rememberSaveable { mutableStateOf(today.year) }
    // Deleting the last day of a year takes the year away: then the one before it, never a blank
    // year with no arrows to leave it.
    val year = if (chosen in years) chosen else years.lastOrNull { it <= chosen } ?: today.year
    var strip by rememberSaveable { mutableStateOf(false) }

    val inYear = days.keys.any { it.startsWith("$year-") }
    BoxWithConstraints(Modifier.fillMaxSize().screenInsets()) {
        val viewport = maxHeight
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
                // The year is the screen: the grid takes what the header and the actions leave of it,
                // so the whole year is seen at once instead of from the 1st to the 17th.
                FitBelow(
                    viewport - BOTTOM,
                    restMin = GRID_MIN,
                    top = {
                        // The year as the same light numeral as the day on Today, at the same height: the two
                        // screens are one instrument. No "My year" above it: the bar below already says where
                        // this is, and every line spent here is a row of days less.
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            val i = years.indexOf(year)
                            // Grows like the day on Today and no further: at 2x the four figures took a third of the screen.
                            val density = LocalDensity.current
                            CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale.coerceAtMost(1.15f))) {
                                Text(year.toString(), style = Styles.numeral, modifier = Modifier.weight(1f).semantics { heading() })
                            }
                            // The other view, named by what it switches to.
                            if (inYear) {
                                GlyphButton(
                                    if (strip) Glyph.YEAR else Glyph.STRIP,
                                    if (strip) S.viewGrid else S.viewStrip,
                                    { strip = !strip },
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                            if (years.size > 1) {
                                GlyphButton(Glyph.BACK, S.a11yPreviousYear, { chosen = years[i - 1] }, enabled = i > 0, tint = MaterialTheme.colorScheme.onBackground)
                                GlyphButton(Glyph.FORWARD, S.a11yNextYear, { chosen = years[i + 1] }, enabled = i < years.lastIndex, tint = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                        if (inYear) {
                            // Each at its own width, not halves: halves sent "In words" and its Pro tag to a
                            // second line on most phones, and two stacked buttons cost the grid more rows.
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedAction(S.poster, { onPoster(year) }, glyph = Glyph.POSTER)
                                // Pro, and asked for honestly: the button says so, and after buying the
                                // phrases open on their own instead of asking for a second tap.
                                OutlinedAction(
                                    S.stats,
                                    {
                                        if (ChromaRepository.settings.pro) {
                                            onStats(year)
                                        } else {
                                            Paywall.onPro = { onStats(year) }
                                            Paywall.open = true
                                        }
                                    },
                                    Modifier.weight(1f, fill = false),
                                    glyph = Glyph.WORDS,
                                    pro = !ChromaRepository.settings.pro,
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    },
                ) {
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
                }
                Spacer(Modifier.height(BOTTOM))
            }
        }
    }
}

private val BOTTOM = 16.dp

// The grid card at its flattest rows and tightest gaps (YearGrid): below this the screen scrolls instead.
private val GRID_MIN = 430.dp

/**
 * [top] at its own height, then [rest] measured within what is left of [height], or within
 * [restMin] when less is left: then the screen scrolls.
 */
@Composable
private fun FitBelow(height: Dp, restMin: Dp, top: @Composable () -> Unit, rest: @Composable () -> Unit) {
    Layout(contents = listOf(top, rest)) { (tops, rests), constraints ->
        val loose = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        val above = tops.map { it.measure(loose) }
        val used = above.sumOf { it.height }
        val left = maxOf(height.roundToPx() - used, restMin.roundToPx())
        val below = rests.map { it.measure(loose.copy(maxHeight = left)) }
        layout(constraints.maxWidth, used + below.sumOf { it.height }) {
            var y = 0
            (above + below).forEach {
                it.place(0, y)
                y += it.height
            }
        }
    }
}

/** Two or three options side by side, the chosen one filled. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Same height for all: a label that wraps to two lines (Spanish "Fondo de pantalla" at 360 dp)
    // makes the row taller instead of one option standing out.
    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(RoundedCornerShape(26.dp)).background(colors.surfaceVariant).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f)
                    .fillMaxHeight()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (on) colors.primary else colors.surfaceVariant)
                    .semantics { this.selected = on }
                    .clickable(role = Role.Tab) { onSelect(i) }
                    // Air on the sides too: "Fondo de pantalla" in a third of 360 dp touched the edge.
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    textAlign = TextAlign.Center,
                    style = Styles.label.copy(
                        color = if (on) colors.onPrimary else colors.onSurfaceVariant,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                )
            }
        }
    }
}
