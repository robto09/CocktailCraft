import SwiftUI
import shared

@main
struct CocktailCraftApp: App {
    
    init() {
        // Initialize Koin
        KoinInitializer.instance.initialize()
    }
    
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

// MARK: - Global Koin Helper Functions
// Using global functions to avoid namespace conflicts

func getSharedKoinHelper() -> KoinHelper {
    return shared.KoinHelper()
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