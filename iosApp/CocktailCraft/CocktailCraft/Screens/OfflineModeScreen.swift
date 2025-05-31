import SwiftUI
import shared

struct OfflineModeScreen: View {
    @ObservedObject private var offlineModeViewModel = ViewModelProvider.shared.offlineModeViewModel
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var navigationCoordinator: NavigationCoordinator
    
    @State private var showClearCacheAlert = false
    @State private var animatedIndices = Set<Int>()
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    networkStatusSection
                    offlineModeToggleSection
                    cacheInfoSection
                    recentlyViewedSection
                }
                .padding(.vertical)
            }
            .navigationTitle("Offline Mode")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
            .alert("Clear Offline Cache", isPresented: $showClearCacheAlert) {
                Button("Cancel", role: .cancel) { }
                Button("Clear Cache", role: .destructive) {
                    offlineModeViewModel.clearCache()
                }
            } message: {
                Text("Are you sure you want to clear all cached cocktails? You won't be able to view them offline.")
            }
        }
    }
    
    private var networkStatusSection: some View {
        NetworkStatusCard(isNetworkAvailable: offlineModeViewModel.isNetworkAvailable)
            .padding(.horizontal)
    }
    
    private var offlineModeToggleSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Image(systemName: "airplane")
                    .font(.system(size: 24))
                    .foregroundColor(offlineModeViewModel.isOfflineModeEnabled ? .blue : .gray)
                
                Text("Offline Mode")
                    .font(.system(size: 18, weight: .bold))
                
                Spacer()
                
                Toggle("", isOn: Binding(
                    get: { offlineModeViewModel.isOfflineModeEnabled },
                    set: { _ in offlineModeViewModel.toggleOfflineMode() }
                ))
                .labelsHidden()
            }
            
            Text("When enabled, the app will only use cached data and won't make network requests.")
                .font(.system(size: 14))
                .foregroundColor(.secondary)
        }
        .padding()
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
        .padding(.horizontal)
    }
    
    private var cacheInfoSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Image(systemName: "externaldrive")
                    .font(.system(size: 24))
                    .foregroundColor(.blue)
                
                Text("Cached Cocktails")
                    .font(.system(size: 18, weight: .bold))
            }
            
            HStack {
                Text("Cocktails available offline:")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
                
                Spacer()
                
                Text("\(offlineModeViewModel.recentlyViewedCocktails.count)")
                    .font(.system(size: 14, weight: .bold))
            }
            
            clearCacheButton
        }
        .padding()
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
        .padding(.horizontal)
    }
    
    private var clearCacheButton: some View {
        Button(action: {
            showClearCacheAlert = true
        }) {
            HStack {
                Image(systemName: "trash")
                Text("Clear Cache")
            }
            .font(.system(size: 16, weight: .medium))
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(Color.red.opacity(0.9))
            .cornerRadius(8)
        }
    }
    
    private var recentlyViewedSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "clock.arrow.circlepath")
                    .font(.system(size: 24))
                    .foregroundColor(.blue)
                
                Text("Recently Viewed")
                    .font(.system(size: 18, weight: .bold))
            }
            .padding(.horizontal)
            
            if offlineModeViewModel.recentlyViewedCocktails.isEmpty {
                EmptyRecentlyViewedView {
                    dismiss()
                    navigationCoordinator.selectedTab = .home
                }
                .padding(.horizontal)
            } else {
                cocktailsList
            }
        }
        .padding(.vertical)
    }
    
    @ViewBuilder
    private var cocktailsList: some View {
        if offlineModeViewModel.recentlyViewedCocktails.isEmpty {
            EmptyView()
        } else {
            VStack(spacing: 16) {
                ForEach(offlineModeViewModel.recentlyViewedCocktails.indices, id: \.self) { index in
                    cocktailItemView(at: index)
                }
            }
        }
    }
    
    @ViewBuilder
    private func cocktailItemView(at index: Int) -> some View {
        let cocktail = offlineModeViewModel.recentlyViewedCocktails[index]
        AnimatedCocktailItem(
            cocktail: cocktail,
            index: index,
            isFavorite: false,
            onFavoriteToggle: { },
            onAddToCart: { },
            onTap: {
                dismiss()
                navigationCoordinator.navigateToCocktailDetail(cocktailId: cocktail.id)
            }
        )
        .padding(.horizontal)
        .onAppear {
            withAnimation(.easeOut(duration: 0.3).delay(Double(index) * 0.05)) {
                _ = animatedIndices.insert(index)
            }
        }
        .onDisappear {
            animatedIndices.remove(index)
        }
    }
}

struct NetworkStatusCard: View {
    let isNetworkAvailable: Bool
    
    var body: some View {
        HStack {
            Image(systemName: isNetworkAvailable ? "wifi" : "wifi.slash")
                .font(.system(size: 28))
                .foregroundColor(.white)
            
            Text(isNetworkAvailable ? "Network Available" : "Network Unavailable")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(isNetworkAvailable ? Color.green : Color.red.opacity(0.9))
        .cornerRadius(12)
    }
}

struct EmptyRecentlyViewedView: View {
    let onGoToHome: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "info.circle")
                .font(.system(size: 48))
                .foregroundColor(.gray)
            
            Text("No recently viewed cocktails")
                .font(.system(size: 16))
                .foregroundColor(.secondary)
            
            Text("Browse cocktails in the Home screen to cache them for offline viewing")
                .font(.system(size: 14))
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            Button(action: onGoToHome) {
                HStack {
                    Image(systemName: "house")
                    Text("Go to Home")
                }
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.white)
                .padding(.horizontal, 24)
                .padding(.vertical, 12)
                .background(Color.blue)
                .cornerRadius(8)
            }
            .padding(.top, 8)
        }
        .padding(.vertical, 32)
    }
}