import SwiftUI
import shared

struct SearchFilterChips: View {
    let filters: SearchFilters
    let onRemove: (String) -> Void
    let onClearAll: () -> Void
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                // Clear All Button
                Button(action: onClearAll) {
                    Label("Clear All", systemImage: "xmark.circle.fill")
                        .font(.caption)
                        .foregroundColor(.red)
                }
                .padding(.trailing, 4)
                
                // Category Filter
                if let category = filters.category, !category.isEmpty {
                    RemovableFilterChip(
                        label: "Category: \(category)",
                        onRemove: { onRemove("category") }
                    )
                }
                
                // Ingredient Filters
                ForEach(filters.ingredients, id: \.self) { ingredient in
                    RemovableFilterChip(
                        label: ingredient,
                        onRemove: { onRemove("ingredient:\(ingredient)") }
                    )
                }
                
                // Glass Type Filter
                if let glass = filters.glass, !glass.isEmpty {
                    RemovableFilterChip(
                        label: "Glass: \(glass)",
                        onRemove: { onRemove("glass") }
                    )
                }
                
                // Price Range Filter
                if let priceRange = filters.priceRange {
                    RemovableFilterChip(
                        label: {
                            if let start = priceRange.start as? Float,
                               let end = priceRange.endInclusive as? Float {
                                return "Price: $\(Int(start))-$\(Int(end))"
                            } else {
                                return "Price Range"
                            }
                        }(),
                        onRemove: { onRemove("price") }
                    )
                }
                
                // Is Alcoholic Filter
                if let alcoholic = filters.alcoholic {
                    RemovableFilterChip(
                        label: alcoholic.boolValue ? "Alcoholic" : "Non-Alcoholic",
                        onRemove: { onRemove("alcoholic") }
                    )
                }
            }
            .padding(.horizontal)
        }
    }
}

struct RemovableFilterChip: View {
    let label: String
    let onRemove: () -> Void
    
    var body: some View {
        HStack(spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundColor(.primary)
            
            Button(action: onRemove) {
                Image(systemName: "xmark.circle.fill")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(Color(.systemGray6))
        .cornerRadius(15)
    }
}