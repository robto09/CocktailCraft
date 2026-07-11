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
        await run { try await sharedViewModel.loadCocktails() }
    }

    func loadCocktailsByCategory(_ category: String?) async {
        await run { try await sharedViewModel.loadCocktailsByCategory(category: category) }
    }

    func searchCocktails(query: String) async {
        await run { try await sharedViewModel.searchCocktails(query: query) }
    }

    /// Apply the advanced-search filters. The shared ViewModel stores them in
    /// `state.searchFilters` (driving the active-filter chips) and loads the
    /// intersected results.
    func applyFilters(_ filters: SearchFilters) async {
        await run { try await sharedViewModel.applyFilters(filters: filters) }
    }

    /// Load the API-backed option lists (categories / ingredients)
    /// that back the advanced-search filter sheet.
    func loadFilterOptions() async {
        await run { try await sharedViewModel.loadFilterOptions() }
    }

    func loadMoreCocktails() async {
        await run { try await sharedViewModel.loadMoreCocktails() }
    }

    func toggleFavorite(_ cocktail: Cocktail) async {
        await run { try await sharedViewModel.toggleFavorite(cocktail: cocktail) }
    }

    func addToCart(_ cocktail: Cocktail) async {
        await run { try await cartViewModel.addToCart(cocktail: cocktail, quantity: 1) }
    }

    func sortByPrice(ascending: Bool) async {
        await run { try await sharedViewModel.sortByPrice(ascending: ascending) }
    }

    func sortByRating() async {
        await run { try await sharedViewModel.sortByRating() }
    }

    func sortByPopularity() async {
        await run { try await sharedViewModel.sortByPopularity() }
    }

    func getCocktailById(_ id: String) async -> Cocktail? {
        return await run(fallback: nil as Cocktail?) { try await sharedViewModel.getCocktailById(id: id) }
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

    /// Reset all advanced-search filters (and the query) and reload the default list.
    func clearSearchFilters() {
        sharedViewModel.clearSearchFilters()
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
