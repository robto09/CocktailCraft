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
    
    var body: some View {
        VStack {
            // Modern Search Bar
            HStack {
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundStyle(.secondary)
                    
                    TextField("Search cocktails...", text: $searchText)
                        .textFieldStyle(.plain)
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
                                .foregroundStyle(.secondary)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 10))
            }
            .padding(.horizontal)
            
            // Cocktail List
            if viewModel.isLoading && viewModel.cocktails.isEmpty {
                ScrollView {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                        ForEach(0..<6, id: \.self) { _ in
                            CocktailCardPlaceholder()
                        }
                    }
                    .padding()
                }
                .transition(.opacity)
            } else if viewModel.cocktails.isEmpty && !viewModel.isLoading {
                EmptyStateView(
                    icon: "wineglass",
                    title: "No Cocktails Found",
                    subtitle: "Try adjusting your search or filters",
                    actionTitle: "Clear Filters",
                    action: {
                        viewModel.clearSearch()
                        searchText = ""
                    }
                )
            } else {
                ScrollView {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                        ForEach(viewModel.filteredCocktails, id: \.id) { cocktail in
                            NavigationLink(destination: CocktailDetailView(cocktailId: cocktail.id)) {
                                CocktailCard(
                                    cocktail: cocktail,
                                    isFavorite: viewModel.isFavorite(cocktail.id),
                                    onToggleFavorite: {
                                        Task {
                                            await viewModel.toggleFavorite(cocktail)
                                        }
                                    }
                                )
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                        
                        // Load More Button
                        if viewModel.hasMoreData && !viewModel.isLoadingMore {
                            Button(action: {
                                Task {
                                    await viewModel.loadMoreCocktails()
                                }
                            }) {
                                Text("Load More")
                                    .foregroundColor(.blue)
                                    .padding()
                            }
                        } else if viewModel.isLoadingMore {
                            ProgressView()
                                .padding()
                        }
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("Cocktails")
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { showingFilters.toggle() }) {
                    Image(systemName: "line.3.horizontal.decrease.circle")
                }
            }
        }
        .sheet(isPresented: $showingFilters) {
            FilterViewSKIE(viewModel: viewModel)
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
            Task {
                await viewModel.searchCocktails(query: newValue)
            }
        }
        .task {
            // Initial load when view appears
            if viewModel.cocktails.isEmpty {
                await viewModel.loadCocktails()
            }
        }
        .refreshable {
            await viewModel.refreshCocktails()
        }
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

struct CocktailCard: View {
    let cocktail: Cocktail
    let isFavorite: Bool
    let onToggleFavorite: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Image with favorite button
            ZStack(alignment: .topTrailing) {
                if let imageUrl = cocktail.imageUrl {
                    AsyncImage(url: URL(string: imageUrl)) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Rectangle()
                            .fill(Color.gray.opacity(0.2))
                            .overlay(
                                ProgressView()
                            )
                    }
                    .frame(height: 150)
                    .clipped()
                }
                
                Button(action: onToggleFavorite) {
                    Image(systemName: isFavorite ? "heart.fill" : "heart")
                        .foregroundColor(isFavorite ? .red : .white)
                        .padding(8)
                        .background(Color.black.opacity(0.3))
                        .clipShape(Circle())
                }
                .padding(8)
            }
            
            // Content
            VStack(alignment: .leading, spacing: 4) {
                Text(cocktail.name)
                    .font(.headline)
                    .lineLimit(1)
                
                Text(cocktail.category ?? "Cocktail")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                HStack {
                    Text(String(format: "$%.2f", cocktail.price ?? 0))
                        .font(.subheadline)
                        .fontWeight(.semibold)
                    
                    Spacer()
                    
                    if let rating = cocktail.rating {
                        HStack(spacing: 2) {
                            Image(systemName: "star.fill")
                                .font(.caption)
                                .foregroundColor(.yellow)
                            Text(String(format: "%.1f", rating))
                                .font(.caption)
                        }
                    }
                }
            }
            .padding(12)
        }
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

struct CocktailCardPlaceholder: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Rectangle()
                .fill(Color.gray.opacity(0.2))
                .frame(height: 150)
            
            VStack(alignment: .leading, spacing: 8) {
                Rectangle()
                    .fill(Color.gray.opacity(0.2))
                    .frame(height: 20)
                
                Rectangle()
                    .fill(Color.gray.opacity(0.2))
                    .frame(width: 100, height: 16)
                
                HStack {
                    Rectangle()
                        .fill(Color.gray.opacity(0.2))
                        .frame(width: 60, height: 16)
                    
                    Spacer()
                    
                    Rectangle()
                        .fill(Color.gray.opacity(0.2))
                        .frame(width: 40, height: 16)
                }
            }
            .padding(12)
        }
        .cornerRadius(12)
        .redacted(reason: .placeholder)
    }
}

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