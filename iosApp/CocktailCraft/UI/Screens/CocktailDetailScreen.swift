import SwiftUI
import shared

struct CocktailDetailScreen: View {
    let cocktailId: String
    @StateObject private var viewModel = DependencyContainer.shared.makeCocktailDetailViewModel()
    @Environment(\.presentationMode) var presentationMode
    @State private var quantity = 1
    @State private var showingReviewSheet = false
    @State private var showingAddToCartSheet = false
    @State private var showingError = false
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Hero Image
                heroImage
                
                // Content
                VStack(alignment: .leading, spacing: 20) {
                    // Header
                    header
                    
                    // Description
                    description
                    
                    // Ingredients
                    ingredients
                    
                    // Instructions
                    instructions
                    
                    // Reviews
                    reviews
                    
                    // Nutritional Info
                    nutritionalInfo
                    
                    // Similar Cocktails
                    similarCocktails
                }
                .padding()
            }
        }
        .overlay(loadingOverlay)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                favoriteButton
            }
        }
        .sheet(isPresented: $showingReviewSheet) {
            AddReviewSheet(cocktailName: viewModel.cocktail?.name ?? "") { rating, comment in
                viewModel.addReview(rating: Int32(rating), comment: comment)
            }
        }
        .sheet(isPresented: $showingAddToCartSheet) {
            AddToCartSheet(
                cocktail: viewModel.cocktail,
                quantity: $quantity,
                onAdd: { quantity in
                    viewModel.addToCart(quantity: Int32(quantity))
                }
            )
        }
        .alert("Error", isPresented: $showingError, presenting: viewModel.error) { _ in
            Button("OK") {}
        } message: { error in
            Text(error)
        }
        .onAppear {
            viewModel.loadCocktail(id: cocktailId)
        }
    }
    
    private var heroImage: some View {
        ZStack(alignment: .bottom) {
            if let imageUrl = viewModel.cocktail?.imageUrl {
                AsyncImage(url: URL(string: imageUrl)) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color.gray.opacity(0.3)
                }
                .frame(height: 300)
                .clipped()
            }
            
            // Price Tag
            if let price = viewModel.cocktail?.price {
                Text("$\(String(format: "%.2f", price))")
                    .font(.title2)
                    .bold()
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(Color.accentColor)
                    .cornerRadius(8)
                    .padding(.bottom)
            }
        }
    }
    
    private var header: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(viewModel.cocktail?.name ?? "")
                .font(.title)
                .bold()
            
            if let category = viewModel.cocktail?.category {
                Text(category)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            HStack {
                RatingView(rating: viewModel.cocktail?.rating ?? 0)
                
                Text("(\(viewModel.cocktail?.reviews.count ?? 0) reviews)")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            // Add to Cart Button
            Button(action: {
                showingAddToCartSheet = true
            }) {
                HStack {
                    Image(systemName: "cart.badge.plus")
                    Text("Add to Cart")
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.accentColor)
                .foregroundColor(.white)
                .cornerRadius(12)
            }
            .padding(.top)
        }
    }
    
    private var description: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Description")
                .font(.headline)
            
            Text(viewModel.cocktail?.description_ ?? "")
                .foregroundColor(.secondary)
        }
    }
    
    private var ingredients: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Ingredients")
                .font(.headline)
            
            ForEach(viewModel.cocktail?.ingredients ?? [], id: \.name) { ingredient in
                HStack {
                    Text("•")
                    Text("\(String(format: "%.1f", ingredient.amount)) \(ingredient.unit) \(ingredient.name)")
                        .foregroundColor(.secondary)
                    if ingredient.isOptional {
                        Text("(optional)")
                            .italic()
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
    }
    
    private var instructions: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Instructions")
                .font(.headline)
            
            if let instructions = viewModel.cocktail?.instructions {
                Text(instructions)
                    .foregroundColor(.secondary)
            }
            
            if let glassType = viewModel.cocktail?.glassType {
                Text("Serve in: \(glassType)")
                    .foregroundColor(.secondary)
                    .padding(.top, 4)
            }
            
            if let garnish = viewModel.cocktail?.garnish {
                Text("Garnish: \(garnish)")
                    .foregroundColor(.secondary)
            }
        }
    }
    
    private var reviews: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Reviews")
                    .font(.headline)
                
                Spacer()
                
                Button("Add Review") {
                    showingReviewSheet = true
                }
            }
            
            ForEach(viewModel.cocktail?.reviews ?? [], id: \.id) { review in
                ReviewCard(review: review)
            }
        }
    }
    
    private var nutritionalInfo: some View {
        if let info = viewModel.cocktail?.nutritionalInfo {
            VStack(alignment: .leading, spacing: 8) {
                Text("Nutritional Information")
                    .font(.headline)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text("Calories: \(info.calories) kcal")
                    Text("Alcohol: \(String(format: "%.1f", info.alcohol))%")
                    Text("Sugar: \(String(format: "%.1f", info.sugar))g")
                    
                    HStack {
                        if info.isVegan {
                            Label("Vegan", systemImage: "leaf.fill")
                        }
                        if info.isGlutenFree {
                            Label("Gluten-free", systemImage: "checkmark.circle.fill")
                        }
                    }
                    
                    if !info.allergens.isEmpty {
                        Text("Allergens: \(info.allergens.joined(separator: ", "))")
                    }
                }
                .foregroundColor(.secondary)
            }
        }
    }
    
    private var similarCocktails: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("You might also like")
                .font(.headline)
            
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 16) {
                    ForEach(viewModel.similarCocktails, id: \.id) { cocktail in
                        NavigationLink(
                            destination: CocktailDetailScreen(cocktailId: cocktail.id)
                        ) {
                            SimilarCocktailCard(cocktail: cocktail)
                        }
                    }
                }
            }
        }
    }
    
    private var favoriteButton: some View {
        Button(action: {
            viewModel.toggleFavorite()
        }) {
            Image(systemName: viewModel.isFavorite ? "heart.fill" : "heart")
                .foregroundColor(viewModel.isFavorite ? .red : .primary)
        }
    }
    
    private var loadingOverlay: some View {
        Group {
            if viewModel.isLoading {
                Color.black.opacity(0.3)
                    .ignoresSafeArea()
                    .overlay(ProgressView())
            }
        }
    }
}

