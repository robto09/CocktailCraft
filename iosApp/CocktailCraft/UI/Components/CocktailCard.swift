import SwiftUI
import shared

struct CocktailCard: View {
    let cocktail: Cocktail
    var onFavoriteClick: () async -> Void
    var onAddToCart: () async -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Image placeholder - replace with actual image loading
            Rectangle()
                .fill(Color.gray.opacity(0.2))
                .aspectRatio(4/3, contentMode: .fit)
                .cornerRadius(8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(cocktail.name)
                    .font(.headline)
                
                Text(cocktail.description_)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
                
                HStack {
                    Text("$\(String(format: "%.2f", cocktail.price))")
                        .font(.callout)
                        .bold()
                    
                    Spacer()
                    
                    Button(action: {
                        Task {
                            await onFavoriteClick()
                        }
                    }) {
                        Image(systemName: cocktail.isFavorite ? "heart.fill" : "heart")
                            .foregroundColor(cocktail.isFavorite ? .red : .gray)
                    }
                    
                    Button(action: {
                        Task {
                            await onAddToCart()
                        }
                    }) {
                        Image(systemName: "cart.badge.plus")
                    }
                }
            }
            .padding(.horizontal, 8)
        }
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}

// Preview provider
struct CocktailCard_Previews: PreviewProvider {
    static var previews: some View {
        CocktailCard(
            cocktail: Cocktail(
                id: "1",
                name: "Mojito",
                description: "Classic Cuban cocktail with rum, mint, and lime",
                price: 9.99,
                imageUrl: "",
                ingredients: [],
                isFavorite: false
            ),
            onFavoriteClick: {},
            onAddToCart: {}
        )
        .padding()
        .previewLayout(.sizeThatFits)
    }
}