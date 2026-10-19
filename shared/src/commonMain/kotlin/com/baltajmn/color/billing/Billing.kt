package com.baltajmn.color.billing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.color.data.ChromaRepository
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction

/**
 * The public SDK key of the RevenueCat project, one per store. Null until the project exists, and
 * the app then runs as free only instead of crashing.
 */
expect val revenueCatApiKey: String?

/**
 * Two ways to the same [ENTITLEMENT]: the lifetime purchase (the one that is pushed) and a monthly
 * subscription (mostly the anchor beside it). Product ids and prices live in the RevenueCat
 * dashboard and never here, so changing a price is not an app update (docs/tecnico.md 6.8).
 */
object Billing {

    const val ENTITLEMENT = "pro"

    private var configured = false

    /** What the user holds, for the Settings line and the "your plan ended" note on the paywall. */
    var state by mutableStateOf(ProState.Free)
        private set

    private fun apply(info: CustomerInfo): Boolean {
        val e = info.entitlements[ENTITLEMENT]
        val next = proState(e?.isActive == true, e != null, e?.expirationDateMillis)
        state = next
        ChromaRepository.updatePro(next.isPro)
        return next.isPro
    }

    /** Called once at startup. With no key it does nothing at all. */
    fun configure() {
        if (configured) return
        val key = revenueCatApiKey ?: return
        configured = true
        Purchases.logLevel = LogLevel.WARN
        Purchases.configure(PurchasesConfiguration.Builder(key).build())
        // A payment approved later (a carrier, a parent) or a refund arrives here, with the app open
        // or on its next start, without waiting for anyone to ask.
        Purchases.sharedInstance.delegate = object : PurchasesDelegate {
            override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
                apply(customerInfo)
            }

            override fun onPurchasePromoProduct(
                product: StoreProduct,
                startPurchase: (
                    onError: (error: PurchasesError, userCancelled: Boolean) -> Unit,
                    onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit,
                ) -> Unit,
            ) = Unit
        }
    }

    /**
     * Re-checks the entitlement against the store. A failure leaves the cached value alone on
     * purpose: whoever paid keeps their Pro on a plane.
     */
    suspend fun refresh() {
        if (!configured) return
        runCatching { Purchases.sharedInstance.awaitCustomerInfo() }
            .onSuccess { apply(it) }
    }

    /** What to sell, or null while offline or before the dashboard is filled in. */
    suspend fun proOffer(): ProOffer? {
        if (!configured) return null
        val offering = runCatching { Purchases.sharedInstance.awaitOfferings().current }.getOrNull() ?: return null
        val lifetime = offering.lifetime ?: offering.availablePackages.firstOrNull { it != offering.monthly }
        return if (lifetime == null && offering.monthly == null) null else ProOffer(lifetime, offering.monthly)
    }

    suspend fun purchase(pack: Package): PurchaseOutcome = try {
        val purchase = Purchases.sharedInstance.awaitPurchase(packageToPurchase = pack)
        val active = apply(purchase.customerInfo)
        // The store took it and the entitlement did not move: a dashboard mistake, not a purchase.
        if (active) PurchaseOutcome.Success else PurchaseOutcome.Failed
    } catch (e: PurchasesTransactionException) {
        when {
            e.userCancelled -> PurchaseOutcome.Cancelled
            // Paid later or by someone else (a carrier, a parent): the store finishes it on its own,
            // and the delegate above turns Pro on when it does.
            e.code == PurchasesErrorCode.PaymentPendingError -> PurchaseOutcome.Pending
            // Already theirs (a pending payment that went through, a second tap): what is missing is
            // the sync, not a purchase.
            e.code == PurchasesErrorCode.ProductAlreadyPurchasedError ->
                if (restore() == RestoreOutcome.Found) PurchaseOutcome.Success else PurchaseOutcome.Failed
            // On Android a network error comes after Play charged, while the receipt was on its way:
            // the SDK sends it again by itself. Not "the store is down", and not a failure.
            e.code.offline -> PurchaseOutcome.Offline
            // The store unreachable before anything was charged.
            e.code == PurchasesErrorCode.StoreProblemError -> PurchaseOutcome.Unreachable
            else -> PurchaseOutcome.Failed
        }
    } catch (e: Exception) {
        PurchaseOutcome.Failed
    }

    /**
     * Both stores require this to be reachable without buying anything first. Not reaching the store
     * is not "nothing to restore": that answer would scare whoever paid.
     */
    suspend fun restore(): RestoreOutcome {
        if (!configured) return RestoreOutcome.Unreachable
        val info = try {
            Purchases.sharedInstance.awaitRestore()
        } catch (e: Exception) {
            return RestoreOutcome.Unreachable
        }
        val active = apply(info)
        return if (active) RestoreOutcome.Found else RestoreOutcome.Nothing
    }

    private val PurchasesErrorCode.offline: Boolean
        get() = this == PurchasesErrorCode.NetworkError || this == PurchasesErrorCode.OfflineConnectionError
}

/** The two packages of the current offering; either can be missing while the dashboard is half filled. */
class ProOffer(val lifetime: Package?, val monthly: Package?)

enum class ProState(val isPro: Boolean) { Free(false), Monthly(true), Lifetime(true), Ended(false) }

/**
 * Lifetime or an active monthly plan is Pro; a monthly plan that lapsed is [ProState.Ended], which
 * is Free plus a note on the paywall. A lifetime purchase has no expiration date.
 */
internal fun proState(active: Boolean, everHad: Boolean, expiresAtMillis: Long?): ProState = when {
    active && expiresAtMillis == null -> ProState.Lifetime
    active -> ProState.Monthly
    everHad -> ProState.Ended
    else -> ProState.Free
}

enum class PurchaseOutcome { Success, Pending, Cancelled, Offline, Unreachable, Failed }

enum class RestoreOutcome { Found, Nothing, Unreachable }
