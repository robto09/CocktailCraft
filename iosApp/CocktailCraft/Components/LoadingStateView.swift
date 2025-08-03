import SwiftUI

struct LoadingStateView: View {
    let message: String
    @Environment(\.isDarkMode) var isDarkMode
    
    init(message: String = "Loading...") {
        self.message = message
    }
    
    var body: some View {
        VStack(spacing: AppTheme.Spacing.lg) {
            ProgressView()
                .scaleEffect(1.2)
                .tint(AppColors.primary(isDarkMode: isDarkMode))
            
            Text(message)
                .font(AppTheme.Typography.body)
                .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppColors.background(isDarkMode: isDarkMode))
    }
}

#Preview {
    LoadingStateView(message: "Loading cocktail details...")
        .environment(\.isDarkMode, false)
}
