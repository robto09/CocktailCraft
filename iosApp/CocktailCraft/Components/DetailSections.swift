import SwiftUI
// @preconcurrency suppresses Sendable/actor-isolation diagnostics for the
// whole KMP framework import. Under the project's current Swift 5.10 /
// minimal-concurrency build it is close to a no-op; it becomes load-bearing
// (and must be re-audited per-type) the moment SWIFT_STRICT_CONCURRENCY is
// set to targeted/complete or the target moves to Swift 6 language mode.
// Tracked as tech debt (IO-12) — don't copy this pattern to new files.
@preconcurrency import shared

/// Section subviews for CocktailDetailView, split out of the view file so it
/// stays focused on state wiring and top-level branching — mirroring
/// Android's detail-screen section files.
extension CocktailDetailView {

    // MARK: - Main Info

    func mainInfoCard(_ cocktail: Cocktail) -> some View {
        // Laid out like Android's DetailMainInfoCard: name, alcoholic/category
        // line, price, preparation. Only API data (no rating or stock, which the
        // API doesn't have); adding to cart happens from the Home and Favorites
        // cards. Its rounded top overlaps the header image; see
        // CocktailDetailView.
        VStack(alignment: .leading, spacing: 0) {
            Text(cocktail.name)
                .font(.title.weight(.bold))
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .accessibilityAddTraits(.isHeader)

            let subtitle = [cocktail.knownAlcoholic, cocktail.category].compactMap { $0 }.joined(separator: " • ")
            if !subtitle.isEmpty {
                Text(subtitle)
                    .font(.callout.weight(.medium))
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .padding(.top, AppTheme.Spacing.xs)
            }

            Text(viewModel.formatPrice())
                .font(.title2.weight(.bold))
                .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                // A bare "$7.46" reads without context (and fails the
                // audit's human-readable label check)
                .accessibilityLabel(L(\.detail_price_a11y, viewModel.formatPrice()))
                .padding(.top, AppTheme.Spacing.sm)

            if let instructions = cocktail.instructions, !instructions.isEmpty {
                Text(L(\.how_to_prepare))
                    .font(.headline.weight(.bold))
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .accessibilityAddTraits(.isHeader)
                    .padding(.top, AppTheme.Spacing.lg)

                Text(instructions)
                    .font(.callout)
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .lineSpacing(4)
                    .padding(.top, AppTheme.Spacing.sm)
            }
        }
        .padding(AppTheme.Spacing.lg)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            UnevenRoundedRectangle(
                topLeadingRadius: 24,
                bottomLeadingRadius: AppTheme.CornerRadius.card,
                bottomTrailingRadius: AppTheme.CornerRadius.card,
                topTrailingRadius: 24
            )
            .fill(AppColors.surface(isDarkMode: isDarkMode))
            .shadow(
                color: AppTheme.Shadow.card.color,
                radius: AppTheme.Shadow.card.radius,
                x: AppTheme.Shadow.card.x,
                y: AppTheme.Shadow.card.y
            )
        )
        .padding(.horizontal)
    }

    // MARK: - Section card

    /// The card every detail section uses, as Android's DetailInfoCard: a bold
    /// title, then the content, with 16pt padding.
    func sectionCard<Content: View>(title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
            Text(title)
                .font(.headline.weight(.bold))
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .accessibilityAddTraits(.isHeader)

            content()
        }
        .padding(AppTheme.Spacing.lg)
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
        .padding(.horizontal)
    }

    // MARK: - Ingredients

    func ingredientsSection(_ cocktail: Cocktail) -> some View {
        sectionCard(title: L(\.ingredients)) {
            // API order, "1 oz Vodka" (shared format), orange dot bullets as
            // on Android
            VStack(alignment: .leading, spacing: 0) {
                ForEach(Array(cocktail.ingredients.enumerated()), id: \.offset) { _, ingredient in
                    HStack(spacing: AppTheme.Spacing.md) {
                        Circle()
                            .fill(AppColors.primary(isDarkMode: isDarkMode))
                            .frame(width: 8, height: 8)
                            .accessibilityHidden(true)
                        Text(ingredient.displayText)
                            .font(.callout)
                            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    }
                    .padding(.vertical, 6)
                }
            }
        }
    }

    // MARK: - Details chips

    func chipsSection(_ cocktail: Cocktail) -> some View {
        // Same chips as Android's DetailChipsSection, each only when the API
        // has the value
        let chips: [String] = [
            cocktail.category.map { L(\.category_label, $0) },
            cocktail.glass.map { L(\.glass_label, $0) },
            cocktail.knownAlcoholic,
            cocktail.iba.map { L(\.detail_iba_label, $0) }
        ].compactMap { $0 }

        return sectionCard(title: L(\.details)) {
            // Wraps onto new lines instead of scrolling, so no chip is cut off
            WrappingHStack(spacing: AppTheme.Spacing.sm) {
                ForEach(chips, id: \.self) { chip in
                    Text(chip)
                        .font(.subheadline.weight(.medium))
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        .padding(.horizontal, AppTheme.Spacing.lg)
                        .frame(minHeight: 32)
                        .background(
                            RoundedRectangle(cornerRadius: AppTheme.CornerRadius.sm)
                                .fill(AppColors.chipFill(isDarkMode: isDarkMode))
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: AppTheme.CornerRadius.sm)
                                .stroke(AppColors.neutralSubtle(isDarkMode: isDarkMode), lineWidth: 1)
                        )
                }
            }
        }
    }

    // MARK: - Related Cocktails

    var relatedCocktailsSection: some View {
        sectionCard(title: L(\.detail_you_might_also_like)) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: AppTheme.Spacing.md) {
                    ForEach(viewModel.state.relatedCocktails, id: \.id) { related in
                        // Pushes another detail screen through the enclosing
                        // stack's navigationDestination(for: String.self)
                        NavigationLink(value: related.id) {
                            RelatedCocktailCard(cocktail: related)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
                // Room for the cards' shadows inside the scroll view
                .padding(.vertical, AppTheme.Spacing.xs)
            }
        }
    }

    /// Shimmering stand-ins for the related cocktail cards while they load,
    /// as Android's RecommendationsLoadingRow.
    var relatedCocktailsLoadingSection: some View {
        sectionCard(title: L(\.detail_you_might_also_like)) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: AppTheme.Spacing.md) {
                    ForEach(0..<3, id: \.self) { _ in
                        SkeletonBlock(width: 140, height: 200, cornerRadius: AppTheme.CornerRadius.card)
                    }
                }
                .shimmering()
                .padding(.vertical, AppTheme.Spacing.xs)
            }
            .scrollDisabled(true)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(L(\.detail_loading_recommendations))
        }
    }

    // MARK: - Reviews

    /// Mirrors Android's DetailReviewsSection: count header with a
    /// write-review action, an empty-state prompt, or the newest three
    /// reviews separated by dividers.
    var reviewsSection: some View {
        let latestReviews = Array(reviewViewModel.reviews.prefix(Self.visibleReviewCount))

        return VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
            HStack {
                Text(L(\.reviews_title, reviewViewModel.reviewCount))
                    .font(.headline.weight(.bold))
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .accessibilityAddTraits(.isHeader)
                    .accessibilityIdentifier("detail.reviewsHeader")

                Spacer()

                Button(action: { showingWriteReview = true }) {
                    Label(L(\.write_review), systemImage: "square.and.pencil")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                        .frame(minHeight: 44)
                        .contentShape(Rectangle())
                }
                .accessibilityIdentifier("detail.writeReviewButton")
            }

            if latestReviews.isEmpty {
                Text(L(\.detail_be_first_review))
                    .font(.body)
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, AppTheme.Spacing.xxl)
                    .accessibilityIdentifier("detail.reviewsEmptyState")
            } else {
                ForEach(Array(latestReviews.enumerated()), id: \.element.id) { index, review in
                    ReviewRow(review: review)

                    if index < latestReviews.count - 1 {
                        Divider()
                            .padding(.vertical, AppTheme.Spacing.xs)
                    }
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
        .padding(.horizontal)
    }

    /// Android shows the newest three reviews on the detail screen.
    static var visibleReviewCount: Int { 3 }
}

