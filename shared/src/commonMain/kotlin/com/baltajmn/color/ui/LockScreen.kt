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
import com.baltajmn.color.data.ChromaRepository
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
    // Asked again on every try: the screen lock of the phone can go while this layer is up. Without
    // one nothing could ever answer the prompt, and whoever holds a phone with no lock can already
    // open everything on it, so the lock of Chroma goes too.
    val attempt = {
        if (Lock.isAvailable()) {
            Lock.authenticate { if (it) onUnlocked() }
        } else {
            ChromaRepository.updateSettings { it.copy(lockOn = false) }
            onUnlocked()
        }
    }
    LaunchedEffect(Unit) {
        focus.clearFocus(force = true)
        attempt()
    }

    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).blockTouches(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GlyphTile(Glyph.LOCK)
            Spacer(Modifier.height(16.dp))
            Text("Chroma", style = Styles.display)
            Spacer(Modifier.height(32.dp))
            PrimaryAction(S.unlock, attempt)
        }
    }
}
