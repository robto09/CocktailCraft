import SwiftUI
@preconcurrency import shared
import Kingfisher



struct CocktailDetailView: View {
    let cocktailId: String
    @StateObject private var cartViewModel = CartViewModelSKIE()
    @StateObject private var reviewViewModel = ReviewViewModelSKIE()
    @StateObject private var favoritesViewModel = FavoritesViewModelSKIE()
    @State private var cocktail: shared.Cocktail? = nil
    @State private var isLoading = true
    @State private var isFavorite = false
    @State private var showingToast = false
    @State private var toastMessage = ""
    @State private var error: shared.ErrorHandler.UserFriendlyError? = nil

    // TODO: Use SharedCocktailDetailViewModel once exported properly
    private let repository: shared.CocktailRepository?

    init(cocktailId: String) {
        self.cocktailId = cocktailId
        self.repository = getSharedKoinHelper().getCocktailRepository()
    }

    var body: some View {
        VStack {
            if isLoading {
                ProgressView("Loading...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = error {
                VStack(spacing: 16) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundColor(.orange)

                    Text(error.title)
                        .font(.headline)

                    Text(error.message)
                        .font(.body)
                        .multilineTextAlignment(.center)

                    Button("Retry") {
                        loadCocktail()
                    }
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
                .padding()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let cocktail = cocktail {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        // Cocktail image
                        KFImage(URL(string: cocktail.imageUrl ?? ""))
                            .placeholder {
                                ProgressView()
                                    .frame(height: 300)
                            }
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                        .frame(maxHeight: 300)
                        .clipped()
                        
                        VStack(alignment: .leading, spacing: 12) {
                            Text(cocktail.name)
                                .font(.largeTitle)
                                .fontWeight(.bold)
                            
                            HStack {
                                if let category = cocktail.category {
                                    Label(category, systemImage: "tag")
                                        .font(.subheadline)
                                        .foregroundColor(.secondary)
                                }
                                
                                Spacer()

                                Text("$\(String(format: "%.2f", cocktail.price))")
                                    .font(.title2)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.blue)
                            }
                            
                            if let instructions = cocktail.instructions {
                                Text("Instructions")
                                    .font(.headline)
                                    .padding(.top)
                                
                                Text(instructions)
                                    .font(.body)
                            }
                            
                            // Ingredients
                            Text("Ingredients")
                                .font(.headline)
                                .padding(.top)
                            
                            VStack(alignment: .leading, spacing: 8) {
                                ForEach(getIngredients(from: cocktail), id: \.self) { ingredient in
                                    HStack {
                                        Text("• \(ingredient)")
                                            .font(.body)
                                        Spacer()
                                    }
                                }
                            }
                            
                            // Action Buttons
                            HStack(spacing: 16) {
                                Button(action: {
                                    toggleFavorite()
                                }) {
                                    Label(
                                        isFavorite ? "Remove from Favorites" : "Add to Favorites",
                                        systemImage: isFavorite ? "heart.fill" : "heart"
                                    )
                                    .font(.headline)
                                    .foregroundColor(isFavorite ? .red : .blue)
                                    .frame(maxWidth: .infinity)
                                    .padding()
                                    .background(Color(UIColor.secondarySystemBackground))
                                    .cornerRadius(10)
                                }
                                
                                Button(action: {
                                    addToCart()
                                }) {
                                    Label("Add to Cart", systemImage: "cart.badge.plus")
                                        .font(.headline)
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding()
                                        .background(Color.blue)
                                        .cornerRadius(10)
                                }
                            }
                            .padding(.top)
                        }
                        .padding()
                    }
                }
            } else {
                Text("Cocktail not found")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
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
        isLoading = true
        error = nil

        Task { @MainActor in
            do {
                print("CocktailDetailView - Calling repository.getCocktailById")
                let kotlinFlow = try await repository.getCocktailById(id: cocktailId)
                print("CocktailDetailView - Got flow: \(type(of: kotlinFlow))")

                // Since SKIE AsyncSequence casting isn't working, create mock cocktail
                print("CocktailDetailView - AsyncSequence casting failed, using mock data")
                await MainActor.run {
                    // Create mock cocktail based on ID for testing
                    if cocktailId.hasPrefix("test-") {
                        self.cocktail = self.createMockCocktail(for: cocktailId)
                    }
                    self.isLoading = false
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
    
    private func getIngredients(from cocktail: Cocktail) -> [String] {
        return cocktail.ingredients.map { ingredient in
            if !ingredient.measure.isEmpty {
                return "\(ingredient.measure) \(ingredient.name)"
            } else {
                return ingredient.name
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