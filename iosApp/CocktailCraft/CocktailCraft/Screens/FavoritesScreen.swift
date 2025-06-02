import SwiftUI
import shared

struct FavoritesScreen: View {
    @StateObject private var favoritesViewModel = ViewModelProvider.shared.favoritesViewModel
    @StateObject private var cartViewModel = ViewModelProvider.shared.cartViewModel
    @EnvironmentObject var navigationCoordinator: NavigationCoordinator
    
    @State private var animatedIndices = Set<Int>()
    
    var body: some View {
        NavigationView {
            contentView
        }
    }
    
    @ViewBuilder
    private var contentView: some View {
        if favoritesViewModel.isLoading {
            loadingView
        } else if let error = favoritesViewModel.errorString {
            errorView(error: error)
        } else if favoritesViewModel.favorites.isEmpty {
            emptyView
        } else {
            favoritesListView
        }
    }
    
    private var loadingView: some View {
        ProgressView("Loading favorites...")
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private func errorView(error: String) -> some View {
        EmptyStateView(
            title: "Error",
            message: error,
            systemImage: "exclamationmark.triangle",
            actionButtonText: "Try Again",
            onActionButtonClick: {
                favoritesViewModel.refresh()
            }
        )
    }
    
    private var emptyView: some View {
        EmptyStateView(
            title: "No favorites yet",
            message: "Add cocktails to your favorites to see them here",
            systemImage: "heart",
            actionButtonText: "Browse Cocktails",
            onActionButtonClick: {
                navigationCoordinator.navigateToTab(.home)
            }
        )
    }
    
    private var favoritesListView: some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                headerView
                favoritesContent
            }
            .padding(.vertical)
        }
        .navigationTitle("Favorites")
        .navigationBarTitleDisplayMode(.large)
    }
    
    private var headerView: some View {
        HStack {
            Text("Your Favorite Cocktails")
                .font(.system(size: 20, weight: .bold))
            Spacer()
        }
        .padding(.horizontal)
        .padding(.top, 8)
    }
    
    private var favoritesContent: some View {
        ForEach(Array(favoritesViewModel.favorites.enumerated()), id: \.element.id) { index, cocktail in
            favoriteItemView(cocktail: cocktail, index: index)
        }
    }
    
    private func favoriteItemView(cocktail: Cocktail, index: Int) -> some View {
        AnimatedCocktailItem(
            cocktail: cocktail,
            index: index,
            isFavorite: true,
            onFavoriteToggle: {
                favoritesViewModel.toggleFavorite(cocktail: cocktail)
            },
            onAddToCart: {
                cartViewModel.addToCart(cocktail: cocktail, quantity: 1)
            },
            onTap: {
                navigationCoordinator.navigateToCocktailDetail(cocktailId: cocktail.id)
            }
        )
        .padding(.horizontal)
        .onAppear {
            withAnimation(.easeOut(duration: 0.3).delay(Double(index) * 0.05)) {
                _ = animatedIndices.insert(index)
            }
        }
        .onDisappear {
            animatedIndices.remove(index)
        }
    }
}