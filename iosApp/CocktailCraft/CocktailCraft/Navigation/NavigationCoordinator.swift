import SwiftUI
import Combine

class NavigationCoordinator: ObservableObject {
    @Published var selectedTab: TabItem = .home
    @Published var navigationPaths: [TabItem: [NavigationPath]] = [:]
    @Published var presentedSheet: NavigationPath?
    
    private var cancellables = Set<AnyCancellable>()
    
    init() {
        // Initialize navigation paths for each tab
        TabItem.allCases.forEach { tab in
            navigationPaths[tab] = []
        }
    }
    
    // MARK: - Navigation Methods
    
    func navigateToTab(_ tab: TabItem) {
        selectedTab = tab
    }
    
    func navigateToCocktailDetail(cocktailId: String, from tab: TabItem? = nil) {
        let currentTab = tab ?? selectedTab
        navigationPaths[currentTab]?.append(.cocktailDetail(cocktailId: cocktailId))
    }
    
    func navigateToReviews(cocktailId: String, from tab: TabItem? = nil) {
        let currentTab = tab ?? selectedTab
        navigationPaths[currentTab]?.append(.reviews(cocktailId: cocktailId))
    }
    
    func navigateToOfflineMode() {
        presentedSheet = .offlineMode
    }
    
    func navigateBack(from tab: TabItem? = nil) {
        let currentTab = tab ?? selectedTab
        if navigationPaths[currentTab]?.isEmpty == false {
            _ = navigationPaths[currentTab]?.removeLast()
        }
    }
    
    func popToRoot(for tab: TabItem? = nil) {
        let currentTab = tab ?? selectedTab
        navigationPaths[currentTab] = []
    }
    
    func dismissSheet() {
        presentedSheet = nil
    }
    
    // MARK: - Path Management
    
    func currentPath(for tab: TabItem) -> [NavigationPath] {
        return navigationPaths[tab] ?? []
    }
    
    func isTabSelected(_ tab: TabItem) -> Bool {
        return selectedTab == tab
    }
    
    // MARK: - Deep Linking Support
    
    func navigateToPath(_ path: NavigationPath) {
        switch path {
        case .home:
            navigateToTab(.home)
            popToRoot(for: .home)
        case .cart:
            navigateToTab(.cart)
            popToRoot(for: .cart)
        case .favorites:
            navigateToTab(.favorites)
            popToRoot(for: .favorites)
        case .orders:
            navigateToTab(.orders)
            popToRoot(for: .orders)
        case .profile:
            navigateToTab(.profile)
            popToRoot(for: .profile)
        case .cocktailDetail(let cocktailId):
            navigateToTab(.home)
            navigateToCocktailDetail(cocktailId: cocktailId)
        case .reviews(let cocktailId):
            // Navigate to cocktail detail first, then reviews
            navigateToTab(.home)
            navigateToCocktailDetail(cocktailId: cocktailId)
            navigateToReviews(cocktailId: cocktailId)
        case .offlineMode:
            navigateToOfflineMode()
        }
    }
}