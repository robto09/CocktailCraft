import SwiftUI
import shared

struct ContentView: View {
    @State private var router = AppRouter()
    @State private var cartViewModel = CartViewModelSKIE()
    private let themeViewModel = ThemeViewModelSKIE.shared

    var body: some View {
        let themeState = themeViewModel.state
        // Publish accent + high contrast into AppColors before children
        // render (mirrors Android's AppColors globals; see applyAppearance).
        let _ = Self.applyAppearance(themeState)

        ZStack {
            TabView(selection: $router.selectedTab) {
                // Home Tab (explicit path so widget deep links can push
                // a detail screen onto it)
                NavigationStack(path: $router.homePath) {
                    HomeViewSKIE()
                }
                .tabItem {
                    Label("Home", systemImage: "house.fill")
                }
                .tag(AppRouter.Tab.home)
                .brandedTabBar()

                // Cart Tab
                NavigationStack {
                    CartView()
                }
                .tabItem {
                    Label("Cart", systemImage: "cart.fill")
                }
                .badge(Int(cartViewModel.state.itemCount))
                .tag(AppRouter.Tab.cart)
                .brandedTabBar()

                // Favorites Tab
                NavigationStack {
                    FavoritesView()
                }
                .tabItem {
                    Label("Favorites", systemImage: "heart.fill")
                }
                .tag(AppRouter.Tab.favorites)
                .brandedTabBar()

                // Orders Tab
                NavigationStack {
                    OrderListView()
                }
                .tabItem {
                    Label("Orders", systemImage: "list.bullet")
                }
                .tag(AppRouter.Tab.orders)
                .brandedTabBar()

                // Profile Tab
                NavigationStack {
                    ProfileView()
                }
                .tabItem {
                    Label("Profile", systemImage: "person.fill")
                }
                .tag(AppRouter.Tab.profile)
                .brandedTabBar()
            }
            // Selected tab in the brand color, matching the Android bottom nav
            .tint(AppColors.primary(isDarkMode: themeState.isDarkMode))
            // Re-identify the tree when accent/contrast change so every view
            // re-reads the AppColors statics. These change only from the
            // Settings sheet, so the (rare) navigation-state reset is fine.
            .id("appearance-\(themeState.accentColor)-\(themeState.isHighContrast)")

            // Offline Banner
            VStack {
                OfflineBanner()
                Spacer()
            }
        }
        .background(AppColors.background(isDarkMode: themeViewModel.state.isDarkMode))
        .onOpenURL { url in
            // Widget taps: cocktailcraft://cocktail/{id}
            if let cocktailId = WidgetDeepLink.cocktailId(from: url) {
                router.openCocktailDetail(id: cocktailId)
            }
        }
        #if DEBUG
        // UI-test hook (IO-7): XCUITest cannot deliver a custom-scheme URL
        // to a cold-launching app reliably, so DeepLinkTests injects the
        // widget deep link's cocktail id through the launch environment and
        // this drives the same openCocktailDetail path onOpenURL uses.
        // Compiled out of Release builds.
        .onAppear {
            if let cocktailId = ProcessInfo.processInfo.environment["DEEPLINK_COCKTAIL_ID"],
               !cocktailId.isEmpty {
                router.openCocktailDetail(id: cocktailId)
            }
        }
        #endif
        .environment(cartViewModel)
        .environment(router)
        .environment(\.isDarkMode, themeState.isDarkMode)
        .preferredColorScheme(themeState.isSystemTheme ? nil : (themeState.isDarkMode ? .dark : .light))
        // In-app Font Size setting (Dynamic Type override; .large = default)
        .dynamicTypeSize(Self.dynamicTypeSize(for: themeState.fontSize))
        // Reduce Motion: strip implicit animations app-wide
        .transaction { transaction in
            if themeViewModel.state.isReducedMotion {
                transaction.animation = nil
            }
        }
    }

    /// Mirrors the shared theme state into AppColors' appearance statics.
    /// Runs at the top of body, before any child view resolves a color.
    @discardableResult
    private static func applyAppearance(_ state: ThemeUiState) -> Bool {
        AppColors.accentName = state.accentColor
        AppColors.isHighContrast = state.isHighContrast
        return true
    }

    /// Maps the shared font-size setting onto a Dynamic Type size.
    private static func dynamicTypeSize(for fontSize: String) -> DynamicTypeSize {
        switch fontSize.lowercased() {
        case "small": return .small
        case "large": return .xLarge
        case "xlarge": return .xxLarge
        default: return .large
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}