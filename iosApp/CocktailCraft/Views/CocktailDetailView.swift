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
/// The section subviews live in Components/DetailSections.swift.
struct CocktailDetailView: View {
    let cocktailId: String
    // Internal (not private) so the section extension in
    // Components/DetailSections.swift can read them.
    @State var viewModel = CocktailDetailViewModelSKIE()
    @Environment(\.isDarkMode) var isDarkMode

    // Distinguishes "load not started yet" from "loaded but not found"
    @State private var hasLoaded = false
    // Serializes cart mutations: the stepper reads the async-mirrored
    // cartQuantity, so a second tap before the first round-trip lands would
    // compute from stale state (lost or duplicated increments).
    @State var cartActionInFlight = false

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
                        .tint(.white)

                        Button(action: {
                            Task { await viewModel.toggleFavorite() }
                        }) {
                            Image(systemName: viewModel.state.isFavorite ? "heart.fill" : "heart")
                                .foregroundColor(.white)
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
        .navigationTitle(viewModel.state.cocktail?.name ?? "")
        .brandedNavigationBar()
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
}
