package com.baltajmn.color

import android.app.Application
import com.baltajmn.color.data.AndroidContext

/**
 * Runs before anything else in the process. The system also starts it just to refresh a widget,
 * and the widgets read their file through AndroidContext like everyone else.
 */
class ChromaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContext.init(this)
    }
}
