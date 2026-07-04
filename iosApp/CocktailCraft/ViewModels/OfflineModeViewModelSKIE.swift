import SwiftUI
import shared
import Observation

/**
 * iOS ViewModel wrapper for SharedOfflineModeViewModel using pure SKIE integration.
 * Mirrors the consolidated uiState as Observation-tracked state.
 */
@MainActor
@Observable
class OfflineModeViewModelSKIE {
    // Consolidated UI state from the shared ViewModel
    private(set) var state: OfflineUiState
    // The single error channel from the shared ViewModel base class
    var error: ErrorHandler.UserFriendlyError? = nil

    // Computed properties
    var hasRecentlyViewed: Bool {
        !state.recentlyViewedCocktails.isEmpty
    }

    var isOnlineAndOfflineModeDisabled: Bool {
        state.isNetworkAvailable && !state.isOfflineModeEnabled
    }

    var cacheStatusSummary: String {
        sharedViewModel.getCacheStatusSummary()
    }

    var networkStatus: String {
        sharedViewModel.getNetworkStatus()
    }

    var offlineModeStatus: String {
        sharedViewModel.getOfflineModeStatus()
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedOfflineModeViewModel

    // Tasks for async observation
    @ObservationIgnored private var observationTasks: [Task<Void, Never>] = []

    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedOfflineModeViewModel()

        // Seed synchronously so the first frame renders the current state
        self.state = sharedViewModel.uiState.value

        // Start observing StateFlows using SKIE async/await
        startObserving()
    }

    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        // Note: Do NOT call onCleared() — this wraps a Koin `single` whose
        // coroutine scope must survive the lifetime of any one wrapper.
        // (Factory-scoped wrappers — CocktailDetail, Review — do call it.)
    }

    // MARK: - SKIE StateFlow Observation

    private func startObserving() {
        // These Tasks inherit @MainActor, so assignments land on the main thread.
        observationTasks.append(Task { [weak self] in
            guard let flow = self?.sharedViewModel.uiState else { return }
            for await state in flow {
                self?.state = state
            }
        })

        observationTasks.append(Task { [weak self] in
            guard let flow = self?.sharedViewModel.error else { return }
            for await errorValue in flow {
                self?.error = errorValue
            }
        })
    }

    // MARK: - Public Methods (using SKIE async/await)

    func toggleOfflineMode() async {
        do {
            try await sharedViewModel.toggleOfflineMode()
        } catch {
            print("OfflineModeViewModelSKIE - Error toggling offline mode: \(error)")
        }
    }

    func setOfflineMode(_ enabled: Bool) async {
        do {
            try await sharedViewModel.setOfflineMode(enabled: enabled)
        } catch {
            print("OfflineModeViewModelSKIE - Error setting offline mode: \(error)")
        }
    }

    func syncCachedData() async {
        do {
            try await sharedViewModel.syncCachedData()
        } catch {
            print("OfflineModeViewModelSKIE - Error syncing cached data: \(error)")
        }
    }

    func clearCache() async {
        do {
            try await sharedViewModel.clearCache()
        } catch {
            print("OfflineModeViewModelSKIE - Error clearing cache: \(error)")
        }
    }

    func loadRecentlyViewedCocktails() async {
        do {
            try await sharedViewModel.loadRecentlyViewedCocktails()
        } catch {
            print("OfflineModeViewModelSKIE - Error loading recently viewed cocktails: \(error)")
        }
    }

    // MARK: - Synchronous Methods

    func getCachedCocktailCount() -> Int {
        return Int(sharedViewModel.getCachedCocktailCount())
    }

    func getRecentlyViewedByCategory(_ category: String) -> [Cocktail] {
        return sharedViewModel.getRecentlyViewedByCategory(category: category)
    }

    func getRecentlyViewedWithLimit(_ limit: Int) -> [Cocktail] {
        return sharedViewModel.getRecentlyViewedWithLimit(limit: Int32(limit))
    }

    func isOfflineModeRecommended() -> Bool {
        return sharedViewModel.isOfflineModeRecommended()
    }

    func clearError() {
        sharedViewModel.clearError()
    }

    func refresh() {
        sharedViewModel.refresh()
    }

    // MARK: - Helper Methods for SwiftUI

    func formatLastSyncTime() -> String {
        guard let syncTime = state.lastSyncTime else {
            return "Never synced"
        }
        return "Last sync: \(syncTime)"
    }

    func getCacheSizeText() -> String {
        let count = getCachedCocktailCount()
        return "\(count) cocktail\(count == 1 ? "" : "s") cached"
    }

    func getNetworkStatusColor() -> Color {
        return state.isNetworkAvailable ? .green : .red
    }

    func getOfflineModeColor() -> Color {
        return state.isOfflineModeEnabled ? .orange : .blue
    }

    func shouldShowOfflineRecommendation() -> Bool {
        return !state.isNetworkAvailable && !state.isOfflineModeEnabled
    }
}
