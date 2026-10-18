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
        XCTAssertFalse(DeliveryPolicy.shared.isFreeDelivery(subtotal: Money(cents: 4999)))
        XCTAssertTrue(DeliveryPolicy.shared.isFreeDelivery(subtotal: Money(cents: 5000)))
    }

    func testDeliveryFee() {
        XCTAssertEqual(DeliveryPolicy.shared.deliveryFee(subtotal: Money(cents: 1000)).cents, 599)
        XCTAssertEqual(DeliveryPolicy.shared.deliveryFee(subtotal: Money(cents: 7500)).cents, 0)
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
        let belowThreshold = CartUiState(cartItems: [], totalPrice: Money(cents: 1000), itemCount: 1, isLoading: false)
        XCTAssertEqual(belowThreshold.deliveryFee.cents, 599)
        XCTAssertEqual(belowThreshold.finalTotal.cents, 1599)

        let free = CartUiState(cartItems: [], totalPrice: Money(cents: 5000), itemCount: 5, isLoading: false)
        XCTAssertEqual(free.deliveryFee.cents, 0)
        XCTAssertEqual(free.finalTotal.cents, 5000)
    }
}

/// The shared Money type reaches Swift as a typed class (not a bare Int64,
/// as a Kotlin value class would), with its formatter and arithmetic.
final class MoneyBridgeTests: XCTestCase {

    func testFormatsUsDollarsThroughTheSharedFormatter() {
        XCTAssertEqual(Money(cents: 1250).formatted(languageTag: "en-US"), "$12.50")
        XCTAssertEqual(Money(cents: 123_456_789).formatted(languageTag: "en-US"), "$1,234,567.89")
        // The device locale's number style, still in dollars
        let german = Money(cents: 123_450).formatted(languageTag: "de-DE")
        XCTAssertTrue(german.contains("1.234,50"), german)
        XCTAssertTrue(german.contains("$"), german)
    }

    func testArithmeticAndEqualityAreInCents() {
        XCTAssertEqual(Money(cents: 1250).plus(other: Money(cents: 599)), Money(cents: 1849))
        XCTAssertEqual(Money(cents: 1250).times(quantity: 3).cents, 3750)
        XCTAssertEqual(CocktailCartItem(cocktail: SnapshotFixtures.margarita, quantity: 2).lineTotal.cents, 2500)
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
