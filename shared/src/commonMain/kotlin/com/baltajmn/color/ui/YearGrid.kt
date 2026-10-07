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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.baltajmn.color.color.blendsInto
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.nearestName
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.isoKey
import com.baltajmn.color.share.STRIP_MIN_DAYS
import com.baltajmn.color.ui.theme.Styles
import kotlin.math.min
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private val LEFT = 20.dp
private val GAP = 3.dp
private val CELL_MAX = 24.dp
private val ROW_MIN = 10.dp
// Rows flatter than this close the gaps between days first: a 16:9 phone (411x731 dp) fits the year.
private val ROW_TIGHT = 14.dp
private val GAP_TIGHT = 2.dp
private val HEAD = 20.dp
private const val ROWS = 31

/**
 * The year, a column per month and a row per day, each cell painted its day's color. A blank day
 * is the neutral of the theme, never a warning. Painted in one Canvas; the taps and the screen
 * reader ride on top, one node per day that has a color. [days] maps an ISO date to its color, so
 * the same grid draws my year and a friend's.
 *
 * Given a bounded height, the 31 rows fit in it: the year at a glance, with cells flatter than they
 * are wide, like the chips of a paint strip, down to [ROW_MIN]; below that it keeps [ROW_MIN] and the
 * screen scrolls. Unbounded, the cells are square.
 */
@Composable
fun YearGrid(year: Int, days: Map<String, String>, today: LocalDate, onOpenDay: (LocalDate) -> Unit) {
    val empty = MaterialTheme.colorScheme.surfaceVariant
    val card = MaterialTheme.colorScheme.surface
    val edge = MaterialTheme.colorScheme.outline
    val ring = MaterialTheme.colorScheme.onBackground
    val caption = Styles.caption
    val initials = remember { S.monthInitials() }
    val names = remember { S.monthNames() }
    val monthDays = remember(year) {
        (1..12).map { m -> LocalDate(year, m, 1).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).day }
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cell = min(CELL_MAX.value, (maxWidth.value - LEFT.value - 11 * GAP.value) / 12).dp
        val bounded = constraints.hasBoundedHeight
        fun fit(gap: Dp) = (maxHeight - HEAD - gap * (ROWS - 1)) / ROWS
        val gapY = if (bounded && fit(GAP) < ROW_TIGHT) GAP_TIGHT else GAP
        val row = if (bounded) fit(gapY).coerceIn(minOf(ROW_MIN, cell), cell) else cell
        val step = cell + GAP
        val rowStep = row + gapY
        val height = HEAD + row * ROWS + gapY * (ROWS - 1)
        // On a wide phone the cells hit CELL_MAX before the width runs out: center the block
        // (day numbers included) instead of leaving the spare width on the right.
        val inset = ((maxWidth - LEFT - step * 12 + GAP) / 2).coerceAtLeast(0.dp)
        fun x(month: Int) = inset + LEFT + step * (month - 1)
        fun y(day: Int) = HEAD + rowStep * (day - 1)

        // Font scale fixed to 1: the grid geometry is fixed dp, so labels that grew with the
        // system font size would overflow their cells. The cells themselves are unaffected,
        // since dp-to-px conversion depends on density, not on this fontScale override.
        CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1f)) {
            val measurer = rememberTextMeasurer()
            Canvas(Modifier.fillMaxWidth().height(height)) {
                val box = Size(cell.toPx(), row.toPx())
                val radius = CornerRadius(3.dp.toPx())
                initials.forEachIndexed { i, label ->
                    val laid = measurer.measure(label, caption)
                    drawText(laid, topLeft = Offset(x(i + 1).toPx() + (box.width - laid.size.width) / 2, HEAD.toPx() - laid.size.height - 4.dp.toPx()))
                }
                listOf(1, 10, 20, 30).forEach { day ->
                    val laid = measurer.measure(day.toString(), caption)
                    drawText(laid, topLeft = Offset((inset + LEFT).toPx() - 4.dp.toPx() - laid.size.width, y(day).toPx() + (box.height - laid.size.height) / 2))
                }
                for (month in 1..12) {
                    for (day in 1..monthDays[month - 1]) {
                        val date = LocalDate(year, month, day)
                        val at = Offset(x(month).toPx(), y(day).toPx())
                        val hex = days[date.isoKey()]
                        when {
                            hex != null -> {
                                val fill = colorOf(hex)
                                drawRoundRect(fill, at, box, radius)
                                if (blendsInto(fill, card) || blendsInto(fill, empty)) {
                                    drawRoundRect(edge, at, box, radius, style = Stroke(1.dp.toPx()))
                                }
                            }
                            date > today -> drawRoundRect(empty.copy(alpha = 0.45f), at, box, radius)
                            else -> drawRoundRect(empty, at, box, radius)
                        }
                        if (date == today) {
                            val out = 2.dp.toPx()
                            drawRoundRect(ring, Offset(at.x - out, at.y - out), Size(box.width + out * 2, box.height + out * 2), CornerRadius(5.dp.toPx()), Stroke(1.5.dp.toPx()))
                        }
                    }
                }
            }
        }

        // Only days with a color open anything, so only they are nodes. Each takes its cell and half
        // the gap around it, so the grid has no dead spots between days. A month is a traversal group
        // headed by its name: a screen reader walks the year month by month, down each column, and can
        // jump from month to month by heading.
        val byMonth = days.keys.filter { it.startsWith("$year-") }.sorted().map(LocalDate::parse).groupBy { it.month.ordinal + 1 }
        for ((month, dates) in byMonth) {
            Box(
                Modifier.offset { IntOffset((x(month) - GAP / 2).roundToPx(), 0) }
                    .size(step, height + gapY)
                    .semantics { isTraversalGroup = true },
            ) {
                Box(Modifier.size(step, HEAD).semantics { heading(); contentDescription = names[month - 1] })
                for (date in dates) {
                    val hex = days.getValue(date.isoKey())
                    Box(
                        Modifier.offset { IntOffset(0, (y(date.day) - gapY / 2).roundToPx()) }
                            .size(step, rowStep)
                            .clickable(role = Role.Button) { onOpenDay(date) }
                            .semantics {
                                contentDescription = S.a11yDay(date, nearestName(hex).key) + if (date == today) ", ${S.navToday}" else ""
                            },
                    )
                }
            }
        }
    }
}

