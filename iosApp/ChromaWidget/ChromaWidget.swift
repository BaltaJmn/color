import SwiftUI
import WidgetKit

struct TodayEntry: TimelineEntry {
    let date: Date
    let state: ChromaState?
}

struct TodayProvider: TimelineProvider {
    func placeholder(in context: Context) -> TodayEntry { TodayEntry(date: .now, state: nil) }

    func getSnapshot(in context: Context, completion: @escaping (TodayEntry) -> Void) {
        completion(TodayEntry(date: .now, state: ChromaStore.entry(at: .now)))
    }

    /// Now and the next 03:00, each already seen through the logical day it belongs to, and then
    /// ask to be woken at that cutoff. A widget must be right on its own overnight.
    func getTimeline(in context: Context, completion: @escaping (Timeline<TodayEntry>) -> Void) {
        let cutoff = ChromaStore.nextCutoff(after: .now)
        let entries = [Date.now, cutoff].map { TodayEntry(date: $0, state: ChromaStore.entry(at: $0)) }
        completion(Timeline(entries: entries, policy: .after(cutoff)))
    }
}

/// Free. Today's color edge to edge with its name, or a quiet invitation. Never the photo: the
/// home screen is seen by whoever is next to you.
private struct TodayView: View {
    let entry: TodayEntry

    var body: some View {
        content
            .widgetURL(URL(string: "com.baltajmn.color://today"))
            .containerBackground(for: .widget) { background }
    }

    @ViewBuilder private var background: some View {
        if let hex = entry.state?.color { ChromaStore.color(hex) } else { Color("WidgetEmpty") }
    }

    @ViewBuilder private var content: some View {
        if let hex = entry.state?.color, let name = entry.state?.name {
            VStack(alignment: .leading, spacing: 2) {
                Spacer()
                Text(L.today).font(.system(size: 13))
                Text(name).font(.system(size: 17, weight: .medium)).lineLimit(2)
                FriendsStrip(colors: entry.state?.friends ?? [])
            }
            .foregroundStyle(ChromaStore.ink(hex))
            .frame(maxWidth: .infinity, alignment: .leading)
        } else {
            VStack(spacing: 0) {
                Spacer()
                Text(L.empty)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(Color("WidgetMuted"))
                    .multilineTextAlignment(.center)
                Spacer()
                FriendsStrip(colors: entry.state?.friends ?? [])
            }
        }
    }
}

/// #42: a thin strip with the circle's colors today, in order of the hour. Colors only, so the home
/// screen says how your people's day went without naming anyone. Nothing at all without friends.
private struct FriendsStrip: View {
    let colors: [String]

    var body: some View {
        if !colors.isEmpty {
            HStack(spacing: 0) {
                ForEach(Array(colors.enumerated()), id: \.offset) { _, hex in ChromaStore.color(hex) }
            }
            .frame(height: 8)
            .clipShape(RoundedRectangle(cornerRadius: 4))
            .padding(.top, 8)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(Text(L.friendsToday))
        }
    }
}

struct ChromaTodayWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "ChromaTodayWidget", provider: TodayProvider()) { entry in
            TodayView(entry: entry)
        }
        // Literals, not L: the gallery reads them at build time; translations in <lang>.lproj.
        .configurationDisplayName("Today")
        .description("Today's color")
        .supportedFamilies([.systemSmall])
    }
}

@main
struct ChromaWidgets: WidgetBundle {
    var body: some Widget {
        ChromaTodayWidget()
        ChromaYearWidget()
    }
}
