import SwiftUI
import shared

struct MainScreen: View {
    @StateObject private var viewModel = DependencyContainer.shared.makeMainViewModel()
    @Environment(\.colorScheme) private var colorScheme
    
    var body: some View {
        TabView {
            HomeTab(viewModel: viewModel)
                .tabItem {
                    Label("Home", systemImage: "house.fill")
                }
            
            FavoritesTab()
                .tabItem {
                    Label("Favorites", systemImage: "heart.fill")
                }
            
            CartTab()
                .tabItem {
                    Label("Cart", systemImage: "cart.fill")
                }
            
            ProfileTab()
                .tabItem {
                    Label("Profile", systemImage: "person.fill")
                }
        }
        .accentColor(colorScheme == .dark ? .white : .black)
    }
}

// MARK: - Home Tab
private struct HomeTab: View {
    @ObservedObject var viewModel: MainViewModel
    @State private var searchText = ""
    @State private var showingAdvancedSearch = false
    
    var body: some View {
        NavigationView {
            ZStack {
                Color(.systemBackground)
                    .ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // Search Bar
                    searchBar
                    
                    // Main Content
                    if viewModel.isLoading {
                        ProgressView()
                            .scaleEffect(1.5)
                            .padding()
                    } else {
                        ScrollView {
                            LazyVStack(spacing: 16) {
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
                                            viewModel.loadMoreCocktails()
                                        }
                                }
                            }
                            .padding()
                        }
                        .refreshable {
                            viewModel.loadCocktails()
                        }
                    }
                }
            }
            .navigationTitle("CocktailCraft")
            .sheet(isPresented: $showingAdvancedSearch) {
                AdvancedSearchView(
                    filters: viewModel.searchFilters,
                    onApply: { filters in
                        viewModel.updateSearchFilters(filters: filters)
                        showingAdvancedSearch = false
                    }
                )
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
                        viewModel.searchCocktails(query: query)
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
}

// MARK: - Cocktail Card
private struct CocktailCard: View {
    let cocktail: Cocktail
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            AsyncImage(url: URL(string: cocktail.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.3)
            }
            .frame(height: 200)
            .clipped()
            .cornerRadius(12)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(cocktail.name)
                    .font(.headline)
                
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
            .padding(.horizontal, 8)
            .padding(.bottom, 8)
        }
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 4)
    }
}

// MARK: - Advanced Search View
private struct AdvancedSearchView: View {
    let filters: SearchFilters
    let onApply: (SearchFilters) -> Void
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationView {
            Form {
                // Add filter options here based on SearchFilters model
                // This is a placeholder for the actual implementation
                Text("Advanced Search Options")
            }
            .navigationTitle("Advanced Search")
            .navigationBarItems(
                leading: Button("Cancel") {
                    dismiss()
                },
                trailing: Button("Apply") {
                    onApply(filters)
                }
            )
        }
    }
}

// MARK: - Preview
#Preview {
    MainScreen()
}