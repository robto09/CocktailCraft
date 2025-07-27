import SwiftUI
import shared

struct HomeView: View {
    @StateObject private var viewModel = HomeViewModelSKIE()
    @State private var searchText = ""
    @State private var showingFilters = false
    @State private var showingAdvancedSearch = false
    @State private var selectedCategory: String? = nil
    @State private var activeFilters: [String] = []
    
    // Android-style colors
    private let primaryColor = Color(red: 0.92, green: 0.42, blue: 0.26) // #EB6A43
    private let secondaryColor = Color(red: 1.0, green: 0.78, blue: 0.30) // #FFC84D
    private let backgroundColor = Color(red: 0.98, green: 0.98, blue: 0.98) // #FAFAFA
    private let surfaceColor = Color(UIColor.systemBackground)
    private let textSecondary = Color.secondary
    
    var body: some View {
        VStack(spacing: 0) {
            headerSection
            contentSection
        }
        .navigationTitle("My Bar")
        .navigationBarTitleDisplayMode(.large)
        .toolbarBackground(primaryColor, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .sheet(isPresented: $showingFilters) {
            FilterView(viewModel: viewModel)
                .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showingAdvancedSearch) {
            advancedSearchSheet
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
        .onChange(of: searchText) { oldValue, newValue in
            if !newValue.isEmpty {
                Task {
                    await viewModel.searchCocktails(query: newValue)
                }
            }
        }
        .task {
            if viewModel.cocktails.isEmpty {
                await viewModel.loadCocktails()
            }
        }
    }
    
    // MARK: - Header Section
    
    private var headerSection: some View {
        VStack(spacing: 12) {
            searchBarSection
            activeFiltersSection
            categoryFiltersSection
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(backgroundColor)
    }
    
    private var searchBarSection: some View {
        HStack(spacing: 8) {
            searchBar
            advancedSearchButton
        }
    }
    
    private var searchBar: some View {
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
    }
    
    private var advancedSearchButton: some View {
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
    
    @ViewBuilder
    private var activeFiltersSection: some View {
        if !activeFilters.isEmpty {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(activeFilters, id: \.self) { filter in
                        filterChip(for: filter)
                    }
                }
                .padding(.horizontal, 16)
            }
        }
    }
    
    private func filterChip(for filter: String) -> some View {
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
    
    @ViewBuilder
    private var categoryFiltersSection: some View {
        if searchText.isEmpty {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    allCategoryChip
                    categoryChips
                }
                .padding(.horizontal, 16)
            }
        }
    }
    
    private var allCategoryChip: some View {
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
    }
    
    @ViewBuilder
    private var categoryChips: some View {
        ForEach(viewModel.getCategories(), id: \.self) { category in
            categoryChip(for: category)
        }
    }
    
    private func categoryChip(for category: String) -> some View {
        Button(category) {
            selectedCategory = category
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
    
    // MARK: - Content Section
    
    private var contentSection: some View {
        Group {
            if viewModel.isLoading && viewModel.cocktails.isEmpty {
                loadingSection
            } else if viewModel.cocktails.isEmpty && !viewModel.isLoading {
                emptyStateSection
            } else {
                cocktailListSection
            }
        }
    }
    
    private var loadingSection: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(0..<6, id: \.self) { _ in
                    loadingPlaceholder
                }
            }
            .padding(16)
        }
        .background(backgroundColor)
        .transition(.opacity)
    }
    
    private var loadingPlaceholder: some View {
        HStack(spacing: 16) {
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.gray.opacity(0.2))
                .frame(width: 120, height: 120)
            
            VStack(alignment: .leading, spacing: 6) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.gray.opacity(0.2))
                    .frame(height: 22)
                
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.gray.opacity(0.2))
                    .frame(width: 140, height: 18)
                
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.gray.opacity(0.2))
                    .frame(width: 120, height: 16)
                
                Spacer(minLength: 8)
                
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
    
    private var emptyStateSection: some View {
        ScrollView {
            VStack {
                Spacer()
                VStack(spacing: 16) {
                    Image(systemName: "wineglass")
                        .font(.system(size: 60))
                        .foregroundColor(.secondary)
                    
                    Text("No Cocktails Found")
                        .font(.title2)
                        .fontWeight(.semibold)
                    
                    Text("Try adjusting your search or filters")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                    
                    Button("Clear Filters") {
                        clearAllFilters()
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(primaryColor)
                }
                Spacer()
            }
        }
        .background(backgroundColor)
    }
    
    private var cocktailListSection: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                cocktailList
                loadMoreSection
            }
            .padding(16)
        }
        .background(backgroundColor)
        .refreshable {
            await viewModel.refreshCocktails()
        }
    }
    
    @ViewBuilder
    private var cocktailList: some View {
        ForEach(viewModel.filteredCocktails, id: \.id) { cocktail in
            cocktailRow(for: cocktail)
        }
    }
    
    private func cocktailRow(for cocktail: Cocktail) -> some View {
        NavigationLink(destination: {
            print("HomeView - Navigating to cocktail with ID: '\(cocktail.id)', name: '\(cocktail.name)'")
            return CocktailDetailView(cocktailId: cocktail.id)
        }()) {
            CocktailCard(
                cocktail: cocktail,
                isFavorite: viewModel.isFavorite(cocktail.id),
                onFavoriteToggle: {
                    Task {
                        await viewModel.toggleFavorite(cocktail)
                    }
                },
                onAddToCart: {
                    print("Add to cart: \(cocktail.name)")
                }
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    @ViewBuilder
    private var loadMoreSection: some View {
        if viewModel.hasMoreData && !viewModel.isLoadingMore {
            loadMoreButton
        } else if viewModel.isLoadingMore {
            loadingMoreIndicator
        }
    }
    
    private var loadMoreButton: some View {
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
    }
    
    private var loadingMoreIndicator: some View {
        HStack {
            ProgressView()
                .foregroundColor(primaryColor)
            Text("Loading more...")
                .font(.callout)
                .foregroundColor(textSecondary)
        }
        .padding(.bottom, 32)
    }
    
    private var advancedSearchSheet: some View {
        NavigationView {
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
}

struct HomeView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            HomeView()
        }
    }
}