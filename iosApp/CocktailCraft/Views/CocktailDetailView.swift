import SwiftUI
@preconcurrency import shared

struct CocktailDetailView: View {
    let cocktailId: String
    @State private var viewModel = CocktailDetailViewModelSKIE()

    // Distinguishes "load not started yet" from "loaded but not found"
    @State private var hasLoaded = false

    var body: some View {
        VStack {
            if viewModel.state.isLoading || !hasLoaded {
                VStack {
                    ProgressView()
                    Text("Loading cocktail details...")
                }
            } else if let error = viewModel.error {
                ErrorView(
                    error: error,
                    onRetry: {
                        Task { await viewModel.loadCocktail(cocktailId) }
                    }
                )
            } else if let cocktail = viewModel.state.cocktail {
                ScrollView {
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.lg) {
                        // Cocktail image
                        CocktailImageView(
                            imageUrl: cocktail.imageUrl,
                            height: 300
                        )

                        // Cocktail info
                        VStack(alignment: .leading) {
                            Text(cocktail.name)
                                .font(.largeTitle)
                                .fontWeight(.bold)
                            Text(cocktail.category ?? "")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        .padding()

                        // Ingredients
                        VStack(alignment: .leading) {
                            Text("Ingredients")
                                .font(.headline)
                            ForEach(Array(cocktail.ingredients.enumerated()), id: \.offset) { index, ingredient in
                                Text("• \(ingredient.name): \(ingredient.measure ?? "")")
                                    .font(.body)
                            }
                        }
                        .padding()
                    }
                }
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
        .task {
            await viewModel.loadCocktail(cocktailId)
            hasLoaded = true
        }
    }
}
