import SwiftUI

struct CacheInfoCard: View {
    let cacheSize: String
    let lastSyncTime: String
    let networkStatus: String
    let offlineModeStatus: String
    let networkStatusColor: Color
    let offlineModeColor: Color
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.lg) {
            Text("Cache Information")
                .font(AppTheme.Typography.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            VStack(spacing: AppTheme.Spacing.md) {
                InfoRow(
                    label: "Cached Cocktails:",
                    value: cacheSize,
                    valueColor: AppColors.primary(isDarkMode: isDarkMode)
                )

                InfoRow(
                    label: "Last Sync:",
                    value: lastSyncTime,
                    valueColor: AppColors.textSecondary(isDarkMode: isDarkMode)
                )

                InfoRow(
                    label: "Network Status:",
                    value: networkStatus,
                    valueColor: networkStatusColor
                )

                InfoRow(
                    label: "Offline Mode:",
                    value: offlineModeStatus,
                    valueColor: offlineModeColor
                )
            }
        }
        .padding(AppTheme.Spacing.lg)
        .cardStyle()
    }
}

struct InfoRow: View {
    let label: String
    let value: String
    let valueColor: Color
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        HStack {
            Text(label)
                .font(AppTheme.Typography.body)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            Spacer()

            Text(value)
                .font(AppTheme.Typography.body)
                .fontWeight(.medium)
                .foregroundColor(valueColor)
        }
    }
}

#Preview {
    CacheInfoCard(
        cacheSize: "25 cocktails",
        lastSyncTime: "2 hours ago",
        networkStatus: "Connected",
        offlineModeStatus: "Enabled",
        networkStatusColor: .green,
        offlineModeColor: .blue
    )
    .environment(\.isDarkMode, false)
    .padding()
}
