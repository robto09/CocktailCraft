import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedFavoritesViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper; views read `state.favorites`.
 */
final class FavoritesViewModelSKIE: SharedViewModelWrapper<FavoritesUiState> {

    // Shared ViewModel instance
    private let sharedViewModel: SharedFavoritesViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedFavoritesViewModel()
        self.sharedViewModel = viewModel
        super.init(viewModel: viewModel, uiState: viewModel.uiState)
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

    // MARK: - Helper Methods for SwiftUI

    func refreshFavorites() async {
        // No artificial delay: the spinner should last exactly as long as the
        // reload does (IO-2).
        await loadFavorites()
    }
}
