import SwiftUI

import shared

struct FavoritesView: View {
    @StateObject private var viewModel = FavoritesViewModelSKIE()

    var body: some View {
        Group {
            if viewModel.favorites.isEmpty {
                EmptyStateView(
                    icon: "heart",
                    title: "No favorites yet",
                    message: "Start adding cocktails to your favorites"
                )
            } else {
                ScrollView {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                        ForEach(viewModel.favorites, id: \.id) { cocktail in
                            NavigationLink(destination: CocktailDetailView(cocktailId: cocktail.id)) {
                                CocktailCard(cocktail: cocktail, isFavorite: true) {
                                    Task {
                                        await viewModel.toggleFavorite(cocktail)
                                    }
                                }
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .padding()
                }
                .refreshable {
                    await viewModel.refreshFavorites()
                }
            }
        }
        .navigationTitle("Favorites")
        .onAppear {
            Task {
                await viewModel.loadFavorites()
            }
        }
    }
}