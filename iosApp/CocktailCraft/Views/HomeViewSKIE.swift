import SwiftUI
import shared

/// Home screen driven by the shared HomeViewModel (via HomeViewModelSKIE).
/// Search bar, filter rows, the loading skeleton, and the advanced-search
/// sheet live in Components/; this view owns the state wiring and the
/// loading/empty/content branching.
struct HomeViewSKIE: View {
    @State private var viewModel = HomeViewModelSKIE()
    @State private var searchText = ""
    @State private var showingAdvancedSearch = false
    @State private var selectedCategory: String? = nil
    @State private var showingToast = false
    @State private var toastMessage = ""
    @Environment(\.isDarkMode) private var isDarkMode

    // Theme-derived colors (the old hardcoded hex values bypassed AppTheme,
    // so Home ignored dark mode)
    private var primaryColor: Color { AppColors.primary(isDarkMode: isDarkMode) }
    private var backgroundColor: Color { AppColors.background(isDarkMode: isDarkMode) }
    private var surfaceColor: Color { AppColors.surface(isDarkMode: isDarkMode) }
    private var textSecondary: Color { AppColors.textSecondary(isDarkMode: isDarkMode) }

    var body: some View {
        VStack(spacing: 0) {
            searchHeader

            // Content Area with Pull-to-Refresh
            if viewModel.state.isLoading && viewModel.state.cocktails.isEmpty {
                CocktailListSkeleton()
                    .background(backgroundColor)
                    .transition(.opacity)
            } else if viewModel.state.cocktails.isEmpty && !viewModel.state.isLoading {
                emptyState
            } else {
                cocktailList
            }
        }
        .navigationTitle("CocktailCraft")
        .navigationBarTitleDisplayMode(.large)
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
        .task {
            // Initial load when view appears
            if viewModel.state.cocktails.isEmpty {
                await viewModel.loadCocktails()
            }
        }
        .toast(isShowing: $showingToast, message: toastMessage, type: .success, duration: 2)
    }

    // MARK: - Sections

    private var searchHeader: some View {
        VStack(spacing: 12) {
            HomeSearchBar(
                searchText: $searchText,
                onSubmit: {
                    Task {
                        await viewModel.searchCocktails(query: searchText)
                    }
                },
                onClear: {
                    searchText = ""
                    viewModel.clearSearch()
                },
                onAdvancedSearch: { showingAdvancedSearch.toggle() }
            )

            // Active Filters Row (driven by the shared searchFilters state)
            if viewModel.state.searchFilters.hasActiveFilters() {
                ActiveFiltersRow(
                    labels: activeFilterLabels,
                    onClearAll: {
                        searchText = ""
                        selectedCategory = nil
                        viewModel.clearSearchFilters()
                    }
                )
            }

            // Category Filter Row (only show when not searching)
            if searchText.isEmpty {
                CategoryChipRow(
                    categories: availableCategories,
                    selectedCategory: selectedCategory,
                    onSelectAll: {
                        selectedCategory = nil
                        Task {
                            await viewModel.loadCocktails()
                        }
                    },
                    onSelect: { category in
                        selectedCategory = category
                        Task {
                            await viewModel.loadCocktailsByCategory(category)
                        }
                    }
                )
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(backgroundColor)
    }

    private var emptyState: some View {
        ScrollView {
            VStack {
                Spacer()
                HomeEmptyStateView(
                    icon: "wineglass",
                    title: String(localized: "No Cocktails Found"),
                    subtitle: String(localized: "Try adjusting your search or filters"),
                    actionTitle: String(localized: "Clear Filters"),
                    action: {
                        searchText = ""
                        selectedCategory = nil
                        viewModel.clearSearchFilters()
                    }
                )
                Spacer()
            }
        }
        .background(backgroundColor)
    }

    private var cocktailList: some View {
        // Derived from the observed state (not the synchronous isFavorite()
        // helper, which sits outside SwiftUI's Observation graph): the hearts
        // re-render the moment state.favorites changes — including the
        // optimistic publish on toggle.
        let favoriteIds = Set(viewModel.state.favorites.map(\.id))
        return ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(viewModel.state.cocktails, id: \.id) { cocktail in
                    // Value-based link: the destination (and its
                    // factory-scoped detail ViewModel) is only built
                    // on push, not eagerly for every visible row.
                    NavigationLink(value: cocktail.id) {
                        CocktailCard(
                            cocktail: cocktail,
                            isFavorite: favoriteIds.contains(cocktail.id),
                            onFavoriteToggle: {
                                Task {
                                    await viewModel.toggleFavorite(cocktail)
                                }
                            },
                            onAddToCart: {
                                // Main-actor Task: the whole flow (VM call +
                                // toast state) runs on main, without the old
                                // mid-task MainActor.run hop.
                                Task { @MainActor in
                                    await viewModel.addToCart(cocktail)
                                    toastMessage = String(localized: "Added \(cocktail.name) to cart")
                                    withAnimation {
                                        showingToast = true
                                    }
                                }
                            },
                            onCardTap: nil // Explicitly set to nil to prevent interference with NavigationLink
                        )
                    }
                    .buttonStyle(PlainButtonStyle())
                }

                loadMoreSection
            }
            .padding(16)
        }
        .background(backgroundColor)
        .refreshable {
            await viewModel.refreshCocktails()
        }
    }

    /// Pagination belongs to the category listing only. Search results are a
    /// complete set (shared clears `hasMoreData` on search; this guard is
    /// defense-in-depth mirroring Android's `!isSearchActive` scroll gate).
    private var isSearchInProgress: Bool {
        !searchText.isEmpty || viewModel.state.searchFilters.hasActiveFilters()
    }

    @ViewBuilder
    private var loadMoreSection: some View {
        if isSearchInProgress {
            EmptyView()
        } else if viewModel.state.hasMoreData && !viewModel.state.isLoadingMore {
            Button(action: {
                Task {
                    await viewModel.loadMoreCocktails()
                }
            }) {
                Text(String(localized: "Load More"))
                    .font(.callout)
                    .fontWeight(.medium)
                    .foregroundColor(primaryColor)
                    .padding(.vertical, 12)
                    .padding(.horizontal, 20)
                    .background(surfaceColor)
                    .cornerRadius(8)
                    .shadow(color: Color.black.opacity(0.15), radius: 2, x: 0, y: 1)
            }
            .buttonStyle(.plain)
            .padding(.bottom, 32)
        } else if viewModel.state.isLoadingMore {
            HStack {
                ProgressView()
                    .foregroundColor(primaryColor)
                Text(String(localized: "Loading more..."))
                    .font(.callout)
                    .foregroundColor(textSecondary)
            }
            .padding(.bottom, 32)
        }
    }

    // MARK: - Helper Functions

    /// Human-readable labels for the active advanced-search filters, derived
    /// from the shared `searchFilters` state (single source of truth).
    private var activeFilterLabels: [String] {
        let filters = viewModel.state.searchFilters
        var labels: [String] = []
        if let category = filters.category {
            labels.append("Category: \(category)")
        }
        if let ingredient = filters.ingredient {
            labels.append("Ingredient: \(ingredient)")
        }
        if let alcoholic = filters.alcoholic?.boolValue {
            labels.append(alcoholic ? "Alcoholic" : "Non-Alcoholic")
        }
        return labels
    }

    // Canonical curated categories come from shared — single source of truth
    private var availableCategories: [String] {
        viewModel.curatedCategories
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
