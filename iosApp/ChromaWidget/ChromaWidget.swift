import WidgetKit
import SwiftUI

// Placeholder until the widgets land (#21, #22): the target has to build from the first commit.
struct PlaceholderEntry: TimelineEntry {
    let date: Date
}

struct PlaceholderProvider: TimelineProvider {
    func placeholder(in context: Context) -> PlaceholderEntry { PlaceholderEntry(date: .now) }
    func getSnapshot(in context: Context, completion: @escaping (PlaceholderEntry) -> Void) {
        completion(PlaceholderEntry(date: .now))
    }
    func getTimeline(in context: Context, completion: @escaping (Timeline<PlaceholderEntry>) -> Void) {
        completion(Timeline(entries: [PlaceholderEntry(date: .now)], policy: .never))
    }
}

struct ChromaTodayWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "ChromaTodayWidget", provider: PlaceholderProvider()) { _ in
            Text("Chroma").containerBackground(.background, for: .widget)
        }
        .supportedFamilies([.systemSmall])
    }
}

@main
struct ChromaWidgets: WidgetBundle {
    var body: some Widget {
        ChromaTodayWidget()
    }
}
