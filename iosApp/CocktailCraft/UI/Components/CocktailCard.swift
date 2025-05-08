import SwiftUI
import shared

struct CocktailCard: View {
    let cocktail: Cocktail
    var showFavoriteButton: Bool = true
    var onFavoriteToggle: (() -> Void)?
    var onAddToCart: (() -> Void)?
    
    @State private var showingAddToCartConfirmation = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Image
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
            .overlay(alignment: .topTrailing) {
                if showFavoriteButton {
                    favoriteButton
                }
            }
            
            // Content
            VStack(alignment: .leading, spacing: 4) {
                // Title and Category
                Text(cocktail.name)
                    .font(.headline)
                    .lineLimit(1)
                
                if let category = cocktail.category {
                    Text(category)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
                
                // Price and Rating
                HStack {
                    Text("$\(String(format: "%.2f", cocktail.price))")
                        .font(.callout)
                        .bold()
                    
                    Spacer()
                    
                    if let onAddToCart = onAddToCart {
                        Button(action: {
                            showingAddToCartConfirmation = true
                        }) {
                            Image(systemName: "cart.badge.plus")
                                .foregroundColor(.blue)
                        }
                    }
                    
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
        .alert("Add to Cart", isPresented: $showingAddToCartConfirmation) {
            Button("Cancel", role: .cancel) { }
            Button("Add") {
                onAddToCart?()
            }
        } message: {
            Text("Would you like to add \(cocktail.name) to your cart?")
        }
    }
    
    private var favoriteButton: some View {
        Button(action: {
            onFavoriteToggle?()
        }) {
            Image(systemName: "heart.fill")
                .foregroundColor(.red)
                .padding(8)
                .background(
                    Circle()
                        .fill(Color(.systemBackground))
                        .shadow(radius: 2)
                )
        }
        .padding(8)
    }
}

struct CocktailCardList: View {
    let cocktails: [Cocktail]
    var showFavoriteButton: Bool = true
    var onFavoriteToggle: ((Cocktail) -> Void)?
    var onAddToCart: ((Cocktail) -> Void)?
    var onCocktailTap: ((Cocktail) -> Void)?
    
    var body: some View {
        LazyVStack(spacing: 16) {
            ForEach(cocktails, id: \.id) { cocktail in
                Button(action: {
                    onCocktailTap?(cocktail)
                }) {
                    CocktailCard(
                        cocktail: cocktail,
                        showFavoriteButton: showFavoriteButton,
                        onFavoriteToggle: {
                            onFavoriteToggle?(cocktail)
                        },
                        onAddToCart: {
                            onAddToCart?(cocktail)
                        }
                    )
                }
            }
        }
        .padding()
    }
}

// MARK: - Compact Card Style
struct CompactCocktailCard: View {
    let cocktail: Cocktail
    var onAddToCart: (() -> Void)?
    
    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: cocktail.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.3)
            }
            .frame(width: 60, height: 60)
            .cornerRadius(8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(cocktail.name)
                    .font(.headline)
                    .lineLimit(1)
                
                if let category = cocktail.category {
                    Text(category)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Text("$\(String(format: "%.2f", cocktail.price))")
                    .font(.subheadline)
                    .bold()
            }
            
            Spacer()
            
            if let onAddToCart = onAddToCart {
                Button(action: onAddToCart) {
                    Image(systemName: "cart.badge.plus")
                        .foregroundColor(.blue)
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

#Preview("Full Card") {
    CocktailCard(
        cocktail: Cocktail(
            id: "1",
            name: "Mojito",
            category: "Cocktail",
            price: 12.99,
            rating: 4.5
        ),
        onFavoriteToggle: {},
        onAddToCart: {}
    )
    .padding()
}

#Preview("Compact Card") {
    CompactCocktailCard(
        cocktail: Cocktail(
            id: "1",
            name: "Mojito",
            category: "Cocktail",
            price: 12.99,
            rating: 4.5
        ),
        onAddToCart: {}
    )
    .padding()
}