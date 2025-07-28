import SwiftUI
import shared

/**
 * Example HomeView using the SKIE-enabled HomeViewModelSKIE.
 * This demonstrates how to use shared ViewModels with pure SKIE integration.
 */
struct HomeViewSKIE: View {
    @StateObject private var viewModel = HomeViewModelSKIE()
    @State private var searchText = ""
    @State private var showingFilters = false
    @State private var showingAdvancedSearch = false
    @State private var selectedCategory: String? = nil
    @State private var activeFilters: [String] = []
    @State private var showingToast = false
    @State private var toastMessage = ""

    
    // Android-style colors
    private let primaryColor = Color(red: 0.92, green: 0.42, blue: 0.26) // #EB6A43
    private let secondaryColor = Color(red: 1.0, green: 0.78, blue: 0.30) // #FFC84D
    private let backgroundColor = Color(red: 0.98, green: 0.98, blue: 0.98) // #FAFAFA
    private let surfaceColor = Color(UIColor.systemBackground)
    private let textSecondary = Color.secondary
    
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
                            ForEach(viewModel.getCategories(), id: \.self) { category in
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
                        }
                        .padding(.horizontal, 16)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(backgroundColor)
            
            // Content Area with Pull-to-Refresh
            if viewModel.isLoading && viewModel.cocktails.isEmpty {
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
            } else if viewModel.cocktails.isEmpty && !viewModel.isLoading {
                ScrollView {
                    VStack {
                        Spacer()
                        EmptyStateView(
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
                        ForEach(viewModel.filteredCocktails, id: \.id) { cocktail in
                            NavigationLink(destination: CocktailDetailView(cocktailId: cocktail.id, cartViewModel: CartViewModelSKIE())) {
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
                        if viewModel.hasMoreData && !viewModel.isLoadingMore {
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
                        } else if viewModel.isLoadingMore {
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
        .navigationTitle("My Bar")
        .navigationBarTitleDisplayMode(.large)
        .toolbarBackground(primaryColor, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .sheet(isPresented: $showingFilters) {
            FilterViewSKIE(viewModel: viewModel)
                .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showingAdvancedSearch) {
            // Simple advanced search placeholder
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
        .onChange(of: searchText) { newValue in
            if !newValue.isEmpty {
                Task {
                    await viewModel.searchCocktails(query: newValue)
                }
            }
        }
        .task {
            // Initial load when view appears
            if viewModel.cocktails.isEmpty {
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
}

// MARK: - Filter View using SKIE

struct FilterViewSKIE: View {
    let viewModel: HomeViewModelSKIE
    @Environment(\.dismiss) private var dismiss
    @State private var selectedCategory: String? = nil
    @State private var selectedSort = "nameAsc"
    
    var body: some View {
        NavigationView {
            Form {
                Section("Category") {
                    ForEach(viewModel.getCategories(), id: \.self) { category in
                        Button(action: {
                            selectedCategory = category
                        }) {
                            HStack {
                                Text(category)
                                    .foregroundColor(.primary)
                                Spacer()
                                if selectedCategory == category {
                                    Image(systemName: "checkmark")
                                        .foregroundColor(.blue)
                                }
                            }
                        }
                    }
                }
                
                Section("Sort By") {
                    Picker("Sort", selection: $selectedSort) {
                        Text("Name (A-Z)").tag("nameAsc")
                        Text("Name (Z-A)").tag("nameDesc")
                        Text("Price (Low to High)").tag("priceAsc")
                        Text("Price (High to Low)").tag("priceDesc")
                        Text("Rating").tag("rating")
                        Text("Popularity").tag("popularity")
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("Filters")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Clear") {
                        selectedCategory = nil
                        selectedSort = "nameAsc"
                        viewModel.clearSearch()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Apply") {
                        Task {
                            // Apply category filter
                            if let category = selectedCategory {
                                await viewModel.loadCocktailsByCategory(category)
                            }
                            
                            // Apply sort
                            switch selectedSort {
                            case "nameDesc":
                                // Implement name descending sort
                                break
                            case "priceAsc":
                                await viewModel.sortByPrice(ascending: true)
                            case "priceDesc":
                                await viewModel.sortByPrice(ascending: false)
                            case "rating":
                                await viewModel.sortByRating()
                            case "popularity":
                                await viewModel.sortByPopularity()
                            default:
                                break
                            }
                            
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Supporting Views

struct EmptyStateView: View {
    let icon: String
    let title: String
    let subtitle: String
    let actionTitle: String?
    let action: (() -> Void)?
    
    init(icon: String, title: String, subtitle: String, actionTitle: String? = nil, action: (() -> Void)? = nil) {
        self.icon = icon
        self.title = title
        self.subtitle = subtitle
        self.actionTitle = actionTitle
        self.action = action
    }
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 60))
                .foregroundColor(.secondary)
            
            Text(title)
                .font(.title2)
                .fontWeight(.semibold)
            
            Text(subtitle)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
            
            if let actionTitle = actionTitle, let action = action {
                Button(action: action) {
                    Text(actionTitle)
                        .fontWeight(.medium)
                }
                .buttonStyle(.borderedProminent)
            }
        }
        .padding()
    }
}

// MARK: - Preview

struct HomeViewSKIE_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            HomeViewSKIE()
        }
    }
}