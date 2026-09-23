package com.baltajmn.color.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.baltajmn.color.billing.Billing
import com.baltajmn.color.billing.PurchaseOutcome
import com.baltajmn.color.i18n.S
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
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(S.proTitle, style = Styles.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(S.proPoster, S.proYearWidget, S.proStats).forEach { Text("- $it", style = Styles.body) }
                Text(S.proOnce, style = Styles.body)
                note?.let { Text(it, style = Styles.muted) }
            }
        },
        // With no store there is no price, and a button that cannot say what it costs is not an
        // offer: the note explains it instead.
        confirmButton = {
            if (target != null && price != null) TextAction(
                label = if (busy) S.working else S.buy(price),
                enabled = !busy,
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
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextAction(
                    label = S.restore,
                    enabled = !busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            val found = Billing.restore()
                            busy = false
                            if (found) onDismiss() else note = S.restoreNothing
                        }
                    },
                )
                TextAction(S.notNow, enabled = !busy, onClick = { onDismiss() })
            }
        },
    )
}
