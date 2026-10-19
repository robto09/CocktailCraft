import SwiftUI
import shared

/// Home screen driven by the shared HomeViewModel (via HomeViewModelSKIE).
/// Search bar, filter rows, the loading skeleton, and the advanced-search
/// sheet live in Components/; this view owns the state wiring and the
/// loading/empty/content branching.
struct HomeViewSKIE: View {
    // Built once via ViewModelHolder, not on every parent re-render (IO-13)
    @State private var model = ViewModelHolder(HomeViewModelSKIE.init)
    private var viewModel: HomeViewModelSKIE { model.wrapper }
    @State private var searchText = ""
    @State private var showingAdvancedSearch = false
    @State private var selectedCategory: String? = nil
    // The latest add-to-cart outcome, until its toast picks it up
    @State private var addToCartOutcome: UiEvent?
    @Environment(\.isDarkMode) private var isDarkMode

    // Theme-derived colors (the old hardcoded hex values bypassed AppTheme,
    // so Home ignored dark mode)
    private var backgroundColor: Color { AppColors.background(isDarkMode: isDarkMode) }
    private var surfaceColor: Color { AppColors.surface(isDarkMode: isDarkMode) }

    var body: some View {
        VStack(spacing: 0) {
            searchHeader

            // Content Area with Pull-to-Refresh
            if viewModel.state.isLoading && viewModel.state.cocktails.isEmpty {
                CocktailListSkeleton()
                    .background(backgroundColor)
                    .transition(.opacity)
            } else if viewModel.state.cocktails.isEmpty && !viewModel.state.isNetworkAvailable
                        && !isSearchInProgress && selectedCategory == nil {
                // Offline with nothing cached (e.g. first launch in airplane
                // mode): full-screen no-connection state with Retry, parity
                // with Android's NetworkErrorStateDisplay. Offline searches,
                // filters, and category picks with no matches keep the normal
                // empty state; with cached content the list renders under the
                // global OfflineBanner.
                noConnectionState
            } else if viewModel.state.cocktails.isEmpty && !viewModel.state.isLoading {
                emptyState
            } else {
                cocktailList
            }
        }
        .brandedNavigationBar(title: L(\.app_title))
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                HomeSortMenu(selection: viewModel.state.sortOption) { option in
                    viewModel.setSortOption(option)
                }
            }
        }
        .navigationDestination(for: String.self) { cocktailId in
            CocktailDetailView(cocktailId: cocktailId)
        }
        .sheet(isPresented: $showingAdvancedSearch) {
            AdvancedSearchSheet(viewModel: viewModel)
                .presentationDetents([.medium, .large])
        }
        .sharedErrorAlert(viewModel.error, clear: { viewModel.clearError() })
        .onChange(of: searchText) { _, newValue in
            // Every change — including deleting back to empty — goes through the
            // shared debounced pipeline: no per-keystroke Tasks, no stale
            // last-completed-wins results, and an empty query restores the
            // category listing (IO-1).
            viewModel.updateSearchQuery(newValue)
        }
        .onAppear {
            // Also on the way back from a detail page, which may have added
            // to Recently viewed. (No initial load here: the shared
            // ViewModel starts it itself.)
            viewModel.refreshRecentlyViewed()
        }
        // Add-to-cart outcomes: returned to this screen, which asked
        .sharedEventToast(addToCartOutcome, clear: { addToCartOutcome = nil })
    }

    // MARK: - Sections

    private var searchHeader: some View {
        VStack(spacing: 12) {
            HomeSearchBar(
                searchText: $searchText,
                onSubmit: {
                    viewModel.searchCocktails(query: searchText)
                },
                onClear: {
                    searchText = ""
                    viewModel.clearSearch()
                },
                onAdvancedSearch: { showingAdvancedSearch.toggle() },
                isFilterActive: showingAdvancedSearch || viewModel.state.searchFilters.hasActiveFilters()
            )

            // Active Filters card (driven by the shared searchFilters state);
            // tapping a chip re-applies the reduced filter set.
            if viewModel.state.searchFilters.hasActiveFilters() {
                ActiveFiltersCard(
                    filters: viewModel.state.searchFilters,
                    onApply: { reduced in
                        viewModel.applyFilters(reduced)
                    },
                    onClearAll: {
                        // Keep the chip: the shared clear reloads the
                        // selected category, as on Android
                        searchText = ""
                        viewModel.clearSearchFilters()
                    }
                )
            }

            // Category Filter Row (only show when not searching)
            if searchText.isEmpty {
                CategoryChipRow(
                    categories: availableCategories,
                    selectedCategory: selectedCategory,
                    // No Task per tap: the shared ViewModel tracks one
                    // listing load, so a slow earlier category can never
                    // replace the one tapped last
                    onSelectAll: {
                        selectedCategory = nil
                        viewModel.selectCategory(nil)
                    },
                    onSelect: { category in
                        selectedCategory = category
                        viewModel.selectCategory(category)
                    }
                )
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(backgroundColor)
    }

    private var noConnectionState: some View {
        EmptyStateView(
            icon: "wifi.slash",
            title: L(\.home_no_connection_title),
            message: L(\.home_no_connection_message),
            actionTitle: L(\.common_retry),
            action: { viewModel.retry() }
        )
        .background(backgroundColor)
    }

    private var emptyState: some View {
        // GeometryReader gives the scroll content a real minimum height —
        // without it the Spacers collapse inside the ScrollView and the
        // content pins to the top instead of centering like Android.
        GeometryReader { geometry in
            ScrollView {
                VStack {
                    Spacer()
                    SearchEmptyStateView(
                        searchQuery: searchText,
                        selectedCategory: selectedCategory,
                        hasActiveFilters: viewModel.state.searchFilters.hasActiveFilters(),
                        onClearSearch: {
                            searchText = ""
                            viewModel.clearSearch()
                        },
                        onClearCategory: {
                            selectedCategory = nil
                            viewModel.selectCategory(nil)
                        },
                        onClearFilters: {
                            // Drop only the advanced filters; any active query
                            // re-runs on its own (empty query + no filters =
                            // default listing).
                            let filters = viewModel.state.searchFilters
                            viewModel.applyFilters(SearchFilters(
                                query: filters.query,
                                category: nil,
                                ingredient: nil,
                                alcoholic: nil
                            ))
                        },
                        onRefresh: {
                            viewModel.retry()
                        }
                    )
                    Spacer()
                }
                .frame(maxWidth: .infinity, minHeight: geometry.size.height)
            }
        }
        .background(backgroundColor)
    }

    private var cocktailList: some View {
        HomeCocktailList(
            list: viewModel.list,
            isSearchInProgress: isSearchInProgress,
            onToggleFavorite: { cocktail in
                viewModel.toggleFavorite(cocktail)
            },
            onAddToCart: { cocktail in
                // Main-actor Task: the VM call and the toast state both on
                // main. Only a real add is confirmed; a failed one says so
                // instead.
                Task { @MainActor in
                    addToCartOutcome = await viewModel.addToCart(cocktail)
                }
            },
            onLoadMore: {
                viewModel.loadMoreCocktails()
            },
            onRefresh: {
                await viewModel.refreshCocktails()
            }
        )
        // Skips the list's body when Home re-renders for anything else
        // (the search text, loading flags): it re-renders on its own when
        // `list.content` changes
        .equatable()
    }

    /// Pagination belongs to the category listing only. Search results are a
    /// complete set (shared clears `hasMoreData` on search; this guard is
    /// defense-in-depth mirroring Android's `!isSearchActive` scroll gate).
    private var isSearchInProgress: Bool {
        !searchText.isEmpty || viewModel.state.searchFilters.hasActiveFilters()
    }

    // MARK: - Helper Functions

    // Canonical curated categories come from shared — single source of truth
    private var availableCategories: [String] {
        viewModel.curatedCategories
    }
}

// MARK: - Cocktail list

/// Home's cocktail list, apart from the rest of the screen. HomeViewSKIE
/// mirrors the whole HomeUiState, so every keystroke in the search field
/// used to rebuild every card (and the favorite id set). This view reads
/// only `list.content`, which changes when the cocktails, favorites,
/// ratings or paging do, and compares equal across Home's other re-renders,
/// so SwiftUI skips its body then. The closures are left out of `==`: they
/// only forward to the (same) ViewModel.
private struct HomeCocktailList: View, Equatable {
    let list: HomeListModel
    let isSearchInProgress: Bool
    let onToggleFavorite: (Cocktail) -> Void
    let onAddToCart: (Cocktail) -> Void
    let onLoadMore: () -> Void
    let onRefresh: () async -> Void

    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.appearance) private var appearance

    static func == (lhs: HomeCocktailList, rhs: HomeCocktailList) -> Bool {
        lhs.list === rhs.list && lhs.isSearchInProgress == rhs.isSearchInProgress
    }

    var body: some View {
        let content = list.content
        // Reaching any of the last 5 rows loads the next page, like
        // Android's OnBottomReached(buffer = 5); no Load More button
        let nearEndIds = Set(content.cocktails.suffix(5).map(\.id))
        ScrollView {
            LazyVStack(spacing: 12) {
                // Hidden while a search query is active (shared rule)
                if !content.recentlyViewed.isEmpty {
                    RecentlyViewedRow(
                        cocktails: content.recentlyViewed,
                        // The list's own horizontal padding
                        horizontalBleed: 16
                    )
                    .padding(.bottom, 4)
                }

                ForEach(content.cocktails, id: \.id) { cocktail in
                    // Value-based link: the destination (and its
                    // factory-scoped detail ViewModel) is only built
                    // on push, not eagerly for every visible row.
                    NavigationLink(value: cocktail.id) {
                        // The hearts come from the observed favorites (not
                        // the synchronous isFavorite() helper, outside
                        // SwiftUI's Observation graph), so they re-render
                        // the moment they change — including the
                        // optimistic publish on toggle.
                        CocktailCard(
                            cocktail: cocktail,
                            isFavorite: content.favoriteIds.contains(cocktail.id),
                            reviewRating: content.ratingSummaries[cocktail.id],
                            onFavoriteToggle: { onToggleFavorite(cocktail) },
                            onAddToCart: { onAddToCart(cocktail) },
                            onCardTap: nil // Explicitly set to nil to prevent interference with NavigationLink
                        )
                    }
                    .buttonStyle(PlainButtonStyle())
                    .onAppear {
                        if nearEndIds.contains(cocktail.id) && !isSearchInProgress && content.hasMoreData {
                            onLoadMore()
                        }
                    }
                }

                footer(content)
            }
            .padding(16)
        }
        .background(AppColors.background(isDarkMode: isDarkMode))
        .refreshable {
            await onRefresh()
        }
    }

    /// Android's end-of-list line once the category is exhausted. (The next
    /// page is already stored, so there is no loading spinner.)
    @ViewBuilder
    private func footer(_ content: HomeListContent) -> some View {
        if !isSearchInProgress && !content.hasMoreData && !content.cocktails.isEmpty {
            Text(L(\.list_end_of_list_message))
                .font(.subheadline)
                .foregroundColor(AppColors.textSecondary(appearance))
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                .padding(16)
        }
    }
}

// MARK: - Preview

struct HomeViewSKIE_Previews: PreviewProvider {
    static var previews: some View {
        NavigationStack {
            HomeViewSKIE()
        }
    }
}
