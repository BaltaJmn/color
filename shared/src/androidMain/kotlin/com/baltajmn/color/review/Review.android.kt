package com.baltajmn.color.review

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import java.lang.ref.WeakReference

actual object Review {

    /** Set by MainActivity: the Play In-App Review API needs a live Activity to show anything in. */
    var host: WeakReference<Activity>? = null

    actual fun request() {
        val activity = host?.get() ?: return
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            // A failed request has nothing to launch. launchReviewFlow itself never reports whether
            // it actually showed anything: that is by design, so no result is worth reacting to.
            if (task.isSuccessful) runCatching { manager.launchReviewFlow(activity, task.result) }
        }
    }
}
