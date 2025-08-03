import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedOfflineModeViewModel using pure SKIE integration.
 * No FlowCollector bridge needed - uses native Swift async/await.
 */
@MainActor
class OfflineModeViewModelSKIE: ObservableObject {
    // Published properties for SwiftUI
    @Published var isOfflineModeEnabled = false
    @Published var isNetworkAvailable = true
    @Published var recentlyViewedCocktails: [Cocktail] = []
    @Published var cacheSize = 0
    @Published var lastSyncTime: String? = nil
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    
    // Computed properties
    var hasRecentlyViewed: Bool {
        !recentlyViewedCocktails.isEmpty
    }
    
    var isOnlineAndOfflineModeDisabled: Bool {
        isNetworkAvailable && !isOfflineModeEnabled
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
    private var observationTasks: [Task<Void, Never>] = []
    
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedOfflineModeViewModel()
        
        // Start observing StateFlows using SKIE async/await
        startObserving()
    }
    
    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        sharedViewModel.onCleared()
    }
    
    // MARK: - SKIE StateFlow Observation
    
    private func startObserving() {
        // Observe offline mode enabled state
        observationTasks.append(Task {
            for await enabled in sharedViewModel.isOfflineModeEnabled {
                await MainActor.run {
                    self.isOfflineModeEnabled = enabled.boolValue
                }
            }
        })
        
        // Observe network availability
        observationTasks.append(Task {
            for await available in sharedViewModel.isNetworkAvailable {
                await MainActor.run {
                    self.isNetworkAvailable = available.boolValue
                }
            }
        })
        
        // Observe recently viewed cocktails
        observationTasks.append(Task {
            for await cocktails in sharedViewModel.recentlyViewedCocktails {
                await MainActor.run {
                    self.recentlyViewedCocktails = cocktails
                }
            }
        })
        
        // Observe cache size
        observationTasks.append(Task {
            for await size in sharedViewModel.cacheSize {
                await MainActor.run {
                    self.cacheSize = Int(truncating: size)
                }
            }
        })
        
        // Observe last sync time
        observationTasks.append(Task {
            for await syncTime in sharedViewModel.lastSyncTime {
                await MainActor.run {
                    self.lastSyncTime = syncTime
                }
            }
        })
        
        // Observe loading state
        observationTasks.append(Task {
            for await loading in sharedViewModel.isLoading {
                await MainActor.run {
                    self.isLoading = loading.boolValue
                }
            }
        })
        
        // Observe error state
        observationTasks.append(Task {
            for await errorValue in sharedViewModel.error {
                await MainActor.run {
                    self.error = errorValue
                }
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
        guard let syncTime = lastSyncTime else {
            return "Never synced"
        }
        return "Last sync: \(syncTime)"
    }
    
    func getCacheSizeText() -> String {
        let count = getCachedCocktailCount()
        return "\(count) cocktail\(count == 1 ? "" : "s") cached"
    }
    
    func getNetworkStatusColor() -> Color {
        return isNetworkAvailable ? .green : .red
    }
    
    func getOfflineModeColor() -> Color {
        return isOfflineModeEnabled ? .orange : .blue
    }
    
    func shouldShowOfflineRecommendation() -> Bool {
        return !isNetworkAvailable && !isOfflineModeEnabled
    }
}