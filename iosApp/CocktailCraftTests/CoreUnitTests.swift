import XCTest
import SwiftUI
@testable import CocktailCraft
import shared

/// Pure Swift routing logic — no Koin needed.
@MainActor
final class AppRouterTests: XCTestCase {

    func testDefaultTabIsHome() {
        XCTAssertEqual(AppRouter().selectedTab, .home)
    }

    func testTabSwitchIsObservedValue() {
        let router = AppRouter()
        router.selectedTab = .orders
        XCTAssertEqual(router.selectedTab, .orders)
    }
}

/// Shared DeliveryPolicy through the SKIE bridge — validates both the
/// Kotlin rules and that the object bridges correctly into Swift.
final class DeliveryPolicyBridgeTests: XCTestCase {

    func testFreeDeliveryThreshold() {
        XCTAssertFalse(DeliveryPolicy.shared.isFreeDelivery(subtotal: 49.99))
        XCTAssertTrue(DeliveryPolicy.shared.isFreeDelivery(subtotal: 50.0))
    }

    func testDeliveryFee() {
        XCTAssertEqual(DeliveryPolicy.shared.deliveryFee(subtotal: 10.0), 5.99, accuracy: 0.001)
        XCTAssertEqual(DeliveryPolicy.shared.deliveryFee(subtotal: 75.0), 0.0, accuracy: 0.001)
    }

    func testEstimatedDeliveryTimeBuckets() {
        XCTAssertEqual(DeliveryPolicy.shared.estimatedDeliveryTime(itemCount: 0), "No items")
        XCTAssertEqual(DeliveryPolicy.shared.estimatedDeliveryTime(itemCount: 2), "15-20 minutes")
        XCTAssertEqual(DeliveryPolicy.shared.estimatedDeliveryTime(itemCount: 9), "25-30 minutes")
    }
}

/// Swift-side presentation mapping on the order wrapper (Koin-backed).
@MainActor
final class OrderStatusMappingTests: TestSetup {

    func testStatusColors() {
        let vm = OrderViewModelSKIE()
        XCTAssertEqual(vm.getOrderStatusColor("delivered"), .green)
        XCTAssertEqual(vm.getOrderStatusColor("cancelled"), .red)
        XCTAssertEqual(vm.getOrderStatusColor("unknown-status"), .gray)
    }

    func testStatusIcons() {
        let vm = OrderViewModelSKIE()
        XCTAssertEqual(vm.getOrderStatusIcon("shipped"), "shippingbox")
        XCTAssertEqual(vm.getOrderStatusIcon("pending"), "clock")
    }
}
