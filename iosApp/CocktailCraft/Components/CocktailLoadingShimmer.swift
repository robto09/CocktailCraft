import SwiftUI

struct CocktailLoadingShimmer: View {
    @State private var isAnimating = false
    
    var body: some View {
        HStack(spacing: AppTheme.Spacing.md) {
            // Image placeholder
            RoundedRectangle(cornerRadius: AppTheme.CornerRadius.sm)
                .fill(AppColors.lightGray)
                .frame(width: 100, height: 100)
                .overlay(
                    RoundedRectangle(cornerRadius: AppTheme.CornerRadius.sm)
                        .fill(
                            LinearGradient(
                                colors: [Color.clear, Color.white.opacity(0.3), Color.clear],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .offset(x: isAnimating ? 120 : -120)
                        .animation(
                            Animation.linear(duration: 1.5).repeatForever(autoreverses: false),
                            value: isAnimating
                        )
                )
            
            // Content placeholder
            VStack(alignment: .leading, spacing: AppTheme.Spacing.xs) {
                // Title placeholder
                RoundedRectangle(cornerRadius: 4)
                    .fill(AppColors.lightGray)
                    .frame(height: 20)
                
                // Subtitle placeholder
                RoundedRectangle(cornerRadius: 4)
                    .fill(AppColors.lightGray)
                    .frame(width: 120, height: 16)
                
                // Ingredients placeholder
                RoundedRectangle(cornerRadius: 4)
                    .fill(AppColors.lightGray)
                    .frame(width: 100, height: 12)
                
                Spacer()
                
                // Bottom row placeholder
                HStack {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(AppColors.lightGray)
                        .frame(width: 60, height: 16)
                    
                    Spacer()
                    
                    HStack(spacing: AppTheme.Spacing.sm) {
                        Circle()
                            .fill(AppColors.lightGray)
                            .frame(width: 32, height: 32)
                        
                        Circle()
                            .fill(AppColors.lightGray)
                            .frame(width: 32, height: 32)
                    }
                }
            }
            .padding(.vertical, AppTheme.Spacing.xs)
        }
        .padding(AppTheme.Spacing.cardPadding)
        .cardStyle()
        .onAppear {
            isAnimating = true
        }
    }
}

struct CocktailListLoadingView: View {
    var body: some View {
        LazyVStack(spacing: AppTheme.Spacing.cardSpacing) {
            ForEach(0..<6, id: \.self) { _ in
                CocktailLoadingShimmer()
            }
        }
        .padding(AppTheme.Spacing.lg)
    }
}

#Preview {
    VStack(spacing: 20) {
        Text("Single Shimmer")
            .font(AppTheme.Typography.headline)
        
        CocktailLoadingShimmer()
        
        Text("List Shimmer")
            .font(AppTheme.Typography.headline)
        
        ScrollView {
            CocktailListLoadingView()
        }
        .frame(height: 300)
    }
    .padding()
    .background(AppColors.background)
}