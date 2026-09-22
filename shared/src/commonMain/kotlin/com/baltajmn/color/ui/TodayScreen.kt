package com.baltajmn.color.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.baltajmn.color.color.extractSwatches
import com.baltajmn.color.color.nearestName
import com.baltajmn.color.data.Capture
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.Picked
import com.baltajmn.color.data.Reminder
import com.baltajmn.color.data.decodeImage
import com.baltajmn.color.data.isFromToday
import com.baltajmn.color.data.samplePixels
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.WORD_MAX
import com.baltajmn.color.model.codePointCount
import com.baltajmn.color.model.isoKey
import com.baltajmn.color.model.limitEdit
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
    onShare: ((LocalDate) -> Unit)?,
    below: @Composable () -> Unit = {},
) {
    val journal = ChromaRepository.journal
    val entry = journal[today.isoKey()]
    var pending by remember(today) { mutableStateOf<Pending?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var working by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun capture(from: suspend () -> Picked?) {
        if (working) return
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
                picked == null -> Unit
                !isFromToday(picked.takenOn, today) -> notice = S.galleryNotToday
                result == null || result.swatches.isEmpty() -> notice = S.photoUnreadable
                else -> pending = result
            }
        }
    }

    val camera = { capture { Capture.camera() } }
    val gallery = { capture { Capture.gallery() } }

    Column(
        Modifier.fillMaxSize().screenInsets().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(S.longDate(today), style = Styles.title, modifier = Modifier.weight(1f))
                if (entry != null && pending == null) {
                    CardMenu(
                        onRetake = { if (Capture.cameraAvailable) camera() else gallery() },
                        onShare = onShare?.let { { it(today) } },
                        onDelete = { confirmDelete = true },
                    )
                }
                GlyphButton(Glyph.SETTINGS, S.a11ySettings, onSettings)
            }

            if (ChromaRepository.saveFailed) Notice(S.noticeSaveFailed)
            if (ChromaRepository.corrupt) Notice(S.noticeCorrupt, S.ok to ChromaRepository::dismissCorrupt)
            notice?.let { Notice(it, S.ok to { notice = null }) }
            // After the first color, and only once: waving it away counts as an answer.
            val settings = ChromaRepository.settings
            if (!settings.reminderOffered && entry != null && pending == null) {
                Notice(
                    S.offerReminder(settings.reminderHour, settings.reminderMinute),
                    S.notNow to { ChromaRepository.updateSettings { it.copy(reminderOffered = true) } },
                    S.yes to {
                        ChromaRepository.updateSettings { it.copy(reminderOffered = true, reminderOn = true) }
                        Reminder.sync(askPermission = true)
                    },
                )
            }
            below()

            Spacer(Modifier.height(8.dp))
            val waiting = pending
            when {
                waiting != null -> Picking(waiting, onPick = { hex ->
                    ChromaRepository.pick(hex, waiting.swatches, nearestName(hex).key, waiting.jpeg)
                    pending = null
                }, onCancel = if (entry != null) ({ pending = null }) else null)
                entry != null -> {
                    ChromaCard(entry, today, onPhoto = onPhoto)
                    Spacer(Modifier.height(20.dp))
                    SwatchRow(entry.swatches, entry.color, 40.dp) { hex ->
                        ChromaRepository.pick(hex, entry.swatches, nearestName(hex).key)
                    }
                    Spacer(Modifier.height(12.dp))
                    WordField(today, entry.word)
                }
                else -> Empty(
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
        )
    }
}

@Composable
private fun Empty(firstTime: Boolean, working: Boolean, onCamera: () -> Unit, onGallery: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(48.dp))
        Box(
            Modifier.size(160.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .semantics { contentDescription = S.takePhoto }
                .clickable(role = Role.Button, enabled = !working) { if (Capture.cameraAvailable) onCamera() else onGallery() },
            contentAlignment = Alignment.Center,
        ) { GlyphIcon(Glyph.CAMERA, size = 44.dp) }
        Spacer(Modifier.height(24.dp))
        Text(S.todayPrompt, style = Styles.title)
        if (firstTime) {
            Spacer(Modifier.height(8.dp))
            Text(S.firstHelp, style = Styles.muted, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(32.dp))
        if (Capture.cameraAvailable) PrimaryAction(S.takePhoto, onCamera, Modifier.fillMaxWidth(), enabled = !working)
        Spacer(Modifier.height(8.dp))
        TextAction(S.fromGallery, onGallery, enabled = !working)
        if (working) Text(S.working, style = Styles.caption)
    }
}

@Composable
private fun Picking(pending: Pending, onPick: (String) -> Unit, onCancel: (() -> Unit)?) {
    Image(
        pending.image,
        null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().aspectRatio(4f / 5f).clip(RoundedCornerShape(CARD_RADIUS)),
    )
    Spacer(Modifier.height(20.dp))
    Text(S.pickColor, style = Styles.label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    SwatchRow(pending.swatches, null, 48.dp, onPick)
    onCancel?.let {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TextAction(S.cancel, it) }
    }
}

/** The optional word: closed until asked for, a single line, with the limit in sight. */
@Composable
private fun WordField(today: LocalDate, word: String?) {
    var open by remember(today) { mutableStateOf(word != null) }
    var value by remember(today) { mutableStateOf(TextFieldValue(word.orEmpty())) }
    if (!open) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TextAction(S.addWord, { open = true }) }
        return
    }
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
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
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(S.counter(value.text.codePointCount(), WORD_MAX), style = Styles.caption)
    }
}

@Composable
private fun CardMenu(onRetake: () -> Unit, onShare: (() -> Unit)?, onDelete: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        GlyphButton(Glyph.MORE, S.a11yMore, { open = true })
        DropdownMenu(open, onDismissRequest = { open = false }, containerColor = MaterialTheme.colorScheme.surface) {
            DropdownMenuItem(text = { Text(S.retakePhoto, style = Styles.body) }, onClick = { open = false; onRetake() })
            onShare?.let { share ->
                DropdownMenuItem(text = { Text(S.share, style = Styles.body) }, onClick = { open = false; share() })
            }
            DropdownMenuItem(text = { Text(S.deleteDay, style = Styles.body) }, onClick = { open = false; onDelete() })
        }
    }
}
