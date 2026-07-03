import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedCocktailDetailViewModel using pure SKIE integration.
 * Mirrors the consolidated uiState as a single @Published value.
 */
@MainActor
class CocktailDetailViewModelSKIE: ObservableObject {
    // Consolidated UI state from the shared ViewModel
    @Published private(set) var state: DetailUiState
    // Base-class error flow (distinct from state.error, matching prior behavior)
    @Published var error: ErrorHandler.UserFriendlyError? = nil

    // Computed properties
    var hasRelatedCocktails: Bool {
        !state.relatedCocktails.isEmpty
    }

    var shareableText: String {
        sharedViewModel.getShareableText()
    }

    var nutritionFacts: [String: String] {
        sharedViewModel.getNutritionFacts()
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedCocktailDetailViewModel

    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []

    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedCocktailDetailViewModel()

        // Seed synchronously so the first frame renders the current state
        self.state = sharedViewModel.uiState.value

        // Start observing StateFlows using SKIE async/await
        startObserving()
    }

    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        // This wraps a Koin `factory` instance owned by this wrapper, so its
        // scope is cancelled here. (Singleton-backed wrappers must NOT do this.)
        sharedViewModel.onCleared()
    }

    // MARK: - SKIE StateFlow Observation

    private func startObserving() {
        // These Tasks inherit @MainActor, so assignments land on the main thread.
        observationTasks.append(Task { [weak self] in
            guard let flow = self?.sharedViewModel.uiState else { return }
            for await state in flow {
                self?.state = state
            }
        })

        observationTasks.append(Task { [weak self] in
            guard let flow = self?.sharedViewModel.error else { return }
            for await errorValue in flow {
                self?.error = errorValue
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
        return String(format: "$%.2f", cocktail.price)
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

    func getNutritionValue(for key: String) -> String {
        return nutritionFacts[key] ?? "N/A"
    }
}
