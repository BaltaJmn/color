package com.baltajmn.color.data

import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

actual object AppInfo {

    actual val version: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""

    actual fun open(url: String) {
        NSURL.URLWithString(url)?.let { UIApplication.sharedApplication.openURL(it) }
    }

    // A trip of our own: coming back does not ask for the lock after one minute, nor for a rating.
    actual fun openSettings() {
        Trip.start()
        open(UIApplicationOpenSettingsURLString)
    }
}

actual val Sibling.storeUrl: String? get() = iosUrl

actual val onIos: Boolean = true
