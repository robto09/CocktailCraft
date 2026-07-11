import Foundation
import UIKit
import WidgetKit
import shared
import Combine

/// App-side half of the widget data flow: snapshots the favorites list from
/// the shared KMP layer into the App Group container (WidgetSnapshotStore)
/// and asks WidgetKit to re-render. The widget extension never links the
/// shared framework — this bridge is its only data source.
@MainActor
final class WidgetDataBridge {
    static let shared = WidgetDataBridge()

    private var cancellables = Set<AnyCancellable>()
    private var started = false

    private init() {}

    /// Call once at launch (after Koin is initialized). Snapshots
    /// immediately, then again every time the app leaves the foreground —
    /// the natural point where favorite changes are final.
    func start() {
        guard !started else { return }
        started = true

        NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)
            .sink { _ in
                Task { @MainActor in
                    await WidgetDataBridge.shared.refreshFavoritesSnapshot()
                }
            }
            .store(in: &cancellables)

        Task { await refreshFavoritesSnapshot() }
    }

    func refreshFavoritesSnapshot() async {
        // A failed fetch throws (KoinHelper unwraps via getOrThrow); keep
        // the widget's last-known-good snapshot rather than blanking it.
        guard let favorites = try? await getSharedKoinHelper().getFavoriteCocktailsSnapshot() else {
            return
        }
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
        // Only the favorites widget consumes this snapshot. Reloading ALL
        // timelines would also reset the Random Cocktail widget (new pick +
        // network fetch) on every launch/backgrounding.
        WidgetCenter.shared.reloadTimelines(ofKind: WidgetKind.favorites)
    }
}
