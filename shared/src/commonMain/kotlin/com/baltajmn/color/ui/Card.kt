package com.baltajmn.color.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.inkColorFor
import com.baltajmn.color.color.weekHit
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.Photos
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.ui.theme.CARD_RADIUS
import com.baltajmn.color.ui.theme.Styles
import kotlin.math.hypot
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

/**
 * The card (docs/pantallas.md 6): the color to the edges, its name, and the photo small in the
 * bottom right. The same component in Today, the open day, the feed and a friend's year, so a
 * color looks the same wherever it is seen.
 *
 * [photo] defaults to the local file; the feed passes a friend's downloaded one, or null when the
 * day was shared as color only or its photo has already left the server.
 *
 * [reveal] is for the moment of picking: the color spreads out of the photo to the edges, because
 * that is where it came from. Everywhere else the card is simply there. Reduce Motion (iOS) and the
 * animation scale (Android) reach this through Compose, so it turns itself off for whoever asked.
 */
@Composable
fun ChromaCard(
    entry: ChromaEntry,
    date: LocalDate,
    modifier: Modifier = Modifier,
    author: String? = null,
    compact: Boolean = false,
    photo: ImageBitmap? = Photos.get(entry.photo),
    onPhoto: ((ImageBitmap) -> Unit)? = null,
    onAuthor: (() -> Unit)? = null,
    reveal: Boolean = false,
    marks: @Composable RowScope.() -> Unit = {},
) {
    // Changing the color during the day fades between the two instead of jumping.
    val fill by animateColorAsState(colorOf(entry.color), tween(400))
    val ink by animateColorAsState(inkColorFor(entry.color), tween(400))
    val spread = remember { Animatable(if (reveal) 0f else 1f) }
    LaunchedEffect(Unit) { spread.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
    // The words arrive once the color has mostly covered the card, never on the bare background.
    val words = ((spread.value - 0.5f) / 0.5f).coerceIn(0f, 1f)
    val pad = if (compact) 16.dp else 24.dp
    val thumbMargin = if (compact) 12.dp else 20.dp
    @Suppress("DEPRECATION") // LocalClipboard needs a platform ClipEntry for plain text; this does not.
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var copied by remember(entry.color) { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }
    // The card is a fixed 4:5: text that grew without a limit would run under the photo and the date.
    // Up to 1.3 it grows like everywhere else, and the name shrinks to fit before breaking a word.
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale.coerceAtMost(1.3f))) {
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .clip(RoundedCornerShape(CARD_RADIUS))
            .drawBehind {
                if (spread.value >= 1f) {
                    drawRect(fill)
                } else {
                    val side = size.width * 0.3f
                    val margin = thumbMargin.toPx()
                    val origin = Offset(size.width - margin - side / 2, size.height - margin - side / 2)
                    drawCircle(fill, radius = hypot(origin.x, origin.y) * spread.value, center = origin)
                }
            },
    ) {
        val thumb = maxWidth * 0.3f
        val name = S.colorName(entry.name)
        Column(
            Modifier.align(Alignment.TopStart).padding(pad).padding(end = 40.dp).alpha(words)
                // Copies the code, for whoever wants this exact color in another app. Only on your own
                // card: on a friend's, the top of the card is theirs, not a control.
                .then(
                    if (author != null) Modifier
                    else Modifier.clickable(role = Role.Button, onClickLabel = S.a11yCopyHex) {
                        @Suppress("DEPRECATION")
                        clipboard.setText(AnnotatedString(entry.color))
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        copied = true
                    },
                ),
        ) {
            val big = if (compact) Styles.title else Styles.display.copy(lineHeight = 1.1.em, fontWeight = FontWeight.SemiBold)
            // One line per word at most: a word that does not fit shrinks instead of breaking. A word too
            // long even at the smallest size breaks after all, rather than losing its end.
            var broke by remember(name) { mutableStateOf(false) }
            val shrinks = !compact && !broke
            Text(
                name,
                style = big.copy(color = ink).let { if (broke) it.copy(fontSize = 24.sp) else it },
                maxLines = if (shrinks) name.count { it == ' ' || it == '-' } + 1 else Int.MAX_VALUE,
                autoSize = if (shrinks) TextAutoSize.StepBased(minFontSize = 24.sp, maxFontSize = 40.sp) else null,
                onTextLayout = { if (shrinks && it.hasVisualOverflow) broke = true },
            )
            // Tabular figures and a little air: it reads as a code, like the one on a paint chip.
            Text(if (copied) S.copied else entry.color, style = Styles.code.copy(color = ink), modifier = Modifier.padding(top = 2.dp))
            entry.word?.let {
                Text(
                    it,
                    style = Styles.body.copy(color = ink, fontStyle = FontStyle.Italic),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
        Row(
            Modifier.align(Alignment.TopEnd).padding(pad).alpha(words),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = marks,
        )
        Column(Modifier.align(Alignment.BottomStart).padding(pad).padding(end = thumb).alpha(words)) {
            author?.let {
                Box(
                    Modifier.heightIn(min = 48.dp)
                        .then(if (onAuthor != null) Modifier.clickable(role = Role.Button, onClick = onAuthor) else Modifier),
                    contentAlignment = Alignment.CenterStart,
                ) { Text(it, style = Styles.body.copy(color = ink, fontWeight = FontWeight.Medium)) }
            }
            Text(S.shortDate(date).uppercase(), style = Styles.eyebrow.copy(color = ink))
        }
        photo?.let { image ->
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(thumbMargin)
                    .size(thumb)
                    .clip(RoundedCornerShape(14.dp))
                    .border(2.dp, ink.copy(alpha = 0.24f), RoundedCornerShape(14.dp))
                    .then(
                        if (onPhoto != null) {
                            Modifier
                                .semantics { contentDescription = S.a11yPhoto }
                                .clickable(role = Role.Button) { onPhoto(image) }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Image(image, null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
            }
        }
    }
    }
}

/** The color of the week showed up in this day (docs/pantallas.md 6): a small diamond, never a score. */
@Composable
fun WeekMark(color: String, date: LocalDate) {
    if (!ChromaRepository.settings.weekColorOn || !weekHit(color, date)) return
    val ink = inkColorFor(color)
    val label = S.weekColorRow
    Canvas(Modifier.size(10.dp).semantics { contentDescription = label }) {
        drawPath(
            Path().apply {
                moveTo(size.width / 2, 0f)
                lineTo(size.width, size.height / 2)
                lineTo(size.width / 2, size.height)
                lineTo(0f, size.height / 2)
                close()
            },
            ink,
        )
    }
}

