import SwiftUI
import shared

struct OrderListScreen: View {
    @StateObject private var orderViewModel = ViewModelProvider.shared.orderViewModel
    @EnvironmentObject var navigationCoordinator: NavigationCoordinator
    
    @State private var animatedIndices = Set<Int>()
    
    var body: some View {
        NavigationView {
            contentView
        }
        .onAppear {
            if orderViewModel.orders.isEmpty {
                orderViewModel.loadOrders()
            }
        }
    }
    
    @ViewBuilder
    private var contentView: some View {
        if orderViewModel.isLoading {
            loadingView
        } else if orderViewModel.orders.isEmpty {
            emptyView
        } else {
            ordersListView
        }
    }
    
    private var loadingView: some View {
        ProgressView("Loading orders...")
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private var emptyView: some View {
        EmptyStateView(
            title: "No orders yet",
            message: "Your order history will appear here",
            systemImage: "list.bullet.rectangle",
            actionButtonText: "Browse Cocktails",
            onActionButtonClick: {
                navigationCoordinator.navigateToTab(.home)
            }
        )
    }
    
    private var ordersListView: some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                headerView
                ordersContent
            }
            .padding(.vertical)
        }
        .navigationTitle("Orders")
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    orderViewModel.loadOrders()
                }) {
                    Image(systemName: "arrow.clockwise")
                }
            }
        }
    }
    
    private var headerView: some View {
        HStack {
            Text("Your Orders")
                .font(.system(size: 20, weight: .bold))
            Spacer()
        }
        .padding(.horizontal)
        .padding(.top, 8)
    }
    
    private var ordersContent: some View {
        ForEach(Array(orderViewModel.orders.enumerated()), id: \.element.id) { index, order in
            orderItemView(order: order, index: index)
        }
    }
    
    private func orderItemView(order: Order, index: Int) -> some View {
        OrderItemView(order: order)
            .padding(.horizontal)
            .onAppear {
                withAnimation(.easeOut(duration: 0.3).delay(Double(index) * 0.05)) {
                    _ = animatedIndices.insert(index)
                }
            }
            .onDisappear {
                animatedIndices.remove(index)
            }
    }
}