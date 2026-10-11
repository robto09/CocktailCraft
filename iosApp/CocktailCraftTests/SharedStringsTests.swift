import XCTest
@testable import CocktailCraft
import shared

/// The shared strings (moko-resources) resolve from the bundle the build
/// phase copies into the app, with positional format arguments and plurals.
final class SharedStringsTests: XCTestCase {

    func testPlainString() {
        XCTAssertEqual(L(\.cart_empty_title), "Your cart is empty")
    }

    func testFormattedString() {
        XCTAssertEqual(
            L(\.confirm_order_message, "$12.00"),
            "Are you sure you want to place this order for $12.00?"
        )
    }

    func testPlural() {
        XCTAssertEqual(L(\.review_count, count: 1), "(1 review)")
        XCTAssertEqual(L(\.review_count, count: 3), "(3 reviews)")
    }
}
