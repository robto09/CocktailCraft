import Foundation
import shared
import Combine

@MainActor
class MainViewModel: ObservableObject {
    private let cocktailRepository: CocktailRepository
    private let cartRepository: CartRepository
    private let favoritesRepository: FavoritesRepository
    
    @Published var cocktails: [Cocktail] = []
    @Published var isLoading: Bool = false
    @Published var error: String? = nil
    
    init() {
        // Get repositories from Koin
        let koin = KoinKt.doInitKoin().koin
        self.cocktailRepository = koin.get(objCClass: CocktailRepository.self) as! CocktailRepository
        self.cartRepository = koin.get(objCClass: CartRepository.self) as! CartRepository
        self.favoritesRepository = koin.get(objCClass: FavoritesRepository.self) as! FavoritesRepository
        
        // Initial load
        Task {
            await loadCocktails()
        }
    }
    
    func loadCocktails() async {
        isLoading = true
        error = nil
        
        do {
            let result = try await cocktailRepository.getCocktails()
            switch result {
            case let success as Result.Success<NSArray>:
                if let cocktailArray = success.data as? [Cocktail] {
                    cocktails = cocktailArray
                }
            case let failure as Result.Error:
                error = failure.error.description
            default:
                error = "Unknown error occurred"
            }
        } catch {
            self.error = error.localizedDescription
        }
        
        isLoading = false
    }
    
    func toggleFavorite(cocktail: Cocktail) async {
        do {
            try await favoritesRepository.toggleFavorite(cocktail: cocktail)
            // Reload cocktails to reflect changes
            await loadCocktails()
        } catch {
            self.error = error.localizedDescription
        }
    }
    
    func addToCart(cocktail: Cocktail) async {
        do {
            try await cartRepository.addToCart(cocktail: cocktail)
        } catch {
            self.error = error.localizedDescription
        }
    }
}