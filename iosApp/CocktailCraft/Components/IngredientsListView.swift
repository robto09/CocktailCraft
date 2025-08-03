import SwiftUI
import shared

struct IngredientsListView: View {
    let ingredients: [CocktailIngredient]
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
            Text("Ingredients")
                .font(AppTheme.Typography.headline)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .padding(.top, AppTheme.Spacing.md)
            
            VStack(alignment: .leading, spacing: AppTheme.Spacing.sm) {
                ForEach(formattedIngredients, id: \.self) { ingredient in
                    HStack {
                        Text("• \(ingredient)")
                            .font(AppTheme.Typography.body)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        Spacer()
                    }
                }
            }
        }
        .padding(.horizontal, AppTheme.Spacing.lg)
    }
    
    private var formattedIngredients: [String] {
        ingredients.map { ingredient in
            if !ingredient.measure.isEmpty {
                return "\(ingredient.measure) \(ingredient.name)"
            } else {
                return ingredient.name
            }
        }
    }
}

#Preview {
    IngredientsListView(
        ingredients: [
            CocktailIngredient(name: "Tequila", measure: "1.5 oz"),
            CocktailIngredient(name: "Triple sec", measure: "0.5 oz"),
            CocktailIngredient(name: "Lime juice", measure: "1 oz"),
            CocktailIngredient(name: "Salt", measure: "")
        ]
    )
    .environment(\.isDarkMode, false)
}
