import XCTest

/**
 * Smoke tests for the Favorites screen. A fresh install has no favorites,
 * so these assert the two legal states explicitly (empty message or grid).
 */
class FavoritesScreenTests: UITestSetup {

    func testFavoritesShowsEmptyStateOrGrid() {
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites tab")
        XCTAssertTrue(waitForElement(app.navigationBars["Favorites"]), "Favorites screen should be displayed")

        let grid = app.scrollViews.firstMatch
        XCTAssertTrue(emptyStateTitle.exists || grid.exists,
                      "Favorites must show its empty state or the favorites grid — it showed neither")
    }

    func testEmptyFavoritesBrowseGoesHome() {
        // Test mode launches with an empty store, so the empty state is certain
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites tab")
        XCTAssertTrue(waitForElement(emptyStateTitle), "Fresh favorites should be empty")

        app.buttons["emptyState.actionButton"].firstMatch.tap()

        XCTAssertTrue(waitForHomeScreen(timeout: 5.0), "Browse Cocktails should switch to Home")
        XCTAssertTrue(app.tabBars.buttons["Home"].isSelected, "Home tab should be selected")
    }

    func testFavoritesStateIsStableAcrossTabSwitches() {
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites tab")
        XCTAssertTrue(waitForElement(app.navigationBars["Favorites"]), "Favorites screen should be displayed")
        let wasEmpty = emptyStateTitle.exists

        XCTAssertTrue(navigateToTab("Home"), "Should navigate to Home")
        XCTAssertTrue(waitForHomeScreen(timeout: 5.0), "Home should be restored")
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate back to Favorites")
        XCTAssertTrue(waitForElement(app.navigationBars["Favorites"]), "Favorites screen should be displayed again")

        XCTAssertEqual(emptyStateTitle.exists, wasEmpty,
                       "Favorites should be unchanged by tab navigation")
    }
}
