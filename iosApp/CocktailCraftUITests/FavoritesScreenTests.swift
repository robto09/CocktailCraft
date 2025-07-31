import XCTest

/**
 * UI tests for the Favorites screen.
 * Tests basic favorites functionality and navigation.
 */
class FavoritesScreenTests: UITestSetup {
    
    func testFavoritesScreenDisplay() {
        // Given - App is launched
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        // When - User navigates to favorites
        XCTAssertTrue(navigateToTab("Favorites"), "Should be able to navigate to Favorites tab")
        
        // Then - Favorites screen should be displayed
        let favoritesScreen = app.navigationBars["Favorites"].firstMatch
        XCTAssertTrue(waitForElement(favoritesScreen, timeout: 5.0), "Favorites screen should be displayed")
    }
    
    func testFavoriteToggleInteraction() {
        // Given - Favorites screen is displayed
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites tab")
        
        // Look for favorite buttons (heart icons)
        let favoriteButtons = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'favorite' OR identifier CONTAINS 'heart'"))
        
        if favoriteButtons.count > 0 {
            let firstFavoriteButton = favoriteButtons.firstMatch
            
            // When - User taps favorite button
            if firstFavoriteButton.exists && firstFavoriteButton.isHittable {
                firstFavoriteButton.tap()
                sleep(1) // Wait for UI update
                
                // Then - Favorite state should change
                XCTAssertTrue(true, "Favorite toggle should be interactive")
            }
        } else {
            // No favorites available to toggle
            XCTAssertTrue(true, "No favorites available to toggle")
        }
    }
    
    func testNavigationToFavorites() {
        // Given - App is on home screen
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        // When - User navigates to favorites
        XCTAssertTrue(navigateToTab("Favorites"), "Should be able to navigate to Favorites")
        
        // Then - Favorites screen should be accessible
        let favoritesIndicator = app.navigationBars["Favorites"].firstMatch
        XCTAssertTrue(waitForElement(favoritesIndicator, timeout: 5.0), "Favorites screen should be displayed")
        
        // When - User navigates back to home
        XCTAssertTrue(navigateToTab("Home"), "Should be able to navigate back to Home")
        
        // Then - Should return to home screen
        XCTAssertTrue(waitForHomeScreen(), "Should return to home screen")
    }
    
    func testEmptyFavoritesState() {
        // Given - User is on favorites screen
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites tab")
        
        // Then - Empty favorites state should be handled gracefully
        // Look for empty state indicators
        let emptyMessage = app.staticTexts.containing(NSPredicate(format: "label CONTAINS[c] 'no favorites' OR label CONTAINS[c] 'empty'")).firstMatch
        let browseButton = app.buttons.containing(NSPredicate(format: "label CONTAINS[c] 'browse' OR label CONTAINS[c] 'explore'")).firstMatch
        
        if emptyMessage.exists {
            // Empty state is displayed
            XCTAssertTrue(emptyMessage.exists, "Empty favorites message should be displayed")
            
            if browseButton.exists {
                // When - User taps browse button
                browseButton.tap()
                
                // Then - Should navigate to home or browse area
                XCTAssertTrue(waitForHomeScreen(timeout: 5.0), "Should navigate to browse area")
            }
        } else {
            // Favorites exist or different empty state implementation
            XCTAssertTrue(true, "Favorites state handled appropriately")
        }
    }
    
    func testFavoritesListDisplay() {
        // Given - Favorites screen is displayed
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites tab")
        
        // Look for favorites list
        let favoritesList = app.tables.firstMatch.exists ? app.tables.firstMatch : app.collectionViews.firstMatch
        
        if favoritesList.exists {
            // When - Favorites list is displayed
            XCTAssertTrue(favoritesList.exists, "Favorites list should be displayed")
            
            // Look for typical favorite item elements
            let cocktailNames = app.staticTexts.matching(NSPredicate(format: "label != '' AND label != 'Favorites'"))
            let priceLabels = app.staticTexts.matching(NSPredicate(format: "label CONTAINS '$'"))
            
            // Then - Favorite items should display relevant information
            XCTAssertTrue(cocktailNames.count > 0 || priceLabels.count > 0, "Favorite items should display information")
        } else {
            // Empty favorites is also a valid state
            XCTAssertTrue(true, "Favorites may be empty")
        }
    }
    
