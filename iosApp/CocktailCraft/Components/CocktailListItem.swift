import SwiftUI
import shared

struct CocktailListItem: View {
    let cocktail: Cocktail
    let isFavorite: Bool
    let onToggleFavorite: () -> Void
    let onAddToCart: (() -> Void)?
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            CocktailCard(
                cocktail: cocktail,
                isFavorite: isFavorite,
                onFavoriteToggle: onToggleFavorite,
                onAddToCart: onAddToCart
            )
        }
        .buttonStyle(.plain)
    }
}

struct AnimatedCocktailList: View {
    let cocktails: [Cocktail]
    let favorites: Set<String>
    let onToggleFavorite: (Cocktail) -> Void
    let onAddToCart: ((Cocktail) -> Void)?
    let onCocktailTap: (Cocktail) -> Void
    
    var body: some View {
        LazyVStack(spacing: AppTheme.Spacing.cardSpacing) {
            ForEach(Array(cocktails.enumerated()), id: \.element.id) { index, cocktail in
                CocktailListItem(
                    cocktail: cocktail,
                    isFavorite: favorites.contains(cocktail.id),
                    onToggleFavorite: { onToggleFavorite(cocktail) },
                    onAddToCart: onAddToCart != nil ? { onAddToCart!(cocktail) } : nil,
                    onTap: { onCocktailTap(cocktail) }
                )
                .transition(.asymmetric(
                    insertion: .move(edge: .leading).combined(with: .opacity),
                    removal: .move(edge: .trailing).combined(with: .opacity)
                ))
                .animation(
                    AppTheme.Animation.standard.delay(Double(index) * 0.1),
                    value: cocktails.count
                )
            }
        }
        .padding(AppTheme.Spacing.lg)
    }
}

#Preview {
    ScrollView {
        AnimatedCocktailList(
            cocktails: [],
            favorites: [],
            onToggleFavorite: { _ in },
            onAddToCart: { _ in },
            onCocktailTap: { _ in }
        )
    }
    .background(AppColors.background)
}