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
    @Published var cartItems: [CocktailCartItem] = []
    @Published var totalPrice: Double = 0.0
    @Published var itemCount: Int = 0
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    
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
    private let sharedViewModel: SharedCartViewModel
    
    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []
    
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = KoinInitializer.shared.getSharedCartViewModel()
        
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
    
    func addToCart(_ cocktail: Cocktail, quantity: Int = 1) async {
        do {
            try await sharedViewModel.addToCart(cocktail: cocktail, quantity: Int32(quantity))
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
    
    func incrementQuantity(_ cocktailId: String) async {
        do {
            try await sharedViewModel.incrementQuantity(cocktailId: cocktailId)
        } catch {
            // Handle error silently
        }
    }
    
    func decrementQuantity(_ cocktailId: String) async {
        do {
            try await sharedViewModel.decrementQuantity(cocktailId: cocktailId)
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