package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.i18n.S
import com.baltajmn.color.social.Acting
import com.baltajmn.color.social.FeedRow
import com.baltajmn.color.social.Friends
import com.baltajmn.color.social.Profile
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/** A full screen over the app with a close button and a title, like the day sheet. */
@Composable
private fun Overlay(
    title: String,
    onClose: () -> Unit,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.CLOSE, S.a11yClose, onClose)
                Text(title, style = Styles.title, modifier = Modifier.weight(1f).padding(start = 4.dp))
                action?.invoke()
            }
            content()
            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Names only, in alphabetical order, and no number anywhere (SPEC 5). */
@Composable
fun FriendList(onClose: () -> Unit) {
    Overlay(S.friendsRow, onClose) {
        if (Friends.friends.isEmpty()) {
            Text(S.friendsEmpty, style = Styles.muted, modifier = Modifier.padding(vertical = 16.dp))
        }
        Friends.friends.forEach { person ->
            Text(
                person.displayName,
                style = Styles.body,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .clickable(role = Role.Button) { Friends.viewing = person }
                    .padding(vertical = 15.dp),
            )
        }
    }
}

/**
 * docs/pantallas.md 8.4: the grid of My year with only what this friend shared. Days older than the
 * photo's week on the server are just their color, and that is how they look here.
 */
@Composable
fun FriendYear(person: Profile, today: LocalDate, onClose: () -> Unit) {
    var rows by remember(person.id) { mutableStateOf<List<FeedRow>?>(null) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }
    LaunchedEffect(person.id, attempt) {
        runCatching { Friends.year(person.id) }
            .onSuccess { rows = it; failed = false }
            .onFailure { failed = true }
    }
    val days = rows.orEmpty().filter { it.key !in ChromaRepository.settings.hiddenCards }.associate { it.day to it.color }
    val years = remember(days.keys) { (days.keys.map { it.take(4).toInt() } + today.year).distinct().sorted() }
    var year by remember(person.id) { mutableStateOf(today.year) }

    Overlay(person.displayName, onClose, { MoreButton(Acting(person)) }) {
        if (failed) Notice(S.friendsOffline, S.retry to { attempt++ })
        if (years.size > 1) {
            val i = years.indexOf(year)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.BACK, S.a11yPreviousYear, { year = years[i - 1] }, enabled = i > 0)
                Text(year.toString(), style = Styles.body, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                GlyphButton(Glyph.FORWARD, S.a11yNextYear, { year = years[i + 1] }, enabled = i < years.lastIndex)
            }
        }
        Spacer(Modifier.height(12.dp))
        when {
            rows == null && !failed -> Text(S.working, style = Styles.caption)
            days.keys.none { it.startsWith("$year-") } -> Text(S.friendYearEmpty, style = Styles.muted, modifier = Modifier.padding(vertical = 24.dp))
            else -> YearGrid(year, days, today) { date -> rows?.find { it.date == date }?.let { Friends.viewingDay = it } }
        }
    }
}

/** One of a friend's days, as big as your own. */
@Composable
fun FriendDay(row: FeedRow, person: Profile, onClose: () -> Unit, onPhoto: (ImageBitmap) -> Unit) {
    val photo by produceState<ImageBitmap?>(null, row.cacheName) { value = Friends.photo(row) }
    Overlay(S.longDateWithYear(row.date), onClose, { MoreButton(Acting(person, row)) }) {
        Spacer(Modifier.height(8.dp))
        ChromaCard(row.entry(), row.date, author = person.displayName, photo = photo, onPhoto = onPhoto)
    }
}

@Composable
private fun MoreButton(acting: Acting) {
    GlyphButton(Glyph.MORE, S.a11yMore, { Friends.acting = acting })
}

private enum class Step { Menu, Remove, Block, Report, Busy, Failed }

/**
 * docs/pantallas.md 8.3: what can be done about a friend or one of their days. Quiet on purpose:
 * the other person is never told, and none of it sits on the card where a reaction would.
 */
@Composable
fun FriendActions(acting: Acting, onDone: () -> Unit) {
    val (person, day) = acting
    var step by remember { mutableStateOf(Step.Menu) }
    val scope = rememberCoroutineScope()
    fun perform(block: suspend () -> Unit) {
        step = Step.Busy
        scope.launch { if (runCatching { block() }.isSuccess) onDone() else step = Step.Failed }
    }
    fun hide(key: String, on: Boolean) = ChromaRepository.updateSettings {
        it.copy(hiddenCards = if (on) it.hiddenCards + key else it.hiddenCards - key)
    }

    when (step) {
        Step.Menu -> AlertDialog(
            onDismissRequest = onDone,
            title = { Text(person.displayName, style = Styles.title) },
            text = {
                Column {
                    if (day != null) MenuRow(S.report) { step = Step.Report }
                    MenuRow(S.block) { step = Step.Block }
                    MenuRow(S.removeFriend) { step = Step.Remove }
                }
            },
            confirmButton = { TextAction(S.cancel, onDone) },
            containerColor = MaterialTheme.colorScheme.surface,
        )
        Step.Remove -> Ask(S.removeFriend, S.removeFriendText(person.displayName), S.removeFriend, { perform { Friends.remove(person.id) } }, onDone)
        Step.Block -> Ask(S.block, S.blockText(person.displayName), S.block, { perform { Friends.block(person.id) } }, onDone)
        Step.Report -> Ask(S.report, S.reportText, S.report, {
            val row = day ?: return@Ask onDone()
            // Hidden at once, as Apple asks; shown again only if the report never left.
            hide(row.key, true)
            if (Friends.viewingDay == row) Friends.viewingDay = null
            perform {
                runCatching { Friends.report(row) }.onFailure {
                    hide(row.key, false)
                    throw it
                }
            }
        }, onDone)
        Step.Busy -> Unit
        Step.Failed -> Ask(null, S.friendsOffline, S.ok, onConfirm = onDone)
    }
}

@Composable
private fun MenuRow(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = Styles.body,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onClick).padding(vertical = 13.dp),
    )
}
