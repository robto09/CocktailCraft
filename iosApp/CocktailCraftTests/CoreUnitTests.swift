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
        XCTAssertEqual(DeliveryPolicy.shared.estimatedDeliveryTime(itemCount: 0).localized(), "No items")
        XCTAssertEqual(DeliveryPolicy.shared.estimatedDeliveryTime(itemCount: 2).localized(), "15-20 minutes")
        XCTAssertEqual(DeliveryPolicy.shared.estimatedDeliveryTime(itemCount: 9).localized(), "25-30 minutes")
    }
}

/// Derived values on the shared UiState classes (the single state surface)
/// bridge as plain Swift properties, so views can read them off `state`.
final class UiStateDerivedValueBridgeTests: XCTestCase {

    func testCartStateDerivesDeliveryPricing() {
        let belowThreshold = CartUiState(cartItems: [], totalPrice: 10.0, itemCount: 1, isLoading: false)
        XCTAssertEqual(belowThreshold.deliveryFee, 5.99, accuracy: 0.001)
        XCTAssertEqual(belowThreshold.finalTotal, 15.99, accuracy: 0.001)

        let free = CartUiState(cartItems: [], totalPrice: 50.0, itemCount: 5, isLoading: false)
        XCTAssertEqual(free.deliveryFee, 0.0, accuracy: 0.001)
        XCTAssertEqual(free.finalTotal, 50.0, accuracy: 0.001)
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
