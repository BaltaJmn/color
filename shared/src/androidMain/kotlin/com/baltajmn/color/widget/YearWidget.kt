package com.baltajmn.color.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.baltajmn.color.color.rgbOf
import com.baltajmn.color.data.WidgetState
import com.baltajmn.color.data.readWidgetState
import com.baltajmn.color.data.today
import com.baltajmn.color.data.widgetView
import com.baltajmn.color.i18n.S
import com.baltajmn.color.model.isoKey
import com.baltajmn.color.ui.theme.Dark
import com.baltajmn.color.ui.theme.Light
import kotlin.math.min
import kotlinx.datetime.LocalDate

class YearWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = YearWidget()
}

/** Pro. The year in colors, and nothing else: widget.json has no photo and no word to give it. */
class YearWidget : GlanceAppWidget() {

    // Exact and not Responsive: the grid is a bitmap and it is painted for the size it gets.
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = readWidgetState()?.let { widgetView(it, today()) }
        provideContent { Year(state) }
    }
}

@Composable
private fun Year(state: WidgetState?) {
    val context = LocalContext.current
    val day = state?.date?.let(LocalDate::parse) ?: today()
    val pro = state?.pro == true
    val night = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
        Configuration.UI_MODE_NIGHT_YES
    val scheme = if (night) Dark else Light
    // Without Pro it opens the paywall: a locked grid that opened My year would explain nothing.
    val open = Intent()
        .setComponent(ComponentName(context.packageName, "com.baltajmn.color.MainActivity"))
        .putExtra("screen", if (pro) "year" else "pro")

    val density = context.resources.displayMetrics.density
    val size = LocalSize.current
    val grid = yearBitmap(
        width = ((size.width.value - 28) * density).toInt(),
        height = ((size.height.value - 28 - 22) * density).toInt(),
        days = if (pro) state?.days.orEmpty() else emptyMap(),
        today = day,
        empty = scheme.surfaceVariant.toArgb(),
    )

    Column(
        GlanceModifier.fillMaxSize().appWidgetBackground()
            .cornerRadius(android.R.dimen.system_app_widget_background_radius)
            .background(scheme.background).padding(14.dp)
            .clickable(actionStartActivity(open)),
    ) {
        Text(
            day.year.toString(),
            style = TextStyle(color = ColorProvider(scheme.onBackground), fontSize = 13.sp, fontWeight = FontWeight.Medium),
        )
        Spacer(GlanceModifier.height(8.dp))
        Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(ImageProvider(grid), contentDescription = null)
            if (!pro) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(S.proTitle, style = TextStyle(color = ColorProvider(scheme.onBackground), fontSize = 13.sp, fontWeight = FontWeight.Medium))
                    Text(S.widgetUnlock, style = TextStyle(color = ColorProvider(scheme.onSurfaceVariant), fontSize = 12.sp))
                }
            }
        }
    }
}

private const val MONTHS = 12
private const val DAYS = 31

/**
 * Twelve rows of thirty-one: the widget is wider than it is tall, so the grid of My year lies on
 * its side. The cells are square and the bitmap is only as big as the grid, so a tall widget
 * centres it and pays nothing for the space. Empty [days] paints it all blank, which is the lock.
 */
private fun yearBitmap(width: Int, height: Int, days: Map<String, String>, today: LocalDate, empty: Int): Bitmap {
    val gap = 2f
    val side = min((width.coerceAtLeast(DAYS) - (DAYS - 1) * gap) / DAYS, (height.coerceAtLeast(MONTHS) - (MONTHS - 1) * gap) / MONTHS)
    val step = side + gap
    val bitmap = Bitmap.createBitmap(
        (step * DAYS - gap).toInt().coerceAtLeast(DAYS),
        (step * MONTHS - gap).toInt().coerceAtLeast(MONTHS),
        Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(bitmap)
    val radius = side / 5
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    for (month in 1..MONTHS) {
        for (day in 1..DAYS) {
            val date = runCatching { LocalDate(today.year, month, day) }.getOrNull() ?: continue
            val x = step * (day - 1)
            val y = step * (month - 1)
            paint.color = days[date.isoKey()]?.let(::rgbOf) ?: empty
            // The days still to come are fainter, so the year reads as far as it has got.
            paint.alpha = if (date > today && date.isoKey() !in days) 110 else 255
            canvas.drawRoundRect(RectF(x, y, x + side, y + side), radius, radius, paint)
        }
    }
    return bitmap
}
