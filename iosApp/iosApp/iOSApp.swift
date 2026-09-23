import SwiftUI
import WidgetKit
import Shared

@main
struct iOSApp: App {
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
                    Color(colorScheme == .dark ? UIColor(red: 0.078, green: 0.075, blue: 0.071, alpha: 1)
                                               : UIColor(red: 0.965, green: 0.957, blue: 0.945, alpha: 1))
                        .ignoresSafeArea()
                }
            }
            .onOpenURL { ChromaBridge.shared.open(url: $0.absoluteString) }
        }
    }
}
