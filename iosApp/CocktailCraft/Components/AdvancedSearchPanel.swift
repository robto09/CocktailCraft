import SwiftUI

struct AdvancedSearchPanel: View {
    @Binding var isPresented: Bool
    @State private var selectedCategory: String?
    @State private var selectedSort = "nameAsc"
    @State private var priceRange: ClosedRange<Double> = 0...50
    @State private var selectedTaste: String?
    @State private var selectedComplexity: String?
    @State private var selectedPrepTime: String?
    
    let categories = ["Cocktail", "Shot", "Beer", "Wine", "Ordinary Drink", "Punch/Party Drink"]
    let sortOptions = [
        ("nameAsc", "Name (A-Z)"),
        ("nameDesc", "Name (Z-A)"),
        ("priceAsc", "Price (Low to High)"),
        ("priceDesc", "Price (High to Low)"),
        ("rating", "Rating"),
        ("popularity", "Popularity")
    ]
    let tasteProfiles = ["Sweet", "Sour", "Bitter", "Spicy", "Fruity", "Herbal", "Creamy"]
    let complexityLevels = ["Easy", "Medium", "Complex"]
    let prepTimes = ["Quick", "Medium", "Long"]
    
    let onApplyFilters: (AdvancedSearchFilters) -> Void
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: AppTheme.Spacing.xl) {
                    
                    // Category Section
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
                        Text("Category")
                            .font(AppTheme.Typography.headline)
                            .foregroundColor(AppColors.textPrimary)
                        
                        LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 2), spacing: AppTheme.Spacing.sm) {
                            ForEach(categories, id: \.self) { category in
                                FilterChip(
                                    title: category,
                                    isSelected: selectedCategory == category
                                ) {
                                    selectedCategory = selectedCategory == category ? nil : category
                                }
                            }
                        }
                    }
                    
                    // Sort Section
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
                        Text("Sort By")
                            .font(AppTheme.Typography.headline)
                            .foregroundColor(AppColors.textPrimary)
                        
                        VStack(spacing: AppTheme.Spacing.xs) {
                            ForEach(sortOptions, id: \.0) { option in
                                HStack {
                                    Text(option.1)
                                        .font(AppTheme.Typography.body)
                                        .foregroundColor(AppColors.textPrimary)
                                    
                                    Spacer()
                                    
                                    if selectedSort == option.0 {
                                        Image(systemName: "checkmark")
                                            .foregroundColor(AppColors.primary)
                                    }
                                }
                                .contentShape(Rectangle())
                                .onTapGesture {
                                    selectedSort = option.0
                                }
                                .padding(.vertical, AppTheme.Spacing.xs)
                            }
                        }
                    }
                    
                    // Price Range Section
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
                        Text("Price Range")
                            .font(AppTheme.Typography.headline)
                            .foregroundColor(AppColors.textPrimary)
                        
                        VStack(spacing: AppTheme.Spacing.sm) {
                            HStack {
                                Text("$\(Int(priceRange.lowerBound))")
                                    .font(AppTheme.Typography.callout)
                                    .foregroundColor(AppColors.textSecondary)
                                
                                Spacer()
                                
                                Text("$\(Int(priceRange.upperBound))")
                                    .font(AppTheme.Typography.callout)
                                    .foregroundColor(AppColors.textSecondary)
                            }
                            
                            // Note: Custom range slider would be ideal, but using simple representation
                            HStack {
                                Button("$0-15") {
                                    priceRange = 0...15
                                }
                                .chipStyle(isSelected: priceRange == 0...15)
                                
                                Button("$15-30") {
                                    priceRange = 15...30
                                }
                                .chipStyle(isSelected: priceRange == 15...30)
                                
                                Button("$30+") {
                                    priceRange = 30...50
                                }
                                .chipStyle(isSelected: priceRange == 30...50)
                            }
                        }
                    }
                    
                    // Taste Profile Section
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
                        Text("Taste Profile")
                            .font(AppTheme.Typography.headline)
                            .foregroundColor(AppColors.textPrimary)
                        
                        LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 3), spacing: AppTheme.Spacing.sm) {
                            ForEach(tasteProfiles, id: \.self) { taste in
                                FilterChip(
                                    title: taste,
                                    isSelected: selectedTaste == taste
                                ) {
                                    selectedTaste = selectedTaste == taste ? nil : taste
                                }
                            }
                        }
                    }
                    
                    // Complexity Section
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
                        Text("Complexity")
                            .font(AppTheme.Typography.headline)
                            .foregroundColor(AppColors.textPrimary)
                        
                        HStack(spacing: AppTheme.Spacing.sm) {
                            ForEach(complexityLevels, id: \.self) { complexity in
                                FilterChip(
                                    title: complexity,
                                    isSelected: selectedComplexity == complexity
                                ) {
                                    selectedComplexity = selectedComplexity == complexity ? nil : complexity
                                }
                            }
                        }
                    }
                    
                    // Preparation Time Section
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
                        Text("Preparation Time")
                            .font(AppTheme.Typography.headline)
                            .foregroundColor(AppColors.textPrimary)
                        
                        HStack(spacing: AppTheme.Spacing.sm) {
                            ForEach(prepTimes, id: \.self) { time in
                                FilterChip(
                                    title: time,
                                    isSelected: selectedPrepTime == time
                                ) {
                                    selectedPrepTime = selectedPrepTime == time ? nil : time
                                }
                            }
                        }
                    }
                }
                .padding(AppTheme.Spacing.lg)
            }
            .background(AppColors.background)
            .navigationTitle("Advanced Search")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Clear") {
                        clearFilters()
                    }
                    .foregroundColor(AppColors.primary)
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Apply") {
                        let filters = AdvancedSearchFilters(
                            category: selectedCategory,
                            sortOption: selectedSort,
                            priceRange: priceRange,
                            tasteProfile: selectedTaste,
                            complexity: selectedComplexity,
                            preparationTime: selectedPrepTime
                        )
                        onApplyFilters(filters)
                        isPresented = false
                    }
                    .foregroundColor(AppColors.primary)
                    .fontWeight(.semibold)
                }
            }
        }
    }
    
    private func clearFilters() {
        selectedCategory = nil
        selectedSort = "nameAsc"
        priceRange = 0...50
        selectedTaste = nil
        selectedComplexity = nil
        selectedPrepTime = nil
    }
}

struct AdvancedSearchFilters {
    let category: String?
    let sortOption: String
    let priceRange: ClosedRange<Double>
    let tasteProfile: String?
    let complexity: String?
    let preparationTime: String?
}

#Preview {
    AdvancedSearchPanel(
        isPresented: .constant(true),
        onApplyFilters: { _ in }
    )
}