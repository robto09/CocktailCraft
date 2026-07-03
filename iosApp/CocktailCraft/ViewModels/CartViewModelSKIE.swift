import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedCartViewModel using pure SKIE integration.
 * Mirrors the consolidated uiState as a single @Published value.
 */
@MainActor
class CartViewModelSKIE: ObservableObject {
    // Consolidated UI state from the shared ViewModel
    @Published private(set) var state: CartUiState
    // The single error channel from the shared ViewModel base class
    @Published var error: ErrorHandler.UserFriendlyError? = nil

    // Computed properties
    var isEmpty: Bool {
        state.cartItems.isEmpty
    }

    var hasItems: Bool {
        !state.cartItems.isEmpty
    }

    var estimatedDeliveryTime: String {
        sharedViewModel.getEstimatedDeliveryTime()
    }

    var isFreeDelivery: Bool {
        sharedViewModel.isFreeDelivery()
    }

    var deliveryFee: Double {
        sharedViewModel.getDeliveryFee()
    }

    var finalTotal: Double {
        sharedViewModel.getFinalTotal()
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedCartViewModel

    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []

    // Prevent concurrent updates
    private var isUpdating = false

    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedCartViewModel()

        // Seed synchronously so the first frame renders the current state
        self.state = sharedViewModel.uiState.value

        // Start observing StateFlows using SKIE async/await
        startObserving()
    }

    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        // Note: Do NOT call onCleared() — this wraps a Koin `single` whose
        // coroutine scope must survive the lifetime of any one wrapper.
        // (Factory-scoped wrappers — CocktailDetail, Review — do call it.)
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

    /// Replace the published state with a locally-modified copy for instant
    /// UI feedback while the shared update round-trips.
    private func applyOptimisticCartItems(_ items: [CocktailCartItem]) {
        let totalPrice = items.reduce(0.0) { $0 + ($1.cocktail.price * Double($1.quantity)) }
        let itemCount = items.reduce(Int32(0)) { $0 + $1.quantity }
        state = CartUiState(
            cartItems: items,
            totalPrice: totalPrice,
            itemCount: itemCount,
            isLoading: state.isLoading
        )
    }

    // MARK: - Public Methods (using SKIE async/await)

    func addToCart(_ cocktail: Cocktail, quantity: Int = 1) async {
        do {
            try await sharedViewModel.addToCart(cocktail: cocktail, quantity: Int32(quantity))
            // Force a refresh of the cart state to ensure UI updates
            await refreshCartState()
        } catch {
            // Handle error silently
        }
    }

    func decrementQuantity(_ cocktailId: String) async {
        // Prevent concurrent updates
        guard !isUpdating else { return }
        isUpdating = true
        defer { isUpdating = false }

        // Find the current item first
        let currentItem = state.cartItems.first { $0.cocktail.id == cocktailId }
        guard let item = currentItem else { return }

        // Update local state immediately for instant UI feedback
        var items = state.cartItems
        if item.quantity > 1 {
            if let index = items.firstIndex(where: { $0.cocktail.id == cocktailId }) {
                items[index] = CocktailCartItem(cocktail: items[index].cocktail, quantity: item.quantity - 1)
            }
        } else {
            items.removeAll { $0.cocktail.id == cocktailId }
        }
        applyOptimisticCartItems(items)

        // Then update the shared state
        do {
            if item.quantity > 1 {
                try await sharedViewModel.updateQuantity(cocktailId: cocktailId, quantity: item.quantity - 1)
            } else {
                try await sharedViewModel.removeFromCart(cocktailId: cocktailId)
            }
        } catch {
            // If the shared update fails, revert the local state
            await refreshCartState()
        }
    }

    func incrementQuantity(_ cocktailId: String) async {
        // Prevent concurrent updates
        guard !isUpdating else { return }
        isUpdating = true
        defer { isUpdating = false }

        // Find the current item first
        let currentItem = state.cartItems.first { $0.cocktail.id == cocktailId }
        guard let item = currentItem else { return }

        let newQuantity = item.quantity + 1

        // Update local state immediately for instant UI feedback
        var items = state.cartItems
        if let index = items.firstIndex(where: { $0.cocktail.id == cocktailId }) {
            items[index] = CocktailCartItem(cocktail: items[index].cocktail, quantity: newQuantity)
        }
        applyOptimisticCartItems(items)

        // Then update the shared state
        do {
            try await sharedViewModel.updateQuantity(cocktailId: cocktailId, quantity: newQuantity)
        } catch {
            // If the shared update fails, revert the local state
            await refreshCartState()
        }
    }

    func removeFromCart(_ cocktailId: String) async {
        do {
            try await sharedViewModel.removeFromCart(cocktailId: cocktailId)
            // Force a refresh of the cart state to ensure UI updates
            await refreshCartState()
        } catch {
            // Handle error silently
        }
    }

    func updateQuantity(_ cocktailId: String, quantity: Int) async {
        do {
            try await sharedViewModel.updateQuantity(cocktailId: cocktailId, quantity: Int32(quantity))
            // Force a refresh of the cart state to ensure UI updates
            await refreshCartState()
        } catch {
            // Handle error silently
        }
    }

    private func refreshCartState() async {
        // Force a UI update to ensure the state changes are reflected
        objectWillChange.send()

        // Give a small delay to allow the shared state to propagate
        try? await Task.sleep(nanoseconds: 100_000_000) // 0.1 seconds

        objectWillChange.send()
    }

    func clearCart() async {
        do {
            try await sharedViewModel.clearCart()
            // Force a refresh of the cart state to ensure UI updates
            await refreshCartState()
        } catch {
            // Handle error silently
        }
    }

    // MARK: - Synchronous Methods

    func isInCart(_ cocktailId: String) -> Bool {
        return sharedViewModel.isInCart(cocktailId: cocktailId)
    }

    func getQuantity(_ cocktailId: String) -> Int {
        return Int(sharedViewModel.getQuantity(cocktailId: cocktailId))
    }

    func getCartItem(_ cocktailId: String) -> CocktailCartItem? {
        return sharedViewModel.getCartItem(cocktailId: cocktailId)
    }

    func clearError() {
        sharedViewModel.clearError()
    }

    func refresh() {
        sharedViewModel.refresh()
    }

    // MARK: - Helper Methods for SwiftUI

    func formatPrice(_ price: Double) -> String {
        return String(format: "$%.2f", price)
    }

    func getDeliveryText() -> String {
        if isFreeDelivery {
            return "FREE Delivery"
        } else {
            return formatPrice(deliveryFee) + " Delivery"
        }
    }

    func getSavingsAmount() -> Double? {
        if state.totalPrice >= 30.0 && state.totalPrice < 50.0 {
            return 50.0 - state.totalPrice // Amount needed for free delivery
        }
        return nil
    }
}
