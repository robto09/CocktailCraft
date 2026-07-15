import SwiftUI
import shared

struct OfflineModeView: View {
    @State private var viewModel = OfflineModeViewModelSKIE()
    @State private var showingAllRecentlyViewed = false
    @Environment(\.dismiss) private var dismiss
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    // Network Status Card
                    networkStatusCard

                    // Background Sync Card
                    BackgroundSyncCard()

                    // Offline Mode Toggle Card
                    offlineModeToggleCard

                    // Cache Information Card
                    cacheInformationCard

                    // Recently Viewed Cocktails Card
                    if viewModel.hasRecentlyViewed {
                        recentlyViewedCard
                    }

                    // Cache Management Card
                    cacheManagementCard
                }
                .padding()
            }
            .background(AppColors.background(isDarkMode: isDarkMode))
            .navigationTitle("Offline Mode")
            .brandedNavigationBar()
            .navigationDestination(for: String.self) { cocktailId in
                CocktailDetailView(cocktailId: cocktailId)
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                    .tint(.white)
                }
            }
        }
        .task {
            await viewModel.loadRecentlyViewedCocktails()
        }
        .sharedErrorAlert(viewModel.error, clear: { viewModel.clearError() })
    }
    
    // MARK: - Network Status Card
    private var networkStatusCard: some View {
        VStack(spacing: 12) {
            HStack {
                Image(systemName: viewModel.state.isNetworkAvailable ? "wifi" : "wifi.slash")
                    .foregroundColor(.white)
                    .font(.title2)
                
                Text(viewModel.state.isNetworkAvailable ? "Network Available" : "Network Unavailable")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                
                Spacer()
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(viewModel.state.isNetworkAvailable ? AppColors.success : AppColors.error)
            )
        }
        .cardStyle()
    }
    
    // MARK: - Offline Mode Toggle Card
    private var offlineModeToggleCard: some View {
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
                        get: { viewModel.state.isOfflineModeEnabled },
                        set: { _ in
                            Task {
                                await viewModel.toggleOfflineMode()
                            }
                        }
                    ))
                    .toggleStyle(SwitchToggleStyle(tint: AppColors.primary(isDarkMode: isDarkMode)))
                }
                
                if viewModel.shouldShowOfflineRecommendation() {
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
    
    // MARK: - Cache Information Card
    private var cacheInformationCard: some View {
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

                    Text(viewModel.getCacheSizeText())
                        .font(.body)
                        .fontWeight(.medium)
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
                }

                HStack {
                    Text("Last Sync:")
                        .font(.body)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                    Spacer()

                    Text(viewModel.formatLastSyncTime())
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                }

                HStack {
                    Text("Network Status:")
                        .font(.body)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                    Spacer()

                    Text(viewModel.networkStatus)
                        .font(.body)
                        .foregroundColor(viewModel.getNetworkStatusColor())
                }

                HStack {
                    Text("Offline Mode:")
                        .font(.body)
                        .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                    
                    Spacer()
                    
                    Text(viewModel.offlineModeStatus)
                        .font(.body)
                        .foregroundColor(viewModel.getOfflineModeColor())
                }
            }
        }
        .padding()
        .cardStyle()
    }

    // MARK: - Recently Viewed Card
    private var recentlyViewedCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Recently Viewed Cocktails")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            if viewModel.state.recentlyViewedCocktails.isEmpty {
                Text("No recently viewed cocktails")
                    .font(.body)
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                    .padding(.vertical, 20)
            } else {
                let allRecentlyViewed = viewModel.state.recentlyViewedCocktails
                let visibleCocktails = showingAllRecentlyViewed
                    ? allRecentlyViewed
                    : Array(allRecentlyViewed.prefix(6))

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

                if allRecentlyViewed.count > 6 {
                    Button(showingAllRecentlyViewed
                        ? "Show Less"
                        : "View All (\(allRecentlyViewed.count))") {
                        showingAllRecentlyViewed.toggle()
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

    // MARK: - Cache Management Card
    private var cacheManagementCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Cache Management")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            VStack(spacing: 12) {
                // Sync Cache Button
                Button(action: {
                    Task {
                        await viewModel.syncCachedData()
                    }
                }) {
                    HStack {
                        Image(systemName: "arrow.clockwise")
                            .foregroundColor(.white)

                        Text("Sync Cache")
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundColor(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(AppColors.primary(isDarkMode: isDarkMode))
                    .cornerRadius(8)
                }
                .disabled(!viewModel.state.isNetworkAvailable || viewModel.state.isLoading)

                // Clear Cache Button
                Button(action: {
                    Task {
                        await viewModel.clearCache()
                    }
                }) {
                    HStack {
                        Image(systemName: "trash")
                            .foregroundColor(.white)

                        Text("Clear Cache")
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundColor(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(AppColors.error)
                    .cornerRadius(8)
                }
                .disabled(viewModel.getCachedCocktailCount() == 0 || viewModel.state.isLoading)

                if viewModel.state.isLoading {
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
private struct CocktailGridItem: View {
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
