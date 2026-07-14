import XCTest

/**
 * Cold-launch deep-link smoke test: verifies the widget deep-link path
 * (cocktailcraft://cocktail/{id}) lands on the cocktail detail screen.
 *
 * XCUITest has no reliable way to hand a custom-scheme URL to the app under
 * test (Safari round-trips are flaky), so the app exposes a DEBUG-only
 * launch-environment hook — DEEPLINK_COCKTAIL_ID, read in ContentView —
 * that feeds the same AppRouter.openCocktailDetail(id:) entry point
 * onOpenURL uses for real widget taps.
 */
class DeepLinkTests: UITestSetup {

    /// Replaces (not extends) the base setUp: the launch environment must be
    /// set before launch, so the base class's plain `app.launch()` is skipped.
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        // "11007" is TheCocktailDB's Margarita — the same fixture id the
        // WidgetDeepLink unit tests use. Only Debug builds read this.
        app.launchEnvironment["DEEPLINK_COCKTAIL_ID"] = "11007"
        app.launch()
    }

    func testColdLaunchDeepLinkShowsCocktailDetail() {
        // detail.screen marks CocktailDetailView's root in every branch
        // (loading/loaded/error), so this asserts the navigation itself
        // without depending on live network data.
        let detailScreen = app.otherElements["detail.screen"].firstMatch
        XCTAssertTrue(waitForElement(detailScreen, timeout: 15.0),
                      "Deep link should push the cocktail detail screen on launch")

        // The detail screen is pushed onto the Home tab's stack.
        XCTAssertTrue(app.tabBars.buttons["Home"].isSelected,
                      "Deep link should land on the Home tab")
    }
}
