package com.baltajmn.color.data

import com.baltajmn.color.social.LOGIN_HOST
import com.baltajmn.color.social.LOGIN_SCHEME
import com.baltajmn.color.social.Social
import io.github.jan.supabase.auth.handleDeeplinks
import platform.Foundation.NSURL

/** The little that Swift needs to ask Kotlin, and the little Kotlin needs from Swift. */
object ChromaBridge {
    /** Assigned by iOSApp.swift: WidgetCenter belongs to Swift, and Kotlin only asks. */
    var reloadWidgets: (() -> Unit)? = null

    /**
     * com.baltajmn.color://today from a widget, or ://login back from the web sign in. Anything else
     * is left alone rather than guessed.
     */
    fun open(url: String) {
        if (url.startsWith("$LOGIN_SCHEME://$LOGIN_HOST")) {
            if (Social.available) NSURL.URLWithString(url)?.let { Social.client.handleDeeplinks(it) }
            return
        }
        Route.pending = url.substringAfterLast('/').takeIf { it.isNotEmpty() }
    }
}
