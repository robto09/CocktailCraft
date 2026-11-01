import SwiftUI
import shared

struct ContentView: View {
    @State private var router = AppRouter()
    // Built once via ViewModelHolder, not on every re-render
    @State private var cartModel = ViewModelHolder(CartViewModelSKIE.init)
    private var cartViewModel: CartViewModelSKIE { cartModel.wrapper }
    private let themeViewModel = ThemeViewModelSKIE.shared
    // System accessibility settings, read above the app's own overrides
    @Environment(\.dynamicTypeSize) private var systemTypeSize
    @Environment(\.accessibilityReduceMotion) private var systemReduceMotion

    var body: some View {
        let themeState = themeViewModel.state
        let appearance = AppAppearance(
            isDarkMode: themeState.isDarkMode,
            accentName: themeState.accentColor,
            isHighContrast: themeState.isHighContrast
        )

        ZStack {
            TabView(selection: $router.selectedTab) {
                // Home Tab (explicit path so widget deep links can push
                // a detail screen onto it)
                NavigationStack(path: $router.homePath) {
                    HomeViewSKIE()
                }
                .tabItem {
                    Label(L(\.home), systemImage: "house.fill")
                }
                .tag(AppRouter.Tab.home)
                .brandedTabBar()

                // Cart Tab
                NavigationStack {
                    CartView()
                }
                .tabItem {
                    Label(L(\.cart), systemImage: "cart.fill")
                }
                .badge(Int(cartViewModel.state.itemCount))
                .tag(AppRouter.Tab.cart)
                .brandedTabBar()

                // Favorites Tab
                NavigationStack {
                    FavoritesView()
                }
                .tabItem {
                    Label(L(\.favorites), systemImage: "heart.fill")
                }
                .tag(AppRouter.Tab.favorites)
                .brandedTabBar()

                // Orders Tab
                NavigationStack {
                    OrderListView()
                }
                .tabItem {
                    Label(L(\.orders), systemImage: "list.bullet")
                }
                .tag(AppRouter.Tab.orders)
                .brandedTabBar()

                // Profile Tab
                NavigationStack {
                    ProfileView()
                }
                .tabItem {
                    Label(L(\.profile), systemImage: "person.fill")
                }
                .tag(AppRouter.Tab.profile)
                .brandedTabBar()
            }
            // Selected tab in the brand color, matching the Android bottom nav
            .tint(AppColors.primary(appearance))

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
        // UI-test hook: XCUITest cannot deliver a custom-scheme URL
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
        // Theme, accent and contrast for every AppColors lookup below; a
        // change re-renders the views that read them, in place (navigation
        // stacks and screen ViewModels survive it)
        .environment(\.appearance, appearance)
        .preferredColorScheme(themeState.isSystemTheme ? nil : (themeState.isDarkMode ? .dark : .light))
        // In-app Font Size setting, applied relative to the system Dynamic
        // Type size so accessibility sizes keep working
        .dynamicTypeSize(Self.dynamicTypeSize(for: themeState.fontSize, system: systemTypeSize))
        // Reduce Motion (in-app setting or the system one): strip
        // animations app-wide
        .transaction { transaction in
            if themeViewModel.state.isReducedMotion || systemReduceMotion {
                transaction.animation = nil
            }
        }
    }

    /// Maps the shared font-size setting onto a Dynamic Type size, as steps
    /// from the user's system size — like Android, where the in-app setting
    /// multiplies the system font scale. "Medium" (the default) is the system
    /// size itself, so the system accessibility sizes (up to AX5) apply;
    /// with the default system size (.large) the other settings land on
    /// .small / .xLarge / .xxLarge as before.
    static func dynamicTypeSize(for fontSize: String, system: DynamicTypeSize) -> DynamicTypeSize {
        let steps: Int
        switch fontSize.lowercased() {
        case "small": steps = -2
        case "large": steps = 1
        case "xlarge": steps = 2
        default: steps = 0
        }
        let sizes = DynamicTypeSize.allCases
        guard let index = sizes.firstIndex(of: system) else { return system }
        return sizes[min(max(index + steps, 0), sizes.count - 1)]
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}