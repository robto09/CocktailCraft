import SwiftUI
@preconcurrency import shared
import Combine

@MainActor
class OrderViewModel: ObservableObject {
    @Published var orders: [Order] = []
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil

    private let orderRepository: OrderRepository?
    private var cancellables = Set<AnyCancellable>()

    // Singleton instance
    static let shared = OrderViewModel()

    private init() {
        self.orderRepository = KoinInitializer.shared.getOrderRepository()
        loadOrders()
    }

    func loadOrders() {
        print("OrderViewModel.loadOrders() called")

        guard let orderRepository = orderRepository else {
            print("OrderViewModel: No repository found, using session orders only")
            // If no repository, use session orders only
            orders = OrderViewModel.sessionOrders
            isLoading = false
            return
        }

        print("OrderViewModel: Repository found, loading orders...")
        isLoading = true
        error = nil

        // Use a simpler approach - directly use the repository without complex flow collection
        Task { @MainActor in
            do {
                print("OrderViewModel: Getting order history from repository...")

                // Try to get orders directly from repository
                let ordersFlow = try await orderRepository.getOrderHistory()
                print("OrderViewModel: Got orders flow")

                // Use a simple collector that just gets the first emission
                let collector = SimpleOrderCollector { [weak self] orderArray in
                    DispatchQueue.main.async {
                        print("OrderViewModel: Received \(orderArray?.count ?? 0) orders from repository")

                        var repositoryOrders: [Order] = []
                        if let orderArray = orderArray {
                            repositoryOrders = orderArray.compactMap { $0 as? Order }
                        }

                        // Combine repository orders with session orders
                        var allOrders = repositoryOrders

                        // Add session orders that aren't already in repository
                        for sessionOrder in OrderViewModel.sessionOrders {
                            if !allOrders.contains(where: { $0.id == sessionOrder.id }) {
                                allOrders.append(sessionOrder)
                            }
                        }

                        print("OrderViewModel: Total orders (repository + session): \(allOrders.count)")
                        self?.orders = allOrders.sorted { $0.date > $1.date }
                        self?.isLoading = false
                    }
                }

                // Collect from the flow
                ordersFlow.collect(collector: collector) { error in
                    DispatchQueue.main.async {
                        if let error = error {
                            print("OrderViewModel: Flow collection error: \(error)")
                        }
                        // Even if there's an error, show session orders
                        if self.isLoading {
                            print("OrderViewModel: Using session orders due to flow error")
                            self.orders = OrderViewModel.sessionOrders.sorted { $0.date > $1.date }
                            self.isLoading = false
                        }
                    }
                }

            } catch {
                print("OrderViewModel: Error loading orders: \(error)")
                // Use session orders if repository fails
                self.orders = OrderViewModel.sessionOrders.sorted { $0.date > $1.date }
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



    // Store session orders for fallback
    @MainActor
    private static var sessionOrders: [Order] = []

    // Add order to session storage (fallback when repository doesn't work)
    func addOrderToSession(_ order: Order) {
        OrderViewModel.sessionOrders.append(order)
    }

    // No mock data - only use real orders from repository and session

    func clearError() {
        error = nil
    }

    func retryLoadOrders() {
        clearError()
        loadOrders()
    }
}

// Simple collector for order flows
class SimpleOrderCollector: NSObject, Kotlinx_coroutines_coreFlowCollector {
    private let onValue: (NSArray?) -> Void

    init(onValue: @escaping (NSArray?) -> Void) {
        self.onValue = onValue
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let orderArray = value as? NSArray {
            onValue(orderArray)
        } else {
            onValue(nil)
        }
        completionHandler(nil)
    }
}
