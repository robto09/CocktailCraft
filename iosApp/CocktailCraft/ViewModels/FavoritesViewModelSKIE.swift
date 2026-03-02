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
    @Published var favorites: [Cocktail] = []
    @Published var favoriteCount: Int = 0
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    
    // Computed properties
    var isEmpty: Bool {
        favorites.isEmpty
    }
    
    var hasItems: Bool {
        !favorites.isEmpty
    }
    
    // Shared ViewModel instance
    private let sharedViewModel: SharedFavoritesViewModel
    
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
        // Note: Do NOT call onCleared() — this is a Koin singleton whose
        // coroutine scope must survive the lifetime of any single wrapper.
    }
    
    // MARK: - SKIE StateFlow Observation
    
    private func startObserving() {
        // Single consolidated observation of uiState
        observationTasks.append(Task {
            for await state in sharedViewModel.uiState {
                await MainActor.run {
                    self.favorites = state.favorites
                    self.favoriteCount = Int(state.favoriteCount)
                    self.isLoading = state.isLoading
                }
            }
        })

        // Observe error from base class
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
    
    func toggleFavorite(_ cocktail: Cocktail) async {
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
    
    func getFavoritesByCategory(_ category: String) -> [Cocktail] {
        return sharedViewModel.getFavoritesByCategory(category: category)
    }

    func getFavoriteCategories() -> [String] {
        return sharedViewModel.getFavoriteCategories()
    }

    func searchFavorites(query: String) -> [Cocktail] {
        return sharedViewModel.searchFavorites(query: query)
    }

    func getFavoritesSortedByName() -> [Cocktail] {
        return sharedViewModel.getFavoritesSortedByName()
    }

    func getFavoritesSortedByDate() -> [Cocktail] {
        return sharedViewModel.getFavoritesSortedByDate()
    }

    func getFavoritesSortedByRating() -> [Cocktail] {
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
    
    func getTopRatedFavorites(limit: Int = 5) -> [Cocktail] {
        return getFavoritesSortedByRating().prefix(limit).map { $0 }
    }
}