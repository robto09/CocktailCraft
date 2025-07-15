import SwiftUI

import shared

struct FavoritesView: View {
    @StateObject private var viewModel = FavoritesViewModel()
    
    var body: some View {
        NavigationView {
            if viewModel.favoriteCocktails.isEmpty {
                EmptyStateView(
                    icon: "heart",
                    title: "No favorites yet",
                    message: "Start adding cocktails to your favorites"
                )
                .navigationTitle("Favorites")
            } else {
                ScrollView {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                        ForEach(viewModel.favoriteCocktails, id: \.id) { cocktail in
                            NavigationLink(destination: CocktailDetailView(cocktailId: cocktail.id)) {
                                CocktailCard(cocktail: cocktail, isFavorite: true) {
                                    viewModel.removeFavorite(cocktailId: cocktail.id)
                                }
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .padding()
                }
                .navigationTitle("Favorites")
            }
        }
        .onAppear {
            viewModel.loadFavorites()
        }
    }
}