package com.baltajmn.color.review

import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene

actual object Review {

    actual fun request() {
        // requestReviewInScene needs a live, connected window scene; no scene yet is nothing to
        // silently give up on, same as no Activity is on Android.
        val scene = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull() ?: return
        SKStoreReviewController.requestReviewInScene(scene)
    }
}
