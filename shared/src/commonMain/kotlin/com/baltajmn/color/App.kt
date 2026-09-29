package com.baltajmn.color

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.baltajmn.color.billing.Billing
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.Lock
import com.baltajmn.color.data.Reminder
import com.baltajmn.color.data.Route
import com.baltajmn.color.data.today
import com.baltajmn.color.i18n.S
import com.baltajmn.color.social.Friends
import com.baltajmn.color.social.Outbox
import com.baltajmn.color.social.Social
import com.baltajmn.color.ui.DaySheet
import com.baltajmn.color.ui.FriendActions
import com.baltajmn.color.ui.FriendDay
import com.baltajmn.color.ui.FriendList
import com.baltajmn.color.ui.FriendYear
import com.baltajmn.color.ui.FriendsScreen
import com.baltajmn.color.ui.Glyph
import com.baltajmn.color.ui.GlyphIcon
import com.baltajmn.color.ui.InviteScreen
import com.baltajmn.color.ui.LockScreen
import com.baltajmn.color.ui.Paywall
import com.baltajmn.color.ui.PhotoViewer
import com.baltajmn.color.ui.PosterScreen
import com.baltajmn.color.ui.ProDialog
import com.baltajmn.color.ui.SettingsScreen
import com.baltajmn.color.ui.ShareScreen
import com.baltajmn.color.ui.StatsScreen
import com.baltajmn.color.ui.TodayScreen
import com.baltajmn.color.ui.YearScreen
import com.baltajmn.color.ui.theme.ChromaTheme
import com.baltajmn.color.ui.theme.Styles
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.datetime.LocalDate

/** Four screens do not justify a navigation library. Friends stays hidden until v1.1. */
enum class Screen { Today, Year, Friends, Settings }

/** A minute in the background. Short enough to protect, long enough to answer the door. */
val RELOCK_AFTER = 60.seconds

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    remember {
        ChromaRepository.ensureLoaded()
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
    var stats by remember { mutableStateOf<Int?>(null) }
    var locked by remember { mutableStateOf(ChromaRepository.settings.lockOn) }
    var leftAt by remember { mutableStateOf<TimeSource.Monotonic.ValueTimeMark?>(null) }
    var proCheck by remember { mutableStateOf(0) }
    var barHeight by remember { mutableStateOf(0.dp) }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        // Coming back after 03:00 is a new day, and the widgets are told before they are looked at.
        day = today()
        ChromaRepository.syncWidgets()
        Reminder.sync(askPermission = false)
        Outbox.kick()
        // A purchase or a refund may have happened on another device.
        proCheck++
        // A minute away locks it again; stepping out to the camera or a share sheet does not.
        val away = leftAt?.elapsedNow()
        if (ChromaRepository.settings.lockOn && away != null && away >= RELOCK_AFTER) locked = true
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
            stats = null
            Friends.inviteOpen = false
            Friends.listOpen = false
            Friends.viewing = null
            Friends.viewingDay = null
            Route.pending = null
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        // The debounce may still be waiting when the app leaves the screen: write now.
        ChromaRepository.saveNow()
        leftAt = TimeSource.Monotonic.markNow()
    }
    // The task switcher takes its picture without asking, so the window is told in advance.
    LaunchedEffect(ChromaRepository.settings.lockOn) { Lock.setHidesPreview(ChromaRepository.settings.lockOn) }

    ChromaTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(Modifier.fillMaxSize()) {
                // The keyboard rises over the bottom bar as well; the screens pad for it with
                // imePadding, so what the bar already takes is consumed here, not padded twice.
                val bar = if (screen != Screen.Settings) barHeight else 0.dp
                Box(Modifier.weight(1f).fillMaxWidth().consumeWindowInsets(PaddingValues(bottom = bar))) {
                    when (screen) {
                        Screen.Today -> TodayScreen(
                            today = day,
                            onSettings = { screen = Screen.Settings },
                            onPhoto = { photo = it },
                            onShare = { sharing = it },
                        )
                        Screen.Year -> YearScreen(day, onOpenDay = { openDay = it }, onPoster = { poster = it }, onStats = { stats = it })
                        Screen.Settings -> SettingsScreen(onBack = { screen = Screen.Today })
                        Screen.Friends -> FriendsScreen(day, onPhoto = { photo = it })
                    }
                }
                if (screen != Screen.Settings) {
                    val density = LocalDensity.current
                    BottomBar(
                        screen,
                        ChromaRepository.entryOn(day)?.color,
                        Modifier.onSizeChanged { barHeight = with(density) { it.height.toDp() } },
                    ) { screen = it }
                }
            }
            openDay?.let { DaySheet(it, onClose = { openDay = null }, onPhoto = { photo = it }, onShare = { sharing = it }) }
            sharing?.let { ShareScreen(it, onClose = { sharing = null }) }
            poster?.let { PosterScreen(it, onClose = { poster = null }) }
            stats?.let { StatsScreen(it, onClose = { stats = null }) }
            if (Friends.inviteOpen) InviteScreen { Friends.inviteOpen = false }
            if (Friends.listOpen) FriendList { Friends.listOpen = false }
            Friends.viewing?.let { FriendYear(it, day, onClose = { Friends.viewing = null }) }
            Friends.viewingDay?.let { row ->
                Friends.viewing?.let { FriendDay(row, it, onClose = { Friends.viewingDay = null }, onPhoto = { photo = it }) }
            }
            Friends.acting?.let { FriendActions(it) { Friends.acting = null } }
            photo?.let { PhotoViewer(it) { photo = null } }
            if (Paywall.open) ProDialog { Paywall.open = false }
            // Last, so it covers every other layer, dialogs included.
            if (locked) LockScreen { locked = false }

            // Locked, back does nothing: the layers under the lock are not the user's to close yet.
            BackHandler(
                !locked && (
                    Paywall.open || photo != null || sharing != null || poster != null || stats != null ||
                        Friends.inviteOpen || Friends.listOpen || Friends.viewing != null || Friends.viewingDay != null ||
                        openDay != null || screen != Screen.Today
                    ),
            ) {
                when {
                    Paywall.open -> Paywall.open = false
                    photo != null -> photo = null
                    sharing != null -> sharing = null
                    poster != null -> poster = null
                    stats != null -> stats = null
                    Friends.inviteOpen -> Friends.inviteOpen = false
                    Friends.viewingDay != null -> Friends.viewingDay = null
                    Friends.viewing != null -> Friends.viewing = null
                    Friends.listOpen -> Friends.listOpen = false
                    openDay != null -> openDay = null
                    else -> screen = Screen.Today
                }
            }
        }
    }
}

