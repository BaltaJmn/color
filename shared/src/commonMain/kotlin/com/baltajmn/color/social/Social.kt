package com.baltajmn.color.social

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.color.data.ChromaRepository
import com.baltajmn.color.model.Share
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.appleNativeLogin
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.Storage
import io.ktor.http.isSuccess
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    /**
     * Throws when offline, and when there is no live session yet (a stale token that has not been
     * renewed): the caller says so and offers to retry, instead of showing a wait that never ends.
     */
    suspend fun loadMe() {
        userId() ?: error("no session")
        // Through a function: the invite code is not readable from the table, not even one's own.
        val row = client.postgrest.rpc("my_profile").decodeList<Profile>().firstOrNull()
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

    /**
     * Leaves the account where it is: signing in again brings the friends back. What is still queued
     * stays queued for that account, and goes to no other.
     */
    suspend fun signOut() = withContext(NonCancellable) {
        // Offline the server cannot be told, but this phone must still let go of the session.
        runCatching { client.auth.signOut() }.onFailure { runCatching { client.auth.clearSession() } }
        forget(accountGone = false)
    }

    /**
     * Apple 5.1.1(v) and Play. The server erases the profile, the friendships, what was shared and
     * its photos; the journal on this phone is not the server's to touch.
     */
    suspend fun deleteAccount() {
        val answer = client.functions.invoke("delete-account")
        if (!answer.status.isSuccess()) error("delete-account: ${answer.status}")
        // Past this point the account is gone whatever happens to the screen that asked: leaving
        // Settings mid-request must not leave a session to an account that no longer exists.
        withContext(NonCancellable) {
            // No sign out call: the user no longer exists for the server to sign out of.
            client.auth.clearSession()
            forget(accountGone = true)
        }
    }

    private val scope by lazy { MainScope() }
    private var watching = false

    /**
     * The client drops a session by itself when its refresh token is revoked or its user is deleted
     * from the dashboard, and nothing else says so: this phone must stop showing that account to
     * whoever signs in next. Once per process. It also fires after signOut and deleteAccount, which
     * have already cleaned up: [forget] is safe to run twice.
     */
    fun watchSession() {
        if (!available || watching) return
        watching = true
        scope.launch {
            var before: SessionStatus? = null
            client.auth.sessionStatus.collect { now ->
                if (sessionLost(before, now)) runCatching { forget(accountGone = false) }
                before = now
            }
        }
    }

    internal suspend fun forget(accountGone: Boolean) {
        me = null
        needsName = false
        Friends.forget()
        // Without an account nothing is shared, so a day saved now must not be marked as if it were:
        // it would go up, unseen, the day anyone signs in. The question is asked again with the first friend.
        ChromaRepository.updateSettings { it.copy(defaultShare = Share.Private, shareAsked = false) }
        // The server kept nothing of the days that were shared: the phone stops saying they are.
        if (accountGone) Outbox.releaseAll(owner = null)
    }
}

/**
 * A session this phone had and the client has let go of. Not the NotAuthenticated that a phone
 * without an account starts with: there is nothing of anyone to forget there. That one has
 * isSignOut false; the client clears a session it rejects at start (Initializing straight to
 * NotAuthenticated) with isSignOut true, and that one is a loss with no earlier status to show it.
 */
internal fun sessionLost(before: SessionStatus?, now: SessionStatus): Boolean =
    now is SessionStatus.NotAuthenticated &&
        (now.isSignOut || before is SessionStatus.Authenticated || before is SessionStatus.RefreshFailure)

/**
 * Whether this phone has an account, as state: a live session, or one that is only waiting for the
 * network to renew it. Settings and Today need it before Friends has ever been opened.
 */
@Composable
fun hasAccount(): Boolean {
    if (!Social.available) return false
    val status by Social.client.auth.sessionStatus.collectAsState()
    return status is SessionStatus.Authenticated || status is SessionStatus.RefreshFailure
}

/**
 * Whether the session is live right now, as state. Unlike [hasAccount] it turns false while the token
 * waits for the network and true again when the client renews it, so an effect keyed on it runs
 * again on that return.
 */
@Composable
fun hasLiveSession(): Boolean {
    if (!Social.available) return false
    val status by Social.client.auth.sessionStatus.collectAsState()
    return status is SessionStatus.Authenticated
}

/** The name rule of the server (1 to 30), said once for the field and the button. */
fun isValidName(name: String): Boolean = name.trim().length in 1..30
