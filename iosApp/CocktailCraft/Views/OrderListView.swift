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
            .brandedNavigationBar(title: L(\.orders))
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

    // Cards on the app background, spaced like Android's LazyColumn
    // (16 around the list and between cards)
    private var ordersList: some View {
        ScrollView {
            LazyVStack(spacing: AppTheme.Spacing.lg) {
                ForEach(Array(viewModel.state.orders), id: \.id) { order in
                    orderCard(order: order)
                }
            }
            .padding(AppTheme.Spacing.lg)
        }
        // Pull-to-refresh, parity with Android Orders
        .refreshable {
            await viewModel.loadOrders()
        }
    }

    /// Laid out like Android's OrderCard: order number and date, one row per
    /// item, a divider, the total, then the colored status.
    private func orderCard(order: Order) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                // Short number, as on Android ("Order #42052")
                Text(L(\.order_number, String(order.id.suffix(5))))
                    .font(.callout.weight(.bold))
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .accessibilityAddTraits(.isHeader)
                Spacer()
                Text(order.date)
                    .font(.subheadline)
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
            }
            .padding(.bottom, AppTheme.Spacing.md)

            ForEach(Array(order.items.enumerated()), id: \.offset) { _, item in
                HStack {
                    Text(L(\.order_item_quantity_name, item.quantity, item.name))
                        .font(.subheadline)
                    Spacer()
                    Text((item.price * Double(item.quantity)).asPrice)
                        .font(.subheadline.weight(.medium))
                }
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .padding(.vertical, AppTheme.Spacing.xs)
                .accessibilityElement(children: .combine)
            }

            Divider()
                .overlay(AppColors.neutralSubtle(isDarkMode: isDarkMode))
                .padding(.vertical, AppTheme.Spacing.md)

            HStack {
                Text(L(\.total))
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                Spacer()
                Text(order.total.asPrice)
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
            }
            .font(.callout.weight(.bold))
            .accessibilityElement(children: .combine)

            HStack(spacing: AppTheme.Spacing.sm) {
                Circle()
                    .fill(statusColor(order.status))
                    .frame(width: 10, height: 10)
                Text(order.status)
                    .font(.subheadline)
                    .foregroundColor(statusColor(order.status))
            }
            .padding(.top, AppTheme.Spacing.md)
            .accessibilityElement(children: .combine)
        }
        .padding(AppTheme.Spacing.lg)
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
    }

    private func statusColor(_ status: String) -> Color {
        switch status {
        case "Completed": return AppColors.success
        case "In Progress": return AppColors.warning
        default: return AppColors.gray
        }
    }
}

