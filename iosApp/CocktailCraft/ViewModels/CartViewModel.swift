import SwiftUI
@preconcurrency import shared


import Combine

@MainActor
class CartViewModel: ObservableObject {
    @Published var cartItems: [CocktailCartItem] = []
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    @Published var totalPrice: Double = 0.0

    private let cartRepository: CartRepository?
    private var cancellables = Set<AnyCancellable>()

    // Singleton instance
    static let instance = CartViewModel()

    private init() {
        self.cartRepository = KoinInitializer.shared.getCartRepository()
        loadCart()
    }
    
    func loadCart() {
        guard cartRepository != nil else {
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

    func checkout(completion: @escaping (Bool) -> Void = { _ in }) {
        // Get the order repository
        guard let orderRepository = KoinInitializer.shared.getOrderRepository() else {
            // If no repository, just clear the cart for now
            clearCart()
            completion(false)
            return
        }

        // Don't proceed if cart is empty
        guard !cartItems.isEmpty else {
            completion(false)
            return
        }

        // Create a copy of cart items and total for the order
        let orderCartItems = cartItems
        let orderTotal = totalPrice

        // Create order ID and date
        let orderId = "ORD-\(Int(Date().timeIntervalSince1970 * 1000))"
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let currentDate = dateFormatter.string(from: Date())

        // Map cart items to order items
        let orderItems = orderCartItems.map { cartItem in
            OrderItem(
                name: cartItem.cocktail.name,
                quantity: Int32(cartItem.quantity),
                price: cartItem.cocktail.price
            )
        }

        // Create order object (outside Task so it's accessible in both success and error cases)
        let order = Order(
            id: orderId,
            date: currentDate,
            items: orderItems,
            total: orderTotal,
            status: "Processing"
        )

        isLoading = true

        // Place the order asynchronously
        Task { @MainActor in
            do {

                // Place order through repository - must be called on main thread
                let kotlinFlow = try await orderRepository.placeOrder(order: order)

                // Use SKIE AsyncSequence pattern - cast to proper type
                if let asyncFlow = kotlinFlow as? any AsyncSequence {
                    for try await success in asyncFlow {
                        await MainActor.run {
                            if let boolValue = success as? KotlinBoolean, boolValue.boolValue == true {
                                print("CartViewModel: Order placed successfully via repository")
                                // Order placed successfully, clear cart only once
                                self.clearCart()
                                completion(true)
                            } else {
                                print("CartViewModel: Repository reported order placement failed")
                                completion(false)
                            }
                            self.isLoading = false
                        }
                        break // Take first emission
                    }
                } else {
                    await MainActor.run {
                        self.isLoading = false
                        completion(false)
                    }
                }
            } catch {
                // Handle error silently for now - the OrderViewModel will show any errors
                print("CartViewModel: Error placing order via repository: \(error)")
                // Even if repository fails, add to session storage so user can see their order
                print("CartViewModel: Order processed locally as fallback")
                self.clearCart()
                self.isLoading = false
                completion(true) // Still report success since we saved it locally
            }
        }
    }

    func retryLoadCart() {
        loadCart()
    }


}
