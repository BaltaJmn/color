package com.baltajmn.color.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.color.model.isoKey
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import platform.Foundation.NSDateComponents
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

const val REMINDER_ID_PREFIX = "reminder-"

/**
 * iOS cannot be told at fire time that the day already has its color, so nothing repeats: the whole
 * window is booked as single requests and rebuilt on every start and every save. Sixty of them are
 * well inside the limit of 64 pending requests.
 */
actual object Reminder {

    private var off by mutableStateOf(false)
    actual val blocked: Boolean get() = off

    // Never asked on this phone: the settings page has no Notifications section yet, only the question.
    private var neverAsked = false

    @OptIn(ExperimentalTime::class)
    actual fun sync(askPermission: Boolean) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val on = ChromaRepository.settings.reminderOn
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            val unasked = status == UNAuthorizationStatusNotDetermined
            // A journal restored on a new iPhone brings the reminder on and not the permission: iOS
            // drops every request until someone asks. Unless the question is on screen right now.
            val denied = status == UNAuthorizationStatusDenied || (unasked && on && !askPermission)
            dispatch_async(dispatch_get_main_queue()) {
                off = denied
                neverAsked = unasked
            }
        }
        // A nudge still in Notification Centre once the color is picked, or from a day already over,
        // asks for something nobody has to do any more.
        val picked = today().isoKey() in ChromaRepository.journal
        val began = dayStart(today()).toInstant(TimeZone.currentSystemDefault()).epochSeconds.toDouble()
        center.getDeliveredNotificationsWithCompletionHandler { delivered ->
            val stale = delivered.orEmpty()
                .filterIsInstance<UNNotification>()
                .filter { it.request.identifier.startsWith(REMINDER_ID_PREFIX) && (picked || it.date.timeIntervalSince1970 < began) }
                .map { it.request.identifier }
            if (stale.isNotEmpty()) center.removeDeliveredNotificationsWithIdentifiers(stale)
        }
        if (askPermission) {
            center.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound,
            ) { granted, _ ->
                // The answer, at once: no trip to the background is needed to see it in Settings.
                dispatch_async(dispatch_get_main_queue()) {
                    off = !granted
                    neverAsked = false
                }
                reschedule(center)
            }
        } else {
            reschedule(center)
        }
    }

    actual fun unblock() {
        if (neverAsked) sync(askPermission = true) else AppInfo.openSettings()
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
