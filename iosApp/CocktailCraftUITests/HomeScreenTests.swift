import XCTest

/**
 * Smoke tests for app launch, tab structure, and home search.
 */
class HomeScreenTests: UITestSetup {

    func testLaunchShowsHomeAndAllFiveTabs() {
        XCTAssertTrue(waitForHomeScreen(), "Home search field should appear after launch")

        for tab in ["Home", "Cart", "Favorites", "Orders", "Profile"] {
            XCTAssertTrue(app.tabBars.buttons[tab].exists, "Tab bar should contain '\(tab)'")
        }
    }

    func testHomeListsTheTestModeFixtures() {
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")

        // Test mode serves fixture cocktails, not the live API; Mojito is in
        // the default "Cocktail" category. Cards combine their text into one
        // accessibility element, so match on the label.
        let mojito = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", "Mojito")).firstMatch
        XCTAssertTrue(waitForElement(mojito, timeout: 10.0),
                      "Home should list the fixture cocktails")
    }

    func testSearchFieldAcceptsInput() {
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")

        homeSearchField.tap()
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 5.0),
                      "Keyboard should appear when the search field is tapped")

        homeSearchField.typeText("Mojito")
        XCTAssertEqual(homeSearchField.value as? String, "Mojito",
                       "Search field should contain the typed text")
    }

    func testTabRoundTripReturnsToHome() {
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")

        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart")
        XCTAssertTrue(waitForElement(app.navigationBars["Cart"]), "Cart chrome should appear")

        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites")
        XCTAssertTrue(waitForElement(app.navigationBars["Favorites"]), "Favorites chrome should appear")

        XCTAssertTrue(navigateToTab("Home"), "Should navigate back to Home")
        XCTAssertTrue(waitForHomeScreen(timeout: 5.0), "Home content should be restored")
    }
}
