import SwiftUI

struct CartSummaryCard: View {
    let subtotal: Double
    let deliveryFee: Double
    let total: Double
    @Environment(\.isDarkMode) var isDarkMode
    
    /// Laid out like Android's OrderSummaryCard: the title inside the card,
    /// subtotal and delivery rows, one divider, then the total.
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(L(\.order_summary_title))
                .font(.title3.weight(.bold))
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .accessibilityAddTraits(.isHeader)
                .padding(.bottom, AppTheme.Spacing.md)

            VStack(spacing: AppTheme.Spacing.sm) {
                SummaryRow(
                    label: L(\.order_subtotal),
                    value: subtotal,
                    isTotal: false
                )

                SummaryRow(
                    label: L(\.order_delivery_fee),
                    value: deliveryFee,
                    isTotal: false
                )
            }

            Divider()
                .overlay(AppColors.neutralSubtle(isDarkMode: isDarkMode))
                .padding(.vertical, AppTheme.Spacing.md)

            SummaryRow(
                label: L(\.total),
                value: total,
                isTotal: true
            )
        }
        .padding(AppTheme.Spacing.lg)
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
        .padding(.horizontal, AppTheme.Spacing.lg)
    }
}

struct SummaryRow: View {
    let label: String
    let value: Double
    let isTotal: Bool
    @Environment(\.isDarkMode) var isDarkMode
    @Environment(\.appearance) var appearance
    
    var body: some View {
        // Label and amount side by side; stacked when they don't fit
        // (large Dynamic Type) rather than breaking words and prices
        ViewThatFits(in: .horizontal) {
            HStack {
                labelText
                Spacer()
                valueText
            }

            VStack(alignment: .leading, spacing: AppTheme.Spacing.xs) {
                labelText
                valueText
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        // "Subtotal, $25.00" as one element
        .accessibilityElement(children: .combine)
    }

    private var labelText: some View {
        Text(label)
            .font(isTotal ? AppTheme.Typography.headline : AppTheme.Typography.callout)
            .fontWeight(isTotal ? .bold : .regular)
            .foregroundColor(isTotal ? AppColors.textPrimary(isDarkMode: isDarkMode) : AppColors.textSecondary(appearance))
    }

    private var valueText: some View {
        Text(value.asPrice)
            .font(isTotal ? AppTheme.Typography.headline : AppTheme.Typography.callout)
            .fontWeight(isTotal ? .bold : .regular)
            .foregroundColor(isTotal ? AppColors.primary(appearance) : AppColors.textPrimary(isDarkMode: isDarkMode))
            .fixedSize()
    }
}

#Preview {
    CartSummaryCard(
        subtotal: 25.98,
        deliveryFee: 3.99,
        total: 29.97
    )
    .environment(\.isDarkMode, false)
    .padding()
}
