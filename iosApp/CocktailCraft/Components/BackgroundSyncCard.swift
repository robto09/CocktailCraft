import SwiftUI

struct BackgroundSyncCard: View {
    private let backgroundSyncManager = BackgroundSyncManager.shared
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.lg) {
            // Header
            HStack {
                Image(systemName: "arrow.clockwise.circle.fill")
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                    .font(.title2)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text("Background Sync")
                        .font(AppTheme.Typography.headline)
                        .fontWeight(.bold)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    
                    Text("Keep your cache fresh automatically")
                        .font(AppTheme.Typography.caption)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                }
                
                Spacer()
                
                // Enable/Disable Toggle
                Toggle("", isOn: Binding(
                    get: { backgroundSyncManager.backgroundSyncEnabled },
                    set: { _ in backgroundSyncManager.toggleBackgroundSync() }
                ))
                .toggleStyle(SwitchToggleStyle(tint: AppColors.primary(isDarkMode: isDarkMode)))
            }
            
            if backgroundSyncManager.backgroundSyncEnabled {
                Divider()
                    .background(AppColors.lightGray)
                
                // Sync Status Information
                VStack(spacing: AppTheme.Spacing.md) {
                    SyncInfoRow(
                        icon: "clock.fill",
                        label: "Last Sync:",
                        value: backgroundSyncManager.timeSinceLastSync,
                        valueColor: syncStatusColor
                    )
                    
                    SyncInfoRow(
                        icon: "calendar.circle.fill",
                        label: "Next Sync:",
                        value: backgroundSyncManager.nextScheduledSync,
                        valueColor: AppColors.textSecondary(isDarkMode: isDarkMode)
                    )
                    
                    SyncInfoRow(
                        icon: "gear.circle.fill",
                        label: "Status:",
                        value: syncStatusText,
                        valueColor: syncStatusColor
                    )
                }
                
                // Manual Sync Button
                if !backgroundSyncManager.syncInProgress {
                    Divider()
                        .background(AppColors.lightGray)
                    
                    Button(action: {
                        Task {
                            await backgroundSyncManager.performImmediateSync()
                        }
                    }) {
                        HStack {
                            Image(systemName: "arrow.clockwise")
                                .foregroundColor(.white)
                            
                            Text("Sync Now")
                                .font(AppTheme.Typography.body)
                                .fontWeight(.medium)
                                .foregroundColor(.white)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, AppTheme.Spacing.sm)
                        .background(AppColors.primary(isDarkMode: isDarkMode))
                        .cornerRadius(AppTheme.CornerRadius.md)
                    }
                    .disabled(!NetworkMonitor.shared.isConnected)
                }
                
                // Sync in Progress Indicator
                if backgroundSyncManager.syncInProgress {
                    Divider()
                        .background(AppColors.lightGray)
                    
                    HStack {
                        ProgressView()
                            .scaleEffect(0.8)
                            .progressViewStyle(CircularProgressViewStyle(tint: AppColors.primary(isDarkMode: isDarkMode)))
                        
                        Text("Syncing...")
                            .font(AppTheme.Typography.body)
                            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        
                        Spacer()
                    }
                    .padding(.vertical, AppTheme.Spacing.sm)
                }
            } else {
                Divider()
                    .background(AppColors.lightGray)
                
                // Disabled State Info
                HStack {
                    Image(systemName: "info.circle")
                        .foregroundColor(.orange)
                    
                    Text("Background sync is disabled. Enable it to keep your cache updated automatically.")
                        .font(AppTheme.Typography.caption)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .multilineTextAlignment(.leading)
                }
                .padding(.vertical, AppTheme.Spacing.xs)
            }
        }
        .padding(AppTheme.Spacing.lg)
        .cardStyle()
    }
    
    // MARK: - Computed Properties
    
    private var syncStatusText: String {
        if backgroundSyncManager.syncInProgress {
            return "Syncing..."
        } else if !NetworkMonitor.shared.isConnected {
            return "Waiting for network"
        } else if backgroundSyncManager.lastBackgroundSync == nil {
            return "Ready to sync"
        } else {
            return "Up to date"
        }
    }
    
    private var syncStatusColor: Color {
        if backgroundSyncManager.syncInProgress {
            return AppColors.primary(isDarkMode: isDarkMode)
        } else if !NetworkMonitor.shared.isConnected {
            return .orange
        } else if backgroundSyncManager.lastBackgroundSync == nil {
            return .blue
        } else {
            return .green
        }
    }
}

// MARK: - Supporting Views

struct SyncInfoRow: View {
    let icon: String
    let label: String
    let value: String
    let valueColor: Color
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                .frame(width: 20)
            
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

// MARK: - Preview

#Preview {
    VStack(spacing: 16) {
        BackgroundSyncCard()
        
        // Preview with sync disabled
        BackgroundSyncCard()
            .onAppear {
                BackgroundSyncManager.shared.backgroundSyncEnabled = false
            }
    }
    .padding()
    .background(Color(.systemBackground))
    .environment(\.isDarkMode, false)
}
