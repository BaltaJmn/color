package com.baltajmn.color.review

import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindowScene

actual object Review {

    actual fun request(onAsked: () -> Unit) {
        // requestReviewInScene needs a live, connected window scene; no scene yet is nothing to
        // silently give up on, same as no Activity is on Android.
        // The active one: a scene still coming to the front drops the request, and it would be spent.
        val scene = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive } ?: return
        SKStoreReviewController.requestReviewInScene(scene)
        onAsked()
    }
}
