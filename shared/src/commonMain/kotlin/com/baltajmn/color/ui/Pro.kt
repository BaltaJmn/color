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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.baltajmn.color.billing.RestoreOutcome
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

    /** What the user was reaching for when the paywall opened: done as soon as Pro arrives. */
    var onPro: (() -> Unit)? = null
}

@Composable
fun ProDialog(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var pack by remember { mutableStateOf<Package?>(null) }
    var busy by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    fun unlocked() {
        Paywall.onPro?.let {
            Paywall.onPro = null
            it()
        }
        onDismiss()
    }
    // Pro arriving while this is open (a pending payment approved, a restore elsewhere) closes it.
    val pro = ChromaRepository.settings.pro
    LaunchedEffect(pro) { if (pro) unlocked() }
    // Closed without buying, the wish goes with it: a purchase made later from elsewhere opens nothing.
    DisposableEffect(Unit) { onDispose { Paywall.onPro = null } }
    var note by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        pack = Billing.proPackage()
        loading = false
        if (pack == null) note = S.storeUnavailable
    }

    val target = pack
    val price = target?.storeProduct?.price?.formatted
    val colors = MaterialTheme.colorScheme
    val year = today().year
    val mine = ChromaRepository.journal.filterKeys { it.startsWith("$year-") }.mapValues { it.value.color }
    if (LocalLocked.current) return
    Dialog(onDismissRequest = { if (!busy) onDismiss() }) {
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surface)
                // With large text, in another language or sideways, the buy button would fall off
                // the window, and this is the only way to pay.
                .verticalScroll(rememberScrollState())
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
            // While the store answers, the button's place is kept: the dialog does not jump when the price arrives.
            if (loading) {
                PrimaryAction(S.working, {}, Modifier.fillMaxWidth(), enabled = false)
                Spacer(Modifier.height(4.dp))
            }
            // With no store there is no price, and a button that cannot say what it costs is not an
            // offer: the note explains it instead.
            if (target != null && price != null) {
                PrimaryAction(
                    label = if (busy) S.working else S.buy(price),
                    // A payment on its way is not bought twice: the store would only say it is owned.
                    enabled = !busy && !pending,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        busy = true
                        note = null
                        scope.launch {
                            val outcome = Billing.purchase(target)
                            busy = false
                            when (outcome) {
                                PurchaseOutcome.Success -> unlocked()
                                // Changing your mind says nothing and shows nothing.
                                PurchaseOutcome.Cancelled -> Unit
                                PurchaseOutcome.Pending -> {
                                    note = S.buyPending
                                    pending = true
                                }
                                PurchaseOutcome.Offline -> note = S.buyOffline
                                PurchaseOutcome.Unreachable -> note = S.storeUnavailable
                                PurchaseOutcome.Failed -> note = S.buyFailed
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
                            val outcome = Billing.restore()
                            busy = false
                            when (outcome) {
                                RestoreOutcome.Found -> unlocked()
                                RestoreOutcome.Nothing -> note = S.restoreNothing
                                RestoreOutcome.Unreachable -> note = S.storeUnavailable
                            }
                        }
                    },
                )
                TextAction(S.notNow, enabled = !busy, quiet = true, onClick = { onDismiss() })
            }
        }
    }
}
