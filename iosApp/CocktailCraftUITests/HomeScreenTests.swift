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

    func testSortMenuReordersTheList() {
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        let mojito = card(named: "Mojito")
        let afterglow = card(named: "Afterglow")
        XCTAssertTrue(waitForElement(mojito, timeout: 10.0), "Home should list the fixture cocktails")
        XCTAssertTrue(waitForElement(afterglow), "Home should list the fixture cocktails")

        let sortMenu = app.buttons["home.sortMenu"].firstMatch
        XCTAssertTrue(waitForElement(sortMenu), "The sort menu should be in the navigation bar")
        XCTAssertEqual(sortMenu.label, "Sort cocktails")
        XCTAssertEqual(sortMenu.value as? String, "Default")
        XCTAssertLessThan(mojito.frame.minY, afterglow.frame.minY, "Catalog order before sorting")

        sortMenu.tap()
        let nameOption = app.buttons["Name A–Z"].firstMatch
        XCTAssertTrue(waitForElement(nameOption), "The menu should offer the sort options")
        nameOption.tap()

        XCTAssertEqual(sortMenu.value as? String, "Name A–Z", "The control should speak the new sort")
        // The fixture category lists Afterglow, Mojito, Old Fashioned by name
        XCTAssertLessThan(afterglow.frame.minY, mojito.frame.minY, "Afterglow should now come first")
    }

    func testViewedCocktailAppearsInRecentlyViewed() {
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        let recentlyViewed = app.descendants(matching: .any)["home.recentlyViewed"].firstMatch
        let mojito = card(named: "Mojito")
        XCTAssertTrue(waitForElement(mojito, timeout: 10.0), "Home should list the fixture cocktails")
        XCTAssertFalse(recentlyViewed.exists, "Nothing has been viewed in a fresh test launch")

        mojito.tap()
        XCTAssertTrue(waitForElement(app.buttons["detail.shareButton"].firstMatch),
                      "The detail page should open")
        // The system back button leads the detail page's navigation bar
        app.navigationBars.buttons.element(boundBy: 0).tap()

        let recentMojito = app.buttons["home.recentlyViewed.11000"].firstMatch
        XCTAssertTrue(waitForElement(recentMojito, timeout: 5.0),
                      "Coming back to Home should show the viewed cocktail in Recently viewed")
        XCTAssertEqual(recentMojito.label, "Mojito")

        homeSearchField.tap()
        homeSearchField.typeText("Mo")
        XCTAssertTrue(recentMojito.waitForNonExistence(timeout: 5.0),
                      "Recently viewed should hide while a search query is active")
    }

    /// A cocktail card on Home; cards combine their text into one element.
    private func card(named name: String) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label BEGINSWITH %@", "\(name),")).firstMatch
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
