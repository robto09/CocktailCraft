import SwiftUI

import shared

import Kingfisher

struct CocktailCard: View {
    let cocktail: Cocktail
    var isFavorite: Bool = false
    var onFavoriteToggle: (() -> Void)? = nil
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Cocktail Image
            ZStack(alignment: .topTrailing) {
                KFImage(URL(string: cocktail.imageUrl ?? ""))
                    .placeholder {
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color.gray.opacity(0.2))
                            .overlay(
                                ProgressView()
                            )
                    }
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(height: 150)
                    .clipped()
                    .cornerRadius(12)
                
                // Favorite Button
                if let onToggle = onFavoriteToggle {
                    Button(action: onToggle) {
                        Image(systemName: isFavorite ? "heart.fill" : "heart")
                            .foregroundColor(isFavorite ? .red : .white)
                            .padding(8)
                            .background(Color.black.opacity(0.6))
                            .clipShape(Circle())
                    }
                    .padding(8)
                }
            }
            
            // Cocktail Info
            VStack(alignment: .leading, spacing: 4) {
                Text(cocktail.name)
                    .font(.headline)
                    .lineLimit(2)
                    .foregroundColor(.primary)
                
                HStack {
                    if let category = cocktail.category {
                        Text(category)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                    
                    Text("$12.99")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.blue)
                }
            }
            .padding(.horizontal, 4)
        }
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}