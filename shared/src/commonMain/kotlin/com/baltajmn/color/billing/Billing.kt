package com.baltajmn.color.billing

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
 * The single paid product: a non-consumable that grants [ENTITLEMENT] forever. The product id and
 * the price live in the RevenueCat dashboard and never here, so changing the price is not an app
 * update (docs/tecnico.md 6.8).
 */
object Billing {

    const val ENTITLEMENT = "pro"

    private var configured = false

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
                ChromaRepository.updatePro(customerInfo.entitlements[ENTITLEMENT]?.isActive == true)
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
            .onSuccess { ChromaRepository.updatePro(it.entitlements[ENTITLEMENT]?.isActive == true) }
    }

    /** What to sell, or null while offline or before the dashboard is filled in. */
    suspend fun proPackage(): Package? {
        if (!configured) return null
        return runCatching {
            Purchases.sharedInstance.awaitOfferings().current?.availablePackages?.firstOrNull()
        }.getOrNull()
    }

    suspend fun purchase(pack: Package): PurchaseOutcome = try {
        val purchase = Purchases.sharedInstance.awaitPurchase(packageToPurchase = pack)
        val active = purchase.customerInfo.entitlements[ENTITLEMENT]?.isActive == true
        ChromaRepository.updatePro(active)
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
        val active = info.entitlements[ENTITLEMENT]?.isActive == true
        ChromaRepository.updatePro(active)
        return if (active) RestoreOutcome.Found else RestoreOutcome.Nothing
    }

    private val PurchasesErrorCode.offline: Boolean
        get() = this == PurchasesErrorCode.NetworkError || this == PurchasesErrorCode.OfflineConnectionError
}

enum class PurchaseOutcome { Success, Pending, Cancelled, Offline, Unreachable, Failed }

enum class RestoreOutcome { Found, Nothing, Unreachable }
