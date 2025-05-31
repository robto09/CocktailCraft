import SwiftUI

struct CategoryFilterRow: View {
    @Binding var selectedCategory: String?
    let onCategorySelected: (String?) -> Void
    
    let categories = [
        "All",
        "Cocktail",
        "Shot",
        "Coffee / Tea",
        "Beer",
        "Soft Drink",
        "Other / Unknown"
    ]
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(categories, id: \.self) { category in
                    CategoryChip(
                        title: category,
                        isSelected: (category == "All" && selectedCategory == nil) || selectedCategory == category,
                        action: {
                            if category == "All" {
                                selectedCategory = nil
                                onCategorySelected(nil)
                            } else {
                                selectedCategory = category
                                onCategorySelected(category)
                            }
                        }
                    )
                }
            }
            .padding(.horizontal)
        }
    }
}

struct CategoryChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .fontWeight(isSelected ? .semibold : .regular)
                .foregroundColor(isSelected ? .white : .primary)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(
                    Capsule()
                        .fill(isSelected ? Color.accentColor : Color(.systemGray6))
                )
        }
        .animation(.easeInOut(duration: 0.2), value: isSelected)
    }
}