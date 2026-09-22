package com.baltajmn.color.data

/**
 * One nudge a day, at one time, that shuts up once the day has its color. Both platforms read the
 * settings and the journal themselves, so every caller only has to say "things changed".
 */
expect object Reminder {
    /**
     * Re-books everything from the current settings. [askPermission] only when the user just turned
     * the reminder on: asking at start-up is rude, and on Android it cannot be done from composition.
     */
    fun sync(askPermission: Boolean)
}
