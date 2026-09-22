package com.baltajmn.color.social

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.appleNativeLogin
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Where the OAuth web flow comes back to: com.baltajmn.color://login. */
const val LOGIN_SCHEME = "com.baltajmn.color"
const val LOGIN_HOST = "login"

@Serializable
data class Profile(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("invite_code") val inviteCode: String = "",
)

@Serializable
private data class NewProfile(val id: String, @SerialName("display_name") val displayName: String)

@Serializable
private data class Rename(@SerialName("display_name") val displayName: String)

/**
 * The session with Supabase, and who this person is to their friends. Nothing here is needed to use
 * Chroma: without an account, or without a project at all, the app is v1.0.
 */
object Social {

    val available: Boolean get() = SupabaseConfig.url != null && SupabaseConfig.anonKey != null

    val client: SupabaseClient by lazy {
        createSupabaseClient(SupabaseConfig.url!!, SupabaseConfig.anonKey!!) {
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = LOGIN_SCHEME
                host = LOGIN_HOST
            }
            install(Postgrest)
            install(Storage)
            install(Functions)
            install(ComposeAuth) {
                SupabaseConfig.googleServerClientId?.let { googleNativeLogin(serverClientId = it) }
                appleNativeLogin()
            }
        }
    }

    /** This person's row, once signed in and named. Null while unknown, and when there is none yet. */
    var me by mutableStateOf<Profile?>(null)
        private set

    /** The profile was asked for and there is none: the first sign in, which still needs a name. */
    var needsName by mutableStateOf(false)
        private set

    fun userId(): String? = if (available) client.auth.currentUserOrNull()?.id else null

    /** Throws when offline; the caller says so and offers to retry. */
    suspend fun loadMe() {
        val id = userId() ?: return
        val row = client.from("profiles").select { filter { eq("id", id) } }.decodeSingleOrNull<Profile>()
        me = row
        needsName = row == null
    }

    suspend fun createProfile(name: String) {
        val id = userId() ?: return
        client.from("profiles").insert(NewProfile(id, name.trim()))
        loadMe()
    }

    suspend fun rename(name: String) {
        val id = userId() ?: return
        client.from("profiles").update(Rename(name.trim())) { filter { eq("id", id) } }
        loadMe()
    }

    /** Leaves the account where it is: signing in again brings the friends back. */
    suspend fun signOut() {
        runCatching { client.auth.signOut() }
        me = null
        needsName = false
    }

    internal fun forget() {
        me = null
        needsName = false
    }
}

/** The name rule of the server (1 to 30), said once for the field and the button. */
fun isValidName(name: String): Boolean = name.trim().length in 1..30
