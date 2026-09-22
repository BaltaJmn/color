package com.baltajmn.color.data

import com.baltajmn.color.model.Journal
import com.baltajmn.color.model.Settings
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Everything the widgets are allowed to know, and nothing else: colors and one translated color
 * name. No photo, no word, no friend's name ever crosses this file, so a widget cannot leak them
 * onto the home screen.
 */
@Serializable
data class WidgetState(
    /** Logical date of the day this state was built for. */
    val date: String,
    /** Today's color, or null while today has none. */
    val color: String? = null,
    /** Today's color name, already translated: the widget does not carry the name table. */
    val name: String? = null,
    val pro: Boolean = false,
    val year: Int,
    /** Local ISO date to color, for the year widget. Empty without Pro: the widget paints it locked. */
    val days: Map<String, String> = emptyMap(),
    /** Friends' colors today, from v1.2. Colors only. */
    val friends: List<String> = emptyList(),
)

/** Swift decodes every field, so none of them may be missing. */
@OptIn(ExperimentalSerializationApi::class)
val WidgetJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    explicitNulls = true
}

fun widgetState(
    j: Journal,
    s: Settings,
    today: LocalDate,
    nameOf: (String) -> String,
    friends: List<String> = emptyList(),
): WidgetState {
    val entry = j[today.toString()]
    return WidgetState(
        date = today.toString(),
        color = entry?.color,
        name = entry?.let { nameOf(it.name) },
        pro = s.pro,
        year = today.year,
        days = if (s.pro) j.filterKeys { it.startsWith("${today.year}-") }.mapValues { it.value.color } else emptyMap(),
        friends = friends,
    )
}

/**
 * What the widget should paint right now, without the app having run. A widget that wakes up after
 * 03:00 holds yesterday's state, and yesterday's color is not today's, nor last year's grid this year's.
 */
fun widgetView(st: WidgetState, today: LocalDate): WidgetState {
    val sameDay = st.date == today.toString()
    return st.copy(
        date = today.toString(),
        color = st.color.takeIf { sameDay },
        name = st.name.takeIf { sameDay },
        friends = if (sameDay) st.friends else emptyList(),
        year = today.year,
        days = if (st.year == today.year) st.days else emptyMap(),
    )
}
