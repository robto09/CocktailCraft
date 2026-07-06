import XCTest
@testable import CocktailCraft
import shared

/**
 * Base class for CocktailCraft unit tests. Starts Koin once per process
 * (startKoin throws on double-start) so tests can construct the Swift
 * wrapper ViewModels, which resolve their shared VMs through Koin.
 *
 * NOTE: the test targets are defined in project.yml — run
 * `xcodegen generate && pod install` once to (re)wire them into the
 * Xcode project, then run with Product > Test.
 */
class TestSetup: XCTestCase {

    private static var koinStarted = false

    override func setUpWithError() throws {
        if !Self.koinStarted {
            _ = KoinIOSKt.doInitKoin()
            Self.koinStarted = true
        }
        continueAfterFailure = false
    }
}
