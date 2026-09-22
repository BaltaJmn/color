package com.baltajmn.color.data

/** The little that Swift needs to ask Kotlin, and the little Kotlin needs from Swift. */
object ChromaBridge {
    /** Assigned by iOSApp.swift: WidgetCenter belongs to Swift, and Kotlin only asks. */
    var reloadWidgets: (() -> Unit)? = null

    /** com.baltajmn.color://today from a widget. Anything else is left alone rather than guessed. */
    fun open(url: String) {
        Route.pending = url.substringAfterLast('/').takeIf { it.isNotEmpty() }
    }
}
