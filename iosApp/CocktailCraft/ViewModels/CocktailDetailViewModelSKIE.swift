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

    func formatPrice() -> String {
        guard let cocktail = state.cocktail else { return "$0.00" }
        return cocktail.price.asPrice
    }

    func canAddToCart() -> Bool {
        guard let cocktail = state.cocktail else { return false }
        return cocktail.inStock && cocktail.stockCount > 0
    }
}
