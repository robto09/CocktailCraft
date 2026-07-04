import SwiftUI
import shared
import Observation

/**
 * iOS ViewModel wrapper for SharedOrderViewModel using pure SKIE integration.
 * Mirrors the consolidated uiState as Observation-tracked state.
 */
@MainActor
@Observable
class OrderViewModelSKIE {
    // Consolidated UI state from the shared ViewModel
    private(set) var state: OrderUiState
    // The single error channel from the shared ViewModel base class
    var error: ErrorHandler.UserFriendlyError? = nil

    // Computed properties
    var hasOrders: Bool {
        !state.orders.isEmpty
    }

    var isEmpty: Bool {
        state.orders.isEmpty
    }

    var recentOrders: [Order] {
        sharedViewModel.getRecentOrders()
    }

    var orderStatistics: [String: Any] {
        sharedViewModel.getOrderStatistics()
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedOrderViewModel

    // Tasks for async observation
    @ObservationIgnored private var observationTasks: [Task<Void, Never>] = []

    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedOrderViewModel()

        // Seed synchronously so the first frame renders the current state
        self.state = sharedViewModel.uiState.value

        // Start observing StateFlows using SKIE async/await
        startObserving()
    }

    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        // Note: Do NOT call onCleared() — this wraps a Koin `single` whose
        // coroutine scope must survive the lifetime of any one wrapper.
        // (Factory-scoped wrappers — CocktailDetail, Review — do call it.)
    }

    // MARK: - SKIE StateFlow Observation

    private func startObserving() {
        // These Tasks inherit @MainActor, so assignments land on the main thread.
        observationTasks.append(Task { [weak self] in
            guard let flow = self?.sharedViewModel.uiState else { return }
            for await state in flow {
                self?.state = state
            }
        })

        observationTasks.append(Task { [weak self] in
            guard let flow = self?.sharedViewModel.error else { return }
            for await errorValue in flow {
                self?.error = errorValue
            }
        })
    }

    // MARK: - Public Methods (using SKIE async/await)

    func loadOrders() async {
        do {
            try await sharedViewModel.loadOrders()
        } catch {
            // Handle error silently
        }
    }

    func placeOrder(cartItems: [CocktailCartItem], totalPrice: Double) async -> Bool {
        print("Swift: placeOrder called with \(cartItems.count) items, total: \(totalPrice)")
        return await withCheckedContinuation { continuation in
            print("Swift: About to call placeOrderWithCallback")
            sharedViewModel.placeOrderWithCallback(cartItems: cartItems, totalPrice: totalPrice) { result in
                print("Swift: Callback received with result: \(result.boolValue)")
                continuation.resume(returning: result.boolValue)
            }
        }
    }

    func getOrderById(_ orderId: String) async -> Order? {
        do {
            return try await sharedViewModel.getOrderById(orderId: orderId)
        } catch {
            return nil
        }
    }

    func updateOrderStatus(_ orderId: String, status: String) async -> Bool {
        do {
            return try await sharedViewModel.updateOrderStatus(orderId: orderId, status: status).boolValue
        } catch {
            return false
        }
    }

    func cancelOrder(_ orderId: String) async -> Bool {
        do {
            return try await sharedViewModel.cancelOrder(orderId: orderId).boolValue
        } catch {
            return false
        }
    }

    func reorderItems(_ orderId: String) async -> Bool {
        do {
            return try await sharedViewModel.reorderItems(orderId: orderId).boolValue
        } catch {
            return false
        }
    }

    // MARK: - Synchronous Methods

    func getOrdersByStatus(_ status: String) -> [Order] {
        return sharedViewModel.getOrdersByStatus(status: status)
    }

    func getTotalSpent() -> Double {
        return sharedViewModel.getTotalSpent()
    }

    func getOrderHistory(limit: Int = 10) -> [Order] {
        return sharedViewModel.getOrderHistory(limit: Int32(limit))
    }

    func getOrdersByDateRange(startDate: String, endDate: String) -> [Order] {
        return sharedViewModel.getOrdersByDateRange(startDate: startDate, endDate: endDate)
    }

    func canCancelOrder(_ orderId: String) -> Bool {
        return sharedViewModel.canCancelOrder(orderId: orderId)
    }

    func getOrderStatusDisplay(_ status: String) -> String {
        return sharedViewModel.getOrderStatusDisplay(status: status)
    }

    func getEstimatedDeliveryTime(_ orderId: String) -> String {
        return sharedViewModel.getEstimatedDeliveryTime(orderId: orderId)
    }

    func clearError() {
        sharedViewModel.clearError()
    }

    func refresh() {
        sharedViewModel.refresh()
    }

    // MARK: - Helper Methods for SwiftUI

    func formatPrice(_ price: Double) -> String {
        return price.asPrice
    }

    func formatTotalSpent() -> String {
        return formatPrice(state.totalSpent)
    }

    func getOrderStatusColor(_ status: String) -> Color {
        switch status.lowercased() {
        case "pending":
            return .orange
        case "processing":
            return .blue
        case "shipped":
            return .purple
        case "delivered":
            return .green
        case "cancelled":
            return .red
        default:
            return .gray
        }
    }

    func getOrderStatusIcon(_ status: String) -> String {
        switch status.lowercased() {
        case "pending":
            return "clock"
        case "processing":
            return "gear"
        case "shipped":
            return "shippingbox"
        case "delivered":
            return "checkmark.circle"
        case "cancelled":
            return "xmark.circle"
        default:
            return "questionmark.circle"
        }
    }

    func getOrdersGroupedByStatus() -> [String: [Order]] {
        return Dictionary(grouping: state.orders) { $0.status }
    }

    func getAverageOrderValue() -> Double {
        guard !state.orders.isEmpty else { return 0.0 }
        return state.orders.reduce(0.0) { $0 + $1.total } / Double(state.orders.count)
    }

    func formatAverageOrderValue() -> String {
        return formatPrice(getAverageOrderValue())
    }

    func getMostRecentOrder() -> Order? {
        return state.orders.first
    }

    func getOrdersThisMonth() -> [Order] {
        let calendar = Calendar.current
        let now = Date()
        let startOfMonth = calendar.dateInterval(of: .month, for: now)?.start ?? now

        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let startDateString = formatter.string(from: startOfMonth)
        let endDateString = formatter.string(from: now)

        return getOrdersByDateRange(startDate: startDateString, endDate: endDateString)
    }
}
