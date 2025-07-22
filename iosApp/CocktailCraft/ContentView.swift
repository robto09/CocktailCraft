import SwiftUI
import shared

struct ContentView: View {
    @State private var selectedTab = 0

    var body: some View {
        ZStack {
            TabView(selection: $selectedTab) {
                // Home Tab
                NavigationStack {
                    HomeView()
                }
                .tabItem {
                    Label("Home", systemImage: "house.fill")
                }
                .tag(0)

                // Cart Tab
                NavigationStack {
                    CartView(selectedTab: $selectedTab)
                }
                .tabItem {
                    Label("Cart", systemImage: "cart.fill")
                }
                .tag(1)

                // Favorites Tab
                NavigationStack {
                    FavoritesView()
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
                    ProfileView()
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
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}