package com.baltajmn.color.ui

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.baltajmn.color.billing.Billing
import com.baltajmn.color.billing.ProOffer
import com.baltajmn.color.billing.ProState
import com.baltajmn.color.billing.PurchaseOutcome
import com.baltajmn.color.billing.RestoreOutcome
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.data.PRIVACY_URL
import com.baltajmn.color.data.TERMS_URL
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

    /** Opened by the one offer after a week of colors, not by the user reaching a wall: adds a line of why. */
    var offered by mutableStateOf(false)

    /** What the user was reaching for when the paywall opened: done as soon as Pro arrives. */
    var onPro: (() -> Unit)? = null
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanOption(
    title: String,
    price: String,
    caption: String?,
    badge: String?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline, RoundedCornerShape(16.dp))
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(14.dp),
    ) {
        // With large text the price goes under the name rather than breaking it in the middle of a word.
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = Styles.body, modifier = Modifier.padding(end = 12.dp))
            Text(price, style = Styles.body)
        }
        badge?.let { Text(it, style = Styles.caption.copy(color = colors.primary)) }
        caption?.let { Text(it, style = Styles.caption) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProDialog(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var offer by remember { mutableStateOf<ProOffer?>(null) }
    var picked by remember { mutableStateOf<Package?>(null) }
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
    DisposableEffect(Unit) {
        onDispose {
            Paywall.onPro = null
            Paywall.offered = false
        }
    }
    var note by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        offer = Billing.proOffer()
        loading = false
        if (offer == null) note = S.storeUnavailable
    }

    // The lifetime purchase is the default and the highlighted one; the month is mostly its anchor.
    val target = picked ?: offer?.let { it.lifetime ?: it.monthly }
    val price = target?.storeProduct?.price?.formatted
    val monthly = offer?.monthly
    val lifetime = offer?.lifetime
    val uriHandler = LocalUriHandler.current
    val colors = MaterialTheme.colorScheme
    val year = today().year
    val mine = ChromaRepository.journal.filterKeys { it.startsWith("$year-") }.mapValues { it.value.color }
    if (LocalLocked.current) return
    val scroll = rememberScrollState()
    val paper = colors.surface
    Dialog(onDismissRequest = { if (!busy) onDismiss() }) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(paper)) {
            Column(
                Modifier.fillMaxWidth()
                    // What is left below fades out instead of ending on a cut line.
                    .drawWithContent {
                        drawContent()
                        val fade = 32.dp.toPx()
                        if (scroll.canScrollForward) {
                            drawRect(
                                Brush.verticalGradient(listOf(Color.Transparent, paper), startY = size.height - fade, endY = size.height),
                                topLeft = Offset(0f, size.height - fade),
                            )
                        }
                    }
                    // With large text, in another language or sideways, the buy button would fall off
                    // the window, and this is the only way to pay.
                    .verticalScroll(scroll)
                    .padding(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlyphTile(Glyph.SPARK)
                    Spacer(Modifier.width(12.dp))
                    Text(S.proTitle, style = Styles.title, modifier = Modifier.weight(1f))
                    // The close button's place, kept clear of the title.
                    Spacer(Modifier.width(36.dp))
                }
                if (Paywall.offered) {
                    Spacer(Modifier.height(12.dp))
                    Text(S.proWeekIntro, style = Styles.body)
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
                // "No subscription" is only true while the month is not on offer.
                val once = if (monthly == null) S.proOnce else ""
                Text(if (Social.available) "$once ${S.proFriendsFree}".trim() else once, style = Styles.caption)
                if (Billing.state == ProState.Ended) {
                    Spacer(Modifier.height(8.dp))
                    Text(S.subscriptionEnded, style = Styles.caption.copy(color = colors.onBackground))
                }
                note?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = Styles.caption.copy(color = colors.onBackground))
                }
                Spacer(Modifier.height(20.dp))
                if (lifetime != null && monthly != null) {
                    PlanOption(
                        title = S.planForever,
                        price = lifetime.storeProduct.price.formatted,
                        caption = S.foreverNote,
                        badge = S.bestValue,
                        selected = target == lifetime,
                        enabled = !busy,
                        onClick = { picked = lifetime },
                    )
                    Spacer(Modifier.height(8.dp))
                    PlanOption(
                        title = S.planMonthly,
                        price = S.perMonth(monthly.storeProduct.price.formatted),
                        caption = null,
                        badge = null,
                        selected = target == monthly,
                        enabled = !busy,
                        onClick = { picked = monthly },
                    )
                    Spacer(Modifier.height(16.dp))
                }
                // While the store answers, the button's place is kept: the dialog does not jump when the price arrives.
                if (loading) {
                    PrimaryAction(S.working, {}, Modifier.fillMaxWidth(), enabled = false)
                    Spacer(Modifier.height(4.dp))
                }
                // With no store there is no price, and a button that cannot say what it costs is not an
                // offer: the note explains it instead.
                if (target != null && price != null) {
                    PrimaryAction(
                        label = when {
                            busy -> S.working
                            target == monthly -> S.subscribe(price)
                            else -> S.buy(price)
                        },
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
                // Apple 3.1.2: renewal and how to cancel whenever the month is on offer, and the two documents
                // always, so a product the store did not return cannot take them away (MoodTraker 1.1 was
                // rejected for not showing them, 20-10-2026).
                Spacer(Modifier.height(8.dp))
                if (monthly != null) Text(S.subscriptionTerms, style = Styles.caption)
                // Flow rows and not Rows: in a Row "Privacy policy" got what "Terms of use" left and broke in two
                // on an iPhone; here a link that does not fit goes to the next line whole.
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextAction(S.termsOfUse, quiet = true, onClick = { runCatching { uriHandler.openUri(TERMS_URL) } })
                    TextAction(S.privacyRow, quiet = true, onClick = { runCatching { uriHandler.openUri(PRIVACY_URL) } })
                }
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
            // Pinned over the scroll: "Not now" sits under the plans, below the fold with large text,
            // and leaving has to be in sight from the start.
            GlyphButton(
                Glyph.CLOSE,
                S.a11yClose,
                { onDismiss() },
                // On a disc of the dialog's paper: what scrolls under it does not show through.
                Modifier.align(Alignment.TopEnd).padding(top = 18.dp, end = 12.dp).background(paper, CircleShape),
                enabled = !busy,
            )
        }
    }
}
