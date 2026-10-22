package com.baltajmn.color.ui

import androidx.compose.foundation.Canvas
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.layout.Layout
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.inkColorFor
import com.baltajmn.color.data.AppInfo
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.TERMS_URL
import com.baltajmn.color.i18n.S
import com.baltajmn.color.social.Acting
import com.baltajmn.color.social.FeedRow
import com.baltajmn.color.social.Friends
import com.baltajmn.color.social.Groups
import com.baltajmn.color.social.InviteResult
import com.baltajmn.color.social.Outbox
import com.baltajmn.color.social.Social
import com.baltajmn.color.social.appleName
import com.baltajmn.color.social.inTune
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles
import com.baltajmn.color.ui.theme.screenInsets
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithApple
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.coroutines.CancellationException
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
        // The stored session is being read: there is no one to ask for yet, and the screen follows
        // the status, so it is on the next frame once there is.
        is SessionStatus.Initializing -> Page { Text(S.working, style = Styles.caption) }
        // The session stands but cannot be renewed without a network, and the client keeps trying by
        // itself: asked now, everything would fail. This screen comes back with the connection.
        is SessionStatus.RefreshFailure -> Page { Notice(S.friendsOffline) }
        is SessionStatus.Authenticated -> SignedIn(today, onPhoto)
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
private fun Header(action: (@Composable () -> Unit)? = null) = FriendsHeader(S.navFriends, action)

/**
 * The title of the tab, or of the group on screen, with what can be done there beside it. When both
 * do not fit on one line (large text, a long language) the action goes under it, at the end: the
 * title is never squeezed into broken words.
 */
@Composable
internal fun FriendsHeader(title: String, action: (@Composable () -> Unit)? = null) {
    Layout(
        content = {
            Text(title, style = Styles.title, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.semantics { heading() })
            action?.invoke()
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val act = measurables.getOrNull(1)?.measure(loose)
        val actWidth = act?.width ?: 0
        val inline = act == null || measurables[0].maxIntrinsicWidth(Int.MAX_VALUE) + actWidth <= width
        val text = measurables[0].measure(loose.copy(maxWidth = if (inline) width - actWidth else width))
        if (inline) {
            val height = maxOf(constraints.minHeight, text.height, act?.height ?: 0)
            layout(width, height) {
                text.placeRelative(0, (height - text.height) / 2)
                act?.placeRelative(width - actWidth, (height - act.height) / 2)
            }
        } else {
            val band = maxOf(constraints.minHeight, text.height)
            layout(width, band + act!!.height) {
                text.placeRelative(0, (band - text.height) / 2)
                act.placeRelative(width - actWidth, band)
            }
        }
    }
}

/**
 * One segment per color, no names: the intro's example week, and the circle's today. [description]
 * announces the strip as one thing when it carries meaning; null keeps a purely decorative one
 * (like the intro's made-up example) silent for a screen reader.
 */
@Composable
internal fun PaletteStrip(colors: List<String>, description: String? = null) {
    val base = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp))
    Row(if (description != null) base.semantics { contentDescription = description } else base.clearAndSetSemantics {}) {
        colors.forEach { Box(Modifier.weight(1f).height(24.dp).background(colorOf(it))) }
    }
}

