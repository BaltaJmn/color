package com.baltajmn.color.review

/**
 * Asks the store for an in-app rating. The system decides on its own whether anything is actually
 * shown: both stores throttle this internally, so the caller only has to ask at a moment that makes
 * sense and not worry about asking too often. A failure (no host to show it in, API unavailable) is
 * ignored in silence.
 */
expect object Review {
    fun request()
}
