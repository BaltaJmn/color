package com.baltajmn.color.i18n

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

/** "es-ES", "en-GB": only the language matters here. iOS relaunches the app to change it, so once is enough. */
private val language: String by lazy { (NSLocale.preferredLanguages.firstOrNull() as? String)?.take(2) ?: "en" }

actual fun systemLanguage(): String = language
