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
        sharedViewModel.onCleared()
        cartViewModel.onCleared()
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
                    self.isSearchActive = active.boolValue
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
                    self.isOfflineMode = offline.boolValue
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
        
        // Observe pagination state
        observationTasks.append(Task {
            for await hasMore in sharedViewModel.hasMoreData {
                await MainActor.run {
                    self.hasMoreData = hasMore.boolValue
                }
            }
        })
        
        observationTasks.append(Task {
            for await loadingMore in sharedViewModel.isLoadingMore {
                await MainActor.run {
                    self.isLoadingMore = loadingMore.boolValue
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
    
    func applyFilters() {
        applyFilters(category: selectedCategory, ingredient: selectedIngredient)
    }
    
    func applyFilters(category: String? = nil, ingredient: String? = nil) {
        var filtered = cocktails
        
        // Apply category filter
        let categoryToUse = category ?? selectedCategory
        if let categoryToUse = categoryToUse {
            filtered = filtered.filter { $0.category == categoryToUse }
        }
        
        // Apply ingredient filter
        let ingredientToUse = ingredient ?? selectedIngredient
        if let ingredientToUse = ingredientToUse {
            filtered = filtered.filter { cocktail in
                cocktail.ingredients.contains { $0.name == ingredientToUse }
            }
        }
        
        // Apply sorting
        switch sortOption {
        case .nameAsc:
            filtered = filtered.sorted { $0.name < $1.name }
        case .nameDesc:
            filtered = filtered.sorted { $0.name > $1.name }
        case .priceAsc:
            filtered = filtered.sorted { $0.price < $1.price }
        case .priceDesc:
            filtered = filtered.sorted { $0.price > $1.price }
        case .ratingDesc:
            filtered = filtered.sorted { $0.rating > $1.rating }
        case .popularityDesc:
            filtered = filtered.sorted { $0.rating > $1.rating } // Using rating as popularity proxy
        }
        
        filteredCocktails = filtered
    }
}