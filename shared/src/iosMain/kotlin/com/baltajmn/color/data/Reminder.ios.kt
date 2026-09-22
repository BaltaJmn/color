package com.baltajmn.color.data

import kotlinx.datetime.number
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

const val REMINDER_ID_PREFIX = "reminder-"

/**
 * iOS cannot be told at fire time that the day already has its color, so nothing repeats: the whole
 * window is booked as single requests and rebuilt on every start and every save. Sixty of them are
 * well inside the limit of 64 pending requests.
 */
actual object Reminder {

    actual fun sync(askPermission: Boolean) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        if (askPermission) {
            center.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound,
            ) { _, _ -> reschedule(center) }
        } else {
            reschedule(center)
        }
    }

    private fun reschedule(center: UNUserNotificationCenter) {
        center.getPendingNotificationRequestsWithCompletionHandler { pending ->
            val mine = pending.orEmpty()
                .filterIsInstance<UNNotificationRequest>()
                .map { it.identifier }
                .filter { it.startsWith(REMINDER_ID_PREFIX) }
            if (mine.isNotEmpty()) center.removePendingNotificationRequestsWithIdentifiers(mine)

            val now = nowLocal()
            reminderPlan(ChromaRepository.journal, ChromaRepository.settings, now).forEach { planned ->
                val content = UNMutableNotificationContent().apply {
                    setTitle(planned.title)
                    setBody(planned.body)
                    setSound(UNNotificationSound.defaultSound)
                }
                val at = NSDateComponents().apply {
                    year = planned.at.year.toLong()
                    month = planned.at.month.number.toLong()
                    day = planned.at.day.toLong()
                    hour = planned.at.hour.toLong()
                    minute = planned.at.minute.toLong()
                }
                center.addNotificationRequest(
                    UNNotificationRequest.requestWithIdentifier(
                        planned.id,
                        content,
                        UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(at, repeats = false),
                    ),
                    null,
                )
            }
        }
    }
}
