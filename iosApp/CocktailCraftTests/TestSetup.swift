import XCTest
@testable import CocktailCraft
import shared

/**
 * Base class for CocktailCraft unit tests that resolve dependencies through
 * Koin. The tests run inside the host app (TEST_HOST), whose launch already
 * calls doInitKoin() — starting Koin again would throw Kotlin-side and kill
 * the test process, so this base only asserts the graph is reachable.
 *
 * NOTE: the test targets are defined in project.yml — run
 * `xcodegen generate && pod install` once to (re)wire them into the
 * Xcode project, then run with Product > Test.
 */
class TestSetup: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }
}
