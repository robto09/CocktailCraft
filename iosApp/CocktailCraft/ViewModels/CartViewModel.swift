import SwiftUI

import shared
import Combine

class CartViewModel: ObservableObject {
    @Published var cartItems: [CocktailCartItem] = []
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    @Published var totalPrice: Double = 0.0

    private let cartRepository: CartRepository?

    init() {
        self.cartRepository = KoinInitializer.shared.getCartRepository()
        loadCart()
    }
    
    func loadCart() {
        guard let cartRepository = cartRepository else {
            // If no repository, keep empty cart
            cartItems = []
            totalPrice = 0.0
            return
        }

        isLoading = true
        error = nil

        Task {
            do {
                // Load cart items
                let cartFlow = try await cartRepository.getCartItems()
                let cartCollector = FlowCollector<NSArray>(flow: cartFlow)

                // Load total price
                let totalFlow = try await cartRepository.getCartTotal()
                let totalCollector = FlowCollector<KotlinDouble>(flow: totalFlow)

                await MainActor.run {
                    if let cartArray = cartCollector.value as? [CocktailCartItem] {
                        self.cartItems = cartArray
                    }
                    if let total = totalCollector.value?.doubleValue {
                        self.totalPrice = total
                    }
                    self.isLoading = cartCollector.isLoading || totalCollector.isLoading
                    if let error = cartCollector.error ?? totalCollector.error {
                        self.error = ErrorHandler.shared.createUserFriendlyError(
                            title: "Loading Error",
                            message: "Failed to load cart: \(error.localizedDescription)",
                            category: ErrorHandler.ErrorCategory.unknown,
                            recoveryAction: nil,
                            originalException: nil,
                            errorCode: ErrorCode.unknown
                        )
                    }
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Loading Error",
                        message: "Failed to load cart: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
                    )
                }
            }
        }
    }

    func addToCart(cocktail: Cocktail) {
        guard let cartRepository = cartRepository else {
            // Fallback to local storage if repository is not available
            if let index = cartItems.firstIndex(where: { $0.cocktail.id == cocktail.id }) {
                cartItems[index] = cartItems[index].doCopy(cocktail: cartItems[index].cocktail, quantity: cartItems[index].quantity + 1)
            } else {
                let cartItem = CocktailCartItem(cocktail: cocktail, quantity: 1)
                cartItems.append(cartItem)
            }
            return
        }

        Task {
            do {
                let cartItem = CocktailCartItem(cocktail: cocktail, quantity: 1)
                try await cartRepository.addToCart(cartItem: cartItem)
                // Reload cart to get updated data
                await MainActor.run {
                    self.loadCart()
                }
            } catch {
                await MainActor.run {
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Add to Cart Error",
                        message: "Failed to add item to cart: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
                    )
                }
            }
        }
    }

    func removeFromCart(cocktailId: String) {
        guard let cartRepository = cartRepository else {
            // Fallback to local storage if repository is not available
            cartItems.removeAll { $0.cocktail.id == cocktailId }
            return
        }

        Task {
            do {
                try await cartRepository.removeFromCart(cocktailId: cocktailId)
                // Reload cart to get updated data
                await MainActor.run {
                    self.loadCart()
                }
            } catch {
                await MainActor.run {
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Remove from Cart Error",
                        message: "Failed to remove item from cart: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
                    )
                }
            }
        }
    }

    func updateQuantity(cocktailId: String, quantity: Int) {
        guard let cartRepository = cartRepository else {
            // Fallback to local storage if repository is not available
            if let index = cartItems.firstIndex(where: { $0.cocktail.id == cocktailId }) {
                if quantity > 0 {
                    cartItems[index] = cartItems[index].doCopy(cocktail: cartItems[index].cocktail, quantity: Int32(quantity))
                } else {
                    cartItems.remove(at: index)
                }
            }
            return
        }

        Task {
            do {
                try await cartRepository.updateQuantity(cocktailId: cocktailId, quantity: Int32(quantity))
                // Reload cart to get updated data
                await MainActor.run {
                    self.loadCart()
                }
            } catch {
                await MainActor.run {
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Update Quantity Error",
                        message: "Failed to update quantity: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
                    )
                }
            }
        }
    }

    func clearCart() {
        guard let cartRepository = cartRepository else {
            // Fallback to local storage if repository is not available
            cartItems.removeAll()
            totalPrice = 0.0
            return
        }

        Task {
            do {
                try await cartRepository.clearCart()
                // Reload cart to get updated data
                await MainActor.run {
                    self.loadCart()
                }
            } catch {
                await MainActor.run {
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Clear Cart Error",
                        message: "Failed to clear cart: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
                    )
                }
            }
        }
    }

    func checkout() {
        // Implement checkout logic
        // For now, just clear the cart
        clearCart()
    }

    func retryLoadCart() {
        loadCart()
    }
}