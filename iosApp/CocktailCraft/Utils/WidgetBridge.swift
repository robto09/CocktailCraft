import Foundation

// Compiled into BOTH the app target and the widget extension (see
// project.yml) so the deep-link format, App Group id, snapshot schema, and
// price formatting have exactly one definition. Keep this file
// Foundation-only.

/// WidgetKit kind strings, shared so the app can reload a specific widget's
/// timelines without stringly-typed drift.
enum WidgetKind {
    static let favorites = "CocktailCraftFavoritesWidget"
    static let randomCocktail = "CocktailCraftRandomWidget"
}

/// Deep-link format shared by the widgets (writers) and the app (reader):
/// cocktailcraft://cocktail/{id}
enum WidgetDeepLink {
    static let scheme = "cocktailcraft"
    static let cocktailHost = "cocktail"

    static func cocktailURL(id: String) -> URL? {
        var components = URLComponents()
        components.scheme = scheme
        components.host = cocktailHost
        components.path = "/" + id
        return components.url
    }

    static func cocktailId(from url: URL) -> String? {
        guard url.scheme == scheme, url.host == cocktailHost else { return nil }
        let id = url.lastPathComponent
        return id.isEmpty || id == "/" ? nil : id
    }
}

/// Minimal cocktail projection the widgets render — decoupled from the KMP
/// `Cocktail` model on purpose: the widget extension does not link the
/// shared framework.
struct WidgetCocktail: Codable, Identifiable {
    let id: String
    let name: String
    let imageUrl: String?
    let price: Double
}

struct WidgetFavoritesSnapshot: Codable {
    let favorites: [WidgetCocktail]
    let updatedAt: Date
}

/// Reads/writes the favorites snapshot in the shared App Group container.
enum WidgetSnapshotStore {
    static let appGroupId = "group.com.cocktailcraft"
    private static let filename = "widget-favorites-snapshot.json"

    private static var fileURL: URL? {
        FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: appGroupId)?
            .appendingPathComponent(filename)
    }

    static func write(_ snapshot: WidgetFavoritesSnapshot) {
        guard let url = fileURL else { return }
        do {
            let data = try JSONEncoder().encode(snapshot)
            try data.write(to: url, options: .atomic)
        } catch {
            #if DEBUG
            print("WidgetSnapshotStore: write failed: \(error)")
            #endif
        }
    }

    static func read() -> WidgetFavoritesSnapshot? {
        guard let url = fileURL,
              let data = try? Data(contentsOf: url) else { return nil }
        return try? JSONDecoder().decode(WidgetFavoritesSnapshot.self, from: data)
    }
}

extension Double {
    /// Canonical price string, shared by the app and the widget extension.
    /// Prices are demo USD values, so the currency is fixed to USD while the
    /// *rendering* follows the device locale — en_US shows "$12.50", fr_FR
    /// shows "12,50 $US" — instead of hard-pinning an en_US string.
    var asPrice: String {
        Double.priceFormatter.string(from: NSNumber(value: self)) ?? "$" + String(format: "%.2f", self)
    }

    private static let priceFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = "USD"
        formatter.locale = Locale.current
        return formatter
    }()
}
