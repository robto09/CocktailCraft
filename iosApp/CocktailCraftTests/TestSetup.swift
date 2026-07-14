import XCTest
@testable import CocktailCraft
import shared

/**
 * Base class for CocktailCraft unit tests that resolve dependencies through
 * Koin. The tests run inside the host app (TEST_HOST), whose launch already
 * calls doInitKoin() — starting Koin again would throw Kotlin-side and kill
 * the test process, so this base only asserts the graph is reachable.
 *
 * NOTE: the test targets are defined in project.yml (the project source of
 * truth — see iOS_Setup_Instructions.md). Run `xcodegen generate && pod
 * install` EACH TIME a target's file list changes (e.g. adding a test file),
 * then run with Product > Test.
 */
class TestSetup: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }
}
