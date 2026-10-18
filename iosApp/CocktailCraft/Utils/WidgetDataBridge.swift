import Foundation
import UIKit
import WidgetKit
import shared
import Combine

/// App-side half of the widget data flow: snapshots data from the shared KMP
/// layer into the App Group container (WidgetSnapshotStore,
/// WidgetRandomSnapshotStore) and asks WidgetKit to re-render. The widget
/// extension never links the shared framework and does no networking —
/// this bridge is its only data source.
@MainActor
final class WidgetDataBridge {
    static let shared = WidgetDataBridge()

    /// Random Cocktail pool size: the fresh pick plus recently viewed ones.
    private static let randomPoolSize: Int32 = 6

    private var cancellables = Set<AnyCancellable>()
    private var favoritesObservation: Task<Void, Never>?
    private var lastFavoriteIds: [String]?
    private var started = false

    private init() {}

    /// Call once at launch (after Koin is initialized).
    /// - Favorites: follows the shared favorites flow, so the widget updates
    ///   as soon as a favorite is added or removed anywhere in the app.
    /// - Random Cocktail: refreshes the pool now and whenever the app leaves
    ///   the foreground (when this session's recently viewed list is final).
    func start() {
        guard !started else { return }
        started = true

        favoritesObservation = Task { [weak self] in
            for await favorites in getSharedKoinHelper().observeFavoriteCocktails() {
                self?.writeFavoritesSnapshot(favorites)
            }
        }

        NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)
            .sink { _ in
                Task { @MainActor in
                    await WidgetDataBridge.shared.refreshRandomCocktailPool()
                }
            }
            .store(in: &cancellables)

        Task { await refreshRandomCocktailPool() }
    }

    // MARK: - Favorites

    private func writeFavoritesSnapshot(_ favorites: [Cocktail]) {
        // The flow re-emits on in-place upgrades too (e.g. a favorite's
        // details filled in); only rewrite + reload when the list changed.
        let ids = favorites.map(\.id)
        guard ids != lastFavoriteIds else { return }
        lastFavoriteIds = ids

        let snapshot = WidgetFavoritesSnapshot(
            favorites: favorites.map {
                WidgetCocktail(
                    id: $0.id,
                    name: $0.name,
                    imageUrl: $0.imageUrl,
                    price: $0.price
                )
            },
            updatedAt: Date()
        )
        WidgetSnapshotStore.write(snapshot)
        // Only the favorites widget consumes this snapshot.
        WidgetCenter.shared.reloadTimelines(ofKind: WidgetKind.favorites)
    }

    // MARK: - Random Cocktail

    /// Rewrites the Random Cocktail pool: a fresh random pick when online,
    /// then recently viewed cocktails, each with its preview thumbnail.
    /// A failed refresh keeps the previous pool.
    func refreshRandomCocktailPool() async {
        guard let cocktails = try? await getSharedKoinHelper()
            .getRandomCocktailWidgetPool(limit: Self.randomPoolSize),
            !cocktails.isEmpty else {
            return
        }

        let previous = WidgetRandomSnapshotStore.read()
        // Reuse thumbnails already in the pool instead of re-downloading
        var thumbnails: [String: Data] = [:]
        for cocktail in previous?.cocktails ?? [] {
            thumbnails[cocktail.id] = cocktail.thumbnail
        }

        var pool: [WidgetRandomCocktail] = []
        for cocktail in cocktails {
            let thumbnail: Data?
            if let cached = thumbnails[cocktail.id] {
                thumbnail = cached
            } else {
                thumbnail = await Self.downloadThumbnail(imageUrl: cocktail.imageUrl)
            }
            pool.append(WidgetRandomCocktail(
                id: cocktail.id,
                name: cocktail.name,
                category: cocktail.category,
                thumbnail: thumbnail
            ))
        }
        WidgetRandomSnapshotStore.write(WidgetRandomCocktailSnapshot(cocktails: pool, updatedAt: Date()))

        // The widget picks the new pool up at its next 4-hourly refresh;
        // reloading on every backgrounding would swap the cocktail each time
        // the user leaves the app. Only an empty widget is filled right away.
        if previous?.cocktails.isEmpty ?? true {
            WidgetCenter.shared.reloadTimelines(ofKind: WidgetKind.randomCocktail)
        }
    }

    /// TheCocktailDB's /preview variant (~100px): plenty for a widget and
    /// small enough to embed in the snapshot. nil on any failure — the
    /// widget then shows its placeholder glyph.
    private static func downloadThumbnail(imageUrl: String?) async -> Data? {
        guard let imageUrl, !imageUrl.isEmpty,
              let url = URL(string: imageUrl + "/preview"),
              let (data, response) = try? await URLSession.shared.data(from: url),
              (response as? HTTPURLResponse)?.statusCode == 200,
              UIImage(data: data) != nil else {
            return nil
        }
        return data
    }
}
