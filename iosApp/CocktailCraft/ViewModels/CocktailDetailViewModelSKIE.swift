import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedCocktailDetailViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper; views read `state.<field>` (including the derived
 * `state.nutritionFacts` / `state.shareableText`). The helpers below are
 * Swift-only presentation formatting over that state.
 */
final class CocktailDetailViewModelSKIE: SharedViewModelWrapper<DetailUiState> {

    // Shared ViewModel instance
    private let sharedViewModel: SharedCocktailDetailViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedCocktailDetailViewModel()
        self.sharedViewModel = viewModel
        super.init(viewModel: viewModel, uiState: viewModel.uiState, ownsViewModel: true)
        #if DEBUG
        // IO-13 instrumentation: @State evaluates its initializer on every
        // parent re-render and discards the extra instances — each one spins
        // up (and deinit immediately tears down) a fresh Koin factory VM with
        // its own coroutine scope. Watch these logs on the detail flow; if
        // create/teardown pairs are frequent, move to lazy resolution in
        // .task or a lighter factory.
        Self.liveInstances += 1
        print("CocktailDetailViewModelSKIE: created (live: \(Self.liveInstances))")
        #endif
    }

    // The wrapped Koin `factory` instance is owned (ownsViewModel), so the
    // base deinit clears its scope; this one only feeds the IO-13 gauge.
    #if DEBUG
    deinit {
        Self.liveInstances -= 1
        print("CocktailDetailViewModelSKIE: torn down (live: \(Self.liveInstances))")
    }
    #endif

    #if DEBUG
    // Rough churn gauge. nonisolated(unsafe): deinit is nonisolated and must
    // decrement it; main-thread-confined in practice and debug-only.
    nonisolated(unsafe) private static var liveInstances = 0
    #endif

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

    // MARK: - Helper Methods for SwiftUI

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
        guard let cocktail = state.cocktail else { return L(\.common_unknown) }

        if !cocktail.inStock {
            return L(\.detail_out_of_stock)
        } else if cocktail.stockCount <= 5 {
            return L(\.detail_low_stock, cocktail.stockCount)
        } else {
            return L(\.detail_in_stock_ios)
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
}
