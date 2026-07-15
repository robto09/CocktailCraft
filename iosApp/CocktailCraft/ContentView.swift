import SwiftUI
import shared

struct ContentView: View {
    @State private var router = AppRouter()
    @State private var cartViewModel = CartViewModelSKIE()
    private let themeViewModel = ThemeViewModelSKIE.shared

    var body: some View {
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
            .tint(AppColors.primary(isDarkMode: themeViewModel.state.isDarkMode))

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
        .environment(\.isDarkMode, themeViewModel.state.isDarkMode)
        .preferredColorScheme(themeViewModel.state.isSystemTheme ? nil : (themeViewModel.state.isDarkMode ? .dark : .light))
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}