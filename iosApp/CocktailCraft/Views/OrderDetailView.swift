import SwiftUI
import shared

/// Navigation value for one order's detail screen (pushed on the Orders tab).
struct OrderDetailRoute: Hashable {
    let orderId: String
}

/// One order: status, delivery estimate, items and totals, with Reorder
/// (its items back into the cart) and Cancel Order (behind a
/// confirmation) while the order is still cancellable —
/// the counterpart of Android's OrderDetailScreen. Shares the order list's
/// wrapper, whose shared ViewModel holds the selected order.
struct OrderDetailView: View {
    let orderId: String
    let viewModel: OrderViewModelSKIE

    // Distinguishes "load not started yet" from "loaded but not found"
    @State private var hasLoaded = false
    @State private var showCancelConfirmation = false
    @Environment(\.isDarkMode) private var isDarkMode

    /// The shared ViewModel is a Koin single: never show another order an
    /// earlier visit left selected.
    private var order: Order? {
        guard let selected = viewModel.state.selectedOrder, selected.id == orderId else { return nil }
        return selected
    }

    var body: some View {
        content
            // Its own alert level, apart from the error alert below
            .alert(L(\.order_cancel_confirm_title), isPresented: $showCancelConfirmation) {
                Button(L(\.order_cancel), role: .destructive) {
                    Task { await viewModel.cancelOrder(orderId) }
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
                await viewModel.loadOrderDetail(orderId)
                if !Task.isCancelled { hasLoaded = true }
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
                    subtotal: viewModel.state.selectedOrderSubtotal,
                    deliveryFee: viewModel.state.selectedOrderDeliveryFee,
                    deliveryEstimate: viewModel.state.selectedOrderDeliveryEstimate.localized(),
                    canCancel: viewModel.state.canCancelSelectedOrder,
                    isCancelling: viewModel.state.isCancellingOrder,
                    onCancel: { showCancelConfirmation = true },
                    canReorder: viewModel.state.canReorderSelectedOrder,
                    isReordering: viewModel.state.isReordering,
                    onReorder: {
                        Task { await viewModel.reorder(orderId) }
                    }
                )
            }
        } else if !hasLoaded || viewModel.state.isLoadingOrderDetail {
            OrderListSkeleton(cardCount: 1)
        } else if let error = viewModel.error {
            ErrorView(
                error: error,
                onRetry: {
                    Task { await viewModel.loadOrderDetail(orderId) }
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
