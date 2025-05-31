import SwiftUI
import Shared

struct MainView: View {
    @StateObject private var navigationCoordinator = NavigationCoordinator()
    @StateObject private var themeViewModel = ViewModelProvider.shared.themeViewModel
    @StateObject private var offlineModeViewModel = ViewModelProvider.shared.offlineModeViewModel
    
    var body: some View {
        ZStack {
            TabView(selection: $navigationCoordinator.selectedTab) {
                ForEach(TabItem.allCases, id: \.self) { tab in
                    NavigationStack(path: Binding(
                        get: { navigationCoordinator.currentPath(for: tab) },
                        set: { navigationCoordinator.navigationPaths[tab] = $0 }
                    )) {
                        tabRootView(for: tab)
                            .navigationDestination(for: NavigationPath.self) { path in
                                destinationView(for: path)
                            }
                    }
                    .tabItem {
                        Label(tab.title, systemImage: tab.iconName)
                    }
                    .tag(tab)
                }
            }
            .preferredColorScheme(themeViewModel.isDarkMode ? .dark : .light)
            
            // Offline Mode Banner
            if offlineModeViewModel.isOffline {
                VStack {
                    OfflineBanner {
                        navigationCoordinator.navigateToOfflineMode()
                    }
                    Spacer()
                }
                .transition(.move(edge: .top))
                .animation(.easeInOut, value: offlineModeViewModel.isOffline)
            }
        }
        .environmentObject(navigationCoordinator)
        .sheet(item: $navigationCoordinator.presentedSheet) { path in
            NavigationStack {
                destinationView(for: path)
                    .toolbar {
                        ToolbarItem(placement: .navigationBarTrailing) {
                            Button("Done") {
                                navigationCoordinator.dismissSheet()
                            }
                        }
                    }
                    .navigationBarTitleDisplayMode(.inline)
            }
        }
    }
    
    @ViewBuilder
    private func tabRootView(for tab: TabItem) -> some View {
        switch tab {
        case .home:
            HomeView()
        case .cart:
            CartView()
        case .favorites:
            FavoritesView()
        case .orders:
            OrderListView()
        case .profile:
            ProfileView()
        }
    }
    
    @ViewBuilder
    private func destinationView(for path: NavigationPath) -> some View {
        switch path {
        case .home:
            HomeView()
        case .cart:
            CartView()
        case .favorites:
            FavoritesView()
        case .orders:
            OrderListView()
        case .profile:
            ProfileView()
        case .cocktailDetail(let cocktailId):
            CocktailDetailView(cocktailId: cocktailId)
        case .reviews(let cocktailId):
            ReviewsView(cocktailId: cocktailId)
        case .offlineMode:
            OfflineModeView()
        }
    }
}

struct OfflineBanner: View {
    let onTap: () -> Void
    
    var body: some View {
        HStack {
            Image(systemName: "wifi.slash")
                .foregroundColor(.white)
            Text("You are offline")
                .font(.subheadline)
                .foregroundColor(.white)
            Spacer()
            Text("Settings")
                .font(.caption)
                .foregroundColor(.white)
                .underline()
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
        .background(Color.orange)
        .onTapGesture {
            onTap()
        }
    }
}

// Placeholder views - these will be implemented in subsequent tasks
struct HomeView: View {
    var body: some View {
        Text("Home Screen")
            .navigationTitle("Home")
    }
}

struct CartView: View {
    var body: some View {
        Text("Cart Screen")
            .navigationTitle("Cart")
    }
}

struct FavoritesView: View {
    var body: some View {
        Text("Favorites Screen")
            .navigationTitle("Favorites")
    }
}

struct OrderListView: View {
    var body: some View {
        Text("Recipes Screen")
            .navigationTitle("Recipes")
    }
}

struct ProfileView: View {
    var body: some View {
        Text("Profile Screen")
            .navigationTitle("Profile")
    }
}

struct CocktailDetailView: View {
    let cocktailId: String
    
    var body: some View {
        Text("Cocktail Detail: \(cocktailId)")
            .navigationTitle("Cocktail Details")
            .navigationBarTitleDisplayMode(.inline)
    }
}

struct ReviewsView: View {
    let cocktailId: String
    
    var body: some View {
        Text("Reviews for Cocktail: \(cocktailId)")
            .navigationTitle("Reviews")
            .navigationBarTitleDisplayMode(.inline)
    }
}

struct OfflineModeView: View {
    var body: some View {
        Text("Offline Mode Settings")
            .navigationTitle("Offline Mode")
            .navigationBarTitleDisplayMode(.inline)
    }
}

struct MainView_Previews: PreviewProvider {
    static var previews: some View {
        MainView()
    }
}