// MARK: - Related Cocktail Card

/// As Android's RecommendationCard: 140 wide, a square image flush with the
/// card's top, then the name and price.
struct RelatedCocktailCard: View {
    let cocktail: Cocktail
    @Environment(\.isDarkMode) var isDarkMode

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            CocktailImageView(
                imageUrl: cocktail.imageUrl,
                height: 140,
                cornerRadius: 0,
                width: 140
            )

            VStack(alignment: .leading, spacing: AppTheme.Spacing.xs) {
                Text(cocktail.name)
                    .font(.subheadline.weight(.bold))
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .lineLimit(1)

                Text(cocktail.price.asPrice)
                    .font(.subheadline.weight(.bold))
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
            }
            .padding(AppTheme.Spacing.sm)
        }
        .frame(width: 140, alignment: .leading)
        .background(AppColors.surface(isDarkMode: isDarkMode))
        .clipShape(RoundedRectangle(cornerRadius: AppTheme.CornerRadius.card))
        .shadow(
            color: AppTheme.Shadow.card.color,
            radius: AppTheme.Shadow.card.radius,
            x: AppTheme.Shadow.card.x,
            y: AppTheme.Shadow.card.y
        )
    }
}

// MARK: - Wrapping layout

/// Lays its children out left to right, starting a new row when the next one
/// doesn't fit (the counterpart of Compose's FlowRow).
struct WrappingHStack: Layout {
    var spacing: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0, y: CGFloat = 0, rowHeight: CGFloat = 0, widest: CGFloat = 0
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x > 0 && x + size.width > maxWidth {
                y += rowHeight + spacing
                x = 0
                rowHeight = 0
            }
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
            widest = max(widest, x - spacing)
        }
        return CGSize(width: min(widest, maxWidth), height: y + rowHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX, y = bounds.minY, rowHeight: CGFloat = 0
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x > bounds.minX && x + size.width > bounds.maxX {
                y += rowHeight + spacing
                x = bounds.minX
                rowHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}
