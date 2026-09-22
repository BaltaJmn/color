package com.baltajmn.color.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.inkColorFor
import com.baltajmn.color.data.Photos
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.ChromaEntry
import com.baltajmn.color.ui.theme.CARD_RADIUS
import com.baltajmn.color.ui.theme.Styles
import kotlinx.datetime.LocalDate

/**
 * The card (docs/pantallas.md 6): the color to the edges, its name, and the photo small in the
 * bottom right. The same component in Today, the open day, the feed and a friend's year, so a
 * color looks the same wherever it is seen.
 *
 * [photo] defaults to the local file; the feed passes a friend's downloaded one, or null when the
 * day was shared as color only or its photo has already left the server.
 */
@Composable
fun ChromaCard(
    entry: ChromaEntry,
    date: LocalDate,
    modifier: Modifier = Modifier,
    author: String? = null,
    compact: Boolean = false,
    photo: ImageBitmap? = Photos.get(entry.photo),
    onPhoto: ((ImageBitmap) -> Unit)? = null,
    onAuthor: (() -> Unit)? = null,
    marks: @Composable RowScope.() -> Unit = {},
) {
    val ink = inkColorFor(entry.color)
    val pad = if (compact) 18.dp else 24.dp
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .clip(RoundedCornerShape(CARD_RADIUS))
            .background(colorOf(entry.color)),
    ) {
        val thumb = maxWidth * 0.3f
        Column(Modifier.align(Alignment.TopStart).padding(pad).padding(end = 40.dp)) {
            Text(S.colorName(entry.name), style = (if (compact) Styles.title else Styles.display).copy(color = ink))
            Text(entry.color, style = Styles.label.copy(color = ink, fontWeight = FontWeight.Normal))
            entry.word?.let {
                Text(
                    it,
                    style = Styles.body.copy(color = ink, fontStyle = FontStyle.Italic),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
        Row(Modifier.align(Alignment.TopEnd).padding(pad), horizontalArrangement = Arrangement.spacedBy(6.dp), content = marks)
        Column(Modifier.align(Alignment.BottomStart).padding(pad).padding(end = thumb)) {
            author?.let {
                Text(
                    it,
                    style = Styles.body.copy(color = ink, fontWeight = FontWeight.Medium),
                    modifier = if (onAuthor != null) Modifier.clickable(role = Role.Button, onClick = onAuthor) else Modifier,
                )
            }
            Text(S.shortDate(date), style = Styles.label.copy(color = ink, fontWeight = FontWeight.Normal))
        }
        photo?.let { image ->
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(if (compact) 14.dp else 20.dp)
                    .size(thumb)
                    .clip(RoundedCornerShape(14.dp))
                    .border(2.dp, ink.copy(alpha = 0.24f), RoundedCornerShape(14.dp))
                    .then(
                        if (onPhoto != null) {
                            Modifier
                                .semantics { contentDescription = S.a11yPhoto }
                                .clickable(role = Role.Button) { onPhoto(image) }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Image(image, null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
            }
        }
    }
}
