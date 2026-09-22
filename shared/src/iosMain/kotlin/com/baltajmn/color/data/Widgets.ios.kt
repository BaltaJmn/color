package com.baltajmn.color.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.writeToURL

const val APP_GROUP = "group.com.baltajmn.color"

/**
 * The App Group is the one place the widget extension can read, which is exactly why the journal
 * never goes here: only this file does.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun writeWidgetState(json: String) {
    val container = NSFileManager.defaultManager
        .containerURLForSecurityApplicationGroupIdentifier(APP_GROUP)
        ?: error("no App Group container")
    val url = container.URLByAppendingPathComponent("widget.json") ?: error("no widget.json path")
    (json as NSString).writeToURL(url, true, NSUTF8StringEncoding, null)
}

/** Swift owns WidgetCenter; it hands the call back through ChromaBridge when the app starts. */
actual fun refreshWidgets() {
    ChromaBridge.reloadWidgets?.invoke()
}
