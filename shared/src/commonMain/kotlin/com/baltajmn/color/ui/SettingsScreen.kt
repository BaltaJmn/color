package com.baltajmn.color.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.baltajmn.color.data.AppInfo
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.PRIVACY_URL
import com.baltajmn.color.data.SIBLINGS
import com.baltajmn.color.data.storeUrl
import com.baltajmn.color.i18n.S
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles

/**
 * Settings, as a list of sections (docs/pantallas.md 7). Each feature brings its own section
 * through [sections], so this file does not grow a dependency on every part of the app.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit, sections: @Composable () -> Unit = {}) {
    val settings = ChromaRepository.settings
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.BACK, S.a11yBack, onBack, Modifier.padding(end = 8.dp))
                Text(S.settingsTitle, style = Styles.title)
            }

            sections()

            Section(S.sectionCard) {
                SettingRow(S.watermarkRow) {
                    SoftSwitch(settings.watermark) { on -> ChromaRepository.updateSettings { it.copy(watermark = on) } }
                }
            }

            val siblings = SIBLINGS.filter { it.storeUrl != null }
            if (siblings.isNotEmpty()) {
                Section(S.sectionMoreApps) {
                    siblings.forEach { app -> SettingRow(app.name, app.tagline, onClick = { AppInfo.open(app.storeUrl!!) }) }
                }
            }

            Section(S.sectionAbout) {
                SettingRow(S.privacyRow, onClick = { AppInfo.open(PRIVACY_URL) })
                SettingRow(S.version(AppInfo.version))
            }
            Spacer(Modifier.height(32.dp))
        }
    }
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
fun SoftSwitch(checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
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
