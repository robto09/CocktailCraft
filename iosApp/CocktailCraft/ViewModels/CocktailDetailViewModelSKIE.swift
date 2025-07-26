import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedCocktailDetailViewModel using pure SKIE integration.
 * No FlowCollector bridge needed - uses native Swift async/await.
 */
@MainActor
class CocktailDetailViewModelSKIE: ObservableObject {
    // Published properties for SwiftUI
    @Published var cocktail: shared.Cocktail? = nil
    @Published var isFavorite = false
    @Published var isInCart = false
    @Published var cartQuantity: Int = 0
    @Published var relatedCocktails: [shared.Cocktail] = []
    @Published var ingredientsByType: [String: [String]] = [:]
    @Published var isLoading = false
    @Published var error: shared.ErrorHandler.UserFriendlyError? = nil
    
    // Computed properties
    var hasRelatedCocktails: Bool {
        !relatedCocktails.isEmpty
    }
    
    var shareableText: String {
        sharedViewModel.getShareableText()
    }
    
    var nutritionFacts: [String: String] {
        sharedViewModel.getNutritionFacts()
    }
    
    // Shared ViewModel instance
    private let sharedViewModel: shared.SharedCocktailDetailViewModel
    
    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []
    
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedCocktailDetailViewModel()
        
        // Start observing StateFlows using SKIE async/await
        startObserving()
    }
    
    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        sharedViewModel.onCleared()
    }
    
    // MARK: - SKIE StateFlow Observation
    
    private func startObserving() {
        // Observe cocktail using SKIE async sequence
        observationTasks.append(Task {
            for await cocktailValue in sharedViewModel.cocktail {
                await MainActor.run {
                    self.cocktail = cocktailValue
                }
            }
        })
        
        // Observe favorite status
        observationTasks.append(Task {
            for await favorite in sharedViewModel.isFavorite {
                await MainActor.run {
                    self.isFavorite = favorite.boolValue
                }
            }
        })
        
        // Observe cart status
        observationTasks.append(Task {
            for await inCart in sharedViewModel.isInCart {
                await MainActor.run {
                    self.isInCart = inCart.boolValue
                }
            }
        })
        
        // Observe cart quantity
        observationTasks.append(Task {
            for await quantity in sharedViewModel.cartQuantity {
                await MainActor.run {
                    self.cartQuantity = Int(quantity.intValue)
                }
            }
        })
        
        // Observe related cocktails
        observationTasks.append(Task {
            for await related in sharedViewModel.relatedCocktails {
                await MainActor.run {
                    self.relatedCocktails = related
                }
            }
        })
        
        // Observe ingredients by type
        observationTasks.append(Task {
            for await ingredients in sharedViewModel.ingredientsByType {
                await MainActor.run {
                    self.ingredientsByType = ingredients
                }
            }
        })
        
        // Observe loading state
        observationTasks.append(Task {
            for await loading in sharedViewModel.isLoading {
                await MainActor.run {
                    self.isLoading = loading.boolValue
                }
            }
        })
        
        // Observe error state
        observationTasks.append(Task {
            for await errorValue in sharedViewModel.error {
                await MainActor.run {
                    self.error = errorValue
                }
            }
        })
    }
    
    // MARK: - Public Methods (using SKIE async/await)
    
    func loadCocktail(_ cocktailId: String) async {
        do {
            try await sharedViewModel.loadCocktail(cocktailId: cocktailId)
        } catch {
            print("CocktailDetailViewModelSKIE - Error loading cocktail: \(error)")
        }
    }
    
    func toggleFavorite() async {
        do {
            try await sharedViewModel.toggleFavorite()
        } catch {
            print("CocktailDetailViewModelSKIE - Error toggling favorite: \(error)")
        }
    }
    
    func addToCart(quantity: Int = 1) async {
        do {
            try await sharedViewModel.addToCart(quantity: Int32(quantity))
        } catch {
            print("CocktailDetailViewModelSKIE - Error adding to cart: \(error)")
        }
    }
    
    func updateCartQuantity(_ quantity: Int) async {
        do {
            try await sharedViewModel.updateCartQuantity(quantity: Int32(quantity))
        } catch {
            print("CocktailDetailViewModelSKIE - Error updating cart quantity: \(error)")
        }
    }
    
    func removeFromCart() async {
        do {
            try await sharedViewModel.removeFromCart()
        } catch {
            print("CocktailDetailViewModelSKIE - Error removing from cart: \(error)")
        }
    }
    
    // MARK: - Synchronous Methods
    
    func clearError() {
        sharedViewModel.clearError()
    }
    
    func refresh() {
        sharedViewModel.refresh()
    }
    
    // MARK: - Helper Methods for SwiftUI
    
    func formatIngredients() -> String {
        guard let cocktail = cocktail else { return "" }
        
        return cocktail.ingredients.map { ingredient in
            let measure = ingredient.measure.isEmpty ? "" : "\(ingredient.measure) "
            return "• \(measure)\(ingredient.name)"
        }.joined(separator: "\n")
    }
    
    func getIngredientsByType(_ type: String) -> [String] {
        return ingredientsByType[type] ?? []
    }
    
    func getAllIngredientTypes() -> [String] {
        return Array(ingredientsByType.keys).sorted()
    }
    
    func formatPrice() -> String {
        guard let cocktail = cocktail else { return "$0.00" }
        return String(format: "$%.2f", cocktail.price)
    }
    
    func formatRating() -> String {
        guard let cocktail = cocktail else { return "0.0" }
        return String(format: "%.1f", cocktail.rating)
    }
    
    func getStockStatus() -> String {
        guard let cocktail = cocktail else { return "Unknown" }
        
        if !cocktail.inStock {
            return "Out of Stock"
        } else if cocktail.stockCount <= 5 {
            return "Low Stock (\(cocktail.stockCount) left)"
        } else {
            return "In Stock"
        }
    }
    
    func getStockStatusColor() -> Color {
        guard let cocktail = cocktail else { return .gray }
        
        if !cocktail.inStock {
            return .red
        } else if cocktail.stockCount <= 5 {
            return .orange
        } else {
            return .green
        }
    }
    
    func canAddToCart() -> Bool {
        guard let cocktail = cocktail else { return false }
        return cocktail.inStock && cocktail.stockCount > 0
    }
    
    func shareContent() -> String {
        return shareableText
    }
    
    func getNutritionValue(for key: String) -> String {
        return nutritionFacts[key] ?? "N/A"
    }
}