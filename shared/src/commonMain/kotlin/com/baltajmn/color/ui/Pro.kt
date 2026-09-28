package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.baltajmn.color.billing.Billing
import com.baltajmn.color.billing.PurchaseOutcome
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.today
import com.baltajmn.color.i18n.S
import com.baltajmn.color.social.Social
import com.baltajmn.color.ui.theme.Styles
import com.revenuecat.purchases.kmp.models.Package
import kotlinx.coroutines.launch

/**
 * The only paywall, opened from wherever a free user hits a wall and never at startup: exporting
 * the poster, the locked year widget, the Settings row (docs/tecnico.md 6.8).
 */
object Paywall {
    var open by mutableStateOf(false)
}

@Composable
fun ProDialog(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var pack by remember { mutableStateOf<Package?>(null) }
    var busy by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        pack = Billing.proPackage()
        if (pack == null) note = S.storeUnavailable
    }

    val target = pack
    val price = target?.storeProduct?.price?.formatted
    val colors = MaterialTheme.colorScheme
    val year = today().year
    val mine = ChromaRepository.journal.filterKeys { it.startsWith("$year-") }.mapValues { it.value.color }
    Dialog(onDismissRequest = { if (!busy) onDismiss() }) {
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surface)
                .padding(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlyphTile(Glyph.SPARK)
                Spacer(Modifier.width(12.dp))
                Text(S.proTitle, style = Styles.title)
            }
            // The poster is made of the user's own days: the offer shows their year, not a stock picture.
            if (mine.size >= 2) {
                Spacer(Modifier.height(18.dp))
                YearStrip(year, mine, Modifier.height(56.dp).clip(RoundedCornerShape(14.dp)), fill = true)
            }
            Spacer(Modifier.height(18.dp))
            listOf(Glyph.POSTER to S.proPoster, Glyph.WIDGET to S.proYearWidget, Glyph.WORDS to S.proStats).forEach { (glyph, text) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    GlyphTile(glyph)
                    Spacer(Modifier.width(14.dp))
                    Text(text, style = Styles.body, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
            // Friends only exists with the server (v1.1): promising it before then sells something absent.
            Text(if (Social.available) "${S.proOnce} ${S.proFriendsFree}" else S.proOnce, style = Styles.caption)
            note?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = Styles.caption.copy(color = colors.onBackground))
            }
            Spacer(Modifier.height(20.dp))
            // With no store there is no price, and a button that cannot say what it costs is not an
            // offer: the note explains it instead.
            if (target != null && price != null) {
                PrimaryAction(
                    label = if (busy) S.working else S.buy(price),
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        busy = true
                        note = null
                        scope.launch {
                            when (Billing.purchase(target)) {
                                PurchaseOutcome.Success -> onDismiss()
                                // Changing your mind says nothing and shows nothing.
                                PurchaseOutcome.Cancelled -> busy = false
                                PurchaseOutcome.Failed -> {
                                    busy = false
                                    note = S.buyFailed
                                }
                            }
                        }
                    },
                )
                Spacer(Modifier.height(4.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextAction(
                    label = S.restore,
                    enabled = !busy,
                    quiet = true,
                    onClick = {
                        busy = true
                        scope.launch {
                            val found = Billing.restore()
                            busy = false
                            if (found) onDismiss() else note = S.restoreNothing
                        }
                    },
                )
                TextAction(S.notNow, enabled = !busy, quiet = true, onClick = { onDismiss() })
            }
        }
    }
}
