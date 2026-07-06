import SwiftUI
import shared

struct ContentView: View {
    @State private var router = AppRouter()
    @State private var cartViewModel = CartViewModelSKIE()
    private let themeViewModel = ThemeViewModelSKIE.shared

    var body: some View {
        ZStack {
            TabView(selection: $router.selectedTab) {
                // Home Tab
                NavigationStack {
                    HomeViewSKIE()
                }
                .tabItem {
                    Label("Home", systemImage: "house.fill")
                }
                .tag(AppRouter.Tab.home)

                // Cart Tab
                NavigationStack {
                    CartView()
                }
                .tabItem {
                    Label("Cart", systemImage: "cart.fill")
                }
                .badge(Int(cartViewModel.state.itemCount))
                .tag(AppRouter.Tab.cart)

                // Favorites Tab
                NavigationStack {
                    FavoritesView()
                }
                .tabItem {
                    Label("Favorites", systemImage: "heart.fill")
                }
                .tag(AppRouter.Tab.favorites)

                // Orders Tab
                NavigationStack {
                    OrderListView()
                }
                .tabItem {
                    Label("Orders", systemImage: "list.bullet")
                }
                .tag(AppRouter.Tab.orders)

                // Profile Tab
                NavigationStack {
                    ProfileView()
                }
                .tabItem {
                    Label("Profile", systemImage: "person.fill")
                }
                .tag(AppRouter.Tab.profile)
            }

            // Offline Banner
            VStack {
                OfflineBanner()
                Spacer()
            }
        }
        .background(AppColors.background(isDarkMode: themeViewModel.state.isDarkMode))
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