package com.baltajmn.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.today
import com.baltajmn.color.i18n.S
import com.baltajmn.color.ui.Glyph
import com.baltajmn.color.ui.GlyphIcon
import com.baltajmn.color.ui.PhotoViewer
import com.baltajmn.color.ui.TodayScreen
import com.baltajmn.color.ui.theme.ChromaTheme
import com.baltajmn.color.ui.theme.Styles
import androidx.compose.ui.unit.dp

/** Four screens do not justify a navigation library. Friends stays hidden until v1.1. */
enum class Screen { Today, Year, Friends, Settings }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    remember { ChromaRepository.load() }
    var day by remember { mutableStateOf(today()) }
    var screen by remember { mutableStateOf(Screen.Today) }
    // The photo is an overlay over whichever screen opened it, so back closes it first.
    var photo by remember { mutableStateOf<ImageBitmap?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        // Coming back after 03:00 is a new day, and the widgets are told before they are looked at.
        day = today()
        ChromaRepository.syncWidgets()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        // The debounce may still be waiting when the app leaves the screen: write now.
        ChromaRepository.saveNow()
    }

    ChromaTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (screen) {
                        Screen.Today -> TodayScreen(
                            today = day,
                            onSettings = { screen = Screen.Settings },
                            onPhoto = { photo = it },
                            onShare = null,
                        )
                        // Filled in by #13, #16 and v1.1.
                        Screen.Year, Screen.Friends, Screen.Settings -> Unit
                    }
                }
                if (screen != Screen.Settings) BottomBar(screen) { screen = it }
            }
            photo?.let { PhotoViewer(it) { photo = null } }

            BackHandler(photo != null || screen != Screen.Today) {
                if (photo != null) photo = null else screen = Screen.Today
            }
        }
    }
}

@Composable
private fun BottomBar(current: Screen, onSelect: (Screen) -> Unit) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(containerColor = colors.background, tonalElevation = 0.dp) {
        listOf(Screen.Today to (Glyph.TODAY to S.navToday), Screen.Year to (Glyph.YEAR to S.navYear)).forEach { (s, look) ->
            val (glyph, label) = look
            val tint = if (s == current) colors.onBackground else colors.onSurfaceVariant
            NavigationBarItem(
                selected = s == current,
                onClick = { onSelect(s) },
                icon = { GlyphIcon(glyph, tint = tint) },
                label = { Text(label, style = Styles.caption.copy(color = tint)) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = colors.surfaceVariant),
            )
        }
    }
}

