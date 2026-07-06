import XCTest

/**
 * Smoke tests for the Cart screen. A fresh install has an empty cart, so
 * these assert the two legal states explicitly (empty message or order UI)
 * instead of flows that require a seeded cart.
 */
class CartScreenTests: UITestSetup {

    func testCartShowsEmptyStateOrOrderSummary() {
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart tab")
        XCTAssertTrue(waitForElement(app.navigationBars["Cart"]), "Cart screen should be displayed")

        let emptyMessage = app.staticTexts["Your cart is empty"]
        let placeOrderButton = app.buttons["Place Order"]
        XCTAssertTrue(emptyMessage.exists || placeOrderButton.exists,
                      "Cart must show its empty state or an order summary — it showed neither")
    }

    func testCartStateIsStableAcrossTabSwitches() {
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart tab")
        XCTAssertTrue(waitForElement(app.navigationBars["Cart"]), "Cart screen should be displayed")
        let wasEmpty = app.staticTexts["Your cart is empty"].exists

        XCTAssertTrue(navigateToTab("Home"), "Should navigate to Home")
        XCTAssertTrue(waitForHomeScreen(timeout: 5.0), "Home should be restored")
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate back to Cart")
        XCTAssertTrue(waitForElement(app.navigationBars["Cart"]), "Cart screen should be displayed again")

        XCTAssertEqual(app.staticTexts["Your cart is empty"].exists, wasEmpty,
                       "Cart contents should be unchanged by tab navigation")
    }
}
