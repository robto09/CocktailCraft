import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedOrderViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper.
 */
final class OrderViewModelSKIE: SharedViewModelWrapper<OrderUiState> {

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

    var orderStatistics: OrderStatistics {
        sharedViewModel.getOrderStatistics()
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedOrderViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedOrderViewModel()
        self.sharedViewModel = viewModel
        super.init(uiState: viewModel.uiState, errorFlow: viewModel.error)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

    // MARK: - Public Methods (using SKIE async/await)

    func loadOrders() async {
        await run { try await sharedViewModel.loadOrders() }
    }

    func placeOrder(cartItems: [CocktailCartItem], totalPrice: Double) async -> Bool {
        return await run(fallback: false) { try await sharedViewModel.placeOrder(cartItems: cartItems, totalPrice: totalPrice).boolValue }
    }

    func getOrderById(_ orderId: String) async -> Order? {
        return await run(fallback: nil as Order?) { try await sharedViewModel.getOrderById(orderId: orderId) }
    }

    func updateOrderStatus(_ orderId: String, status: String) async -> Bool {
        return await run(fallback: false) { try await sharedViewModel.updateOrderStatus(orderId: orderId, status: status).boolValue }
    }

    func cancelOrder(_ orderId: String) async -> Bool {
        return await run(fallback: false) { try await sharedViewModel.cancelOrder(orderId: orderId).boolValue }
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
        return sharedViewModel.getAverageOrderValue()
    }

    func formatAverageOrderValue() -> String {
        return formatPrice(getAverageOrderValue())
    }

    func getMostRecentOrder() -> Order? {
        return state.orders.first
    }

    func getOrdersThisMonth() -> [Order] {
        return sharedViewModel.getOrdersThisMonth()
    }
}
