import SwiftUI

struct ActionButtonsRow: View {
    let isFavorite: Bool
    let onFavoriteToggle: () -> Void
    let onAddToCart: () -> Void
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        HStack(spacing: AppTheme.Spacing.lg) {
            // Favorite Button
            Button(action: onFavoriteToggle) {
                Label(
                    isFavorite ? "Remove from Favorites" : "Add to Favorites",
                    systemImage: isFavorite ? "heart.fill" : "heart"
                )
                .font(AppTheme.Typography.headline)
                .foregroundColor(isFavorite ? AppColors.error : AppColors.primary(isDarkMode: isDarkMode))
                .frame(maxWidth: .infinity)
                .padding(AppTheme.Spacing.lg)
                .background(AppColors.surface(isDarkMode: isDarkMode))
                .cornerRadius(AppTheme.CornerRadius.button)
                .overlay(
                    RoundedRectangle(cornerRadius: AppTheme.CornerRadius.button)
                        .stroke(isFavorite ? AppColors.error : AppColors.primary(isDarkMode: isDarkMode), lineWidth: 1)
                )
            }
            .buttonStyle(PlainButtonStyle())
            
            // Add to Cart Button
            Button(action: onAddToCart) {
                Label("Add to Cart", systemImage: "cart.badge.plus")
                    .font(AppTheme.Typography.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(AppTheme.Spacing.lg)
                    .background(AppColors.primary(isDarkMode: isDarkMode))
                    .cornerRadius(AppTheme.CornerRadius.button)
            }
            .buttonStyle(PlainButtonStyle())
        }
        .padding(.horizontal, AppTheme.Spacing.lg)
        .padding(.top, AppTheme.Spacing.lg)
    }
}

#Preview {
    VStack(spacing: 20) {
        ActionButtonsRow(
            isFavorite: false,
            onFavoriteToggle: {},
            onAddToCart: {}
        )
        
        ActionButtonsRow(
            isFavorite: true,
            onFavoriteToggle: {},
            onAddToCart: {}
        )
    }
    .environment(\.isDarkMode, false)
    .padding()
}
