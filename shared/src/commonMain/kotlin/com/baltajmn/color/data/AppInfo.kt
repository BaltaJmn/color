package com.baltajmn.color.data

import com.baltajmn.color.i18n.S

/**
 * The published privacy policy. Play, the App Store and the About section all point at the same
 * URL, so it lives here and not in three places. Source of the page: web/index.html.
 */
const val PRIVACY_URL = "https://color.baltajmn.dev/"

/** The few things about the build itself that the About section needs. */
expect object AppInfo {
    /** Version name as the store shows it, "1.0". */
    val version: String

    fun open(url: String)
}

data class Sibling(val name: String, val tagline: String, val androidUrl: String?, val iosUrl: String?)

/** The store of the platform this build runs on, or null while that app has no page there. */
expect val Sibling.storeUrl: String?

expect val onIos: Boolean

/**
 * The sister apps. A store URL only once that app is in production on that store the day Chroma is
 * published; until then it is null and its row is not shown.
 */
val SIBLINGS: List<Sibling>
    get() = listOf(
        Sibling("Quilt", S.siblingQuilt, androidUrl = null, iosUrl = null),
        Sibling("MoodTraker", S.siblingMood, androidUrl = null, iosUrl = null),
        Sibling("Purl", S.siblingPurl, androidUrl = null, iosUrl = null),
    )
