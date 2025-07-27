import SwiftUI

import shared

struct FavoritesView: View {
    @StateObject private var viewModel = FavoritesViewModelSKIE()

    var body: some View {
        Group {
            if viewModel.favorites.isEmpty {
                emptyStateView
            } else {
                favoritesGridView
            }
        }
        .navigationTitle("Favorites")
        .onAppear {
            Task {
                await viewModel.loadFavorites()
            }
        }
    }
    
    private var emptyStateView: some View {
        EmptyStateView(
            icon: "heart",
            title: "No favorites yet",
            message: "Start adding cocktails to your favorites"
        )
    }
    
    private var favoritesGridView: some View {
        ScrollView {
            gridContent
                .padding()
        }
        .refreshable {
            await viewModel.refreshFavorites()
        }
    }
    
    private var gridContent: some View {
        LazyVGrid(columns: gridColumns, spacing: 16) {
            ForEach(viewModel.favorites, id: \.id) { cocktail in
                cocktailGridItem(cocktail)
            }
        }
    }
    
    private var gridColumns: [GridItem] {
        [GridItem(.flexible()), GridItem(.flexible())]
    }
    
    private func cocktailGridItem(_ cocktail: Cocktail) -> some View {
        NavigationLink(destination: CocktailDetailView(cocktailId: cocktail.id)) {
            CocktailCard(
                cocktail: cocktail,
                isFavorite: true,
                onFavoriteToggle: {
                    Task {
                        await viewModel.toggleFavorite(cocktail)
                    }
                },
                layout: .vertical
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}