import SwiftUI
import shared
import Combine

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
    @Published var isSearchActive = false
    @Published var searchQuery = ""
    @Published var isOfflineMode = false
    @Published var isNetworkAvailable = true
    @Published var hasMoreData = false
    @Published var isLoadingMore = false
    @Published var favorites: [Cocktail] = []
    
    // Shared ViewModel instance
    private let sharedViewModel: SharedHomeViewModel
    
    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []
    
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = KoinInitializer.shared.getSharedHomeViewModel()
        
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
        sharedViewModel.onCleared()
    }
    
    // MARK: - SKIE StateFlow Observation
    
    private func startObserving() {
        // Observe cocktails using SKIE async sequence
        observationTasks.append(Task {
            for await cocktailList in sharedViewModel.cocktails {
                await MainActor.run {
                    self.cocktails = cocktailList
                    self.filteredCocktails = cocktailList
                }
            }
        })
        
        // Observe loading state
        observationTasks.append(Task {
            for await loading in sharedViewModel.isLoading {
                await MainActor.run {
                    self.isLoading = loading
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
        
        // Observe selected category
        observationTasks.append(Task {
            for await category in sharedViewModel.selectedCategory {
                await MainActor.run {
                    self.selectedCategory = category
                }
            }
        })
        
        // Observe search state
        observationTasks.append(Task {
            for await active in sharedViewModel.isSearchActive {
                await MainActor.run {
                    self.isSearchActive = active
                }
            }
        })
        
        // Observe search query
        observationTasks.append(Task {
            for await query in sharedViewModel.searchQuery {
                await MainActor.run {
                    self.searchQuery = query
                }
            }
        })
        
        // Observe offline mode
        observationTasks.append(Task {
            for await offline in sharedViewModel.isOfflineMode {
                await MainActor.run {
                    self.isOfflineMode = offline
                }
            }
        })
        
        // Observe network availability
        observationTasks.append(Task {
            for await available in sharedViewModel.isNetworkAvailable {
                await MainActor.run {
                    self.isNetworkAvailable = available
                }
            }
        })
        
        // Observe pagination state
        observationTasks.append(Task {
            for await hasMore in sharedViewModel.hasMoreData {
                await MainActor.run {
                    self.hasMoreData = hasMore
                }
            }
        })
        
        observationTasks.append(Task {
            for await loadingMore in sharedViewModel.isLoadingMore {
                await MainActor.run {
                    self.isLoadingMore = loadingMore
                }
            }
        })
        
        // Observe favorites
        observationTasks.append(Task {
            for await favoritesList in sharedViewModel.favorites {
                await MainActor.run {
                    self.favorites = favoritesList
                }
            }
        })
    }
    
    // MARK: - Public Methods (using SKIE async/await)
    
    func loadCocktails() async {
        await sharedViewModel.loadCocktails()
    }
    
    func loadCocktailsByCategory(_ category: String?) async {
        await sharedViewModel.loadCocktailsByCategory(category: category)
    }
    
    func searchCocktails(query: String) async {
        await sharedViewModel.searchCocktails(query: query)
    }
    
    func loadMoreCocktails() async {
        await sharedViewModel.loadMoreCocktails()
    }
    
    func toggleFavorite(_ cocktail: Cocktail) async {
        await sharedViewModel.toggleFavorite(cocktail: cocktail)
    }
    
    func sortByPrice(ascending: Bool) async {
        await sharedViewModel.sortByPrice(ascending: ascending)
    }
    
    func sortByRating() async {
        await sharedViewModel.sortByRating()
    }
    
    func sortByPopularity() async {
        await sharedViewModel.sortByPopularity()
    }
    
    func getCocktailById(_ id: String) async -> Cocktail? {
        return await sharedViewModel.getCocktailById(id: id)
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
    
    func applyFilters(category: String? = nil, ingredient: String? = nil) {
        var filtered = cocktails
        
        if let category = category {
            filtered = filtered.filter { $0.category == category }
        }
        
        if let ingredient = ingredient {
            filtered = filtered.filter { cocktail in
                cocktail.ingredients.contains { $0.name == ingredient }
            }
        }
        
        filteredCocktails = filtered
    }
}