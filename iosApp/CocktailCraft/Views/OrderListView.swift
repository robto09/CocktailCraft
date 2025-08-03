import SwiftUI
import shared

struct OrderListView: View {
    @StateObject private var viewModel = OrderViewModelSKIE()
    @State private var hasAppeared = false

    var body: some View {
        VStack {
            if !hasAppeared || viewModel.isLoading {
                ProgressView("Loading orders...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.orders.isEmpty {
                EmptyStateView(
                    icon: "bag",
                    title: "No orders yet",
                    message: "Your order history will appear here"
                )
            } else {
                ScrollView {
                    LazyVStack(spacing: AppTheme.Spacing.lg) {
                        ForEach(viewModel.orders, id: \.id) { order in
                            OrderCard(order: order)
                                .padding(.horizontal, AppTheme.Spacing.lg)
                        }
                    }
                    .padding(.vertical, AppTheme.Spacing.lg)
                }
            }
        }
        .navigationTitle("My Orders")
        .onAppear {
            if !hasAppeared {
                hasAppeared = true
                Task {
                    await viewModel.loadOrders()
                }
            }
        }
        .refreshable {
            await viewModel.loadOrders()
        }
        .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("OrderPlaced"))) { _ in
            Task {
                await viewModel.loadOrders()
            }
        }
        .alert(isPresented: .constant(viewModel.error != nil)) {
            Alert(
                title: Text("Error"),
                message: viewModel.error != nil ? Text(viewModel.error!.message) : nil,
                primaryButton: .default(Text("Retry")) {
                    Task {
                        await viewModel.loadOrders()
                    }
                },
                secondaryButton: .cancel {
                    viewModel.clearError()
                }
            )
        }
    }
}

