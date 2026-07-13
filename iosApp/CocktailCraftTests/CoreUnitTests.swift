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

/// AppColors must build its light-mode brand colors from the cross-target
/// BrandColorComponents tokens (WidgetBridge.swift) that the widget
/// extension also compiles — this pins the app half of that contract so a
/// reverted hex literal can't silently diverge from the widgets.
final class BrandColorParityTests: XCTestCase {

    private func assertColor(_ color: Color,
                             matches components: (red: Double, green: Double, blue: Double),
                             _ name: String) {
        var red: CGFloat = 0, green: CGFloat = 0, blue: CGFloat = 0, alpha: CGFloat = 0
        XCTAssertTrue(UIColor(color).getRed(&red, green: &green, blue: &blue, alpha: &alpha))
        XCTAssertEqual(Double(red), components.red, accuracy: 0.001, "\(name) red")
        XCTAssertEqual(Double(green), components.green, accuracy: 0.001, "\(name) green")
        XCTAssertEqual(Double(blue), components.blue, accuracy: 0.001, "\(name) blue")
    }

    func testAppPrimaryMatchesSharedBrandTokens() {
        assertColor(AppColors.primaryLight, matches: BrandColorComponents.primary, "primary")
    }

    func testAppSecondaryMatchesSharedBrandTokens() {
        assertColor(AppColors.secondaryLight, matches: BrandColorComponents.secondary, "secondary")
    }
}

/// Pure budget math extracted from BackgroundSyncManager.performSync (IO-7):
/// the shared sync service gets the platform allowance minus a 5s teardown
/// margin, and never a negative budget.
final class BackgroundSyncBudgetTests: XCTestCase {

    func testForegroundAllowanceLeavesTeardownMargin() {
        XCTAssertEqual(BackgroundSyncManager.syncBudgetMs(maxDuration: 30), 25_000)
    }

    func testBackgroundAllowanceLeavesTeardownMargin() {
        XCTAssertEqual(BackgroundSyncManager.syncBudgetMs(maxDuration: 25), 20_000)
    }

    func testSubMarginAllowanceIsFlooredAtZero() {
        XCTAssertEqual(BackgroundSyncManager.syncBudgetMs(maxDuration: 3), 0)
    }
}
