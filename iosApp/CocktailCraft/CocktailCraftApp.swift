import SwiftUI
import shared
import BackgroundTasks
#if DEBUG
import DebugSwift
#endif

@main
struct CocktailCraftApp: App {
    #if DEBUG
    static var debugSwift = DebugSwift()
    #endif

    init() {
        // Initialize Koin
        KoinInitializer.instance.initialize()

        // Initialize background sync
        Task { @MainActor in
            BackgroundSyncManager.shared.registerBackgroundTasks()
        }

        // Initialize DebugSwift for QA debugging
        #if DEBUG
        setupDebugSwift()
        #endif
    }

    #if DEBUG
    private func setupDebugSwift() {
        // Configure network monitoring for TheCocktailDB API
        configureNetworkMonitoring()

        // Configure custom debug actions for CocktailCraft
        configureCustomDebugActions()

        // Configure app-specific information
        configureAppInfo()

        // Setup DebugSwift with configuration optimized for CocktailCraft
        CocktailCraftApp.debugSwift.setup(
            hideFeatures: [], // Show all features for comprehensive debugging
            disable: [] // Enable all monitoring capabilities
        )

        // Show DebugSwift interface
        CocktailCraftApp.debugSwift.show()
    }

    private func configureNetworkMonitoring() {
        // Monitor only TheCocktailDB API calls for focused debugging
        DebugSwift.Network.shared.onlyURLs = [
            "https://www.thecocktaildb.com",
            "http://www.thecocktaildb.com"
        ]

        // Ignore analytics or other non-essential URLs if any
        DebugSwift.Network.shared.ignoredURLs = []
    }

    private func configureCustomDebugActions() {
        // Add custom debugging actions specific to CocktailCraft
        DebugSwift.App.shared.customAction = {
            [
                .init(title: "CocktailCraft Debug Tools", actions: [
                    .init(title: "Clear Favorites Cache") {
                        // Clear favorites from UserDefaults
                        UserDefaults.standard.removeObject(forKey: "favorites")
                        print("🗑️ Favorites cache cleared")
                    },
                    .init(title: "Clear Cart Data") {
                        // Clear cart data
                        UserDefaults.standard.removeObject(forKey: "cart")
                        print("🛒 Cart data cleared")
                    },
                    .init(title: "Reset App State") {
                        // Reset all app state
                        UserDefaults.standard.removeObject(forKey: "favorites")
                        UserDefaults.standard.removeObject(forKey: "cart")
                        UserDefaults.standard.removeObject(forKey: "searchHistory")
                        print("🔄 App state reset")
                    },
                    .init(title: "Force Network Error") {
                        // Simulate network error for testing
                        print("🌐 Network error simulation triggered")
                    }
                ])
            ]
        }
    }

    private func configureAppInfo() {
        // Add app-specific information for debugging
        DebugSwift.App.shared.customInfo = {
            [
                .init(
                    title: "CocktailCraft Info",
                    infos: [
                        .init(title: "App Version", subtitle: "1.0"),
                        .init(title: "API", subtitle: "TheCocktailDB (Free Tier)"),
                        .init(title: "Platform", subtitle: "iOS (Kotlin Multiplatform)"),
                        .init(title: "Architecture", subtitle: "MVVM + Clean Architecture"),
                        .init(title: "DI Framework", subtitle: "Koin"),
                        .init(title: "UI Framework", subtitle: "SwiftUI")
                    ]
                )
            ]
        }
    }
    #endif
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}


// MARK: - Koin Initializer
class KoinInitializer {
    static let instance = KoinInitializer()

    private init() {}

    func initialize() {
        // Start Koin with all modules using SKIE generated function
        let koinApplication = shared.doInitKoin()
        _koin = koinApplication.koin
    }

    private var _koin: Koin_coreKoin?
    var koin: Koin_coreKoin {
        return _koin!
    }
}

// Global accessor for Koin - use lazy initialization
var koin: Koin_coreKoin {
    return KoinInitializer.instance.koin
}

#if DEBUG
// MARK: - Shake to Toggle DebugSwift
extension UIWindow {
    open override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        super.motionEnded(motion, with: event)

        if motion == .motionShake {
            // Toggle DebugSwift interface
            CocktailCraftApp.debugSwift.toggle()
        }
    }
}
#endif

// MARK: - Global Koin Helper Functions
// Using global functions to avoid namespace conflicts

// Constructing KoinHelper resolves nothing, so this global is safe as long as
// Koin itself is initialized before the first getter call (KoinInitializer
// runs in CocktailCraftApp.init(), before any view constructs a ViewModel).
private let sharedKoinHelperInstance = shared.KoinHelper()

func getSharedKoinHelper() -> KoinHelper {
    return sharedKoinHelperInstance
}
