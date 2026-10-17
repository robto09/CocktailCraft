import SwiftUI
import shared

/// The cocktail row used by the Home and Favorites lists, laid out like
/// Android's CocktailItem.
struct CocktailCard: View {
    let cocktail: Cocktail
    var isFavorite: Bool = false
    var onFavoriteToggle: (() -> Void)? = nil
    var onAddToCart: (() -> Void)? = nil
    var onCardTap: (() -> Void)? = nil
    
    // Inverse of CocktailDetailViewModelSKIE.canAddToCart() — the card and
    // the detail screen must agree on when Add to Cart is available.
    private var isOutOfStock: Bool {
        !cocktail.inStock || cocktail.stockCount <= 0
    }
    
    @Environment(\.isDarkMode) var isDarkMode
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        Group {
            // At accessibility text sizes the image stacks above the text,
            // which then gets the full card width
            if dynamicTypeSize.isAccessibilitySize {
                VStack(alignment: .leading, spacing: 12) {
                    horizontalImage
                    horizontalContent
                }
            } else {
                HStack(spacing: 12) {
                    horizontalImage
                    horizontalContent
                }
            }
        }
        // Same metrics as Android's CocktailItem: 12 padding and gap,
        // 12 corners, 100 image with 8 corners
        .padding(12)
        .background(AppColors.surface(isDarkMode: isDarkMode))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.12), radius: 6, x: 0, y: 3)
        .if(onCardTap != nil) { view in
            view.onTapGesture {
                onCardTap?()
            }
        }
    }
    
    @ViewBuilder
    private var horizontalImage: some View {
        // Cocktail Image - Larger size (Kingfisher-backed: disk+memory
        // cache and retry, instead of AsyncImage refetching on recycle)
        ZStack(alignment: .center) {
            CocktailImageView(
                imageUrl: cocktail.imageUrl,
                height: 100,
                cornerRadius: 8,
                width: 100
            )

            // Stock overlay for out of stock items
            if isOutOfStock {
                Rectangle()
                    .fill(Color.black.opacity(0.6))
                    .cornerRadius(8)
                    .overlay(
                        // Shrinks rather than truncating on the 100 pt image
                        // at large text sizes
                        Text(L(\.list_out_of_stock))
                            .font(.caption)
                            .foregroundColor(.white)
                            .fontWeight(.medium)
                            .multilineTextAlignment(.center)
                            .minimumScaleFactor(0.5)
                            .padding(4)
                    )
                    // Read as part of the card summary
                    .accessibilityHidden(true)
            }
        }
        // Stacked, keep the out-of-stock overlay on the image instead of
        // stretching across the card
        .frame(
            width: dynamicTypeSize.isAccessibilitySize ? 100 : nil,
            height: dynamicTypeSize.isAccessibilitySize ? 100 : nil
        )
    }

    @ViewBuilder
    private var horizontalContent: some View {
        // Content Section
        VStack(alignment: .leading, spacing: 4) {
            // Cocktail Name. VoiceOver reads the whole card
            // summary here, in one stop (the lines below are hidden);
            // the heart and cart buttons stay separate elements.
            Text(cocktail.name)
                .font(.headline.weight(.bold))
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .lineLimit(2)
                .multilineTextAlignment(.leading)
                .modifier(CardSummaryAccessibility(summary: accessibilitySummary, onTap: onCardTap))

            // Category Info (Alcoholic • Category)
            Text(subtitleText)
                .font(.subheadline)
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                .lineLimit(dynamicTypeSize.isAccessibilitySize ? nil : 1)
                .fixedSize(horizontal: false, vertical: true)
                .accessibilityHidden(true)

            // Ingredients (first 2 or placeholder)
            Text(getIngredientsText())
                .font(.caption)
                .italic()
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                .lineLimit(dynamicTypeSize.isAccessibilitySize ? 2 : 1)
                .multilineTextAlignment(.leading)
                .accessibilityHidden(true)

            // Pushes the price row to the bottom beside the image; in the
            // stacked (accessibility size) layout there is nothing to align
            if dynamicTypeSize.isAccessibilitySize {
                Color.clear.frame(height: 2)
            } else {
                Spacer(minLength: 4)
            }

            // Price and Actions Row
            HStack(alignment: .center) {
                Text((cocktail.price ?? 12.99).asPrice)
                    .font(.headline.weight(.bold))
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                    .lineLimit(1)
                    .fixedSize()
                    .accessibilityHidden(true)

                Spacer()
                
                // Action Buttons: 44pt hit targets 4pt apart put the icons
                // 48 apart, as Android's adjacent 48dp IconButtons do
                HStack(spacing: 4) {
                    // Favorite Button
                    if let onToggle = onFavoriteToggle {
                        Button(action: {
                            // Immediate tactile ack on tap, before any async
                            // state round trip — parity with Android's
                            // performToggleFavorite haptic.
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            onToggle()
                        }) {
                            Image(systemName: isFavorite ? "heart.fill" : "heart")
                                .font(.title3)
                                .foregroundColor(isFavorite ? AppColors.secondary(isDarkMode: isDarkMode) : AppColors.gray)
                                .contentTransition(.symbolEffect(.replace))
                                .animation(.easeInOut(duration: 0.2), value: isFavorite)
                                .minimumHitTarget()
                        }
                        .buttonStyle(.borderless)
                        // One stable label; the on/off state is the trait
                        .accessibilityLabel(L(\.favorite_toggle_a11y))
                        .accessibilityAddTraits(isFavorite ? .isSelected : [])
                        .accessibilityIdentifier("card.favoriteButton")
                    }

                    // Add to Cart Button
                    if let onAddToCart = onAddToCart {
                        Button(action: onAddToCart) {
                            // Plain filled cart, Android's icon
                            Image(systemName: "cart.fill")
                                .font(.title3)
                                .foregroundColor(isOutOfStock ? AppColors.gray : AppColors.primary(isDarkMode: isDarkMode))
                                .minimumHitTarget()
                        }
                        .disabled(isOutOfStock)
                        .buttonStyle(.borderless)
                        .accessibilityLabel(L(\.add_to_cart_item_a11y, cocktail.name))
                        .accessibilityIdentifier("card.addToCartButton")
                    }
                }
            }
        }
    }

    /// Name, category, ingredients, price — and out-of-stock — as one
    /// VoiceOver phrase (each part is shared, localized copy).
    private var accessibilitySummary: String {
        var parts = [cocktail.name]
        if !subtitleText.isEmpty {
            parts.append(subtitleText)
        }
        parts.append(getIngredientsText())
        parts.append((cocktail.price ?? 12.99).asPrice)
        if isOutOfStock {
            parts.append(L(\.list_out_of_stock))
        }
        return parts.joined(separator: ", ")
    }

    /// "Alcoholic • Shot" from the cocktail's own data, matching Android's
    /// list item; list endpoints leave `alcoholic` unknown until enriched.
    private var subtitleText: String {
        // An alcoholic value the list endpoint didn't supply is left out
        // rather than shown as "Unknown" (same as Android)
        switch (cocktail.knownAlcoholic, cocktail.category) {
        case let (alcoholic?, category?): return L(\.list_alcoholic_category, alcoholic, category)
        case let (alcoholic?, nil): return alcoholic
        case let (nil, category?): return category
        case (nil, nil): return ""
        }
    }

    private func getIngredientsText() -> String {
        // Try to get first 2 ingredients if available
        if let ingredients = cocktail.ingredients as? [CocktailIngredient], !ingredients.isEmpty {
            let firstTwo = Array(ingredients.prefix(2))
            let ingredientNames = firstTwo.compactMap { $0.name }
            // "..." marks that there are more, as on Android
            let more = ingredients.count > 2 ? "..." : ""
            return ingredientNames.joined(separator: ", ") + more
        }
        return L(\.list_premium_ingredients)
    }
}

// MARK: - Card summary for VoiceOver

/// Gives the card's title the whole card summary as its label; when the
/// card itself is tappable, the title also becomes the card's button.
private struct CardSummaryAccessibility: ViewModifier {
    let summary: String
    let onTap: (() -> Void)?

    func body(content: Content) -> some View {
        if let onTap {
            content
                .accessibilityLabel(summary)
                .accessibilityAddTraits(.isButton)
                .accessibilityAction { onTap() }
        } else {
            content.accessibilityLabel(summary)
        }
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