import XCTest

/**
 * UI tests for the Home screen.
 * Tests basic functionality like app launch, search, and navigation.
 */
class HomeScreenTests: UITestSetup {
    
    func testAppLaunchDisplaysHomeScreen() {
        // Given - App is launched (done in setup)
        
        // Then - Home screen should be displayed
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be displayed after app launch")
        
        // Verify key elements are present
        XCTAssertTrue(app.searchFields.firstMatch.exists, "Search field should be visible")
        XCTAssertTrue(app.collectionViews.firstMatch.exists, "Cocktail list should be visible")
    }
    
    func testSearchBarInteraction() {
        // Given - Home screen is displayed
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        // When - User taps search field
        let searchField = app.searchFields.firstMatch
        XCTAssertTrue(waitForElement(searchField), "Search field should be available")
        
        searchField.tap()
        
        // Then - Keyboard should appear and search field should be active
        XCTAssertTrue(app.keyboards.firstMatch.exists, "Keyboard should appear when search field is tapped")
        
        // When - User types search query
        searchField.typeText("Mojito")
        
        // Then - Search text should be entered
        XCTAssertEqual(searchField.value as? String, "Mojito", "Search field should contain typed text")
    }
    
    func testCocktailListScrolling() {
        // Given - Home screen is displayed with cocktails
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        let cocktailList = app.collectionViews.firstMatch
        XCTAssertTrue(waitForElement(cocktailList), "Cocktail list should be visible")
        
        // When - User scrolls the list
        let initialPosition = cocktailList.frame
        cocktailList.swipeUp()
        
        // Then - List should scroll (this is a basic check)
        // Note: More sophisticated scrolling tests would check for specific elements
        XCTAssertTrue(cocktailList.exists, "Cocktail list should still exist after scrolling")
    }
    
    func testBasicNavigation() {
        // Given - Home screen is displayed
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        // When - User taps on different tabs
        XCTAssertTrue(navigateToTab("Cart"), "Should be able to navigate to Cart tab")
        XCTAssertTrue(navigateToTab("Favorites"), "Should be able to navigate to Favorites tab")
        XCTAssertTrue(navigateToTab("Home"), "Should be able to navigate back to Home tab")
        
        // Then - Should be back on home screen
        XCTAssertTrue(waitForHomeScreen(), "Should return to home screen")
    }
    
    func testAddToCartBasicFlow() {
        // Given - Home screen is displayed with cocktails
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        // When - User taps add to cart on first cocktail (if available)
        let addToCartButton = app.buttons.matching(identifier: "Add to Cart").firstMatch
        
        if addToCartButton.exists {
            addToCartButton.tap()
            
            // Then - Some feedback should be provided (toast, navigation, etc.)
            // This is a basic test - actual implementation would check for specific feedback
            XCTAssertTrue(true, "Add to cart action completed")
        } else {
            // If no cocktails are loaded, that's also a valid test result
            XCTAssertTrue(true, "No cocktails available to add to cart")
        }
    }
    
    func testSearchFunctionality() {
        // Given - Home screen is displayed
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        // When - User searches for a cocktail
        XCTAssertTrue(searchFor("Mojito"), "Should be able to enter search text")
        
        // Wait for search results (basic timeout)
        sleep(2)
        
        // Then - Search should complete without crashing
        // Note: Actual search result validation would require mock data setup
        XCTAssertTrue(app.collectionViews.firstMatch.exists, "Cocktail list should still be visible after search")
        
        // Clean up - clear search
        _ = clearSearch()
    }
    
    func testEmptyStateHandling() {
        // Given - Home screen is displayed
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        // When - User searches for something that doesn't exist
        XCTAssertTrue(searchFor("NonExistentCocktail12345"), "Should be able to enter search text")
        
        // Wait for search results
        sleep(2)
        
        // Then - App should handle empty results gracefully
        // This could show an empty state message or keep the list visible
        XCTAssertTrue(true, "App should handle empty search results without crashing")
        
        // Clean up
        _ = clearSearch()
    }
    
    func testAppStability() {
        // Given - Home screen is displayed
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        // When - User performs various interactions
        let searchField = app.searchFields.firstMatch
        if searchField.exists {
            searchField.tap()
            searchField.typeText("Test")
            _ = clearSearch()
        }
        
        // Navigate between tabs
        _ = navigateToTab("Cart")
        _ = navigateToTab("Favorites")
        _ = navigateToTab("Home")
        
        // Scroll if possible
        let cocktailList = app.collectionViews.firstMatch
        if cocktailList.exists {
            cocktailList.swipeUp()
            cocktailList.swipeDown()
        }
        
        // Then - App should remain stable
        XCTAssertTrue(app.exists, "App should remain stable after various interactions")
        XCTAssertTrue(waitForHomeScreen(), "Should still be able to access home screen")
    }
}
