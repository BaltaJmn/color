package com.baltajmn.color.social

/**
 * The public half of the Supabase project (store/servidor.md 1). Both values travel in the binary
 * anyway, so they may live here; the service key never does. Null until the project exists, and
 * then Friends does not appear at all and the app is v1.0.
 */
object SupabaseConfig {
    /** "https://<ref>.supabase.co". */
    val url: String? = "https://djfbmvagflfptqhabdln.supabase.co"

    /** The anon key. Public by design: RLS is what protects the data. */
    val anonKey: String? =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRqZmJtdmFnZmxmcHRxaGFiZGxuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTI0MDEyMzQsImV4cCI6MjEwNzk3NzIzNH0.1Ybi39q6bcAnPfR9AKulFgdacx8NhtpWF-VF3vzGL98"

    /** The web OAuth client of Google, which native sign-in on Android asks for. */
    val googleServerClientId: String? = "416801056544-dk993o86hbfcv974aj4mu7vtkfmgo3a5.apps.googleusercontent.com"
}
