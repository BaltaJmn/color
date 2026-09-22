package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.i18n.S
import com.baltajmn.color.social.FeedRow
import com.baltajmn.color.social.Friends
import com.baltajmn.color.social.InviteResult
import com.baltajmn.color.social.Outbox
import com.baltajmn.color.social.Social
import com.baltajmn.color.social.isValidName
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles
import com.baltajmn.color.ui.theme.screenInsets
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithApple
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/** A made-up week of colors for the intro: a strip says "friends' colors" before any word does. */
private val EXAMPLE = listOf("#E07A5F", "#F2CC8F", "#81B29A", "#5B8DB8", "#3D5A80", "#C77DC4", "#DCC6A0")

/**
 * Friends, only ever asked for here (SPEC 4): the account is proposed the first time this tab is
 * opened, never at start. Everything below the intro needs a session and a name.
 */
@Composable
fun FriendsScreen(today: LocalDate, onPhoto: (ImageBitmap) -> Unit) {
    val status by Social.client.auth.sessionStatus.collectAsState()
    when (status) {
        is SessionStatus.NotAuthenticated -> Page { Intro() }
        // Initializing, or a refresh that failed offline: the stored session still stands.
        else -> SignedIn(today, onPhoto)
    }
}

/** The title and a scrolling column, for everything but the feed, which is a lazy list of its own. */
@Composable
private fun Page(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().screenInsets().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
            Header()
            content()
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Header(action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(S.navFriends, style = Styles.title, modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

/** One segment per color, no names: the intro's example week, and the circle's today. */
@Composable
private fun PaletteStrip(colors: List<String>) {
    Row(Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp))) {
        colors.forEach { Box(Modifier.weight(1f).height(24.dp).background(colorOf(it))) }
    }
}

@Composable
private fun Intro() {
    var failed by remember { mutableStateOf(false) }
    val auth = Social.client.composeAuth
    val onResult = { r: NativeSignInResult -> failed = r is NativeSignInResult.Error || r is NativeSignInResult.NetworkError }
    // Native where the platform has it (Apple on iOS, Google on Android), the web flow elsewhere.
    val apple = auth.rememberSignInWithApple(onResult = onResult, fallback = { Social.client.auth.signInWith(Apple) })
    val google = auth.rememberSignInWithGoogle(onResult = onResult, fallback = { Social.client.auth.signInWith(Google) })

    PaletteStrip(EXAMPLE)
    Spacer(Modifier.height(24.dp))
    listOf(S.friendsIntro1, S.friendsIntro2, S.friendsIntro3).forEach {
        Text(it, style = Styles.body)
        Spacer(Modifier.height(12.dp))
    }
    Spacer(Modifier.height(20.dp))
    if (failed) Notice(S.signInFailed, S.ok to { failed = false })
    // Apple first and as prominent as any other: App Store guideline 4.8.
    PrimaryAction(S.signInApple, { apple.startFlow() }, Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    OutlinedAction(S.signInGoogle, { google.startFlow() }, Modifier.fillMaxWidth())
}

@Composable
private fun SignedIn(today: LocalDate, onPhoto: (ImageBitmap) -> Unit) {
    var offline by remember { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }
    LaunchedEffect(attempt) {
        offline = runCatching { Social.loadMe() }.isFailure
        Outbox.kick()
    }
    when {
        offline -> Page { Notice(S.friendsOffline, S.retry to { attempt++ }) }
        Social.needsName -> Page { NameAndAge() }
        Social.me != null -> FriendsHome(today, onPhoto)
        else -> Page { Text(S.working, style = Styles.caption) }
    }
}

/** The first sign in: a name to be known by, and the age that Friends requires (SPEC 4). */
@Composable
private fun NameAndAge() {
    var name by remember { mutableStateOf("") }
    var adult by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Text(S.nameTitle, style = Styles.title)
    Spacer(Modifier.height(4.dp))
    Text(S.nameHint, style = Styles.muted)
    Spacer(Modifier.height(16.dp))
    NameField(name) { name = it }
    Spacer(Modifier.height(12.dp))
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Checkbox) { adult = !adult },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            adult,
            onCheckedChange = { adult = it },
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.onBackground),
        )
        Text(S.age16, style = Styles.body)
    }
    Spacer(Modifier.height(20.dp))
    if (failed) Notice(S.friendsOffline, S.ok to { failed = false })
    PrimaryAction(
        if (busy) S.working else S.continueAction,
        {
            busy = true
            scope.launch {
                failed = runCatching { Social.createProfile(name) }.isFailure
                busy = false
            }
        },
        Modifier.fillMaxWidth(),
        enabled = !busy && adult && isValidName(name),
    )
}

