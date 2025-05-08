import SwiftUI
import shared

struct AdvancedSearchView: View {
    @State private var filters: SearchFilters
    let onApply: (SearchFilters) -> Void
    @Environment(\.dismiss) private var dismiss
    
    // Local state for filter values
    @State private var minPrice: Double?
    @State private var maxPrice: Double?
    @State private var minRating: Float?
    @State private var selectedCategories: Set<String> = []
    @State private var selectedIngredients: Set<String> = []
    @State private var isAlcoholic: Bool?
    @State private var sortOption: SortOption = .NONE
    @State private var sortDirection: SortDirection = .ASC
    
    // Available options
    private let categories = ["Martini", "Margarita", "Mojito", "Old Fashioned", "Cocktail", "Shot"]
    private let ingredients = ["Vodka", "Gin", "Rum", "Tequila", "Whiskey", "Triple Sec", "Lime", "Mint"]
    
    init(filters: SearchFilters, onApply: @escaping (SearchFilters) -> Void) {
        self.onApply = onApply
        _filters = State(initialValue: filters)
        
        // Initialize local state from filters
        _minPrice = State(initialValue: filters.minPrice)
        _maxPrice = State(initialValue: filters.maxPrice)
        _minRating = State(initialValue: filters.minRating)
        _selectedCategories = State(initialValue: Set(filters.categories))
        _selectedIngredients = State(initialValue: Set(filters.ingredients))
        _isAlcoholic = State(initialValue: filters.isAlcoholic)
        _sortOption = State(initialValue: filters.sortOption)
        _sortDirection = State(initialValue: filters.sortDirection)
    }
    
    var body: some View {
        NavigationView {
            Form {
                // Price Range Section
                Section(header: Text("Price Range")) {
                    HStack {
                        Text("Min:")
                        TextField("Min Price", value: $minPrice, format: .currency(code: "USD"))
                            .keyboardType(.decimalPad)
                    }
                    HStack {
                        Text("Max:")
                        TextField("Max Price", value: $maxPrice, format: .currency(code: "USD"))
                            .keyboardType(.decimalPad)
                    }
                }
                
                // Rating Section
                Section(header: Text("Minimum Rating")) {
                    if let rating = minRating {
                        HStack {
                            Slider(value: Binding(get: { rating }, set: { minRating = $0 }), in: 0...5, step: 0.5)
                            Text(String(format: "%.1f ★", rating))
                        }
                    } else {
                        HStack {
                            Slider(value: Binding(get: { 0 }, set: { minRating = $0 }), in: 0...5, step: 0.5)
                            Text("Any")
                        }
                    }
                }
                
                // Categories Section
                Section(header: Text("Categories")) {
                    ForEach(categories, id: \.self) { category in
                        Toggle(category, isOn: Binding(
                            get: { selectedCategories.contains(category) },
                            set: { isSelected in
                                if isSelected {
                                    selectedCategories.insert(category)
                                } else {
                                    selectedCategories.remove(category)
                                }
                            }
                        ))
                    }
                }
                
                // Ingredients Section
                Section(header: Text("Ingredients")) {
                    ForEach(ingredients, id: \.self) { ingredient in
                        Toggle(ingredient, isOn: Binding(
                            get: { selectedIngredients.contains(ingredient) },
                            set: { isSelected in
                                if isSelected {
                                    selectedIngredients.insert(ingredient)
                                } else {
                                    selectedIngredients.remove(ingredient)
                                }
                            }
                        ))
                    }
                }
                
                // Alcoholic Toggle
                Section(header: Text("Alcohol Content")) {
                    Picker("Contains Alcohol", selection: $isAlcoholic) {
                        Text("Any").tag(Optional<Bool>.none)
                        Text("Alcoholic").tag(Optional<Bool>.some(true))
                        Text("Non-Alcoholic").tag(Optional<Bool>.some(false))
                    }
                }
                
                // Sort Options
                Section(header: Text("Sort By")) {
                    Picker("Sort Option", selection: $sortOption) {
                        Text("None").tag(SortOption.NONE)
                        Text("Price").tag(SortOption.PRICE)
                        Text("Rating").tag(SortOption.RATING)
                        Text("Popularity").tag(SortOption.POPULARITY)
                        Text("Name").tag(SortOption.NAME)
                    }
                    
                    if sortOption != .NONE {
                        Picker("Sort Direction", selection: $sortDirection) {
                            Text("Ascending").tag(SortDirection.ASC)
                            Text("Descending").tag(SortDirection.DESC)
                        }
                        .pickerStyle(.segmented)
                    }
                }
                
                // Reset Button
                Section {
                    Button("Reset Filters", role: .destructive) {
                        resetFilters()
                    }
                }
            }
            .navigationTitle("Advanced Search")
            .navigationBarItems(
                leading: Button("Cancel") {
                    dismiss()
                },
                trailing: Button("Apply") {
                    applyFilters()
                }
            )
        }
    }
    
    private func resetFilters() {
        minPrice = nil
        maxPrice = nil
        minRating = nil
        selectedCategories.removeAll()
        selectedIngredients.removeAll()
        isAlcoholic = nil
        sortOption = .NONE
        sortDirection = .ASC
    }
    
    private func applyFilters() {
        let newFilters = SearchFilters(
            categories: Array(selectedCategories),
            minPrice: minPrice,
            maxPrice: maxPrice,
            minRating: minRating,
            ingredients: Array(selectedIngredients),
            isAlcoholic: isAlcoholic,
            sortOption: sortOption,
            sortDirection: sortDirection
        )
        onApply(newFilters)
        dismiss()
    }
}