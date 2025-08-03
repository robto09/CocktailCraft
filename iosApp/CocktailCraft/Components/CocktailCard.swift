import SwiftUI
import shared

enum CocktailCardLayout {
    case horizontal // For Home tab list
    case vertical   // For Favorites grid
}

struct CocktailCard: View {
    let cocktail: Cocktail
    var isFavorite: Bool = false
    var onFavoriteToggle: (() -> Void)? = nil
    var onAddToCart: (() -> Void)? = nil
    var onCardTap: (() -> Void)? = nil
    var layout: CocktailCardLayout = .horizontal
    
    // Check if cocktail is out of stock (using stockCount if available)
    private var isOutOfStock: Bool {
        // Assuming Cocktail model has stockCount property, otherwise default to false
        return false // cocktail.stockCount <= 0
    }
    
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        switch layout {
        case .horizontal:
            horizontalLayout
        case .vertical:
            verticalLayout
        }
    }
    
    @ViewBuilder
    private var horizontalLayout: some View {
        HStack(spacing: 16) {
            // Cocktail Image - Larger size
            ZStack(alignment: .center) {
                AsyncImage(url: URL(string: cocktail.imageUrl ?? "")) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(AppColors.lightGray)
                        .overlay(
                            ProgressView()
                                .scaleEffect(1.2)
                        )
                }
                .frame(width: 120, height: 120)
                .clipped()
                .cornerRadius(12)
                
                // Stock overlay for out of stock items
                if isOutOfStock {
                    Rectangle()
                        .fill(Color.black.opacity(0.6))
                        .cornerRadius(12)
                        .overlay(
                            Text("Out of Stock")
                                .font(.caption)
                                .foregroundColor(.white)
                                .fontWeight(.medium)
                        )
                }
            }
            
            // Content Section
            VStack(alignment: .leading, spacing: 6) {
                // Cocktail Name - Larger font
                Text(cocktail.name)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                
                // Category Info (Alcoholic • Category)
                if let category = cocktail.category {
                    Text("Alcoholic • \(category)")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .lineLimit(1)
                }
                
                // Ingredients (first 2 or placeholder)
                Text(getIngredientsText())
                    .font(.system(size: 13, weight: .regular))
                    .italic()
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                
                Spacer(minLength: 8)
                
                // Price and Actions Row
                HStack(alignment: .center) {
                    // Price - Larger and more prominent
                    Text(String(format: "$%.2f", cocktail.price ?? 12.99))
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                    
                    Spacer()
                    
                    // Action Buttons - Larger and more spaced
                    HStack(spacing: 12) {
                        // Favorite Button
                        if let onToggle = onFavoriteToggle {
                            Button(action: onToggle) {
                                Image(systemName: isFavorite ? "heart.fill" : "heart")
                                    .font(.system(size: 24))
                                    .foregroundColor(isFavorite ? AppColors.secondary(isDarkMode: isDarkMode) : AppColors.gray)
                            }
                            .frame(width: 40, height: 40)
                            .buttonStyle(.borderless)
                        }
                        
                        // Add to Cart Button
                        if let onAddToCart = onAddToCart {
                            Button(action: onAddToCart) {
                                Image(systemName: "cart.badge.plus")
                                    .font(.system(size: 24))
                                    .foregroundColor(isOutOfStock ? AppColors.gray : AppColors.primary(isDarkMode: isDarkMode))
                            }
                            .frame(width: 40, height: 40)
                            .disabled(isOutOfStock)
                            .buttonStyle(.borderless)
                        }
                    }
                }
            }
            .padding(.vertical, 8)
        }
        .padding(16)
        .background(AppColors.surface(isDarkMode: isDarkMode))
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.12), radius: 6, x: 0, y: 3)
        .if(onCardTap != nil) { view in
            view.onTapGesture {
                onCardTap?()
            }
        }
    }
    
    @ViewBuilder
    private var verticalLayout: some View {
        VStack(spacing: 12) {
            // Cocktail Image - Full width
            ZStack(alignment: .center) {
                AsyncImage(url: URL(string: cocktail.imageUrl ?? "")) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(AppColors.lightGray)
                        .overlay(
                            ProgressView()
                                .scaleEffect(1.2)
                        )
                }
                .frame(height: 140)
                .clipped()
                .cornerRadius(12)
                
                // Stock overlay for out of stock items
                if isOutOfStock {
                    Rectangle()
                        .fill(Color.black.opacity(0.6))
                        .cornerRadius(12)
                        .overlay(
                            Text("Out of Stock")
                                .font(.caption)
                                .foregroundColor(.white)
                                .fontWeight(.medium)
                        )
                }
            }
            
            // Content Section
            VStack(alignment: .leading, spacing: 8) {
                // Cocktail Name - Allow more lines
                Text(cocktail.name)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                // Category Info (Alcoholic • Category)
                if let category = cocktail.category {
                    Text("Alcoholic • \(category)")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .lineLimit(2)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                
                // Ingredients (first 2 or placeholder)
                Text(getIngredientsText())
                    .font(.system(size: 12, weight: .regular))
                    .italic()
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                // Price and Actions Row
                HStack(alignment: .center) {
                    // Price - Prominent
                    Text(String(format: "$%.2f", cocktail.price ?? 12.99))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                    
                    Spacer()
                    
                    // Action Buttons - Compact
                    HStack(spacing: 8) {
                        // Favorite Button
                        if let onToggle = onFavoriteToggle {
                            Button(action: onToggle) {
                                Image(systemName: isFavorite ? "heart.fill" : "heart")
                                    .font(.system(size: 20))
                                    .foregroundColor(isFavorite ? AppColors.secondary(isDarkMode: isDarkMode) : AppColors.gray)
                            }
                            .frame(width: 32, height: 32)
                            .buttonStyle(.borderless)
                        }
                        
                        // Add to Cart Button
                        if let onAddToCart = onAddToCart {
                            Button(action: onAddToCart) {
                                Image(systemName: "cart.badge.plus")
                                    .font(.system(size: 20))
                                    .foregroundColor(isOutOfStock ? AppColors.gray : AppColors.primary(isDarkMode: isDarkMode))
                            }
                            .frame(width: 32, height: 32)
                            .disabled(isOutOfStock)
                            .buttonStyle(.borderless)
                        }
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
        }
        .background(AppColors.surface(isDarkMode: isDarkMode))
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.12), radius: 6, x: 0, y: 3)
        .if(onCardTap != nil) { view in
            view.onTapGesture {
                onCardTap?()
            }
        }
    }
    
    private func getIngredientsText() -> String {
        // Try to get first 2 ingredients if available
        if let ingredients = cocktail.ingredients as? [CocktailIngredient], !ingredients.isEmpty {
            let firstTwo = Array(ingredients.prefix(2))
            let ingredientNames = firstTwo.compactMap { $0.name }
            return ingredientNames.joined(separator: ", ")
        }
        return "Premium ingredients"
    }
}

// MARK: - View Extension for Conditional Modifiers
extension View {
    @ViewBuilder func `if`<Content: View>(_ condition: Bool, transform: (Self) -> Content) -> some View {
        if condition {
            transform(self)
        } else {
            self
        }
    }
}