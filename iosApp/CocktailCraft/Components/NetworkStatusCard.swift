import SwiftUI

struct NetworkStatusCard: View {
    let isNetworkAvailable: Bool
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(spacing: AppTheme.Spacing.md) {
            HStack {
                Image(systemName: isNetworkAvailable ? "wifi" : "wifi.slash")
                    .foregroundColor(.white)
                    .font(.title2)
                
                Text(isNetworkAvailable ? "Network Available" : "Network Unavailable")
                    .font(AppTheme.Typography.headline)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                
                Spacer()
            }
            .padding(AppTheme.Spacing.lg)
            .background(
                RoundedRectangle(cornerRadius: AppTheme.CornerRadius.md)
                    .fill(isNetworkAvailable ? AppColors.success : AppColors.error)
            )
        }
        .cardStyle()
    }
}

#Preview {
    VStack(spacing: 20) {
        NetworkStatusCard(isNetworkAvailable: true)
        NetworkStatusCard(isNetworkAvailable: false)
    }
    .environment(\.isDarkMode, false)
    .padding()
}
