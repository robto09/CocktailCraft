import XCTest

/**
 * End-to-end flow over the test-mode fixtures: add a cocktail to the cart
 * from its detail screen, remove it and Undo, place the order, open it from
 * the order list and cancel it there. Starts on the Margarita detail screen
 * through the DEBUG deep-link hook (see DeepLinkTests).
 */
class OrderFlowTests: UITestSetup {

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = makeApp()
        app.launchEnvironment["DEEPLINK_COCKTAIL_ID"] = "11007"
        app.launch()
    }

    func testAddFromDetailUndoRemovalThenPlaceAndCancelOrder() {
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
    }
}
