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

    /// The cocktail list's inputs, mirrored apart from `state`: they change
    /// far less often than the rest of HomeUiState (the search text on
    /// every keystroke, loading flags), and the list reads only these.
    let list: HomeListModel
    @ObservationIgnored private var listObservation: Task<Void, Never>?

    init() {
        let viewModel = getSharedKoinHelper().getSharedHomeViewModel()
        self.sharedViewModel = viewModel
        self.cartViewModel = getSharedKoinHelper().getSharedCartViewModel()
        let list = HomeListModel(HomeListContent(viewModel.uiState.value))
        self.list = list
        // No initial load here or in the view: the shared ViewModel (a
        // Koin single) starts the one initial load itself.
        super.init(viewModel: viewModel, uiState: viewModel.uiState)
        let uiState = viewModel.uiState
        listObservation = Task {
            for await value in uiState {
                list.update(HomeListContent(value))
            }
        }
    }

    // Wraps a Koin `single` whose coroutine scope must survive any one
    // wrapper — never onCleared(). The base class cancels its own
    // observation; this one stops the list's.
    deinit {
        listObservation?.cancel()
    }

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
    /// search pipeline. One request per typing pause, superseded
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

    /// Returns the outcome ("Added … to cart", or the failure) for the view
    /// to toast; nil only if the call itself was cancelled.
    func addToCart(_ cocktail: Cocktail) async -> UiEvent? {
        await run(fallback: nil) { try await cartViewModel.addToCart(cocktail: cocktail, quantity: 1) }
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
    /// long as the reload does.
    func refreshCocktails() async {
        await run { try await sharedViewModel.refresh() }
    }
}

/// What Home's cocktail list renders, derived once per state change (the
/// favorite id set used to be rebuilt on every render of Home).
struct HomeListContent: Equatable {
    let cocktails: [Cocktail]
    let favoriteIds: Set<String>
    let ratingSummaries: [String: CocktailRatingSummary]
    /// Empty while the row is hidden (an active search query)
    let recentlyViewed: [Cocktail]
    let hasMoreData: Bool

    init(_ state: HomeUiState) {
        cocktails = state.cocktails
        favoriteIds = Set(state.favorites.map(\.id))
        ratingSummaries = state.ratingSummaries
        recentlyViewed = state.showRecentlyViewed ? state.recentlyViewed : []
        hasMoreData = state.hasMoreData
    }
}

/// Observation holder for [HomeListContent]: views reading `content` only
/// re-render when it actually changes.
@MainActor
@Observable
final class HomeListModel {
    private(set) var content: HomeListContent

    init(_ content: HomeListContent) {
        self.content = content
    }

    /// Publishes a real change only: an @Observable property notifies its
    /// readers on every assignment, even of an equal value.
    func update(_ next: HomeListContent) {
        if next != content {
            content = next
        }
    }
}
