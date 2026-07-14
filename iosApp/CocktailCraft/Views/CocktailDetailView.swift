import SwiftUI
// @preconcurrency suppresses Sendable/actor-isolation diagnostics for the
// whole KMP framework import. Under the project's current Swift 5.10 /
// minimal-concurrency build it is close to a no-op; it becomes load-bearing
// (and must be re-audited per-type) the moment SWIFT_STRICT_CONCURRENCY is
// set to targeted/complete or the target moves to Swift 6 language mode.
// Tracked as tech debt (IO-12) — don't copy this pattern to new files.
@preconcurrency import shared

/// Full detail screen driven by CocktailDetailViewModelSKIE. Section order
/// mirrors Android's CocktailDetailScreen for design parity: header image,
/// main info (name/rating/price/stock/cart controls/instructions),
/// ingredients grouped by type, attribute chips, then related cocktails.
struct CocktailDetailView: View {
    let cocktailId: String
    @State private var viewModel = CocktailDetailViewModelSKIE()
    @Environment(\.isDarkMode) var isDarkMode

    // Distinguishes "load not started yet" from "loaded but not found"
    @State private var hasLoaded = false
    // Serializes cart mutations: the stepper reads the async-mirrored
    // cartQuantity, so a second tap before the first round-trip lands would
    // compute from stale state (lost or duplicated increments).
    @State private var cartActionInFlight = false

