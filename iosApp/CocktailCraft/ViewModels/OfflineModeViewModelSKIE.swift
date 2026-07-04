import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedOfflineModeViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper.
 */
final class OfflineModeViewModelSKIE: SharedViewModelWrapper<OfflineUiState> {

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

    init() {
        let viewModel = getSharedKoinHelper().getSharedOfflineModeViewModel()
        self.sharedViewModel = viewModel
        super.init(uiState: viewModel.uiState, errorFlow: viewModel.error)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

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
