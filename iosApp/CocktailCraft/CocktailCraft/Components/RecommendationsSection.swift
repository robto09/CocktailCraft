import SwiftUI
import Shared

struct RecommendationsSection: View {
    let category: String
    let recommendations: [Cocktail]
    let isLoading: Bool
    let onCocktailTap: (Cocktail) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(recommendations.isEmpty ? "You might also like" : "More \(category)")
                .font(.headline)
                .padding(.horizontal)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 16) {
                    if isLoading {
                        // Loading shimmer
                        ForEach(0..<3, id: \.self) { _ in
                            RecommendationLoadingCard()
                        }
                    } else {
                        ForEach(recommendations.prefix(6), id: \.id) { cocktail in
                            RecommendationCard(
                                cocktail: cocktail,
                                onTap: { onCocktailTap(cocktail) }
                            )
                        }
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}

struct RecommendationCard: View {
    let cocktail: Cocktail
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 8) {
                // Cocktail Image
                AsyncImage(url: URL(string: cocktail.strDrinkThumb ?? "")) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color(.systemGray5))
                        .overlay(
                            Image(systemName: "photo")
                                .foregroundColor(.secondary)
                        )
                }
                .frame(width: 140, height: 140)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(cocktail.strDrink)
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                        .foregroundColor(.primary)
                    
                    Text(String(format: "$%.2f", cocktail.price))
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundColor(.secondary)
                }
                .frame(width: 140, alignment: .leading)
            }
        }
        .buttonStyle(.plain)
    }
}

struct RecommendationLoadingCard: View {
    @State private var isAnimating = false
    
    var body: some View {
        VStack(spacing: 8) {
            // Image placeholder
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemGray6))
                .frame(width: 140, height: 140)
                .overlay(
                    LinearGradient(
                        gradient: Gradient(colors: [
                            Color.clear,
                            Color.white.opacity(0.3),
                            Color.clear
                        ]),
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                    .rotationEffect(.degrees(-45))
                    .offset(x: isAnimating ? 200 : -200)
                )
            
            VStack(alignment: .leading, spacing: 4) {
                // Title placeholder
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color(.systemGray6))
                    .frame(width: 100, height: 16)
                
                // Price placeholder
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color(.systemGray6))
                    .frame(width: 60, height: 14)
            }
            .frame(width: 140, alignment: .leading)
        }
        .onAppear {
            withAnimation(.linear(duration: 1.5).repeatForever(autoreverses: false)) {
                isAnimating = true
            }
        }
    }
}