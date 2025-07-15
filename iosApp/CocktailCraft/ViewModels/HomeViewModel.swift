import SwiftUI

import shared
import Combine

class HomeViewModel: ObservableObject {
    @Published var cocktails: [Cocktail] = []
    @Published var filteredCocktails: [Cocktail] = []
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    @Published var selectedCategory: String? = nil
    @Published var selectedIngredient: String? = nil
    @Published var sortOption: SortOption = .nameAsc
    
    private let repository: CocktailRepository?
    private var cancellables = Set<AnyCancellable>()

    init() {
        self.repository = KoinInitializer.shared.getCocktailRepository()
        // Load mock data for now since repository is nil
        loadMockData()
    }
    
    func loadCocktails() {
        isLoading = true
        error = nil
        
        // For now, use sample data while we set up proper Flow collection
        // TODO: Implement proper Flow collection from repository
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            self.cocktails = self.getSampleCocktails()
            self.filteredCocktails = self.cocktails
            self.applySorting()
            self.isLoading = false
        }
    }
    
    private func getSampleCocktails() -> [Cocktail] {
        // Sample cocktails for testing
        return [
            Cocktail(
                id: "11007",
                name: "Margarita",
                alternateName: nil,
                tags: ["IBA", "Contemporary Classic"],
                category: "Ordinary Drink",
                iba: "Contemporary Classic",
                alcoholic: "Alcoholic",
                glass: "Cocktail glass",
                instructions: "Rub the rim of the glass with the lime slice to make the salt stick to it. Take care to moisten only the outer rim and sprinkle the salt on it. The salt should present to the lips of the imbiber and never mix into the cocktail. Shake the other ingredients with ice, then carefully pour into the glass.",
                imageUrl: "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg",
                ingredients: [
                    CocktailIngredient(name: "Tequila", measure: "1 1/2 oz"),
                    CocktailIngredient(name: "Triple sec", measure: "1/2 oz"),
                    CocktailIngredient(name: "Lime juice", measure: "1 oz"),
                    CocktailIngredient(name: "Salt", measure: "")
                ],
                imageSource: "https://commons.wikimedia.org/wiki/File:Klassiche_Margarita.jpg",
                imageAttribution: "Cocktailmarler",
                creativeCommonsConfirmed: true,
                dateModified: "2015-08-18 14:42:59",
                price: 12.99,
                inStock: true,
                stockCount: 50,
                rating: 4.5,
                popularity: 95,
                dateAdded: Int64(Date().timeIntervalSince1970 * 1000)
            ),
            Cocktail(
                id: "11118",
                name: "Blue Margarita",
                alternateName: nil,
                tags: nil,
                category: "Ordinary Drink",
                iba: nil,
                alcoholic: "Alcoholic",
                glass: "Cocktail glass",
                instructions: "Rub rim of cocktail glass with lime juice. Dip rim in coarse salt. Shake tequila, blue curacao, and lime juice with ice, strain into the salt-rimmed glass, and serve.",
                imageUrl: "https://www.thecocktaildb.com/images/media/drink/bry4qh1582751040.jpg",
                ingredients: [
                    CocktailIngredient(name: "Tequila", measure: "1 1/2 oz"),
                    CocktailIngredient(name: "Blue Curacao", measure: "1 oz"),
                    CocktailIngredient(name: "Lime juice", measure: "1 oz"),
                    CocktailIngredient(name: "Salt", measure: "Coarse")
                ],
                imageSource: nil,
                imageAttribution: nil,
                creativeCommonsConfirmed: true,
                dateModified: "2015-08-18 14:51:53",
                price: 13.99,
                inStock: true,
                stockCount: 45,
                rating: 4.2,
                popularity: 78,
                dateAdded: Int64(Date().timeIntervalSince1970 * 1000)
            )
        ]
    }
    
    func searchCocktails(query: String) {
        if query.isEmpty {
            filteredCocktails = cocktails
        } else {
            filteredCocktails = cocktails.filter { cocktail in
                cocktail.name.lowercased().contains(query.lowercased())
            }
        }
        applySorting()
    }
    
    func applyFilters() {
        var filtered = cocktails

        if let category = selectedCategory {
            filtered = filtered.filter { $0.category == category }
        }

        if let ingredient = selectedIngredient {
            filtered = filtered.filter { cocktail in
                // Check if any ingredient matches
                cocktail.ingredients.contains { $0.name == ingredient }
            }
        }

        filteredCocktails = filtered
        applySorting()
    }
    
    func applySorting() {
        switch sortOption {
        case .nameAsc:
            filteredCocktails.sort { $0.name < $1.name }
        case .nameDesc:
            filteredCocktails.sort { $0.name > $1.name }
        default:
            break
        }
    }
    
    func retryLoadCocktails() {
        loadCocktails()
    }
    
    @MainActor
    func refreshCocktails() async {
        // Simulate refresh
        try? await Task.sleep(nanoseconds: 1_000_000_000) // 1 second
        loadCocktails()
    }

    // MARK: - Mock Data (Temporary)
    private func loadMockData() {
        // Create some mock cocktails for testing
        let mockIngredient1 = CocktailIngredient(name: "Vodka", measure: "2 oz")
        let mockIngredient2 = CocktailIngredient(name: "Orange Juice", measure: "4 oz")

        let mockCocktail = Cocktail(
            id: "1",
            name: "Screwdriver",
            alternateName: nil,
            tags: nil,
            category: "Ordinary Drink",
            iba: nil,
            alcoholic: "Alcoholic",
            glass: "Highball glass",
            instructions: "Mix vodka and orange juice in a glass with ice.",
            imageUrl: "https://www.thecocktaildb.com/images/media/drink/8xnyke1504352207.jpg",
            ingredients: [mockIngredient1, mockIngredient2],
            imageSource: nil,
            imageAttribution: nil,
            creativeCommonsConfirmed: nil,
            dateModified: nil,
            price: 8.50,
            inStock: true,
            stockCount: 10,
            rating: 4.5,
            popularity: 85,
            dateAdded: 1672531200000 // 2023-01-01 as timestamp
        )

        self.cocktails = [mockCocktail]
        self.filteredCocktails = [mockCocktail]
    }
}