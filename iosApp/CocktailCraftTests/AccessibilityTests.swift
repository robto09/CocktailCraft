import XCTest
import SwiftUI
@testable import CocktailCraft

/// Accessibility behavior that isn't visible in screenshots: how the in-app
/// font setting combines with the system Dynamic Type size, and the spoken
/// form of ratings. (Screen-level checks live in the UI tests'
/// AccessibilityAuditTests.)
final class AccessibilityTests: XCTestCase {

    func testDefaultFontSettingFollowsSystemTextSize() {
        // "Medium" must never pin the size, or system accessibility sizes
        // would be ignored
        for size in DynamicTypeSize.allCases {
            XCTAssertEqual(ContentView.dynamicTypeSize(for: "medium", system: size), size)
        }
    }

    func testFontSettingStepsFromTheDefaultSystemSize() {
        XCTAssertEqual(ContentView.dynamicTypeSize(for: "small", system: .large), .small)
        XCTAssertEqual(ContentView.dynamicTypeSize(for: "large", system: .large), .xLarge)
        XCTAssertEqual(ContentView.dynamicTypeSize(for: "xlarge", system: .large), .xxLarge)
    }

    func testFontSettingStepsFromAccessibilitySizesAndClamps() {
        XCTAssertEqual(ContentView.dynamicTypeSize(for: "large", system: .accessibility3), .accessibility4)
        XCTAssertEqual(ContentView.dynamicTypeSize(for: "xlarge", system: .accessibility5), .accessibility5)
        XCTAssertEqual(ContentView.dynamicTypeSize(for: "small", system: .xSmall), .xSmall)
    }

    func testSpokenRatingKeepsAtMostOneDecimal() {
        XCTAssertEqual(StarRatingView.spokenRating(4.5), "4.5")
        XCTAssertEqual(StarRatingView.spokenRating(3), "3")
        XCTAssertEqual(L(\.review_stars_a11y, StarRatingView.spokenRating(4.5), 5), "4.5 out of 5 stars")
    }
}
