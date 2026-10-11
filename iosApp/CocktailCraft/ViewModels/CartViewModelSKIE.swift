import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedCartViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper; views read `state.<field>` — including the
 * derived `state.deliveryFee` / `state.finalTotal`, which come from the
 * shared DeliveryPolicy (never hardcode delivery amounts on the Swift side).
 */
final class CartViewModelSKIE: SharedViewModelWrapper<CartUiState> {

    // Shared ViewModel instance
    private let sharedViewModel: SharedCartViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedCartViewModel()
        self.sharedViewModel = viewModel
        super.init(uiState: viewModel.uiState, errorFlow: viewModel.error)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

    // MARK: - Public Methods (using SKIE async/await)

    func addToCart(_ cocktail: Cocktail, quantity: Int = 1) async {
        await run { try await sharedViewModel.addToCart(cocktail: cocktail, quantity: Int32(quantity)) }
    }

    func decrementQuantity(_ cocktailId: String) async {
        // Optimistic apply/revert lives in the shared ViewModel
        await run { try await sharedViewModel.decrementQuantity(cocktailId: cocktailId) }
    }

    func incrementQuantity(_ cocktailId: String) async {
        // Optimistic apply/revert lives in the shared ViewModel
        await run { try await sharedViewModel.incrementQuantity(cocktailId: cocktailId) }
    }

    func removeFromCart(_ cocktailId: String) async {
        await run { try await sharedViewModel.removeFromCart(cocktailId: cocktailId) }
    }

    // MARK: - Synchronous Methods

    func clearError() {
        sharedViewModel.clearError()
    }
}
