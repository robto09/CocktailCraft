import Foundation
import BackgroundTasks
import UIKit
import shared
import Combine

/**
 * Manages background sync operations for keeping cached data fresh.
 * Handles background app refresh and periodic sync tasks.
 */
@MainActor
class BackgroundSyncManager: ObservableObject {
    static let shared = BackgroundSyncManager()
    
    // Background task identifiers
    private static let backgroundRefreshTaskId = "com.cocktailcraft.background-refresh"
    private static let backgroundFetchTaskId = "com.cocktailcraft.background-fetch"
    
    // Sync configuration
    private let syncInterval: TimeInterval = 3600 // 1 hour
    private let maxBackgroundTime: TimeInterval = 25 // 25 seconds (iOS gives ~30s)
    
    // Dependencies
    private let networkMonitor = NetworkMonitor.shared
    private var cancellables = Set<AnyCancellable>()
    
    // Sync state
    @Published var lastBackgroundSync: Date?
    @Published var backgroundSyncEnabled = true
    @Published var syncInProgress = false
    
    // UserDefaults keys
    private let lastSyncKey = "last_background_sync"
    private let syncEnabledKey = "background_sync_enabled"
    
    private init() {
        loadSettings()
        setupNotificationObservers()
    }
    
    // MARK: - Public Interface
    
    /**
     * Register background tasks with the system.
     * Call this during app launch.
     */
    func registerBackgroundTasks() {
        // Register background app refresh task
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: Self.backgroundRefreshTaskId,
            using: nil
        ) { [weak self] task in
            self?.handleBackgroundRefresh(task: task as! BGAppRefreshTask)
        }
        