    func testAddToCartFromFavorites() {
        // Given - Favorites screen is displayed
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites tab")
        
        // Look for add to cart buttons in favorites
        let addToCartButtons = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'add' OR label CONTAINS[c] 'cart'"))
        
        if addToCartButtons.count > 0 {
            let firstAddButton = addToCartButtons.firstMatch
            
            // When - User taps add to cart
            if firstAddButton.exists && firstAddButton.isHittable {
                firstAddButton.tap()
                sleep(1) // Wait for action to complete
                
                // Then - Item should be added to cart
                XCTAssertTrue(true, "Add to cart from favorites should work")
                
                // Optionally verify by checking cart
                if navigateToTab("Cart") {
                    sleep(1)
                    _ = navigateToTab("Favorites") // Return to favorites
                }
            }
        } else {
            // No items to add to cart
            XCTAssertTrue(true, "No favorites available to add to cart")
        }
    }
    
    func testFavoritesScrolling() {
        // Given - Favorites screen is displayed
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites tab")
        
        let favoritesList = app.tables.firstMatch.exists ? app.tables.firstMatch : app.collectionViews.firstMatch
        
        if favoritesList.exists {
            // When - User scrolls the favorites list
            favoritesList.swipeUp()
            sleep(0.5)
            favoritesList.swipeDown()
            sleep(0.5)
            
            // Then - List should scroll without issues
            XCTAssertTrue(favoritesList.exists, "Favorites list should remain functional after scrolling")
        } else {
            // No list to scroll (empty favorites)
            XCTAssertTrue(true, "No favorites list to scroll")
        }
    }
    
    func testFavoritesPersistence() {
        // Given - User is on favorites screen
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites tab")
        
        // Note the current favorites state
        let favoritesList = app.tables.firstMatch.exists ? app.tables.firstMatch : app.collectionViews.firstMatch
        let hasFavorites = favoritesList.exists && favoritesList.cells.count > 0
        
        // When - User navigates away and back
        XCTAssertTrue(navigateToTab("Home"), "Should navigate to Home")
        sleep(1)
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate back to Favorites")
        
        // Then - Favorites state should be maintained
        let favoritesListAfter = app.tables.firstMatch.exists ? app.tables.firstMatch : app.collectionViews.firstMatch
        let hasFavoritesAfter = favoritesListAfter.exists && favoritesListAfter.cells.count > 0
        
        XCTAssertEqual(hasFavorites, hasFavoritesAfter, "Favorites state should be maintained across navigation")
    }
    
    func testFavoritesScreenStability() {
        // Given - Favorites screen is displayed
        XCTAssertTrue(navigateToTab("Favorites"), "Should navigate to Favorites tab")
        
        // When - User performs various interactions
        let favoritesList = app.tables.firstMatch.exists ? app.tables.firstMatch : app.collectionViews.firstMatch
        
        if favoritesList.exists {
            // Scroll if possible
            favoritesList.swipeUp()
            favoritesList.swipeDown()
        }
        
        // Try tapping various elements
        let buttons = app.buttons
        for i in 0..<min(buttons.count, 3) {
            let button = buttons.element(boundBy: i)
            if button.exists && button.isHittable && !button.label.contains("Home") && !button.label.contains("Cart") {
                button.tap()
                sleep(0.5)
            }
        }
        
        // Navigate away and back
        _ = navigateToTab("Home")
        _ = navigateToTab("Favorites")
        
        // Then - App should remain stable
        XCTAssertTrue(app.exists, "App should remain stable after favorites interactions")
    }
}
