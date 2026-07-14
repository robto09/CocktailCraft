import SwiftUI

import shared

struct FavoritesView: View {
    @State private var viewModel = FavoritesViewModelSKIE()
    /// Ids mid-unfavorite: their heart renders deselected for a beat before
    /// the card animates out, instead of the card vanishing on tap.
    @State private var removingIds: Set<String> = []

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
        .task {
            await viewModel.loadFavorites()
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
        // The favorites list changes via the shared flow (outside any SwiftUI
        // animation transaction), so animate insert/remove off the id list —
        // the counterpart of Android's .animateItem().
        .animation(.default, value: viewModel.state.favorites.map(\.id))
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
                isFavorite: !removingIds.contains(cocktail.id),
                onFavoriteToggle: {
                    unfavorite(cocktail)
                },
                layout: .vertical
            )
        }
        .buttonStyle(PlainButtonStyle())
    }

    /// Staged removal: deselect the heart immediately, give it a beat to
    /// register, then toggle — the card animates out when the shared flow
    /// re-emits. If the toggle fails (state rolls back), clearing the id
    /// restores the filled heart.
    private func unfavorite(_ cocktail: Cocktail) {
        guard !removingIds.contains(cocktail.id) else { return }
        removingIds.insert(cocktail.id)
        Task {
            try? await Task.sleep(for: .milliseconds(350))
            await viewModel.toggleFavorite(cocktail)
            removingIds.remove(cocktail.id)
        }
    }
}