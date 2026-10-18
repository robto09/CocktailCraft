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
        // No initial load here or in the view: the shared ViewModel (a
        // Koin single) starts the one initial load itself.
        super.init(viewModel: viewModel, uiState: viewModel.uiState)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

    // MARK: - Intents
    // Fire-and-forget: the shared ViewModel runs each on its own scope and
    // reports back through `state`/`error`, so views call them without a Task.

    /// Shows a category (nil = "All"). Re-selecting the category already
    /// shown does nothing, and a later selection always replaces a slower
    /// earlier one in shared code, so chip taps can't race.
    func selectCategory(_ category: String?) {
        sharedViewModel.selectCategory(category: category)
    }

    /// Explicit submit: searches right away, without the debounce.
    func searchCocktails(query: String) {
        sharedViewModel.searchCocktails(query: query)
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
    func applyFilters(_ filters: SearchFilters) {
        sharedViewModel.applyFilters(filters: filters)
    }

    /// Load the API-backed option lists (categories / ingredients)
    /// that back the advanced-search filter sheet — once per app run.
    func loadFilterOptions() {
        sharedViewModel.loadFilterOptions()
    }

    /// Shows the next page of the stored category (no request).
    func loadMoreCocktails() {
        sharedViewModel.loadMoreCocktails()
    }

    func toggleFavorite(_ cocktail: Cocktail) {
        sharedViewModel.toggleFavorite(cocktail: cocktail)
    }

    /// Returns whether the cocktail was added, so the view confirms only a
    /// real add.
    @discardableResult
    func addToCart(_ cocktail: Cocktail) async -> Bool {
        await run(fallback: false) { try await cartViewModel.addToCart(cocktail: cocktail, quantity: 1).boolValue }
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

    /// Orders the list; the shared state keeps the choice across category
    /// changes, searches and catalog refreshes.
    func setSortOption(_ option: HomeSortOption) {
        sharedViewModel.setSortOption(option: option)
    }

    /// Re-reads the "Recently viewed" row (the store has no change flow), so
    /// a cocktail opened since shows up when Home comes back on screen.
    func refreshRecentlyViewed() {
        sharedViewModel.refreshRecentlyViewed()
    }

    // MARK: - Helper Methods for SwiftUI

    /// Pull-to-refresh: refetches what the list shows and returns when it
    /// is on screen. No artificial delay: the spinner should last exactly as
    /// long as the reload does (IO-2).
    func refreshCocktails() async {
        await run { try await sharedViewModel.refresh() }
    }
}
