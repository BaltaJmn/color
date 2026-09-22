package com.baltajmn.color.social

/**
 * The public half of the Supabase project (store/servidor.md 1). Both values travel in the binary
 * anyway, so they may live here; the service key never does. Null until the project exists, and
 * then Friends does not appear at all and the app is v1.0.
 */
object SupabaseConfig {
    /** "https://<ref>.supabase.co". */
    val url: String? = null

    /** The anon key. Public by design: RLS is what protects the data. */
    val anonKey: String? = null

    /** The web OAuth client of Google, which native sign-in on Android asks for. */
    val googleServerClientId: String? = null
}
