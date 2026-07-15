import SwiftUI
import shared

/// Offline mode screen: state wiring only — the card subviews live in
/// Components/OfflineModeCards.swift.
struct OfflineModeView: View {
    @State private var viewModel = OfflineModeViewModelSKIE()
    @State private var showingAllRecentlyViewed = false
    @Environment(\.dismiss) private var dismiss
    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    NetworkStatusCard(
                        isNetworkAvailable: viewModel.state.isNetworkAvailable
                    )

                    BackgroundSyncCard()

                    OfflineModeToggleCard(
                        isOfflineModeEnabled: viewModel.state.isOfflineModeEnabled,
                        showRecommendation: viewModel.shouldShowOfflineRecommendation(),
                        onToggle: {
                            Task {
                                await viewModel.toggleOfflineMode()
                            }
                        }
                    )

                    CacheInformationCard(
                        cacheSizeText: viewModel.getCacheSizeText(),
                        lastSyncText: viewModel.formatLastSyncTime(),
                        networkStatus: viewModel.networkStatus,
                        networkStatusColor: viewModel.getNetworkStatusColor(),
                        offlineModeStatus: viewModel.offlineModeStatus,
                        offlineModeColor: viewModel.getOfflineModeColor()
                    )

                    if viewModel.hasRecentlyViewed {
                        RecentlyViewedCard(
                            cocktails: viewModel.state.recentlyViewedCocktails,
                            showingAll: $showingAllRecentlyViewed
                        )
                    }

                    CacheManagementCard(
                        isSyncDisabled: !viewModel.state.isNetworkAvailable || viewModel.state.isLoading,
                        isClearDisabled: viewModel.getCachedCocktailCount() == 0 || viewModel.state.isLoading,
                        isLoading: viewModel.state.isLoading,
                        onSyncCache: {
                            Task {
                                await viewModel.syncCachedData()
                            }
                        },
                        onClearCache: {
                            Task {
                                await viewModel.clearCache()
                            }
                        }
                    )
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
}
