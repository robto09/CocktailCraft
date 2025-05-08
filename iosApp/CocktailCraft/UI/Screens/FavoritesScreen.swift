import SwiftUI
import shared

struct FavoritesScreen: View {
    @StateObject private var viewModel = DependencyContainer.shared.makeFavoritesViewModel()
    @State private var showingError = false
    @State private var showingClearConfirmation = false
    
    var body: some View {
        NavigationView {
            ZStack {
                if viewModel.isLoading {
                    loadingView
                } else if viewModel.favorites.isEmpty {
                    emptyView
                } else {
                    favoritesContent
                }
            }
            .navigationTitle("Favorites")
            .toolbar {
                if !viewModel.favorites.isEmpty {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Menu {
                            Button(role: .destructive, action: {
                                showingClearConfirmation = true
                            }) {
                                Label("Clear All", systemImage: "trash")
                            }
                            
                            Button(action: {
                                // TODO: Implement sharing
                            }) {
                                Label("Share List", systemImage: "square.and.arrow.up")
                            }
                            
                            Button(action: {
                                // TODO: Implement export
                            }) {
                                Label("Export", systemImage: "arrow.down.doc")
                            }
                        } label: {
                            Image(systemName: "ellipsis.circle")
                        }
                    }
                }
            }
            .alert("Error", isPresented: $showingError, presenting: viewModel.error) { _ in
                Button("OK") {}
            } message: { error in
                Text(error)
            }
            .confirmationDialog(
                "Clear All Favorites",
                isPresented: $showingClearConfirmation,
                titleVisibility: .visible
            ) {
                Button("Clear All", role: .destructive) {
                    viewModel.clearFavorites()
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("This action cannot be undone.")
            }
            .refreshable {
                viewModel.loadFavorites()
            }
        }
    }
    
    private var loadingView: some View {
        VStack {
            ProgressView()
                .scaleEffect(1.5)
            Text("Loading favorites...")
                .foregroundColor(.secondary)
                .padding(.top)
        }
    }
    
    private var emptyView: some View {
        VStack(spacing: 16) {
            Image(systemName: "heart")
                .font(.system(size: 64))
                .foregroundColor(.secondary)
            
            Text("No favorites yet")
                .font(.title2)
            
            Text("Add cocktails to your favorites list!")
                .foregroundColor(.secondary)
            
            NavigationLink(destination: MainScreen()) {
                Text("Browse Cocktails")
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding()
                    .background(Color.accentColor)
                    .cornerRadius(12)
            }
            .padding(.top)
        }
    }
    
    private var favoritesContent: some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                ForEach(viewModel.favorites, id: \.id) { cocktail in
                    FavoriteCard(
                        cocktail: cocktail,
                        onRemove: {
                            viewModel.removeFavorite(cocktailId: cocktail.id)
                        }
                    )
                }
            }
            .padding()
        }
    }
}

private struct FavoriteCard: View {
    let cocktail: Cocktail
    let onRemove: () -> Void
    
    @State private var offset: CGFloat = 0
    @State private var isSwiped = false
    
    var body: some View {
        NavigationLink(
            destination: CocktailDetailScreen(cocktailId: cocktail.id)
        ) {
            ZStack {
                // Delete button background
                HStack {
                    Spacer()
                    Button(action: delete) {
                        Image(systemName: "trash")
                            .font(.title2)
                            .foregroundColor(.white)
                            .frame(width: 60)
                    }
                    .background(Color.red)
                }
                
                // Card content
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
                            .foregroundColor(.primary)
                        
                        if let category = cocktail.category {
                            Text(category)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        
                        HStack {
                            Text("$\(String(format: "%.2f", cocktail.price))")
                                .font(.callout)
                                .bold()
                                .foregroundColor(.primary)
                            
                            Spacer()
                            
                            HStack(spacing: 4) {
                                Image(systemName: "star.fill")
                                    .foregroundColor(.yellow)
                                Text(String(format: "%.1f", cocktail.rating))
                                    .font(.callout)
                                    .foregroundColor(.primary)
                            }
                        }
                    }
                    
                    Spacer()
                }
                .padding()
                .background(Color(.systemBackground))
                .cornerRadius(12)
                .shadow(radius: 4)
                .offset(x: offset)
                .gesture(
                    DragGesture()
                        .onChanged { value in
                            if value.translation.width < 0 {
                                offset = max(value.translation.width, -60)
                            }
                        }
                        .onEnded { value in
                            withAnimation {
                                if value.translation.width < -50 {
                                    offset = -60
                                    isSwiped = true
                                } else {
                                    offset = 0
                                    isSwiped = false
                                }
                            }
                        }
                )
            }
        }
    }
    
    private func delete() {
        withAnimation {
            onRemove()
        }
    }
}