        // Register background fetch task
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: Self.backgroundFetchTaskId,
            using: nil
        ) { [weak self] task in
            self?.handleBackgroundFetch(task: task as! BGProcessingTask)
        }
        
        print("BackgroundSyncManager: Background tasks registered")
    }
    
    /**
     * Schedule the next background sync.
     * Call this when app enters background.
     */
    func scheduleBackgroundSync() {
        guard backgroundSyncEnabled else {
            print("BackgroundSyncManager: Background sync disabled")
            return
        }
        
        // Cancel existing requests
        BGTaskScheduler.shared.cancelAllTaskRequests()
        
        // Schedule background app refresh (for quick updates)
        let refreshRequest = BGAppRefreshTaskRequest(identifier: Self.backgroundRefreshTaskId)
        refreshRequest.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60) // 15 minutes
        
        // Schedule background fetch (for comprehensive sync)
        let fetchRequest = BGProcessingTaskRequest(identifier: Self.backgroundFetchTaskId)
        fetchRequest.earliestBeginDate = Date(timeIntervalSinceNow: syncInterval)
        fetchRequest.requiresNetworkConnectivity = true
        fetchRequest.requiresExternalPower = false
        
        do {
            try BGTaskScheduler.shared.submit(refreshRequest)
            try BGTaskScheduler.shared.submit(fetchRequest)
            print("BackgroundSyncManager: Background tasks scheduled")
        } catch {
            print("BackgroundSyncManager: Failed to schedule background tasks: \(error)")
        }
    }
    
    /**
     * Perform immediate sync if conditions are met.
     */
    func performImmediateSync() async {
        guard !syncInProgress else {
            print("BackgroundSyncManager: Sync already in progress")
            return
        }
        
        guard networkMonitor.isConnected else {
            print("BackgroundSyncManager: No network connection for sync")
            return
        }
        
        await performSync(isBackground: false)
    }
    
    /**
     * Toggle background sync on/off.
     */
    func toggleBackgroundSync() {
        backgroundSyncEnabled.toggle()
        UserDefaults.standard.set(backgroundSyncEnabled, forKey: syncEnabledKey)
        
        if backgroundSyncEnabled {
            scheduleBackgroundSync()
        } else {
            BGTaskScheduler.shared.cancelAllTaskRequests()
        }
        
        print("BackgroundSyncManager: Background sync \(backgroundSyncEnabled ? "enabled" : "disabled")")
    }
    
    // MARK: - Private Implementation
    
    private func loadSettings() {
        backgroundSyncEnabled = UserDefaults.standard.bool(forKey: syncEnabledKey)
        if let lastSync = UserDefaults.standard.object(forKey: lastSyncKey) as? Date {
            lastBackgroundSync = lastSync
        }
    }
    
    private func setupNotificationObservers() {
        // Listen for app lifecycle events
        NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)
            .sink { [weak self] _ in
                Task { @MainActor in
                    self?.scheduleBackgroundSync()
                }
            }
            .store(in: &cancellables)
        
        NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)
            .sink { [weak self] _ in
                Task { @MainActor in
                    await self?.checkForForegroundSync()
                }
            }
            .store(in: &cancellables)
    }
    
    private func handleBackgroundRefresh(task: BGAppRefreshTask) {
        print("BackgroundSyncManager: Handling background refresh")
        
        // Schedule next refresh
        scheduleBackgroundSync()
        
        // Perform quick sync
        Task {
            await performSync(isBackground: true, maxDuration: 25)
            task.setTaskCompleted(success: true)
        }
        
        // Set expiration handler
        task.expirationHandler = {
            print("BackgroundSyncManager: Background refresh expired")
            task.setTaskCompleted(success: false)
        }
    }
    
    private func handleBackgroundFetch(task: BGProcessingTask) {
        print("BackgroundSyncManager: Handling background fetch")
        
        // Schedule next fetch
        scheduleBackgroundSync()
        
        // Perform comprehensive sync
        Task {
            await performSync(isBackground: true, maxDuration: 25)
            task.setTaskCompleted(success: true)
        }
        
        // Set expiration handler
        task.expirationHandler = {
            print("BackgroundSyncManager: Background fetch expired")
            task.setTaskCompleted(success: false)
        }
    }
    
    private func performSync(isBackground: Bool, maxDuration: TimeInterval = 30) async {
        let startTime = Date()
        syncInProgress = true
        
        defer {
            syncInProgress = false
            lastBackgroundSync = Date()
            UserDefaults.standard.set(lastBackgroundSync, forKey: lastSyncKey)
        }
        
        print("BackgroundSyncManager: Starting \(isBackground ? "background" : "foreground") sync")
        
        // Get shared ViewModels for sync operations
        let koinHelper = getSharedKoinHelper()
        let homeViewModel = koinHelper.getSharedHomeViewModel()
        let offlineViewModel = koinHelper.getSharedOfflineModeViewModel()

        // Check if we have enough time remaining
        let timeElapsed = Date().timeIntervalSince(startTime)
        guard timeElapsed < maxDuration - 5 else {
            print("BackgroundSyncManager: Time limit approaching, stopping sync")
            return
        }

        // Sync cocktails data (priority: high)
        try? await homeViewModel.loadCocktails()

        // Check time again
        let timeElapsed2 = Date().timeIntervalSince(startTime)
        guard timeElapsed2 < maxDuration - 3 else {
            print("BackgroundSyncManager: Time limit approaching after cocktails sync")
            return
        }

        // Sync cached data (priority: medium)
        try? await offlineViewModel.syncCachedData()

        print("BackgroundSyncManager: Sync completed successfully in \(Date().timeIntervalSince(startTime))s")
    }
    
    private func checkForForegroundSync() async {
        // Check if we should sync when app comes to foreground
        guard let lastSync = lastBackgroundSync else {
            // No previous sync, perform one
            await performImmediateSync()
            return
        }
        
        // Sync if last sync was more than 30 minutes ago
        if Date().timeIntervalSince(lastSync) > 1800 {
            await performImmediateSync()
        }
    }
}

// MARK: - Helper Extensions

extension BackgroundSyncManager {
    var timeSinceLastSync: String {
        guard let lastSync = lastBackgroundSync else {
            return "Never"
        }
        
        let interval = Date().timeIntervalSince(lastSync)
        
        if interval < 60 {
            return "Just now"
        } else if interval < 3600 {
            let minutes = Int(interval / 60)
            return "\(minutes) minute\(minutes == 1 ? "" : "s") ago"
        } else if interval < 86400 {
            let hours = Int(interval / 3600)
            return "\(hours) hour\(hours == 1 ? "" : "s") ago"
        } else {
            let days = Int(interval / 86400)
            return "\(days) day\(days == 1 ? "" : "s") ago"
        }
    }
    
    var nextScheduledSync: String {
        guard backgroundSyncEnabled else {
            return "Disabled"
        }
        
        guard let lastSync = lastBackgroundSync else {
            return "Soon"
        }
        
        let nextSync = lastSync.addingTimeInterval(syncInterval)
        let timeUntilNext = nextSync.timeIntervalSinceNow
        
        if timeUntilNext <= 0 {
            return "Soon"
        } else if timeUntilNext < 3600 {
            let minutes = Int(timeUntilNext / 60)
            return "In \(minutes) minute\(minutes == 1 ? "" : "s")"
        } else {
            let hours = Int(timeUntilNext / 3600)
            return "In \(hours) hour\(hours == 1 ? "" : "s")"
        }
    }
}
