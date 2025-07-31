import SwiftUI
@preconcurrency import shared

/**
 * Proof of Concept: SwiftUI view testing shared ViewModel with SKIE
 * 
 * This view demonstrates:
 * - Using shared ViewModel across platforms
 * - SKIE StateFlow → @Published automatic conversion
 * - SKIE suspend functions → Swift async functions
 * - Type safety without casting
 */
struct SharedViewModelTestView: View {
    @StateObject private var viewModel = HomeViewModelSKIE()
    @State private var searchText = ""
    
    var body: some View {
        NavigationView {
            VStack {
                // Search Bar
                SearchBar(text: $searchText, onSearchButtonClicked: {
                    Task {
                        await viewModel.searchCocktails(query: searchText)
                    }
                })
                
                // Status Information
                HStack {
                    Text("Cocktails: \(viewModel.cocktails.count)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    if viewModel.isSearchActive {
                        Text("Searching: '\(viewModel.searchQuery)'")
                            .font(.caption)
                            .foregroundColor(.blue)
                    } else if viewModel.selectedCategory != nil {
                        Text("Category: \(viewModel.selectedCategory ?? "")")
                            .font(.caption)
                            .foregroundColor(.green)
                    }
                }
                .padding(.horizontal)
                
                // Content
                if viewModel.isLoading {
                    Spacer()
                    ProgressView("Loading cocktails...")
                    Spacer()
                } else if viewModel.cocktails.isEmpty {
                    Spacer()
                    VStack {
                        Image(systemName: "wineglass")
                            .font(.system(size: 50))
                            .foregroundColor(.gray)
                        Text("No cocktails found")
                            .font(.headline)
                            .foregroundColor(.gray)
                        Text("Try searching for something else")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                } else {
                    // Cocktail List
                    List(viewModel.cocktails, id: \.id) { cocktail in
                        CocktailRowView(cocktail: cocktail)
                    }
                    .refreshable {
                        await viewModel.refreshCocktails()
                    }
                }
                
                // Category Buttons
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        CategoryButton(title: "All", isSelected: viewModel.selectedCategory == nil) {
                            viewModel.clearSearch()
                        }
                        
                        CategoryButton(title: "Cocktail", isSelected: viewModel.selectedCategory == "Cocktail") {
                            Task {
                                await viewModel.loadCocktailsByCategory("Cocktail")
                            }
                        }
                        
                        CategoryButton(title: "Shot", isSelected: viewModel.selectedCategory == "Shot") {
                            Task {
                                await viewModel.loadCocktailsByCategory("Shot")
                            }
                        }
                        
                        CategoryButton(title: "Beer", isSelected: viewModel.selectedCategory == "Beer") {
                            Task {
                                await viewModel.loadCocktailsByCategory("Beer")
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom)
            }
            .navigationTitle("Shared ViewModel Test")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Refresh") {
                        Task {
                            await viewModel.refreshCocktails()
                        }
                    }
                }
            }
            .alert("Error", isPresented: .constant(viewModel.error != nil)) {
                Button("OK") {
                    viewModel.clearError()
                }
            } message: {
                Text(viewModel.error?.message ?? "Unknown error")
            }
        }
    }
}

// MARK: - Supporting Views

struct CocktailRowView: View {
    let cocktail: Cocktail
    
    var body: some View {
        HStack {
            AsyncImage(url: URL(string: cocktail.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Rectangle()
                    .fill(Color.gray.opacity(0.3))
            }
            .frame(width: 60, height: 60)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            
            VStack(alignment: .leading, spacing: 4) {
                Text(cocktail.name)
                    .font(.headline)
                    .lineLimit(1)
                
                if let category = cocktail.category {
                    Text(category)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                if let alcoholic = cocktail.alcoholic {
                    Text(alcoholic)
                        .font(.caption2)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(Color.blue.opacity(0.1))
                        .foregroundColor(.blue)
                        .clipShape(Capsule())
                }
            }
            
            Spacer()
        }
        .padding(.vertical, 4)
    }
}

struct CategoryButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption)
                .fontWeight(isSelected ? .semibold : .regular)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color.blue : Color.gray.opacity(0.2))
                .foregroundColor(isSelected ? .white : .primary)
                .clipShape(Capsule())
        }
    }
}

struct SearchBar: View {
    @Binding var text: String
    let onSearchButtonClicked: () -> Void
    
    var body: some View {
        HStack {
            TextField("Search cocktails...", text: $text)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .onSubmit {
                    onSearchButtonClicked()
                }
            
            Button("Search", action: onSearchButtonClicked)
                .buttonStyle(.borderedProminent)
        }
        .padding(.horizontal)
    }
}

// MARK: - Preview
struct SharedViewModelTestView_Previews: PreviewProvider {
    static var previews: some View {
        SharedViewModelTestView()
    }
}