@Composable
private fun Intro() {
    var failed by remember { mutableStateOf(false) }
    var emailOpen by remember { mutableStateOf(false) }
    var adult by remember { mutableStateOf(false) }
    val auth = Social.client.composeAuth
    val onResult = { r: NativeSignInResult ->
        r.appleName()?.let { Social.nameHint = it }
        failed = r is NativeSignInResult.Error || r is NativeSignInResult.NetworkError
    }
    // Native where the platform has it (Apple on iOS, Google on Android), the web flow elsewhere.
    val apple = auth.rememberSignInWithApple(onResult = onResult, fallback = { Social.client.auth.signInWith(Apple) })
    val google = auth.rememberSignInWithGoogle(onResult = onResult, fallback = { Social.client.auth.signInWith(Google) })

    PaletteStrip(EXAMPLE)
    Spacer(Modifier.height(24.dp))
    // One idea and its small print, not three lines of equal weight.
    Text(S.friendsIntro1, style = Styles.title)
    Spacer(Modifier.height(8.dp))
    Text("${S.friendsIntro2} ${S.friendsIntro3}", style = Styles.muted)
    if (failed) {
        Spacer(Modifier.height(16.dp))
        Notice(S.signInFailed, S.ok to { failed = false })
    }
    // The age and the terms are said before signing in, not after: nothing is asked of a person
    // once Apple or Google has said who they are (App Store 4). Apple 1.2: the terms are accepted
    // before anything shared by others is seen.
    Section(null) {
        Row(
            Modifier.fillMaxWidth()
                .heightIn(min = 60.dp)
                .toggleable(adult, role = Role.Checkbox) { adult = it }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // In the place of a glyph tile, so the words start where every row's do.
            Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                Checkbox(
                    adult,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.onBackground),
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(S.age16, style = Styles.body, modifier = Modifier.weight(1f).padding(vertical = 10.dp))
        }
        SettingRow(S.termsRow, S.termsAgree, onClick = { AppInfo.open(TERMS_URL) }, glyph = Glyph.DOC)
    }
    Spacer(Modifier.height(16.dp))
    // Apple first, and identical to Google: App Store guideline 4.8, equally prominent.
    OutlinedAction(S.signInApple, { apple.startFlow() }, Modifier.fillMaxWidth(), enabled = adult)
    Spacer(Modifier.height(8.dp))
    OutlinedAction(S.signInGoogle, { google.startFlow() }, Modifier.fillMaxWidth(), enabled = adult)
    // Two buttons off without a word look broken: the reason sits under them until the box is ticked.
    if (!adult) {
        Text(
            S.signInNeedsAge,
            style = Styles.caption,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
    Spacer(Modifier.height(8.dp))
    // Only for the App Review account (docs/tecnico.md 9.1): the server refuses every other email
    // sign-up. A visible link, not a gesture, so the reviewer finds it (Apple 2.3.1).
    if (emailOpen) EmailSignIn { emailOpen = false }
    else Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextAction(S.signInEmail, { emailOpen = true }, quiet = true)
    }
}

/** Email and password, sign in only: there is no sign-up here and the server would refuse it. */
@Composable
private fun EmailSignIn(onClose: () -> Unit) {
    var mail by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    FormField(mail, { mail = it }, S.emailField, KeyboardType.Email)
    Spacer(Modifier.height(8.dp))
    FormField(secret, { secret = it }, S.passwordField, KeyboardType.Password, hidden = true)
    Spacer(Modifier.height(12.dp))
    if (failed) Text(S.signInEmailFailed, style = Styles.caption, modifier = Modifier.padding(bottom = 12.dp))
    PrimaryAction(
        if (busy) S.working else S.signInAction,
        {
            busy = true
            failed = false
            scope.launch {
                // A success changes the session status and the screen leaves this form by itself.
                failed = runCatching {
                    Social.client.auth.signInWith(Email) {
                        email = mail.trim()
                        password = secret
                    }
                }.isFailure
                busy = false
            }
        },
        Modifier.fillMaxWidth(),
        enabled = !busy && mail.isNotBlank() && secret.isNotEmpty(),
    )
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextAction(S.cancel, onClose, quiet = true)
    }
}

/** One line on a surface, named by its placeholder for a screen reader while it is empty. */
@Composable
private fun FormField(value: String, onChange: (String) -> Unit, placeholder: String, type: KeyboardType, hidden: Boolean = false) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = Styles.body,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false, keyboardType = type),
        visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fieldSurface(placeholder),
        decorationBox = { field ->
            Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                if (value.isEmpty()) Text(placeholder, style = Styles.muted)
                field()
            }
        },
    )
}

/**
 * The surface is the field, padding included: a tap anywhere on it starts typing, and it keeps its
 * name for a screen reader once there is text in it.
 */
@Composable
private fun Modifier.fieldSurface(label: String): Modifier = fillMaxWidth()
    .clip(RoundedCornerShape(20.dp))
    .background(MaterialTheme.colorScheme.surfaceVariant)
    .semantics { contentDescription = label }

@Composable
private fun SignedIn(today: LocalDate, onPhoto: (ImageBitmap) -> Unit) {
    var offline by remember { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }
    LaunchedEffect(attempt) {
        // A first sign in has no profile: it is made here from what the provider gave, not asked for.
        offline = runCatching { Social.loadMe(); Social.ensureProfile() }.isFailure
        Outbox.kick()
    }
    when {
        offline -> Page { Notice(S.friendsOffline, S.retry to { attempt++ }) }
        Social.me != null -> SocialHome(today, onPhoto)
        else -> Page { Text(S.working, style = Styles.caption) }
    }
}

/**
 * One line, the name, with the server's limit of 30 kept on the way in, and a counter under it.
 * [label] is the dialog's title, said by a screen reader on the field.
 */
