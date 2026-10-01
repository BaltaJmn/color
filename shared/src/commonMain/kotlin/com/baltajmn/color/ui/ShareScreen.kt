package com.baltajmn.color.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.Photos
import com.baltajmn.color.data.Trip
import com.baltajmn.color.i18n.S
import com.baltajmn.color.share.Sharing
import com.baltajmn.color.share.encodeToPng
import com.baltajmn.color.share.renderDayCard
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles
import kotlinx.datetime.LocalDate

/** Density 1, not the screen's: pictures are laid out in pixels, and the screen's density would triple every text. */
@Composable
fun rememberPixelMeasurer(): TextMeasurer {
    val fonts = LocalFontFamilyResolver.current
    return remember(fonts) { TextMeasurer(fonts, Density(1f), LayoutDirection.Ltr) }
}

/**
 * The day card at the size it will be posted, and the ways out of the app with it. Free forever:
 * the card is what makes the app known. The photo can stay home: color only is one tap away.
 */
@Composable
fun ShareScreen(date: LocalDate, onClose: () -> Unit) {
    val entry = ChromaRepository.entryOn(date) ?: return
    val watermark = ChromaRepository.settings.watermark
    val photo = Photos.get(entry.photo)
    var withPhoto by remember(date) { mutableStateOf(photo != null) }
    val measurer = rememberPixelMeasurer()
    val card = remember(entry, withPhoto, watermark) {
        renderDayCard(entry, date, photo.takeIf { withPhoto }, watermark, measurer)
    }

    val description = S.colorName(entry.name) + ", " + entry.color + ", " + S.shortDate(date)
    PictureScreen(S.share, card, description, onClose) {
        if (photo != null) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface)) {
                ToggleRow(S.includePhoto, checked = withPhoto, glyph = Glyph.PHOTO) { withPhoto = it }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * A finished picture over the whole screen, with share and save under it. The poster uses it too:
 * [locked] still shows the picture and sends both buttons to [onLocked]. [description] speaks the
 * picture for a screen reader, since it is the whole content of the screen.
 */
@Composable
fun PictureScreen(
    title: String,
    picture: ImageBitmap,
    description: String,
    onClose: () -> Unit,
    locked: Boolean = false,
    onLocked: () -> Unit = {},
    options: @Composable () -> Unit,
) {
    var saved by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.CLOSE, S.a11yClose, onClose, Modifier.offset(x = (-12).dp), tint = MaterialTheme.colorScheme.onBackground)
                Text(title, style = Styles.title, modifier = Modifier.semantics { heading() })
            }
            // The picture on a stage of its own, the way a print is looked at before it leaves the shop.
            Box(
                Modifier.fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                val shape = RoundedCornerShape(12.dp)
                Image(
                    bitmap = picture,
                    contentDescription = description,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.widthIn(max = 320.dp).heightIn(max = 480.dp)
                        .shadow(16.dp, shape, ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.12f))
                        .clip(shape),
                )
            }
            options()
            val share = {
                if (locked) {
                    onLocked()
                } else {
                    Trip.start()
                    Sharing.sharePng(picture.encodeToPng())
                }
            }
            val save = {
                if (locked) {
                    onLocked()
                } else {
                    Sharing.savePngToPhotos(picture.encodeToPng()) { ok -> saved = if (ok) S.saved else S.saveFailed }
                }
            }
            ActionRow(Modifier.fillMaxWidth()) {
                if (locked) {
                    OutlinedAction(S.share, share, pro = true, glyph = Glyph.SHARE)
                } else {
                    PrimaryAction(S.share, share, glyph = Glyph.SHARE)
                }
                if (Sharing.canSaveToPhotos) OutlinedAction(S.saveToPhotos, save, pro = locked)
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    saved?.let { Ask(null, it, S.ok, onConfirm = { saved = null }) }
}
