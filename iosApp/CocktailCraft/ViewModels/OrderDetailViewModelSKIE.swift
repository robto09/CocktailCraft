import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedOrderDetailViewModel using pure SKIE integration.
 * State/error/event mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper; views read `state.order` and its derived values
 * (`state.subtotal`, `state.deliveryEstimate`, `state.canCancel`, ...).
 */
final class OrderDetailViewModelSKIE: SharedViewModelWrapper<OrderDetailUiState> {

    // Shared ViewModel instance
    private let sharedViewModel: SharedOrderDetailViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedOrderDetailViewModel()
        self.sharedViewModel = viewModel
        super.init(viewModel: viewModel, uiState: viewModel.uiState, ownsViewModel: true)
    }

    // No deinit: this wraps a screen-scoped instance it owns
    // (ownsViewModel), so the base deinit clears its scope.

    // MARK: - Intents
    // Fire-and-forget: the shared ViewModel runs each on its own scope.

    /// Shows the order (`state.isLoading` until looked up; nil afterwards
    /// when there is no such order, a failed lookup surfacing on `error`).
    /// Also the Retry after a failed lookup.
    func loadOrder(_ orderId: String) {
        sharedViewModel.loadOrder(orderId: orderId)
    }

    /// Cancels the order; success is announced on the mirrored `event`
    /// ("Order cancelled"), failure on `error`.
    func cancelOrder() {
        sharedViewModel.cancelOrder()
    }

    /// Puts the order's items back in the cart; the outcome is announced
    /// on the mirrored `event` ("Added 3 items to your cart", or the
    /// failure).
    func reorder() {
        sharedViewModel.reorder()
    }
}
