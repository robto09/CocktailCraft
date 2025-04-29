import SwiftUI
import shared

@main
struct CocktailCraftApp: App {
    init() {
        // Initialize KMM modules
        KoinKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ContentView: View {
    var body: some View {
        MainScreen()
    }
}