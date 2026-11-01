import SwiftUI
import shared

extension OrderStatus {
    /// The color a status is shown in, on the order list and the detail
    /// screen (as Android's orderStatusColor).
    var tint: Color {
        switch self {
        case .delivered: return AppColors.success
        case .pending, .processing, .shipped: return AppColors.warning
        case .cancelled: return AppColors.gray
        }
    }
}

/// A colored dot and the status's localized label ("Being Prepared").
struct OrderStatusIndicator: View {
    let status: OrderStatus

    var body: some View {
        HStack(spacing: AppTheme.Spacing.sm) {
            Circle()
                .fill(status.tint)
                .frame(width: 10, height: 10)
                .accessibilityHidden(true)
            // Localized label from the shared OrderStatus, as on Android
            Text(status.displayName.localized())
                .font(.subheadline)
                .foregroundColor(status.tint)
        }
    }
}

/// Stateless body of the order detail screen, laid out like Android's
/// OrderDetailContent: number/date/status/delivery card, items card, the
/// order summary, Reorder, then Cancel Order while the order is
/// cancellable. Values come straight from the shared OrderUiState
/// (subtotal, delivery fee, estimate and canReorder are derived there).
struct OrderDetailContent: View {
    let order: Order
    let subtotal: Money
    let deliveryFee: Money
    let deliveryEstimate: String
    let canCancel: Bool
    var isCancelling: Bool = false
    var onCancel: () -> Void = {}
    /// False for orders placed before items recorded their cocktail.
    var canReorder: Bool = false
    var isReordering: Bool = false
    var onReorder: () -> Void = {}

    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.appearance) private var appearance

    var body: some View {
        VStack(spacing: AppTheme.Spacing.lg) {
            statusCard
            itemsCard
            CartSummaryCard(subtotal: subtotal, deliveryFee: deliveryFee, total: order.total)

            reorderButton

            if canCancel {
                PrimaryButton(
                    title: L(\.order_cancel),
                    isLoading: isCancelling,
                    isDestructive: true,
                    action: onCancel
                )
                .accessibilityIdentifier("orderDetail.cancelButton")
                .padding(.horizontal, AppTheme.Spacing.lg)
            }
        }
        .padding(.vertical, AppTheme.Spacing.lg)
    }

    /// Reorder, or for an order that can't be reordered the dimmed button
    /// with the reason underneath. VoiceOver reads the reason as the
    /// button's value instead of as a separate element.
    private var reorderButton: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
            PrimaryButton(
                title: L(\.order_reorder),
                isLoading: isReordering,
                isDisabled: !canReorder,
                action: onReorder
            )
            .accessibilityIdentifier("orderDetail.reorderButton")
            .accessibilityValue(canReorder ? "" : L(\.order_reorder_unavailable))

            if !canReorder {
                Text(L(\.order_reorder_unavailable))
                    .font(.footnote)
                    .foregroundColor(AppColors.textSecondary(appearance))
                    .fixedSize(horizontal: false, vertical: true)
                    .accessibilityHidden(true)
            }
        }
        .padding(.horizontal, AppTheme.Spacing.lg)
    }

    private var statusCard: some View {
        card {
            VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
                VStack(alignment: .leading, spacing: AppTheme.Spacing.xs) {
                    // Short number, as on the list ("Order #42052")
                    Text(L(\.order_number, String(order.id.suffix(5))))
                        .font(.title3.weight(.bold))
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        .accessibilityAddTraits(.isHeader)
                    Text(L(\.order_detail_placed_on, order.date))
                        .font(.subheadline)
                        .foregroundColor(AppColors.textSecondary(appearance))
                }

                labeledRow(L(\.order_detail_status)) {
                    OrderStatusIndicator(status: order.status)
                }
                labeledRow(L(\.order_detail_delivery)) {
                    Text(deliveryEstimate)
                        .font(.subheadline.weight(.medium))
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                }
            }
        }
    }

    private var itemsCard: some View {
        card {
            VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
                Text(L(\.order_detail_items))
                    .font(.title3.weight(.bold))
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .accessibilityAddTraits(.isHeader)

                ForEach(Array(order.items.enumerated()), id: \.offset) { _, item in
                    VStack(alignment: .leading, spacing: 2) {
                        HStack(alignment: .firstTextBaseline) {
                            Text(L(\.order_item_quantity_name, item.quantity, item.name))
                                .font(.callout.weight(.medium))
                            Spacer()
                            Text(item.lineTotal.formatted())
                                .font(.callout.weight(.medium))
                        }
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        Text(L(\.order_detail_item_each, item.price.formatted()))
                            .font(.caption)
                            .foregroundColor(AppColors.textSecondary(appearance))
                    }
                    // One VoiceOver stop per item
                    .accessibilityElement(children: .combine)
                }
            }
        }
    }

    /// "Status   ● Being Prepared" — one VoiceOver stop per row.
    private func labeledRow<Value: View>(_ label: String, @ViewBuilder value: () -> Value) -> some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundColor(AppColors.textSecondary(appearance))
            Spacer()
            value()
        }
        .accessibilityElement(children: .combine)
    }

    private func card<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        content()
            .padding(AppTheme.Spacing.lg)
            .frame(maxWidth: .infinity, alignment: .leading)
            .cardStyle()
            .padding(.horizontal, AppTheme.Spacing.lg)
    }
}
