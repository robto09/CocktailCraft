import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedFavoritesViewModel using pure SKIE integration.
 * Mirrors the consolidated uiState as a single @Published value.
 */
@MainActor
class FavoritesViewModelSKIE: ObservableObject {
    // Consolidated UI state from the shared ViewModel
    @Published private(set) var state: FavoritesUiState
    // Base-class error flow (distinct from state.error, matching prior behavior)
    @Published var error: ErrorHandler.UserFriendlyError? = nil

    // Computed properties
    var isEmpty: Bool {
        state.favorites.isEmpty
    }

    var hasItems: Bool {
        !state.favorites.isEmpty
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedFavoritesViewModel

    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []

    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedFavoritesViewModel()

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
