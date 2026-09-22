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
import androidx.compose.runtime.LaunchedEffect
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
import com.baltajmn.color.billing.Billing
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.Reminder
import com.baltajmn.color.data.Route
import com.baltajmn.color.data.today
import com.baltajmn.color.social.Outbox
import com.baltajmn.color.social.Social
import kotlinx.datetime.LocalDate
import com.baltajmn.color.i18n.S
import com.baltajmn.color.ui.Glyph
import com.baltajmn.color.ui.GlyphIcon
import com.baltajmn.color.ui.DaySheet
import com.baltajmn.color.ui.FriendsScreen
import com.baltajmn.color.ui.Paywall
import com.baltajmn.color.ui.PhotoViewer
import com.baltajmn.color.ui.PosterScreen
import com.baltajmn.color.ui.ProDialog
import com.baltajmn.color.ui.SettingsScreen
import com.baltajmn.color.ui.ShareScreen
import com.baltajmn.color.ui.YearScreen
import com.baltajmn.color.ui.TodayScreen
import com.baltajmn.color.ui.theme.ChromaTheme
import com.baltajmn.color.ui.theme.Styles
import androidx.compose.ui.unit.dp

/** Four screens do not justify a navigation library. Friends stays hidden until v1.1. */
enum class Screen { Today, Year, Friends, Settings }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    remember {
        ChromaRepository.load()
        Billing.configure()
        // Whatever was saved and shared goes out after the save, never before it.
        ChromaRepository.afterSave += { Outbox.kick() }
    }
    var day by remember { mutableStateOf(today()) }
    var screen by remember { mutableStateOf(Screen.Today) }
    // The photo is an overlay over whichever screen opened it, so back closes it first.
    var photo by remember { mutableStateOf<ImageBitmap?>(null) }
    var openDay by remember { mutableStateOf<LocalDate?>(null) }
    var sharing by remember { mutableStateOf<LocalDate?>(null) }
    var poster by remember { mutableStateOf<Int?>(null) }
    var proCheck by remember { mutableStateOf(0) }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        // Coming back after 03:00 is a new day, and the widgets are told before they are looked at.
        day = today()
        ChromaRepository.syncWidgets()
        Reminder.sync(askPermission = false)
        Outbox.kick()
        // A purchase or a refund may have happened on another device.
        proCheck++
    }
    LaunchedEffect(proCheck) { Billing.refresh() }
    // A widget asked for a screen, maybe before the app existed.
    LaunchedEffect(Route.pending) {
        when (Route.pending) {
            "today" -> screen = Screen.Today
            "year" -> screen = Screen.Year
            "friends" -> if (Social.available) screen = Screen.Friends
            "pro" -> {
                screen = Screen.Year
                Paywall.open = true
            }
            else -> Unit
        }
        if (Route.pending != null) {
            openDay = null
            sharing = null
            poster = null
            Route.pending = null
        }
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
                            onShare = { sharing = it },
                        )
                        Screen.Year -> YearScreen(day, onOpenDay = { openDay = it }, onPoster = { poster = it })
                        Screen.Settings -> SettingsScreen(onBack = { screen = Screen.Today })
                        Screen.Friends -> FriendsScreen(day)
                    }
                }
                if (screen != Screen.Settings) BottomBar(screen) { screen = it }
            }
            openDay?.let { DaySheet(it, onClose = { openDay = null }, onPhoto = { photo = it }, onShare = { sharing = it }) }
            sharing?.let { ShareScreen(it, onClose = { sharing = null }) }
            poster?.let { PosterScreen(it, onClose = { poster = null }) }
            photo?.let { PhotoViewer(it) { photo = null } }
            if (Paywall.open) ProDialog { Paywall.open = false }

            BackHandler(Paywall.open || photo != null || sharing != null || poster != null || openDay != null || screen != Screen.Today) {
                when {
                    Paywall.open -> Paywall.open = false
                    photo != null -> photo = null
                    sharing != null -> sharing = null
                    poster != null -> poster = null
                    openDay != null -> openDay = null
                    else -> screen = Screen.Today
                }
            }
        }
    }
}

@Composable
private fun BottomBar(current: Screen, onSelect: (Screen) -> Unit) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(containerColor = colors.background, tonalElevation = 0.dp) {
        // Friends only exists once there is a server to be friends on.
        val tabs = listOfNotNull(
            Screen.Today to (Glyph.TODAY to S.navToday),
            Screen.Year to (Glyph.YEAR to S.navYear),
            (Screen.Friends to (Glyph.FRIENDS to S.navFriends)).takeIf { Social.available },
        )
        tabs.forEach { (s, look) ->
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

