import SwiftUI
import shared

struct OrderCard: View {
    let order: Order
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
            // Header with Order ID and Status
            HStack {
                Text("Order #\(order.id)")
                    .font(AppTheme.Typography.headline)
                    .fontWeight(.semibold)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                
                Spacer()
                
                OrderStatusBadge(status: order.status)
            }

            // Date
            Text("Date: \(order.date)")
                .font(AppTheme.Typography.subheadline)
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))

            // Items Section
            VStack(alignment: .leading, spacing: 4) {
                Text("Items:")
                    .font(AppTheme.Typography.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                ForEach(order.items, id: \.name) { item in
                    OrderItemRow(item: item)
                }
            }

            Divider()

            // Total
            HStack {
                Text("Total:")
                    .font(AppTheme.Typography.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                
                Spacer()
                
                Text(order.total.asPrice)
                    .font(AppTheme.Typography.subheadline)
                    .fontWeight(.bold)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
            }
        }
        .padding(AppTheme.Spacing.lg)
        .cardStyle()
    }
}

struct OrderStatusBadge: View {
    let status: String
    
    var body: some View {
        Text(status)
            .font(AppTheme.Typography.caption)
            .padding(.horizontal, AppTheme.Spacing.sm)
            .padding(.vertical, 4)
            .background(statusBackgroundColor)
            .foregroundColor(statusTextColor)
            .cornerRadius(AppTheme.CornerRadius.sm)
    }
    
    private var statusBackgroundColor: Color {
        switch status.lowercased() {
        case "processing":
            return AppColors.warning.opacity(0.2)
        case "delivered":
            return AppColors.success.opacity(0.2)
        case "cancelled":
            return AppColors.error.opacity(0.2)
        default:
            return Color.blue.opacity(0.2)
        }
    }
    
    private var statusTextColor: Color {
        switch status.lowercased() {
        case "processing":
            return AppColors.warning
        case "delivered":
            return AppColors.success
        case "cancelled":
            return AppColors.error
        default:
            return .blue
        }
    }
}

struct OrderItemRow: View {
    let item: OrderItem
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        HStack {
            Text("\(item.quantity)x \(item.name)")
                .font(AppTheme.Typography.caption)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
            
            Spacer()
            
            Text((item.price * Double(item.quantity)).asPrice)
                .font(AppTheme.Typography.caption)
                .fontWeight(.medium)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
        }
    }
}

#Preview {
    OrderCard(
        order: Order(
            id: "12345",
            date: "2023-12-01",
            status: "Processing",
            items: [
                OrderItem(name: "Classic Margarita", quantity: 2, price: 12.99),
                OrderItem(name: "Mojito", quantity: 1, price: 11.50)
            ],
            total: 37.48
        )
    )
    .environment(\.isDarkMode, false)
    .padding()
}
