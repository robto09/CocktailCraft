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

    func testHomeSortOptionsHaveSharedTitles() {
        XCTAssertEqual(HomeSortOption.allCases.map(\.title), [
            "Default", "Name A–Z", "Price: low to high", "Price: high to low", "Rating", "Popularity"
        ])
        XCTAssertEqual(L(\.home_sort_current_a11y, HomeSortOption.rating.title), "Sorted by Rating")
    }

    func testEscapedCharactersSurviveGeneration() {
        XCTAssertEqual(L(\.offline_you_are_offline), "You're offline — showing saved cocktails.")
        XCTAssertEqual(L(\.search_no_results_query, "Mojito"), "No cocktails found matching \"Mojito\"")
    }

    func testSharedCodeMessagesLocalize() {
        let error = ErrorHandler.shared.getErrorFromException(
            exception: KotlinIllegalStateException(message: nil),
            defaultMessage: ResourceStringDesc(stringRes: MR.strings.shared.error_generic),
            recoveryAction: nil
        )
        XCTAssertEqual(error.title.localized(), "Data Error")
        XCTAssertEqual(error.message.localized(), "Invalid data received. Please try again.")
    }

    /// No translations ship yet, so a non-English locale must fall back to the
    /// base (English) strings rather than show keys.
    func testLocaleWithoutTranslationsFallsBackToBase() {
        StringDescCompanion.shared.localeType = StringDescLocaleType.Custom(locale: "de")
        defer { StringDescCompanion.shared.localeType = StringDescLocaleType.System.shared }

        XCTAssertEqual(L(\.cart_empty_title), "Your cart is empty")
    }
}
