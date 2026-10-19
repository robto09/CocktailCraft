import Foundation
import shared
import Observation

/// Observation bridge over the shared KMP NetworkMonitor.
///
/// Connectivity has a single source of truth in shared code (an
/// NWPathMonitor lives in shared's iosMain implementation and feeds the
/// same StateFlow the shared ViewModels consume); this class only mirrors
/// that flow into an Observation-tracked property for SwiftUI. (Not named
/// NetworkMonitor: that would shadow the exported shared.NetworkMonitor.)
@MainActor
@Observable
final class ConnectivityObserver {
    static let shared = ConnectivityObserver()

    private(set) var isConnected = true

    @ObservationIgnored private var observationTask: Task<Void, Never>?

    private init() {
        let monitor = getSharedKoinHelper().getNetworkMonitor()
        isConnected = monitor.isOnline.value.boolValue
        observationTask = Task { [weak self] in
            for await online in monitor.isOnline {
                self?.isConnected = online.boolValue
            }
        }
    }

    deinit {
        observationTask?.cancel()
    }
}
