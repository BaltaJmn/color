import SwiftUI
import UserNotifications
import WidgetKit
import Shared

/** Only for the notification centre, whose delegate has to be set before launch finishes. */
final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    // The daily nudge asks about today's color: tapping it opens Today, wherever the app was left.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        if response.notification.request.identifier.hasPrefix("reminder-") {
            ChromaBridge.shared.open(url: "com.baltajmn.color://today")
        }
        completionHandler()
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.colorScheme) private var colorScheme

    init() {
        // WidgetCenter is Swift's; Kotlin writes widget.json and asks through here.
        ChromaBridge.shared.reloadWidgets = { WidgetCenter.shared.reloadAllTimelines() }
    }

    var body: some Scene {
        WindowGroup {
            // The system takes the task switcher picture before Compose could repaint, so the cover
            // is painted here, in the window Swift owns: the paper of the app, and nothing on it.
            ZStack {
                ContentView()
                if scenePhase != .active && ChromaBridge.shared.isLockOn() {
                    Color(colorScheme == .dark ? UIColor(red: 0.071, green: 0.071, blue: 0.071, alpha: 1)
                                               : UIColor(red: 0.953, green: 0.953, blue: 0.953, alpha: 1))
                        .ignoresSafeArea()
                }
            }
            .onOpenURL { ChromaBridge.shared.open(url: $0.absoluteString) }
        }
    }
}
