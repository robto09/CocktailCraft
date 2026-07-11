import XCTest

/**
 * Base class for CocktailCraft UI tests: launches the app and provides
 * helpers keyed to stable accessibility identifiers (home.searchField,
 * emptyState.title, cart.placeOrderButton, ...) rather than display copy,
 * so copy changes and localization can't break element lookup. Tab-bar
 * buttons and navigation-bar titles remain label-matched: SwiftUI offers
 * no per-tabItem identifier hook, and chrome titles are structural.
 *
 * These are smoke tests against live data — they assert structure that is
 * state-independent (tabs, screen chrome, empty-state-or-content) rather
 * than flows that need a seeded cart/favorites store.
 */
class UITestSetup: XCTestCase {

    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    override func tearDownWithError() throws {
        app = nil
    }

    // MARK: - Helper Methods

    func waitForElement(_ element: XCUIElement, timeout: TimeInterval = 5.0) -> Bool {
        return element.waitForExistence(timeout: timeout)
    }

    /// The home screen has no navigation bar; its stable marker is the
    /// search field (matched by accessibility identifier).
    var homeSearchField: XCUIElement {
        app.textFields["home.searchField"].firstMatch
    }

    /// Title of whichever EmptyStateView is on screen (cart/favorites/...).
    var emptyStateTitle: XCUIElement {
        app.staticTexts["emptyState.title"].firstMatch
    }

    var placeOrderButton: XCUIElement {
        app.buttons["cart.placeOrderButton"].firstMatch
    }

    /// Cold launches include the shared framework + Koin start; be generous.
    @discardableResult
    func waitForHomeScreen(timeout: TimeInterval = 15.0) -> Bool {
        return waitForElement(homeSearchField, timeout: timeout)
    }

    @discardableResult
    func navigateToTab(_ tabName: String) -> Bool {
        let tabButton = app.tabBars.buttons[tabName].firstMatch
        guard waitForElement(tabButton, timeout: 5.0) else { return false }
        tabButton.tap()
        return true
    }
}
