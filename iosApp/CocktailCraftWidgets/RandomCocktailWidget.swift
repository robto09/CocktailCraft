import WidgetKit
import SwiftUI

/// "Cocktail of the hour": rotates through the pool the app writes into the
/// App Group container (WidgetRandomSnapshotStore) — a fresh random pick
/// from the app's last online session, then recently viewed cocktails — and
/// deep-links to the shown cocktail's detail screen on tap. Like the
/// favorites widget it does no networking, so it keeps working offline.
/// Mirrors Android's RandomCocktailWidget (random pick, recently viewed as
/// the offline fallback).
struct RandomCocktailWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: WidgetKind.randomCocktail, provider: RandomCocktailProvider()) { entry in
            RandomCocktailWidgetView(entry: entry)
                .containerBackground(.background, for: .widget)
                .widgetURL(entry.cocktail.flatMap { WidgetDeepLink.cocktailURL(id: $0.id) })
        }
        .configurationDisplayName(String(localized: "Random Cocktail"))
        .description(String(localized: "Discover a new cocktail every few hours."))
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct RandomCocktailEntry: TimelineEntry {
    let date: Date
    let cocktail: WidgetRandomCocktail?
}

struct RandomCocktailProvider: TimelineProvider {
    /// A new pick from the pool every 4 hours.
    static let refreshInterval: TimeInterval = 4 * 3600

    func placeholder(in context: Context) -> RandomCocktailEntry {
        RandomCocktailEntry(
            date: Date(),
            cocktail: WidgetRandomCocktail(id: "11007", name: "Margarita", category: "Ordinary Drink", thumbnail: nil)
        )
    }

    func getSnapshot(in context: Context, completion: @escaping (RandomCocktailEntry) -> Void) {
        if context.isPreview {
            completion(placeholder(in: context))
        } else {
            completion(entry(at: Date()))
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<RandomCocktailEntry>) -> Void) {
        // The app reloads this timeline when it first fills an empty pool;
        // otherwise the widget advances through the pool on its own.
        let now = Date()
        completion(Timeline(
            entries: [entry(at: now)],
            policy: .after(now.addingTimeInterval(Self.refreshInterval))
        ))
    }

    private func entry(at date: Date) -> RandomCocktailEntry {
        RandomCocktailEntry(
            date: date,
            cocktail: WidgetRandomSnapshotStore.cocktail(
                in: WidgetRandomSnapshotStore.read(),
                at: date,
                rotation: Self.refreshInterval
            )
        )
    }
}

struct RandomCocktailWidgetView: View {
    let entry: RandomCocktailEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        if let cocktail = entry.cocktail {
            switch family {
            case .systemMedium:
                HStack(spacing: 12) {
                    thumbnail(cocktail, size: 64)
                    info(cocktail)
                    Spacer(minLength: 0)
                }
            default:
                VStack(alignment: .leading, spacing: 6) {
                    thumbnail(cocktail, size: 48)
                    info(cocktail)
                    Spacer(minLength: 0)
                }
            }
        } else {
            // Nothing written yet (the app hasn't run, or has never been
            // online nor shown a cocktail): point the user at the app
            VStack(spacing: 4) {
                Image(systemName: "wineglass")
                    .font(.title3)
                    .foregroundStyle(.secondary)
                Text(String(localized: "No cocktail right now"))
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(String(localized: "Open CocktailCraft to discover some"))
                    .font(.caption2)
                    .foregroundStyle(.tertiary)
            }
            .multilineTextAlignment(.center)
        }
    }

    private func thumbnail(_ cocktail: WidgetRandomCocktail, size: CGFloat) -> some View {
        Group {
            if let data = cocktail.thumbnail, let image = UIImage(data: data) {
                Image(uiImage: image)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } else {
                Rectangle()
                    .fill(WidgetColors.primary.opacity(0.15))
                    .overlay(
                        Image(systemName: "wineglass")
                            .foregroundStyle(WidgetColors.primary)
                    )
            }
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }

    private func info(_ cocktail: WidgetRandomCocktail) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(String(localized: "Try something new"))
                .font(.caption2)
                .foregroundStyle(WidgetColors.primary)
            Text(cocktail.name)
                .font(.headline)
                .lineLimit(2)
            if let category = cocktail.category {
                Text(category)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            }
        }
    }
}
