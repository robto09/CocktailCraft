import SwiftUI
import shared

struct CocktailDetailView: View {
    let cocktailId: String
    @StateObject private var cartViewModel = CartViewModel()
    @StateObject private var favoritesViewModel = FavoritesViewModel()
    @State private var cocktail: Cocktail? = nil
    @State private var isLoading = true
    @State private var isFavorite = false
    @State private var showingToast = false
    @State private var toastMessage = ""
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView("Loading...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let cocktail = cocktail {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        // Cocktail image
                        AsyncImage(url: URL(string: cocktail.strDrinkThumb ?? "")) { image in
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        } placeholder: {
                            ProgressView()
                                .frame(height: 300)
                        }
                        .frame(maxHeight: 300)
                        .clipped()
                        
                        VStack(alignment: .leading, spacing: 12) {
                            Text(cocktail.strDrink)
                                .font(.largeTitle)
                                .fontWeight(.bold)
                            
                            HStack {
                                if let category = cocktail.strCategory {
                                    Label(category, systemImage: "tag")
                                        .font(.subheadline)
                                        .foregroundColor(.secondary)
                                }
                                
                                Spacer()
                                
                                Text("$12.99")
                                    .font(.title2)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.blue)
                            }
                            
                            if let instructions = cocktail.strInstructions {
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
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadCocktail()
        }
        .toast(isShowing: $showingToast, message: toastMessage, type: .success)
    }
    
    private func loadCocktail() {
        // In a real app, this would fetch from the repository
        // For now, we'll create a sample cocktail
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.cocktail = Cocktail(
                idDrink: self.cocktailId,
                strDrink: "Margarita",
                strDrinkAlternate: nil,
                strTags: "IBA,Contemporary Classic",
                strVideo: nil,
                strCategory: "Ordinary Drink",
                strIBA: "Contemporary Classic",
                strAlcoholic: "Alcoholic",
                strGlass: "Cocktail glass",
                strInstructions: "Rub the rim of the glass with the lime slice to make the salt stick to it. Take care to moisten only the outer rim and sprinkle the salt on it. The salt should present to the lips of the imbiber and never mix into the cocktail. Shake the other ingredients with ice, then carefully pour into the glass.",
                strInstructionsES: nil,
                strInstructionsDE: nil,
                strInstructionsFR: nil,
                strInstructionsIT: nil,
                strInstructionsZH_HANS: nil,
                strInstructionsZH_HANT: nil,
                strDrinkThumb: "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg",
                strIngredient1: "Tequila",
                strIngredient2: "Triple sec",
                strIngredient3: "Lime juice",
                strIngredient4: "Salt",
                strIngredient5: nil,
                strIngredient6: nil,
                strIngredient7: nil,
                strIngredient8: nil,
                strIngredient9: nil,
                strIngredient10: nil,
                strIngredient11: nil,
                strIngredient12: nil,
                strIngredient13: nil,
                strIngredient14: nil,
                strIngredient15: nil,
                strMeasure1: "1 1/2 oz",
                strMeasure2: "1/2 oz",
                strMeasure3: "1 oz",
                strMeasure4: nil,
                strMeasure5: nil,
                strMeasure6: nil,
                strMeasure7: nil,
                strMeasure8: nil,
                strMeasure9: nil,
                strMeasure10: nil,
                strMeasure11: nil,
                strMeasure12: nil,
                strMeasure13: nil,
                strMeasure14: nil,
                strMeasure15: nil,
                strImageSource: nil,
                strImageAttribution: nil,
                strCreativeCommonsConfirmed: "Yes",
                dateModified: "2015-08-18 14:42:59",
                reviews: []
            )
            self.isFavorite = self.favoritesViewModel.isFavorite(cocktailId: self.cocktailId)
            self.isLoading = false
        }
    }
    
    private func getIngredients(from cocktail: Cocktail) -> [String] {
        var ingredients: [String] = []
        
        let ingredientMeasurePairs = [
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
        
        for (ingredient, measure) in ingredientMeasurePairs {
            if let ingredient = ingredient, !ingredient.isEmpty {
                if let measure = measure, !measure.isEmpty {
                    ingredients.append("\(measure) \(ingredient)")
                } else {
                    ingredients.append(ingredient)
                }
            }
        }
        
        return ingredients
    }
    
    private func toggleFavorite() {
        guard let cocktail = cocktail else { return }
        
        if isFavorite {
            favoritesViewModel.removeFavorite(cocktailId: cocktail.idDrink)
        } else {
            favoritesViewModel.addFavorite(cocktail: cocktail)
        }
        isFavorite.toggle()
    }
    
    private func addToCart() {
        guard let cocktail = cocktail else { return }
        cartViewModel.addToCart(cocktail: cocktail)
        
        // Show toast feedback
        toastMessage = "Added \(cocktail.strDrink) to cart"
        showingToast = true
    }
}