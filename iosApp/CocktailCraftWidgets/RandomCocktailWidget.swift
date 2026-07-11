import WidgetKit
import SwiftUI

/// "Cocktail of the hour": fetches a random cocktail straight from
/// TheCocktailDB (the API is public, so the extension needs no app data)
/// and deep-links to its detail screen on tap. Mirrors Android's
/// RandomCocktailWidget.
struct RandomCocktailWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: WidgetKind.randomCocktail, provider: RandomCocktailProvider()) { entry in
            RandomCocktailWidgetView(entry: entry)
                .containerBackground(.background, for: .widget)
                .widgetURL(entry.cocktail.flatMap { WidgetDeepLink.cocktailURL(id: $0.id) })
        }
        .configurationDisplayName("Random Cocktail")
        .description("Discover a new cocktail every few hours.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct RandomCocktailEntry: TimelineEntry {
    let date: Date
    let cocktail: RandomWidgetCocktail?
}

struct RandomWidgetCocktail {
    let id: String
    let name: String
    let category: String?
    let imageData: Data?
}

struct RandomCocktailProvider: TimelineProvider {
    private static let randomEndpoint = URL(string: "https://www.thecocktaildb.com/api/json/v1/1/random.php")!

    func placeholder(in context: Context) -> RandomCocktailEntry {
        RandomCocktailEntry(
            date: Date(),
            cocktail: RandomWidgetCocktail(id: "11007", name: "Margarita", category: "Ordinary Drink", imageData: nil)
        )
    }

    func getSnapshot(in context: Context, completion: @escaping (RandomCocktailEntry) -> Void) {
        if context.isPreview {
            completion(placeholder(in: context))
            return
        }
        Task { completion(await fetchEntry()) }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<RandomCocktailEntry>) -> Void) {
        Task {
            let entry = await fetchEntry()
            // A fresh pick every ~4 hours; on failure retry sooner.
            let refresh = Calendar.current.date(
                byAdding: .hour,
                value: entry.cocktail == nil ? 1 : 4,
                to: entry.date
            ) ?? entry.date.addingTimeInterval(4 * 3600)
            completion(Timeline(entries: [entry], policy: .after(refresh)))
        }
    }

    private func fetchEntry() async -> RandomCocktailEntry {
        do {
            let (data, _) = try await URLSession.shared.data(from: Self.randomEndpoint)
            let response = try JSONDecoder().decode(RandomDrinkResponse.self, from: data)
            guard let drink = response.drinks.first else {
                return RandomCocktailEntry(date: Date(), cocktail: nil)
            }

            // The /preview variant is ~100x100 — plenty for a widget and
            // safely inside the extension's memory budget.
            var imageData: Data? = nil
            if let thumb = drink.strDrinkThumb,
               let imageUrl = URL(string: thumb + "/preview") {
                imageData = try? await URLSession.shared.data(from: imageUrl).0
            }

            return RandomCocktailEntry(
                date: Date(),
                cocktail: RandomWidgetCocktail(
                    id: drink.idDrink,
                    name: drink.strDrink,
                    category: drink.strCategory,
                    imageData: imageData
                )
            )
        } catch {
            return RandomCocktailEntry(date: Date(), cocktail: nil)
        }
    }
}

private struct RandomDrinkResponse: Decodable {
    struct Drink: Decodable {
        let idDrink: String
        let strDrink: String
        let strCategory: String?
        let strDrinkThumb: String?
    }

    let drinks: [Drink]
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
            VStack(spacing: 4) {
                Image(systemName: "wineglass")
                    .font(.title3)
                    .foregroundStyle(.secondary)
                Text("No cocktail right now")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text("Check back soon")
                    .font(.caption2)
                    .foregroundStyle(.tertiary)
            }
        }
    }

    private func thumbnail(_ cocktail: RandomWidgetCocktail, size: CGFloat) -> some View {
        Group {
            if let data = cocktail.imageData, let image = UIImage(data: data) {
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

    private func info(_ cocktail: RandomWidgetCocktail) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("Try something new")
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
