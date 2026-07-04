import SwiftUI
import shared
import Observation

/**
 * iOS ViewModel wrapper for SharedHomeViewModel using pure SKIE integration.
 * Mirrors the consolidated uiState as Observation-tracked state; views read
 * `viewModel.state.<field>` for everything the shared ViewModel owns.
 */
@MainActor
@Observable
class HomeViewModelSKIE {
    // Consolidated UI state from the shared ViewModel
    private(set) var state: HomeUiState
    // The single error channel from the shared ViewModel base class
    var error: ErrorHandler.UserFriendlyError? = nil

    // Shared ViewModel instances
    private let sharedViewModel: SharedHomeViewModel
    private let cartViewModel: SharedCartViewModel

    // Tasks for async observation
    @ObservationIgnored private var observationTasks: [Task<Void, Never>] = []

    init() {
        // Get shared ViewModels from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedHomeViewModel()
        self.cartViewModel = getSharedKoinHelper().getSharedCartViewModel()

        // Seed synchronously so the first frame renders the current state
        self.state = sharedViewModel.uiState.value

        // Start observing StateFlows using SKIE async/await
        startObserving()
        // No initial load here: @State (unlike @StateObject) evaluates its
        // initial value eagerly on every parent re-render, and discarded
        // instances must not fire side effects. HomeViewSKIE's .task loads.
    }

    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        // Note: Do NOT call onCleared() — this wraps Koin `single` instances
        // whose coroutine scope must survive the lifetime of any one wrapper.
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
}
