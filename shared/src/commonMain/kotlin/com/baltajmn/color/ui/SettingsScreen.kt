package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.baltajmn.color.billing.Billing
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.weekColor
import com.baltajmn.color.data.AppInfo
import com.baltajmn.color.data.Backup
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.FilePicker
import com.baltajmn.color.data.ImportFailed
import com.baltajmn.color.data.ImportProblem
import com.baltajmn.color.data.Lock
import com.baltajmn.color.data.MergeResult
import com.baltajmn.color.data.PRIVACY_URL
import com.baltajmn.color.data.PickResult
import com.baltajmn.color.data.Reminder
import com.baltajmn.color.data.SIBLINGS
import com.baltajmn.color.data.TERMS_URL
import com.baltajmn.color.data.abandonImport
import com.baltajmn.color.data.merge
import com.baltajmn.color.data.readBackup
import com.baltajmn.color.data.startExport
import com.baltajmn.color.data.storeUrl
import com.baltajmn.color.data.today
import com.baltajmn.color.i18n.S
import com.baltajmn.color.social.Friends
import com.baltajmn.color.social.Social
import com.baltajmn.color.social.isValidName
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/** Settings, as a list of sections in the order of docs/pantallas.md 7. */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val settings = ChromaRepository.settings
    var pickTime by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var applying by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<Pair<MergeResult, Set<String>>?>(null) }
    // Title and text together: an export that fails is not an import that fails.
    var failure by remember { mutableStateOf<Pair<String?, String>?>(null) }
    var imported by remember { mutableStateOf<Int?>(null) }
    var restoring by remember { mutableStateOf(false) }
    var restored by remember { mutableStateOf<String?>(null) }
    var renaming by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }
    var erasing by remember { mutableStateOf(false) }
    var erasingNow by remember { mutableStateOf(false) }
    var choosingShare by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.BACK, S.a11yBack, onBack, Modifier.padding(end = 8.dp))
                Text(S.settingsTitle, style = Styles.title)
            }

            Section(S.sectionReminder) {
                val setReminder: (Boolean) -> Unit = { on ->
                    ChromaRepository.updateSettings { it.copy(reminderOn = on, reminderOffered = true) }
                    // Turning it on is the one moment the permission question makes sense.
                    Reminder.sync(askPermission = on)
                }
                SettingRow(
                    title = S.reminderRow,
                    subtitle = if (settings.reminderOn) S.reminderAt(settings.reminderHour, settings.reminderMinute) else S.reminderOff,
                    // Off: tapping the row turns it on, the same path as the switch. On: it opens the
                    // time picker, and only the switch itself turns it back off.
                    onClick = if (settings.reminderOn) ({ pickTime = true }) else ({ setReminder(true) }),
                ) {
                    SoftSwitch(settings.reminderOn, onChange = setReminder)
                }
            }

            val me = Social.me
            if (Social.available && me != null) {
                Section(S.sectionFriends) {
                    SettingRow(S.nameRow, me.displayName, onClick = { renaming = true })
                    SettingRow(S.defaultShareRow, shareLabel(settings.defaultShare), onClick = { choosingShare = true })
                    SettingRow(S.friendsRow, onClick = { Friends.listOpen = true })
                    SettingRow(S.inviteFriend, onClick = { Friends.inviteOpen = true })
                    SettingRow(S.signOut, onClick = { leaving = true })
                    SettingRow(
                        S.deleteAccount,
                        if (erasingNow) S.working else null,
                        enabled = !erasingNow,
                        onClick = { erasing = true },
                    )
                }
            }

            Section(S.sectionPrivacy) {
                val canLock = remember { Lock.isAvailable() }
                ToggleRow(
                    title = S.lockRow,
                    subtitle = if (canLock) S.lockSubtitle else S.lockUnavailable,
                    checked = settings.lockOn,
                    enabled = canLock,
                ) { on ->
                    // Turning it on proves who is asking: otherwise whoever has the phone in their
                    // hand could lock the owner out of their own year.
                    val store = { ChromaRepository.updateSettings { it.copy(lockOn = on) } }
                    if (on) Lock.authenticate { if (it) store() } else store()
                }
                SettingRow(S.privacyRow, onClick = { AppInfo.open(PRIVACY_URL) })
                if (Social.available) SettingRow(S.termsRow, onClick = { AppInfo.open(TERMS_URL) })
            }

            Section(S.sectionCard) {
                ToggleRow(S.watermarkRow, checked = settings.watermark) { on ->
                    ChromaRepository.updateSettings { it.copy(watermark = on) }
                }
                val week = weekColor(today())
                ToggleRow(S.weekColorRow, S.colorName(week.key), checked = settings.weekColorOn, swatch = week.hex) { on ->
                    ChromaRepository.updateSettings { it.copy(weekColorOn = on) }
                }
            }

            Section(S.sectionBackup) {
                val empty = ChromaRepository.journal.isEmpty()
                SettingRow(
                    title = S.exportRow,
                    subtitle = if (empty) {
                        S.exportNothing
                    } else {
                        settings.lastBackup?.let { S.lastBackup(LocalDate.parse(it)) } ?: S.lastBackupNever
                    },
                    enabled = !empty && FilePicker.available && !busy,
                    onClick = {
                        startExport(today()) { result -> if (result == PickResult.Failed) failure = null to S.exportFailed }
                    },
                )
                SettingRow(
                    title = S.importRow,
                    subtitle = S.importSubtitle,
                    enabled = FilePicker.available && !busy,
                    onClick = {
                        busy = true
                        var read: Result<Backup>? = null
                        FilePicker.importFile({ source -> read = runCatching { readBackup(source) } }) { result ->
                            busy = false
                            val answer = read
                            when {
                                result == PickResult.Cancelled -> abandonImport()
                                answer == null -> {
                                    abandonImport()
                                    failure = S.importFailedTitle to S.importDamaged
                                }
                                else -> answer.fold(
                                    onSuccess = { pending = merge(ChromaRepository.journal, it.journal) to it.photos },
                                    onFailure = {
                                        // The photos it had already parked go with the refusal.
                                        abandonImport()
                                        failure = S.importFailedTitle to importText(it)
                                    },
                                )
                            }
                        }
                    },
                )
            }

            Section(S.sectionPro) {
                SettingRow(
                    title = S.proRow,
                    subtitle = if (settings.pro) S.proOwned else S.proSubtitle,
                    enabled = !settings.pro,
                    onClick = { Paywall.open = true },
                )
                // Both stores ask for this to be reachable without buying anything first.
                SettingRow(
                    title = S.restoreRow,
                    enabled = !restoring,
                    onClick = {
                        restoring = true
                        scope.launch {
                            val found = Billing.restore()
                            restoring = false
                            restored = if (found) S.restoreDone else S.restoreNothing
                        }
                    },
                )
            }

            val siblings = SIBLINGS.filter { it.storeUrl != null }
            if (siblings.isNotEmpty()) {
                Section(S.sectionMoreApps) {
                    siblings.forEach { app -> SettingRow(app.name, app.tagline, onClick = { AppInfo.open(app.storeUrl!!) }) }
                }
            }

            Section(S.sectionAbout) {
                SettingRow(S.version(AppInfo.version))
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    pending?.let { (result, delivered) ->
        Ask(
            title = S.importTitle,
            text = S.importSummary(result.added, result.kept),
            // The button says so while the photos are copied and the journal rewritten.
            confirm = if (applying) S.working else S.importAction,
            onConfirm = {
                if (!applying) {
                    applying = true
                    ChromaRepository.applyImport(result, delivered) {
                        applying = false
                        imported = result.added
                        pending = null
                    }
                }
            },
            onDismiss = if (applying) {
                null
            } else {
                {
                    abandonImport()
                    pending = null
                }
            },
        )
    }

    restored?.let { Ask(null, it, S.ok, onConfirm = { restored = null }) }

    if (choosingShare) {
        ShareChoiceDialog(
            S.defaultShareRow,
            null,
            settings.defaultShare,
            onPick = { share -> ChromaRepository.updateSettings { it.copy(defaultShare = share, shareAsked = true) } },
            onDismiss = { choosingShare = false },
        )
    }

    if (renaming) RenameDialog(Social.me?.displayName.orEmpty()) { renaming = false }

    if (leaving) {
        Ask(
            S.signOut,
            S.signOutText,
            S.signOut,
            onConfirm = {
                leaving = false
                scope.launch { Social.signOut() }
            },
            onDismiss = { leaving = false },
        )
    }

    if (erasing) {
        Ask(
            S.deleteAccount,
            S.deleteAccountText,
            S.deleteAccount,
            onConfirm = {
                erasing = false
                erasingNow = true
                scope.launch {
                    runCatching { Social.deleteAccount() }.onFailure { failure = S.deleteAccount to S.friendsOffline }
                    erasingNow = false
                }
            },
            onDismiss = { erasing = false },
            destructive = true,
        )
    }

    failure?.let { (title, text) -> Ask(title = title, text = text, confirm = S.ok, onConfirm = { failure = null }) }

    imported?.let { n -> Ask(title = S.importTitle, text = S.importDone(n), confirm = S.ok, onConfirm = { imported = null }) }

    if (pickTime) {
        TimeDialog(settings.reminderHour, settings.reminderMinute, onDismiss = { pickTime = false }) { h, m ->
            ChromaRepository.updateSettings { it.copy(reminderHour = h, reminderMinute = m) }
            Reminder.sync(askPermission = false)
            pickTime = false
        }
    }
}

