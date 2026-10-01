package com.baltajmn.color.review

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.review.ReviewManagerFactory
import java.lang.ref.WeakReference

actual object Review {

    /** Set by MainActivity: the Play In-App Review API needs a live Activity to show anything in. */
    var host: WeakReference<Activity>? = null

    // One flow at a time: two quick returns would otherwise end in two sheets.
    private var asking = false

    actual fun request(onAsked: () -> Unit) {
        val activity = host?.get() ?: return
        if (asking) return
        asking = true
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            // Left before the store answered: nothing is shown and nothing is spent. A failed request
            // has nothing to launch. launchReviewFlow never says whether it showed anything (by
            // design), so finishing the flow is what counts as asked.
            val inFront = (activity as? LifecycleOwner)?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true
            if (!task.isSuccessful || !inFront) {
                asking = false
                return@addOnCompleteListener
            }
            runCatching {
                manager.launchReviewFlow(activity, task.result).addOnCompleteListener {
                    asking = false
                    onAsked()
                }
            }.onFailure { asking = false }
        }
    }
}
