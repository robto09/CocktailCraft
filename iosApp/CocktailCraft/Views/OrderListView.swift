import SwiftUI
import shared

struct OrderListView: View {
    @StateObject private var viewModel = OrderViewModelSKIE()
    @State private var hasAppeared = false

    var body: some View {
        NavigationView {
            contentView
                .navigationTitle("My Orders")
                .onAppear {
                    if !hasAppeared {
                        hasAppeared = true
                        Task {
                            await viewModel.loadOrders()
                        }
                    }
                }
        }
    }

    @ViewBuilder
    private var contentView: some View {
        if !hasAppeared || viewModel.state.isLoading {
            loadingView
        } else if viewModel.state.orders.isEmpty {
            emptyView
        } else {
            ordersList
        }
    }

    private var loadingView: some View {
        ProgressView("Loading orders...")
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var emptyView: some View {
        Text("No orders yet")
            .foregroundColor(.secondary)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var ordersList: some View {
        List {
            ForEach(Array(viewModel.state.orders), id: \.id) { order in
                orderRow(order: order)
            }
        }
    }

    private func orderRow(order: Order) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Order #\(order.id)")
                .font(.headline)
            Text("Status: \(order.status)")
                .font(.subheadline)
                .foregroundColor(.secondary)
            Text("Total: $\(order.total, specifier: "%.2f")")
                .font(.body)
        }
        .padding(.vertical, 4)
    }
}

