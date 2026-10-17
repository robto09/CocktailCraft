import SwiftUI
import shared

struct OrderListView: View {
    @State private var viewModel = OrderViewModelSKIE()
    @State private var hasLoaded = false
    // Tab switches route through the app-level router (same pattern as
    // CartView/ProfileView) so "Browse Cocktails" can land on Home.
    @Environment(AppRouter.self) private var router
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        // No nav container here: ContentView already wraps this tab in a
        // NavigationStack (the old inner NavigationView double-nested it).
        contentView
            // App background (not the system black/white) behind every state
            .background(AppColors.background(isDarkMode: isDarkMode).ignoresSafeArea())
            .navigationTitle(L(\.orders))
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
        LoadingStateView(message: L(\.order_list_loading))
    }

    // Mirrors Android's Orders empty state: icon/title/message plus a
    // "Browse Cocktails" CTA that switches to the Home tab.
    private var emptyView: some View {
        EmptyStateView(
            icon: "list.bullet",
            title: L(\.order_list_empty_title),
            message: L(\.order_list_empty_message),
            actionTitle: L(\.browse_cocktails),
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
        // Pull-to-refresh, parity with Android Orders
        .refreshable {
            await viewModel.loadOrders()
        }
    }

    private func orderRow(order: Order) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(L(\.order_number, order.id))
                .font(.headline)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .accessibilityAddTraits(.isHeader)
            // Colored status indicator matching Android's OrderCard
            HStack(spacing: 6) {
                Circle()
                    .fill(statusColor(order.status))
                    .frame(width: 8, height: 8)
                Text(order.status)
                    .font(.subheadline)
                    .foregroundColor(statusColor(order.status))
            }
            Text(L(\.order_total_label, order.total.asPrice))
                .font(.body)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
        }
        .padding(.vertical, 4)
        .accessibilityElement(children: .combine)
    }

    private func statusColor(_ status: String) -> Color {
        switch status {
        case "Completed": return AppColors.success
        case "In Progress": return AppColors.warning
        default: return AppColors.gray
        }
    }
}

