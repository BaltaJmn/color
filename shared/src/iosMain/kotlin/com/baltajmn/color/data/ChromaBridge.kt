package com.baltajmn.color.data

/** The little that Swift needs to ask Kotlin, and the little Kotlin needs from Swift. */
object ChromaBridge {
    /** Assigned by iOSApp.swift: WidgetCenter belongs to Swift, and Kotlin only asks. */
    var reloadWidgets: (() -> Unit)? = null
}
