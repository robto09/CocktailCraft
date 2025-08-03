import SwiftUI
import shared

struct OrderListView: View {
    @StateObject private var viewModel = OrderViewModelSKIE()
    @State private var hasAppeared = false

    var body: some View {
        NavigationView {
            VStack {
                if !hasAppeared || viewModel.isLoading {
                    ProgressView("Loading orders...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.orders.isEmpty {
                    Text("No orders yet")
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        ForEach(Array(viewModel.orders), id: \.id) { order in
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Order #\(order.id)")
                                    .font(.headline)
                                Text("Status: \(order.status)")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                Text("Total: $\(order.totalPrice, specifier: "%.2f")")
                                    .font(.body)
                            }
                            .padding(.vertical, 4)
                        }
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
        }
    }
}

