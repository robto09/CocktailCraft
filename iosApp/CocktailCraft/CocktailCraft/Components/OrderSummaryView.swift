import SwiftUI

struct OrderSummaryView: View {
    let subtotal: Double
    let deliveryFee: Double
    let total: Double?
    let showDeliveryFee: Bool
    let additionalItems: [(String, Double)]?
    
    private var calculatedTotal: Double {
        total ?? (subtotal + deliveryFee)
    }
    
    private var currencyFormatter: NumberFormatter {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = Locale(identifier: "en_US")
        return formatter
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Order Summary")
                .font(.system(size: 18, weight: .bold))
            
            VStack(spacing: 8) {
                // Subtotal
                SummaryRow(
                    label: "Subtotal",
                    value: currencyFormatter.string(from: NSNumber(value: subtotal)) ?? "$0.00"
                )
                
                // Delivery Fee
                if showDeliveryFee {
                    SummaryRow(
                        label: "Delivery Fee",
                        value: currencyFormatter.string(from: NSNumber(value: deliveryFee)) ?? "$0.00"
                    )
                }
                
                // Additional Items
                if let additionalItems = additionalItems {
                    ForEach(additionalItems, id: \.0) { item in
                        SummaryRow(
                            label: item.0,
                            value: currencyFormatter.string(from: NSNumber(value: item.1)) ?? "$0.00"
                        )
                    }
                }
            }
            
            Divider()
                .padding(.vertical, 4)
            
            // Total
            HStack {
                Text("Total")
                    .font(.system(size: 16, weight: .bold))
                
                Spacer()
                
                Text(currencyFormatter.string(from: NSNumber(value: calculatedTotal)) ?? "$0.00")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.blue)
            }
        }
        .padding()
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }
}

struct SummaryRow: View {
    let label: String
    let value: String
    
    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 15))
                .foregroundColor(.secondary)
            
            Spacer()
            
            Text(value)
                .font(.system(size: 15, weight: .medium))
        }
    }
}