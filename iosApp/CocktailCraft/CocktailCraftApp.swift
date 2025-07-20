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

// Use KoinHelper to get repositories without reified generics
extension KoinInitializer {
    func getCocktailRepository() -> CocktailRepository? {
        let helper = KoinHelper()
        return helper.getCocktailRepository()
    }

    func getAuthRepository() -> AuthRepository? {
        let helper = KoinHelper()
        return helper.getAuthRepository()
    }

    func getCartRepository() -> CartRepository? {
        let helper = KoinHelper()
        return helper.getCartRepository()
    }

    func getOrderRepository() -> OrderRepository? {
        let helper = KoinHelper()
        return helper.getOrderRepository()
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