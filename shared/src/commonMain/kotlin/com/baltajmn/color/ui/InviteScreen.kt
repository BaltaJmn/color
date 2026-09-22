package com.baltajmn.color.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.baltajmn.color.i18n.S
import com.baltajmn.color.share.Sharing
import com.baltajmn.color.social.Friends
import com.baltajmn.color.social.QrCode
import com.baltajmn.color.social.Social
import com.baltajmn.color.social.inviteLink
import com.baltajmn.color.social.qrEncode
import com.baltajmn.color.ui.theme.GUTTER
import com.baltajmn.color.ui.theme.Light
import com.baltajmn.color.ui.theme.MAX_CONTENT_WIDTH
import com.baltajmn.color.ui.theme.Styles
import kotlinx.coroutines.launch
import kotlin.math.floor

/** docs/pantallas.md 8.6: the link, its QR for showing in person, and a way to retire it. */
@Composable
fun InviteScreen(onClose: () -> Unit) {
    val code = Social.me?.inviteCode.orEmpty()
    val link = inviteLink(code)
    val qr = remember(link) { qrEncode(link) }
    var confirming by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth().padding(horizontal = GUTTER),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.CLOSE, S.a11yClose, onClose)
            }
            Text(S.inviteFriend, style = Styles.title, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(S.inviteText, style = Styles.muted, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            if (code.isEmpty() || qr == null) {
                Text(S.working, style = Styles.caption)
            } else {
                QrImage(qr)
                Spacer(Modifier.height(12.dp))
                Text(link, style = Styles.caption)
                Spacer(Modifier.height(24.dp))
                if (failed) Notice(S.friendsOffline, S.ok to { failed = false })
                OutlinedAction(S.shareLink, { Sharing.shareText(S.inviteMessage(link)) })
                Spacer(Modifier.height(8.dp))
                TextAction(if (busy) S.working else S.regenerateLink, { confirming = true }, enabled = !busy)
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (confirming) {
        Ask(
            null,
            S.regenerateText,
            S.regenerateLink,
            onConfirm = {
                confirming = false
                busy = true
                scope.launch {
                    failed = runCatching { Friends.regenerate() }.isFailure
                    busy = false
                }
            },
            onDismiss = { confirming = false },
        )
    }
}

/**
 * Always ink on white with a quiet zone of four modules, whatever the theme: not every camera
 * reads an inverted code, and one that fails in a dark room fails the whole invite.
 */
@Composable
private fun QrImage(qr: QrCode) {
    Canvas(Modifier.size(240.dp).clip(RoundedCornerShape(20.dp)).background(Light.surface)) {
        // Whole pixels per module: a fractional one leaves hairlines between dark modules.
        val cell = floor(size.width / (qr.size + 8))
        val origin = (size.width - cell * qr.size) / 2
        for (y in 0 until qr.size) {
            for (x in 0 until qr.size) {
                if (qr[x, y]) drawRect(Light.onBackground, Offset(origin + x * cell, origin + y * cell), Size(cell, cell))
            }
        }
    }
}
