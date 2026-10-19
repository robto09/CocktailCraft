import SwiftUI
import shared

/// Navigation value for one order's detail screen (pushed on the Orders tab).
struct OrderDetailRoute: Hashable {
    let orderId: String
}

/// One order: status, delivery estimate, items and totals, with Reorder
/// (its items back into the cart) and Cancel Order (behind a
/// confirmation) while the order is still cancellable —
/// the counterpart of Android's OrderDetailScreen. Owns its screen-scoped
/// shared ViewModel, so it never shows an order an earlier visit left.
struct OrderDetailView: View {
    let orderId: String

    // Built once via ViewModelHolder, not on every parent re-render (IO-13)
    @State private var model = ViewModelHolder(OrderDetailViewModelSKIE.init)
    private var viewModel: OrderDetailViewModelSKIE { model.wrapper }
    @State private var showCancelConfirmation = false
    @Environment(\.isDarkMode) private var isDarkMode

    private var order: Order? { viewModel.state.order }

    var body: some View {
        content
            // Its own alert level, apart from the error alert below
            .alert(L(\.order_cancel_confirm_title), isPresented: $showCancelConfirmation) {
                Button(L(\.order_cancel), role: .destructive) {
                    viewModel.cancelOrder()
                }
                Button(L(\.order_cancel_keep), role: .cancel) {}
            } message: {
                Text(L(\.order_cancel_confirm_message, String(orderId.suffix(5))))
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(AppColors.background(isDarkMode: isDarkMode).ignoresSafeArea())
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("orderDetail.screen")
            .brandedNavigationBar(title: L(\.order_detail_title))
            .task {
                // Re-fires when the view re-appears: skip the lookup when
                // this screen already shows its order
                if order?.id != orderId {
                    viewModel.loadOrder(orderId)
                }
            }
            // "Order cancelled", "Added 3 items to your cart" or a failed reorder
            .sharedEventToast(viewModel.event, clear: { viewModel.clearEvent() })
            // A failed cancel alerts over the order; a failed load is the
            // full-screen ErrorView with Retry instead
            .sharedErrorAlert(order != nil ? viewModel.error : nil, clear: { viewModel.clearError() })
    }

    @ViewBuilder
    private var content: some View {
        if let order {
            ScrollView {
                OrderDetailContent(
                    order: order,
                    subtotal: viewModel.state.subtotal,
                    deliveryFee: viewModel.state.deliveryFee,
                    deliveryEstimate: viewModel.state.deliveryEstimate.localized(),
                    canCancel: viewModel.state.canCancel,
                    isCancelling: viewModel.state.isCancelling,
                    onCancel: { showCancelConfirmation = true },
                    canReorder: viewModel.state.canReorder,
                    isReordering: viewModel.state.isReordering,
                    onReorder: {
                        viewModel.reorder()
                    }
                )
            }
        } else if viewModel.state.isLoading {
            OrderListSkeleton(cardCount: 1)
        } else if let error = viewModel.error {
            ErrorView(
                error: error,
                onRetry: {
                    viewModel.loadOrder(orderId)
                }
            )
        } else {
            VStack(spacing: AppTheme.Spacing.sm) {
                Image(systemName: "doc.text.magnifyingglass")
                    .font(.largeTitle)
                    .foregroundColor(.secondary)
                    .accessibilityHidden(true)
                Text(L(\.order_detail_not_found_title))
                    .font(.headline)
                    .accessibilityAddTraits(.isHeader)
                Text(L(\.order_detail_not_found_message))
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
        }
    }
}
