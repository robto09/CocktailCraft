import SwiftUI
import shared

struct OfflineModeStatusBar: View {
    let coordinator: OfflineModeCoordinator
    @State private var syncState: OfflineModeCoordinator.SyncState = .Idle
    @State private var showingError = false
    @State private var errorMessage = ""
    
    var body: some View {
        VStack(spacing: 0) {
            if !NetworkMonitor.shared.isOnline.value {
                offlineBar
            } else if case .Syncing = syncState {
                syncingBar
            } else if case .Error = syncState {
                errorBar
            }
        }
        .animation(.easeInOut, value: syncState)
        .animation(.easeInOut, value: NetworkMonitor.shared.isOnline.value)
        .alert("Sync Error", isPresented: $showingError) {
            Button("OK") {}
            Button("Retry") {
                sync()
            }
        } message: {
            Text(errorMessage)
        }
        .onAppear {
            observeSyncState()
        }
    }
    
    private var offlineBar: some View {
        HStack(spacing: 8) {
            Image(systemName: "wifi.slash")
            Text("You're offline")
            Spacer()
            Button(action: sync) {
                Label("Sync", systemImage: "arrow.clockwise")
                    .font(.caption)
            }
            .disabled(true)
        }
        .padding(8)
        .background(Color(.systemGray6))
        .transition(.move(edge: .top))
    }
    
    private var syncingBar: some View {
        HStack(spacing: 8) {
            ProgressView()
                .scaleEffect(0.8)
            Text("Syncing data...")
            Spacer()
            Button(action: cancelSync) {
                Label("Cancel", systemImage: "xmark")
                    .font(.caption)
            }
        }
        .padding(8)
        .background(Color(.systemGray6))
        .transition(.move(edge: .top))
    }
    
    private var errorBar: some View {
        HStack(spacing: 8) {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.orange)
            Text("Sync failed")
            Spacer()
            Button(action: sync) {
                Label("Retry", systemImage: "arrow.clockwise")
                    .font(.caption)
            }
        }
        .padding(8)
        .background(Color(.systemGray6))
        .transition(.move(edge: .top))
    }
    
    private func observeSyncState() {
        // Observe sync state changes
        Task {
            for await state in coordinator.syncState {
                syncState = state
                
                if case let .Error(error) = state {
                    errorMessage = error.message ?: "An error occurred during synchronization"
                    showingError = true
                }
            }
        }
    }
    
    private func sync() {
        Task {
            try? await coordinator.synchronizeData()
        }
    }
    
    private func cancelSync() {
        coordinator.cancelSync()
    }
}

// Preview
struct OfflineModeStatusBar_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            // Online, Idle
            OfflineModeStatusBar(coordinator: PreviewOfflineModeCoordinator(initialState: .Idle))
                .previewDisplayName("Online, Idle")
            
            // Offline
            OfflineModeStatusBar(coordinator: PreviewOfflineModeCoordinator(initialState: .Idle))
                .environment(\.isOffline, true)
                .previewDisplayName("Offline")
            
            // Syncing
            OfflineModeStatusBar(coordinator: PreviewOfflineModeCoordinator(initialState: .Syncing))
                .previewDisplayName("Syncing")
            
            // Error
            OfflineModeStatusBar(coordinator: PreviewOfflineModeCoordinator(initialState: .Error(NSError(domain: "", code: 0))))
                .previewDisplayName("Error")
        }
    }
}

// MARK: - Preview Helpers

private class PreviewOfflineModeCoordinator: OfflineModeCoordinator {
    private let initialState: SyncState
    
    init(initialState: SyncState) {
        self.initialState = initialState
        super.init(
            networkMonitor: NetworkMonitor.shared,
            localCache: IosCocktailLocalCache(),
            cocktailRepository: MockCocktailRepository(),
            cartRepository: MockCartRepository(),
            favoritesRepository: MockFavoritesRepository(),
            orderRepository: MockOrderRepository()
        )
    }
    
    override var syncState: StateFlow<SyncState> {
        MutableStateFlow(initialState)
    }
}

private struct IsOfflineKey: EnvironmentKey {
    static let defaultValue = false
}

extension EnvironmentValues {
    var isOffline: Bool {
        get { self[IsOfflineKey.self] }
        set { self[IsOfflineKey.self] = newValue }
    }
}