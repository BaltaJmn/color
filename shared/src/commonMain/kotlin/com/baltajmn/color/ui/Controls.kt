package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baltajmn.color.i18n.S
import com.baltajmn.color.ui.theme.Styles

/** The one filled button of a screen: ink on paper, a pill 52 high (docs/pantallas.md 1). */
@Composable
fun PrimaryAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier.heightIn(min = 52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(if (enabled) colors.primary else colors.surfaceVariant)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = Styles.body.copy(
                color = if (enabled) colors.onPrimary else colors.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

/**
 * A button with a border: 48 high, a pill, 1 dp of outline. [pro] says before the tap that it opens
 * the paywall, instead of a Share that turns out to be a purchase.
 */
@Composable
fun OutlinedAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, pro: Boolean = false) {
    Box(
        modifier.heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = Styles.body.copy(fontWeight = FontWeight.Medium))
            if (pro) Text(S.proTag, style = Styles.label, modifier = Modifier.padding(start = 8.dp))
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
                fontWeight = FontWeight.Medium,
                color = when {
                    !enabled -> colors.onSurfaceVariant
                    destructive -> colors.error
                    else -> colors.onBackground
                },
            ),
        )
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
    AlertDialog(
        onDismissRequest = onDismiss ?: onConfirm,
        title = title?.let { { Text(it, style = Styles.title) } },
        text = { Text(text, style = Styles.body) },
        confirmButton = { TextAction(confirm, onClick = onConfirm, destructive = destructive) },
        dismissButton = onDismiss?.let { { TextAction(S.cancel, onClick = it) } },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

/** A quiet box at the top of a screen, with its actions under the text. */
@Composable
fun Notice(message: String, vararg actions: Pair<String, () -> Unit>) {
    Column(
        Modifier.fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Text(message, style = Styles.body)
        if (actions.isNotEmpty()) Row { actions.forEach { (label, go) -> TextAction(label, go) } }
    }
}
