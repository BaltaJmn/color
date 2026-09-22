package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.isoKey
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles
import kotlinx.datetime.LocalDate

/** One day, opened from the year. A past day is looked at, never edited; it can only be deleted. */
@Composable
fun DaySheet(
    date: LocalDate,
    onClose: () -> Unit,
    onPhoto: (ImageBitmap) -> Unit,
    onShare: ((LocalDate) -> Unit)?,
) {
    val entry = ChromaRepository.journal[date.isoKey()]
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.CLOSE, S.a11yClose, onClose)
                Text(S.longDateWithYear(date), style = Styles.title, modifier = Modifier.weight(1f).padding(start = 4.dp))
                if (entry != null) {
                    onShare?.let { GlyphButton(Glyph.SHARE, S.a11yShare, { it(date) }) }
                    GlyphButton(Glyph.TRASH, S.deleteDay, { confirmDelete = true })
                }
            }
            Spacer(Modifier.height(8.dp))
            entry?.let { ChromaCard(it, date, onPhoto = onPhoto) }
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
                ChromaRepository.delete(date)
                onClose()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}
