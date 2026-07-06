import XCTest

/**
 * Base UI test setup class for CocktailCraft UI tests.
 * Handles app launch and common UI test configuration.
 */
class UITestSetup: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        // Stop on first failure for clearer debugging
        continueAfterFailure = false
        
        // Initialize and configure the app
        app = XCUIApplication()
        
        // Launch the app
        app.launch()
    }
    
    override func tearDownWithError() throws {
        // Clean up
        app = nil
    }
    
    // MARK: - Helper Methods
    
    /**
     * Waits for an element to appear with timeout
     */
    func waitForElement(_ element: XCUIElement, timeout: TimeInterval = 5.0) -> Bool {
        return element.waitForExistence(timeout: timeout)
    }
    
    /**
     * Taps an element if it exists
     */
    func tapIfExists(_ element: XCUIElement) -> Bool {
        if element.exists {
            element.tap()
            return true
        }
        return false
    }
    
    /**
     * Scrolls to find and tap an element
     */
    func scrollToAndTap(_ identifier: String, in scrollView: XCUIElement) -> Bool {
        let element = app.buttons[identifier].firstMatch
        
        // Try to find element without scrolling first
        if element.exists && element.isHittable {
            element.tap()
            return true
        }
        
        // Scroll to find element
        var attempts = 0
        let maxAttempts = 10
        
        while attempts < maxAttempts {
            if element.exists && element.isHittable {
                element.tap()
                return true
            }
            
            scrollView.swipeUp()
            attempts += 1
        }
        
        return false
    }
    
    /**
     * Waits for app to reach home screen
     */
    func waitForHomeScreen(timeout: TimeInterval = 10.0) -> Bool {
        let homeIndicator = app.navigationBars["Home"].firstMatch
        return waitForElement(homeIndicator, timeout: timeout)
    }
    
    /**
     * Navigates to a specific tab
     */
    func navigateToTab(_ tabName: String) -> Bool {
        let tabButton = app.tabBars.buttons[tabName].firstMatch
        if waitForElement(tabButton, timeout: 5.0) {
            tabButton.tap()
            return true
        }
        return false
    }
    
    /**
     * Types text in a search field
     */
    func searchFor(_ text: String) -> Bool {
        let searchField = app.searchFields.firstMatch
        if waitForElement(searchField, timeout: 5.0) {
            searchField.tap()
            searchField.typeText(text)
            return true
        }
        return false
    }
    
    /**
     * Clears search field
     */
    func clearSearch() -> Bool {
        let clearButton = app.buttons["Clear text"].firstMatch
        return tapIfExists(clearButton)
    }
}
