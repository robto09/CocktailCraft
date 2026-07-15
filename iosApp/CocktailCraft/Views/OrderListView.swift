import SwiftUI
import shared

struct OrderListView: View {
    @State private var viewModel = OrderViewModelSKIE()
    @State private var hasLoaded = false
    // Tab switches route through the app-level router (same pattern as
    // CartView/ProfileView) so "Browse Cocktails" can land on Home.
    @Environment(AppRouter.self) private var router

    var body: some View {
        // No nav container here: ContentView already wraps this tab in a
        // NavigationStack (the old inner NavigationView double-nested it).
        contentView
            .navigationTitle("Orders")
            .brandedNavigationBar()
            // Seed load once; afterwards the observed orders flow keeps the
            // list fresh. A load cancelled mid-flight (user leaves the tab)
            // leaves hasLoaded false, so the next visit retries.
            .task {
                guard !hasLoaded else { return }
                await viewModel.loadOrders()
                if !Task.isCancelled { hasLoaded = true }
            }
            .sharedErrorAlert(viewModel.error, clear: { viewModel.clearError() })
    }

    @ViewBuilder
    private var contentView: some View {
        if !hasLoaded || viewModel.state.isLoading {
            loadingView
        } else if viewModel.state.orders.isEmpty {
            emptyView
        } else {
            ordersList
        }
    }

    private var loadingView: some View {
        LoadingStateView(message: "Loading orders...")
    }

    // Mirrors Android's Orders empty state: icon/title/message plus a
    // "Browse Cocktails" CTA that switches to the Home tab.
    private var emptyView: some View {
        EmptyStateView(
            icon: "list.bullet",
            title: String(localized: "No orders yet"),
            message: String(localized: "Your order history will appear here"),
            actionTitle: String(localized: "Browse Cocktails"),
            action: {
                router.selectedTab = .home
            }
        )
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
            Text("Total: " + order.total.asPrice)
                .font(.body)
        }
        .padding(.vertical, 4)
    }
}

