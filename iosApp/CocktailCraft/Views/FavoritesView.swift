import SwiftUI

import shared

struct FavoritesView: View {
    // Built once via ViewModelHolder, not on every parent re-render (IO-13)
    @State private var model = ViewModelHolder(FavoritesViewModelSKIE.init)
    private var viewModel: FavoritesViewModelSKIE { model.wrapper }
    @Environment(CartViewModelSKIE.self) private var cartViewModel
    @Environment(AppRouter.self) private var router
    /// Ids mid-unfavorite: their heart renders deselected for a beat before
    /// the card animates out, instead of the card vanishing on tap.
    @State private var removingIds: Set<String> = []
    @State private var showingToast = false
    @State private var toastMessage = ""
    @State private var toastType: Toast.ToastType = .success
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        Group {
            if viewModel.state.favorites.isEmpty {
                emptyStateView
            } else {
                favoritesList
            }
        }
        // App background (not the system black/white) behind every state
        .background(AppColors.background(isDarkMode: isDarkMode).ignoresSafeArea())
        .brandedNavigationBar(title: L(\.favorites))
        .navigationDestination(for: String.self) { cocktailId in
            CocktailDetailView(cocktailId: cocktailId)
        }
        .task {
            await viewModel.loadFavorites()
        }
        .toast(isShowing: $showingToast, message: toastMessage, type: toastType, duration: 2)
        .sharedErrorAlert(viewModel.error, clear: { viewModel.clearError() })
    }
    
    /// "Browse Cocktails" goes to Home, as on Android.
    private var emptyStateView: some View {
        EmptyStateView(
            icon: "heart",
            title: L(\.no_favorites),
            message: L(\.favorites_empty_message_ios),
            actionTitle: L(\.browse_cocktails),
            action: { router.selectedTab = .home }
        )
    }
    
    /// One full-width card per row, the same card as Home and Android's
    /// Favorites list (16 between cards, 16 around the list).
    private var favoritesList: some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                ForEach(viewModel.state.favorites, id: \.id) { cocktail in
                    favoriteRow(cocktail)
                }
            }
            // The favorites list changes via the shared flow (outside any
            // SwiftUI animation transaction), so animate insert/remove off
            // the id list — the counterpart of Android's .animateItem().
            .animation(.default, value: viewModel.state.favorites.map(\.id))
            .padding(16)
        }
        .refreshable {
            await viewModel.refreshFavorites()
        }
    }

    private func favoriteRow(_ cocktail: Cocktail) -> some View {
        // Value-based link: the destination (and its factory-scoped detail
        // ViewModel) is only built on push, not eagerly per row.
        NavigationLink(value: cocktail.id) {
            CocktailCard(
                cocktail: cocktail,
                isFavorite: !removingIds.contains(cocktail.id),
                onFavoriteToggle: {
                    unfavorite(cocktail)
                },
                // Add-to-cart with toast confirmation, matching Home and the
                // Android Favorites list (which had this button all along)
                onAddToCart: {
                    // Only a real add is confirmed; a failed one says so instead
                    Task { @MainActor in
                        let added = await cartViewModel.addToCart(cocktail)
                        toastMessage = added
                            ? L(\.added_to_cart, cocktail.name)
                            : L(\.error_add_item_to_cart_failed)
                        toastType = added ? .success : .error
                        withAnimation {
                            showingToast = true
                        }
                    }
                }
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