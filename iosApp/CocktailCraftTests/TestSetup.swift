import XCTest
@testable import CocktailCraft
import shared

/**
 * Base test setup class for CocktailCraft unit tests.
 * Handles Koin initialization and common test configuration.
 */
class TestSetup: XCTestCase {
    
    override func setUpWithError() throws {
        // Initialize Koin for testing
        KoinIOSKt.doInitKoinIOS()
        
        // Set up test environment
        continueAfterFailure = false
    }
    
    override func tearDownWithError() throws {
        // Clean up after tests
        // Note: Koin cleanup would be handled here if needed
    }
    
    // MARK: - Helper Methods
    
    /**
     * Creates a test cocktail for use in tests
     */
    func createTestCocktail(id: String = "test_1", name: String = "Test Mojito") -> Cocktail {
        return Cocktail(
            id: id,
            name: name,
            category: "Cocktail",
            alcoholic: "Alcoholic",
            glass: "Highball glass",
            instructions: "Test instructions",
            image: "https://example.com/test.jpg",
            ingredients: ["Rum", "Mint", "Lime"],
            price: 12.99
        )
    }
    
    /**
     * Creates multiple test cocktails for list testing
     */
    func createTestCocktails(count: Int = 3) -> [Cocktail] {
        return (1...count).map { index in
            createTestCocktail(id: "test_\(index)", name: "Test Cocktail \(index)")
        }
    }
    
    /**
     * Waits for async operations to complete
     */
    func waitForAsync(timeout: TimeInterval = 2.0, completion: @escaping () -> Bool) {
        let expectation = XCTestExpectation(description: "Async operation")
        
        DispatchQueue.global().async {
            while !completion() {
                Thread.sleep(forTimeInterval: 0.1)
            }
            expectation.fulfill()
        }
        
        wait(for: [expectation], timeout: timeout)
    }
}
