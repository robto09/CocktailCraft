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
        VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
            Text(cocktail.name)
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            HStack(spacing: AppTheme.Spacing.sm) {
                if let category = cocktail.category {
                    Text(category)
                        .font(.subheadline)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                }

                Spacer()

                Image(systemName: "star.fill")
                    .font(.caption)
                    .foregroundColor(.yellow)
                Text(viewModel.formatRating())
                    .font(.subheadline)
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
            }

            HStack {
                Text(viewModel.formatPrice())
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))

                Spacer()

                // Stock badge colored by the ViewModel's shared stock rule
                Text(viewModel.getStockStatus())
                    .font(.caption)
                    .fontWeight(.medium)
                    .padding(.horizontal, AppTheme.Spacing.md)
                    .padding(.vertical, AppTheme.Spacing.xs)
                    .background(viewModel.getStockStatusColor().opacity(0.15))
                    .foregroundColor(viewModel.getStockStatusColor())
                    .cornerRadius(AppTheme.CornerRadius.chip)
            }

            cartControls

            if let instructions = cocktail.instructions, !instructions.isEmpty {
                VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
                    Text(L(\.how_to_prepare_ios))
                        .font(.headline)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                    Text(instructions)
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                }
                .padding(.top, AppTheme.Spacing.sm)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
        .padding(.horizontal)
    }

    @ViewBuilder
    var cartControls: some View {
        if viewModel.state.isInCart {
            // Already in cart: quantity stepper; minus at 1 removes the line
            HStack(spacing: AppTheme.Spacing.lg) {
                Button(action: {
                    runCartAction {
                        if viewModel.state.cartQuantity <= 1 {
                            await viewModel.removeFromCart()
                        } else {
                            await viewModel.updateCartQuantity(Int(viewModel.state.cartQuantity) - 1)
                        }
                    }
                }) {
                    Image(systemName: viewModel.state.cartQuantity <= 1 ? "trash" : "minus.circle.fill")
                        .font(.title2)
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                        .minimumHitTarget()
                }
                .disabled(cartActionInFlight)
                .accessibilityLabel(viewModel.state.cartQuantity <= 1 ? L(\.remove_from_cart_ios) : L(\.cart_decrease_quantity_ios))
                .accessibilityIdentifier("detail.decrementButton")

                Text(L(\.detail_in_cart, viewModel.state.cartQuantity))
                    .font(.headline)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    .frame(maxWidth: .infinity)
                    .accessibilityIdentifier("detail.cartQuantityValue")

                Button(action: {
                    runCartAction {
                        await viewModel.updateCartQuantity(Int(viewModel.state.cartQuantity) + 1)
                    }
                }) {
                    Image(systemName: "plus.circle.fill")
                        .font(.title2)
                        .foregroundColor(canIncrementQuantity
                            ? AppColors.primary(isDarkMode: isDarkMode)
                            : AppColors.gray)
                        .minimumHitTarget()
                }
                .disabled(!canIncrementQuantity || cartActionInFlight)
                .accessibilityLabel(L(\.cart_increase_quantity_ios))
                .accessibilityIdentifier("detail.incrementButton")
            }
            .padding(.vertical, AppTheme.Spacing.sm)
        } else {
            PrimaryButton(
                title: viewModel.canAddToCart()
                    ? L(\.add_to_cart)
                    : L(\.detail_out_of_stock),
                icon: "cart.badge.plus",
                isDisabled: !viewModel.canAddToCart(),
                action: {
                    runCartAction { await viewModel.addToCart(quantity: 1) }
                }
            )
            .accessibilityLabel(L(\.add_to_cart_ios))
            .accessibilityIdentifier("detail.addToCartButton")
        }
    }

    var canIncrementQuantity: Bool {
        guard let cocktail = viewModel.state.cocktail else { return false }
        return viewModel.state.cartQuantity < cocktail.stockCount
    }

    /// One cart mutation at a time: quantities are computed from the
    /// async-mirrored state, so overlapping mutations would read stale values.
    func runCartAction(_ operation: @escaping () async -> Void) {
        guard !cartActionInFlight else { return }
        cartActionInFlight = true
        Task {
            await operation()
            cartActionInFlight = false
        }
    }

    // MARK: - Ingredients

    func ingredientsSection(_ cocktail: Cocktail) -> some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
            Text(L(\.ingredients))
                .font(.headline)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            let types = viewModel.getAllIngredientTypes()
            if types.isEmpty {
                // No grouping available: flat list straight off the model
                ForEach(Array(cocktail.ingredients.enumerated()), id: \.offset) { _, ingredient in
                    ingredientRow(name: ingredient.name, measure: ingredient.measure)
                }
            } else {
                ForEach(types, id: \.self) { type in
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.xs) {
                        Text(type)
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))

                        ForEach(viewModel.getIngredientsByType(type), id: \.self) { entry in
                            Text(L(\.detail_ingredient_bullet, entry))
                                .font(.body)
                                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        }
                    }
                    .padding(.bottom, AppTheme.Spacing.xs)
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
        .padding(.horizontal)
    }

    func ingredientRow(name: String, measure: String?) -> some View {
        Text(L(\.detail_ingredient_measure_bullet, measure.flatMap { $0.isEmpty ? nil : "\($0) " } ?? "", name))
            .font(.body)
            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
    }

    // MARK: - Nutrition

    func nutritionSection(_ nutrition: NutritionFacts) -> some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
            Text(L(\.detail_estimated_nutrition))
                .font(.headline)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            HStack(spacing: AppTheme.Spacing.sm) {
                nutritionStat(value: "\(nutrition.calories)", unit: L(\.detail_nutrition_unit_kcal), label: L(\.detail_nutrition_calories))
                nutritionStat(value: "\(nutrition.alcoholGrams)", unit: L(\.detail_nutrition_unit_grams), label: L(\.detail_nutrition_alcohol))
                nutritionStat(value: "\(nutrition.carbsGrams)", unit: L(\.detail_nutrition_unit_grams), label: L(\.detail_nutrition_carbs))
                nutritionStat(value: "\(nutrition.sugarGrams)", unit: L(\.detail_nutrition_unit_grams), label: L(\.detail_nutrition_sugar))
            }

            Text(L(\.detail_nutrition_disclaimer))
                .font(.caption2)
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
        .padding(.horizontal)
    }

    func nutritionStat(value: String, unit: String, label: String) -> some View {
        VStack(spacing: AppTheme.Spacing.xs) {
            Text(value)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
            Text(unit)
                .font(.caption2)
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
            Text(label)
                .font(.caption)
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, AppTheme.Spacing.sm)
        .background(AppColors.primary(isDarkMode: isDarkMode).opacity(0.08))
        .cornerRadius(AppTheme.CornerRadius.sm)
    }

    // MARK: - Chips

    func chipsSection(_ cocktail: Cocktail) -> some View {
        // Attribute chips mirroring Android's DetailChipsSection
        let chips: [(icon: String, text: String)] = [
            cocktail.alcoholic.map { (icon: "wineglass", text: $0) },
            cocktail.glass.map { (icon: "cup.and.saucer", text: $0) },
            cocktail.iba.map { (icon: "rosette", text: L(\.detail_iba_label, $0)) }
        ].compactMap { $0 }

        return ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: AppTheme.Spacing.sm) {
                ForEach(chips, id: \.text) { chip in
                    HStack(spacing: AppTheme.Spacing.xs) {
                        Image(systemName: chip.icon)
                            .font(.caption)
                        Text(chip.text)
                            .font(.subheadline)
                    }
                    .chipStyle(isSelected: false)
                }
            }
            .padding(.horizontal)
        }
    }

    // MARK: - Related Cocktails

    var relatedCocktailsSection: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
            Text(L(\.detail_you_might_also_like_ios))
                .font(.headline)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .padding(.horizontal)

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
                .padding(.horizontal)
            }
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
                    .font(.headline)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
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

struct RelatedCocktailCard: View {
    let cocktail: Cocktail
    @Environment(\.isDarkMode) var isDarkMode

    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
            CocktailImageView(
                imageUrl: cocktail.imageUrl,
                height: 100,
                cornerRadius: AppTheme.CornerRadius.sm
            )
            .frame(width: 140)

            Text(cocktail.name)
                .font(.subheadline)
                .fontWeight(.medium)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .lineLimit(2)
                .multilineTextAlignment(.leading)

            Text(cocktail.price.asPrice)
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
        }
        .frame(width: 140, alignment: .leading)
        .padding(AppTheme.Spacing.sm)
        .cardStyle()
    }
}
