import SwiftUI
import Observation

/// App-level tab router. Views request tab switches through this instead of
/// threading `@Binding selectedTab: Int` down the view hierarchy.
@MainActor
@Observable
final class AppRouter {
    enum Tab: Int, Hashable {
        case home, cart, favorites, orders, profile
    }

    var selectedTab: Tab = .home

    /// Home tab's navigation path — explicit so deep links (widget taps)
    /// can push programmatically. String values resolve through the
    /// navigationDestination(for: String.self) registered in HomeViewSKIE.
    var homePath = NavigationPath()

    /// Entry point for cocktailcraft://cocktail/{id} deep links.
    /// Resets the Home stack first: tapping a widget while deep in the stack
    /// must land on the detail screen, not push on top of it — and repeated
    /// taps must not stack duplicate details (IO-11).
    func openCocktailDetail(id: String) {
        selectedTab = .home
        homePath = NavigationPath()
        homePath.append(id)
    }
}
