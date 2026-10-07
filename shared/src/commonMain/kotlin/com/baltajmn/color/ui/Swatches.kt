package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.nearestName
import com.baltajmn.color.i18n.S

/**
 * The candidates, as circles. Tapping one picks it: there is no confirm button, the pick is saved
 * at once and can change all day (docs/pantallas.md 3).
 */
@Composable
fun SwatchRow(
    colors: List<String>,
    selected: String?,
    size: Dp,
    // A tick for changing it during the day; the first pick of the day passes Confirm.
    feedback: HapticFeedbackType = HapticFeedbackType.SegmentTick,
    // The candidate under the finger, null once it lifts. A press held long enough to name it is a
    // look, not a pick: letting go then picks nothing.
    onHeld: ((String?) -> Unit)? = null,
    onPick: (String) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, androidx.compose.ui.Alignment.CenterHorizontally),
    ) {
        colors.forEachIndexed { i, hex ->
            val isSelected = hex == selected
            val touch = remember { MutableInteractionSource() }
            val pressed by touch.collectIsPressedAsState()
            if (onHeld != null) LaunchedEffect(pressed) { onHeld(hex.takeIf { pressed }) }
            // Five candidates of 56 need about 400 and a phone leaves about 320: they share the
            // width and stay round, instead of the last one being squeezed into an oval.
            Box(
                Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = size + 14.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape) else Modifier)
                    .semantics {
                        contentDescription = S.a11ySwatch(nearestName(hex).key, isSelected, i + 1, colors.size)
                        this.selected = isSelected
                    }
                    .combinedClickable(
                        interactionSource = touch,
                        indication = LocalIndication.current,
                        role = Role.RadioButton,
                        onLongClick = onHeld?.let { { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } },
                    ) {
                        haptic.performHapticFeedback(feedback)
                        onPick(hex)
                    }
                    .padding(7.dp),
            ) {
                Box(
                    Modifier.size(size)
                        .clip(CircleShape)
                        .background(colorOf(hex))
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                )
            }
        }
    }
}