/** One line, the name, with the server's limit of 30 kept on the way in. */
@Composable
fun NameField(value: String, onChange: (String) -> Unit) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = { onChange(it.take(30)) },
            singleLine = true,
            textStyle = Styles.body,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** docs/pantallas.md 8.3: the circle's palette, requests, today, yesterday, and an end. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendsHome(today: LocalDate, onPhoto: (ImageBitmap) -> Unit) {
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }
    var askShare by remember { mutableStateOf(false) }
    val code = Friends.pendingCode

    suspend fun load() {
        failed = runCatching {
            Friends.refresh()
            // A link opened before there was an account goes out now, once.
            code?.let {
                message = inviteResultText(Friends.request(it))
                Friends.pendingCode = null
            }
            Friends.refreshFeed(today)
        }.isFailure
    }
    LaunchedEffect(code, attempt, today) { load() }
    // The default is asked when the first friend arrives, on whichever side of the request.
    LaunchedEffect(Friends.friends.isNotEmpty()) {
        if (Friends.friends.isNotEmpty() && !ChromaRepository.settings.shareAsked) askShare = true
    }
    fun act(block: suspend () -> Unit) {
        scope.launch { if (runCatching { block() }.isFailure) failed = true }
    }

    val names = Friends.friends.associate { it.id to it.displayName }
    val shown = Friends.feed.filter { it.author in names }
    val todays = shown.filter { it.date == today }
    val yesterdays = shown.filter { it.date != today }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                load()
                refreshing = false
            }
        },
        modifier = Modifier.fillMaxSize().screenInsets(),
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            block {
                Header(
                    if (names.isEmpty()) null else ({ TextAction(S.inviteFriend, { Friends.inviteOpen = true }) }),
                )
            }
            if (failed) block { Notice(S.friendsOffline, S.retry to { attempt++ }) }
            message?.let { text -> block { Notice(text, S.ok to { message = null }) } }
            // In order of the hour, earliest first, like the day itself went.
            if (todays.isNotEmpty()) {
                block {
                    PaletteStrip(todays.asReversed().map { it.color })
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (Friends.requests.isNotEmpty()) {
                block {
                    Spacer(Modifier.height(8.dp))
                    Text(S.requestsTitle, style = Styles.label)
                    Friends.requests.forEach { person ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(person.displayName, style = Styles.body, modifier = Modifier.weight(1f))
                            TextAction(S.ignore, { act { Friends.decline(person.id) } })
                            OutlinedAction(S.accept, { act { if (!Friends.accept(person.id)) message = S.friendLimit } })
                        }
                    }
                }
            }
            if (names.isEmpty()) {
                block {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(S.friendsEmpty, style = Styles.muted, textAlign = TextAlign.Center)
                        PrimaryAction(S.inviteFriend, { Friends.inviteOpen = true })
                    }
                }
            } else {
                feedSection(S.feedToday, todays, names, onPhoto)
                feedSection(S.feedYesterday, yesterdays, names, onPhoto)
                // The end is an end: nothing loads below it.
                block {
                    Text(
                        S.caughtUp,
                        style = Styles.muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    )
                }
            }
        }
    }

    if (askShare) {
        ShareChoiceDialog(
            S.askDefaultShareTitle,
            S.askDefaultShareText,
            ChromaRepository.settings.defaultShare,
            onPick = { share -> ChromaRepository.updateSettings { it.copy(defaultShare = share, shareAsked = true) } },
            onDismiss = {
                ChromaRepository.updateSettings { it.copy(shareAsked = true) }
                askShare = false
            },
        )
    }
}

private fun LazyListScope.block(content: @Composable ColumnScope.() -> Unit) = item {
    Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER), content = content)
}

private fun LazyListScope.feedSection(
    title: String,
    rows: List<FeedRow>,
    names: Map<String, String>,
    onPhoto: (ImageBitmap) -> Unit,
) {
    if (rows.isEmpty()) return
    block { Text(title, style = Styles.label, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) }
    items(rows, key = { it.author + it.day }) { row ->
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER, vertical = 8.dp)) {
            FeedCard(row, names.getValue(row.author), onPhoto)
        }
    }
}

/** A friend's day: the same card as your own, with their name and their date. No counts, no marks of being seen. */
@Composable
private fun FeedCard(row: FeedRow, author: String, onPhoto: (ImageBitmap) -> Unit) {
    val photo by produceState<ImageBitmap?>(null, row.cacheName) { value = Friends.photo(row) }
    ChromaCard(
        row.entry(),
        row.date,
        author = author,
        compact = true,
        photo = photo,
        onPhoto = onPhoto,
        onAuthor = { Friends.viewing = Friends.friends.find { it.id == row.author } },
    )
}

private fun inviteResultText(result: InviteResult): String = when (result) {
    InviteResult.Sent -> S.inviteSent
    InviteResult.Accepted -> S.inviteAccepted
    InviteResult.Already -> S.inviteAlready
    InviteResult.Self -> S.inviteSelf
    InviteResult.Invalid -> S.inviteInvalid
    InviteResult.Limit -> S.friendLimit
}
