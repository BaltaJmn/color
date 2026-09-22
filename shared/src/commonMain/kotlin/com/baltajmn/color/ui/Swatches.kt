package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun SwatchRow(colors: List<String>, selected: String?, size: Dp, onPick: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, androidx.compose.ui.Alignment.CenterHorizontally),
    ) {
        colors.forEach { hex ->
            val isSelected = hex == selected
            Box(
                Modifier
                    .size(size + 14.dp)
                    .clip(CircleShape)
                    .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape) else Modifier)
                    .semantics {
                        contentDescription = S.a11ySwatch(nearestName(hex).key, isSelected)
                        this.selected = isSelected
                    }
                    .clickable(role = Role.RadioButton) { onPick(hex) }
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
