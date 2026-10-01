package com.baltajmn.color.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.baltajmn.color.i18n.S
import com.baltajmn.color.shared.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal const val CHANNEL_ID = "color-daily"
internal const val REMINDER_NOTIFICATION_ID = 1

/** Fires the daily nudge unless the day already has its color, then books the next one. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ChromaRepository.ensureLoaded()
        // The next one is booked whatever happens to this one, or the reminders stop for good.
        try {
            if (ChromaRepository.entryOn(today()) == null) notify(context)
        } finally {
            Reminder.sync(askPermission = false)
        }
    }

    private fun notify(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, S.reminderChannel, NotificationManager.IMPORTANCE_DEFAULT),
        )

        // To Today, wherever the app was left: the nudge asks about today's color and nothing else.
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.putExtra("screen", "today")
        val tap = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(S.reminderTitle)
            .setContentText(S.reminderText)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
    }
}

/**
 * Alarms do not survive a reboot, a reinstall or a change of clock, so book them again. A new clock
 * or time zone may also be a new day for the widgets.
 */
class BootReceiver : BroadcastReceiver() {
    // Exported, as the system broadcasts require: anything else knocking is ignored.
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in SYSTEM_ACTIONS) turnThePage()
    }
}

private val SYSTEM_ACTIONS = setOf(
    Intent.ACTION_BOOT_COMPLETED,
    Intent.ACTION_MY_PACKAGE_REPLACED,
    Intent.ACTION_TIME_CHANGED,
    Intent.ACTION_TIMEZONE_CHANGED,
)

/** 03:00: the widgets still show yesterday until someone repaints them. */
class DayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = turnThePage()
}

// goAsync: the widgets redraw on a coroutine, and a receiver that returns may be killed before. The
// system allows about ten seconds, and finish() is called whatever happened.
private fun BroadcastReceiver.turnThePage() {
    val pending = goAsync()
    try {
        ChromaRepository.ensureLoaded()
        ChromaRepository.syncWidgets()
        Reminder.sync(askPermission = false)
    } finally {
        CoroutineScope(Dispatchers.Default).launch {
            withTimeoutOrNull(8_000) { lastRefresh?.join() }
            pending.finish()
        }
    }
}
