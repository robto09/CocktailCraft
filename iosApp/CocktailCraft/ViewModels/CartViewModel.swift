import Foundation
import shared
import Combine

@MainActor
class CartViewModel: ObservableObject, ICartViewModel {
    private let cartRepository: CartRepository
    private let manageCartUseCase: ManageCartUseCase
    
    // MARK: - Published Properties
    @Published private(set) var cartItems: [CocktailCartItem] = []
    @Published private(set) var totalPrice: Double = 0.0
    @Published private(set) var isLoading: Bool = false
    
    // MARK: - StateFlow Properties
    var cartItemsFlow: any StateFlow<[CocktailCartItem]> {
        StateFlowWrapper(publisher: $cartItems.eraseToAnyPublisher())
    }
    
    var totalPriceFlow: any StateFlow<Double> {
        StateFlowWrapper(publisher: $totalPrice.eraseToAnyPublisher())
    }
    
    var isLoadingFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $isLoading.eraseToAnyPublisher())
    }
    
    init() {
        let container = DependencyContainer.shared
        self.cartRepository = container.cartRepository
        self.manageCartUseCase = container.manageCartUseCase
        
        // Initial load
        Task {
            await loadCartItems()
        }
    }
    
    // MARK: - ICartViewModel Implementation
    
    func loadCartItems() {
        Task {
            isLoading = true
            do {
                let result = try await cartRepository.getCartItems()
                if let itemsResult = result as? Result.Success<[CocktailCartItem]> {
                    cartItems = itemsResult.data
                    calculateTotalPrice()
                }
            } catch {
                print("Error loading cart items: \(error)")
            }
            isLoading = false
        }
    }
    
    func addToCart(cocktail: Cocktail, quantity: Int32 = 1) {
        Task {
            isLoading = true
            do {
                try await manageCartUseCase.addToCart(cocktail: cocktail, quantity: quantity)
                await loadCartItems()
            } catch {
                print("Error adding to cart: \(error)")
            }
            isLoading = false
        }
    }
    
    func removeFromCart(cocktailId: String) {
        Task {
            isLoading = true
            do {
                try await manageCartUseCase.removeFromCart(cocktailId: cocktailId)
                await loadCartItems()
            } catch {
                print("Error removing from cart: \(error)")
            }
            isLoading = false
        }
    }
    
    func updateQuantity(cocktailId: String, quantity: Int32) {
        Task {
            isLoading = true
            do {
                try await manageCartUseCase.updateQuantity(cocktailId: cocktailId, quantity: quantity)
                await loadCartItems()
            } catch {
                print("Error updating quantity: \(error)")
            }
            isLoading = false
        }
    }
    
    func clearCart() {
        Task {
            isLoading = true
            do {
                try await manageCartUseCase.clearCart()
                cartItems = []
                totalPrice = 0.0
            } catch {
                print("Error clearing cart: \(error)")
            }
            isLoading = false
        }
    }
    
    // MARK: - Private Helper Methods
    
    private func calculateTotalPrice() {
        totalPrice = cartItems.reduce(0.0) { total, item in
            total + (item.cocktail.price * Double(item.quantity))
        }
    }
}

// MARK: - StateFlow Wrapper
private class StateFlowWrapper<T>: StateFlow {
    private let publisher: AnyPublisher<T, Never>
    private var currentValue: T
    
    init(publisher: AnyPublisher<T, Never>, initialValue: T? = nil) {
        self.publisher = publisher
        self.currentValue = initialValue ?? publisher.value
    }
    
    var value: T {
        get { currentValue }
        set { currentValue = newValue }
    }
}