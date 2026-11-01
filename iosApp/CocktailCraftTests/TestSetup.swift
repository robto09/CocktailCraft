import XCTest
@testable import CocktailCraft
import shared

/**
 * Base class for CocktailCraft unit tests that resolve dependencies through
 * Koin. The tests run inside the host app (TEST_HOST), whose launch already
 * started Koin — starting it again would throw Kotlin-side and kill the test
 * process. Under the tests the host starts it in test mode
 * (KoinInitializer.isTestMode: the scheme sets COCKTAILCRAFT_TEST_MODE), so
 * the graph runs over an in-memory database, throwaway settings and fixture
 * network responses: tests may write freely without touching the
 * simulator's real app data. setUp fails fast if that isn't the case.
 *
 * NOTE: the test targets are defined in project.yml (the project source of
 * truth — see iOS_Setup_Instructions.md). Run `xcodegen generate` EACH TIME
 * a target's file list changes (e.g. adding a test file), then run with
 * Product > Test.
 */
class TestSetup: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        XCTAssertTrue(KoinInitializer.isTestMode,
                      "The test host must run in test mode, or these tests would use the real app data")
    }
}

/// Polls `condition` on the main actor until it holds or `timeout` passes
/// (2s by default). Each sleep suspends, so the wrappers' observation tasks
/// get to run between checks.
@MainActor
func waitUntil(timeout: TimeInterval = 2.0,
               _ condition: () -> Bool) async -> Bool {
    let deadline = Date().addingTimeInterval(timeout)
    while Date() < deadline {
        if condition() { return true }
        try? await Task.sleep(nanoseconds: 50_000_000)
    }
    return condition()
}
