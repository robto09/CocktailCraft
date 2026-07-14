import SwiftUI

/// Horizontal category filter chips ("All" plus the curated categories).
struct CategoryChipRow: View {
    let categories: [String]
    let selectedCategory: String?
    let onSelectAll: () -> Void
    let onSelect: (String) -> Void
    @Environment(\.isDarkMode) private var isDarkMode

    private var primaryColor: Color { AppColors.primary(isDarkMode: isDarkMode) }
    private var surfaceColor: Color { AppColors.surface(isDarkMode: isDarkMode) }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                chip("All", isSelected: selectedCategory == nil, action: onSelectAll)

                ForEach(categories, id: \.self) { category in
                    chip(category, isSelected: selectedCategory == category) {
                        onSelect(category)
                    }
                }
            }
            .padding(.horizontal, 16)
        }
    }

    private func chip(_ label: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(label, action: action)
            .padding(.horizontal, 16)
            .padding(.vertical, 6)
            .background(isSelected ? primaryColor : surfaceColor)
            .foregroundColor(isSelected ? .white : primaryColor)
            .cornerRadius(16)
            .shadow(color: isSelected ? Color.black.opacity(0.15) : Color.clear, radius: 2, x: 0, y: 1)
    }
}
