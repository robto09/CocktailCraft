import SwiftUI
@preconcurrency import shared
import Combine

/**
 * Proof of Concept: Swift wrapper for SharedCocktailListViewModel
 * 
 * This demonstrates SKIE's capabilities for shared ViewModels:
 * - StateFlow automatically converted to AsyncSequence
 * - Suspend functions become async functions
 * - Type safety preserved (no casting needed)
 * - Lifecycle managed with ViewModelStore pattern
 */
@MainActor
class SharedCocktailListViewModelWrapper: ObservableObject {
    
    // MARK: - Published Properties (driven by SKIE StateFlows)
    @Published var cocktails: [Cocktail] = []
    @Published var searchQuery: String = ""
    @Published var selectedCategory: String? = nil
    @Published var isLoading: Bool = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    
    // MARK: - Private Properties
    private let sharedViewModel: SharedCocktailListViewModel
    private var cancellables = Set<AnyCancellable>()
    private var observationTasks = Set<Task<Void, Never>>()
    
    // MARK: - Initialization
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = KoinInitializer.shared.getSharedCocktailListViewModel()
        
        // Start observing SKIE StateFlows
        startObservingStateFlows()
    }
    
    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        
        // Clean up shared ViewModel
        sharedViewModel.onCleared()
    }
    
    // MARK: - SKIE StateFlow Observation
    private func startObservingStateFlows() {
        // Observe cocktails StateFlow (SKIE converts to AsyncSequence)
        let cocktailsTask = Task {
            for await cocktailList in sharedViewModel.cocktails {
                await MainActor.run {
                    self.cocktails = cocktailList
                }
            }
        }
        observationTasks.insert(cocktailsTask)
        
        // Observe search query StateFlow
        let searchTask = Task {
            for await query in sharedViewModel.searchQuery {
                await MainActor.run {
                    self.searchQuery = query
                }
            }
        }
        observationTasks.insert(searchTask)
        
        // Observe selected category StateFlow
        let categoryTask = Task {
            for await category in sharedViewModel.selectedCategory {
                await MainActor.run {
                    self.selectedCategory = category
                }
            }
        }
        observationTasks.insert(categoryTask)
        
        // Observe loading state from base SharedViewModel
        let loadingTask = Task {
            for await loading in sharedViewModel.isLoading {
                await MainActor.run {
                    self.isLoading = loading
                }
            }
        }
        observationTasks.insert(loadingTask)
        
        // Observe error state from base SharedViewModel
        let errorTask = Task {
            for await error in sharedViewModel.error {
                await MainActor.run {
                    self.error = error
                }
            }
        }
        observationTasks.insert(errorTask)
    }
    
    // MARK: - Public Methods (SKIE converts suspend functions to async)
    
    func loadInitialCocktails() {
        Task {
            await sharedViewModel.loadInitialCocktails()
        }
    }
    
    func searchCocktails(query: String) {
        Task {
            await sharedViewModel.searchCocktails(query: query)
        }
    }
    
    func loadCocktailsByCategory(category: String) {
        Task {
            await sharedViewModel.loadCocktailsByCategory(category: category)
        }
    }
    
    func refresh() {
        Task {
            await sharedViewModel.refresh()
        }
    }
    
    func clearSearch() {
        sharedViewModel.clearSearch()
    }
    
    // MARK: - Computed Properties
    var cocktailCount: Int {
        sharedViewModel.getCocktailCount()
    }
    
    var isSearching: Bool {
        sharedViewModel.isSearching()
    }
    
    var hasSelectedCategory: Bool {
        sharedViewModel.hasSelectedCategory()
    }
    
    // MARK: - Error Handling
    func clearError() {
        sharedViewModel.clearError()
    }
}

// Note: getSharedCocktailListViewModel() is now defined in CocktailCraftApp.swift
