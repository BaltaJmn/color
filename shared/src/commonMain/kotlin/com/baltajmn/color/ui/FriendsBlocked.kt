package com.baltajmn.color.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.baltajmn.color.i18n.S
import com.baltajmn.color.social.Friends
import com.baltajmn.color.social.Profile
import com.baltajmn.color.ui.theme.Styles
import kotlinx.coroutines.launch

/**
 * Settings, Friends, Blocked: the people this person blocked, by name, each with a way back. A
 * dialog and not a screen of its own, so the system's back closes it where it is. Unblocking asks
 * first and tells nobody; it only lets requests between the two work again.
 */
@Composable
fun BlockedDialog(onClose: () -> Unit) {
    var people by remember { mutableStateOf<List<Profile>?>(null) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }
    var asking by remember { mutableStateOf<Profile?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(attempt) {
        runCatching { Friends.blocked() }
            .onSuccess { people = it; failed = false }
            .onFailure { failed = true }
    }

    if (LocalLocked.current) return
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(S.blockedRow, style = Styles.title) },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                val list = people
                when {
                    failed -> {
                        Text(S.friendsOffline, style = Styles.body)
                        TextAction(S.retry, { attempt++ }, Modifier.padding(top = 8.dp))
                    }
                    list == null -> Text(S.working, style = Styles.caption)
                    list.isEmpty() -> Text(S.blockedEmpty, style = Styles.muted)
                    else -> list.forEach { person ->
                        // The name on a line of its own, like a request in Friends: beside the button
                        // it is left no width in a dialog this narrow with large type.
                        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(person.displayName, style = Styles.body, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            TextAction(
                                S.unblock,
                                { asking = person },
                                Modifier.align(Alignment.End).semantics { contentDescription = "${S.unblock}, ${person.displayName}" },
                                enabled = !busy,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextAction(S.a11yClose, onClose) },
        containerColor = MaterialTheme.colorScheme.surface,
    )

    asking?.let { person ->
        Ask(
            S.unblock,
            S.unblockText(person.displayName),
            S.unblock,
            onConfirm = {
                asking = null
                busy = true
                scope.launch {
                    runCatching { Friends.unblock(person.id) }
                        .onSuccess { people = people?.filter { it.id != person.id } }
                        .onFailure { failed = true }
                    busy = false
                }
            },
            onDismiss = { asking = null },
        )
    }
}
