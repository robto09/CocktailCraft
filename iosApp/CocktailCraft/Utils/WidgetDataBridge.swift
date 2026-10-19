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
    ///   the foreground (when this session's recently viewed list is final),
    ///   at most once per `minimumRefreshInterval`.
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
                    priceText: $0.price.formatted()
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
    /// A failed refresh keeps the previous pool, and so does a refresh
    /// within `minimumRefreshInterval` of the last one.
    func refreshRandomCocktailPool() async {
        let previous = WidgetRandomSnapshotStore.read()
        guard !Self.isFresh(previous, at: Date()) else { return }

        guard let cocktails = try? await getSharedKoinHelper()
            .getRandomCocktailWidgetPool(limit: Self.randomPoolSize),
            !cocktails.isEmpty else {
            return
        }

        // Reuse thumbnails already in the pool instead of re-downloading;
        // fetch the rest concurrently
        var thumbnails: [String: Data] = [:]
        for cocktail in previous?.cocktails ?? [] {
            thumbnails[cocktail.id] = cocktail.thumbnail
        }
        let missing = cocktails
            .filter { thumbnails[$0.id] == nil }
            .map { (id: $0.id, imageUrl: $0.imageUrl) }
        thumbnails.merge(await Self.downloadThumbnails(missing)) { cached, _ in cached }

        let pool = cocktails.map { cocktail in
            WidgetRandomCocktail(
                id: cocktail.id,
                name: cocktail.name,
                category: cocktail.category,
                thumbnail: thumbnails[cocktail.id]
            )
        }
        WidgetRandomSnapshotStore.write(WidgetRandomCocktailSnapshot(cocktails: pool, updatedAt: Date()))

        // The widget picks the new pool up at its next 4-hourly refresh;
        // reloading on every backgrounding would swap the cocktail each time
        // the user leaves the app. Only an empty widget is filled right away.
        if previous?.cocktails.isEmpty ?? true {
            WidgetCenter.shared.reloadTimelines(ofKind: WidgetKind.randomCocktail)
        }
    }

    /// True when `snapshot` holds a pool written less than
    /// `minimumRefreshInterval` before `date`, so refreshing again would
    /// only spend another random.php call and thumbnail downloads: the
    /// widget shows a new pool at its next 4-hourly timeline refresh anyway.
    /// An empty pool, or one dated in the future (clock change), is stale.
    nonisolated static func isFresh(_ snapshot: WidgetRandomCocktailSnapshot?, at date: Date) -> Bool {
        guard let snapshot, !snapshot.cocktails.isEmpty else { return false }
        let age = date.timeIntervalSince(snapshot.updatedAt)
        return age >= 0 && age < minimumRefreshInterval
    }

    /// The app refreshes the pool at launch and on every backgrounding;
    /// within this interval of the last refresh it keeps the pool as is.
    nonisolated static let minimumRefreshInterval: TimeInterval = 60 * 60

    /// Downloads with at most this many requests in flight.
    private static let maxConcurrentDownloads = 3

    /// Thumbnails for `cocktails` by id, downloaded concurrently (bounded);
    /// a failed download is simply missing from the result.
    private static func downloadThumbnails(
        _ cocktails: [(id: String, imageUrl: String?)]
    ) async -> [String: Data] {
        await withTaskGroup(of: (String, Data?).self) { group in
            var pending = cocktails.makeIterator()
            func startNext() {
                guard let next = pending.next() else { return }
                group.addTask { (next.id, await downloadThumbnail(imageUrl: next.imageUrl)) }
            }
            for _ in 0..<maxConcurrentDownloads { startNext() }

            var thumbnails: [String: Data] = [:]
            for await (id, data) in group {
                thumbnails[id] = data
                startNext()
            }
            return thumbnails
        }
    }

    /// TheCocktailDB's /preview variant (~100px): plenty for a widget and
    /// small enough to embed in the snapshot. nil on any failure — the
    /// widget then shows its placeholder glyph.
    private nonisolated static func downloadThumbnail(imageUrl: String?) async -> Data? {
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