/**
 * The year as thin columns, one per day with a color, blank days left out: its barcode. As My year it
 * is the screen and says so to a screen reader; as a band ([fill]) it is decoration and stays quiet.
 */
@Composable
fun YearStrip(year: Int, days: Map<String, String>, modifier: Modifier = Modifier, fill: Boolean = false) {
    val colors = remember(year, days) { days.filterKeys { it.startsWith("$year-") }.entries.sortedBy { it.key }.map { colorOf(it.value) } }
    // [fill]: take the height the caller gives, as a thin band, instead of the 4:5 picture of My year.
    val shape = if (fill) {
        Modifier
    } else {
        Modifier.aspectRatio(4f / 5f).clip(RoundedCornerShape(24.dp)).semantics { contentDescription = S.a11yYearStrip(year, colors.size) }
    }
    // A band is decoration and compresses whatever it has; the picture keeps room for the days to come.
    val slots = if (fill) colors.size else maxOf(colors.size, STRIP_MIN_DAYS)
    val empty = MaterialTheme.colorScheme.surfaceVariant
    val seam = MaterialTheme.colorScheme.outline
    Canvas(modifier.fillMaxWidth().then(shape)) {
        if (colors.isEmpty()) return@Canvas
        val w = size.width / slots
        if (slots > colors.size) drawRect(empty)
        colors.forEachIndexed { i, c -> drawRect(c, Offset(i * w, 0f), Size(w + 0.5f, size.height)) }
        // A near white last day would run into the blank and read as one day fewer.
        if (slots > colors.size && blendsInto(colors.last(), empty)) {
            drawLine(seam, Offset(colors.size * w, 0f), Offset(colors.size * w, size.height), 1.dp.toPx())
        }
    }
}
