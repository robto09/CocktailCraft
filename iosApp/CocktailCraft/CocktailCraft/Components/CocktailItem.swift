import SwiftUI
import shared

struct CocktailItem: View {
    let cocktail: Cocktail
    let onClick: () -> Void
    let onAddToCart: (Cocktail) -> Void
    let isFavorite: Bool
    let onToggleFavorite: (Cocktail) -> Void
    
    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 12) {
                // Cocktail image
                ZStack {
                    if let url = URL(string: cocktail.imageUrl ?? "") {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .empty:
                                Rectangle()
                                    .fill(Color.gray.opacity(0.3))
                            case .success(let image):
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                            case .failure(_):
                                Rectangle()
                                    .fill(Color.gray.opacity(0.2))
                                    .overlay(
                                        Image(systemName: "photo")
                                            .foregroundColor(.gray)
                                    )
                            @unknown default:
                                EmptyView()
                            }
                        }
                        .frame(width: 100, height: 100)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                    
                    // Out of stock overlay
                    if cocktail.stockCount <= 0 {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.black.opacity(0.5))
                            .overlay(
                                Text("Out of Stock")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                    .multilineTextAlignment(.center)
                                    .padding(4)
                            )
                    }
                }
                .frame(width: 100, height: 100)
                
                // Cocktail details
                VStack(alignment: .leading, spacing: 4) {
                    Text(cocktail.name)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.primary)
                        .lineLimit(1)
                    
                    // Alcoholic info with category
                    HStack(spacing: 0) {
                        Text(cocktail.alcoholic ?? "Unknown")
                        if let category = cocktail.category {
                            Text(" • \(category)")
                        }
                    }
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                    
                    // Ingredients
                    Text(ingredientsText)
                        .font(.system(size: 12))
                        .italic()
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                    
                    Spacer(minLength: 8)
                    
                    // Price and actions
                    HStack {
                        Text("$\(String(format: "%.2f", cocktail.price))")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.orange)
                        
                        Spacer()
                        
                        // Favorite button
                        Button(action: { onToggleFavorite(cocktail) }) {
                            Image(systemName: isFavorite ? "heart.fill" : "heart")
                                .font(.system(size: 16))
                                .foregroundColor(isFavorite ? .red : .gray)
                        }
                        .buttonStyle(PlainButtonStyle())
                        .frame(width: 32, height: 32)
                        
                        // Add to cart button
                        Button(action: { onAddToCart(cocktail) }) {
                            Image(systemName: "cart.badge.plus")
                                .font(.system(size: 16))
                                .foregroundColor(cocktail.stockCount > 0 ? .orange : .gray)
                        }
                        .buttonStyle(PlainButtonStyle())
                        .frame(width: 32, height: 32)
                        .disabled(cocktail.stockCount <= 0)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(12)
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(12)
            .shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 1)
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private var ingredientsText: String {
        if cocktail.ingredients.isEmpty {
            return "No ingredients listed"
        } else {
            let firstTwo = cocktail.ingredients.prefix(2).map { $0.name }.joined(separator: ", ")
            return cocktail.ingredients.count > 2 ? "\(firstTwo)..." : firstTwo
        }
    }
}

#Preview {
    EmptyView()
    // Preview disabled due to complex Cocktail initializer requirements
}