import WidgetKit
import SwiftUI

/// Home-screen list of favorite cocktails, fed by the snapshot the app
/// writes into the App Group container (WidgetSnapshotStore). Each row
/// deep-links to its cocktail's detail screen.
struct FavoritesWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: WidgetKind.favorites, provider: FavoritesProvider()) { entry in
            FavoritesWidgetView(entry: entry)
                .containerBackground(.background, for: .widget)
        }
        .configurationDisplayName(String(localized: "Favorite Cocktails"))
        .description(String(localized: "Your favorite cocktails at a glance."))
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}

struct FavoritesEntry: TimelineEntry {
    let date: Date
    let favorites: [WidgetCocktail]
}

struct FavoritesProvider: TimelineProvider {
    func placeholder(in context: Context) -> FavoritesEntry {
        FavoritesEntry(date: Date(), favorites: FavoritesProvider.sampleFavorites)
    }

    func getSnapshot(in context: Context, completion: @escaping (FavoritesEntry) -> Void) {
        if context.isPreview {
            completion(placeholder(in: context))
        } else {
            completion(currentEntry())
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<FavoritesEntry>) -> Void) {
        // The app reloads timelines whenever it rewrites the snapshot, so
        // the widget itself never needs to poll.
        completion(Timeline(entries: [currentEntry()], policy: .never))
    }

    private func currentEntry() -> FavoritesEntry {
        let snapshot = WidgetSnapshotStore.read()
        return FavoritesEntry(date: Date(), favorites: snapshot?.favorites ?? [])
    }

    static let sampleFavorites = [
        WidgetCocktail(id: "11007", name: "Margarita", imageUrl: nil, price: 12.5),
        WidgetCocktail(id: "11000", name: "Mojito", imageUrl: nil, price: 11.0),
        WidgetCocktail(id: "11001", name: "Old Fashioned", imageUrl: nil, price: 14.0),
    ]
}

struct FavoritesWidgetView: View {
    let entry: FavoritesEntry
    @Environment(\.widgetFamily) private var family

    private var maxRows: Int {
        family == .systemLarge ? 6 : 3
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 4) {
                Image(systemName: "heart.fill")
                    .font(.caption)
                    .foregroundStyle(WidgetColors.primary)
                Text(String(localized: "Favorites"))
                    .font(.headline)
                Spacer()
            }

            if entry.favorites.isEmpty {
                Spacer()
                VStack(spacing: 4) {
                    Image(systemName: "heart")
                        .font(.title3)
                        .foregroundStyle(.secondary)
                    Text(String(localized: "No favorites yet"))
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    Text(String(localized: "Open CocktailCraft to add some"))
                        .font(.caption2)
                        .foregroundStyle(.tertiary)
                }
                .frame(maxWidth: .infinity)
                Spacer()
            } else {
                ForEach(entry.favorites.prefix(maxRows)) { cocktail in
                    if let url = WidgetDeepLink.cocktailURL(id: cocktail.id) {
                        Link(destination: url) {
                            FavoriteRow(cocktail: cocktail)
                        }
                    } else {
                        FavoriteRow(cocktail: cocktail)
                    }
                }
                Spacer(minLength: 0)
            }
        }
    }
}

private struct FavoriteRow: View {
    let cocktail: WidgetCocktail

    var body: some View {
        HStack(spacing: 8) {
            Circle()
                .fill(WidgetColors.primary.opacity(0.15))
                .frame(width: 24, height: 24)
                .overlay(
                    Image(systemName: "wineglass")
                        .font(.caption2)
                        .foregroundStyle(WidgetColors.primary)
                )

            Text(cocktail.name)
                .font(.subheadline)
                .lineLimit(1)

            Spacer()

            Text(cocktail.price.asPrice)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
    }
}
