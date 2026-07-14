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
