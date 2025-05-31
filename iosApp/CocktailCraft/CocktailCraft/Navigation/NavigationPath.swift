import Foundation

enum NavigationPath: Hashable, Identifiable {
    case home
    case cart
    case favorites
    case orders
    case profile
    case cocktailDetail(cocktailId: String)
    case reviews(cocktailId: String)
    case offlineMode
    
    var id: String {
        switch self {
        case .home:
            return "home"
        case .cart:
            return "cart"
        case .favorites:
            return "favorites"
        case .orders:
            return "orders"
        case .profile:
            return "profile"
        case .cocktailDetail(let id):
            return "cocktailDetail-\(id)"
        case .reviews(let id):
            return "reviews-\(id)"
        case .offlineMode:
            return "offlineMode"
        }
    }
    
    var title: String {
        switch self {
        case .home:
            return "Home"
        case .cart:
            return "Cart"
        case .favorites:
            return "Favorites"
        case .orders:
            return "Recipes"
        case .profile:
            return "Profile"
        case .cocktailDetail:
            return "Cocktail Details"
        case .reviews:
            return "Reviews"
        case .offlineMode:
            return "Offline Mode"
        }
    }
    
    var showsBottomBar: Bool {
        switch self {
        case .home, .cart, .favorites, .orders, .profile:
            return true
        case .cocktailDetail, .reviews, .offlineMode:
            return false
        }
    }
    
    var hasBackButton: Bool {
        switch self {
        case .cocktailDetail, .offlineMode:
            return true
        default:
            return false
        }
    }
}

enum TabItem: CaseIterable {
    case home
    case cart
    case favorites
    case orders
    case profile
    
    var path: NavigationPath {
        switch self {
        case .home:
            return .home
        case .cart:
            return .cart
        case .favorites:
            return .favorites
        case .orders:
            return .orders
        case .profile:
            return .profile
        }
    }
    
    var iconName: String {
        switch self {
        case .home:
            return "house.fill"
        case .cart:
            return "cart.fill"
        case .favorites:
            return "heart.fill"
        case .orders:
            return "book.fill"
        case .profile:
            return "person.fill"
        }
    }
    
    var title: String {
        return path.title
    }
}