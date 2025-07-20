import SwiftUI
import shared

// MARK: - Navigation Destinations
enum NavigationDestination: Hashable {
    case cocktailDetail(String)
    case settings
    case profile
    case orderHistory
    case cart
    
    // Hashable conformance
    func hash(into hasher: inout Hasher) {
        switch self {
        case .cocktailDetail(let id):
            hasher.combine("cocktailDetail")
            hasher.combine(id)
        case .settings:
            hasher.combine("settings")
        case .profile:
            hasher.combine("profile")
        case .orderHistory:
            hasher.combine("orderHistory")
        case .cart:
            hasher.combine("cart")
        }
    }
    
    static func == (lhs: NavigationDestination, rhs: NavigationDestination) -> Bool {
        switch (lhs, rhs) {
        case (.cocktailDetail(let lhsId), .cocktailDetail(let rhsId)):
            return lhsId == rhsId
        case (.settings, .settings), (.profile, .profile), (.orderHistory, .orderHistory), (.cart, .cart):
            return true
        default:
            return false
        }
    }
}

// MARK: - Tab Selection
enum TabSelection: Int, CaseIterable {
    case home = 0
    case cart = 1
    case favorites = 2
    case orders = 3
    case profile = 4
    
    var title: String {
        switch self {
        case .home: return "Home"
        case .cart: return "Cart"
        case .favorites: return "Favorites"
        case .orders: return "Orders"
        case .profile: return "Profile"
        }
    }
    
    var systemImage: String {
        switch self {
        case .home: return "house.fill"
        case .cart: return "cart.fill"
        case .favorites: return "heart.fill"
        case .orders: return "list.bullet"
        case .profile: return "person.fill"
        }
    }
}

// MARK: - Navigation Coordinator
@Observable
class NavigationCoordinator {
    var selectedTab: TabSelection = .home
    var navigationPath = NavigationPath()
    
    // Deep linking support
    var pendingDeepLink: NavigationDestination?
    
    // Navigation methods
    func navigate(to destination: NavigationDestination) {
        navigationPath.append(destination)
    }
    
    func navigateBack() {
        if !navigationPath.isEmpty {
            navigationPath.removeLast()
        }
    }
    
    func popToRoot() {
        navigationPath = NavigationPath()
    }
    
    func switchTab(to tab: TabSelection) {
        selectedTab = tab
        // Clear navigation stack when switching tabs
        popToRoot()
    }
    
    // Deep linking
    func handleDeepLink(_ destination: NavigationDestination) {
        // Switch to appropriate tab based on destination
        switch destination {
        case .cocktailDetail:
            selectedTab = .home
        case .cart:
            selectedTab = .cart
        case .profile, .settings:
            selectedTab = .profile
        case .orderHistory:
            selectedTab = .orders
        }
        
        // Navigate to destination
        navigate(to: destination)
    }
    
    // URL handling for deep links
    func handleURL(_ url: URL) {
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
              let host = components.host else { return }
        
        switch host {
        case "cocktail":
            if let cocktailId = components.queryItems?.first(where: { $0.name == "id" })?.value {
                handleDeepLink(.cocktailDetail(cocktailId))
            }
        case "profile":
            handleDeepLink(.profile)
        case "settings":
            handleDeepLink(.settings)
        case "cart":
            handleDeepLink(.cart)
        case "orders":
            handleDeepLink(.orderHistory)
        default:
            break
        }
    }
}

// MARK: - Navigation View Builder
extension NavigationCoordinator {
    @ViewBuilder
    func destination(for destination: NavigationDestination) -> some View {
        switch destination {
        case .cocktailDetail(let cocktailId):
            CocktailDetailView(cocktailId: cocktailId)
        case .settings:
            SettingsView()
        case .profile:
            ProfileView()
        case .orderHistory:
            OrderListView()
        case .cart:
            CartView(selectedTab: Binding(
                get: { self.selectedTab },
                set: { self.selectedTab = $0 }
            ))
        }
    }
}
