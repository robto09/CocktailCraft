import SwiftUI
import shared

struct OrderItemView: View {
    let order: Order
    
    private var orderNumber: String {
        String(order.id.suffix(5))
    }
    
    private var statusColor: Color {
        switch order.status {
        case "Completed":
            return Color(red: 0.298, green: 0.686, blue: 0.314) // Green
        case "In Progress":
            return Color(red: 1.0, green: 0.627, blue: 0) // Orange
        default:
            return .gray
        }
    }
    
    private var currencyFormatter: NumberFormatter {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = Locale(identifier: "en_US")
        return formatter
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Order header
            HStack {
                Text("Order #\(orderNumber)")
                    .font(.system(size: 16, weight: .bold))
                
                Spacer()
                
                Text(order.date)
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
            }
            
            // Order items
            VStack(alignment: .leading, spacing: 8) {
                ForEach(order.items, id: \.self) { item in
                    HStack {
                        Text("\(item.quantity)x \(item.name)")
                            .font(.system(size: 14))
                        
                        Spacer()
                        
                        Text(currencyFormatter.string(from: NSNumber(value: item.price * Double(item.quantity))) ?? "$0.00")
                            .font(.system(size: 14, weight: .medium))
                    }
                }
            }
            
            Divider()
                .padding(.vertical, 4)
            
            // Total row
            HStack {
                Text("Total")
                    .font(.system(size: 16, weight: .bold))
                
                Spacer()
                
                Text(currencyFormatter.string(from: NSNumber(value: order.total)) ?? "$0.00")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.blue)
            }
            
            // Order status
            HStack(spacing: 8) {
                Circle()
                    .fill(statusColor)
                    .frame(width: 10, height: 10)
                
                Text(order.status)
                    .font(.system(size: 14))
                    .foregroundColor(statusColor)
            }
        }
        .padding()
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }
}