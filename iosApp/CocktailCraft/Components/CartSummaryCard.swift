import SwiftUI

struct CartSummaryCard: View {
    let subtotal: Double
    let deliveryFee: Double
    let total: Double
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.lg) {
            Text(L(\.order_summary_title))
                .font(AppTheme.Typography.title2)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .accessibilityAddTraits(.isHeader)
                .padding(.horizontal, AppTheme.Spacing.lg)

            VStack(spacing: 0) {
                // Subtotal
                SummaryRow(
                    label: L(\.order_subtotal),
                    value: subtotal,
                    isTotal: false
                )
                
                Divider()
                    .padding(.horizontal, AppTheme.Spacing.lg)

                // Delivery Fee
                SummaryRow(
                    label: L(\.order_delivery_fee),
                    value: deliveryFee,
                    isTotal: false
                )
                
                Divider()
                    .padding(.horizontal, AppTheme.Spacing.lg)

                // Total
                SummaryRow(
                    label: L(\.total),
                    value: total,
                    isTotal: true
                )
            }
            .cardStyle()
            .padding(.horizontal, AppTheme.Spacing.lg)
        }
    }
}

struct SummaryRow: View {
    let label: String
    let value: Double
    let isTotal: Bool
    @Environment(\.isDarkMode) var isDarkMode
    
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
        .padding(.horizontal, AppTheme.Spacing.lg)
        .padding(.vertical, AppTheme.Spacing.md)
        // "Subtotal, $25.00" as one element
        .accessibilityElement(children: .combine)
    }

    private var labelText: some View {
        Text(label)
            .font(isTotal ? AppTheme.Typography.headline : AppTheme.Typography.body)
            .fontWeight(isTotal ? .bold : .medium)
            .foregroundColor(isTotal ? AppColors.textPrimary(isDarkMode: isDarkMode) : AppColors.textSecondary(isDarkMode: isDarkMode))
    }

    private var valueText: some View {
        Text(value.asPrice)
            .font(isTotal ? AppTheme.Typography.headline : AppTheme.Typography.body)
            .fontWeight(isTotal ? .bold : .medium)
            .foregroundColor(isTotal ? AppColors.primary(isDarkMode: isDarkMode) : AppColors.textPrimary(isDarkMode: isDarkMode))
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
