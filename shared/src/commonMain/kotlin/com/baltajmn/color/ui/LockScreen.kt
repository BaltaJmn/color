package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.baltajmn.color.data.Lock
import com.baltajmn.color.i18n.S
import com.baltajmn.color.ui.theme.Styles

/**
 * Over everything else, with nothing on it: the name and a way back in. Not even today's color: a
 * locked app shows nothing of the year. The system dialog comes up on its own, and the button is
 * only there for whoever dismissed it.
 */
@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    // A field underneath keeps its focus while the app is away, and the keyboard would come back up
    // over the lock with it. Clearing the focus sends it down and keeps it down.
    val focus = LocalFocusManager.current
    LaunchedEffect(Unit) {
        focus.clearFocus(force = true)
        Lock.authenticate { if (it) onUnlocked() }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Chroma", style = Styles.display)
            Spacer(Modifier.height(32.dp))
            OutlinedAction(S.unlock, { Lock.authenticate { if (it) onUnlocked() } })
        }
    }
}
