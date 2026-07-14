import Foundation
import BackgroundTasks
import UIKit
import shared
import Combine
import Observation
import os

/**
 * Manages background sync operations for keeping cached data fresh.
 * Handles background app refresh and periodic sync tasks.
 */
@MainActor
@Observable
class BackgroundSyncManager {
    static let shared = BackgroundSyncManager()

    // os.Logger instead of print(): level-gated, never spams the release
    // console (IO-9).
    @ObservationIgnored
    private let logger = Logger(subsystem: "com.cocktailcraft", category: "BackgroundSync")
    
    // Background task identifiers
    private static let backgroundRefreshTaskId = "com.cocktailcraft.background-refresh"
    private static let backgroundFetchTaskId = "com.cocktailcraft.background-fetch"
    
    // Sync configuration
    private let syncInterval: TimeInterval = 3600 // 1 hour
    private let maxBackgroundTime: TimeInterval = 25 // 25 seconds (iOS gives ~30s)
    
    // Dependencies
    private let networkMonitor = NetworkMonitor.shared
    @ObservationIgnored private var cancellables = Set<AnyCancellable>()

    // Sync state
    var lastBackgroundSync: Date?
    var backgroundSyncEnabled = true
    var syncInProgress = false
    
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
            // Guard, don't force-cast: an identifier/type misconfiguration
            // must fail the task, not crash the app (IO-10).
            guard let refreshTask = task as? BGAppRefreshTask else {
                task.setTaskCompleted(success: false)
                return
            }
            self?.handleBackgroundRefresh(task: refreshTask)
        }
        
        // Register background fetch task
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: Self.backgroundFetchTaskId,
            using: nil
        ) { [weak self] task in
            guard let fetchTask = task as? BGProcessingTask else {
                task.setTaskCompleted(success: false)
                return
            }
            self?.handleBackgroundFetch(task: fetchTask)
        }
        
        logger.info("Background tasks registered")
    }
    
    /**
     * Schedule the next background sync.
     * Call this when app enters background.
     */
    func scheduleBackgroundSync() {
        guard backgroundSyncEnabled else {
            logger.debug("Background sync disabled")
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
            logger.info("Background tasks scheduled")
        } catch {
            logger.error("Failed to schedule background tasks: \(error)")
        }
    }
    
    /**
     * Perform immediate sync if conditions are met.
     */
    func performImmediateSync() async {
        guard !syncInProgress else {
            logger.debug("Sync already in progress")
            return
        }
        
        guard networkMonitor.isConnected else {
            logger.debug("No network connection for sync")
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
        
        logger.info("Background sync \(self.backgroundSyncEnabled ? "enabled" : "disabled", privacy: .public)")
    }
    
    // MARK: - Private Implementation
    
    private func loadSettings() {
        // bool(forKey:) returns false for an absent key, which silently
        // disabled background sync on every fresh install — only apply the
        // stored value once the user has actually toggled the setting.
        if UserDefaults.standard.object(forKey: syncEnabledKey) != nil {
            backgroundSyncEnabled = UserDefaults.standard.bool(forKey: syncEnabledKey)
        }
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
        logger.info("Handling background refresh")
        runBudgetedSync(for: task)
    }

    private func handleBackgroundFetch(task: BGProcessingTask) {
        logger.info("Handling background fetch")
        runBudgetedSync(for: task)
    }

    private func runBudgetedSync(for task: BGTask) {
        // Schedule the next sync window first — if this run is expired or
        // killed, the follow-up request already exists.
        scheduleBackgroundSync()

        // The expiration handler must be attached before any sync work
        // starts; the closure captures `syncWork` by reference so it sees
        // the Task assigned below. Expiration cancels the sync (SKIE
        // propagates the cancellation into the shared coroutine) and the
        // `isCancelled` guard keeps the normal path from completing the
        // task a second time.
        var syncWork: Task<Void, Never>?
        task.expirationHandler = { [logger] in
            logger.notice("Background task expired")
            syncWork?.cancel()
            task.setTaskCompleted(success: false)
        }

        syncWork = Task {
            await performSync(isBackground: true, maxDuration: maxBackgroundTime)
            if !Task.isCancelled {
                task.setTaskCompleted(success: true)
            }
        }
    }
    
    /// Millisecond time budget handed to the shared BackgroundSyncService:
    /// the platform allowance minus a 5s margin so completion/teardown lands
    /// before iOS's deadline, floored at zero so a sub-margin allowance can
    /// never go negative. Pure and nonisolated so unit tests can call it
    /// directly (IO-7).
    nonisolated static func syncBudgetMs(maxDuration: TimeInterval) -> Int64 {
        max(0, Int64((maxDuration - 5) * 1000))
    }

    private func performSync(isBackground: Bool, maxDuration: TimeInterval = 30) async {
        let startTime = Date()
        syncInProgress = true
        
        defer {
            syncInProgress = false
            lastBackgroundSync = Date()
            UserDefaults.standard.set(lastBackgroundSync, forKey: lastSyncKey)
        }
        
        logger.info("Starting \(isBackground ? "background" : "foreground", privacy: .public) sync")

        // What "a sync" means lives in shared code (BackgroundSyncService,
        // including the offline check and time budget); this class only
        // decides when to run it and mirrors sync state for the UI.
        let syncService = getSharedKoinHelper().getBackgroundSyncService()
        let budgetMs = Self.syncBudgetMs(maxDuration: maxDuration)
        let success = (try? await syncService.performSync(maxDurationMs: budgetMs))?.boolValue ?? false

        logger.info("Sync \(success ? "completed" : "skipped/failed", privacy: .public) in \(Date().timeIntervalSince(startTime))s")
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
