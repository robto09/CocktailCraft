import SwiftUI
@preconcurrency import shared
import Kingfisher

struct CocktailDetailView: View {
    let cocktailId: String
    @ObservedObject private var cartViewModel = CartViewModel.shared
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
                errorCode: ErrorCode.unknown
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
                let flow = try await repository.getCocktailById(id: cocktailId)
                print("CocktailDetailView - Got flow, creating collector")

                // Create a collector to get the cocktail
                let collector = FlowValueCollector<Cocktail>()
                collector.collect(from: flow)

                // Wait for the value to be collected
                var attempts = 0
                while collector.isLoading && attempts < 50 { // Wait up to 5 seconds
                    try await Task.sleep(nanoseconds: 100_000_000) // 0.1 seconds
                    attempts += 1
                }

                if let error = collector.error {
                    throw error
                }

                if let loadedCocktail = collector.value {
                    print("CocktailDetailView - Got cocktail: \(loadedCocktail.name)")
                    self.cocktail = loadedCocktail
                    self.isFavorite = self.favoritesViewModel.isFavorite(cocktailId: self.cocktailId)
                } else {
                    print("CocktailDetailView - No cocktail returned")
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Cocktail Not Found",
                        message: "The requested cocktail could not be found.",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
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
                    errorCode: ErrorCode.unknown
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