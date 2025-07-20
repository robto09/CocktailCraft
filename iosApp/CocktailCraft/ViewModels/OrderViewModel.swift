import SwiftUI
import shared
import Combine

class OrderViewModel: ObservableObject {
    @Published var orders: [Order] = []
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil

    private let orderRepository: OrderRepository?
    private var cancellables = Set<AnyCancellable>()

    init() {
        self.orderRepository = KoinInitializer.shared.getOrderRepository()
        loadOrders()
    }

    func loadOrders() {
        guard let orderRepository = orderRepository else {
            // If no repository, use mock data for testing
            orders = createMockOrders()
            return
        }

        isLoading = true
        error = nil

        Task { @MainActor in
            do {
                // Get orders from repository - must be called on main thread
                let ordersFlow = try await orderRepository.getOrderHistory()

                // Collect the flow using FlowCollector
                let flowCollector = FlowCollector<NSArray>(flow: ordersFlow)

                // Observe the collected value
                flowCollector.$value
                    .compactMap { $0 }
                    .sink { orderArray in
                        // Convert NSArray to [Order]
                        let orders = orderArray.compactMap { $0 as? Order }
                        self.orders = orders
                        self.isLoading = false
                    }
                    .store(in: &cancellables)

            } catch {
                self.error = ErrorHandler.UserFriendlyError(
                    title: "Failed to Load Orders",
                    message: "Unable to load your order history. Please try again.",
                    category: ErrorHandler.ErrorCategory.network,
                    recoveryAction: nil,
                    originalException: nil,
                    errorCode: ErrorCode.networkError
                )
                self.isLoading = false
            }
        }
    }

    func placeOrder(cartItems: [CocktailCartItem], totalPrice: Double) {
        guard let orderRepository = orderRepository else {
            error = ErrorHandler.UserFriendlyError(
                title: "Order Failed",
                message: "Unable to place order. Please try again.",
                category: ErrorHandler.ErrorCategory.client,
                recoveryAction: nil,
                originalException: nil,
                errorCode: ErrorCode.clientError
            )
            return
        }

        isLoading = true
        error = nil

        Task { @MainActor in
            do {
                // Create order ID and date
                let orderId = "ORD-\(Int(Date().timeIntervalSince1970 * 1000))"
                let dateFormatter = DateFormatter()
                dateFormatter.dateFormat = "yyyy-MM-dd"
                let currentDate = dateFormatter.string(from: Date())

                // Map cart items to order items
                let orderItems = cartItems.map { cartItem in
                    OrderItem(
                        name: cartItem.cocktail.name,
                        quantity: Int32(cartItem.quantity),
                        price: cartItem.cocktail.price
                    )
                }

                // Create order object
                let order = Order(
                    id: orderId,
                    date: currentDate,
                    items: orderItems,
                    total: totalPrice,
                    status: "Processing"
                )

                // Place order through repository - must be called on main thread
                let placeOrderFlow = try await orderRepository.placeOrder(order: order)

                // Collect the flow using FlowCollector
                let flowCollector = FlowCollector<KotlinBoolean>(flow: placeOrderFlow)

                // Observe the collected value
                flowCollector.$value
                    .compactMap { $0 }
                    .sink { success in
                        if success.boolValue {
                            self.loadOrders() // Refresh orders list
                        } else {
                            self.error = ErrorHandler.UserFriendlyError(
                                title: "Order Failed",
                                message: "Unable to place order. Please try again.",
                                category: ErrorHandler.ErrorCategory.client,
                                recoveryAction: nil,
                                originalException: nil,
                                errorCode: ErrorCode.clientError
                            )
                        }
                        self.isLoading = false
                    }
                    .store(in: &cancellables)
            } catch {
                self.error = ErrorHandler.UserFriendlyError(
                    title: "Order Failed",
                    message: "Unable to place order. Please try again.",
                    category: ErrorHandler.ErrorCategory.network,
                    recoveryAction: nil,
                    originalException: nil,
                    errorCode: ErrorCode.networkError
                )
                self.isLoading = false
            }
        }
    }

    func updateOrderStatus(orderId: String, status: String) {
        guard let orderRepository = orderRepository else { return }

        Task { @MainActor in
            do {
                try await orderRepository.updateOrderStatus(id: orderId, status: status)
                self.loadOrders() // Refresh orders list
            } catch {
                self.error = ErrorHandler.UserFriendlyError(
                    title: "Update Failed",
                    message: "Unable to update order status. Please try again.",
                    category: ErrorHandler.ErrorCategory.network,
                    recoveryAction: nil,
                    originalException: nil,
                    errorCode: ErrorCode.networkError
                )
            }
        }
    }



    // Mock data for testing when repository is not available
    private func createMockOrders() -> [Order] {
        let mockOrderItems1 = [
            OrderItem(name: "Mojito", quantity: 2, price: 10.0),
            OrderItem(name: "Margarita", quantity: 1, price: 12.0)
        ]

        let mockOrderItems2 = [
            OrderItem(name: "Cosmopolitan", quantity: 1, price: 11.0),
            OrderItem(name: "Old Fashioned", quantity: 1, price: 13.0)
        ]
        
        return [
            Order(
                id: "ORD-001",
                date: "2024-01-15",
                items: mockOrderItems1,
                total: 32.0,
                status: "Delivered"
            ),
            Order(
                id: "ORD-002",
                date: "2024-01-10",
                items: mockOrderItems2,
                total: 24.0,
                status: "Processing"
            )
        ]
    }

    func clearError() {
        error = nil
    }

    func retryLoadOrders() {
        clearError()
        loadOrders()
    }
}
