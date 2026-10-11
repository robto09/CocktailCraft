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
        super.init(viewModel: viewModel, uiState: viewModel.uiState)
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

    /// Per-keystroke entry point: pushes the query into the shared debounced
    /// search pipeline (SH-13/IO-1). One request per typing pause, superseded
    /// searches cancelled in shared code; an empty query clears stale results
    /// by falling back to the category listing.
    func updateSearchQuery(_ query: String) {
        sharedViewModel.updateSearchQuery(query: query)
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

    // MARK: - Synchronous Methods

    /// Canonical curated category list from shared (CocktailCategories.CURATED).
    var curatedCategories: [String] {
        return sharedViewModel.curatedCategories
    }

    func clearSearch() {
        sharedViewModel.clearSearch()
    }

    /// Reset all advanced-search filters (and the query) and reload the default list.
    func clearSearchFilters() {
        sharedViewModel.clearSearchFilters()
    }


    func retry() {
        sharedViewModel.retry()
    }

    // MARK: - Helper Methods for SwiftUI

    func refreshCocktails() async {
        // No artificial delay: the spinner should last exactly as long as the
        // reload does (IO-2).
        await loadCocktails()
    }
}
