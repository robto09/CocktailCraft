import XCTest

/**
 * Base class for CocktailCraft UI tests: launches the app and provides
 * helpers grounded in the real UI (tab labels from ContentView, the home
 * search field's placeholder, screen navigation titles).
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
    /// search field (matched by placeholder).
    var homeSearchField: XCUIElement {
        app.textFields["Search cocktails..."].firstMatch
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
