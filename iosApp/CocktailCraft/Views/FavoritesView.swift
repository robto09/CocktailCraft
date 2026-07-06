import SwiftUI

import shared

struct FavoritesView: View {
    @State private var viewModel = FavoritesViewModelSKIE()

    var body: some View {
        Group {
            if viewModel.state.favorites.isEmpty {
                emptyStateView
            } else {
                favoritesGridView
            }
        }
        .navigationTitle("Favorites")
        .navigationDestination(for: String.self) { cocktailId in
            CocktailDetailView(cocktailId: cocktailId)
        }
        .onAppear {
            Task {
                await viewModel.loadFavorites()
            }
        }
        .sharedErrorAlert(viewModel.error, clear: { viewModel.clearError() })
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
            ForEach(viewModel.state.favorites, id: \.id) { cocktail in
                cocktailGridItem(cocktail)
            }
        }
    }
    
    private var gridColumns: [GridItem] {
        [GridItem(.flexible()), GridItem(.flexible())]
    }
    
    private func cocktailGridItem(_ cocktail: Cocktail) -> some View {
        // Value-based link: the destination (and its factory-scoped detail
        // ViewModel) is only built on push, not eagerly per grid item.
        NavigationLink(value: cocktail.id) {
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