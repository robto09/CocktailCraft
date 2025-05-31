import SwiftUI
import Shared

struct CocktailDetailView: View {
    let cocktailId: String
    
    @StateObject private var detailViewModel: ObservableCocktailDetailViewModel
    @StateObject private var cartViewModel = ViewModelProvider.shared.cartViewModel
    @StateObject private var reviewViewModel: ObservableViewModel<ReviewViewModel>
    @EnvironmentObject var navigationCoordinator: NavigationCoordinator
    
    @State private var showWriteReview = false
    @State private var showAddedToCartAlert = false
    
    init(cocktailId: String) {
        self.cocktailId = cocktailId
        _detailViewModel = StateObject(wrappedValue: ViewModelProvider.shared.cocktailDetailViewModel())
        _reviewViewModel = StateObject(wrappedValue: ViewModelProvider.shared.reviewViewModel())
    }
    
    var body: some View {
        ZStack {
            if detailViewModel.isLoading && detailViewModel.cocktail == nil {
                LoadingStateComponent(
                    isLoading: true,
                    indicatorSize: 50
                )
            } else if let cocktail = detailViewModel.cocktail {
                ScrollView {
                    VStack(spacing: 0) {
                        // Hero Image Section
                        DetailHeaderImage(
                            imageUrl: cocktail.strDrinkThumb ?? "",
                            contentDescription: cocktail.strDrink ?? "Cocktail image",
                            height: 250
                        )
                        
                        // Main Details Card
                        MainDetailsCard(
                            cocktail: cocktail,
                            isFavorite: detailViewModel.isFavorite,
                            isInCart: cartViewModel.cartItems.contains(where: { $0.cocktail.id == cocktail.id }),
                            onFavoriteToggle: {
                                detailViewModel.toggleFavorite()
                            },
                            onAddToCart: {
                                cartViewModel.viewModel.addToCart(cocktail: cocktail)
                                showAddedToCartAlert = true
                            }
                        )
                        .offset(y: -20)
                        .padding(.bottom, -20)
                        
                        VStack(spacing: 16) {
                            // How to Prepare Section
                            PreparationCard(instructions: cocktail.strInstructions)
                            
                            // Ingredients Section
                            IngredientsCard(cocktail: cocktail)
                            
                            // Details Section
                            DetailsCard(cocktail: cocktail)
                            
                            // Recommendations Section
                            if !detailViewModel.recommendations.isEmpty || detailViewModel.isLoadingRecommendations {
                                RecommendationsSection(
                                    category: cocktail.strCategory ?? "Cocktails",
                                    recommendations: detailViewModel.recommendations,
                                    isLoading: detailViewModel.isLoadingRecommendations,
                                    onCocktailTap: { recommendedCocktail in
                                        navigationCoordinator.navigateToCocktailDetail(cocktailId: recommendedCocktail.id)
                                    }
                                )
                            }
                            
                            // Reviews Section
                            ReviewsSection(
                                cocktailId: cocktailId,
                                reviews: reviewViewModel.viewModel.getReviewsForCocktail(cocktailId: cocktailId),
                                onWriteReview: {
                                    showWriteReview = true
                                }
                            )
                        }
                        .padding(.horizontal)
                        .padding(.bottom, 20)
                    }
                }
                .ignoresSafeArea(edges: .top)
            } else if let error = detailViewModel.errorMessage {
                ErrorStateView(
                    message: error,
                    onRetry: {
                        detailViewModel.loadCocktail(id: cocktailId)
                    }
                )
            }
        }
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(false)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text(detailViewModel.cocktail?.strDrink ?? "Loading...")
                    .font(.headline)
                    .foregroundColor(.primary)
            }
        }
        .onAppear {
            detailViewModel.loadCocktail(id: cocktailId)
        }
        .alert("Added to Cart", isPresented: $showAddedToCartAlert) {
            Button("OK", role: .cancel) { }
        } message: {
            Text("\(detailViewModel.cocktail?.strDrink ?? "Cocktail") has been added to your cart")
        }
        .sheet(isPresented: $showWriteReview) {
            if let cocktail = detailViewModel.cocktail {
                WriteReviewView(
                    cocktail: cocktail,
                    onSubmit: { review in
                        reviewViewModel.viewModel.addReview(review: review)
                        showWriteReview = false
                    },
                    onCancel: {
                        showWriteReview = false
                    }
                )
            }
        }
    }
}

// MARK: - Supporting Views

struct MainDetailsCard: View {
    let cocktail: Cocktail
    let isFavorite: Bool
    let isInCart: Bool
    let onFavoriteToggle: () -> Void
    let onAddToCart: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                // Price
                Text(String(format: "$%.2f", cocktail.price))
                    .font(.title)
                    .fontWeight(.bold)
                
                Spacer()
                
