import SwiftUI
import shared

struct FavoritesScreen: View {
    @ObservedObject private var favoritesViewModel = ViewModelProvider.shared.favoritesViewModel
    @ObservedObject private var cartViewModel = ViewModelProvider.shared.cartViewModel
    @EnvironmentObject var navigationCoordinator: NavigationCoordinator
    
    @State private var animatedIndices = Set<Int>()
    
    var body: some View {
        NavigationView {
            ZStack {
                if favoritesViewModel.isLoading {
                    ProgressView("Loading favorites...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = favoritesViewModel.error, !error.isEmpty {
                    EmptyStateView(
                        title: "Error",
                        message: error,
                        systemImage: "exclamationmark.triangle",
                        actionButtonText: "Try Again",
                        onActionButtonClick: {
                            favoritesViewModel.refresh()
                        }
                    )
                } else if favoritesViewModel.favorites.isEmpty {
                    EmptyStateView(
                        title: "No favorites yet",
                        message: "Add cocktails to your favorites to see them here",
                        systemImage: "heart",
                        actionButtonText: "Browse Cocktails",
                        onActionButtonClick: {
                            navigationCoordinator.selectedTab = .home
                        }
                    )
                } else {
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            // Section Header
                            HStack {
                                Text("Your Favorite Cocktails")
                                    .font(.system(size: 20, weight: .bold))
                                Spacer()
                            }
                            .padding(.horizontal)
                            .padding(.top, 8)
                            
                            // Favorites List
                            ForEach(Array(favoritesViewModel.favorites.enumerated()), id: \.element.id) { index, cocktail in
                                AnimatedCocktailItem(
                                    cocktail: cocktail,
                                    isFavorite: true,
                                    isAnimated: animatedIndices.contains(index),
                                    onTap: {
                                        navigationCoordinator.navigate(to: .cocktailDetail(cocktailId: cocktail.id))
                                    },
                                    onAddToCart: {
                                        cartViewModel.addToCart(cocktail: cocktail)
                                        // Show some feedback that item was added
                                    },
                                    onToggleFavorite: {
                                        favoritesViewModel.toggleFavorite(cocktail: cocktail)
                                    }
                                )
                                .padding(.horizontal)
                                .onAppear {
                                    withAnimation(.easeOut(duration: 0.3).delay(Double(index) * 0.05)) {
                                        animatedIndices.insert(index)
                                    }
                                }
                                .onDisappear {
                                    animatedIndices.remove(index)
                                }
                            }
                        }
                        .padding(.vertical)
                    }
                }
            }
            .navigationTitle("Favorites")
            .navigationBarTitleDisplayMode(.large)
        }
    }
}