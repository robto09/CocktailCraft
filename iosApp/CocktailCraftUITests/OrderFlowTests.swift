import XCTest

/**
 * End-to-end flow over the test-mode fixtures: add a cocktail to the cart
 * from its detail screen, remove it and Undo, place the order, open it from
 * the order list, reorder it and cancel it there. Starts on the Margarita detail screen
 * through the DEBUG deep-link hook (see DeepLinkTests).
 */
class OrderFlowTests: UITestSetup {

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = makeApp()
        app.launchEnvironment["DEEPLINK_COCKTAIL_ID"] = "11007"
        app.launch()
    }

    func testAddFromDetailUndoRemovalThenPlaceReorderAndCancelOrder() {
        // Add to cart from the detail screen
        let addToCart = app.buttons["detail.addToCartButton"].firstMatch
        XCTAssertTrue(waitForElement(addToCart, timeout: 15.0), "Detail should offer Add to Cart")
        addToCart.tap()

        // Cart now has the item; remove it and take the removal back
        XCTAssertTrue(navigateToTab("Cart"))
        XCTAssertTrue(waitForElement(placeOrderButton), "The added cocktail should be in the cart")
        app.buttons["cart.removeButton"].firstMatch.tap()
        let undo = app.buttons["toast.actionButton"].firstMatch
        XCTAssertTrue(waitForElement(undo), "Removing should offer Undo")
        undo.tap()
        XCTAssertTrue(waitForElement(placeOrderButton), "Undo should put the cocktail back")

        // Place the order; checkout switches to the Orders tab
        placeOrderButton.tap()
        let confirm = app.alerts.buttons["Confirm"].firstMatch
        XCTAssertTrue(waitForElement(confirm))
        confirm.tap()

        // Open the order and cancel it
        let orderCard = app.buttons["orders.card"].firstMatch
        XCTAssertTrue(waitForElement(orderCard, timeout: 10.0), "The placed order should be listed")
        orderCard.tap()

        // Reorder puts the order's one item back in the (now empty) cart
        let reorder = app.buttons["orderDetail.reorderButton"].firstMatch
        XCTAssertTrue(waitForElement(reorder), "A new order should offer Reorder")
        XCTAssertTrue(reorder.isEnabled, "A new order's items know their cocktail")
        reorder.tap()
        let added = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", "Added 1 item to your cart")).firstMatch
        XCTAssertTrue(waitForElement(added), "Reorder should confirm with a toast")

        let cancel = app.buttons["orderDetail.cancelButton"].firstMatch
        XCTAssertTrue(waitForElement(cancel), "A new order should be cancellable")
        cancel.tap()
        let confirmCancel = app.alerts.buttons["Cancel Order"].firstMatch
        XCTAssertTrue(waitForElement(confirmCancel), "Cancelling should ask for confirmation")
        confirmCancel.tap()

        let gone = NSPredicate(format: "exists == false")
        expectation(for: gone, evaluatedWith: cancel)
        waitForExpectations(timeout: 5.0)
        XCTAssertTrue(app.staticTexts["Cancelled"].firstMatch.exists, "The order should show as cancelled")

        // The reordered item is waiting in the cart
        XCTAssertTrue(navigateToTab("Cart"))
        XCTAssertTrue(waitForElement(placeOrderButton), "Reorder should have filled the cart")
    }
}
