import SwiftUI
import shared

struct MainScreen: View {
    @StateObject private var viewModel = Dependencies.mainViewModel
    @State private var showingError = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Search Bar
                searchBar
                
                // Content
                content
                    .refreshable {
                        viewModel.refreshCocktails()
                    }
            }
            .navigationTitle("CocktailCraft")
            .alert("Error", isPresented: $showingError) {
                Button("OK") {
                    viewModel.clearError()
                }
            } message: {
                Text(viewModel.error ?? "")
            }
            .onAppear {
                viewModel.loadCocktails()
            }
        }
    }
    
    private var searchBar: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.gray)
            
            TextField("Search cocktails...", text: Binding(
                get: { viewModel.searchQuery },
                set: { viewModel.onSearchQueryChanged($0) }
            ))
            .textFieldStyle(RoundedBorderTextFieldStyle())
            
            if !viewModel.searchQuery.isEmpty {
                Button(action: {
                    viewModel.onSearchQueryChanged("")
                }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.gray)
                }
            }
        }
        .padding()
    }
    
    private var content: some View {
        Group {
            if viewModel.isLoading && viewModel.cocktails.isEmpty {
                ProgressView("Loading...")
                    .progressViewStyle(CircularProgressViewStyle())
            } else if viewModel.cocktails.isEmpty {
                emptyState
            } else {
                cocktailsList
            }
        }
    }
    
    private var cocktailsList: some View {
        ScrollView {
            LazyVStack(spacing: AppTheme.padding) {
                ForEach(viewModel.cocktails, id: \.id) { cocktail in
                    NavigationLink(destination: CocktailDetailScreen(cocktail: cocktail)) {
                        CocktailCard(
                            cocktail: cocktail,
                            onFavoriteClick: {
                                viewModel.toggleFavorite(cocktailId: cocktail.id)
                            }
                        )
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
            .padding()
        }
    }
    
    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "wineglass")
                .font(.system(size: 64))
                .foregroundColor(.gray)
            
            Text("No cocktails found")
                .font(AppTheme.Typography.headlineFont)
            
            Text("Try adjusting your search or filters")
                .font(AppTheme.Typography.captionFont)
                .foregroundColor(.secondary)
            
            Button("Refresh") {
                viewModel.refreshCocktails()
            }
            .buttonStyle(.bordered)
        }
        .padding()
    }
}

#Preview {
    MainScreen()
        .withAppTheme()
}