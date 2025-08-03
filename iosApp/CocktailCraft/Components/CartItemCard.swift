import SwiftUI
import shared

struct CartItemCard: View {
    let item: CartItem
    let onIncrementQuantity: () -> Void
    let onDecrementQuantity: () -> Void
    let onRemoveFromCart: () -> Void
    let onToggleFavorite: (() -> Void)?
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        HStack(spacing: AppTheme.Spacing.lg) {
            // Cocktail Image
            CocktailImageView(
                imageUrl: item.cocktail.imageUrl,
                height: 80,
                cornerRadius: AppTheme.CornerRadius.md
            )
            .frame(width: 80, height: 80)

            // Content
            VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
                // Name and Favorite
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(item.cocktail.name)
                            .font(AppTheme.Typography.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                            .lineLimit(2)

                        Text(item.cocktail.category ?? "Unknown")
                            .font(AppTheme.Typography.caption)
                            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    }

                    Spacer()

                    if let onToggleFavorite = onToggleFavorite {
                        Button(action: onToggleFavorite) {
                            Image(systemName: "heart")
                                .font(.title3)
                                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }

                // Price
                Text("$\(item.cocktail.price, specifier: "%.2f")")
                    .font(AppTheme.Typography.headline)
                    .fontWeight(.bold)
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))

                Spacer()

                // Quantity Controls and Total
                HStack {
                    // Quantity Controls
                    QuantityControlView(
                        quantity: item.quantity,
                        onIncrement: onIncrementQuantity,
                        onDecrement: onDecrementQuantity
                    )

                    Spacer()

                    // Item Total and Delete
                    HStack(spacing: AppTheme.Spacing.sm) {
                        Text("$\(item.cocktail.price * Double(item.quantity), specifier: "%.2f")")
                            .font(AppTheme.Typography.headline)
                            .fontWeight(.bold)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        Button(action: onRemoveFromCart) {
                            Image(systemName: "trash")
                                .font(.system(size: 16))
                                .foregroundColor(AppColors.error)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(AppTheme.Spacing.lg)
        .cardStyle()
    }
}

struct QuantityControlView: View {
    let quantity: Int32
    let onIncrement: () -> Void
    let onDecrement: () -> Void
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        HStack(spacing: AppTheme.Spacing.md) {
            Button(action: onDecrement) {
                Image(systemName: "minus")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                    .frame(width: 32, height: 32)
                    .background(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .clipShape(Circle())
            }
            .buttonStyle(PlainButtonStyle())

            Text("\(quantity)")
                .font(AppTheme.Typography.headline)
                .fontWeight(.bold)
                .frame(minWidth: 30)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            Button(action: onIncrement) {
                Image(systemName: "plus")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                    .frame(width: 32, height: 32)
                    .background(AppColors.primary(isDarkMode: isDarkMode))
                    .clipShape(Circle())
            }
            .buttonStyle(PlainButtonStyle())
        }
    }
}

#Preview {
    CartItemCard(
        item: CartItem(
            cocktail: Cocktail(
                id: "test-1",
                name: "Classic Margarita",
                alternateName: nil,
                tags: ["IBA"],
                category: "Ordinary Drink",
                iba: nil,
                alcoholic: "Alcoholic",
                glass: "Cocktail glass",
                instructions: nil,
                imageUrl: "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg",
                ingredients: [],
                imageSource: nil,
                imageAttribution: nil,
                creativeCommonsConfirmed: nil,
                dateModified: nil,
                price: 12.99,
                inStock: true,
                stockCount: 15,
                rating: 4.8,
                popularity: 95,
                dateAdded: 1672531300000
            ),
            quantity: 2
        ),
        onIncrementQuantity: {},
        onDecrementQuantity: {},
        onRemoveFromCart: {},
        onToggleFavorite: {}
    )
    .environment(\.isDarkMode, false)
    .padding()
}
