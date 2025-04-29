import SwiftUI
import shared
import Kingfisher

struct CocktailCard: View {
    let cocktail: Cocktail
    let onFavoriteClick: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Cocktail Image
            KFImage(URL(string: cocktail.imageUrl))
                .placeholder {
                    Rectangle()
                        .foregroundColor(.gray.opacity(0.2))
                }
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(height: 200)
                .clipped()
                .cornerRadius(AppTheme.cornerRadius)
            
            // Cocktail Details
            VStack(alignment: .leading, spacing: 4) {
                Text(cocktail.name)
                    .font(AppTheme.Typography.headlineFont)
                    .foregroundColor(.primary)
                
                Text(cocktail.description_)
                    .font(AppTheme.Typography.captionFont)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
                
                HStack {
                    // Rating
                    HStack(spacing: 4) {
                        Image(systemName: "star.fill")
                            .foregroundColor(.yellow)
                        Text(String(format: "%.1f", cocktail.rating))
                            .font(AppTheme.Typography.captionFont)
                    }
                    
                    Spacer()
                    
                    // Favorite Button
                    Button(action: onFavoriteClick) {
                        Image(systemName: cocktail.isFavorite ? "heart.fill" : "heart")
                            .foregroundColor(cocktail.isFavorite ? .red : .gray)
                    }
                }
            }
            .padding(.horizontal, AppTheme.padding)
        }
        .background(Color(.systemBackground))
        .cornerRadius(AppTheme.cornerRadius)
        .shadow(radius: 2)
    }
}