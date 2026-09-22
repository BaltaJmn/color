package com.baltajmn.color.data

import java.io.File

/** filesDir, next to the journal but readable by the widget process, which the journal never is. */
actual fun writeWidgetState(json: String) {
    val dir = Storage.rootOverride ?: AndroidContext.value.filesDir
    val temp = File(dir, "widget.tmp.json")
    temp.writeText(json)
    if (!temp.renameTo(File(dir, "widget.json"))) error("could not move widget.json into place")
}

/** The only thing a widget is allowed to read, for the widget side to read it. */
fun readWidgetState(): WidgetState? {
    val dir = Storage.rootOverride ?: AndroidContext.value.filesDir
    val text = runCatching { File(dir, "widget.json").readText() }.getOrNull() ?: return null
    return runCatching { WidgetJson.decodeFromString(WidgetState.serializer(), text) }.getOrNull()
}

/** Asked of every widget receiver that exists. They arrive in #21 and #22. */
actual fun refreshWidgets() {
    WidgetRefresh.all.forEach { it() }
}

/** Each Glance widget registers how to update itself, so data/ never imports widget/. */
object WidgetRefresh {
    val all = mutableListOf<() -> Unit>()
}
