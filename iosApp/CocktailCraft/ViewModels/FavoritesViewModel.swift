import SwiftUI

import shared

class FavoritesViewModel: ObservableObject {
    @Published var favoriteCocktails: [Cocktail] = []
    @Published var isLoading = false
    
    private let repository: CocktailRepository?

    init() {
        self.repository = KoinInitializer.shared.getCocktailRepository()
    }
    
    func loadFavorites() {
        // In a real app, this would load from persistent storage
        // For now, we'll use an empty list
        favoriteCocktails = []
    }
    
    func addFavorite(cocktail: Cocktail) {
        if !favoriteCocktails.contains(where: { $0.id == cocktail.id }) {
            favoriteCocktails.append(cocktail)
        }
    }
    
    func removeFavorite(cocktailId: String) {
        favoriteCocktails.removeAll { $0.id == cocktailId }
    }
    
    func isFavorite(cocktailId: String) -> Bool {
        favoriteCocktails.contains { $0.id == cocktailId }
    }
}