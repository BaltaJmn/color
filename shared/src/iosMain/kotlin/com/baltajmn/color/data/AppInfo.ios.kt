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

    actual fun openSettings() = open(UIApplicationOpenSettingsURLString)
}

actual val Sibling.storeUrl: String? get() = iosUrl

actual val onIos: Boolean = true