@Composable
private fun RenameDialog(current: String, onDone: () -> Unit) {
    var name by remember { mutableStateOf(current) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { if (!busy) onDone() },
        title = { Text(S.nameRow, style = Styles.title) },
        text = { NameField(name) { name = it } },
        confirmButton = {
            TextAction(
                if (busy) S.working else S.ok,
                onClick = {
                    busy = true
                    scope.launch {
                        runCatching { Social.rename(name) }
                        onDone()
                    }
                },
                enabled = !busy && isValidName(name),
            )
        },
        dismissButton = { TextAction(S.cancel, onClick = onDone, enabled = !busy) },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

private fun importText(e: Throwable): String = when ((e as? ImportFailed)?.problem) {
    ImportProblem.NotBackup -> S.importNotBackup
    ImportProblem.TooNew -> S.importTooNew
    ImportProblem.Empty -> S.importEmpty
    // A file that broke while being read is damaged as far as the user is concerned.
    ImportProblem.Damaged, null -> S.importDamaged
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(hour: Int, minute: Int, onDismiss: () -> Unit, onPick: (Int, Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state) },
        confirmButton = { TextAction(S.ok, onClick = { onPick(state.hour, state.minute) }) },
        dismissButton = { TextAction(S.cancel, onClick = onDismiss) },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
fun Section(label: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(28.dp))
    Text(label, style = Styles.label)
    Spacer(Modifier.height(4.dp))
    content()
}

/** A row of the list: title, subtitle and whatever sits at the end. The whole row answers. */
@Composable
fun SettingRow(
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(if (onClick != null && enabled) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .alpha(if (enabled) 1f else 0.4f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(title, style = Styles.body)
            if (subtitle != null) Text(subtitle, style = Styles.caption)
        }
        trailing?.invoke()
    }
}

@Composable
fun SoftSwitch(checked: Boolean, enabled: Boolean = true, onChange: ((Boolean) -> Unit)?) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
}

/**
 * A row whose only action is a switch: like a native settings list, the whole row toggles.
 * The switch gets `onCheckedChange = null` so there is a single control for screen readers.
 */
@Composable
fun ToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    // The hex of a color to show as a dot before the subtitle, e.g. this week's color.
    swatch: String? = null,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .alpha(if (enabled) 1f else 0.4f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(title, style = Styles.body)
            if (subtitle != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (swatch != null) {
                        // Decorative next to the name it sits beside: no semantics of its own.
                        Box(
                            Modifier.size(12.dp)
                                .clip(CircleShape)
                                .background(colorOf(swatch))
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .padding(end = 8.dp),
                        )
                    }
                    Text(subtitle, style = Styles.caption)
                }
            }
        }
        SoftSwitch(checked, enabled = enabled, onChange = null)
    }
}
