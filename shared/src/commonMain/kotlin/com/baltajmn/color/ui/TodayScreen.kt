package com.baltajmn.color.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.baltajmn.color.color.ColorName
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.extractSwatches
import com.baltajmn.color.color.nearestName
import com.baltajmn.color.color.weekColor
import com.baltajmn.color.data.AppInfo
import com.baltajmn.color.data.Capture
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.FilePicker
import com.baltajmn.color.data.PickResult
import com.baltajmn.color.data.Picked
import com.baltajmn.color.data.Reminder
import com.baltajmn.color.data.Trip
import com.baltajmn.color.data.decodeImage
import com.baltajmn.color.data.isFromToday
import com.baltajmn.color.data.samplePixels
import com.baltajmn.color.data.startExport
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.WORD_MAX
import com.baltajmn.color.model.codePointCount
import com.baltajmn.color.model.isoKey
import com.baltajmn.color.model.limitEdit
import com.baltajmn.color.social.Social
import com.baltajmn.color.ui.theme.CARD_RADIUS
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles
import com.baltajmn.color.ui.theme.screenInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/** A photo taken and analysed, waiting for the user to pick one of its colors. */
private class Pending(val jpeg: ByteArray, val image: ImageBitmap, val swatches: List<String>)

@Composable
fun TodayScreen(
    today: LocalDate,
    onSettings: () -> Unit,
    onPhoto: (ImageBitmap) -> Unit,
    onShare: (LocalDate) -> Unit,
    below: @Composable () -> Unit = {},
) {
    val journal = ChromaRepository.journal
    val entry = journal[today.isoKey()]
    var pending by remember(today) { mutableStateOf<Pending?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var working by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var cameraDenied by remember { mutableStateOf(false) }
    // Only the card that follows a pick is revealed; coming back to Today later just shows it.
    var justPicked by remember(today) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun capture(from: suspend () -> Picked?) {
        if (working) return
        Trip.start()
        scope.launch {
            working = true
            val picked = from()
            val result = picked?.takeIf { isFromToday(it.takenOn, today) }?.let { p ->
                withContext(Dispatchers.Default) {
                    decodeImage(p.jpeg)?.let { image ->
                        Pending(p.jpeg, image, extractSwatches(samplePixels(image)).map { it.color })
                    }
                }
            }
            working = false
            when {
                picked == null -> if (Capture.launchFailed) notice = S.captureFailed
                !isFromToday(picked.takenOn, today) -> notice = S.galleryNotToday
                result == null || result.swatches.isEmpty() -> notice = S.photoUnreadable
                else -> pending = result
            }
        }
    }

    // A no given in the system prompt just now shows the same way as one given long ago.
    val camera = {
        if (Capture.cameraDenied) {
            cameraDenied = true
        } else {
            capture { Capture.camera().also { if (it == null && Capture.cameraDenied) cameraDenied = true } }
        }
    }
    val gallery = { capture { Capture.gallery() } }

    Column(
        Modifier.fillMaxSize().screenInsets().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
            Masthead(today.day, S.weekday(today), S.monthOf(today)) {
                if (entry != null && pending == null) {
                    CardMenu(
                        onRetake = { if (Capture.cameraAvailable) camera() else gallery() },
                        onShare = { onShare(today) },
                        onDelete = { confirmDelete = true },
                    )
                }
                GlyphButton(Glyph.SETTINGS, S.a11ySettings, onSettings, tint = MaterialTheme.colorScheme.onBackground)
            }

            val settings = ChromaRepository.settings
            val activeNotice = notice
            // At most one Notice on screen at a time, most urgent first.
            when {
                ChromaRepository.saveFailed -> Notice(S.noticeSaveFailed)
                ChromaRepository.corrupt -> Notice(S.noticeCorrupt, S.ok to ChromaRepository::dismissCorrupt)
                activeNotice != null -> Notice(activeNotice, S.ok to { notice = null })
                cameraDenied -> Notice(
                    S.cameraDenied,
                    S.notNow to { cameraDenied = false },
                    S.openSystemSettings to {
                        cameraDenied = false
                        AppInfo.openSettings()
                    },
                )
                entry == null || pending != null -> Unit
                // One offer at a time, and each only once: waving it away counts as an answer.
                !settings.reminderOffered -> Notice(
                    S.offerReminder(settings.reminderHour, settings.reminderMinute),
                    S.notNow to { ChromaRepository.updateSettings { it.copy(reminderOffered = true) } },
                    S.yes to {
                        ChromaRepository.updateSettings { it.copy(reminderOffered = true, reminderOn = true) }
                        Reminder.sync(askPermission = true)
                    },
                    glyph = Glyph.BELL,
                )
                ChromaRepository.needsBackupNotice(today) && FilePicker.available -> Notice(
                    S.noticeBackup,
                    S.notNow to { ChromaRepository.updateSettings { it.copy(backupNoticeDone = true) } },
                    S.makeBackup to {
                        startExport(today) { result -> if (result == PickResult.Failed) notice = S.exportFailed }
                    },
                    glyph = Glyph.EXPORT,
                )
            }
            below()

            Spacer(Modifier.height(8.dp))
            val waiting = pending
            when {
                waiting != null -> Picking(waiting, onPick = { hex ->
                    ChromaRepository.pick(hex, waiting.swatches, nearestName(hex).key, waiting.jpeg)
                    pending = null
                    justPicked = true
                }, onCancel = { pending = null })
                entry != null -> {
                    ChromaCard(entry, today, onPhoto = onPhoto, reveal = justPicked) { WeekMark(entry.color, today) }
                    Spacer(Modifier.height(20.dp))
                    SwatchRow(entry.swatches, entry.color, 40.dp) { hex ->
                        ChromaRepository.pick(hex, entry.swatches, nearestName(hex).key)
                    }
                    Spacer(Modifier.height(12.dp))
                    WordField(today, entry.word)
                    if (Social.available && Social.me != null) {
                        Spacer(Modifier.height(20.dp))
                        ShareSwitch(entry.share, entry.photo != null) { ChromaRepository.setShare(it) }
                    }
                }
                else -> Empty(
                    week = weekColor(today).takeIf { ChromaRepository.settings.weekColorOn },
                    firstTime = journal.isEmpty(),
                    working = working,
                    onCamera = camera,
                    onGallery = gallery,
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (confirmDelete) {
        Ask(
            S.deleteTitle,
            S.deleteText,
            S.delete,
            onConfirm = {
                confirmDelete = false
                ChromaRepository.delete(today)
            },
            onDismiss = { confirmDelete = false },
            destructive = true,
        )
    }
}

/**
 * The card before it has a color: a blank chip, the same size and shape the day will take, with the
 * question where the name goes and an empty code where the hex will be.
 */
@Composable
private fun Empty(week: ColorName?, firstTime: Boolean, working: Boolean, onCamera: () -> Unit, onGallery: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth()
                .aspectRatio(4f / 5f)
                .clip(RoundedCornerShape(CARD_RADIUS))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(24.dp),
        ) {
            // A suggestion to look for, not a task: no count, no streak, and it can be switched off.
            week?.let {
                Row(
                    Modifier.align(Alignment.TopStart)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(colorOf(it.hex)))
                    Spacer(Modifier.width(8.dp))
                    Text(S.weekHint(S.colorName(it.key)), style = Styles.caption.copy(color = MaterialTheme.colorScheme.onBackground))
                }
            }
            Column(Modifier.align(Alignment.BottomStart)) {
                Text(S.todayPrompt, style = Styles.display)
                Spacer(Modifier.height(4.dp))
                Text("#------", style = Styles.code)
            }
        }
        if (firstTime) {
            Spacer(Modifier.height(16.dp))
            Text(S.firstHelp, style = Styles.muted, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(20.dp))
        if (Capture.cameraAvailable) {
            PrimaryAction(if (working) S.working else S.takePhoto, onCamera, Modifier.fillMaxWidth(), enabled = !working, glyph = Glyph.CAMERA)
            Spacer(Modifier.height(4.dp))
        }
        TextAction(S.fromGallery, onGallery, enabled = !working)
    }
}

@Composable
private fun Picking(pending: Pending, onPick: (String) -> Unit, onCancel: () -> Unit) {
    Image(
        pending.image,
        null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().aspectRatio(4f / 5f).clip(RoundedCornerShape(CARD_RADIUS)),
    )
    Spacer(Modifier.height(24.dp))
    Text(S.pickColor.uppercase(), style = Styles.eyebrow, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    SwatchRow(pending.swatches, null, 56.dp, HapticFeedbackType.Confirm, onPick)
    Spacer(Modifier.height(8.dp))
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TextAction(S.cancel, onCancel, quiet = true) }
}

/** The optional word: closed until asked for, a single line, with the limit in sight. */
@Composable
private fun WordField(today: LocalDate, word: String?) {
    var open by remember(today) { mutableStateOf(word != null) }
    var value by remember(today) { mutableStateOf(TextFieldValue(word.orEmpty())) }
    // Asked for with a tap, the keyboard comes with it; an existing word just sits there.
    var asked by remember(today) { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    if (!open) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextAction(S.addWord, {
                open = true
                asked = true
            })
        }
        return
    }
    LaunchedEffect(asked) { if (asked) focus.requestFocus() }
    // The system only scrolls the cursor line above the keyboard; this lifts the whole field,
    // again on every step of the keyboard rising, so it ends fully in sight.
    val whole = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    val keyboard = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(focused, keyboard) { if (focused && keyboard > 0) whole.bringIntoView() }
    Row(
        Modifier.fillMaxWidth()
            .bringIntoViewRequester(whole)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (value.text.isEmpty()) Text(S.wordPlaceholder, style = Styles.muted)
            BasicTextField(
                value = value,
                onValueChange = { next ->
                    val edit = limitEdit(value.text, next.text, next.selection.end, WORD_MAX)
                    value = if (edit.text == next.text) next else TextFieldValue(edit.text, TextRange(edit.cursor))
                    ChromaRepository.setWord(edit.text)
                },
                singleLine = true,
                textStyle = Styles.body,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.fillMaxWidth().focusRequester(focus).onFocusChanged { focused = it.isFocused },
            )
        }
        Text(S.counter(value.text.codePointCount(), WORD_MAX), style = Styles.caption)
    }
}

@Composable
private fun CardMenu(onRetake: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        GlyphButton(Glyph.MORE, S.a11yMore, { open = true })
        DropdownMenu(open && !LocalLocked.current, onDismissRequest = { open = false }, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
            DropdownMenuItem(text = { Text(S.retakePhoto, style = Styles.body) }, onClick = { open = false; onRetake() })
            DropdownMenuItem(text = { Text(S.share, style = Styles.body) }, onClick = { open = false; onShare() })
            DropdownMenuItem(
                text = { Text(S.deleteDay, style = Styles.body, color = MaterialTheme.colorScheme.error) },
                onClick = { open = false; onDelete() },
            )
        }
    }
}
