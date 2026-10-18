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
        super.init(viewModel: viewModel, uiState: viewModel.uiState)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

    // MARK: - Public Methods (using SKIE async/await)

    /// Returns whether the cocktail was added, so callers confirm only a
    /// real add (the shared ViewModel announces the outcome on `events`
    /// too, not on the cart's error channel).
    @discardableResult
    func addToCart(_ cocktail: Cocktail, quantity: Int = 1) async -> Bool {
        await run(fallback: false) {
            try await sharedViewModel.addToCart(cocktail: cocktail, quantity: Int32(quantity)).boolValue
        }
    }

    // Fire-and-forget: the shared ViewModel applies the change at once
    // (optimistically, reverted on failure) and saves it on its own scope

    func decrementQuantity(_ cocktailId: String) {
        sharedViewModel.decrementQuantity(cocktailId: cocktailId)
    }

    func incrementQuantity(_ cocktailId: String) {
        sharedViewModel.incrementQuantity(cocktailId: cocktailId)
    }

    /// Announced on the mirrored `event` with an Undo action.
    func removeFromCart(_ cocktailId: String) {
        sharedViewModel.removeFromCart(cocktailId: cocktailId)
    }
}
