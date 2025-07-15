import SwiftUI

import shared
import Kingfisher

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
        VStack {
            if isLoading {
                ProgressView("Loading...")
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
                                
                                Text("$12.99")
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
        // In a real app, this would fetch from the repository
        // For now, we'll create a sample cocktail
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.cocktail = Cocktail(
                id: self.cocktailId,
                name: "Margarita",
                alternateName: nil,
                tags: ["IBA", "Contemporary Classic"],
                category: "Ordinary Drink",
                iba: "Contemporary Classic",
                alcoholic: "Alcoholic",
                glass: "Cocktail glass",
                instructions: "Rub the rim of the glass with the lime slice to make the salt stick to it. Take care to moisten only the outer rim and sprinkle the salt on it. The salt should present to the lips of the imbiber and never mix into the cocktail. Shake the other ingredients with ice, then carefully pour into the glass.",
                imageUrl: "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg",
                ingredients: [
                    CocktailIngredient(name: "Tequila", measure: "1 1/2 oz"),
                    CocktailIngredient(name: "Triple sec", measure: "1/2 oz"),
                    CocktailIngredient(name: "Lime juice", measure: "1 oz"),
                    CocktailIngredient(name: "Salt", measure: "")
                ],
                imageSource: nil,
                imageAttribution: nil,
                creativeCommonsConfirmed: true,
                dateModified: "2015-08-18 14:42:59",
                price: 12.99,
                inStock: true,
                stockCount: 50,
                rating: 4.5,
                popularity: 95,
                dateAdded: Int64(Date().timeIntervalSince1970 * 1000)
            )
            self.isFavorite = self.favoritesViewModel.isFavorite(cocktailId: self.cocktailId)
            self.isLoading = false
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