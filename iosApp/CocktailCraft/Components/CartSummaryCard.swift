import SwiftUI

struct CartSummaryCard: View {
    let subtotal: Double
    let deliveryFee: Double
    let total: Double
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.lg) {
            Text("Order Summary")
                .font(AppTheme.Typography.title2)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .padding(.horizontal, AppTheme.Spacing.lg)

            VStack(spacing: 0) {
                // Subtotal
                SummaryRow(
                    label: "Subtotal",
                    value: subtotal,
                    isTotal: false
                )
                
                Divider()
                    .padding(.horizontal, AppTheme.Spacing.lg)

                // Delivery Fee
                SummaryRow(
                    label: "Delivery Fee",
                    value: deliveryFee,
                    isTotal: false
                )
                
                Divider()
                    .padding(.horizontal, AppTheme.Spacing.lg)

                // Total
                SummaryRow(
                    label: "Total",
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
        HStack {
            Text(label)
                .font(isTotal ? AppTheme.Typography.headline : AppTheme.Typography.body)
                .fontWeight(isTotal ? .bold : .medium)
                .foregroundColor(isTotal ? AppColors.textPrimary(isDarkMode: isDarkMode) : AppColors.textSecondary(isDarkMode: isDarkMode))
            
            Spacer()
            
            Text(value.asPrice)
                .font(isTotal ? AppTheme.Typography.headline : AppTheme.Typography.body)
                .fontWeight(isTotal ? .bold : .medium)
                .foregroundColor(isTotal ? AppColors.primary(isDarkMode: isDarkMode) : AppColors.textPrimary(isDarkMode: isDarkMode))
        }
        .padding(.horizontal, AppTheme.Spacing.lg)
        .padding(.vertical, AppTheme.Spacing.md)
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
