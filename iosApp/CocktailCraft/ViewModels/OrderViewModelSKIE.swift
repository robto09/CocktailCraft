import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedOrderViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper; views read `state.orders` / `state.isLoading`.
 */
final class OrderViewModelSKIE: SharedViewModelWrapper<OrderUiState> {

    // Shared ViewModel instance
    private let sharedViewModel: SharedOrderViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedOrderViewModel()
        self.sharedViewModel = viewModel
        super.init(uiState: viewModel.uiState, errorFlow: viewModel.error)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

    // MARK: - Public Methods (using SKIE async/await)

    func loadOrders() async {
        await run { try await sharedViewModel.loadOrders() }
    }

    /// Returns whether the order was placed, so the caller can navigate on
    /// success; failures surface on the mirrored `error`.
    func placeOrder(cartItems: [CocktailCartItem], totalPrice: Double) async -> Bool {
        return await run(fallback: false) { try await sharedViewModel.placeOrder(cartItems: cartItems, totalPrice: totalPrice).boolValue }
    }

    // MARK: - Synchronous Methods

    func clearError() {
        sharedViewModel.clearError()
    }
}
