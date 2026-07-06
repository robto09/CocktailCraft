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
}
