package com.baltajmn.color.social

import android.content.Intent
import io.github.jan.supabase.auth.handleDeeplinks

/**
 * The web sign in (Apple on Android) comes back as com.baltajmn.color://login?code=..., and an
 * invite as https://color.baltajmn.dev/i/<code> (a group's, /i/g<code>). Anything else is left alone.
 */
fun handleLinkIntent(intent: Intent?) {
    val data = intent?.data ?: return
    if (!Social.available) return
    if (data.scheme == LOGIN_SCHEME && data.host == LOGIN_HOST) {
        Social.client.handleDeeplinks(intent)
    } else {
        val url = data.toString()
        groupCodeOf(url)?.let(Groups::opened) ?: inviteCodeOf(url)?.let(Friends::opened)
    }
}
