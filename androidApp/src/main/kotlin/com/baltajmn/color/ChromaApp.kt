package com.baltajmn.color

import android.app.Application
import android.content.res.Configuration
import com.baltajmn.color.data.AndroidContext
import com.baltajmn.color.data.ChromaRepository

/**
 * Runs before anything else in the process. The system also starts it just to refresh a widget,
 * and the widgets read their file through AndroidContext like everyone else.
 */
class ChromaApp : Application() {
    private var drawnFor: String? = null

    override fun onCreate() {
        super.onCreate()
        AndroidContext.init(this)
        drawnFor = widgetKey(resources.configuration)
    }

    // The widgets are drawn with the theme and the language of the moment, and nothing else redraws
    // them when either changes: the Activity no longer restarts for the dark mode, and with the app
    // in the background there is no Activity at all.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val key = widgetKey(newConfig)
        if (key == drawnFor) return
        drawnFor = key
        ChromaRepository.ensureLoaded()
        ChromaRepository.syncWidgets()
    }

    private fun widgetKey(c: Configuration) = "${c.uiMode and Configuration.UI_MODE_NIGHT_MASK} ${c.locales.toLanguageTags()}"
}
