import SwiftUI
import shared

struct ContentView: View {
    @State private var selectedTab = 0
    @StateObject private var cartViewModel = CartViewModelSKIE()
    @StateObject private var themeViewModel = ThemeViewModelSKIE.shared

    var body: some View {
        ZStack {
            TabView(selection: $selectedTab) {
                // Home Tab
                NavigationStack {
                    HomeViewSKIE()
                }
                .tabItem {
                    Label("Home", systemImage: "house.fill")
                }
                .tag(0)

                // Cart Tab
                NavigationStack {
                    CartView(cartViewModel: cartViewModel, selectedTab: $selectedTab)
                }
                .tabItem {
                    Label("Cart", systemImage: "cart.fill")
                }
                .badge(cartViewModel.itemCount)
                .tag(1)

                // Favorites Tab
                NavigationStack {
                    FavoritesView(cartViewModel: cartViewModel)
                }
                .tabItem {
                    Label("Favorites", systemImage: "heart.fill")
                }
                .tag(2)

                // Orders Tab
                NavigationStack {
                    OrderListView()
                }
                .tabItem {
                    Label("Orders", systemImage: "list.bullet")
                }
                .tag(3)

                // Profile Tab
                NavigationStack {
                    ProfileView(selectedTab: $selectedTab)
                }
                .tabItem {
                    Label("Profile", systemImage: "person.fill")
                }
                .tag(4)
            }

            // Offline Banner
            VStack {
                OfflineBanner()
                Spacer()
            }
        }
        .background(AppColors.background(isDarkMode: themeViewModel.isDarkMode))
        .environment(\.isDarkMode, themeViewModel.isDarkMode)
        .preferredColorScheme(themeViewModel.isSystemTheme ? nil : (themeViewModel.isDarkMode ? .dark : .light))
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}