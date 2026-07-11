import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedCocktailDetailViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper.
 */
final class CocktailDetailViewModelSKIE: SharedViewModelWrapper<DetailUiState> {

    // Computed properties
    var hasRelatedCocktails: Bool {
        !state.relatedCocktails.isEmpty
    }

    var shareableText: String {
        sharedViewModel.getShareableText()
    }

    var nutritionFacts: NutritionFacts? {
        sharedViewModel.getNutritionFacts()
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedCocktailDetailViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedCocktailDetailViewModel()
        self.sharedViewModel = viewModel
        super.init(uiState: viewModel.uiState, errorFlow: viewModel.error)
    }

    deinit {
        // This wraps a Koin `factory` instance owned by this wrapper, so its
        // scope is cancelled here; the base deinit cancels observation tasks.
        // (Singleton-backed wrappers must NOT call onCleared().)
        sharedViewModel.onCleared()
    }

    // MARK: - Public Methods (using SKIE async/await)

    func loadCocktail(_ cocktailId: String) async {
        await run { try await sharedViewModel.loadCocktail(cocktailId: cocktailId) }
    }

    func toggleFavorite() async {
        await run { try await sharedViewModel.toggleFavorite() }
    }

    func addToCart(quantity: Int = 1) async {
        await run { try await sharedViewModel.addToCart(quantity: Int32(quantity)) }
    }

    func updateCartQuantity(_ quantity: Int) async {
        await run { try await sharedViewModel.updateCartQuantity(quantity: Int32(quantity)) }
    }

    func removeFromCart() async {
        await run { try await sharedViewModel.removeFromCart() }
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
        guard let cocktail = state.cocktail else { return "" }

        return cocktail.ingredients.map { ingredient in
            let measure = ingredient.measure.isEmpty ? "" : "\(ingredient.measure) "
            return "• \(measure)\(ingredient.name)"
        }.joined(separator: "\n")
    }

    func getIngredientsByType(_ type: String) -> [String] {
        return state.ingredientsByType[type] ?? []
    }

    func getAllIngredientTypes() -> [String] {
        return Array(state.ingredientsByType.keys).sorted()
    }

    func formatPrice() -> String {
        guard let cocktail = state.cocktail else { return "$0.00" }
        return cocktail.price.asPrice
    }

    func formatRating() -> String {
        guard let cocktail = state.cocktail else { return "0.0" }
        return String(format: "%.1f", cocktail.rating)
    }

    func getStockStatus() -> String {
        guard let cocktail = state.cocktail else { return "Unknown" }

        if !cocktail.inStock {
            return "Out of Stock"
        } else if cocktail.stockCount <= 5 {
            return "Low Stock (\(cocktail.stockCount) left)"
        } else {
            return "In Stock"
        }
    }

    func getStockStatusColor() -> Color {
        guard let cocktail = state.cocktail else { return .gray }

        if !cocktail.inStock {
            return .red
        } else if cocktail.stockCount <= 5 {
            return .orange
        } else {
            return .green
        }
    }

    func canAddToCart() -> Bool {
        guard let cocktail = state.cocktail else { return false }
        return cocktail.inStock && cocktail.stockCount > 0
    }

    func shareContent() -> String {
        return shareableText
    }
}
