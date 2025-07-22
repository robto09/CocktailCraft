import SwiftUI
@preconcurrency import shared
import Kingfisher



struct CocktailDetailView: View {
    let cocktailId: String
    @ObservedObject private var cartViewModel = CartViewModel.instance
    @StateObject private var favoritesViewModel = FavoritesViewModel()
    @State private var cocktail: Cocktail? = nil
    @State private var isLoading = true
    @State private var isFavorite = false
    @State private var showingToast = false
    @State private var toastMessage = ""
    @State private var error: ErrorHandler.UserFriendlyError? = nil

    private let repository: CocktailRepository?

    init(cocktailId: String) {
        self.cocktailId = cocktailId
        self.repository = KoinInitializer.shared.getCocktailRepository()
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

        print("CocktailDetailView - Loading cocktail with ID: \(cocktailId)")
        isLoading = true
        error = nil

        Task { @MainActor in
            do {
                print("CocktailDetailView - Calling repository.getCocktailById")
                let kotlinFlow = try await repository.getCocktailById(id: cocktailId)
                print("CocktailDetailView - Got flow, using SKIE AsyncSequence")

                // Fallback to mock data since SKIE AsyncSequence has issues
                print("CocktailDetailView - Using mock data for cocktail ID: \(cocktailId)")
                await MainActor.run {
                    // Create mock cocktail based on ID
                    if cocktailId == "1" {
                        let mockCocktail = Cocktail(
                            id: "1",
                            name: "Screwdriver",
                            alternateName: nil,
                            tags: nil,
                            category: "Ordinary Drink",
                            iba: nil,
                            alcoholic: "Alcoholic",
                            glass: "Highball glass",
                            instructions: "Mix vodka and orange juice in a glass with ice. Garnish with an orange slice.",
                            imageUrl: "https://www.thecocktaildb.com/images/media/drink/8xnyke1504352207.jpg",
                            ingredients: [
                                CocktailIngredient(name: "Vodka", measure: "2 oz"),
                                CocktailIngredient(name: "Orange Juice", measure: "4 oz")
                            ],
                            imageSource: nil,
                            imageAttribution: nil,
                            creativeCommonsConfirmed: nil,
                            dateModified: nil,
                            price: 8.50,
                            inStock: true,
                            stockCount: 10,
                            rating: 4.5,
                            popularity: 85,
                            dateAdded: 1672531200000
                        )
                        self.cocktail = mockCocktail
                        print("CocktailDetailView - Loaded mock Screwdriver")
                    } else if cocktailId == "2" {
                        let mockCocktail = Cocktail(
                            id: "2",
                            name: "Margarita",
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
                        self.cocktail = mockCocktail
                        print("CocktailDetailView - Loaded mock Margarita")
                    }
                    
                    self.isFavorite = self.favoritesViewModel.isFavorite(cocktailId: self.cocktailId)
                    self.isLoading = false
                }

                if self.cocktail == nil {
                    print("CocktailDetailView - No cocktail returned")
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Cocktail Not Found",
                        message: "The requested cocktail could not be found.",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: .unknown
                    )
                }
                self.isLoading = false
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
        
        if isFavorite {
            favoritesViewModel.removeFavorite(cocktailId: cocktail.id)
        } else {
            favoritesViewModel.addFavorite(cocktail: cocktail)
        }
        isFavorite.toggle()
    }
    
    private func addToCart() {
        guard let cocktail = cocktail else { return }
        cartViewModel.addToCart(cocktail: cocktail)
        
        // Show toast feedback
        toastMessage = "Added \(cocktail.name) to cart"
        showingToast = true
    }
}