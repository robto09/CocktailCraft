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

        XCTAssertTrue(emptyStateTitle.exists || placeOrderButton.exists,
                      "Cart must show its empty state or an order summary — it showed neither")
    }

    func testEmptyCartStartShoppingGoesHome() {
        // Test mode launches with an empty store, so the empty state is certain
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart tab")
        XCTAssertTrue(waitForElement(emptyStateTitle), "A fresh cart should be empty")

        app.buttons["emptyState.actionButton"].firstMatch.tap()

        XCTAssertTrue(waitForHomeScreen(timeout: 5.0), "Start Shopping should switch to Home")
        XCTAssertTrue(app.tabBars.buttons["Home"].isSelected, "Home tab should be selected")
    }

    func testCartStateIsStableAcrossTabSwitches() {
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart tab")
        XCTAssertTrue(waitForElement(app.navigationBars["Cart"]), "Cart screen should be displayed")
        let wasEmpty = emptyStateTitle.exists

        XCTAssertTrue(navigateToTab("Home"), "Should navigate to Home")
        XCTAssertTrue(waitForHomeScreen(timeout: 5.0), "Home should be restored")
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate back to Cart")
        XCTAssertTrue(waitForElement(app.navigationBars["Cart"]), "Cart screen should be displayed again")

        XCTAssertEqual(emptyStateTitle.exists, wasEmpty,
                       "Cart contents should be unchanged by tab navigation")
    }
}
