import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedHomeViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper; views read `viewModel.state.<field>`.
 */
final class HomeViewModelSKIE: SharedViewModelWrapper<HomeUiState> {

    // Shared ViewModel instances
    private let sharedViewModel: SharedHomeViewModel
    private let cartViewModel: SharedCartViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedHomeViewModel()
        self.sharedViewModel = viewModel
        self.cartViewModel = getSharedKoinHelper().getSharedCartViewModel()
        // No initial load here: @State (unlike @StateObject) evaluates its
        // initial value eagerly on every parent re-render, and discarded
        // instances must not fire side effects. HomeViewSKIE's .task loads.
        super.init(uiState: viewModel.uiState, errorFlow: viewModel.error)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

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

    /// Canonical curated category list from shared (CocktailCategories.CURATED).
    var curatedCategories: [String] {
        return sharedViewModel.curatedCategories
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
