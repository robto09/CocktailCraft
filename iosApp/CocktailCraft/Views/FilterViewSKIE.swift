import SwiftUI
import shared

// MARK: - Filter View using SKIE

struct FilterViewSKIE: View {
    let viewModel: HomeViewModelSKIE
    @Environment(\.dismiss) private var dismiss
    @State private var selectedCategory: String? = nil
    @State private var selectedSort = "nameAsc"
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Category") {
                    ForEach(viewModel.getCategories(), id: \.self) { category in
                        Button(action: {
                            selectedCategory = category
                        }) {
                            HStack {
                                Text(category)
                                    .foregroundColor(.primary)
                                Spacer()
                                if selectedCategory == category {
                                    Image(systemName: "checkmark")
                                        .foregroundColor(.blue)
                                }
                            }
                        }
                    }
                }
                
                Section("Sort By") {
                    Picker("Sort", selection: $selectedSort) {
                        Text("Name (A-Z)").tag("nameAsc")
                        Text("Name (Z-A)").tag("nameDesc")
                        Text("Price (Low to High)").tag("priceAsc")
                        Text("Price (High to Low)").tag("priceDesc")
                        Text("Rating").tag("rating")
                        Text("Popularity").tag("popularity")
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("Filters")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Clear") {
                        selectedCategory = nil
                        selectedSort = "nameAsc"
                        viewModel.clearSearch()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Apply") {
                        Task {
                            // Apply category filter
                            if let category = selectedCategory {
                                await viewModel.loadCocktailsByCategory(category)
                            }
                            
                            // Apply sort
                            switch selectedSort {
                            case "nameDesc":
                                // Implement name descending sort
                                break
                            case "priceAsc":
                                await viewModel.sortByPrice(ascending: true)
                            case "priceDesc":
                                await viewModel.sortByPrice(ascending: false)
                            case "rating":
                                await viewModel.sortByRating()
                            case "popularity":
                                await viewModel.sortByPopularity()
                            default:
                                break
                            }
                            
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}
