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
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

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
        Group {
            // At accessibility text sizes the image stacks above the text,
            // which then gets the full card width
            if dynamicTypeSize.isAccessibilitySize {
                VStack(alignment: .leading, spacing: 16) {
                    horizontalImage
                    horizontalContent
                }
            } else {
                HStack(spacing: 16) {
                    horizontalImage
                    horizontalContent
                }
            }
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
    private var horizontalImage: some View {
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
                        Text(L(\.list_out_of_stock))
                            .font(.caption)
                            .foregroundColor(.white)
                            .fontWeight(.medium)
                    )
                    // Read as part of the card summary
                    .accessibilityHidden(true)
            }
        }
        // Stacked, keep the out-of-stock overlay on the image instead of
        // stretching across the card
        .frame(
            width: dynamicTypeSize.isAccessibilitySize ? 120 : nil,
            height: dynamicTypeSize.isAccessibilitySize ? 120 : nil
        )
    }

    @ViewBuilder
    private var horizontalContent: some View {
        // Content Section
        VStack(alignment: .leading, spacing: 6) {
            // Cocktail Name - Larger font. VoiceOver reads the whole card
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
                .font(.subheadline.weight(.medium))
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                .lineLimit(dynamicTypeSize.isAccessibilitySize ? nil : 1)
                .fixedSize(horizontal: false, vertical: true)
                .accessibilityHidden(true)

            // Ingredients (first 2 or placeholder)
            Text(getIngredientsText())
                .font(.footnote.weight(.regular))
                .italic()
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                .lineLimit(2)
                .multilineTextAlignment(.leading)
                .accessibilityHidden(true)

            // Pushes the price row to the bottom beside the image; in the
            // stacked (accessibility size) layout there is nothing to align
            if dynamicTypeSize.isAccessibilitySize {
                Color.clear.frame(height: 2)
            } else {
                Spacer(minLength: 8)
            }

            // Price and Actions Row
            HStack(alignment: .center) {
                // Price - Larger and more prominent
                Text((cocktail.price ?? 12.99).asPrice)
                    .font(.title3.weight(.bold))
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                    .lineLimit(1)
                    .fixedSize()
                    .accessibilityHidden(true)

                Spacer()
                
                // Action Buttons - Larger and more spaced
                HStack(spacing: 12) {
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
                                .font(.title2)
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
                            Image(systemName: "cart.badge.plus")
                                .font(.title2)
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
        .padding(.vertical, 8)
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
                            Text(L(\.list_out_of_stock))
                                .font(.caption)
                                .foregroundColor(.white)
                                .fontWeight(.medium)
                        )
                        // Read as part of the card summary
                        .accessibilityHidden(true)
                }
            }
            
            // Content Section
            VStack(alignment: .leading, spacing: 8) {
                // Cocktail Name - Allow more lines (carries the card summary
                // for VoiceOver, see the horizontal layout)
                Text(cocktail.name)
                    .font(AppTheme.Typography.cardTitle)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .modifier(CardSummaryAccessibility(summary: accessibilitySummary, onTap: onCardTap))

                // Category Info (Alcoholic • Category)
                Text(subtitleText)
                    .font(.footnote.weight(.medium))
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .lineLimit(2)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .accessibilityHidden(true)

                // Ingredients (first 2 or placeholder)
                Text(getIngredientsText())
                    .font(AppTheme.Typography.cardCaption)
                    .italic()
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .accessibilityHidden(true)

                // Price and Actions Row
                HStack(alignment: .center) {
                    // Price - Prominent
                    Text((cocktail.price ?? 12.99).asPrice)
                        .font(.headline.weight(.bold))
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                        // Never break a price mid-number in the narrow grid
                        // column; shrink it instead
                        .lineLimit(1)
                        .minimumScaleFactor(0.6)
                        .layoutPriority(1)
                        .accessibilityHidden(true)

                    Spacer(minLength: 4)

                    // Action Buttons - Compact; the 44pt hit targets
                    // already space the icons apart
                    HStack(spacing: 0) {
                        // Favorite Button
                        if let onToggle = onFavoriteToggle {
                            Button(action: {
                                // Immediate tactile ack on tap (see horizontal layout).
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
                                Image(systemName: "cart.badge.plus")
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
    
    /// Name, category, ingredients, price — and out-of-stock — as one
    /// VoiceOver phrase (each part is shared, localized copy).
    private var accessibilitySummary: String {
        var parts = [cocktail.name, subtitleText]
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
        let alcoholic = cocktail.alcoholic ?? L(\.list_alcoholic_unknown)
        guard let category = cocktail.category else { return alcoholic }
        return L(\.list_alcoholic_category, alcoholic, category)
    }

    private func getIngredientsText() -> String {
        // Try to get first 2 ingredients if available
        if let ingredients = cocktail.ingredients as? [CocktailIngredient], !ingredients.isEmpty {
            let firstTwo = Array(ingredients.prefix(2))
            let ingredientNames = firstTwo.compactMap { $0.name }
            return ingredientNames.joined(separator: ", ")
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