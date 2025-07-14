import SwiftUI
import shared

class FavoritesViewModel: ObservableObject {
    @Published var favoriteCocktails: [Cocktail] = []
    @Published var isLoading = false
    
    private let repository: CocktailRepository
    
    init() {
        self.repository = koin.get(objCClass: CocktailRepository.self) as! CocktailRepository
    }
    
    func loadFavorites() {
        // In a real app, this would load from persistent storage
        // For now, we'll use an empty list
        favoriteCocktails = []
    }
    
    func addFavorite(cocktail: Cocktail) {
        if !favoriteCocktails.contains(where: { $0.idDrink == cocktail.idDrink }) {
            favoriteCocktails.append(cocktail)
        }
    }
    
    func removeFavorite(cocktailId: String) {
        favoriteCocktails.removeAll { $0.idDrink == cocktailId }
    }
    
    func isFavorite(cocktailId: String) -> Bool {
        favoriteCocktails.contains { $0.idDrink == cocktailId }
    }
}