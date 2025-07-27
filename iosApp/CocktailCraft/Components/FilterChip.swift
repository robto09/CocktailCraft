import SwiftUI

struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            Text(title)
                .font(AppTheme.Typography.callout)
                .fontWeight(.medium)
                .padding(.horizontal, AppTheme.Spacing.lg)
                .padding(.vertical, AppTheme.Spacing.xs)
                .background(isSelected ? AppColors.primary : AppColors.surface)
                .foregroundColor(isSelected ? .white : AppColors.primary)
                .cornerRadius(AppTheme.CornerRadius.chip)
                .shadow(
                    color: isSelected ? AppTheme.Shadow.button.color : Color.clear,
                    radius: AppTheme.Shadow.button.radius,
                    x: AppTheme.Shadow.button.x,
                    y: AppTheme.Shadow.button.y
                )
        }
        .buttonStyle(.plain)
    }
}

struct CategoryFilterRow: View {
    let categories: [String]
    let selectedCategory: String?
    let onCategorySelected: (String?) -> Void
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: AppTheme.Spacing.sm) {
                // "All" chip
                FilterChip(
                    title: "All",
                    isSelected: selectedCategory == nil
                ) {
                    onCategorySelected(nil)
                }
                
                // Category chips
                ForEach(categories, id: \.self) { category in
                    FilterChip(
                        title: category,
                        isSelected: selectedCategory == category
                    ) {
                        onCategorySelected(category)
                    }
                }
            }
            .padding(.horizontal, AppTheme.Spacing.lg)
        }
    }
}

struct ActiveFiltersRow: View {
    let activeFilters: [String]
    let onRemoveFilter: (String) -> Void
    
    var body: some View {
        if !activeFilters.isEmpty {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: AppTheme.Spacing.sm) {
                    ForEach(activeFilters, id: \.self) { filter in
                        HStack(spacing: AppTheme.Spacing.xs) {
                            Text(filter)
                                .font(AppTheme.Typography.caption)
                            
                            Button(action: { onRemoveFilter(filter) }) {
                                Image(systemName: "xmark")
                                    .font(.system(size: 10))
                            }
                            .buttonStyle(.plain)
                        }
                        .padding(.horizontal, AppTheme.Spacing.sm)
                        .padding(.vertical, AppTheme.Spacing.xs / 2)
                        .background(AppColors.primary.opacity(0.1))
                        .foregroundColor(AppColors.primary)
                        .cornerRadius(AppTheme.CornerRadius.sm)
                    }
                }
                .padding(.horizontal, AppTheme.Spacing.lg)
            }
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        FilterChip(title: "Popular", isSelected: false) {}
        FilterChip(title: "Selected", isSelected: true) {}
        
        CategoryFilterRow(
            categories: ["Cocktail", "Shot", "Beer", "Wine"],
            selectedCategory: "Cocktail"
        ) { _ in }
        
        ActiveFiltersRow(
            activeFilters: ["Popular", "Under $15", "5+ Stars"]
        ) { _ in }
    }
    .padding()
    .background(AppColors.background)
}