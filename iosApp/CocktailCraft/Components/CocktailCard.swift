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
    
    // Inverse of CocktailDetailViewModelSKIE.canAddToCart() — the card and
    // the detail screen must agree on when Add to Cart is available.
    private var isOutOfStock: Bool {
        !cocktail.inStock || cocktail.stockCount <= 0
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
            // Cocktail Image - Larger size (Kingfisher-backed: disk+memory
            // cache and retry, instead of AsyncImage refetching on recycle)
            ZStack(alignment: .center) {
                CocktailImageView(
                    imageUrl: cocktail.imageUrl,
                    height: 120,
                    cornerRadius: 12,
                    width: 120
                )

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
                    .font(.headline.weight(.bold))
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                
                // Category Info (Alcoholic • Category)
                if let category = cocktail.category {
                    Text("Alcoholic • \(category)")
                        .font(.subheadline.weight(.medium))
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .lineLimit(1)
                }
                
                // Ingredients (first 2 or placeholder)
                Text(getIngredientsText())
                    .font(.footnote.weight(.regular))
                    .italic()
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                
                Spacer(minLength: 8)
                
                // Price and Actions Row
                HStack(alignment: .center) {
                    // Price - Larger and more prominent
                    Text((cocktail.price ?? 12.99).asPrice)
                        .font(.title3.weight(.bold))
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                    
                    Spacer()
                    
                    // Action Buttons - Larger and more spaced
                    HStack(spacing: 12) {
                        // Favorite Button
                        if let onToggle = onFavoriteToggle {
                            Button(action: onToggle) {
                                Image(systemName: isFavorite ? "heart.fill" : "heart")
                                    .font(.title2)
                                    .foregroundColor(isFavorite ? AppColors.secondary(isDarkMode: isDarkMode) : AppColors.gray)
                                    .minimumHitTarget()
                            }
                            .buttonStyle(.borderless)
                            .accessibilityLabel(isFavorite ? "Remove from favorites" : "Add to favorites")
                            .accessibilityIdentifier("card.favoriteButton")
                        }

                        // Add to Cart Button
                        if let onAddToCart = onAddToCart {
                            Button(action: onAddToCart) {
                                Image(systemName: "cart.badge.plus")
                                    .font(.title2)
                                    .foregroundColor(isOutOfStock ? AppColors.gray : AppColors.primary(isDarkMode: isDarkMode))
                                    .minimumHitTarget()
                            }
                            .disabled(isOutOfStock)
                            .buttonStyle(.borderless)
                            .accessibilityLabel("Add to cart")
                            .accessibilityIdentifier("card.addToCartButton")
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
            // Cocktail Image - Full width (Kingfisher-backed, see above)
            ZStack(alignment: .center) {
                CocktailImageView(
                    imageUrl: cocktail.imageUrl,
                    height: 140,
                    cornerRadius: 12
                )

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
                    .font(AppTheme.Typography.cardTitle)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                // Category Info (Alcoholic • Category)
                if let category = cocktail.category {
                    Text("Alcoholic • \(category)")
                        .font(.footnote.weight(.medium))
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .lineLimit(2)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                
                // Ingredients (first 2 or placeholder)
                Text(getIngredientsText())
                    .font(AppTheme.Typography.cardCaption)
                    .italic()
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                // Price and Actions Row
                HStack(alignment: .center) {
                    // Price - Prominent
                    Text((cocktail.price ?? 12.99).asPrice)
                        .font(.headline.weight(.bold))
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                    
                    Spacer()
                    
                    // Action Buttons - Compact
                    HStack(spacing: 8) {
                        // Favorite Button
                        if let onToggle = onFavoriteToggle {
                            Button(action: onToggle) {
                                Image(systemName: isFavorite ? "heart.fill" : "heart")
                                    .font(.title3)
                                    .foregroundColor(isFavorite ? AppColors.secondary(isDarkMode: isDarkMode) : AppColors.gray)
                                    .minimumHitTarget()
                            }
                            .buttonStyle(.borderless)
                            .accessibilityLabel(isFavorite ? "Remove from favorites" : "Add to favorites")
                            .accessibilityIdentifier("card.favoriteButton")
                        }

                        // Add to Cart Button
                        if let onAddToCart = onAddToCart {
                            Button(action: onAddToCart) {
                                Image(systemName: "cart.badge.plus")
                                    .font(.title3)
                                    .foregroundColor(isOutOfStock ? AppColors.gray : AppColors.primary(isDarkMode: isDarkMode))
                                    .minimumHitTarget()
                            }
                            .disabled(isOutOfStock)
                            .buttonStyle(.borderless)
                            .accessibilityLabel("Add to cart")
                            .accessibilityIdentifier("card.addToCartButton")
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