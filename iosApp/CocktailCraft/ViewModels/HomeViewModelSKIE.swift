import SwiftUI
import shared
import Combine

enum SortOption: CaseIterable {
    case nameAsc
    case nameDesc
    case priceAsc
    case priceDesc
    case ratingDesc
    case popularityDesc
}

/**
 * iOS ViewModel wrapper for SharedHomeViewModel using pure SKIE integration.
 * No FlowCollector bridge needed - uses native Swift async/await.
 */
@MainActor
class HomeViewModelSKIE: ObservableObject {
    // Published properties for SwiftUI
    @Published var cocktails: [Cocktail] = []
    @Published var filteredCocktails: [Cocktail] = []
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    @Published var selectedCategory: String? = nil
    @Published var selectedIngredient: String? = nil
    @Published var sortOption: SortOption = .nameAsc
    @Published var isSearchActive = false
    @Published var searchQuery = ""
    @Published var isOfflineMode = false
    @Published var isNetworkAvailable = true
    @Published var hasMoreData = false
    @Published var isLoadingMore = false
    @Published var favorites: [Cocktail] = []

    // Shared ViewModel instances
    private let sharedViewModel: SharedHomeViewModel
    private let cartViewModel: SharedCartViewModel
    
    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []
    
    init() {
        // Get shared ViewModels from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedHomeViewModel()
        self.cartViewModel = getSharedKoinHelper().getSharedCartViewModel()
        
        // Start observing StateFlows using SKIE async/await
        startObserving()
        
        // Initial load
        Task {
            await loadCocktails()
        }
    }
    
    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        // Note: Do NOT call onCleared() — these are Koin singletons whose
        // coroutine scope must survive the lifetime of any single wrapper.
    }
    
    // MARK: - SKIE StateFlow Observation
    
    private func startObserving() {
        // Single consolidated observation of uiState
        observationTasks.append(Task {
            for await state in sharedViewModel.uiState {
                await MainActor.run {
                    self.cocktails = state.cocktails
                    self.filteredCocktails = state.cocktails
                    self.isLoading = state.isLoading
                    self.selectedCategory = state.selectedCategory
                    self.isSearchActive = state.isSearchActive
                    self.searchQuery = state.searchQuery
                    self.isOfflineMode = state.isOfflineMode
                    self.isNetworkAvailable = state.isNetworkAvailable
                    self.hasMoreData = state.hasMoreData
                    self.isLoadingMore = state.isLoadingMore
                    self.favorites = state.favorites
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
    
    func loadCocktails() async {
        do {
            try await sharedViewModel.loadCocktails()
        } catch {
            print("HomeViewModelSKIE - Error loading cocktails: \(error)")
        }
    }
    
    func loadCocktailsByCategory(_ category: String?) async {
        do {
            try await sharedViewModel.loadCocktailsByCategory(category: category)
        } catch {
            print("HomeViewModelSKIE - Error loading cocktails by category: \(error)")
        }
    }
    
    func searchCocktails(query: String) async {
        do {
            try await sharedViewModel.searchCocktails(query: query)
        } catch {
            print("HomeViewModelSKIE - Error searching cocktails: \(error)")
        }
    }
    
    func loadMoreCocktails() async {
        do {
            try await sharedViewModel.loadMoreCocktails()
        } catch {
            print("HomeViewModelSKIE - Error loading more cocktails: \(error)")
        }
    }
    
    func toggleFavorite(_ cocktail: Cocktail) async {
        do {
            try await sharedViewModel.toggleFavorite(cocktail: cocktail)
        } catch {
            print("HomeViewModelSKIE - Error toggling favorite: \(error)")
        }
    }
    
    func addToCart(_ cocktail: Cocktail) async {
        do {
            try await cartViewModel.addToCart(cocktail: cocktail, quantity: 1)
        } catch {
            print("HomeViewModelSKIE - Error adding to cart: \(error)")
        }
    }
    
    func sortByPrice(ascending: Bool) async {
        do {
            try await sharedViewModel.sortByPrice(ascending: ascending)
        } catch {
            print("HomeViewModelSKIE - Error sorting by price: \(error)")
        }
    }
    
    func sortByRating() async {
        do {
            try await sharedViewModel.sortByRating()
        } catch {
            print("HomeViewModelSKIE - Error sorting by rating: \(error)")
        }
    }
    
    func sortByPopularity() async {
        do {
            try await sharedViewModel.sortByPopularity()
        } catch {
            print("HomeViewModelSKIE - Error sorting by popularity: \(error)")
        }
    }
    
    func getCocktailById(_ id: String) async -> Cocktail? {
        do {
            return try await sharedViewModel.getCocktailById(id: id)
        } catch {
            print("HomeViewModelSKIE - Error getting cocktail by ID: \(error)")
            return nil
        }
    }
    
    // MARK: - Synchronous Methods
    
    func isFavorite(_ cocktailId: String) -> Bool {
        return sharedViewModel.isFavorite(cocktailId: cocktailId)
    }
    
    func getCategories() -> [String] {
        return sharedViewModel.getCategories()
    }
    
    func getCocktailsByCategory(_ category: String, limit: Int32 = 3) -> [Cocktail] {
        return sharedViewModel.getCocktailsByCategory(category: category, limit: limit)
    }
    
    func setOfflineMode(_ enabled: Bool) {
        sharedViewModel.setOfflineMode(enabled: enabled)
    }
    
    func clearSearch() {
        sharedViewModel.clearSearch()
    }
    
    func clearError() {
        sharedViewModel.clearError()
    }
    
    func retry() {
        sharedViewModel.retry()
    }
    
    // MARK: - Helper Methods for SwiftUI

    func refreshCocktails() async {
        // Simulate refresh with delay
        try? await Task.sleep(nanoseconds: 1_000_000_000)
        await loadCocktails()
    }

    /// Apply filters by delegating to the shared ViewModel.
    /// All filtering/sorting logic lives in the shared Kotlin use cases.
    func applyFilters() {
        Task {
            await applyFiltersAsync(category: selectedCategory, ingredient: selectedIngredient)
        }
    }

    /// Apply filters with specific category/ingredient via shared ViewModel.
    func applyFilters(category: String? = nil, ingredient: String? = nil) {
        Task {
            await applyFiltersAsync(category: category ?? selectedCategory, ingredient: ingredient ?? selectedIngredient)
        }
    }

    private func applyFiltersAsync(category: String?, ingredient: String?) async {
        do {
            try await sharedViewModel.applyFilters(category: category, ingredient: ingredient)
        } catch {
            print("HomeViewModelSKIE - Error applying filters: \(error)")
        }
    }
}