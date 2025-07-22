import SwiftUI
@preconcurrency import shared
import Combine

@MainActor
class OrderViewModel: ObservableObject {
    @Published var orders: [Order] = []
    @Published var isLoading = false
    @Published var isPlacingOrder = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil

    private let orderRepository: OrderRepository?
    private var localOrders: [Order] = []
    private var cancellables = Set<AnyCancellable>()

    // Singleton instance
    static let shared = OrderViewModel()

    private init() {
        self.orderRepository = KoinInitializer.shared.getOrderRepository()
        setupMockOrders()
        loadOrders()
    }

    func loadOrders() {
        print("OrderViewModel.loadOrders() called")

        guard let orderRepository = orderRepository else {
            print("OrderViewModel: No repository found, using session orders only")
            // If no repository, use session orders only
            orders = localOrders
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

                // Use SKIE AsyncSequence pattern - cast to proper type
                if let asyncFlow = ordersFlow as? any AsyncSequence {
                    for try await orderArray in asyncFlow {
                        await MainActor.run {
                            if let orderList = orderArray as? NSArray {
                                print("OrderViewModel: Received \(orderList.count) orders from repository")
                                var repositoryOrders: [Order] = []
                                repositoryOrders = orderList.compactMap { $0 as? Order }

                                // Merge with local orders
                                let allOrders = repositoryOrders + self.localOrders
                                self.orders = allOrders.sorted { $0.date > $1.date }
                            } else {
                                // Fallback to local orders
                                self.orders = self.localOrders.sorted { $0.date > $1.date }
                            }
                            self.isLoading = false
                        }
                        break // Take first emission and exit
                    }
                } else {
                    // Fallback to local orders
                    await MainActor.run {
                        self.orders = self.localOrders.sorted { $0.date > $1.date }
                        self.isLoading = false
                    }
                }
            } catch {
                await MainActor.run {
                    print("OrderViewModel: Error loading orders: \(error)")
                    self.orders = self.localOrders.sorted { $0.date > $1.date }
                    self.isLoading = false
                }
            }
        }
    }

    func loadOrdersFromRepository() async {
        // This method is kept for compatibility but now calls loadOrders()
        loadOrders()
    }

    func placeOrder(order: Order) async {
        await MainActor.run {
            self.isPlacingOrder = true
        }

        // Add to local orders immediately for offline support
        await MainActor.run {
            self.localOrders.append(order)
            self.orders = (self.orders + [order]).sorted { $0.date > $1.date }
        }

        // Try to sync with repository if available
        if let orderRepository = orderRepository {
            do {
                print("OrderViewModel: Placing order through repository")

                // Place order through repository - must be called on main thread
                let placeOrderFlow = try await orderRepository.placeOrder(order: order)

                // Use SKIE AsyncSequence pattern - cast to proper type
                if let asyncFlow = placeOrderFlow as? any AsyncSequence {
                    for try await success in asyncFlow {
                        await MainActor.run {
                            if let boolValue = success as? KotlinBoolean, let successValue = boolValue.boolValue as? Bool, successValue {
                                print("OrderViewModel: Order placed successfully through repository")
                            } else {
                                print("OrderViewModel: Failed to place order through repository, keeping local copy")
                            }
                            self.isPlacingOrder = false
                        }
                        break // Take first emission
                    }
                } else {
                    await MainActor.run {
                        self.isPlacingOrder = false
                    }
                }
            } catch {
                await MainActor.run {
                    print("OrderViewModel: Error placing order through repository: \(error), keeping local copy")
                    self.isPlacingOrder = false
                }
            }
        } else {
            await MainActor.run {
                print("OrderViewModel: No repository available, order saved locally only")
                self.isPlacingOrder = false
            }
        }
    }

    // MARK: - Private Methods

    private func setupMockOrders() {
        // Create some mock orders for demonstration
        let orderItem1 = OrderItem(name: "Margarita", quantity: 2, price: 12.99)
        let mockOrder1 = Order(
            id: "mock-1",
            date: "2024-01-15",
            items: [orderItem1],
            total: 25.98,
            status: "completed"
        )

        let orderItem2 = OrderItem(name: "A1", quantity: 1, price: 8.50)
        let mockOrder2 = Order(
            id: "mock-2",
            date: "2024-01-14",
            items: [orderItem2],
            total: 8.50,
            status: "pending"
        )

        self.localOrders = [mockOrder1, mockOrder2]
        self.orders = self.localOrders.sorted { $0.date > $1.date }
    }
}