@Composable
fun NameField(value: String, label: String, onChange: (String) -> Unit) {
    // A Column of its own: the dialog's text slot is a Box and would stack the counter on the field.
    Column(Modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = { onChange(it.take(30)) },
            singleLine = true,
            textStyle = Styles.body,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fieldSurface(label),
            decorationBox = { field -> Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) { field() } },
        )
        Text(
            S.counter(value.length, 30),
            style = Styles.caption,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            textAlign = TextAlign.End,
        )
    }
}

/**
 * The friends' feed or a group's, and what waits over either: the question of the default share
 * when the first friend or group arrives, naming a group, and joining one from a link.
 */
@Composable
private fun SocialHome(today: LocalDate, onPhoto: (ImageBitmap) -> Unit) {
    var askShare by remember { mutableStateOf(false) }
    // Not part of the feeds' loads: a group list that fails must not call the friends' feed offline.
    LaunchedEffect(Unit) { runCatching { Groups.refresh() } }
    // The default is asked when the first friend or group arrives, on whichever side of the link.
    val someone = Friends.friends.isNotEmpty() || Groups.groups.isNotEmpty()
    LaunchedEffect(someone) {
        if (someone && !ChromaRepository.settings.shareAsked) askShare = true
    }
    when (val group = Groups.current) {
        null -> FriendsHome(today, onPhoto)
        else -> GroupHome(group, today, onPhoto)
    }
    GroupDialogs()
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

/** docs/pantallas.md 8.3: the circle's palette, requests, today, yesterday, and an end. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendsHome(today: LocalDate, onPhoto: (ImageBitmap) -> Unit) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var message by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }
    val code = Friends.pendingCode

    suspend fun load() {
        failed = runCatching {
            Friends.refresh()
            // A link opened before there was an account goes out now, once.
            code?.let { message = inviteResultText(Friends.request(it)) }
            Friends.refreshFeed(today)
            runCatching { Groups.refresh() }
            // The request spends the code this effect is keyed on, which restarts it: a cancelled run
            // is not a failed one, or "no connection" would flash before the restart.
        }.onFailure { if (it is CancellationException) throw it }.isFailure
    }
    LaunchedEffect(code, attempt, today) { load() }
    fun act(block: suspend () -> Unit) {
        scope.launch { if (runCatching { block() }.isFailure) failed = true }
    }

    val names = Friends.friends.associate { it.id to it.displayName }
    val hidden = ChromaRepository.settings.hiddenCards
    val shown = Friends.feed.filter { it.author in names && it.key !in hidden }
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
            friendsBlock {
                Header(
                    if (names.isEmpty()) null else ({ TextAction(S.inviteFriend, { Friends.inviteOpen = true }) }),
                )
                GroupBar()
            }
            // One notice at a time (docs/pantallas.md 1): a failed refresh explains why nothing
            // else updates, so it outranks the message.
            if (failed) {
                friendsBlock { Notice(S.friendsOffline, S.retry to { attempt++ }) }
            } else {
                message?.let { text -> friendsBlock { Notice(text, S.ok to { message = null }) } }
                    ?: Groups.notice?.let { text -> friendsBlock { Notice(text, S.ok to { Groups.notice = null }) } }
            }
            // In order of the hour, earliest first, like the day itself went.
            if (todays.isNotEmpty()) {
                friendsBlock {
                    PaletteStrip(todays.asReversed().map { it.color }, S.a11yFriendsToday)
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (Friends.requests.isNotEmpty()) {
                friendsBlock {
                    // A card of their own: a question waiting for an answer, apart from the days below.
                    Section(S.requestsTitle, lineStart = 16.dp) {
                        Friends.requests.forEach { person ->
                            RequestRow(
                                person.displayName,
                                onBlock = { Friends.acting = Acting(person, request = true) },
                                onIgnore = { act { Friends.decline(person.id) } },
                                onAccept = {
                                    act {
                                        // Once per friendship: the moment a request turns into a friend.
                                        if (Friends.accept(person.id)) {
                                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                            // Their days were not readable before: without a reload the new friend shows no card.
                                            attempt++
                                        } else {
                                            message = S.friendLimit
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
            if (names.isEmpty()) {
                friendsBlock {
                    // Lower than the top, toward the optical middle, and with an empty strip where the
                    // circle's colors will be: what this place is for, before there is anything in it.
                    Column(
                        Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp)).clearAndSetSemantics {},
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            repeat(7) { Box(Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant)) }
                        }
                        Text(S.friendsEmpty, style = Styles.muted, textAlign = TextAlign.Center)
                        PrimaryAction(S.inviteFriend, { Friends.inviteOpen = true })
                        if (Groups.groups.isEmpty()) Text(S.friendsEmptyGroups, style = Styles.caption, textAlign = TextAlign.Center)
                    }
                }
            } else {
                feedSection(S.feedToday, todays, names, onPhoto)
                feedSection(S.feedYesterday, yesterdays, names, onPhoto)
                // The end is an end: nothing loads below it.
                friendsBlock {
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
}

/**
 * The name on a line of its own and the answers under it, wrapping: in a long language with big
 * type three buttons leave a name beside them no width at all, and who is asking is the point.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RequestRow(name: String, onBlock: () -> Unit, onIgnore: () -> Unit, onAccept: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 4.dp)) {
        Text(name, style = Styles.body, maxLines = 2, overflow = TextOverflow.Ellipsis)
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalArrangement = Arrangement.Center,
        ) {
            // In red: Ignore leaves the link valid and invites a repeat, Block is for good.
            // The same three words for every person: each says whose request it answers.
            TextAction(S.block, onBlock, Modifier.semantics { contentDescription = "${S.block}, $name" }, destructive = true)
            TextAction(S.ignore, onIgnore, Modifier.semantics { contentDescription = "${S.ignore}, $name" })
            OutlinedAction(S.accept, onAccept, Modifier.semantics { contentDescription = "${S.accept}, $name" })
        }
    }
}

internal fun LazyListScope.friendsBlock(content: @Composable ColumnScope.() -> Unit) = item {
    Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER), content = content)
}

private fun LazyListScope.feedSection(
    title: String,
    rows: List<FeedRow>,
    names: Map<String, String>,
    onPhoto: (ImageBitmap) -> Unit,
) {
    if (rows.isEmpty()) return
    friendsBlock { SectionLabel(title, Modifier.padding(top = 20.dp)) }
    items(rows, key = { it.author + it.day }) { row ->
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER, vertical = 8.dp)) {
            FeedCard(row, names.getValue(row.author), onPhoto)
        }
    }
}

/** A friend's day: the same card as your own, with their name and their date. No counts, no marks of being seen. */
@Composable
private fun FeedCard(row: FeedRow, author: String, onPhoto: (ImageBitmap) -> Unit) {
    val photo = friendPhoto(row)
    val person = Friends.friends.find { it.id == row.author }
    fun menu() {
        person?.let { Friends.acting = Acting(it, row) }
    }
    ChromaCard(
        row.entry(),
        row.date,
        // Report, block and remove: a long press, and the MORE button below for the same menu.
        modifier = Modifier
            .pointerInput(row.key) { detectTapGestures(onLongPress = { menu() }) }
            .semantics {
                onLongClick(S.a11yMore) {
                    menu()
                    true
                }
            },
        author = author,
        compact = true,
        photo = photo,
        onPhoto = onPhoto,
        onAuthor = { Friends.viewing = person },
    ) {
        // Only on the same day, and only for the two of them: everyone else's color is different.
        WeekMark(row.color, row.date)
        val mine = ChromaRepository.journal[row.day]?.color
        if (mine != null && inTune(mine, row.color)) InTuneMark(inkColorFor(row.color))
        // Three dots, the platforms' own sign for more options: visible, and nothing like a reaction.
        if (person != null) MoreButton(Acting(person, row), tint = inkColorFor(row.color))
    }
}

private fun inviteResultText(result: InviteResult): String = when (result) {
    InviteResult.Sent -> S.inviteSent
    InviteResult.Accepted -> S.inviteAccepted
    InviteResult.Already -> S.inviteAlready
    InviteResult.Self -> S.inviteSelf
    InviteResult.Invalid -> S.inviteInvalid
    InviteResult.Limit -> S.inviteLimit
    InviteResult.TooMany -> S.inviteTooMany
}

/** Two overlapping rings, 10 across: nothing to press, nothing to count (docs/pantallas.md 6). */
@Composable
internal fun InTuneMark(ink: Color) {
    val label = S.inTune
    Canvas(Modifier.size(width = 16.dp, height = 10.dp).semantics { contentDescription = label }) {
        val r = size.height / 2
        val stroke = Stroke(1.5.dp.toPx())
        drawCircle(ink, r - stroke.width / 2, Offset(r, r), style = stroke)
        drawCircle(ink, r - stroke.width / 2, Offset(size.width - r, r), style = stroke)
    }
}

