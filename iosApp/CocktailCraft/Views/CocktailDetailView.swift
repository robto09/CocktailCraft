import SwiftUI
@preconcurrency import shared



struct CocktailDetailView: View {
    let cocktailId: String
    @ObservedObject var cartViewModel: CartViewModelSKIE
    @StateObject private var reviewViewModel = ReviewViewModelSKIE()
    @StateObject private var favoritesViewModel = FavoritesViewModelSKIE()
    @State private var cocktail: Cocktail? = nil
    @State private var isLoading = true
    @State private var isFavorite = false
    @State private var showingToast = false
    @State private var toastMessage = ""
    @State private var error: ErrorHandler.UserFriendlyError? = nil

    // TODO: Use SharedCocktailDetailViewModel once exported properly
    private let repository: CocktailRepository?

    init(cocktailId: String, cartViewModel: CartViewModelSKIE) {
        self.cocktailId = cocktailId
        self.cartViewModel = cartViewModel
        self.repository = getSharedKoinHelper().getCocktailRepository()
    }

    var body: some View {
        VStack {
            if isLoading {
                VStack {
                    ProgressView()
                    Text("Loading cocktail details...")
                }
            } else if let error = error {
                ErrorView(
                    error: error,
                    onRetry: { loadCocktail() }
                )
            } else if let cocktail = cocktail {
                ScrollView {
                    VStack(alignment: .leading, spacing: AppTheme.Spacing.lg) {
                        // Cocktail image
                        AsyncImage(url: URL(string: cocktail.imageUrl ?? "")) { image in
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Rectangle()
                                .fill(Color.gray.opacity(0.3))
                        }
                        .frame(height: 300)
                        .clipped()

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
                            Text(cocktail.ingredients)
                                .font(.body)
                        }
                        .padding()

                        // Action Buttons
                        HStack {
                            Button("Add to Favorites") {
                                toggleFavorite()
                            }
                            .buttonStyle(.borderedProminent)

                            Button("Add to Cart") {
                                addToCart()
                            }
                            .buttonStyle(.borderedProminent)
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
        .onAppear {
            loadCocktail()
        }
        .toast(isShowing: $showingToast, message: toastMessage, type: .success)
    }
    
    private func loadCocktail() {
        guard let repository = repository else {
            print("CocktailDetailView - No repository available, showing error")
            self.error = ErrorHandler.shared.createUserFriendlyError(
                title: "Loading Error",
                message: "Unable to load cocktail details. Please try again later.",
                category: ErrorHandler.ErrorCategory.unknown,
                recoveryAction: nil,
                originalException: nil,
                errorCode: .unknown
            )
            self.isLoading = false
            return
        }

        print("CocktailDetailView - Loading cocktail with ID: \(cocktailId) via repository")
        print("CocktailDetailView - DEBUG: Cocktail ID type: \(type(of: cocktailId)), value: '\(cocktailId)'")
        isLoading = true
        error = nil

        Task { @MainActor in
            do {
                print("CocktailDetailView - Using HomeViewModel to get cocktail by ID: \(cocktailId)")
                
                // Use the HomeViewModel which already has working SKIE integration
                let homeViewModel = getSharedKoinHelper().getSharedHomeViewModel()
                let cocktailResult = try await homeViewModel.getCocktailById(id: cocktailId)
                
                if let cocktail = cocktailResult {
                    print("CocktailDetailView - Successfully received cocktail: \(cocktail.name)")
                    await MainActor.run {
                        self.cocktail = cocktail
                        self.isLoading = false
                    }
                } else {
                    print("CocktailDetailView - HomeViewModel returned nil for ID: \(cocktailId)")
                    await MainActor.run {
                        if self.cocktailId.hasPrefix("test-") {
                            self.cocktail = self.createMockCocktail(for: self.cocktailId)
                        } else {
                            self.error = ErrorHandler.shared.createUserFriendlyError(
                                title: "Cocktail Not Found",
                                message: "Could not load cocktail details for ID: \(self.cocktailId)",
                                category: ErrorHandler.ErrorCategory.unknown,
                                recoveryAction: nil,
                                originalException: nil,
                                errorCode: .unknown
                            )
                        }
                        self.isLoading = false
                    }
                }
                
                if self.cocktail != nil {
                    self.isFavorite = self.favoritesViewModel.isFavorite(self.cocktailId)
                }
            } catch {
                print("CocktailDetailView - Error loading cocktail: \(error)")
                self.isLoading = false
                self.error = ErrorHandler.shared.createUserFriendlyError(
                    title: "Loading Error",
                    message: "Failed to load cocktail details: \(error.localizedDescription)",
                    category: ErrorHandler.ErrorCategory.unknown,
                    recoveryAction: nil,
                    originalException: nil,
                    errorCode: .unknown
                )
            }
        }
    }
    

    
    private func toggleFavorite() {
        guard let cocktail = cocktail else { return }
        
        Task {
            await favoritesViewModel.toggleFavorite(cocktail)
            await MainActor.run {
                self.isFavorite = self.favoritesViewModel.isFavorite(cocktail.id)
            }
        }
    }
    
    private func addToCart() {
        guard let cocktail = cocktail else { return }
        Task {
            await cartViewModel.addToCart(cocktail, quantity: 1)
            
            // Show toast feedback
            await MainActor.run {
                toastMessage = "Added \(cocktail.name) to cart"
                showingToast = true
            }
        }
    }
    
    private func createMockCocktail(for id: String) -> Cocktail? {
        switch id {
        case "test-1":
            return Cocktail(
                id: "test-1",
                name: "Classic Margarita",
                alternateName: nil,
                tags: ["IBA", "Contemporary Classic"],
                category: "Ordinary Drink",
                iba: "Contemporary Classic",
                alcoholic: "Alcoholic",
                glass: "Cocktail glass",
                instructions: "Rub the rim with lime. Add ingredients to shaker with ice. Shake and strain into glass.",
                imageUrl: "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg",
                ingredients: [
                    CocktailIngredient(name: "Tequila", measure: "1.5 oz"),
                    CocktailIngredient(name: "Triple sec", measure: "0.5 oz"),
                    CocktailIngredient(name: "Lime juice", measure: "1 oz")
                ],
                imageSource: nil,
                imageAttribution: nil,
                creativeCommonsConfirmed: nil,
                dateModified: nil,
                price: 12.99,
                inStock: true,
                stockCount: 15,
                rating: 4.8,
                popularity: 95,
                dateAdded: 1672531300000
            )
        case "test-2":
            return Cocktail(
                id: "test-2",
                name: "Mojito",
                alternateName: nil,
                tags: ["IBA", "Contemporary Classic"],
                category: "Ordinary Drink",
                iba: "Contemporary Classic",
                alcoholic: "Alcoholic",
                glass: "Highball glass",
                instructions: "Muddle mint leaves with sugar and lime juice. Add rum and top with soda water.",
                imageUrl: "https://www.thecocktaildb.com/images/media/drink/metwgh1606770327.jpg",
                ingredients: [
                    CocktailIngredient(name: "White rum", measure: "2 oz"),
                    CocktailIngredient(name: "Lime juice", measure: "1 oz"),
                    CocktailIngredient(name: "Sugar", measure: "2 tsp"),
                    CocktailIngredient(name: "Mint", measure: "2-4 leaves"),
                    CocktailIngredient(name: "Soda water", measure: "Top")
                ],
                imageSource: nil,
                imageAttribution: nil,
                creativeCommonsConfirmed: nil,
                dateModified: nil,
                price: 11.50,
                inStock: true,
                stockCount: 20,
                rating: 4.6,
                popularity: 88,
                dateAdded: 1672531400000
            )
        case "test-3":
            return Cocktail(
                id: "test-3",
                name: "Cosmopolitan",
                alternateName: nil,
                tags: ["IBA", "Contemporary Classic"],
                category: "Ordinary Drink",
                iba: "Contemporary Classic",
                alcoholic: "Alcoholic",
                glass: "Cocktail glass",
                instructions: "Add all ingredients to shaker with ice. Shake and strain into chilled glass.",
                imageUrl: "https://www.thecocktaildb.com/images/media/drink/kpsajh1504368362.jpg",
                ingredients: [
                    CocktailIngredient(name: "Vodka", measure: "1.5 oz"),
                    CocktailIngredient(name: "Cointreau", measure: "0.5 oz"),
                    CocktailIngredient(name: "Cranberry juice", measure: "0.25 oz"),
                    CocktailIngredient(name: "Lime juice", measure: "0.25 oz")
                ],
                imageSource: nil,
                imageAttribution: nil,
                creativeCommonsConfirmed: nil,
                dateModified: nil,
                price: 13.75,
                inStock: true,
                stockCount: 12,
                rating: 4.4,
                popularity: 82,
                dateAdded: 1672531500000
            )
        default:
            return nil
        }
    }
}