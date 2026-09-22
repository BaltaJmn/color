package com.baltajmn.color.social

import android.content.Intent
import io.github.jan.supabase.auth.handleDeeplinks

/** The web sign in (Apple on Android) comes back as com.baltajmn.color://login?code=... */
fun handleLoginIntent(intent: Intent?) {
    val data = intent?.data ?: return
    if (Social.available && data.scheme == LOGIN_SCHEME && data.host == LOGIN_HOST) {
        Social.client.handleDeeplinks(intent)
    }
}
