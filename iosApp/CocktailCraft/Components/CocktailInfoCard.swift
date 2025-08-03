import SwiftUI
import shared

struct CocktailInfoCard: View {
    let cocktail: Cocktail
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.md) {
            // Title
            Text(cocktail.name)
                .font(AppTheme.Typography.largeTitle)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
            
            // Category and Price Row
            HStack {
                if let category = cocktail.category {
                    Label(category, systemImage: "tag")
                        .font(AppTheme.Typography.subheadline)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                }
                
                Spacer()

                Text("$\(String(format: "%.2f", cocktail.price))")
                    .font(AppTheme.Typography.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
            }
            
            // Instructions
            if let instructions = cocktail.instructions {
                VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
                    Text("Instructions")
                        .font(AppTheme.Typography.headline)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        .padding(.top, AppTheme.Spacing.md)
                    
                    Text(instructions)
                        .font(AppTheme.Typography.body)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                }
            }
        }
        .padding(AppTheme.Spacing.lg)
    }
}

#Preview {
    CocktailInfoCard(
        cocktail: Cocktail(
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
            ingredients: [],
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
    )
    .environment(\.isDarkMode, false)
}
