import SwiftUI

import shared

struct FilterView: View {
    @ObservedObject var viewModel: HomeViewModel
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
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
                                isSelected: viewModel.selectedCategory == nil,
                                action: {
                                    viewModel.selectedCategory = nil
                                    viewModel.applyFilters()
                                }
                            )
                            
                            ForEach(["Cocktail", "Shot", "Ordinary Drink"], id: \.self) { category in
                                FilterChip(
                                    title: category,
                                    isSelected: viewModel.selectedCategory == category,
                                    action: {
                                        viewModel.selectedCategory = category
                                        viewModel.applyFilters()
                                    }
                                )
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .navigationTitle("Filters")
            .navigationBarItems(
                leading: Button("Clear") {
                    viewModel.selectedCategory = nil
                    viewModel.selectedIngredient = nil
                    viewModel.sortOption = .nameAsc
                    viewModel.applyFilters()
                },
                trailing: Button("Done") {
                    presentationMode.wrappedValue.dismiss()
                }
            )
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