import SwiftUI
import shared

// Card subviews for OfflineModeView, split out of the view file so it stays
// focused on state wiring — mirroring Android's offline-screen section files.
// Each card takes plain state values plus action closures; the ViewModel
// stays in OfflineModeView.

// MARK: - Network Status Card

struct NetworkStatusCard: View {
    let isNetworkAvailable: Bool

    var body: some View {
        VStack(spacing: 12) {
            HStack {
                Image(systemName: isNetworkAvailable ? "wifi" : "wifi.slash")
                    .foregroundColor(.white)
                    .font(.title2)

                Text(isNetworkAvailable ? "Network Available" : "Network Unavailable")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(.white)

                Spacer()
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(isNetworkAvailable ? AppColors.success : AppColors.error)
            )
        }
        .cardStyle()
    }
}

// MARK: - Offline Mode Toggle Card

struct OfflineModeToggleCard: View {
    let isOfflineModeEnabled: Bool
    let showRecommendation: Bool
    let onToggle: () -> Void
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Offline Mode Settings")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            VStack(spacing: 12) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Enable Offline Mode")
                            .font(.body)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        Text("Access cached cocktails when offline")
                            .font(.caption)
                            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    }

                    Spacer()

                    Toggle("", isOn: Binding(
                        get: { isOfflineModeEnabled },
                        set: { _ in onToggle() }
                    ))
                    .toggleStyle(SwitchToggleStyle(tint: AppColors.primary(isDarkMode: isDarkMode)))
                }

                if showRecommendation {
                    HStack {
                        Image(systemName: "info.circle")
                            .foregroundColor(AppColors.warning)

                        Text("Offline mode is recommended when network is unavailable")
                            .font(.caption)
                            .foregroundColor(AppColors.warning)
                    }
                    .padding(.top, 8)
                }
            }
        }
        .padding()
        .cardStyle()
    }
}

// MARK: - Cache Information Card

struct CacheInformationCard: View {
    let cacheSizeText: String
    let lastSyncText: String
    let networkStatus: String
    let networkStatusColor: Color
    let offlineModeStatus: String
    let offlineModeColor: Color
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Cache Information")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            VStack(spacing: 12) {
                HStack {
                    Text("Cached Cocktails:")
                        .font(.body)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                    Spacer()

                    Text(cacheSizeText)
                        .font(.body)
                        .fontWeight(.medium)
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                }

                HStack {
                    Text("Last Sync:")
                        .font(.body)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                    Spacer()

                    Text(lastSyncText)
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                }

                HStack {
                    Text("Network Status:")
                        .font(.body)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                    Spacer()

                    Text(networkStatus)
                        .font(.body)
                        .foregroundColor(networkStatusColor)
                }

                HStack {
                    Text("Offline Mode:")
                        .font(.body)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                    Spacer()

                    Text(offlineModeStatus)
                        .font(.body)
                        .foregroundColor(offlineModeColor)
                }
            }
        }
        .padding()
        .cardStyle()
    }
}

// MARK: - Recently Viewed Card

struct RecentlyViewedCard: View {
    let cocktails: [Cocktail]
    @Binding var showingAll: Bool
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Recently Viewed Cocktails")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            if cocktails.isEmpty {
                Text("No recently viewed cocktails")
                    .font(.body)
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .padding(.vertical, 20)
            } else {
                let visibleCocktails = showingAll
                    ? cocktails
                    : Array(cocktails.prefix(6))

                LazyVGrid(columns: [
                    GridItem(.flexible()),
                    GridItem(.flexible())
                ], spacing: 12) {
                    ForEach(visibleCocktails, id: \.id) { cocktail in
                        // Pushes CocktailDetailView via the navigationDestination
                        // on this sheet's NavigationStack, like every other card.
                        NavigationLink(value: cocktail.id) {
                            CocktailGridItem(cocktail: cocktail)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }

                if cocktails.count > 6 {
                    Button(showingAll
                        ? "Show Less"
                        : "View All (\(cocktails.count))") {
                        showingAll.toggle()
                    }
                    .font(.caption)
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                    .padding(.top, 8)
                }
            }
        }
        .padding()
        .cardStyle()
    }
}

// MARK: - Cache Management Card

struct CacheManagementCard: View {
    let isSyncDisabled: Bool
    let isClearDisabled: Bool
    let isLoading: Bool
    let onSyncCache: () -> Void
    let onClearCache: () -> Void
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Cache Management")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            VStack(spacing: 12) {
                PrimaryButton(
                    title: String(localized: "Sync Cache"),
                    icon: "arrow.clockwise",
                    isDisabled: isSyncDisabled,
                    action: onSyncCache
                )

                // Destructive variant: token-based AppColors.error background
                PrimaryButton(
                    title: String(localized: "Clear Cache"),
                    icon: "trash",
                    isDisabled: isClearDisabled,
                    isDestructive: true,
                    action: onClearCache
                )

                if isLoading {
                    HStack {
                        ProgressView()
                            .scaleEffect(0.8)

                        Text("Processing...")
                            .font(.caption)
                            .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    }
                    .padding(.top, 8)
                }
            }
        }
        .padding()
        .cardStyle()
    }
}

// MARK: - Cocktail Grid Item
// Label only — the tap behavior belongs to the enclosing NavigationLink.
struct CocktailGridItem: View {
    let cocktail: Cocktail
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        VStack(spacing: 8) {
            // Kingfisher-backed component: cached thumbnails matter most
            // here, since this grid is the offline surface
            CocktailImageView(
                imageUrl: cocktail.imageUrl,
                height: 80,
                cornerRadius: 8
            )

            Text(cocktail.name)
                .font(.caption)
                .fontWeight(.medium)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                .lineLimit(2)
                .multilineTextAlignment(.center)
        }
    }
}
