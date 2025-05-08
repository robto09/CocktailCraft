import SwiftUI
import shared

struct OrderListScreen: View {
    @StateObject private var viewModel = DependencyContainer.shared.makeOrderViewModel()
    @State private var showingError = false
    @State private var errorMessage = ""
    @State private var showingOrderDetails = false
    
    var body: some View {
        ZStack {
            if viewModel.orders.isEmpty {
                emptyOrdersView
            } else {
                orderListView
            }
            
            if viewModel.isLoading {
                ProgressView()
                    .scaleEffect(1.5)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black.opacity(0.2))
            }
        }
        .navigationTitle("Orders")
        .alert("Error", isPresented: $showingError) {
            Button("OK") {}
        } message: {
            Text(errorMessage)
        }
        .sheet(isPresented: $showingOrderDetails) {
            if let order = viewModel.selectedOrder {
                OrderDetailsView(
                    order: order,
                    onCancel: handleOrderCancel
                )
            }
        }
        .refreshable {
            viewModel.refreshOrders()
        }
        .onAppear {
            observeViewModel()
        }
    }
    
    private var emptyOrdersView: some View {
        VStack(spacing: 16) {
            Image(systemName: "list.bullet.rectangle")
                .font(.system(size: 64))
                .foregroundColor(.secondary)
            
            Text("No orders yet")
                .font(.title2)
                .foregroundColor(.secondary)
            
            NavigationLink(destination: HomeTab()) {
                Text("Browse Cocktails")
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding()
                    .background(Color.blue)
                    .cornerRadius(10)
            }
        }
    }
    
    private var orderListView: some View {
        List {
            ForEach(viewModel.orders, id: \.id) { order in
                OrderRow(
                    order: order,
                    onTap: {
                        viewModel.selectOrder(orderId: order.id)
                        showingOrderDetails = true
                    }
                )
            }
        }
        .listStyle(InsetGroupedListStyle())
    }
    
    private func observeViewModel() {
        // Observe error state
        Task {
            for await error in viewModel.error {
                if let errorText = error {
                    errorMessage = errorText
                    showingError = true
                }
            }
        }
    }
    
    private func handleOrderCancel(orderId: String) {
        viewModel.cancelOrder(orderId: orderId)
        showingOrderDetails = false
    }
}

struct OrderRow: View {
    let order: Order
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("Order #\(order.id)")
                        .font(.headline)
                    Spacer()
                    StatusBadge(status: order.status)
                }
                
                Text(order.items.map { "\($0.quantity)x \($0.name)" }.joined(separator: ", "))
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
                
                HStack {
                    Text(formatDate(order.createdAt))
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    Text("$\(String(format: "%.2f", order.totalAmount))")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                }
            }
            .padding(.vertical, 8)
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private func formatDate(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp / 1000))
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

struct StatusBadge: View {
    let status: OrderStatus
    
    var body: some View {
        Text(status.name)
            .font(.caption)
            .fontWeight(.medium)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(backgroundColor)
            .foregroundColor(.white)
            .cornerRadius(8)
    }
    
    private var backgroundColor: Color {
        switch status {
        case .PENDING:
            return .orange
        case .CONFIRMED:
            return .blue
        case .PREPARING:
            return .purple
        case .READY:
            return .green
        case .DELIVERED:
            return .gray
        case .CANCELLED:
            return .red
        default:
            return .secondary
        }
    }
}

struct OrderDetailsView: View {
    let order: Order
    let onCancel: (String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var showingCancelConfirmation = false
    
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Items")) {
                    ForEach(order.items, id: \.cocktailId) { item in
                        HStack {
                            Text("\(item.quantity)x \(item.name)")
                            Spacer()
                            Text("$\(String(format: "%.2f", item.price * Double(item.quantity)))")
                                .foregroundColor(.secondary)
                        }
                    }
                }
                
                Section(header: Text("Details")) {
                    if let address = order.deliveryAddress {
                        LabeledContent("Delivery Address", value: address)
                    }
                    
                    if let method = order.paymentMethod {
                        LabeledContent("Payment Method", value: method)
                    }
                    
                    if let instructions = order.specialInstructions {
                        LabeledContent("Special Instructions", value: instructions)
                    }
                    
                    LabeledContent("Total Amount", value: "$\(String(format: "%.2f", order.totalAmount))")
                    
                    LabeledContent("Order Date", value: formatDate(order.createdAt))
                }
                
                if order.status == .PENDING {
                    Section {
                        Button(role: .destructive, action: { showingCancelConfirmation = true }) {
                            Text("Cancel Order")
                        }
                    }
                }
            }
            .navigationTitle("Order Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
            .alert("Cancel Order", isPresented: $showingCancelConfirmation) {
                Button("Cancel Order", role: .destructive) {
                    onCancel(order.id)
                }
                Button("Keep Order", role: .cancel) {}
            } message: {
                Text("Are you sure you want to cancel this order?")
            }
        }
    }
    
    private func formatDate(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp / 1000))
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

// MARK: - Preview
struct OrderListScreen_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            OrderListScreen()
        }
    }
}