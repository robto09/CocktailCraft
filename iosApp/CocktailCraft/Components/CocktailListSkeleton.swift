import SwiftUI

/// Shimmering placeholder list matching the cocktail card layout, shown while
/// the first page loads.
struct CocktailListSkeleton: View {
    var rowCount: Int = 6
    @Environment(\.isDarkMode) private var isDarkMode

    private var surfaceColor: Color { AppColors.surface(isDarkMode: isDarkMode) }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(0..<rowCount, id: \.self) { _ in
                    skeletonRow
                }
            }
            .padding(16)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(L(\.loading))
        }
    }

    private var skeletonRow: some View {
        HStack(spacing: 16) {
            SkeletonBlock(width: 120, height: 120, cornerRadius: 12)

            VStack(alignment: .leading, spacing: 6) {
                // Title placeholder
                SkeletonBlock(height: 22)

                // Subtitle placeholder
                SkeletonBlock(width: 140, height: 18)

                // Ingredients placeholder
                SkeletonBlock(width: 120, height: 16)

                Spacer(minLength: 8)

                // Bottom row placeholder
                HStack {
                    SkeletonBlock(width: 70, height: 24)

                    Spacer()

                    HStack(spacing: 12) {
                        SkeletonBlock(width: 40, height: 40, cornerRadius: 20)
                        SkeletonBlock(width: 40, height: 40, cornerRadius: 20)
                    }
                }
            }
            .padding(.vertical, 8)
        }
        // Shimmer the blocks only, not the card behind them
        .shimmering()
        .padding(16)
        .background(surfaceColor)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.12), radius: 6, x: 0, y: 3)
    }
}
