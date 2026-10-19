package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.baltajmn.color.color.inkColorFor
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.i18n.S
import com.baltajmn.color.social.Acting
import com.baltajmn.color.social.FeedRow
import com.baltajmn.color.social.Friends
import com.baltajmn.color.social.Group
import com.baltajmn.color.social.Groups
import com.baltajmn.color.social.JoinResult
import com.baltajmn.color.social.Member
import com.baltajmn.color.social.Profile
import com.baltajmn.color.social.Social
import com.baltajmn.color.social.groupFeed
import com.baltajmn.color.social.groupLink
import com.baltajmn.color.social.inTune
import com.baltajmn.color.social.isValidName
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles
import com.baltajmn.color.ui.theme.screenInsets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * docs/pantallas.md 8.7: the capsules under the title. The friends' feed, each group by order of
 * arrival, and a new one. No count and no mark of anything new.
 */
@Composable
internal fun GroupBar() {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Capsule(S.navFriends, Groups.selected == null) { Groups.select(null) }
        Groups.groups.forEach { group -> Capsule(group.name, Groups.selected == group.id) { Groups.select(group.id) } }
        Capsule("+ ${S.groupNew}", false, tab = false) { Groups.creating = true }
    }
}

@Composable
private fun Capsule(label: String, chosen: Boolean, tab: Boolean = true, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    Box(
        Modifier.heightIn(min = 48.dp)
            .clickable(role = if (tab) Role.Tab else Role.Button, onClick = onClick)
            .semantics { if (tab) selected = chosen },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = Styles.label.copy(color = if (chosen) colors.background else colors.onBackground),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 200.dp).clip(shape)
                .then(if (chosen) Modifier.background(colors.onBackground) else Modifier.border(1.dp, colors.outline, shape))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

/** docs/pantallas.md 8.7: one group's today and yesterday, big cards one under another, and an end. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroupHome(group: Group, today: LocalDate, onPhoto: (ImageBitmap) -> Unit) {
    val scope = rememberCoroutineScope()
    var failed by remember(group.id) { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

    suspend fun load() {
        failed = runCatching { Groups.refreshFeed(today) }
            .onFailure { if (it is CancellationException) throw it }.isFailure
    }
    LaunchedEffect(group.id, attempt, today) { load() }

    val me = Social.userId().orEmpty()
    val names = Groups.members.associate { it.id to it.displayName }
    val shown = groupFeed(Groups.feed, Groups.members, me, ChromaRepository.settings.hiddenCards)
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
                FriendsHeader(group.name) {
                    TextAction(S.groupInvite, { Groups.inviteOpen = true })
                    GlyphButton(Glyph.MORE, S.a11yMore, { menu = true })
                }
                GroupBar()
            }
            if (failed) {
                friendsBlock { Notice(S.friendsOffline, S.retry to { attempt++ }) }
            } else {
                Groups.notice?.let { text -> friendsBlock { Notice(text, S.ok to { Groups.notice = null }) } }
            }
            if (todays.isNotEmpty()) {
                friendsBlock {
                    PaletteStrip(todays.asReversed().map { it.color }, S.a11yFriendsToday)
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (shown.isEmpty()) {
                friendsBlock {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(S.groupEmpty, style = Styles.muted, textAlign = TextAlign.Center)
                        OutlinedAction(S.groupInvite, { Groups.inviteOpen = true })
                    }
                }
            } else {
                groupSection(S.feedToday, todays, names, onPhoto)
                groupSection(S.feedYesterday, yesterdays, names, onPhoto)
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

    if (menu) GroupMenu(group) { menu = false }
}

private fun LazyListScope.groupSection(
    title: String,
    rows: List<FeedRow>,
    names: Map<String, String>,
    onPhoto: (ImageBitmap) -> Unit,
) {
    if (rows.isEmpty()) return
    friendsBlock { Text(title, style = Styles.label, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) }
    items(rows, key = { it.author + it.day }) { row ->
        val name = names[row.author] ?: return@items
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER, vertical = 8.dp)) {
            GroupCard(row, Profile(row.author, name), onPhoto)
        }
    }
}

/** The card at full size: one thing at a time, big, in order. Nobody here has a year to open. */
@Composable
private fun GroupCard(row: FeedRow, person: Profile, onPhoto: (ImageBitmap) -> Unit) {
    val acting = Acting(person, row)
    ChromaCard(
        row.entry(),
        row.date,
        modifier = Modifier
            .pointerInput(row.key) { detectTapGestures(onLongPress = { Friends.acting = acting }) }
            .semantics {
                onLongClick(S.a11yMore) {
                    Friends.acting = acting
                    true
                }
            },
        author = person.displayName,
        photo = friendPhoto(row),
        onPhoto = onPhoto,
    ) {
        WeekMark(row.color, row.date)
        val mine = ChromaRepository.journal[row.day]?.color
        if (mine != null && inTune(mine, row.color)) InTuneMark(inkColorFor(row.color))
        MoreButton(acting, tint = inkColorFor(row.color))
    }
}

private enum class MenuStep { Menu, Members, Leave, Busy, Failed }

/** The group's own menu: who is in it, and the way out. */
@Composable
private fun GroupMenu(group: Group, onDone: () -> Unit) {
    var step by remember { mutableStateOf(MenuStep.Menu) }
    val scope = rememberCoroutineScope()
    if (LocalLocked.current) return
    when (step) {
        MenuStep.Menu -> AlertDialog(
            onDismissRequest = onDone,
            title = { Text(group.name, style = Styles.title) },
            text = {
                Column {
                    MenuRow(S.groupMembers) { step = MenuStep.Members }
                    MenuRow(S.groupLeave) { step = MenuStep.Leave }
                }
            },
            confirmButton = { TextAction(S.cancel, onDone) },
            containerColor = MaterialTheme.colorScheme.surface,
        )
        MenuStep.Members -> MembersDialog(group, onDone)
        MenuStep.Leave -> Ask(S.groupLeave, S.groupLeaveText, S.groupLeave, {
            step = MenuStep.Busy
            scope.launch { if (runCatching { Groups.leave(group.id) }.isSuccess) onDone() else step = MenuStep.Failed }
        }, onDone, destructive = true)
        MenuStep.Busy -> Unit
        MenuStep.Failed -> Ask(null, S.friendsOffline, S.ok, onConfirm = onDone)
    }
}

/** Names by order of arrival, no number. Who manages the group can remove anyone else from it. */
@Composable
private fun MembersDialog(group: Group, onClose: () -> Unit) {
    var asking by remember { mutableStateOf<Member?>(null) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val me = Social.userId()

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(S.groupMembers, style = Styles.title) },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                if (failed) Text(S.friendsOffline, style = Styles.body, modifier = Modifier.padding(bottom = 8.dp))
                Groups.members.forEach { member ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(member.displayName, style = Styles.body, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (member.admin) Text(S.groupAdmin, style = Styles.caption)
                        if (group.admin && member.id != me) {
                            TextAction(
                                S.groupRemove,
                                { asking = member },
                                Modifier.align(Alignment.End).semantics { contentDescription = "${S.groupRemove}, ${member.displayName}" },
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

    asking?.let { member ->
        Ask(
            S.groupRemove,
            S.groupRemoveText(member.displayName),
            S.groupRemove,
            onConfirm = {
                asking = null
                busy = true
                scope.launch {
                    failed = runCatching { Groups.remove(group.id, member.id) }.isFailure
                    busy = false
                }
            },
            onDismiss = { asking = null },
            destructive = true,
        )
    }
}

/**
 * What waits on the friends' screen whatever is showing: naming a new group, and the question
 * before joining one, because joining shows your days to people who may not be your friends.
 */
@Composable
internal fun GroupDialogs() {
    val scope = rememberCoroutineScope()
    if (LocalLocked.current) return
    if (Groups.creating) {
        var name by remember { mutableStateOf("") }
        var busy by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { Groups.creating = false },
            title = { Text(S.groupNewTitle, style = Styles.title) },
            text = { NameField(name) { name = it } },
            confirmButton = {
                TextAction(
                    if (busy) S.working else S.groupCreate,
                    {
                        busy = true
                        scope.launch {
                            val result = runCatching { Groups.create(name) }
                            busy = false
                            Groups.creating = false
                            when (result.getOrNull()) {
                                true -> Groups.inviteOpen = true
                                false -> Groups.notice = S.groupLimit
                                null -> Groups.notice = S.friendsOffline
                            }
                        }
                    },
                    enabled = !busy && isValidName(name),
                )
            },
            dismissButton = { TextAction(S.cancel, { Groups.creating = false }, quiet = true) },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
    Groups.pendingCode?.let { code ->
        Ask(
            S.groupJoinTitle,
            S.groupJoinText,
            S.groupJoin,
            onConfirm = {
                Groups.pendingCode = null
                scope.launch {
                    Groups.notice = runCatching { Groups.join(code) }.fold({ joinText(it) }, { S.friendsOffline })
                }
            },
            onDismiss = { Groups.pendingCode = null },
        )
    }
}

private fun joinText(result: JoinResult): String? = when (result) {
    JoinResult.Joined -> null
    JoinResult.Already -> S.groupAlready
    JoinResult.Invalid -> S.inviteInvalid
    JoinResult.Full -> S.groupFull
    JoinResult.TooMany -> S.inviteTooMany
}

/** The invite of a group: the same layer as a friend's, with its own link. Only who manages it renews it. */
@Composable
fun GroupInviteScreen(group: Group, onClose: () -> Unit) {
    InviteLayer(
        S.groupInvite,
        S.groupInviteText,
        groupLink(group.inviteCode),
        S::groupInviteMessage,
        onRegenerate = if (group.admin) ({ Groups.regenerate(group.id) }) else null,
        onClose = onClose,
    )
}
