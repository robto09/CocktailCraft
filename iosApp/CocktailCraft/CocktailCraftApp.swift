import SwiftUI
import shared

@main
struct CocktailCraftApp: App {
    
    init() {
        // Initialize Koin
        KoinInitializer.shared.initialize()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

// MARK: - Koin Initializer
class KoinInitializer {
    static let shared = KoinInitializer()
    
    private init() {}
    
    func initialize() {
        // Start Koin with all modules
        let koinApplication = KoinKt.doInitKoin()
        _koin = koinApplication.koin
    }
    
    private var _koin: Koin_coreKoin?
    var koin: Koin_coreKoin {
        return _koin!
    }
}

// Global accessor for Koin
let koin = KoinInitializer.shared.koin