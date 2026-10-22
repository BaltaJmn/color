package com.baltajmn.color.social

import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.SignInResultData

internal actual fun NativeSignInResult.appleName(): String? {
    val name = ((this as? NativeSignInResult.Success)?.data as? SignInResultData.Apple)?.credential?.fullName ?: return null
    return listOf(name.givenName, name.familyName).firstOrNull { !it.isNullOrBlank() }
}
