package com.baltajmn.color.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.Share
import com.baltajmn.color.ui.theme.Styles

fun shareLabel(share: Share): String = when (share) {
    Share.Private -> S.sharePrivate
    Share.Color -> S.shareColorOnly
    Share.Photo -> S.shareWithPhoto
}

/**
 * Who sees a day, three ways and none of them marked as the lesser one (SPEC 4): a day shared as a
 * color only looks like any other day in a friend's feed, just without the corner photo.
 */
@Composable
fun ShareSwitch(current: Share, hasPhoto: Boolean, onChange: (Share) -> Unit) {
    val options = listOfNotNull(Share.Private, Share.Color, Share.Photo.takeIf { hasPhoto })
    Column(Modifier.fillMaxWidth()) {
        Text(S.shareLabel, style = Styles.label)
        Spacer(Modifier.height(8.dp))
        Segmented(options.map(::shareLabel), options.indexOf(current).coerceAtLeast(0)) { onChange(options[it]) }
    }
}

/** The default for new days: asked once when the first friend arrives, and kept in Settings. */
@Composable
fun ShareChoiceDialog(title: String, text: String?, current: Share, onPick: (Share) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = Styles.title) },
        text = {
            Column {
                text?.let {
                    Text(it, style = Styles.body)
                    Spacer(Modifier.height(16.dp))
                }
                Segmented(Share.entries.map(::shareLabel), current.ordinal) { onPick(Share.entries[it]) }
            }
        },
        confirmButton = { TextAction(S.ok, onClick = onDismiss) },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