private struct ReviewCard: View {
    let review: Review
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(review.userName)
                    .font(.headline)
                
                Spacer()
                
                RatingView(rating: review.rating)
            }
            
            if let comment = review.comment {
                Text(comment)
                    .foregroundColor(.secondary)
            }
            
            HStack {
                Text(Date(timeIntervalSince1970: TimeInterval(review.date / 1000)).formatted(.relative(presentation: .named)))
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Spacer()
                
                if review.likes > 0 {
                    Label("\(review.likes)", systemImage: "hand.thumbsup.fill")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

private struct RatingView: View {
    let rating: Float
    
    var body: some View {
        HStack(spacing: 4) {
            ForEach(1...5, id: \.self) { index in
                Image(systemName: index <= Int(rating) ? "star.fill" : "star")
                    .foregroundColor(.yellow)
            }
        }
    }
}

private struct SimilarCocktailCard: View {
    let cocktail: Cocktail
    
    var body: some View {
        VStack(alignment: .leading) {
            AsyncImage(url: URL(string: cocktail.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.3)
            }
            .frame(width: 140, height: 140)
            .clipped()
            .cornerRadius(8)
            
            Text(cocktail.name)
                .font(.subheadline)
                .lineLimit(1)
            
            Text("$\(String(format: "%.2f", cocktail.price))")
                .font(.caption)
                .bold()
        }
        .frame(width: 140)
    }
}

private struct AddReviewSheet: View {
    let cocktailName: String
    let onSubmit: (Int, String) -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var rating = 3
    @State private var comment = ""
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    Stepper("Rating: \(rating) stars", value: $rating, in: 1...5)
                    
                    TextEditor(text: $comment)
                        .frame(height: 100)
                }
            }
            .navigationTitle("Review \(cocktailName)")
            .navigationBarItems(
                leading: Button("Cancel") {
                    dismiss()
                },
                trailing: Button("Submit") {
                    onSubmit(rating, comment)
                    dismiss()
                }
            )
        }
    }
}

private struct AddToCartSheet: View {
    let cocktail: Cocktail?
    @Binding var quantity: Int
    let onAdd: (Int) -> Void
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    Stepper("Quantity: \(quantity)", value: $quantity, in: 1...10)
                    
                    if let price = cocktail?.price {
                        HStack {
                            Text("Total")
                            Spacer()
                            Text("$\(String(format: "%.2f", price * Double(quantity)))")
                                .bold()
                        }
                    }
                }
            }
            .navigationTitle("Add to Cart")
            .navigationBarItems(
                leading: Button("Cancel") {
                    dismiss()
                },
                trailing: Button("Add") {
                    onAdd(quantity)
                    dismiss()
                }
            )
        }
    }
}