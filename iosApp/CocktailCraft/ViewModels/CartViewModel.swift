import SwiftUI

import shared
import Combine

struct CartItem {
    let cocktail: Cocktail
    var quantity: Int
    let price: Double = 12.99
}

class CartViewModel: ObservableObject {
    @Published var cartItems: [CartItem] = []
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    
    var totalPrice: Double {
        cartItems.reduce(0) { $0 + ($1.price * Double($1.quantity)) }
    }
    
    private let repository: CocktailRepository?

    init() {
        self.repository = KoinInitializer.shared.getCocktailRepository()
        loadCart()
    }
    
    func loadCart() {
        // In a real app, this would load from persistent storage
        // For now, we'll start with an empty cart
    }
    
    func addToCart(cocktail: Cocktail) {
        if let index = cartItems.firstIndex(where: { $0.cocktail.id == cocktail.id }) {
            cartItems[index].quantity += 1
        } else {
            cartItems.append(CartItem(cocktail: cocktail, quantity: 1))
        }
    }
    
    func removeFromCart(cocktailId: String) {
        cartItems.removeAll { $0.cocktail.id == cocktailId }
    }
    
    func updateQuantity(cocktailId: String, quantity: Int) {
        if let index = cartItems.firstIndex(where: { $0.cocktail.id == cocktailId }) {
            if quantity > 0 {
                cartItems[index].quantity = quantity
            } else {
                cartItems.remove(at: index)
            }
        }
    }
    
    func clearCart() {
        cartItems.removeAll()
    }
    
    func checkout() {
        // Implement checkout logic
        // For now, just clear the cart
        clearCart()
    }
}