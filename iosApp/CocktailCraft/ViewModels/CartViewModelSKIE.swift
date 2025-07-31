import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedCartViewModel using pure SKIE integration.
 * No FlowCollector bridge needed - uses native Swift async/await.
 */
@MainActor
class CartViewModelSKIE: ObservableObject {
    // Published properties for SwiftUI
    @Published var cartItems: [shared.CocktailCartItem] = []
    @Published var totalPrice: Double = 0.0
    @Published var itemCount: Int = 0
    @Published var isLoading = false
    @Published var error: shared.ErrorHandler.UserFriendlyError? = nil
    
    // Computed properties
    var isEmpty: Bool {
        cartItems.isEmpty
    }
    
    var hasItems: Bool {
        !cartItems.isEmpty
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
    private let sharedViewModel: shared.SharedCartViewModel
    
    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []

    // Prevent concurrent updates
    private var isUpdating = false
    
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedCartViewModel()
        
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
        // Observe cart items using SKIE async sequence
        observationTasks.append(Task {
            for await items in sharedViewModel.cartItems {
                await MainActor.run {
                    self.cartItems = items
                }
            }
        })
        
        // Observe total price
        observationTasks.append(Task {
            for await total in sharedViewModel.totalPrice {
                await MainActor.run {
                    self.totalPrice = total.doubleValue
                }
            }
        })
        
        // Observe item count
        observationTasks.append(Task {
            for await count in sharedViewModel.itemCount {
                await MainActor.run {
                    self.itemCount = Int(count.int32Value)
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
    
    func addToCart(_ cocktail: shared.Cocktail, quantity: Int = 1) async {
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
        let currentItem = cartItems.first { $0.cocktail.id == cocktailId }
        guard let item = currentItem else { return }

        // Update local state immediately for instant UI feedback
        await MainActor.run {
            if item.quantity > 1 {
                // Decrease quantity
                let newQuantity = item.quantity - 1

                // Update the local cart items array
                if let index = self.cartItems.firstIndex(where: { $0.cocktail.id == cocktailId }) {
                    var updatedItem = self.cartItems[index]
                    updatedItem = CocktailCartItem(cocktail: updatedItem.cocktail, quantity: Int32(newQuantity))
                    self.cartItems[index] = updatedItem
                }

                // Update totals
                self.totalPrice = self.cartItems.reduce(0.0) { $0 + ($1.cocktail.price * Double($1.quantity)) }
                self.itemCount = Int(self.cartItems.reduce(0) { $0 + $1.quantity })
            } else {
                // Remove item
                self.cartItems.removeAll { $0.cocktail.id == cocktailId }

                // Update totals
                self.totalPrice = self.cartItems.reduce(0.0) { $0 + ($1.cocktail.price * Double($1.quantity)) }
                self.itemCount = Int(self.cartItems.reduce(0) { $0 + $1.quantity })
            }
        }

        // Then update the shared state
        do {
            if item.quantity > 1 {
                try await sharedViewModel.updateQuantity(cocktailId: cocktailId, quantity: Int32(item.quantity - 1))
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
        let currentItem = cartItems.first { $0.cocktail.id == cocktailId }
        guard let item = currentItem else { return }

        let newQuantity = item.quantity + 1

        // Update local state immediately for instant UI feedback
        await MainActor.run {
            // Update the local cart items array
            if let index = self.cartItems.firstIndex(where: { $0.cocktail.id == cocktailId }) {
                var updatedItem = self.cartItems[index]
                updatedItem = CocktailCartItem(cocktail: updatedItem.cocktail, quantity: Int32(newQuantity))
                self.cartItems[index] = updatedItem
            }

            // Update totals
            self.totalPrice = self.cartItems.reduce(0.0) { $0 + ($1.cocktail.price * Double($1.quantity)) }
            self.itemCount = Int(self.cartItems.reduce(0) { $0 + $1.quantity })
        }

        // Then update the shared state
        do {
            try await sharedViewModel.updateQuantity(cocktailId: cocktailId, quantity: Int32(newQuantity))
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
        await MainActor.run {
            self.objectWillChange.send()
        }

        // Give a small delay to allow the shared state to propagate
        try? await Task.sleep(nanoseconds: 100_000_000) // 0.1 seconds

        await MainActor.run {
            self.objectWillChange.send()
        }
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
    
    func getCartItem(_ cocktailId: String) -> shared.CocktailCartItem? {
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
        if totalPrice >= 30.0 && totalPrice < 50.0 {
            return 50.0 - totalPrice // Amount needed for free delivery
        }
        return nil
    }
}