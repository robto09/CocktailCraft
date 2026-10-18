import SwiftUI
import shared

/// Home's "Recently viewed" row, as Android's RecentlyViewedRow: a heading
/// and a horizontally scrolling row of compact cards (image + name), newest
/// first. Each card pushes the cocktail's detail page through the enclosing
/// stack's navigationDestination(for: String.self) and is one VoiceOver
/// button named after the cocktail.
struct RecentlyViewedRow: View {
    let cocktails: [Cocktail]
    /// How far the cards may scroll past the row's own bounds on each side —
    /// the parent's horizontal padding, so they run to the screen edges
    /// while the first card lines up with the heading.
    var horizontalBleed: CGFloat = 0

    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(L(\.home_recently_viewed))
                .font(.headline.weight(.bold))
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .accessibilityAddTraits(.isHeader)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(cocktails, id: \.id) { cocktail in
                        NavigationLink(value: cocktail.id) {
                            RecentlyViewedCard(cocktail: cocktail)
                        }
                        .buttonStyle(PlainButtonStyle())
                        .accessibilityElement(children: .ignore)
                        .accessibilityLabel(cocktail.name)
                        .accessibilityAddTraits(.isButton)
                        .accessibilityIdentifier("home.recentlyViewed.\(cocktail.id)")
                    }
                }
                .padding(.horizontal, horizontalBleed)
                // Room for the cards' shadows inside the scroll view
                .padding(.vertical, 4)
            }
            .padding(.horizontal, -horizontalBleed)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("home.recentlyViewed")
    }
}

/// 112 wide like Android's card: a square image above the name (two lines
/// reserved, so every card in the row has the same height).
private struct RecentlyViewedCard: View {
    let cocktail: Cocktail
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            CocktailImageView(
                imageUrl: cocktail.imageUrl,
                height: 96,
                cornerRadius: 8,
                width: 96
            )
            Text(cocktail.name)
                .font(.subheadline.weight(.semibold))
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .lineLimit(2, reservesSpace: true)
                .multilineTextAlignment(.leading)
        }
        .padding(8)
        .frame(width: 112, alignment: .leading)
        .background(AppColors.surface(isDarkMode: isDarkMode))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.12), radius: 6, x: 0, y: 3)
    }
}
