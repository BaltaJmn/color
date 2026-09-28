package com.baltajmn.color.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.baltajmn.color.color.colorOf
import com.baltajmn.color.color.inkColorFor
import com.baltajmn.color.data.WidgetState
import com.baltajmn.color.data.readWidgetState
import com.baltajmn.color.data.today
import com.baltajmn.color.data.widgetView
import com.baltajmn.color.i18n.S
import com.baltajmn.color.ui.theme.Dark
import com.baltajmn.color.ui.theme.Light
import kotlinx.datetime.LocalDate

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TodayWidget()
}

/**
 * Free. Today's color edge to edge with its name, or a quiet invitation. Never the photo: the home
 * screen is seen by whoever is next to you, and widget.json does not carry it anyway.
 */
class TodayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // The file may be days old if the app has not run: widgetView is what makes it right anyway.
        val state = readWidgetState()?.let { widgetView(it, today()) }
        provideContent { Today(state) }
    }
}

@Composable
private fun Today(state: WidgetState?) {
    val context = LocalContext.current
    // The widget lives in the app's own package, which is the only Activity it may name.
    val open = Intent()
        .setComponent(ComponentName(context.packageName, "com.baltajmn.color.MainActivity"))
        .putExtra("screen", "today")
    // appWidgetBackground lets Android 12 and later clip the color to the launcher's own corners.
    val frame = GlanceModifier.fillMaxSize().appWidgetBackground()
        .cornerRadius(android.R.dimen.system_app_widget_background_radius)
        .clickable(actionStartActivity(open))
    val hex = state?.color
    val name = state?.name
    val day = state?.date?.let(LocalDate::parse) ?: today()

    // The same reading as the app: the day as a big light numeral on top, the color's name and its
    // code at the foot, like the card and the blank chip before it has a color.
    if (hex != null && name != null) {
        val ink = ColorProvider(inkColorFor(hex))
        Column(frame.background(colorOf(hex)).padding(14.dp)) {
            Text(day.day.toString(), style = TextStyle(color = ink, fontSize = 30.sp, fontWeight = FontWeight.Normal))
            Spacer(GlanceModifier.defaultWeight())
            Text(name, maxLines = 2, style = TextStyle(color = ink, fontSize = 17.sp, fontWeight = FontWeight.Bold))
            Text(hex, style = TextStyle(color = ink, fontSize = 12.sp, fontWeight = FontWeight.Medium))
            FriendsStrip(state.friends)
        }
    } else {
        // Glance 1.1.1 has no day/night ColorProvider, so the scheme is read from the host.
        val night = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        val scheme = if (night) Dark else Light
        Column(frame.background(scheme.surfaceVariant).padding(14.dp)) {
            Text(day.day.toString(), style = TextStyle(color = ColorProvider(scheme.onBackground), fontSize = 30.sp, fontWeight = FontWeight.Normal))
            Spacer(GlanceModifier.defaultWeight())
            Text(
                S.widgetEmpty,
                maxLines = 2,
                style = TextStyle(color = ColorProvider(scheme.onBackground), fontSize = 14.sp, fontWeight = FontWeight.Medium),
            )
            Text("#------", style = TextStyle(color = ColorProvider(scheme.onSurfaceVariant), fontSize = 12.sp))
            state?.let { FriendsStrip(it.friends) }
        }
    }
}

/**
 * #42: a thin strip with the circle's colors today, in order of the hour. Colors only, so the home
 * screen says how your people's day went without naming anyone. Nothing at all without friends.
 */
@Composable
private fun FriendsStrip(colors: List<String>) {
    if (colors.isEmpty()) return
    Spacer(GlanceModifier.height(8.dp))
    Row(
        GlanceModifier.fillMaxWidth().height(8.dp).cornerRadius(4.dp)
            .semantics { contentDescription = S.a11yFriendsToday },
    ) {
        colors.forEach { Box(GlanceModifier.defaultWeight().fillMaxHeight().background(colorOf(it))) {} }
    }
}
