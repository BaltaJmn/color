package com.baltajmn.color.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Where something outside the app asked to land: a widget, a tile or a link. It is left here
 * because the app may not be running yet, and App picks it up whenever it does.
 */
object Route {
    /** "today", "year" or "pro". Anything else is ignored rather than guessed at. */
    var pending by mutableStateOf<String?>(null)
}
