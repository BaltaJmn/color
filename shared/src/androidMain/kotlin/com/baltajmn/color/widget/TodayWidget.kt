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
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
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

    if (hex != null && name != null) {
        val ink = ColorProvider(inkColorFor(hex))
        Column(frame.background(colorOf(hex)).padding(14.dp), verticalAlignment = Alignment.Bottom) {
            Text(S.widgetTodayName, style = TextStyle(color = ink, fontSize = 12.sp))
            Text(name, maxLines = 2, style = TextStyle(color = ink, fontSize = 17.sp, fontWeight = FontWeight.Medium))
        }
    } else {
        // Glance 1.1.1 has no day/night ColorProvider, so the scheme is read from the host.
        val night = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        val scheme = if (night) Dark else Light
        Column(
            frame.background(scheme.surfaceVariant).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                S.widgetEmpty,
                maxLines = 2,
                style = TextStyle(
                    color = ColorProvider(scheme.onSurfaceVariant),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
