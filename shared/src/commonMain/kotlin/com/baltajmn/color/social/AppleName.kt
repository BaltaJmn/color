package com.baltajmn.color.social

import io.github.jan.supabase.compose.auth.composable.NativeSignInResult

/**
 * The first name Apple gives in a native sign in, which only exists in the credential it hands to
 * the app, once. Null on Android (Apple goes through the web there, and the server stores the name
 * in the user's metadata) and for any other result.
 */
internal expect fun NativeSignInResult.appleName(): String?