                // Favorite Button
                Button(action: onFavoriteToggle) {
                    Image(systemName: isFavorite ? "heart.fill" : "heart")
                        .font(.title2)
                        .foregroundColor(isFavorite ? .red : .secondary)
                }
            }
            
            // Category and Type
            Text("\(cocktail.strCategory ?? "Cocktail") • \(cocktail.strAlcoholic ?? "Unknown")")
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            // Rating
            if cocktail.averageRating > 0 {
                RatingDisplay(
                    rating: Float(cocktail.averageRating),
                    reviewCount: Int(cocktail.reviewCount)
                )
            }
            
            // Stock Status
            HStack(spacing: 4) {
                Circle()
                    .fill(cocktail.stockCount > 0 ? Color.green : Color.red)
                    .frame(width: 8, height: 8)
                
                Text(cocktail.stockCount > 0 ? "In Stock (\(cocktail.stockCount) available)" : "Out of Stock")
                    .font(.caption)
                    .foregroundColor(cocktail.stockCount > 0 ? .green : .red)
            }
            
            // Add to Cart Button
            Button(action: onAddToCart) {
                HStack {
                    Image(systemName: "cart.badge.plus")
                    Text(isInCart ? "Update Cart" : "Add to Cart")
                        .fontWeight(.semibold)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(cocktail.stockCount > 0 ? Color.accentColor : Color.gray)
                .foregroundColor(.white)
                .cornerRadius(10)
            }
            .disabled(cocktail.stockCount <= 0)
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.1), radius: 10, x: 0, y: 5)
        .padding(.horizontal)
    }
}

struct PreparationCard: View {
    let instructions: String?
    
    var body: some View {
        DetailInfoCard(title: "How to Prepare") {
            if let instructions = instructions, !instructions.isEmpty {
                Text(instructions)
                    .font(.body)
                    .foregroundColor(.secondary)
            } else {
                VStack(spacing: 8) {
                    Text("No instructions available")
                        .font(.body)
                        .foregroundColor(.secondary)
                    
                    Button(action: {
                        // TODO: Implement refresh
                    }) {
                        Label("Refresh Details", systemImage: "arrow.clockwise")
                            .font(.caption)
                    }
                }
            }
        }
    }
}

struct IngredientsCard: View {
    let cocktail: Cocktail
    
    var ingredients: [(name: String, measure: String)] {
        var result: [(String, String)] = []
        
        let ingredients = [
            (cocktail.strIngredient1, cocktail.strMeasure1),
            (cocktail.strIngredient2, cocktail.strMeasure2),
            (cocktail.strIngredient3, cocktail.strMeasure3),
            (cocktail.strIngredient4, cocktail.strMeasure4),
            (cocktail.strIngredient5, cocktail.strMeasure5),
            (cocktail.strIngredient6, cocktail.strMeasure6),
            (cocktail.strIngredient7, cocktail.strMeasure7),
            (cocktail.strIngredient8, cocktail.strMeasure8),
            (cocktail.strIngredient9, cocktail.strMeasure9),
            (cocktail.strIngredient10, cocktail.strMeasure10),
            (cocktail.strIngredient11, cocktail.strMeasure11),
            (cocktail.strIngredient12, cocktail.strMeasure12),
            (cocktail.strIngredient13, cocktail.strMeasure13),
            (cocktail.strIngredient14, cocktail.strMeasure14),
            (cocktail.strIngredient15, cocktail.strMeasure15)
        ]
        
        for (ingredient, measure) in ingredients {
            if let ingredient = ingredient, !ingredient.isEmpty {
                result.append((ingredient, measure ?? ""))
            }
        }
        
        return result
    }
    
    var body: some View {
        DetailInfoCard(title: "Ingredients") {
            VStack(alignment: .leading, spacing: 8) {
                ForEach(ingredients, id: \.name) { ingredient in
                    HStack(alignment: .top) {
                        Text("•")
                            .foregroundColor(.secondary)
                        Text("\(ingredient.name) \(ingredient.measure)")
                            .font(.body)
                            .foregroundColor(.secondary)
                        Spacer()
                    }
                }
            }
        }
    }
}

struct DetailsCard: View {
    let cocktail: Cocktail
    
    var body: some View {
        DetailInfoCard(title: "Details") {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    FilterChip(
                        label: cocktail.strCategory ?? "Unknown",
                        selected: false,
                        onClick: {}
                    )
                    .disabled(true)
                    
                    if let glass = cocktail.strGlass {
                        FilterChip(
                            label: glass,
                            selected: false,
                            onClick: {}
                        )
                        .disabled(true)
                    }
                    
                    if let alcoholic = cocktail.strAlcoholic {
                        FilterChip(
                            label: alcoholic,
                            selected: false,
                            onClick: {}
                        )
                        .disabled(true)
                    }
                }
            }
        }
    }
}



struct ErrorStateView: View {
    let message: String
    let onRetry: () -> Void
    
    var body: some View {
        VStack {
            ErrorDialog(
                errorTitle: "Error Loading Cocktail",
                errorMessage: message,
                errorIcon: "exclamationmark.triangle",
                iconColor: .orange,
                onDismiss: {},
                onRetry: onRetry
            )
        }
        .padding()
    }
}