    var body: some View {
        VStack {
            // Cocktail branch comes first: the shared loadCocktail keeps
            // isLoading true through the slower cart-status/related-cocktails
            // pipeline, and the main content must not wait for (or be
            // replaced by) that. Full-screen error/retry is reserved for a
            // failed initial load; errors from actions on a loaded screen
            // surface through the alert below instead.
            if let cocktail = viewModel.state.cocktail {
                ScrollView {
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.lg) {
                        CocktailImageView(
                            imageUrl: cocktail.imageUrl,
                            height: 300
                        )

                        mainInfoCard(cocktail)

                        ingredientsSection(cocktail)

                        if let nutrition = viewModel.nutritionFacts {
                            nutritionSection(nutrition)
                        }

                        chipsSection(cocktail)

                        if viewModel.hasRelatedCocktails {
                            relatedCocktailsSection
                        }
                    }
                    .padding(.bottom, AppTheme.Spacing.xxl)
                }
                .toolbar {
                    ToolbarItemGroup(placement: .navigationBarTrailing) {
                        ShareLink(item: viewModel.shareContent()) {
                            Image(systemName: "square.and.arrow.up")
                        }
                        .accessibilityLabel("Share cocktail")
                        .accessibilityIdentifier("detail.shareButton")

                        Button(action: {
                            Task { await viewModel.toggleFavorite() }
                        }) {
                            Image(systemName: viewModel.state.isFavorite ? "heart.fill" : "heart")
                                .foregroundColor(viewModel.state.isFavorite ? .red : .primary)
                                .minimumHitTarget()
                        }
                        .accessibilityLabel(viewModel.state.isFavorite ? "Remove from favorites" : "Add to favorites")
                        .accessibilityIdentifier("detail.favoriteButton")
                    }
                }
            } else if viewModel.state.isLoading || !hasLoaded {
                LoadingStateView(message: "Loading cocktail details...")
            } else if let error = viewModel.error {
                ErrorView(
                    error: error,
                    onRetry: {
                        Task { await viewModel.loadCocktail(cocktailId) }
                    }
                )
            } else {
                VStack {
                    Image(systemName: "wineglass")
                        .font(.largeTitle)
                        .foregroundColor(.secondary)
                    Text("Cocktail not found")
                        .font(.headline)
                    Text("The requested cocktail could not be loaded")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }
        }
        // Stable UI-test hook for the deep-link launch test: marks the
        // detail screen's root in every branch (loading/loaded/error), so
        // asserting navigation doesn't depend on live network data.
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("detail.screen")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            // .task re-fires whenever the view re-appears — including on
            // pop-back from a related cocktail. @State survives while the
            // screen sits in the navigation stack, so skip the refetch when
            // this instance already holds its cocktail.
            if !hasLoaded || viewModel.state.cocktail?.id != cocktailId {
                await viewModel.loadCocktail(cocktailId)
                hasLoaded = true
            }
        }
        // Only action errors alert here; a failed initial load renders the
        // full-screen ErrorView (with Retry) instead of a duplicate alert
        // whose dismissal would clear the error into a "not found" dead end.
        .sharedErrorAlert(
            viewModel.state.cocktail != nil ? viewModel.error : nil,
            clear: { viewModel.clearError() }
        )
    }

    // MARK: - Main Info

    private func mainInfoCard(_ cocktail: Cocktail) -> some View {
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
                    Text(String(localized: "Preparation"))
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
    private var cartControls: some View {
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
                .accessibilityLabel(viewModel.state.cartQuantity <= 1 ? "Remove from cart" : "Decrease quantity")
                .accessibilityIdentifier("detail.decrementButton")

                Text("\(viewModel.state.cartQuantity) in cart")
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
                .accessibilityLabel("Increase quantity")
                .accessibilityIdentifier("detail.incrementButton")
            }
            .padding(.vertical, AppTheme.Spacing.sm)
        } else {
            Button(action: {
                runCartAction { await viewModel.addToCart(quantity: 1) }
            }) {
                HStack {
                    Image(systemName: "cart.badge.plus")
                    Text(viewModel.canAddToCart() ? "Add to Cart" : "Out of Stock")
                        .fontWeight(.semibold)
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding()
                .background(viewModel.canAddToCart()
                    ? AppColors.primary(isDarkMode: isDarkMode)
                    : AppColors.gray)
                .cornerRadius(AppTheme.CornerRadius.button)
            }
            .disabled(!viewModel.canAddToCart())
            .accessibilityLabel("Add to cart")
            .accessibilityIdentifier("detail.addToCartButton")
        }
    }

    private var canIncrementQuantity: Bool {
        guard let cocktail = viewModel.state.cocktail else { return false }
        return viewModel.state.cartQuantity < cocktail.stockCount
    }

    /// One cart mutation at a time: quantities are computed from the
    /// async-mirrored state, so overlapping mutations would read stale values.
    private func runCartAction(_ operation: @escaping () async -> Void) {
        guard !cartActionInFlight else { return }
        cartActionInFlight = true
        Task {
            await operation()
            cartActionInFlight = false
        }
    }

    // MARK: - Ingredients

    private func ingredientsSection(_ cocktail: Cocktail) -> some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
            Text(String(localized: "Ingredients"))
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
                            Text("• \(entry)")
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

    private func ingredientRow(name: String, measure: String?) -> some View {
        Text("• \(measure.flatMap { $0.isEmpty ? nil : "\($0) " } ?? "")\(name)")
            .font(.body)
            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
    }

    // MARK: - Nutrition

    private func nutritionSection(_ nutrition: NutritionFacts) -> some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
            Text(String(localized: "Estimated Nutrition"))
                .font(.headline)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            HStack(spacing: AppTheme.Spacing.sm) {
                nutritionStat(value: "\(nutrition.calories)", unit: "kcal", label: "Calories")
                nutritionStat(value: "\(nutrition.alcoholGrams)", unit: "g", label: "Alcohol")
                nutritionStat(value: "\(nutrition.carbsGrams)", unit: "g", label: "Carbs")
                nutritionStat(value: "\(nutrition.sugarGrams)", unit: "g", label: "Sugar")
            }

            Text("Rough keyword-based estimates, not measured data.")
                .font(.caption2)
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .cardStyle()
        .padding(.horizontal)
    }

    private func nutritionStat(value: String, unit: String, label: String) -> some View {
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

    private func chipsSection(_ cocktail: Cocktail) -> some View {
        // Attribute chips mirroring Android's DetailChipsSection
        let chips: [(icon: String, text: String)] = [
            cocktail.alcoholic.map { (icon: "wineglass", text: $0) },
            cocktail.glass.map { (icon: "cup.and.saucer", text: $0) },
            cocktail.iba.map { (icon: "rosette", text: "IBA: \($0)") }
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

    private var relatedCocktailsSection: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
            Text(String(localized: "You Might Also Like"))
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
}

// MARK: - Related Cocktail Card

private struct RelatedCocktailCard: View {
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
