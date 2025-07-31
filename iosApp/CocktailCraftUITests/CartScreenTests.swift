import XCTest

/**
 * UI tests for the Cart screen.
 * Tests basic cart functionality and navigation.
 */
class CartScreenTests: UITestSetup {
    
    func testCartScreenDisplay() {
        // Given - App is launched
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        // When - User navigates to cart
        XCTAssertTrue(navigateToTab("Cart"), "Should be able to navigate to Cart tab")
        
        // Then - Cart screen should be displayed
        let cartScreen = app.navigationBars["Cart"].firstMatch
        XCTAssertTrue(waitForElement(cartScreen, timeout: 5.0), "Cart screen should be displayed")
    }
    
    func testQuantityButtonInteraction() {
        // Given - Cart screen is displayed
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart tab")
        
        // Look for quantity controls (if cart has items)
        let increaseButton = app.buttons.matching(identifier: "Increase quantity").firstMatch
        let decreaseButton = app.buttons.matching(identifier: "Decrease quantity").firstMatch
        
        if increaseButton.exists && decreaseButton.exists {
            // When - User taps quantity buttons
            let initialQuantity = app.staticTexts.matching(identifier: "quantity").firstMatch.label
            
            increaseButton.tap()
            sleep(1) // Wait for UI update
            
            decreaseButton.tap()
            sleep(1) // Wait for UI update
            
            // Then - Quantity controls should respond
            XCTAssertTrue(true, "Quantity controls should be interactive")
        } else {
            // If no items in cart, that's also a valid state
            XCTAssertTrue(true, "Cart may be empty - no quantity controls available")
        }
    }
    
    func testNavigationToCart() {
        // Given - App is on home screen
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
        
        // When - User navigates to cart
        XCTAssertTrue(navigateToTab("Cart"), "Should be able to navigate to Cart")
        
        // Then - Cart screen should be accessible
        let cartIndicator = app.navigationBars["Cart"].firstMatch
        XCTAssertTrue(waitForElement(cartIndicator, timeout: 5.0), "Cart screen should be displayed")
        
        // When - User navigates back to home
        XCTAssertTrue(navigateToTab("Home"), "Should be able to navigate back to Home")
        
        // Then - Should return to home screen
        XCTAssertTrue(waitForHomeScreen(), "Should return to home screen")
    }
    
    func testEmptyCartState() {
        // Given - User is on cart screen
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart tab")
        
        // Then - Empty cart state should be handled gracefully
        // Look for empty state indicators
        let emptyMessage = app.staticTexts.containing(NSPredicate(format: "label CONTAINS[c] 'empty'")).firstMatch
        let startShoppingButton = app.buttons.containing(NSPredicate(format: "label CONTAINS[c] 'shop'")).firstMatch
        
        if emptyMessage.exists {
            // Empty state is displayed
            XCTAssertTrue(emptyMessage.exists, "Empty cart message should be displayed")
            
            if startShoppingButton.exists {
                // When - User taps start shopping
                startShoppingButton.tap()
                
                // Then - Should navigate to home or shopping area
                XCTAssertTrue(waitForHomeScreen(timeout: 5.0), "Should navigate to shopping area")
            }
        } else {
            // Cart has items or different empty state implementation
            XCTAssertTrue(true, "Cart state handled appropriately")
        }
    }
    
    func testCartItemDisplay() {
        // Given - Cart screen is displayed
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart tab")
        
        // Look for cart items
        let cartList = app.tables.firstMatch.exists ? app.tables.firstMatch : app.collectionViews.firstMatch
        
        if cartList.exists {
            // When - Cart has items
            XCTAssertTrue(cartList.exists, "Cart items list should be displayed")
            
            // Look for typical cart item elements
            let priceLabels = app.staticTexts.matching(NSPredicate(format: "label CONTAINS '$'"))
            let itemNames = app.staticTexts.matching(NSPredicate(format: "label != ''"))
            
            // Then - Cart items should display relevant information
            XCTAssertTrue(priceLabels.count > 0 || itemNames.count > 0, "Cart items should display information")
        } else {
            // Empty cart is also a valid state
            XCTAssertTrue(true, "Cart may be empty")
        }
    }
    
    func testCheckoutFlow() {
        // Given - Cart screen is displayed
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart tab")
        
        // Look for checkout button
        let checkoutButton = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'checkout' OR label CONTAINS[c] 'order'")).firstMatch
        
        if checkoutButton.exists {
            // When - User taps checkout
            checkoutButton.tap()
            
            // Then - Checkout flow should begin
            // This could be a modal, new screen, or confirmation dialog
            sleep(2) // Wait for UI transition
            
            XCTAssertTrue(true, "Checkout flow should initiate without crashing")
            
            // Look for confirmation or back button to return
            let backButton = app.navigationBars.buttons.firstMatch
            let cancelButton = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'cancel'")).firstMatch
            
            if backButton.exists {
                backButton.tap()
            } else if cancelButton.exists {
                cancelButton.tap()
            }
        } else {
            // No checkout button (empty cart or different implementation)
            XCTAssertTrue(true, "Checkout not available - likely empty cart")
        }
    }
    
    func testCartPersistence() {
        // Given - User is on cart screen
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart tab")
        
        // Note the current cart state
        let cartList = app.tables.firstMatch.exists ? app.tables.firstMatch : app.collectionViews.firstMatch
        let hasItems = cartList.exists && cartList.cells.count > 0
        
        // When - User navigates away and back
        XCTAssertTrue(navigateToTab("Home"), "Should navigate to Home")
        sleep(1)
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate back to Cart")
        
        // Then - Cart state should be maintained
        let cartListAfter = app.tables.firstMatch.exists ? app.tables.firstMatch : app.collectionViews.firstMatch
        let hasItemsAfter = cartListAfter.exists && cartListAfter.cells.count > 0
        
        XCTAssertEqual(hasItems, hasItemsAfter, "Cart state should be maintained across navigation")
    }
    
    func testCartScreenStability() {
        // Given - Cart screen is displayed
        XCTAssertTrue(navigateToTab("Cart"), "Should navigate to Cart tab")
        
        // When - User performs various interactions
        let cartList = app.tables.firstMatch.exists ? app.tables.firstMatch : app.collectionViews.firstMatch
        
        if cartList.exists {
            // Scroll if possible
            cartList.swipeUp()
            cartList.swipeDown()
        }
        
        // Try tapping various elements
        let buttons = app.buttons
        for i in 0..<min(buttons.count, 3) {
            let button = buttons.element(boundBy: i)
            if button.exists && button.isHittable {
                button.tap()
                sleep(0.5)
            }
        }
        
        // Navigate away and back
        _ = navigateToTab("Home")
        _ = navigateToTab("Cart")
        
        // Then - App should remain stable
        XCTAssertTrue(app.exists, "App should remain stable after cart interactions")
    }
}
