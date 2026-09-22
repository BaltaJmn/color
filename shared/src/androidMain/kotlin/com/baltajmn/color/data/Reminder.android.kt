package com.baltajmn.color.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.baltajmn.color.model.isoKey
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

private const val REQUEST_CODE = 7001

actual object Reminder {

    /** Set by MainActivity: asking for POST_NOTIFICATIONS needs an Activity. */
    var onNeedsPermission: (() -> Unit)? = null

    @OptIn(ExperimentalTime::class)
    actual fun sync(askPermission: Boolean) {
        val context = AndroidContext.value
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
            at.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            pendingIntent(context),
        )
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
