import SwiftUI
import shared

struct OrderListView: View {
    @Environment(\.presentationMode) var presentationMode
    @StateObject private var viewModel = OrderViewModelSKIE()

    var body: some View {
        NavigationView {
            VStack {
                if viewModel.isLoading {
                    ProgressView("Loading orders...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.orders.isEmpty {
                    EmptyStateView(
                        icon: "bag",
                        title: "No orders yet",
                        message: "Your order history will appear here"
                    )
                } else {
                    List(viewModel.orders, id: \.id) { order in
                        OrderRowView(order: order)
                    }
                }
            }
            .navigationTitle("My Orders")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
            }
            .onAppear {
                Task {
                    await viewModel.loadOrders()
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
}

struct OrderRowView: View {
    let order: Order

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("Order #\(order.id)")
                    .font(.headline)
                    .fontWeight(.semibold)
                Spacer()
                Text(statusColor(for: order.status))
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(statusBackgroundColor(for: order.status))
                    .foregroundColor(statusTextColor(for: order.status))
                    .cornerRadius(8)
            }

            Text("Date: \(order.date)")
                .font(.subheadline)
                .foregroundColor(.secondary)

            VStack(alignment: .leading, spacing: 4) {
                Text("Items:")
                    .font(.subheadline)
                    .fontWeight(.medium)

                ForEach(order.items, id: \.name) { item in
                    HStack {
                        Text("\(item.quantity)x \(item.name)")
                            .font(.caption)
                        Spacer()
                        Text("$\(item.price * Double(item.quantity), specifier: "%.2f")")
                            .font(.caption)
                            .fontWeight(.medium)
                    }
                }
            }

            Divider()

            HStack {
                Text("Total:")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                Spacer()
                Text("$\(order.total, specifier: "%.2f")")
                    .font(.subheadline)
                    .fontWeight(.bold)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private func statusColor(for status: String) -> String {
        return status
    }

    private func statusBackgroundColor(for status: String) -> Color {
        switch status.lowercased() {
        case "processing":
            return Color.orange.opacity(0.2)
        case "delivered":
            return Color.green.opacity(0.2)
        case "cancelled":
            return Color.red.opacity(0.2)
        default:
            return Color.blue.opacity(0.2)
        }
    }

    private func statusTextColor(for status: String) -> Color {
        switch status.lowercased() {
        case "processing":
            return Color.orange
        case "delivered":
            return Color.green
        case "cancelled":
            return Color.red
        default:
            return Color.blue
        }
    }
}