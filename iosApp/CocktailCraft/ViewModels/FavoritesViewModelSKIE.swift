import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedFavoritesViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper.
 */
final class FavoritesViewModelSKIE: SharedViewModelWrapper<FavoritesUiState> {

    // Computed properties
    var isEmpty: Bool {
        state.favorites.isEmpty
    }

    var hasItems: Bool {
        !state.favorites.isEmpty
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedFavoritesViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedFavoritesViewModel()
        self.sharedViewModel = viewModel
        super.init(uiState: viewModel.uiState, errorFlow: viewModel.error)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

    // MARK: - Public Methods (using SKIE async/await)

    func loadFavorites() async {
        await run { try await sharedViewModel.loadFavorites() }
    }

    func toggleFavorite(_ cocktail: Cocktail) async {
        await run { try await sharedViewModel.toggleFavorite(cocktail: cocktail) }
    }

    func clearAllFavorites() async {
        await run { try await sharedViewModel.clearAllFavorites() }
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
        // No artificial delay: the spinner should last exactly as long as the
        // reload does (IO-2).
        await loadFavorites()
    }

    func getCategoryCount(_ category: String) -> Int {
        return getFavoritesByCategory(category).count
    }

    func getTopRatedFavorites(limit: Int = 5) -> [Cocktail] {
        return getFavoritesSortedByRating().prefix(limit).map { $0 }
    }
}
