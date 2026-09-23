package com.baltajmn.color.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.nearestName
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.isoKey
import com.baltajmn.color.ui.theme.Styles
import kotlin.math.min
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private val LEFT = 20.dp
private val GAP = 3.dp
private val CELL_MAX = 24.dp
private const val ROWS = 31

/**
 * The year, a column per month and a row per day, each cell painted its day's color. A blank day
 * is the neutral of the theme, never a warning. Painted in one Canvas; the taps and the screen
 * reader ride on top, one node per day that has a color. [days] maps an ISO date to its color, so
 * the same grid draws my year and a friend's.
 */
@Composable
fun YearGrid(year: Int, days: Map<String, String>, today: LocalDate, onOpenDay: (LocalDate) -> Unit) {
    val empty = MaterialTheme.colorScheme.surfaceVariant
    val ring = MaterialTheme.colorScheme.onBackground
    val caption = Styles.caption
    val initials = remember { S.monthInitials() }
    val monthDays = remember(year) {
        (1..12).map { m -> LocalDate(year, m, 1).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).day }
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cell = min(CELL_MAX.value, (maxWidth.value - LEFT.value - 11 * GAP.value) / 12).dp
        val head = 20.dp
        val step = cell + GAP
        fun x(month: Int) = LEFT + step * (month - 1)
        fun y(day: Int) = head + step * (day - 1)

        // Font scale fixed to 1: the grid geometry is fixed dp, so labels that grew with the
        // system font size would overflow their cells. The cells themselves are unaffected,
        // since dp-to-px conversion depends on density, not on this fontScale override.
        CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1f)) {
            val measurer = rememberTextMeasurer()
            Canvas(Modifier.fillMaxWidth().height(head + cell * ROWS + GAP * (ROWS - 1))) {
                val side = cell.toPx()
                val radius = CornerRadius(3.dp.toPx())
                initials.forEachIndexed { i, label ->
                    val laid = measurer.measure(label, caption)
                    drawText(laid, topLeft = Offset(x(i + 1).toPx() + (side - laid.size.width) / 2, head.toPx() - laid.size.height - 4.dp.toPx()))
                }
                listOf(1, 10, 20, 30).forEach { day ->
                    val laid = measurer.measure(day.toString(), caption)
                    drawText(laid, topLeft = Offset(LEFT.toPx() - 4.dp.toPx() - laid.size.width, y(day).toPx() + (side - laid.size.height) / 2))
                }
                for (month in 1..12) {
                    for (day in 1..monthDays[month - 1]) {
                        val date = LocalDate(year, month, day)
                        val at = Offset(x(month).toPx(), y(day).toPx())
                        val box = Size(side, side)
                        val hex = days[date.isoKey()]
                        when {
                            hex != null -> drawRoundRect(colorOf(hex), at, box, radius)
                            date > today -> drawRoundRect(empty.copy(alpha = 0.45f), at, box, radius)
                            else -> drawRoundRect(empty, at, box, radius)
                        }
                        if (date == today) {
                            val out = 2.dp.toPx()
                            drawRoundRect(ring, Offset(at.x - out, at.y - out), Size(side + out * 2, side + out * 2), CornerRadius(5.dp.toPx()), Stroke(1.5.dp.toPx()))
                        }
                    }
                }
            }
        }

        // Only days with a color open anything, so only they are nodes; sorted by ISO key so
        // screen readers walk the year in date order rather than map insertion order.
        for ((key, hex) in days.entries.sortedBy { it.key }) {
            val date = LocalDate.parse(key)
            if (date.year != year) continue
            Box(
                Modifier.offset { IntOffset(x(date.month.ordinal + 1).roundToPx(), y(date.day).roundToPx()) }
                    .size(cell)
                    .clickable(role = Role.Button) { onOpenDay(date) }
                    .semantics { contentDescription = S.a11yDay(date, nearestName(hex).key) },
            )
        }
    }
}

/** The year as thin columns, one per day with a color, blank days left out: its barcode. */
@Composable
fun YearStrip(year: Int, days: Map<String, String>, modifier: Modifier = Modifier) {
    val colors = remember(year, days) { days.filterKeys { it.startsWith("$year-") }.entries.sortedBy { it.key }.map { colorOf(it.value) } }
    Canvas(modifier.fillMaxWidth().aspectRatio(4f / 5f).clip(RoundedCornerShape(20.dp))) {
        if (colors.isEmpty()) return@Canvas
        val w = size.width / colors.size
        colors.forEachIndexed { i, c -> drawRect(c, Offset(i * w, 0f), Size(w + 0.5f, size.height)) }
    }
}
