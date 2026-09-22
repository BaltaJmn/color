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

private const val CHANNEL_ID = "color-daily"
private const val NOTIFICATION_ID = 1

/** Fires the daily nudge unless the day already has its color, then books the next one. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AndroidContext.init(context)
        ChromaRepository.load()
        if (ChromaRepository.entryOn(today()) == null) notify(context)
        Reminder.sync(askPermission = false)
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

        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val tap = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(S.reminderTitle)
            .setContentText(S.reminderText)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}

/** Alarms do not survive a reboot, a reinstall or a change of clock, so book it again. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AndroidContext.init(context)
        ChromaRepository.load()
        Reminder.sync(askPermission = false)
    }
}
