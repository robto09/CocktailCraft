import SwiftUI
import shared

@main
struct CocktailCraftApp: App {
    @StateObject private var authViewModel = DependencyContainer.shared.makeAuthViewModel()
    @Environment(\.scenePhase) private var scenePhase
    
    private let offlineCoordinator = DependencyContainer.shared.get(objCClass: OfflineModeCoordinator.self) as! OfflineModeCoordinator
    
    var body: some Scene {
        WindowGroup {
            ZStack(alignment: .top) {
                if authViewModel.authState.isAuthenticated {
                    mainContent
                } else {
                    AuthScreen()
                }
                
                // Offline mode status bar
                OfflineModeStatusBar(coordinator: offlineCoordinator)
            }
            .onChange(of: scenePhase) { newPhase in
                switch newPhase {
                case .active:
                    handleAppActive()
                case .inactive:
                    handleAppInactive()
                case .background:
                    handleAppBackground()
                @unknown default:
                    break
                }
            }
        }
    }
    
    private var mainContent: some View {
        TabView {
            NavigationView {
                HomeTab(viewModel: DependencyContainer.shared.makeMainViewModel())
            }
            .tabItem {
                Label("Home", systemImage: "house.fill")
            }
            
            NavigationView {
                FavoritesScreen()
            }
            .tabItem {
                Label("Favorites", systemImage: "heart.fill")
            }
            
            NavigationView {
                CartScreen()
            }
            .tabItem {
                Label("Cart", systemImage: "cart.fill")
            }
            
            NavigationView {
                OrderListScreen()
            }
            .tabItem {
                Label("Orders", systemImage: "list.bullet.rectangle.fill")
            }
            
            NavigationView {
                UserProfileScreen()
            }
            .tabItem {
                Label("Profile", systemImage: "person.fill")
            }
        }
        .accentColor(.primary)
    }
    
    private func handleAppActive() {
        // Check for deep links, refresh tokens, etc.
        if authViewModel.authState.isAuthenticated {
            // Refresh token if needed
            refreshTokenIfNeeded()
            
            // Trigger data sync
            Task {
                try? await offlineCoordinator.synchronizeData()
            }
        }
    }
    
    private func handleAppInactive() {
        // Save any pending state
    }
    
    private func handleAppBackground() {
        // Final sync attempt before going to background
        Task {
            try? await offlineCoordinator.synchronizeData()
        }
    }
    
    private func refreshTokenIfNeeded() {
        Task {
            do {
                if let token = try await authViewModel.getAccessToken() {
                    let tokenComponents = token.components(separatedBy: ".")
                    if tokenComponents.count == 3 {
                        // Check token expiration
                        if let payload = tokenComponents[1].decodeJWTPayload(),
                           let exp = payload["exp"] as? TimeInterval,
                           Date(timeIntervalSince1970: exp) < Date().addingTimeInterval(300) { // 5 minutes before expiry
                            await authViewModel.refreshToken()
                        }
                    }
                }
            } catch {
                print("Error refreshing token: \(error)")
            }
        }
    }
}

// MARK: - JWT Helper

extension String {
    func decodeJWTPayload() -> [String: Any]? {
        let components = self.components(separatedBy: ".")
        guard components.count > 1 else { return nil }
        
        let payloadString = components[1]
        
        var paddedPayload = payloadString
        while paddedPayload.count % 4 != 0 {
            paddedPayload += "="
        }
        
        guard let data = Data(base64Encoded: paddedPayload) else { return nil }
        
        return try? JSONSerialization.jsonObject(with: data) as? [String: Any]
    }
}

// MARK: - Theme Helper

extension Color {
    static let primaryBackground = Color(.systemBackground)
    static let secondaryBackground = Color(.secondarySystemBackground)
    static let primaryText = Color(.label)
    static let secondaryText = Color(.secondaryLabel)
    static let primaryAccent = Color.blue
    static let secondaryAccent = Color.orange
    
    static func dynamicColor(light: Color, dark: Color) -> Color {
        return Color(UIColor { traitCollection in
            switch traitCollection.userInterfaceStyle {
            case .light:
                return UIColor(light)
            case .dark:
                return UIColor(dark)
            @unknown default:
                return UIColor(light)
            }
        })
    }
}