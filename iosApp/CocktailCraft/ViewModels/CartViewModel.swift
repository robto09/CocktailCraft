import SwiftUI

import shared
import Combine

class CartViewModel: ObservableObject {
    @Published var cartItems: [CocktailCartItem] = []
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    @Published var totalPrice: Double = 0.0

    private let cartRepository: CartRepository?

    // Singleton instance
    static let shared = CartViewModel()

    private init() {
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

        // Use fallback local storage for now to avoid crashes
        cartItems = []
        totalPrice = 0.0
        isLoading = false
    }

    func addToCart(cocktail: Cocktail) {
        print("CartViewModel - addToCart called for cocktail: \(cocktail.name)")

        // Use local storage for now to avoid crashes
        if let index = cartItems.firstIndex(where: { $0.cocktail.id == cocktail.id }) {
            cartItems[index] = cartItems[index].doCopy(cocktail: cartItems[index].cocktail, quantity: cartItems[index].quantity + 1)
        } else {
            let cartItem = CocktailCartItem(cocktail: cocktail, quantity: 1)
            cartItems.append(cartItem)
        }

        // Update total price
        totalPrice = cartItems.reduce(0) { total, item in
            total + (item.cocktail.price * Double(item.quantity))
        }

        print("CartViewModel - Successfully added to cart locally")
    }

    func removeFromCart(cocktailId: String) {
        // Use local storage for now to avoid crashes
        cartItems.removeAll { $0.cocktail.id == cocktailId }

        // Update total price
        totalPrice = cartItems.reduce(0) { total, item in
            total + (item.cocktail.price * Double(item.quantity))
        }
    }

    func updateQuantity(cocktailId: String, quantity: Int) {
        // Use local storage for now to avoid crashes
        if let index = cartItems.firstIndex(where: { $0.cocktail.id == cocktailId }) {
            if quantity > 0 {
                cartItems[index] = cartItems[index].doCopy(cocktail: cartItems[index].cocktail, quantity: Int32(quantity))
            } else {
                cartItems.remove(at: index)
            }
        }

        // Update total price
        totalPrice = cartItems.reduce(0) { total, item in
            total + (item.cocktail.price * Double(item.quantity))
        }
    }

    func clearCart() {
        // Use local storage for now to avoid crashes
        cartItems.removeAll()
        totalPrice = 0.0
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