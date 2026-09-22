package com.baltajmn.color.data

/**
 * The only channel to the widgets. They read widget.json and nothing else, so wherever the journal
 * lives the widgets cannot reach it, on either platform.
 */
expect fun writeWidgetState(json: String)

/** Tells the system the state changed. Both platforms redraw on their own schedule otherwise. */
expect fun refreshWidgets()

fun syncWidgets(state: WidgetState) {
    runCatching {
        writeWidgetState(WidgetJson.encodeToString(WidgetState.serializer(), state))
        refreshWidgets()
    }
}