/**
 * A capsule floating over the bottom edge rather than a bar across it: two or three places do not
 * need a band of chrome. The chosen one is filled with ink, like the primary button.
 */
@Composable
private fun BottomBar(current: Screen, todayColor: String?, modifier: Modifier = Modifier, onSelect: (Screen) -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Friends only exists once there is a server to be friends on.
    val tabs = listOfNotNull(
        Screen.Today to (Glyph.TODAY to S.navToday),
        Screen.Year to (Glyph.YEAR to S.navYear),
        (Screen.Friends to (Glyph.FRIENDS to S.navFriends)).takeIf { Social.available },
    )
    Box(
        modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(top = 8.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.shadow(12.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black.copy(alpha = 0.10f), spotColor = Color.Black.copy(alpha = 0.10f))
                .clip(RoundedCornerShape(32.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(32.dp))
                .selectableGroup()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabs.forEach { (s, look) ->
                val (glyph, label) = look
                val on = s == current
                val ink = if (on) colors.onPrimary else colors.onSurfaceVariant
                Row(
                    Modifier.heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(if (on) colors.primary else Color.Transparent)
                        .selectable(selected = on, role = Role.Tab, onClick = { onSelect(s) })
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // The one place the chrome takes a color: once picked, Today's dot is the day's own.
                    if (s == Screen.Today && todayColor != null) {
                        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                            Box(
                                Modifier.size(14.dp)
                                    .clip(CircleShape)
                                    .background(colorOf(todayColor))
                                    .border(1.dp, if (on) colors.onPrimary.copy(alpha = 0.6f) else colors.outline, CircleShape),
                            )
                        }
                    } else {
                        GlyphIcon(glyph, tint = ink)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(label, style = Styles.label.copy(color = ink, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium))
                }
            }
        }
    }
}

