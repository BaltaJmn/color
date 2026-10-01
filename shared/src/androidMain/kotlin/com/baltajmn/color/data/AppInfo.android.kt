package com.baltajmn.color.data

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri

actual object AppInfo {

    actual val version: String
        get() = runCatching {
            val context = AndroidContext.value
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: ""

    actual fun open(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { AndroidContext.value.startActivity(intent) }
    }

    actual fun openSettings() {
        // A trip of our own: coming back does not ask for the lock after one minute, nor for a rating.
        Trip.start()
        val context = AndroidContext.value
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}

actual val Sibling.storeUrl: String? get() = androidUrl

actual val onIos: Boolean = false
