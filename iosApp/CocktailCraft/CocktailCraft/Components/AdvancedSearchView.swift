import SwiftUI
import shared

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
    
    private let categories = ["Cocktail", "Shot", "Coffee / Tea", "Beer", "Soft Drink", "Other / Unknown"]
    private let glassTypes = ["Highball glass", "Cocktail glass", "Old-fashioned glass", "Whiskey Glass", "Collins glass", "Champagne flute", "Margarita glass", "Shot glass", "Coffee mug", "Mason jar", "Beer mug", "Wine Glass"]
    private let commonIngredients = ["Vodka", "Gin", "Rum", "Tequila", "Whiskey", "Bourbon", "Lime juice", "Lemon juice", "Sugar", "Simple syrup", "Grenadine", "Orange juice", "Cranberry juice", "Ginger beer", "Soda water", "Tonic water"]
    
    var body: some View {
        NavigationView {
            scrollContent
                .navigationTitle("Advanced Search")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarLeading) {
                        Button("Cancel", action: onCancel)
                    }
                    
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button("Apply", action: applyFilters)
                            .fontWeight(.semibold)
                    }
                }
        }
        .onAppear(perform: initializeFilters)
    }
    
    private var scrollContent: some View {
        ScrollView {
            LazyVStack(spacing: 20) {
                categorySection
                ingredientsSection
                glassSection
                priceSection
                typeSection
            }
            .padding()
        }
    }
    
    private var categorySection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Category")
                .font(.headline)
            
            Picker("Category", selection: $category) {
                Text("Any").tag("")
                ForEach(categories, id: \.self) { cat in
                    Text(cat).tag(cat)
                }
            }
            .pickerStyle(.menu)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
    
    private var ingredientsSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Ingredients")
                .font(.headline)
            
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 100))], spacing: 8) {
                ForEach(commonIngredients, id: \.self) { ingredient in
                    ingredientChip(ingredient: ingredient)
                }
            }
            .frame(maxHeight: 200)
        }
    }
    
    private func ingredientChip(ingredient: String) -> some View {
        Button(action: {
            toggleIngredient(ingredient)
        }) {
            Text(ingredient)
                .font(.caption)
                .foregroundColor(selectedIngredients.contains(ingredient) ? .white : .primary)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(
                    Capsule()
                        .fill(selectedIngredients.contains(ingredient) ? Color.accentColor : Color(.systemGray6))
                )
        }
    }
    
    private var glassSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Glass Type")
                .font(.headline)
            
            Picker("Glass Type", selection: $glass) {
                Text("Any").tag("")
                ForEach(glassTypes, id: \.self) { glassType in
                    Text(glassType).tag(glassType)
                }
            }
            .pickerStyle(.menu)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
    
    private var priceSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Price Range")
                .font(.headline)
            
            VStack(spacing: 12) {
                HStack {
                    Text("$\(Int(minPrice))")
                    Spacer()
                    Text("$\(Int(maxPrice))")
                }
                .font(.caption)
                .foregroundColor(.secondary)
                
                priceSliders
            }
        }
    }
    
    private var priceSliders: some View {
        VStack(spacing: 8) {
            HStack {
                Text("Min:")
                Slider(value: $minPrice, in: 0...99, step: 1)
            }
            
            HStack {
                Text("Max:")
                Slider(value: $maxPrice, in: 1...100, step: 1)
            }
        }
    }
    
    private var typeSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Type")
                .font(.headline)
            
            Picker("Type", selection: $alcoholic) {
                Text("Any").tag(nil as Bool?)
                Text("Alcoholic").tag(true as Bool?)
                Text("Non-Alcoholic").tag(false as Bool?)
            }
            .pickerStyle(.segmented)
        }
    }
    
    private func toggleIngredient(_ ingredient: String) {
        if selectedIngredients.contains(ingredient) {
            selectedIngredients.remove(ingredient)
        } else {
            selectedIngredients.insert(ingredient)
        }
    }
    
    private func applyFilters() {
        let alcoholicValue: KotlinBoolean? = {
            if let alcoholic = alcoholic {
                return KotlinBoolean(bool: alcoholic)
            }
            return nil
        }()
        
        let newFilters = SearchFilters(
            query: filters.query,
            category: category.isEmpty ? nil : category,
            ingredient: nil,
            alcoholic: alcoholicValue,
            glass: glass.isEmpty ? nil : glass,
            priceRange: createPriceRange(),
            ingredients: Array(selectedIngredients),
            excludeIngredients: [],
            tasteProfile: nil,
            complexity: nil,
            preparationTime: nil
        )
        onApply(newFilters)
    }
    
    private func createPriceRange() -> (any KotlinClosedFloatingPointRange)? {
        // For now, returning nil as creating KotlinClosedFloatingPointRange requires more complex setup
        // This can be implemented later when we have a proper range factory method
        return nil
    }
    
    private func initializeFilters() {
        category = filters.category ?? ""
        selectedIngredients = Set(filters.ingredients)
        glass = filters.glass ?? ""
        
        // Convert SharedBoolean? to Bool?
        if let sharedBool = filters.alcoholic {
            alcoholic = sharedBool.boolValue
        } else {
            alcoholic = nil
        }
        
        // For now, use default price range since KotlinClosedFloatingPointRange is complex
        minPrice = 0
        maxPrice = 100
    }
}