import SwiftUI
import shared
import Combine

struct HomeView: View {
    @StateObject private var homeViewModel = ViewModelProvider.shared.homeViewModel
    @StateObject private var cartViewModel = ViewModelProvider.shared.cartViewModel
    @StateObject private var offlineModeViewModel = ViewModelProvider.shared.offlineModeViewModel
    @EnvironmentObject var navigationCoordinator: NavigationCoordinator
    
    @State private var selectedCategory: String? = nil
    @State private var showAdvancedSearch = false
    @State private var searchText = ""
    @State private var searchDebouncer = PassthroughSubject<String, Never>()
    @State private var cancellables = Set<AnyCancellable>()
    
    var body: some View {
        VStack(spacing: 0) {
            // Search Bar
            SearchBar(
                text: $searchText,
                hasActiveFilters: homeViewModel.searchFilters?.hasActiveFilters() ?? false,
                onFilterTap: {
                    showAdvancedSearch.toggle()
                }
            )
            .padding(.horizontal)
            .padding(.top, 8)
            
            // Active Filters
            if let searchFilters = homeViewModel.searchFilters, searchFilters.hasActiveFilters() {
                SearchFilterChips(
                    filters: searchFilters,
                    onRemove: { filter in
                        // TODO: Implement filter removal
                    },
                    onClearAll: {
                        // TODO: Clear all filters
                    }
                )
                .padding(.horizontal)
                .padding(.top, 8)
            }
            
            // Category Filter Row
            CategoryFilterRow(
                selectedCategory: $selectedCategory,
                onCategorySelected: { category in
                    selectedCategory = category
                    if let category = category {
                        homeViewModel.loadCocktailsByCategory(category: category)
                    } else {
                        homeViewModel.loadCocktails()
                    }
                }
            )
            .padding(.top, 8)
            
            // Main Content
            if homeViewModel.isLoading && homeViewModel.cocktails.isEmpty {
                // Loading State
                ScrollView {
                    LazyVStack(spacing: 16) {
                        ForEach(0..<5, id: \.self) { _ in
                            CocktailLoadingShimmer()
                                .padding(.horizontal)
                        }
                    }
                    .padding(.top, 16)
                }
            } else if !homeViewModel.errorString.isEmpty && homeViewModel.cocktails.isEmpty {
                // Error State
                NetworkErrorStateDisplay(
                    error: homeViewModel.errorString,
                    isOffline: offlineModeViewModel.isOffline,
                    onRetry: {
                        homeViewModel.loadCocktails()
                    },
                    onGoOffline: {
                        offlineModeViewModel.setOfflineMode(enabled: true)
                    }
                )
            } else if homeViewModel.cocktails.isEmpty {
                // Empty State
                EmptySearchResultsMessage(
                    searchQuery: searchText,
                    hasFilters: homeViewModel.searchFilters?.hasActiveFilters() ?? false,
                    onClearSearch: {
                        searchText = ""
                        homeViewModel.searchCocktails(query: "")
                    },
                    onClearFilters: {
                        // TODO: Clear filters
                    }
                )
            } else {
                // Cocktail List
                ScrollView {
                    LazyVStack(spacing: 16) {
                        ForEach(Array(homeViewModel.cocktails.enumerated()), id: \.element.id) { index, cocktail in
                            AnimatedCocktailItem(
                                cocktail: cocktail,
                                index: index,
                                isFavorite: homeViewModel.favorites.contains(where: { $0.id == cocktail.id }),
                                onFavoriteToggle: {
                                    // TODO: Implement toggle favorite
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
                                // Load more when reaching near the end
                                if index == homeViewModel.cocktails.count - 5 {
                                    homeViewModel.loadMore()
                                }
                            }
                        }
                        
                        // Loading More Indicator
                        LoadingMoreIndicator(isLoading: homeViewModel.isLoadingMore)
                        
                        // End of List Message
                        if !homeViewModel.hasMoreData && !homeViewModel.cocktails.isEmpty {
                            EndOfListMessage()
                                .padding()
                        }
                    }
                    .padding(.top, 16)
                }
                .refreshable {
                    await refreshCocktails()
                }
            }
        }
        .navigationTitle("CocktailCraft")
        .navigationBarTitleDisplayMode(.large)
        .sheet(isPresented: $showAdvancedSearch) {
            if let searchFilters = homeViewModel.searchFilters {
                AdvancedSearchView(
                    filters: searchFilters,
                    onApply: { filters in
                        // TODO: Apply filters
                        showAdvancedSearch = false
                    },
                    onCancel: {
                        showAdvancedSearch = false
                    }
                )
            }
        }
        .onAppear {
            setupSearchDebouncer()
            if homeViewModel.cocktails.isEmpty {
                homeViewModel.loadCocktails()
            }
        }
        .onChange(of: searchText) { newValue in
            searchDebouncer.send(newValue)
        }
    }
    
    private func setupSearchDebouncer() {
        searchDebouncer
            .debounce(for: .milliseconds(300), scheduler: RunLoop.main)
            .sink { query in
                homeViewModel.searchCocktails(query: query)
            }
            .store(in: &cancellables)
    }
    
    private func refreshCocktails() async {
        await withCheckedContinuation { continuation in
            homeViewModel.loadCocktails()
            // Wait a bit for the refresh to complete
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                continuation.resume()
            }
        }
    }
}