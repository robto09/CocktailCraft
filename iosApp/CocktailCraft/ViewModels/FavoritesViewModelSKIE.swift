import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedFavoritesViewModel using pure SKIE integration.
 * No FlowCollector bridge needed - uses native Swift async/await.
 */
@MainActor
class FavoritesViewModelSKIE: ObservableObject {
    // Published properties for SwiftUI
    @Published var favorites: [shared.Cocktail] = []
    @Published var favoriteCount: Int = 0
    @Published var isLoading = false
    @Published var error: shared.ErrorHandler.UserFriendlyError? = nil
    
    // Computed properties
    var isEmpty: Bool {
        favorites.isEmpty
    }
    
    var hasItems: Bool {
        !favorites.isEmpty
    }
    
    // Shared ViewModel instance
    private let sharedViewModel: shared.SharedFavoritesViewModel
    
    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []
    
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedFavoritesViewModel()
        
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
        // Observe favorites using SKIE async sequence
        observationTasks.append(Task {
            for await favoritesList in sharedViewModel.favorites {
                await MainActor.run {
                    self.favorites = favoritesList
                }
            }
        })
        
        // Observe favorite count
        observationTasks.append(Task {
            for await count in sharedViewModel.favoriteCount {
                await MainActor.run {
                    self.favoriteCount = Int(count.intValue)
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
    
    func loadFavorites() async {
        do {
            try await sharedViewModel.loadFavorites()
        } catch {
            print("FavoritesViewModelSKIE - Error loading favorites: \(error)")
        }
    }
    
    func toggleFavorite(_ cocktail: shared.Cocktail) async {
        do {
            try await sharedViewModel.toggleFavorite(cocktail: cocktail)
        } catch {
            print("FavoritesViewModelSKIE - Error toggling favorite: \(error)")
        }
    }
    
    func clearAllFavorites() async {
        do {
            try await sharedViewModel.clearAllFavorites()
        } catch {
            print("FavoritesViewModelSKIE - Error clearing favorites: \(error)")
        }
    }
    
    // MARK: - Synchronous Methods
    
    func isFavorite(_ cocktailId: String) -> Bool {
        return sharedViewModel.isFavorite(cocktailId: cocktailId)
    }
    
    func getFavoritesByCategory(_ category: String) -> [shared.Cocktail] {
        return sharedViewModel.getFavoritesByCategory(category: category)
    }
    
    func getFavoriteCategories() -> [String] {
        return sharedViewModel.getFavoriteCategories()
    }
    
    func searchFavorites(query: String) -> [shared.Cocktail] {
        return sharedViewModel.searchFavorites(query: query)
    }
    
    func getFavoritesSortedByName() -> [shared.Cocktail] {
        return sharedViewModel.getFavoritesSortedByName()
    }
    
    func getFavoritesSortedByDate() -> [shared.Cocktail] {
        return sharedViewModel.getFavoritesSortedByDate()
    }
    
    func getFavoritesSortedByRating() -> [shared.Cocktail] {
        return sharedViewModel.getFavoritesSortedByRating()
    }
    
    func clearError() {
        sharedViewModel.clearError()
    }
    
    func refresh() {
        sharedViewModel.refresh()
    }
    
    // MARK: - Helper Methods for SwiftUI
    
    func refreshFavorites() async {
        // Simulate refresh with delay
        try? await Task.sleep(nanoseconds: 500_000_000)
        await loadFavorites()
    }
    
    func getCategoryCount(_ category: String) -> Int {
        return getFavoritesByCategory(category).count
    }
    
    func getTopRatedFavorites(limit: Int = 5) -> [shared.Cocktail] {
        return getFavoritesSortedByRating().prefix(limit).map { $0 }
    }
}