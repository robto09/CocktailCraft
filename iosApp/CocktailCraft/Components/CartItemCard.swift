import SwiftUI
import shared

/// Laid out like Android's CartItemCard: image on the left; name and
/// favorite heart, unit price, alcoholic label, then one row with the
/// stepper on the left and the line total and remove button on the right.
struct CartItemCard: View {
    let item: CocktailCartItem
    var isFavorite: Bool = false
    let onIncrementQuantity: () -> Void
    let onDecrementQuantity: () -> Void
    let onRemoveFromCart: () -> Void
    let onToggleFavorite: (() -> Void)?
    @Environment(\.isDarkMode) var isDarkMode
    @Environment(\.appearance) var appearance

    var body: some View {
        HStack(alignment: .center, spacing: AppTheme.Spacing.md) {
            CocktailImageView(
                imageUrl: item.cocktail.imageUrl,
                height: 80,
                cornerRadius: AppTheme.CornerRadius.sm
            )
            .frame(width: 80, height: 80)

            VStack(alignment: .leading, spacing: 0) {
                // Name and favorite
                HStack(spacing: AppTheme.Spacing.xs) {
                    Text(item.cocktail.name)
                        .font(.headline.weight(.bold))
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        .lineLimit(1)

                    Spacer(minLength: 0)

                    if let onToggleFavorite = onToggleFavorite {
                        Button(action: onToggleFavorite) {
                            Image(systemName: isFavorite ? "heart.fill" : "heart")
                                .font(.title3)
                                .foregroundColor(isFavorite ? AppColors.secondary(isDarkMode: isDarkMode) : AppColors.gray)
                                .contentTransition(.symbolEffect(.replace))
                                .minimumHitTarget()
                        }
                        .buttonStyle(PlainButtonStyle())
                        .accessibilityLabel(L(\.favorite_toggle_a11y))
                        .accessibilityAddTraits(isFavorite ? .isSelected : [])
                        .accessibilityIdentifier("cart.favoriteButton")
                    }
                }

                // Unit price
                Text(item.cocktail.price.asPrice)
                    .font(.subheadline.weight(.bold))
                    .foregroundColor(AppColors.primary(appearance))

                if let alcoholic = item.cocktail.knownAlcoholic {
                    Text(alcoholic)
                        .font(.caption)
                        .foregroundColor(AppColors.textSecondary(appearance))
                        .padding(.top, AppTheme.Spacing.xs)
                }

                // Stepper and line total on one row when it fits; otherwise
                // (large Dynamic Type) the total and remove button wrap below
                // the stepper instead of breaking across lines
                ViewThatFits(in: .horizontal) {
                    HStack(spacing: 0) {
                        quantityControls
                        Spacer(minLength: AppTheme.Spacing.xs)
                        totalAndRemove
                    }

                    VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
                        quantityControls
                        totalAndRemove
                    }
                }
                .padding(.top, AppTheme.Spacing.sm)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(AppTheme.Spacing.md)
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
        HStack(spacing: AppTheme.Spacing.xs) {
            // 16 pt like Android's line total, so stepper, total and
            // remove still fit one row with longer prices ("US$13.65")
            Text((item.cocktail.price * Double(item.quantity)).asPrice)
                .font(.callout.weight(.bold))
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .lineLimit(1)
                .fixedSize()

            Button(action: onRemoveFromCart) {
                Image(systemName: "trash.fill")
                    .font(.body)
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
    @Environment(\.appearance) var appearance

    var body: some View {
        // The 44 pt hit targets pad the 36 pt circles by 4 pt; with the gap
        // and the count's min width it sits about as far from each circle as
        // on Android
        HStack(spacing: AppTheme.Spacing.xs) {
            Button(action: onDecrement) {
                Image(systemName: "minus")
                    .font(.callout.weight(.bold))
                    // Fixed 36 pt circle (Android's size): keep the glyph
                    // inside it at large text sizes
                    .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .frame(width: 36, height: 36)
                    .background(AppColors.neutralSubtle(isDarkMode: isDarkMode))
                    .clipShape(Circle())
                    .minimumHitTarget()
            }
            .buttonStyle(PlainButtonStyle())
            .accessibilityLabel(L(\.cart_decrease_quantity_a11y, itemName))
            .accessibilityIdentifier("cart.decrementButton")

            Text("\(quantity)")
                .font(AppTheme.Typography.headline)
                .fontWeight(.bold)
                .frame(minWidth: 28)
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
                    // Fixed 36 pt circle (Android's size): keep the glyph
                    // inside it at large text sizes
                    .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
                    .foregroundColor(AppColors.onPrimary(isDarkMode: isDarkMode))
                    .frame(width: 36, height: 36)
                    .background(AppColors.primary(appearance))
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
