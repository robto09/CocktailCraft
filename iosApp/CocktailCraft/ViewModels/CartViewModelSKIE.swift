import SwiftUI
import shared
import Observation

/**
 * iOS ViewModel wrapper for SharedCartViewModel using pure SKIE integration.
 * Mirrors the consolidated uiState as Observation-tracked state.
 */
@MainActor
@Observable
class CartViewModelSKIE {
    // Consolidated UI state from the shared ViewModel
    private(set) var state: CartUiState
    // The single error channel from the shared ViewModel base class
    var error: ErrorHandler.UserFriendlyError? = nil

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
    @ObservationIgnored private var observationTasks: [Task<Void, Never>] = []

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

    // MARK: - Public Methods (using SKIE async/await)

    func addToCart(_ cocktail: Cocktail, quantity: Int = 1) async {
        do {
            try await sharedViewModel.addToCart(cocktail: cocktail, quantity: Int32(quantity))
        } catch {
            // Handle error silently
        }
    }

    func decrementQuantity(_ cocktailId: String) async {
        // Optimistic apply/revert lives in the shared ViewModel
        do {
            try await sharedViewModel.decrementQuantity(cocktailId: cocktailId)
        } catch {
            // Handle error silently
        }
    }

    func incrementQuantity(_ cocktailId: String) async {
        // Optimistic apply/revert lives in the shared ViewModel
        do {
            try await sharedViewModel.incrementQuantity(cocktailId: cocktailId)
        } catch {
            // Handle error silently
        }
    }

    func removeFromCart(_ cocktailId: String) async {
        do {
            try await sharedViewModel.removeFromCart(cocktailId: cocktailId)
        } catch {
            // Handle error silently
        }
    }

    func updateQuantity(_ cocktailId: String, quantity: Int) async {
        do {
            try await sharedViewModel.updateQuantity(cocktailId: cocktailId, quantity: Int32(quantity))
        } catch {
            // Handle error silently
        }
    }

    func clearCart() async {
        do {
            try await sharedViewModel.clearCart()
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
        return price.asPrice
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
