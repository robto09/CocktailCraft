import SwiftUI
import shared
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

        // White back chevrons/bar buttons on the branded coral navigation
        // bar (see View.brandedNavigationBar in Theme/AppTheme.swift).
        // SwiftUI-rendered toolbar items follow .tint, but the UIKit-provided
        // back indicator follows this appearance proxy.
        UINavigationBar.appearance().tintColor = .white

        // Keep the home-screen widgets' snapshots current — except in test
        // mode, whose fixture data must not overwrite the real widgets'
        if !KoinInitializer.isTestMode {
            WidgetDataBridge.shared.start()
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

        // Show DebugSwift interface — except under the accessibility audit
        // UI tests, which must audit only what ships (DebugSwift's floating
        // overlay is Debug-only and not built for VoiceOver)
        if ProcessInfo.processInfo.environment["HIDE_DEBUG_OVERLAY"] == nil {
            CocktailCraftApp.debugSwift.show()
        }
    }

    private func configureNetworkMonitoring() {
        // Monitor only TheCocktailDB API calls for focused debugging
        DebugSwift.Network.shared.onlyURLs = [
            "https://www.thecocktaildb.com"
        ]

        // Ignore analytics or other non-essential URLs if any
        DebugSwift.Network.shared.ignoredURLs = []
    }

    private func configureCustomDebugActions() {
        // QA actions over the real data stores. Favorites, cart, orders and
        // reviews live in the shared SQLDelight database (not UserDefaults),
        // so each action goes through a KoinHelper debug hook; the screens
        // observe the database and update on their own.
        DebugSwift.App.shared.customAction = {
            let helper = getSharedKoinHelper()
            let simulatingFailure = helper.debugIsSimulatingNetworkFailure()
            return [
                .init(title: "CocktailCraft Debug Tools", actions: [
                    .init(title: "Clear Favorites") {
                        Task { try? await helper.debugClearFavorites() }
                    },
                    .init(title: "Clear Cart") {
                        Task { try? await helper.debugClearCart() }
                    },
                    .init(title: "Delete All User Data (cart, favorites, orders, reviews)") {
                        Task { try? await helper.debugResetUserData() }
                    },
                    // Every API request fails as a dropped connection would
                    // (retries, then cache fallback / error UI) until toggled off
                    .init(title: simulatingFailure
                          ? "Stop Simulating Network Failure"
                          : "Simulate Network Failure") {
                        helper.debugSetSimulatedNetworkFailure(enabled: !simulatingFailure)
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
                        .init(title: "App Version",
                              subtitle: "\(Bundle.main.appVersion) (\(Bundle.main.buildNumber))"),
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
        // Start Koin with all modules using SKIE generated function.
        // Swift never touches the Koin container directly — everything is
        // resolved through the shared KoinHelper below.
        if Self.isTestMode {
            #if DEBUG
            // Same graph over an in-memory database, throwaway settings and
            // fixture network responses (shared/src/iosMain/IosTestMode.kt)
            _ = shared.doInitKoinForTesting()
            #endif
        } else {
            // Release builds pass false so shared code keeps only
            // warning/error logs.
            #if DEBUG
            let isDebug = true
            #else
            let isDebug = false
            #endif
            _ = shared.doInitKoin(isDebug: isDebug)
        }
    }

    /// True when the app runs under the unit or UI tests, which must never
    /// read or write the simulator's real app data or depend on the live
    /// API. Set by COCKTAILCRAFT_TEST_MODE (the scheme's Test action, for
    /// the unit-test host; UITestSetup's launch environment, for UI tests),
    /// or implied when XCTest has injected a test bundle into this process.
    /// Debug builds only: Release always runs against the real stores.
    static let isTestMode: Bool = {
        #if DEBUG
        let environment = ProcessInfo.processInfo.environment
        return environment["COCKTAILCRAFT_TEST_MODE"] != nil
            || environment["XCTestConfigurationFilePath"] != nil
        #else
        return false
        #endif
    }()
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

// MARK: - App Version

extension Bundle {
    /// Marketing version ("1.0"), from MARKETING_VERSION in project.yml.
    var appVersion: String {
        infoDictionary?["CFBundleShortVersionString"] as? String ?? L(\.common_unknown)
    }

    /// Build number ("1"), from CURRENT_PROJECT_VERSION in project.yml.
    var buildNumber: String {
        infoDictionary?["CFBundleVersion"] as? String ?? L(\.common_unknown)
    }
}

// MARK: - Global Koin Helper Functions
// Using global functions to avoid namespace conflicts

// Constructing KoinHelper resolves nothing, so this global is safe as long as
// Koin itself is initialized before the first getter call (KoinInitializer
// runs in CocktailCraftApp.init(), before any view constructs a ViewModel).
private let sharedKoinHelperInstance = shared.KoinHelper()

func getSharedKoinHelper() -> KoinHelper {
    return sharedKoinHelperInstance
}
