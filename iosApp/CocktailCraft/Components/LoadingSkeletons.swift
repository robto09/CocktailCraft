import SwiftUI

/// Shimmering placeholder for the cocktail detail screen while the cocktail
/// loads, laid out like Android's CocktailDetailShimmer: header image, name,
/// rating, attribute chips, description, then the ingredients list.
struct CocktailDetailSkeleton: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                // Header image, full width like CocktailImageView's hero
                SkeletonBlock(height: 250, cornerRadius: 0)

                VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
                    // Name
                    SkeletonBlock(width: 240, height: 26)

                    // Rating
                    HStack(spacing: AppTheme.Spacing.sm) {
                        SkeletonBlock(width: 120, height: 20)
                        SkeletonBlock(width: 40, height: 20)
                    }

                    // Attribute chips
                    HStack(spacing: AppTheme.Spacing.sm) {
                        SkeletonBlock(width: 100, height: 32, cornerRadius: 16)
                        SkeletonBlock(width: 100, height: 32, cornerRadius: 16)
                    }
                    .padding(.top, AppTheme.Spacing.sm)

                    // Description
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
                        SkeletonBlock(height: 16)
                        SkeletonBlock(height: 16)
                        SkeletonBlock(width: 200, height: 16)
                    }
                    .padding(.top, AppTheme.Spacing.lg)

                    // Ingredients title and list
                    SkeletonBlock(width: 120, height: 20)
                        .padding(.top, AppTheme.Spacing.lg)
                    ForEach(0..<4, id: \.self) { _ in
                        SkeletonBlock(width: 240, height: 16)
                    }
                }
                .padding(AppTheme.Spacing.lg)
            }
            .shimmering()
        }
        .scrollDisabled(true)
        // The placeholders mean nothing to VoiceOver: read the loading message
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(L(\.detail_loading))
    }
}

/// Shimmering placeholder for the Orders tab while orders load: cards laid
/// out like OrderListView's order cards (number and date, line items, a
/// divider, the total, then the status).
struct OrderListSkeleton: View {
    var cardCount: Int = 3

    var body: some View {
        ScrollView {
            LazyVStack(spacing: AppTheme.Spacing.lg) {
                ForEach(0..<cardCount, id: \.self) { _ in
                    orderCard
                }
            }
            .padding(AppTheme.Spacing.lg)
        }
        .scrollDisabled(true)
        // The placeholders mean nothing to VoiceOver: read the loading message
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(L(\.order_list_loading))
    }

    private var orderCard: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Order number and date
            HStack {
                SkeletonBlock(width: 110, height: 18)
                Spacer()
                SkeletonBlock(width: 80, height: 16)
            }
            .padding(.bottom, AppTheme.Spacing.md)

            // Line items
            ForEach(0..<2, id: \.self) { _ in
                HStack {
                    SkeletonBlock(width: 170, height: 16)
                    Spacer()
                    SkeletonBlock(width: 50, height: 16)
                }
                .padding(.vertical, AppTheme.Spacing.xs)
            }

            SkeletonBlock(height: 1, cornerRadius: 0)
                .padding(.vertical, AppTheme.Spacing.md)

            // Total
            HStack {
                SkeletonBlock(width: 50, height: 18)
                Spacer()
                SkeletonBlock(width: 60, height: 18)
            }

            // Status
            HStack(spacing: AppTheme.Spacing.sm) {
                SkeletonBlock(width: 10, height: 10, cornerRadius: 5)
                SkeletonBlock(width: 70, height: 16)
            }
            .padding(.top, AppTheme.Spacing.md)
        }
        // Shimmer the blocks only, not the card behind them
        .shimmering()
        .padding(AppTheme.Spacing.lg)
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
    }
}
