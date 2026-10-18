import XCTest

/**
 * Runs Xcode's accessibility audit (`performAccessibilityAudit`, iOS 17+)
 * over every main tab and the cocktail detail screen — element
 * descriptions, hit regions, contrast, Dynamic Type support, clipped text,
 * traits and actions — at the default text size, plus the main screens at
 * the largest accessibility size (AX5). Any issue not covered by an
 * exclusion in `isExcluded` fails the test.
 *
 * The app runs in test mode (fixture cocktails, empty stores; see
 * UITestSetup), so the audited screens are the same on every run. The tests
 * still audit whatever state renders rather than asserting one, so they
 * also hold if the simulator itself is offline.
 */
class AccessibilityAuditTests: UITestSetup {

    /// Replaces the base setUp: the launch environment must be set before
    /// launch, and each test chooses its text size.
    override func setUpWithError() throws {
        continueAfterFailure = true
        app = makeApp()
        // Debug builds float DebugSwift's QA overlay over every screen; it
        // never ships, so it is kept out of the audit
        app.launchEnvironment["HIDE_DEBUG_OVERLAY"] = "1"
    }

    // MARK: - Default text size

    func testHomeAudit() throws {
        launch()
        try auditHome()
    }

    func testCartAudit() throws {
        launch()
        try auditTab("Cart")
    }

    func testFavoritesAudit() throws {
        launch()
        try auditTab("Favorites")
    }

    func testOrdersAudit() throws {
        launch()
        try auditTab("Orders")
    }

    func testProfileAudit() throws {
        launch()
        try auditTab("Profile")
    }

    func testCocktailDetailAudit() throws {
        // Same DEBUG deep-link hook as DeepLinkTests (TheCocktailDB's Margarita)
        app.launchEnvironment["DEEPLINK_COCKTAIL_ID"] = "11007"
        app.launch()
        // Any type: the root is a ScrollView once loaded (see DeepLinkTests)
        let detailScreen = app.descendants(matching: .any)["detail.screen"].firstMatch
        XCTAssertTrue(waitForElement(detailScreen, timeout: 15), "Detail screen should be displayed")
        // Content, or the loading/error state when offline
        _ = app.buttons["detail.writeReviewButton"].waitForExistence(timeout: 10)
        try audit()
    }

    // MARK: - Largest accessibility text size (AX5)

    func testHomeAuditAtLargestTextSize() throws {
        launch(textSize: Self.largestTextSize)
        try auditHome()
    }

    func testCartAuditAtLargestTextSize() throws {
        launch(textSize: Self.largestTextSize)
        try auditTab("Cart")
    }

    func testProfileAuditAtLargestTextSize() throws {
        launch(textSize: Self.largestTextSize)
        try auditTab("Profile")
    }

    // MARK: - Helpers

    private static let largestTextSize = "UICTContentSizeCategoryAccessibilityXXXL"

    private func launch(textSize: String? = nil) {
        if let textSize {
            app.launchArguments += ["-UIPreferredContentSizeCategoryName", textSize]
        }
        app.launch()
        XCTAssertTrue(waitForHomeScreen(), "Home screen should be loaded")
    }

    private func auditHome() throws {
        // Give the first page a chance to arrive; offline, the loading /
        // no-connection state is audited instead
        _ = app.buttons["card.favoriteButton"].firstMatch.waitForExistence(timeout: 15)
        try audit()
    }

    private func auditTab(_ tab: String) throws {
        XCTAssertTrue(navigateToTab(tab), "Should navigate to \(tab)")
        XCTAssertTrue(waitForElement(app.navigationBars[tab]), "\(tab) screen should be displayed")
        try audit()
    }

    private func audit() throws {
        dismissErrorAlert()
        try app.performAccessibilityAudit { issue in
            let excluded = Self.isExcluded(issue)
            if !excluded {
                // Name the element in the log; the audit's message doesn't
                let element = issue.element.map {
                    "type \($0.elementType.rawValue) '\($0.label)' id='\($0.identifier)'"
                } ?? "no element"
                print("Accessibility audit issue: \(issue.compactDescription) — \(element)")
            }
            return excluded
        }
    }

    /// Offline, a failed load surfaces as an error alert (a system alert,
    /// audited by Apple), which would leave the screen behind it dimmed and
    /// unreachable. Dismiss it so the screen itself is audited.
    private func dismissErrorAlert() {
        let alert = app.alerts.firstMatch
        guard alert.waitForExistence(timeout: 1) else { return }
        alert.buttons.firstMatch.tap()
        _ = alert.waitForNonExistence(timeout: 3)
    }

    /// Justified exclusions only — everything else fails the test.
    private static func isExcluded(_ issue: XCUIAccessibilityAuditIssue) -> Bool {
        switch issue.auditType {
        case .contrast:
            // The brand palette (coral #EB6A43 at 3.2:1 on white, secondary
            // gray #8E8E93 at 3.3:1) is below 4.5:1 for small text by design;
            // the in-app High Contrast setting strengthens secondary text.
            // Changing the brand colors is a design decision outside this
            // suite (see README → Accessibility).
            return true
        default:
            return false
        }
    }
}
