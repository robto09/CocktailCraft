import SwiftUI

import shared

struct FilterView: View {
    // @Bindable: the Picker below needs a $viewModel.sortOption binding
    @Bindable var viewModel: HomeViewModelSKIE
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("Sort By")) {
                    Picker("Sort", selection: $viewModel.sortOption) {
                        Text("Name (A-Z)").tag(SortOption.nameAsc)
                        Text("Name (Z-A)").tag(SortOption.nameDesc)
                    }
                    .pickerStyle(SegmentedPickerStyle())
                }
                
                Section(header: Text("Category")) {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack {
                            FilterChip(
                                title: "All",
                                isSelected: viewModel.state.selectedCategory == nil,
                                action: {
                                    viewModel.setCategory(nil)
                                }
                            )
                            
                            ForEach(["Cocktail", "Shot", "Ordinary Drink"], id: \.self) { category in
                                FilterChip(
                                    title: category,
                                    isSelected: viewModel.state.selectedCategory == category,
                                    action: {
                                        viewModel.setCategory(category)
                                    }
                                )
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .navigationTitle("Filters")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Clear") {
                        viewModel.selectedIngredient = nil
                        viewModel.sortOption = SortOption.nameAsc
                        viewModel.setCategory(nil)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}

struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color.blue : Color.gray.opacity(0.2))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(20)
        }
    }
}