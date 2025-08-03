import SwiftUI

struct ToggleCard: View {
    let title: String
    let toggleTitle: String
    let toggleSubtitle: String
    let isToggled: Bool
    let onToggle: () -> Void
    let showRecommendation: Bool
    let recommendationText: String
    @Environment(\.isDarkMode) var isDarkMode
    
    init(
        title: String,
        toggleTitle: String,
        toggleSubtitle: String,
        isToggled: Bool,
        onToggle: @escaping () -> Void,
        showRecommendation: Bool = false,
        recommendationText: String = ""
    ) {
        self.title = title
        self.toggleTitle = toggleTitle
        self.toggleSubtitle = toggleSubtitle
        self.isToggled = isToggled
        self.onToggle = onToggle
        self.showRecommendation = showRecommendation
        self.recommendationText = recommendationText
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.lg) {
            Text(title)
                .font(AppTheme.Typography.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
            
            VStack(spacing: AppTheme.Spacing.md) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(toggleTitle)
                            .font(AppTheme.Typography.body)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        Text(toggleSubtitle)
                            .font(AppTheme.Typography.caption)
                            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    }
                    
                    Spacer()
                    
                    Toggle("", isOn: Binding(
                        get: { isToggled },
                        set: { _ in onToggle() }
                    ))
                    .toggleStyle(SwitchToggleStyle(tint: AppColors.primary(isDarkMode: isDarkMode)))
                }
                
                if showRecommendation {
                    RecommendationBanner(text: recommendationText)
                }
            }
        }
        .padding(AppTheme.Spacing.lg)
        .cardStyle()
    }
}

struct RecommendationBanner: View {
    let text: String
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        HStack {
            Image(systemName: "info.circle")
                .foregroundColor(AppColors.warning)
            
            Text(text)
                .font(AppTheme.Typography.caption)
                .foregroundColor(AppColors.warning)
        }
        .padding(.top, AppTheme.Spacing.sm)
    }
}

struct CacheManagementCard: View {
    let isNetworkAvailable: Bool
    let cachedItemCount: Int
    let isLoading: Bool
    let onSyncCache: () -> Void
    let onClearCache: () -> Void
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.lg) {
            Text("Cache Management")
                .font(AppTheme.Typography.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            VStack(spacing: AppTheme.Spacing.md) {
                // Sync Cache Button
                Button(action: onSyncCache) {
                    HStack {
                        Image(systemName: "arrow.clockwise")
                            .foregroundColor(.white)

                        Text("Sync Cache")
                            .font(AppTheme.Typography.body)
                            .fontWeight(.medium)
                            .foregroundColor(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(AppTheme.Spacing.lg)
                    .background(AppColors.primary(isDarkMode: isDarkMode))
                    .cornerRadius(AppTheme.CornerRadius.button)
                }
                .disabled(!isNetworkAvailable || isLoading)

                // Clear Cache Button
                Button(action: onClearCache) {
                    HStack {
                        Image(systemName: "trash")
                            .foregroundColor(.white)

                        Text("Clear Cache")
                            .font(AppTheme.Typography.body)
                            .fontWeight(.medium)
                            .foregroundColor(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(AppTheme.Spacing.lg)
                    .background(AppColors.error)
                    .cornerRadius(AppTheme.CornerRadius.button)
                }
                .disabled(cachedItemCount == 0 || isLoading)

                if isLoading {
                    HStack {
                        ProgressView()
                            .scaleEffect(0.8)
                            .tint(AppColors.primary(isDarkMode: isDarkMode))

                        Text("Processing...")
                            .font(AppTheme.Typography.caption)
                            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    }
                    .padding(.top, AppTheme.Spacing.sm)
                }
            }
        }
        .padding(AppTheme.Spacing.lg)
        .cardStyle()
    }
}

#Preview {
    VStack(spacing: 20) {
        ToggleCard(
            title: "Offline Mode Settings",
            toggleTitle: "Enable Offline Mode",
            toggleSubtitle: "Access cached cocktails when offline",
            isToggled: true,
            onToggle: {},
            showRecommendation: true,
            recommendationText: "Offline mode is recommended when network is unavailable"
        )
        
        CacheManagementCard(
            isNetworkAvailable: true,
            cachedItemCount: 25,
            isLoading: false,
            onSyncCache: {},
            onClearCache: {}
        )
    }
    .environment(\.isDarkMode, false)
    .padding()
}
