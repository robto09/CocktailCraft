import SwiftUI
import Shared

struct AdvancedSearchView: View {
    let filters: SearchFilters
    let onApply: (SearchFilters) -> Void
    let onCancel: () -> Void
    
    @State private var category: String = ""
    @State private var selectedIngredients: Set<String> = []
    @State private var glass: String = ""
    @State private var minPrice: Double = 0
    @State private var maxPrice: Double = 100
    @State private var alcoholic: Bool? = nil
    
    let categories = ["Cocktail", "Shot", "Coffee / Tea", "Beer", "Soft Drink", "Other / Unknown"]
    let glassTypes = ["Highball glass", "Cocktail glass", "Old-fashioned glass", "Whiskey Glass", "Collins glass", "Champagne flute", "Margarita glass", "Shot glass", "Coffee mug", "Mason jar", "Beer mug", "Wine Glass"]
    let commonIngredients = ["Vodka", "Gin", "Rum", "Tequila", "Whiskey", "Bourbon", "Lime juice", "Lemon juice", "Sugar", "Simple syrup", "Grenadine", "Orange juice", "Cranberry juice", "Ginger beer", "Soda water", "Tonic water"]
    
    var body: some View {
        NavigationView {
            Form {
                // Category Section
                Section("Category") {
                    Picker("Category", selection: $category) {
                        Text("Any").tag("")
                        ForEach(categories, id: \.self) { cat in
                            Text(cat).tag(cat)
                        }
                    }
                    .pickerStyle(.menu)
                }
                
                // Ingredients Section
                Section("Ingredients") {
                    ScrollView {
                        LazyVGrid(columns: [GridItem(.adaptive(minimum: 100))], spacing: 8) {
                            ForEach(commonIngredients, id: \.self) { ingredient in
                                IngredientChip(
                                    ingredient: ingredient,
                                    isSelected: selectedIngredients.contains(ingredient),
                                    onToggle: {
                                        if selectedIngredients.contains(ingredient) {
                                            selectedIngredients.remove(ingredient)
                                        } else {
                                            selectedIngredients.insert(ingredient)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    .frame(maxHeight: 200)
                }
                
                // Glass Type Section
                Section("Glass Type") {
                    Picker("Glass Type", selection: $glass) {
                        Text("Any").tag("")
                        ForEach(glassTypes, id: \.self) { glassType in
                            Text(glassType).tag(glassType)
                        }
                    }
                    .pickerStyle(.menu)
                }
                
                // Price Range Section
                Section("Price Range") {
                    VStack {
                        HStack {
                            Text("$\(Int(minPrice))")
                            Spacer()
                            Text("$\(Int(maxPrice))")
                        }
                        .font(.caption)
                        .foregroundColor(.secondary)
                        
                        HStack {
                            Slider(value: $minPrice, in: 0...maxPrice - 1, step: 1)
                            Text("-")
                            Slider(value: $maxPrice, in: minPrice + 1...100, step: 1)
                        }
                    }
                }
                
                // Alcoholic Section
                Section("Type") {
                    Picker("Type", selection: $alcoholic) {
                        Text("Any").tag(nil as Bool?)
                        Text("Alcoholic").tag(true as Bool?)
                        Text("Non-Alcoholic").tag(false as Bool?)
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("Advanced Search")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel", action: onCancel)
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Apply") {
                        let newFilters = SearchFilters(
                            query: filters.query,
                            category: category.isEmpty ? nil : category,
                            ingredient: nil,
                            alcoholic: alcoholic,
                            glass: glass.isEmpty ? nil : glass,
                            priceRange: minPrice > 0 || maxPrice < 100 ? Float(minPrice)...Float(maxPrice) : nil,
                            ingredients: Array(selectedIngredients),
                            excludeIngredients: [],
                            tasteProfile: nil,
                            complexity: nil,
                            preparationTime: nil
                        )
                        onApply(newFilters)
                    }
                    .fontWeight(.semibold)
                }
            }
        }
        .onAppear {
            // Initialize with current filters
            category = filters.category ?? ""
            selectedIngredients = Set(filters.ingredients)
            glass = filters.glass ?? ""
            if let priceRange = filters.priceRange {
                minPrice = Double(priceRange.lowerBound)
                maxPrice = Double(priceRange.upperBound)
            } else {
                minPrice = 0
                maxPrice = 100
            }
            alcoholic = filters.alcoholic
        }
    }
}

struct IngredientChip: View {
    let ingredient: String
    let isSelected: Bool
    let onToggle: () -> Void
    
    var body: some View {
        Button(action: onToggle) {
            Text(ingredient)
                .font(.caption)
                .foregroundColor(isSelected ? .white : .primary)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(
                    Capsule()
                        .fill(isSelected ? Color.accentColor : Color(.systemGray6))
                )
        }
    }
}