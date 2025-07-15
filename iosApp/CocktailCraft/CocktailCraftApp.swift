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
        let koinApplication = KoinIOSKt.doInitKoin()
        _koin = koinApplication.koin
    }

    private var _koin: Koin_coreKoin?
    var koin: Koin_coreKoin {
        return _koin!
    }
}

// Global accessor for Koin - use lazy initialization
var koin: Koin_coreKoin {
    return KoinInitializer.shared.koin
}

// Temporary placeholder - disable dependency injection for now
// TODO: Replace with proper Koin dependency injection once reified generics issue is resolved
extension KoinInitializer {
    func getCocktailRepository() -> CocktailRepository? {
        // Return nil for now - ViewModels will use mock data instead
        return nil
    }

    func getAuthRepository() -> AuthRepository? {
        // Return nil for now - ViewModels will use mock data instead
        return nil
    }
}

// MARK: - Temporary Content View
struct TemporaryContentView: View {
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Image(systemName: "wineglass")
                    .font(.system(size: 60))
                    .foregroundColor(.blue)

                Text("CocktailCraft")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                Text("iOS App")
                    .font(.title2)
                    .foregroundColor(.secondary)

                Text("Building shared framework...")
                    .font(.body)
                    .foregroundColor(.orange)
                    .padding()
                    .background(Color.orange.opacity(0.1))
                    .cornerRadius(8)

                Spacer()
            }
            .padding()
            .navigationTitle("CocktailCraft")
        }
    }
}