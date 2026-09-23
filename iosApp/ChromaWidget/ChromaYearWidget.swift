import SwiftUI
import WidgetKit

struct YearEntry: TimelineEntry {
    let date: Date
    let state: ChromaState?
}

struct YearProvider: TimelineProvider {
    func placeholder(in context: Context) -> YearEntry { YearEntry(date: .now, state: nil) }

    func getSnapshot(in context: Context, completion: @escaping (YearEntry) -> Void) {
        completion(YearEntry(date: .now, state: ChromaStore.entry(at: .now)))
    }

    /// Now and the next 03:00, like the today widget: the grid gains a day at the same cutoff.
    func getTimeline(in context: Context, completion: @escaping (Timeline<YearEntry>) -> Void) {
        let cutoff = ChromaStore.nextCutoff(after: .now)
        let entries = [Date.now, cutoff].map { YearEntry(date: $0, state: ChromaStore.entry(at: $0)) }
        completion(Timeline(entries: entries, policy: .after(cutoff)))
    }
}

/// Twelve rows of thirty-one: the widget is wider than it is tall, so My year lies on its side.
private struct YearGrid: View {
    let days: [String: String]
    let year: Int
    let today: String

    var body: some View {
        Canvas { context, size in
            let gap: CGFloat = 2
            let side = min((size.width - 30 * gap) / 31, (size.height - 11 * gap) / 12)
            let step = side + gap
            let left = (size.width - (step * 31 - gap)) / 2
            let top = (size.height - (step * 12 - gap)) / 2
            var calendar = Calendar(identifier: .gregorian)
            calendar.timeZone = .current

            for month in 1...12 {
                for day in 1...31 {
                    var parts = DateComponents()
                    parts.year = year
                    parts.month = month
                    parts.day = day
                    // A day the month does not have comes back as another month: skip it.
                    guard let date = calendar.date(from: parts), calendar.component(.day, from: date) == day else { continue }
                    let key = String(format: "%04d-%02d-%02d", year, month, day)
                    let box = CGRect(x: left + step * CGFloat(day - 1), y: top + step * CGFloat(month - 1), width: side, height: side)
                    let cell = Path(roundedRect: box, cornerRadius: side / 5)
                    if let hex = days[key] {
                        context.fill(cell, with: .color(ChromaStore.color(hex)))
                    } else {
                        // The days still to come are fainter, so the year reads as far as it has got.
                        context.fill(cell, with: .color(Color("WidgetEmpty").opacity(key > today ? 0.45 : 1)))
                    }
                }
            }
        }
    }
}

private struct YearView: View {
    let entry: YearEntry

    private var pro: Bool { entry.state?.pro == true }
    private var day: Date { ChromaStore.logicalDay(entry.date) }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(String(ChromaStore.year(day))).font(.system(size: 13, weight: .medium))
            ZStack {
                YearGrid(days: pro ? entry.state?.days ?? [:] : [:], year: ChromaStore.year(day), today: ChromaStore.key(day))
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel(Text(L.yearLabel))
                if !pro {
                    VStack(spacing: 2) {
                        Text(L.proTitle).font(.system(size: 13, weight: .medium))
                        Text(L.unlock).font(.system(size: 13)).foregroundStyle(Color("WidgetMuted"))
                    }
                }
            }
        }
        // A locked grid that opened My year would explain nothing: it sells Pro instead.
        .widgetURL(URL(string: pro ? "com.baltajmn.color://year" : "com.baltajmn.color://pro"))
        .containerBackground(for: .widget) { Color("WidgetBackground") }
    }
}

struct ChromaYearWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "ChromaYearWidget", provider: YearProvider()) { entry in
            YearView(entry: entry)
        }
        // Literals, not L: the gallery reads them at build time; translations in <lang>.lproj.
        .configurationDisplayName("The year")
        .description("Your year in colors")
        .supportedFamilies([.systemMedium])
    }
}
