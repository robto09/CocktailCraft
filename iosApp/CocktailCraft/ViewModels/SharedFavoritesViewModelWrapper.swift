import SwiftUI
@preconcurrency import shared
import Combine

/**
 * Swift wrapper for SharedFavoritesViewModel
 * 
 * This demonstrates SKIE's capabilities for shared ViewModels:
 * - StateFlow automatically converted to AsyncSequence
 * - Suspend functions become async functions
 * - Type safety preserved (no casting needed)
 * - Lifecycle managed with ViewModelStore pattern
 */
@MainActor
class SharedFavoritesViewModelWrapper: ObservableObject {
    
    // MARK: - Published Properties (driven by SKIE StateFlows)
    @Published var favoriteCocktails: [Cocktail] = []
    @Published var favoriteCount: Int = 0
    @Published var isLoading: Bool = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    
    // MARK: - Private Properties
    private let sharedViewModel: SharedFavoritesViewModel
    private var cancellables = Set<AnyCancellable>()
    private var observationTasks = Set<Task<Void, Never>>()
    
    // MARK: - Initialization
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = KoinInitializer.shared.getSharedFavoritesViewModel()
        
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
        // Observe favorite cocktails StateFlow (SKIE converts to AsyncSequence)
        let favoritesTask = Task {
            for await favorites in sharedViewModel.favoriteCocktails {
                await MainActor.run {
                    self.favoriteCocktails = favorites
                }
            }
        }
        observationTasks.insert(favoritesTask)
        
        // Observe favorite count StateFlow
        let countTask = Task {
            for await count in sharedViewModel.favoriteCount {
                await MainActor.run {
                    self.favoriteCount = count
                }
            }
        }
        observationTasks.insert(countTask)
        
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
    
    func loadFavorites() {
        Task {
            await sharedViewModel.loadFavorites()
        }
    }
    
    func toggleFavorite(cocktail: Cocktail) {
        Task {
            do {
                try await sharedViewModel.toggleFavorite(cocktail: cocktail)
            } catch {
                print("Error toggling favorite: \(error)")
            }
        }
    }
    
    func clearAllFavorites() {
        Task {
            do {
                try await sharedViewModel.clearAllFavorites()
            } catch {
                print("Error clearing favorites: \(error)")
            }
        }
    }
    
    func refresh() {
        Task {
            await sharedViewModel.refresh()
        }
    }
    
    // MARK: - Synchronous Helper Methods
    
    func isFavorite(cocktailId: String) -> Bool {
        return sharedViewModel.isFavorite(cocktailId: cocktailId)
    }
    
    func getFavoriteCount() -> Int {
        return sharedViewModel.getFavoriteCount()
    }
    
    func hasFavorites() -> Bool {
        return sharedViewModel.hasFavorites()
    }
    
    // MARK: - Error Handling
    
    func clearError() {
        error = nil
    }
}
