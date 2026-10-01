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

/** A clock that keeps counting while the phone sleeps and that the user cannot set. */
expect fun elapsedMillis(): Long

/** A real trip leaves the app within moments of being started; a mark older than this is stale. */
internal const val TRIP_WINDOW_MS = 10_000L

/**
 * Marked on the way out to the camera, a picker or a share sheet. Coming back from one of those is
 * not walking away from the app, so the lock waits [com.baltajmn.color.TRIP_GRACE] instead of a
 * minute. A time and not a flag: a sheet that never takes the app off screen (iOS) or a launch that
 * fails leaves the mark behind, and it must not give the next ordinary exit ten minutes.
 */
object Trip {
    private var at: Long? = null

    fun start() {
        at = elapsedMillis()
    }

    /** Whether leaving the screen at [leftAt] was this trip. Read once: the mark goes with it. */
    fun consume(leftAt: Long): Boolean = tookTrip(at, leftAt).also { at = null }
}

internal fun tookTrip(startedAt: Long?, leftAt: Long): Boolean =
    startedAt != null && leftAt - startedAt in 0..TRIP_WINDOW_MS
