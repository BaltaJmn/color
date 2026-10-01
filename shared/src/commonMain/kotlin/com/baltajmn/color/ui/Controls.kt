package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.color.i18n.S
import com.baltajmn.color.ui.theme.Styles

/**
 * True while the lock covers the app. Dialogs and menus are windows of their own and would float
 * above it, so they wait for the unlock instead, keeping their state.
 */
val LocalLocked = compositionLocalOf { false }

/**
 * Stops touches from reaching anything under a full-screen layer. Being hit is enough: Compose stops
 * at the first sibling with a pointer node. Consuming the events as well would cancel the taps of the
 * buttons on the layer itself whenever the finger moves a little.
 */
fun Modifier.blockTouches(): Modifier = pointerInput(Unit) {}

/** The one filled button of a screen: ink on paper, a pill 52 high (docs/pantallas.md 1). */
@Composable
fun PrimaryAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glyph: Glyph? = null,
) {
    val colors = MaterialTheme.colorScheme
    val ink = if (enabled) colors.onPrimary else colors.onSurfaceVariant
    Box(
        modifier.heightIn(min = 52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(if (enabled) colors.primary else colors.surfaceVariant)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            glyph?.let {
                GlyphIcon(it, tint = ink)
                Spacer(Modifier.width(10.dp))
            }
            ActionLabel(label, Styles.body.copy(color = ink, fontWeight = FontWeight.SemiBold))
        }
    }
}

/**
 * A button with a border: 48 high, a pill, 1 dp of outline. [pro] says before the tap that it opens
 * the paywall, instead of a Share that turns out to be a purchase.
 */
@Composable
fun OutlinedAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pro: Boolean = false,
    glyph: Glyph? = null,
) {
    Box(
        modifier.heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            glyph?.let {
                GlyphIcon(it, size = 18.dp, tint = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.width(8.dp))
            }
            ActionLabel(label, Styles.body.copy(fontWeight = FontWeight.Medium))
            if (pro) ProTag(Modifier.padding(start = 8.dp))
        }
    }
}

/**
 * A button's words, centred: two buttons side by side with large text leave each little room, and a
 * word that does not fit shrinks instead of breaking ("Compar / tir"). The line count follows the words.
 */
@Composable
private fun RowScope.ActionLabel(label: String, style: TextStyle) {
    Text(
        label,
        style = style,
        textAlign = TextAlign.Center,
        maxLines = label.count { it == ' ' } + 1,
        autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = style.fontSize),
        modifier = Modifier.weight(1f, fill = false),
    )
}

/**
 * Buttons side by side, sharing the width and one height, while every label fits on one line at its
 * own size; otherwise one above the other at full width. With large text a shared half is too narrow
 * for "Compartir" even shrunk, and a cut word is worse than a taller screen.
 */
@Composable
fun ActionRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content, modifier) { buttons, constraints ->
        val gap = 10.dp.roundToPx()
        val width = constraints.maxWidth
        val share = (width - gap * (buttons.size - 1)) / buttons.size.coerceAtLeast(1)
        if (buttons.all { it.maxIntrinsicWidth(Constraints.Infinity) <= share }) {
            val height = buttons.maxOf { it.minIntrinsicHeight(share) }
            val placed = buttons.map { it.measure(Constraints.fixed(share, height)) }
            layout(width, height) { placed.forEachIndexed { i, p -> p.place(i * (share + gap), 0) } }
        } else {
            val placed = buttons.map { it.measure(Constraints(minWidth = width, maxWidth = width)) }
            layout(width, placed.sumOf { it.height } + gap * (placed.size - 1).coerceAtLeast(0)) {
                var y = 0
                placed.forEach {
                    it.place(0, y)
                    y += it.height + gap
                }
            }
        }
    }
}

/** A text button: 48 high, no background. [destructive] marks what cannot be undone. */
@Composable
fun TextAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
    quiet: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier.heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = Styles.body.copy(
                fontWeight = if (quiet) FontWeight.Normal else FontWeight.SemiBold,
                color = when {
                    !enabled -> colors.onSurfaceVariant.copy(alpha = 0.5f)
                    destructive -> colors.error
                    quiet -> colors.onSurfaceVariant
                    else -> colors.onBackground
                },
            ),
        )
    }
}

/** "Pro", as a small tag that sits after whatever opens the paywall. */
@Composable
fun ProTag(modifier: Modifier = Modifier) {
    Text(
        S.proTag.uppercase(),
        style = Styles.eyebrow.copy(color = MaterialTheme.colorScheme.onBackground),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/**
 * The head of a dated screen: the day as a big light numeral, like the readout of an instrument,
 * with the weekday and the month stacked beside it. [actions] sit at the end, level with it.
 */
@Composable
fun Masthead(day: Int, above: String, below: String, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
    // With large text the numeral and the buttons leave the words little room, and "septiembre" has no
    // space to wrap at: the numeral grows less, and a word that still does not fit shrinks, never breaks.
    val density = LocalDensity.current
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale.coerceAtMost(1.15f))) {
            Text(day.toString(), style = Styles.numeral, modifier = Modifier.semantics { heading() })
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            val eyebrow = Styles.eyebrow
            val title = Styles.title.copy(fontWeight = FontWeight.Normal)
            Text(above.uppercase(), style = eyebrow, maxLines = 1, autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = eyebrow.fontSize))
            Text(below, style = title, maxLines = if (below.contains(' ')) 2 else 1, autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = title.fontSize))
        }
        actions()
    }
}

/** A square of surface with a glyph in it: the start of a settings row, the head of a tile. */
@Composable
fun GlyphTile(glyph: Glyph, modifier: Modifier = Modifier) {
    Box(
        modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) { GlyphIcon(glyph, size = 18.dp, tint = MaterialTheme.colorScheme.onBackground) }
}

/** A tile of surface that does one thing: its glyph, its name and, when it opens the paywall, the Pro tag. */
@Composable
fun ActionTile(glyph: Glyph, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, pro: Boolean = false) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            GlyphTile(glyph)
            Spacer(Modifier.weight(1f))
            if (pro) ProTag()
        }
        Spacer(Modifier.heightIn(min = 14.dp))
        Text(label, style = Styles.body.copy(fontWeight = FontWeight.Medium))
    }
}

@Composable
fun Ask(
    title: String?,
    text: String,
    confirm: String,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    destructive: Boolean = false,
) {
    if (LocalLocked.current) return
    AlertDialog(
        onDismissRequest = onDismiss ?: onConfirm,
        title = title?.let { { Text(it, style = Styles.title) } },
        text = { Text(text, style = Styles.body) },
        confirmButton = { TextAction(confirm, onClick = onConfirm, destructive = destructive) },
        dismissButton = onDismiss?.let { { TextAction(S.cancel, onClick = it, quiet = true) } },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
    )
}

/**
 * A quiet card at the top of a screen. The last action is the one it offers, in ink; the ones
 * before it are the ways to say no, in gray.
 */
@Composable
fun Notice(message: String, vararg actions: Pair<String, () -> Unit>, glyph: Glyph? = null) {
    Column(
        Modifier.fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = if (actions.isEmpty()) 16.dp else 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            glyph?.let {
                GlyphTile(it)
                Spacer(Modifier.width(12.dp))
            }
            Text(message, style = Styles.body, modifier = Modifier.weight(1f).padding(end = 8.dp))
        }
        if (actions.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                actions.forEachIndexed { i, (label, go) -> TextAction(label, go, quiet = i < actions.lastIndex) }
            }
        }
    }
}
