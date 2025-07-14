import SwiftUI
import shared
import Combine

class HomeViewModel: ObservableObject {
    @Published var cocktails: [Cocktail] = []
    @Published var filteredCocktails: [Cocktail] = []
    @Published var isLoading = false
    @Published var error: UserFriendlyError? = nil
    @Published var selectedCategory: String? = nil
    @Published var selectedIngredient: String? = nil
    @Published var sortOption: SortOption = .nameAsc
    
    private let repository: CocktailRepository
    private var cancellables = Set<AnyCancellable>()
    
    init() {
        self.repository = koin.get(objCClass: CocktailRepository.self) as! CocktailRepository
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
                idDrink: "11007",
                strDrink: "Margarita",
                strDrinkAlternate: nil,
                strTags: "IBA,Contemporary Classic",
                strVideo: nil,
                strCategory: "Ordinary Drink",
                strIBA: "Contemporary Classic",
                strAlcoholic: "Alcoholic",
                strGlass: "Cocktail glass",
                strInstructions: "Rub the rim of the glass with the lime slice to make the salt stick to it. Take care to moisten only the outer rim and sprinkle the salt on it. The salt should present to the lips of the imbiber and never mix into the cocktail. Shake the other ingredients with ice, then carefully pour into the glass.",
                strInstructionsES: nil,
                strInstructionsDE: nil,
                strInstructionsFR: nil,
                strInstructionsIT: nil,
                strInstructionsZH_HANS: nil,
                strInstructionsZH_HANT: nil,
                strDrinkThumb: "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg",
                strIngredient1: "Tequila",
                strIngredient2: "Triple sec",
                strIngredient3: "Lime juice",
                strIngredient4: "Salt",
                strIngredient5: nil,
                strIngredient6: nil,
                strIngredient7: nil,
                strIngredient8: nil,
                strIngredient9: nil,
                strIngredient10: nil,
                strIngredient11: nil,
                strIngredient12: nil,
                strIngredient13: nil,
                strIngredient14: nil,
                strIngredient15: nil,
                strMeasure1: "1 1/2 oz ",
                strMeasure2: "1/2 oz ",
                strMeasure3: "1 oz ",
                strMeasure4: nil,
                strMeasure5: nil,
                strMeasure6: nil,
                strMeasure7: nil,
                strMeasure8: nil,
                strMeasure9: nil,
                strMeasure10: nil,
                strMeasure11: nil,
                strMeasure12: nil,
                strMeasure13: nil,
                strMeasure14: nil,
                strMeasure15: nil,
                strImageSource: "https://commons.wikimedia.org/wiki/File:Klassiche_Margarita.jpg",
                strImageAttribution: "Cocktailmarler",
                strCreativeCommonsConfirmed: "Yes",
                dateModified: "2015-08-18 14:42:59",
                reviews: []
            ),
            Cocktail(
                idDrink: "11118",
                strDrink: "Blue Margarita",
                strDrinkAlternate: nil,
                strTags: nil,
                strVideo: nil,
                strCategory: "Ordinary Drink",
                strIBA: nil,
                strAlcoholic: "Alcoholic",
                strGlass: "Cocktail glass",
                strInstructions: "Rub rim of cocktail glass with lime juice. Dip rim in coarse salt. Shake tequila, blue curacao, and lime juice with ice, strain into the salt-rimmed glass, and serve.",
                strInstructionsES: nil,
                strInstructionsDE: nil,
                strInstructionsFR: nil,
                strInstructionsIT: nil,
                strInstructionsZH_HANS: nil,
                strInstructionsZH_HANT: nil,
                strDrinkThumb: "https://www.thecocktaildb.com/images/media/drink/bry4qh1582751040.jpg",
                strIngredient1: "Tequila",
                strIngredient2: "Blue Curacao",
                strIngredient3: "Lime juice",
                strIngredient4: "Salt",
                strIngredient5: nil,
                strIngredient6: nil,
                strIngredient7: nil,
                strIngredient8: nil,
                strIngredient9: nil,
                strIngredient10: nil,
                strIngredient11: nil,
                strIngredient12: nil,
                strIngredient13: nil,
                strIngredient14: nil,
                strIngredient15: nil,
                strMeasure1: "1 1/2 oz ",
                strMeasure2: "1 oz ",
                strMeasure3: "1 oz ",
                strMeasure4: "Coarse ",
                strMeasure5: nil,
                strMeasure6: nil,
                strMeasure7: nil,
                strMeasure8: nil,
                strMeasure9: nil,
                strMeasure10: nil,
                strMeasure11: nil,
                strMeasure12: nil,
                strMeasure13: nil,
                strMeasure14: nil,
                strMeasure15: nil,
                strImageSource: nil,
                strImageAttribution: nil,
                strCreativeCommonsConfirmed: "Yes",
                dateModified: "2015-08-18 14:51:53",
                reviews: []
            )
        ]
    }
    
    func searchCocktails(query: String) {
        if query.isEmpty {
            filteredCocktails = cocktails
        } else {
            filteredCocktails = cocktails.filter { cocktail in
                cocktail.strDrink.lowercased().contains(query.lowercased())
            }
        }
        applySorting()
    }
    
    func applyFilters() {
        var filtered = cocktails
        
        if let category = selectedCategory {
            filtered = filtered.filter { $0.strCategory == category }
        }
        
        if let ingredient = selectedIngredient {
            filtered = filtered.filter { cocktail in
                // Check if any ingredient matches
                [cocktail.strIngredient1, cocktail.strIngredient2, cocktail.strIngredient3,
                 cocktail.strIngredient4, cocktail.strIngredient5].contains(ingredient)
            }
        }
        
        filteredCocktails = filtered
        applySorting()
    }
    
    func applySorting() {
        switch sortOption {
        case .nameAsc:
            filteredCocktails.sort { $0.strDrink < $1.strDrink }
        case .nameDesc:
            filteredCocktails.sort { $0.strDrink > $1.strDrink }
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
}