import SwiftUI
import Shared

struct AnimatedCocktailItem: View {
    let cocktail: Cocktail
    let index: Int
    let isFavorite: Bool
    let onFavoriteToggle: () -> Void
    let onAddToCart: () -> Void
    let onTap: () -> Void
    
    @State private var isPressed = false
    @State private var isVisible = false
    
    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                // Cocktail Image
                AsyncImage(url: URL(string: cocktail.strDrinkThumb ?? "")) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color(.systemGray5))
                        .overlay(
                            Image(systemName: "photo")
                                .foregroundColor(.secondary)
                        )
                }
                .frame(width: 100, height: 100)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                
                VStack(alignment: .leading, spacing: 4) {
                    // Name
                    Text(cocktail.strDrink)
                        .font(.headline)
                        .lineLimit(1)
                        .foregroundColor(.primary)
                    
                    // Category and Type
                    Text("\(cocktail.strAlcoholic ?? "Unknown") • \(cocktail.strCategory ?? "Cocktail")")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    // Ingredients Preview
                    if let ingredients = getIngredientsPreview() {
                        Text(ingredients)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                    
                    Spacer()
                    
                    HStack {
                        // Price
                        Text(String(format: "$%.2f", cocktail.price))
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        Spacer()
                        
                        // Favorite Button
                        Button(action: onFavoriteToggle) {
                            Image(systemName: isFavorite ? "heart.fill" : "heart")
                                .foregroundColor(isFavorite ? .red : .secondary)
                        }
                        .buttonStyle(.plain)
                        
                        // Add to Cart Button
                        Button(action: onAddToCart) {
                            Text("Add to Cart")
                                .font(.caption)
                                .fontWeight(.semibold)
                                .foregroundColor(.white)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(cocktail.stockCount > 0 ? Color.accentColor : Color.gray)
                                .cornerRadius(6)
                        }
                        .buttonStyle(.plain)
                        .disabled(cocktail.stockCount <= 0)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding()
            .background(Color(.systemBackground))
            .cornerRadius(16)
            .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
            .overlay(
                // Out of Stock Overlay
                Group {
                    if cocktail.stockCount <= 0 {
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color.black.opacity(0.5))
                        Text("Out of Stock")
                            .font(.headline)
                            .foregroundColor(.white)
                            .rotationEffect(.degrees(-15))
                    }
                }
            )
        }
        .buttonStyle(.plain)
        .scaleEffect(isPressed ? 0.97 : 1.0)
        .opacity(isVisible ? 1 : 0)
        .offset(y: isVisible ? 0 : 20)
        .animation(
            .spring(response: 0.3, dampingFraction: 0.7)
                .delay(Double(index % 3) * 0.1),
            value: isVisible
        )
        .animation(.spring(response: 0.3, dampingFraction: 0.7), value: isPressed)
        .onAppear {
            isVisible = true
        }
        .onLongPressGesture(
            minimumDuration: 0,
            maximumDistance: .infinity,
            pressing: { pressing in
                isPressed = pressing
            },
            perform: {}
        )
    }
    
    private func getIngredientsPreview() -> String? {
        let ingredients = [
            cocktail.strIngredient1,
            cocktail.strIngredient2,
            cocktail.strIngredient3
        ].compactMap { $0 }.filter { !$0.isEmpty }
        
        guard !ingredients.isEmpty else { return nil }
        
        if ingredients.count > 2 {
            return "\(ingredients[0]), \(ingredients[1])..."
        } else {
            return ingredients.joined(separator: ", ")
        }
    }
}