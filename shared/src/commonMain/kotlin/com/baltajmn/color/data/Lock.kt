package com.baltajmn.color.data

/**
 * The lock of the phone, never one of ours. An app inventing its own PIN is one more secret to
 * lose, and the face or the code that opens the phone is already the answer the user knows.
 */
expect object Lock {
    /** False when the phone has no screen lock at all: then the switch cannot be turned on. */
    fun isAvailable(): Boolean

    fun authenticate(onResult: (Boolean) -> Unit)

    /** Hides the app from the task switcher while the lock is on. Nothing to do on iOS: Swift paints it. */
    fun setHidesPreview(on: Boolean)
}
