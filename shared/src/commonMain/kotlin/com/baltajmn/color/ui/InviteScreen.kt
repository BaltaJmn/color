package com.baltajmn.color.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.baltajmn.color.i18n.S
import com.baltajmn.color.share.Sharing
import com.baltajmn.color.social.Friends
import com.baltajmn.color.social.QrCode
import com.baltajmn.color.social.Social
import com.baltajmn.color.social.inviteLink
import com.baltajmn.color.social.qrEncode
import com.baltajmn.color.ui.theme.Light
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

    Overlay(S.inviteFriend, onClose) {
        Text(S.inviteText, style = Styles.muted, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        if (code.isEmpty() || qr == null) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text(S.working, style = Styles.caption) }
        } else {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { QrImage(qr) }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text(link, style = Styles.caption) }
            Spacer(Modifier.height(24.dp))
            if (failed) Notice(S.friendsOffline, S.ok to { failed = false })
            OutlinedAction(S.shareLink, { Sharing.shareText(S.inviteMessage(link)) })
            Spacer(Modifier.height(8.dp))
            TextAction(if (busy) S.working else S.regenerateLink, { confirming = true }, enabled = !busy)
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
            destructive = true,
        )
    }
}

/**
 * Always ink on white with a quiet zone of four modules, whatever the theme: not every camera
 * reads an inverted code, and one that fails in a dark room fails the whole invite.
 */
@Composable
private fun QrImage(qr: QrCode) {
    val label = S.a11yInviteQr
    Canvas(Modifier.size(240.dp).clip(RoundedCornerShape(20.dp)).background(Light.surface).semantics { contentDescription = label }) {
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
