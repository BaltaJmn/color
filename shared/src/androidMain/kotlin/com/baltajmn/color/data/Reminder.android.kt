package com.baltajmn.color.data

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import com.baltajmn.color.model.isoKey
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

private const val REQUEST_CODE = 7001
private const val DAY_REQUEST_CODE = 7002

actual object Reminder {

    /** Set by MainActivity: asking for POST_NOTIFICATIONS needs an Activity. */
    var onNeedsPermission: (() -> Unit)? = null

    private var off by mutableStateOf(false)
    actual val blocked: Boolean get() = off

    @OptIn(ExperimentalTime::class)
    actual fun sync(askPermission: Boolean) {
        val context = AndroidContext.value
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val zone = TimeZone.currentSystemDefault()
        // The widgets turn the page at 03:00 whether or not anyone opens the app or wants a nudge.
        manager.setAndAllowWhileIdle(
            AlarmManager.RTC,
            nextDayStart(nowLocal()).toInstant(zone).toEpochMilliseconds(),
            dayIntent(context),
        )
        val notifications = NotificationManagerCompat.from(context)
        // The nudge's own channel can be switched off from the notification itself, with the app's on.
        off = !notifications.areNotificationsEnabled() ||
            notifications.getNotificationChannel(CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE
        // A nudge still in the tray once the color is picked, or from a day already over, asks for
        // something nobody has to do any more.
        val picked = today().isoKey() in ChromaRepository.journal
        val began = dayStart(today()).toInstant(zone).toEpochMilliseconds()
        val tray = context.getSystemService(NotificationManager::class.java).activeNotifications
        if (tray.any { it.id == REMINDER_NOTIFICATION_ID && (picked || it.postTime < began) }) {
            notifications.cancel(REMINDER_NOTIFICATION_ID)
        }
        manager.cancel(pendingIntent(context))
        val settings = ChromaRepository.settings
        if (!settings.reminderOn) return

        if (askPermission) onNeedsPermission?.invoke()
        val at = nextFire(nowLocal(), settings.reminderHour, settings.reminderMinute) {
            it.isoKey() in ChromaRepository.journal
        }
        // Inexact on purpose: an exact alarm needs SCHEDULE_EXACT_ALARM, and a nudge to look around
        // can arrive a few minutes late without anyone noticing.
        manager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            at.toInstant(zone).toEpochMilliseconds(),
            pendingIntent(context),
        )
    }

    actual fun unblock() {
        val context = AndroidContext.value
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }.onFailure { AppInfo.openSettings() }
    }

    private fun dayIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        DAY_REQUEST_CODE,
        Intent(context, DayReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
