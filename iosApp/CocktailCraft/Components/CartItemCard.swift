import SwiftUI
import shared

struct CartItemCard: View {
    let item: CocktailCartItem
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

                        Text(item.cocktail.category ?? L(\.common_unknown))
                            .font(AppTheme.Typography.caption)
                            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    }

                    Spacer()

                    if let onToggleFavorite = onToggleFavorite {
                        Button(action: onToggleFavorite) {
                            Image(systemName: "heart")
                                .font(.title3)
                                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                                .minimumHitTarget()
                        }
                        .buttonStyle(PlainButtonStyle())
                        .accessibilityLabel(L(\.favorite_toggle_a11y))
                        .accessibilityIdentifier("cart.favoriteButton")
                    }
                }

                // Price
                Text(item.cocktail.price.asPrice)
                    .font(AppTheme.Typography.headline)
                    .fontWeight(.bold)
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))

                Spacer()

                // Quantity Controls and Total: one row when it fits; otherwise
                // (narrow phones, large Dynamic Type) the line total and
                // remove button wrap below the stepper instead of the total
                // breaking across lines
                ViewThatFits(in: .horizontal) {
                    HStack {
                        quantityControls
                        Spacer()
                        totalAndRemove
                    }

                    VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
                        quantityControls
                        totalAndRemove
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(AppTheme.Spacing.lg)
        .cardStyle()
    }

    private var quantityControls: some View {
        QuantityControlView(
            itemName: item.cocktail.name,
            quantity: item.quantity,
            onIncrement: onIncrementQuantity,
            onDecrement: onDecrementQuantity
        )
    }

    private var totalAndRemove: some View {
        HStack(spacing: AppTheme.Spacing.sm) {
            Text((item.cocktail.price * Double(item.quantity)).asPrice)
                .font(AppTheme.Typography.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .lineLimit(1)
                .fixedSize()

            Button(action: onRemoveFromCart) {
                Image(systemName: "trash")
                    .font(.callout)
                    .foregroundColor(AppColors.error)
                    .minimumHitTarget()
            }
            .buttonStyle(PlainButtonStyle())
            .accessibilityLabel(L(\.cart_remove_item_a11y, item.cocktail.name))
            .accessibilityIdentifier("cart.removeButton")
        }
    }
}

struct QuantityControlView: View {
    /// Names the item in the stepper's VoiceOver labels ("Increase
    /// quantity of Margarita"), so rows in a list are distinguishable.
    let itemName: String
    let quantity: Int32
    let onIncrement: () -> Void
    let onDecrement: () -> Void
    @Environment(\.isDarkMode) var isDarkMode

    var body: some View {
        // The 44 pt hit targets already pad the 32 pt circles by 6 pt, so a
        // small gap keeps the row (stepper + line total + remove) on one
        // line at phone width
        HStack(spacing: AppTheme.Spacing.xs) {
            Button(action: onDecrement) {
                Image(systemName: "minus")
                    .font(.callout.weight(.bold))
                    // Fixed 32 pt circle: keep the glyph inside it at large
                    // text sizes
                    .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
                    .foregroundColor(.white)
                    .frame(width: 32, height: 32)
                    .background(isDarkMode ? AppColors.neutralSubtle(isDarkMode: true) : AppColors.textSecondary(isDarkMode: false))
                    .clipShape(Circle())
                    .minimumHitTarget()
            }
            .buttonStyle(PlainButtonStyle())
            .accessibilityLabel(L(\.cart_decrease_quantity_a11y, itemName))
            .accessibilityIdentifier("cart.decrementButton")

            Text("\(quantity)")
                .font(AppTheme.Typography.headline)
                .fontWeight(.bold)
                .frame(minWidth: 30)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .accessibilityLabel(L(\.cart_quantity_label, quantity))
                .accessibilityIdentifier("cart.quantityValue")
                // Focus stays on the +/- button, so speak the new quantity
                .onChange(of: quantity) { _, newValue in
                    AccessibilityNotification.Announcement(L(\.cart_quantity_label, newValue)).post()
                }

            Button(action: onIncrement) {
                Image(systemName: "plus")
                    .font(.callout.weight(.bold))
                    // Fixed 32 pt circle: keep the glyph inside it at large
                    // text sizes
                    .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
                    .foregroundColor(AppColors.onPrimary(isDarkMode: isDarkMode))
                    .frame(width: 32, height: 32)
                    .background(AppColors.primary(isDarkMode: isDarkMode))
                    .clipShape(Circle())
                    .minimumHitTarget()
            }
            .buttonStyle(PlainButtonStyle())
            .accessibilityLabel(L(\.cart_increase_quantity_a11y, itemName))
            .accessibilityIdentifier("cart.incrementButton")
        }
    }
}

#Preview {
    CartItemCard(
        item: CocktailCartItem(
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
