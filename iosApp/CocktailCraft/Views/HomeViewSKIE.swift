import SwiftUI
import shared

/**
 * Example HomeView using the SKIE-enabled HomeViewModelSKIE.
 * This demonstrates how to use shared ViewModels with pure SKIE integration.
 */
struct HomeViewSKIE: View {
    @State private var viewModel = HomeViewModelSKIE()
    @State private var searchText = ""
    @State private var showingFilters = false
    @State private var showingAdvancedSearch = false
    @State private var selectedCategory: String? = nil
    @State private var activeFilters: [String] = []
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
            // Search Bar with Advanced Search Toggle
            VStack(spacing: 12) {
                HStack(spacing: 8) {
                    // Main Search Bar
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(textSecondary)
                        
                        TextField("Search cocktails...", text: $searchText)
                            .textFieldStyle(.plain)
                            .foregroundColor(.primary)
                            .onSubmit {
                                Task {
                                    await viewModel.searchCocktails(query: searchText)
                                }
                            }
                        
                        if !searchText.isEmpty {
                            Button(action: {
                                searchText = ""
                                viewModel.clearSearch()
                            }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(textSecondary)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(surfaceColor)
                    .cornerRadius(10)
                    .shadow(color: Color.black.opacity(0.05), radius: 1, x: 0, y: 1)
                    
                    // Advanced Search Button
                    Button(action: { showingAdvancedSearch.toggle() }) {
                        Image(systemName: "slider.horizontal.3")
                            .font(.system(size: 18))
                            .foregroundColor(primaryColor)
                            .padding(8)
                            .background(surfaceColor)
                            .cornerRadius(8)
                            .shadow(color: Color.black.opacity(0.15), radius: 2, x: 0, y: 1)
                    }
                    .buttonStyle(.plain)
                }
                
                // Active Filters Row (simplified)
                if !activeFilters.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(activeFilters, id: \.self) { filter in
                                HStack(spacing: 4) {
                                    Text(filter)
                                        .font(.caption)
                                    
                                    Button(action: { removeActiveFilter(filter) }) {
                                        Image(systemName: "xmark")
                                            .font(.system(size: 10))
                                    }
                                    .buttonStyle(.plain)
                                }
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(primaryColor.opacity(0.1))
                                .foregroundColor(primaryColor)
                                .cornerRadius(8)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }
                
                // Category Filter Row (only show when not searching)
                if searchText.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            // "All" chip
                            Button("All") {
                                selectedCategory = nil
                                removeActiveFilter("Category")
                                Task {
                                    await viewModel.loadCocktails()
                                }
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 6)
                            .background(selectedCategory == nil ? primaryColor : surfaceColor)
                            .foregroundColor(selectedCategory == nil ? .white : primaryColor)
                            .cornerRadius(16)
                            .shadow(color: selectedCategory == nil ? Color.black.opacity(0.15) : Color.clear, radius: 2, x: 0, y: 1)
                            
                            // Category chips
                            ForEach(availableCategories, id: \.self) { category in
                                Button(category) {
                                    selectedCategory = category
                                    removeActiveFilter("Category")  // Remove previous category filter
                                    addActiveFilter("Category: \(category)")
                                    Task {
                                        await viewModel.loadCocktailsByCategory(category)
                                    }
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 6)
                                .background(selectedCategory == category ? primaryColor : surfaceColor)
                                .foregroundColor(selectedCategory == category ? .white : primaryColor)
                                .cornerRadius(16)
                                .shadow(color: selectedCategory == category ? Color.black.opacity(0.15) : Color.clear, radius: 2, x: 0, y: 1)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(backgroundColor)
            
            // Content Area with Pull-to-Refresh
            if viewModel.state.isLoading && viewModel.state.cocktails.isEmpty {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(0..<6, id: \.self) { _ in
                            // Loading placeholder matching new card size
                            HStack(spacing: 16) {
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color.gray.opacity(0.2))
                                    .frame(width: 120, height: 120)
                                
                                VStack(alignment: .leading, spacing: 6) {
                                    // Title placeholder
                                    RoundedRectangle(cornerRadius: 4)
                                        .fill(Color.gray.opacity(0.2))
                                        .frame(height: 22)
                                    
                                    // Subtitle placeholder
                                    RoundedRectangle(cornerRadius: 4)
                                        .fill(Color.gray.opacity(0.2))
                                        .frame(width: 140, height: 18)
                                    
                                    // Ingredients placeholder
                                    RoundedRectangle(cornerRadius: 4)
                                        .fill(Color.gray.opacity(0.2))
                                        .frame(width: 120, height: 16)
                                    
                                    Spacer(minLength: 8)
                                    
                                    // Bottom row placeholder
                                    HStack {
                                        RoundedRectangle(cornerRadius: 4)
                                            .fill(Color.gray.opacity(0.2))
                                            .frame(width: 70, height: 24)
                                        
                                        Spacer()
                                        
                                        HStack(spacing: 12) {
                                            Circle()
                                                .fill(Color.gray.opacity(0.2))
                                                .frame(width: 40, height: 40)
                                            
                                            Circle()
                                                .fill(Color.gray.opacity(0.2))
                                                .frame(width: 40, height: 40)
                                        }
                                    }
                                }
                                .padding(.vertical, 8)
                            }
                            .padding(16)
                            .background(surfaceColor)
                            .cornerRadius(16)
                            .shadow(color: Color.black.opacity(0.12), radius: 6, x: 0, y: 3)
                            .redacted(reason: .placeholder)
                        }
                    }
                    .padding(16)
                }
                .background(backgroundColor)
                .transition(.opacity)
            } else if viewModel.state.cocktails.isEmpty && !viewModel.state.isLoading {
                ScrollView {
                    VStack {
                        Spacer()
                        HomeEmptyStateView(
                            icon: "wineglass",
                            title: "No Cocktails Found",
                            subtitle: "Try adjusting your search or filters",
                            actionTitle: "Clear Filters",
                            action: {
                                clearAllFilters()
                            }
                        )
                        Spacer()
                    }
                }
                .background(backgroundColor)
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.state.cocktails, id: \.id) { cocktail in
                            // Value-based link: the destination (and its
                            // factory-scoped detail ViewModel) is only built
                            // on push, not eagerly for every visible row.
                            NavigationLink(value: cocktail.id) {
                                CocktailCard(
                                    cocktail: cocktail,
                                    isFavorite: viewModel.isFavorite(cocktail.id),
                                    onFavoriteToggle: {
                                        Task {
                                            await viewModel.toggleFavorite(cocktail)
                                        }
                                    },
                                    onAddToCart: {
                                        Task {
                                            await viewModel.addToCart(cocktail)
                                            await MainActor.run {
                                                toastMessage = "Added \(cocktail.name) to cart"
                                                withAnimation {
                                                    showingToast = true
                                                }
                                            }
                                        }
                                    },
                                    onCardTap: nil // Explicitly set to nil to prevent interference with NavigationLink
                                )
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                        
                        // Load More Section
                        if viewModel.state.hasMoreData && !viewModel.state.isLoadingMore {
                            Button(action: {
                                Task {
                                    await viewModel.loadMoreCocktails()
                                }
                            }) {
                                Text("Load More")
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
                                Text("Loading more...")
                                    .font(.callout)
                                    .foregroundColor(textSecondary)
                            }
                            .padding(.bottom, 32)
                        }
                    }
                    .padding(16)
                }
                .background(backgroundColor)
                .refreshable {
                    await viewModel.refreshCocktails()
                }
            }
        }

        .navigationBarTitleDisplayMode(.large)
        .navigationDestination(for: String.self) { cocktailId in
            CocktailDetailView(cocktailId: cocktailId)
        }
        .sheet(isPresented: $showingFilters) {
            FilterViewSKIE(viewModel: viewModel)
                .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showingAdvancedSearch) {
            // Simple advanced search placeholder
            NavigationStack {
                Text("Advanced Search Coming Soon")
                    .navigationTitle("Advanced Search")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .navigationBarTrailing) {
                            Button("Done") {
                                showingAdvancedSearch = false
                            }
                        }
                    }
            }
        }
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") {
                viewModel.clearError()
            }
            if let recoveryAction = viewModel.error?.recoveryAction {
                Button(recoveryAction.actionLabel) {
                    viewModel.retry()
                }
            }
        } message: {
            Text(viewModel.error?.message ?? "An error occurred")
        }
        .onChange(of: searchText) { _, newValue in
            if !newValue.isEmpty {
                Task {
                    await viewModel.searchCocktails(query: newValue)
                }
            }
        }
        .task {
            // Initial load when view appears
            if viewModel.state.cocktails.isEmpty {
                await viewModel.loadCocktails()
            }
        }
        .toast(isShowing: $showingToast, message: toastMessage, type: .success, duration: 2)

    }
    
    // MARK: - Helper Functions
    
    private func addActiveFilter(_ filter: String) {
        if !activeFilters.contains(filter) {
            activeFilters.append(filter)
        }
    }
    
    private func removeActiveFilter(_ filter: String) {
        activeFilters.removeAll { $0.contains(filter) }
    }
    
    private func clearAllFilters() {
        searchText = ""
        selectedCategory = nil
        activeFilters.removeAll()
        viewModel.clearSearch()
        Task {
            await viewModel.loadCocktails()
        }
    }
    
    private func applyAdvancedFilters() {
        // Placeholder for advanced filters functionality
        print("Advanced filters applied")
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