import SwiftUI
import shared

struct HomeTab: View {
    @ObservedObject var viewModel: MainViewModel
    @State private var searchText = ""
    @State private var showingAdvancedSearch = false
    @State private var selectedCategory: String? = nil
    @State private var showingError = false
    
    private let categories = ["All", "Martini", "Margarita", "Mojito", "Old Fashioned", "Cocktail", "Shot"]
    
    var body: some View {
        NavigationView {
            ZStack {
                Color(.systemBackground)
                    .ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // Search Bar
                    searchBar
                    
                    // Category Filters
                    categoryFilters
                    
                    if viewModel.isLoading {
                        loadingView
                    } else {
                        ScrollView {
                            LazyVStack(spacing: 16) {
                                if searchText.isEmpty && selectedCategory == nil {
                                    featuredSection
                                    popularSection
                                }
                                
                                cocktailList
                            }
                            .padding()
                        }
                        .refreshable {
                            viewModel.refresh()
                        }
                    }
                }
            }
            .navigationTitle("CocktailCraft")
            .sheet(isPresented: $showingAdvancedSearch) {
                AdvancedSearchView(
                    filters: viewModel.searchFilters,
                    onApply: { filters in
                        viewModel.applyFilters(filters)
                        showingAdvancedSearch = false
                    }
                )
            }
            .alert("Error", isPresented: $showingError, presenting: viewModel.error) { _ in
                Button("OK") {}
                Button("Retry") {
                    viewModel.retry()
                }
            } message: { error in
                Text(error)
            }
        }
    }
    
    private var searchBar: some View {
        HStack {
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.gray)
                
                TextField("Search cocktails...", text: $searchText)
                    .onChange(of: searchText) { query in
                        viewModel.search(query)
                    }
                
                if !searchText.isEmpty {
                    Button(action: {
                        searchText = ""
                        viewModel.toggleSearchMode(active: false)
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.gray)
                    }
                }
            }
            .padding(8)
            .background(Color(.systemGray6))
            .cornerRadius(10)
            
            Button(action: {
                showingAdvancedSearch = true
            }) {
                Image(systemName: "slider.horizontal.3")
                    .foregroundColor(.primary)
            }
        }
        .padding()
    }
    
    private var categoryFilters: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(categories, id: \.self) { category in
                    FilterChip(
                        title: category,
                        isSelected: category == "All" ? selectedCategory == nil : selectedCategory == category,
                        action: {
                            if category == "All" {
                                selectedCategory = nil
                                viewModel.loadCocktails()
                            } else {
                                selectedCategory = category
                                viewModel.loadByCategory(category: category)
                            }
                        }
                    )
                }
            }
            .padding(.horizontal)
        }
    }
    
    private var loadingView: some View {
        VStack {
            ProgressView()
                .scaleEffect(1.5)
                .padding()
            Text("Loading cocktails...")
                .foregroundColor(.secondary)
        }
    }
    
    private var featuredSection: some View {
        VStack(alignment: .leading) {
            Text("Featured")
                .font(.title2)
                .bold()
                .padding(.bottom, 8)
            
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 16) {
                    ForEach(viewModel.featuredCocktails, id: \.id) { cocktail in
                        NavigationLink(
                            destination: CocktailDetailScreen(cocktailId: cocktail.id)
                        ) {
                            FeaturedCocktailCard(cocktail: cocktail)
                        }
                    }
                }
            }
        }
    }
    
    private var popularSection: some View {
        VStack(alignment: .leading) {
            Text("Popular")
                .font(.title2)
                .bold()
                .padding(.bottom, 8)
            
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 16) {
                    ForEach(viewModel.popularCocktails, id: \.id) { cocktail in
                        NavigationLink(
                            destination: CocktailDetailScreen(cocktailId: cocktail.id)
                        ) {
                            PopularCocktailCard(cocktail: cocktail)
                        }
                    }
                }
            }
        }
    }
    
    private var cocktailList: some View {
        LazyVStack(spacing: 16) {
            if !searchText.isEmpty {
                Text("Search Results")
                    .font(.title2)
                    .bold()
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            
            ForEach(viewModel.cocktails, id: \.id) { cocktail in
                NavigationLink(
                    destination: CocktailDetailScreen(cocktailId: cocktail.id)
                ) {
                    CocktailCard(cocktail: cocktail)
                }
            }
            
            if viewModel.hasMoreData {
                ProgressView()
                    .onAppear {
                        viewModel.loadMore()
                    }
            }
        }
    }
}

private struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color.accentColor : Color(.systemGray6))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(16)
        }
    }
}

private struct FeaturedCocktailCard: View {
    let cocktail: Cocktail
    
    var body: some View {
        VStack(alignment: .leading) {
            AsyncImage(url: URL(string: cocktail.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.3)
            }
            .frame(width: 280, height: 180)
            .clipped()
            .cornerRadius(12)
            
            Text(cocktail.name)
                .font(.headline)
                .lineLimit(1)
            
            Text(cocktail.description_ ?? "")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .lineLimit(2)
            
            HStack {
                Text("$\(String(format: "%.2f", cocktail.price))")
                    .font(.callout)
                    .bold()
                
                Spacer()
                
                HStack(spacing: 4) {
                    Image(systemName: "star.fill")
                        .foregroundColor(.yellow)
                    Text(String(format: "%.1f", cocktail.rating))
                        .font(.callout)
                }
            }
        }
        .frame(width: 280)
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

private struct PopularCocktailCard: View {
    let cocktail: Cocktail
    
    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: cocktail.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.3)
            }
            .frame(width: 80, height: 80)
            .clipped()
            .cornerRadius(8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(cocktail.name)
                    .font(.headline)
                    .lineLimit(1)
                
                if let category = cocktail.category {
                    Text(category)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                HStack {
                    Text("$\(String(format: "%.2f", cocktail.price))")
                        .font(.callout)
                        .bold()
                    
                    Spacer()
                    
                    HStack(spacing: 4) {
                        Image(systemName: "star.fill")
                            .foregroundColor(.yellow)
                        Text(String(format: "%.1f", cocktail.rating))
                            .font(.callout)
                    }
                }
            }
        }
        .padding()
        .frame(width: 280)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}