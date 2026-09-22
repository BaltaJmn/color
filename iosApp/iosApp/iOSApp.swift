import SwiftUI
import WidgetKit
import Shared

@main
struct iOSApp: App {
    init() {
        // WidgetCenter is Swift's; Kotlin writes widget.json and asks through here.
        ChromaBridge.shared.reloadWidgets = { WidgetCenter.shared.reloadAllTimelines() }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { ChromaBridge.shared.open(url: $0.absoluteString) }
        }
    }
}
