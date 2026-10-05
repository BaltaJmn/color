import Foundation
import SwiftUI

/// Exactly the shape of `widget.json` (docs/tecnico.md 4.2): colors and one translated name. There
/// is no model of the journal in Swift, and no photo, word or friend's name ever arrives.
struct ChromaState: Decodable {
    let date: String
    let color: String?
    let name: String?
    let pro: Bool
    let year: Int
    let days: [String: String]
    let friends: [String]
}

/// Reads the one file the extension is allowed to see, and nothing else.
enum ChromaStore {
    static let appGroup = "group.com.baltajmn.color"

    static func read() -> ChromaState? {
        guard let url = FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: appGroup)?
            .appendingPathComponent("widget.json"),
            let data = try? Data(contentsOf: url)
        else { return nil }
        return try? JSONDecoder().decode(ChromaState.self, from: data)
    }

    /// The same rule as `widgetView` in Kotlin: the widget may wake up days after the app last ran,
    /// and yesterday's color is not today's, nor last year's grid this year's.
    static func view(_ s: ChromaState, on day: String, year: Int) -> ChromaState {
        let sameDay = s.date == day
        return ChromaState(
            date: day,
            color: sameDay ? s.color : nil,
            name: sameDay ? s.name : nil,
            pro: s.pro,
            year: year,
            days: s.year == year ? s.days : [:],
            friends: sameDay ? s.friends : []
        )
    }

    static func entry(at instant: Date) -> ChromaState? {
        guard let state = read() else { return nil }
        let day = logicalDay(instant)
        return view(state, on: key(day), year: year(day))
    }

    // MARK: - The logical day

    private static var calendar: Calendar {
        var c = Calendar(identifier: .gregorian)
        c.timeZone = .current
        return c
    }

    /// The day ends at 03:00, so anything before that still belongs to the night before.
    static func logicalDay(_ instant: Date) -> Date {
        let c = calendar
        let start = c.startOfDay(for: instant)
        return c.component(.hour, from: instant) < 3 ? c.date(byAdding: .day, value: -1, to: start)! : start
    }

    static func nextCutoff(after instant: Date) -> Date {
        let c = calendar
        let todayAt3 = c.date(bySettingHour: 3, minute: 0, second: 0, of: instant)!
        return todayAt3 > instant ? todayAt3 : c.date(byAdding: .day, value: 1, to: todayAt3)!
    }

    static func key(_ day: Date) -> String {
        let f = DateFormatter()
        f.calendar = calendar
        f.timeZone = .current
        f.locale = Locale(identifier: "en_US_POSIX")
        f.dateFormat = "yyyy-MM-dd"
        return f.string(from: day)
    }

    static func year(_ day: Date) -> Int { calendar.component(.year, from: day) }

    // MARK: - Color, the same rules as Contrast.kt

    private static func rgb(_ hex: String) -> (Double, Double, Double) {
        let v = Int(hex.dropFirst(), radix: 16) ?? 0
        return (Double((v >> 16) & 0xFF) / 255, Double((v >> 8) & 0xFF) / 255, Double(v & 0xFF) / 255)
    }

    static func color(_ hex: String) -> Color {
        let (r, g, b) = rgb(hex)
        return Color(.sRGB, red: r, green: g, blue: b)
    }

    private static func luminance(_ hex: String) -> Double {
        func ch(_ c: Double) -> Double { c <= 0.04045 ? c / 12.92 : pow((c + 0.055) / 1.055, 2.4) }
        let (r, g, b) = rgb(hex)
        return 0.2126 * ch(r) + 0.7152 * ch(g) + 0.0722 * ch(b)
    }

    private static func contrast(_ a: Double, _ b: Double) -> Double { (max(a, b) + 0.05) / (min(a, b) + 0.05) }

    /// Pure white or pure black, whichever contrasts more: the same answer as `inkFor`.
    static func ink(_ hex: String) -> Color {
        let l = luminance(hex)
        return contrast(l, 1) >= contrast(l, 0) ? .white : .black
    }

    /// `blendsInto` of Contrast.kt against both backgrounds of the widget (#F1F1F1 and #0E0E0E), as
    /// the Android one does: the edge does not depend on the mode.
    static func blendsIntoBackground(_ hex: String) -> Bool {
        let l = luminance(hex)
        return [luminance("#F1F1F1"), luminance("#0E0E0E")].contains { contrast(l, $0) < 1.3 }
    }
}

/// The mirror of `Strings.kt` for the extension, which cannot reach Kotlin. Same thirteen languages.
enum L {
    private static var code: String {
        let tag = Locale.preferredLanguages.first ?? "en"
        return String(tag.prefix(2))
    }

    private static func t(
        _ en: String, _ es: String, _ pt: String, _ de: String, _ fr: String, _ it: String, _ nl: String,
        _ pl: String, _ ru: String, _ tr: String, _ id: String, _ ja: String, _ ko: String
    ) -> String {
        switch code {
        case "es": return es
        case "pt": return pt
        case "de": return de
        case "fr": return fr
        case "it": return it
        case "nl": return nl
        case "pl": return pl
        case "ru": return ru
        case "tr": return tr
        case "id": return id
        case "ja": return ja
        case "ko": return ko
        default: return en
        }
    }

    static var today: String {
        t(
            "Today", "Hoy", "Hoje", "Heute", "Aujourd'hui", "Oggi", "Vandaag", "Dziś", "Сегодня", "Bugün",
            "Hari ini", "今日", "오늘"
        )
    }
    static var empty: String {
        t(
            "No color yet", "Aún sin color", "Ainda sem cor", "Noch keine Farbe", "Pas encore de couleur",
            "Ancora nessun colore", "Nog geen kleur", "Jeszcze bez koloru", "Пока без цвета", "Henüz renk yok",
            "Belum ada warna", "まだ色がありません", "아직 색이 없어요"
        )
    }
    static var proTitle: String { "Chroma Pro" }
    static var unlock: String {
        t(
            "Tap to turn it on", "Toca para activarlo", "Toque para ativar", "Tippen zum Aktivieren",
            "Touche pour l'activer", "Tocca per attivarlo", "Tik om aan te zetten", "Dotknij, aby włączyć",
            "Нажми, чтобы включить", "Açmak için dokun", "Ketuk untuk mengaktifkan", "タップしてオンにする", "탭해서 켜기"
        )
    }
    static var yearLabel: String {
        t(
            "Your year in color", "Tu año en color", "Seu ano em cores", "Dein Jahr in Farben",
            "Ton année en couleurs", "Il tuo anno a colori", "Je jaar in kleur", "Twój rok w kolorze",
            "Твой год в цвете", "Renklerle yılın", "Tahunmu dalam warna", "色でつづる1年", "색으로 보는 한 해"
        )
    }
    static var friendsToday: String {
        t(
            "Colors your friends picked today", "Los colores que tus amigos eligieron hoy",
            "As cores que seus amigos escolheram hoje", "Die Farben, die deine Freunde heute gewählt haben",
            "Les couleurs choisies aujourd'hui par tes amis",
            "I colori scelti oggi dai tuoi amici",
            "De kleuren die je vrienden vandaag kozen",
            "Kolory, które twoi znajomi wybrali dziś",
            "Цвета, которые сегодня выбрали твои друзья",
            "Arkadaşlarının bugün seçtiği renkler",
            "Warna yang dipilih temanmu hari ini",
            "友だちが今日選んだ色",
            "친구들이 오늘 고른 색"
        )
    }
}
