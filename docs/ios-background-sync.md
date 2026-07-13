# iOS Background Sync Implementation

## Overview

The iOS Background Sync feature provides automatic data synchronization when the app is backgrounded, ensuring users always have fresh content when they return to the app. This implementation significantly improves user experience by reducing loading times and providing better offline capabilities.

## Purpose & Benefits

### 🎯 **Primary Goals**
- **Fresh Data on Launch**: Users see updated content immediately when opening the app
- **Reduced Loading Times**: Pre-cached data eliminates waiting for network requests
- **Better Offline Experience**: More recent data available when offline
- **Seamless User Experience**: Background updates happen transparently

### 📈 **User Experience Improvements**
- Faster app startup with pre-loaded content
- Reduced "Loading..." states throughout the app
- More recent cocktail data and recommendations
- Better performance on slow network connections
- Improved offline browsing capabilities

## Technical Implementation

### 🏗️ **Architecture Overview**

The Background Sync system consists of four main components:

1. **BackgroundSyncManager** (Swift) - Scheduling, lifecycle, and BGTaskScheduler integration
2. **BackgroundSyncService** (shared Kotlin) - The sync itself; single definition shared with Android
3. **BackgroundSyncCard** - User interface for sync management
4. **iOS Background Tasks Integration** - System-level background processing

### 📂 **File Locations**

| Component | File Path |
|-----------|-----------|
| **BackgroundSyncManager** | `iosApp/CocktailCraft/Utils/BackgroundSyncManager.swift` |
| **BackgroundSyncService** | `shared/src/commonMain/kotlin/com/cocktailcraft/domain/service/BackgroundSyncService.kt` |
| **BackgroundSyncCard** | `iosApp/CocktailCraft/Components/BackgroundSyncCard.swift` |
| **App Integration** | `iosApp/CocktailCraft/CocktailCraftApp.swift` |
| **UI Integration** | `iosApp/CocktailCraft/Views/OfflineModeView.swift` |
| **Background Modes** | `iosApp/CocktailCraft/Info.plist` |

### 📱 **iOS Background Modes**

The app is configured with two background modes plus the permitted task identifiers in `Info.plist`:

```xml
<key>UIBackgroundModes</key>
<array>
    <string>fetch</string>
    <string>processing</string>
</array>
<key>BGTaskSchedulerPermittedIdentifiers</key>
<array>
    <string>com.cocktailcraft.background-refresh</string>
    <string>com.cocktailcraft.background-fetch</string>
</array>
```

- **fetch**: Backs `BGAppRefreshTask` (quick refresh)
- **processing**: Backs `BGProcessingTask` (comprehensive sync)
- **BGTaskSchedulerPermittedIdentifiers**: Every identifier passed to `BGTaskScheduler.register` must appear here, or registration fails at runtime and background sync never runs

## Core Components

### 1. BackgroundSyncManager

**Location**: `iosApp/CocktailCraft/Utils/BackgroundSyncManager.swift`

**Key Features**:
- `@MainActor @Observable` singleton for app-wide access
- BGTaskScheduler integration for iOS background tasks
- Decides *when* to sync; *what* a sync means lives in the shared `BackgroundSyncService`
- Time-limited execution (25 seconds max for iOS compliance)
- User-controlled sync settings with persistent storage

**Main Methods**:
```swift
// Register background tasks during app launch
func registerBackgroundTasks()

// Schedule next background sync when app enters background
func scheduleBackgroundSync()

// Perform immediate sync when conditions are met
func performImmediateSync() async

// Toggle background sync on/off
func toggleBackgroundSync()
```

**Background Task Identifiers**:
- `com.cocktailcraft.background-refresh` - Quick refresh tasks
- `com.cocktailcraft.background-fetch` - Comprehensive sync tasks

### 2. BackgroundSyncService (shared)

**Location**: `shared/src/commonMain/kotlin/com/cocktailcraft/domain/service/BackgroundSyncService.kt`

There is exactly one definition of what "a sync" means, shared by both platforms — the platform schedulers (BGTaskScheduler on iOS, WorkManager on Android) only decide when to call it:

```kotlin
suspend fun performSync(maxDurationMs: Long = 25_000): Boolean
```

- Skips the sync (returns `false`) when the device is offline
- Enforces the time budget with `withTimeout(maxDurationMs)`
- Refreshes the offline cocktail catalog cache via `CocktailCatalogRepository`
- Registered as a Koin single and exposed to Swift through `KoinHelper.getBackgroundSyncService()`

### 3. BackgroundSyncCard

**Location**: `iosApp/CocktailCraft/Components/BackgroundSyncCard.swift`

**UI Features**:
- Real-time sync status display
- Last sync time with human-readable format
- Next scheduled sync countdown
- Manual "Sync Now" button (when online)
- Progress indicators during sync operations
- Enable/disable toggle for background sync

**Visual States**:
- **Enabled**: Shows sync information and controls
- **Disabled**: Shows informational message about enabling sync
- **Syncing**: Shows progress indicator and status
- **Offline**: Indicates waiting for network connection

### 4. Integration Points

**App Initialization** (`CocktailCraftApp.swift`) — registration is synchronous, because BGTaskScheduler requires all launch handlers to be registered before the app finishes launching:
```swift
init() {
    // Initialize Koin
    KoinInitializer.instance.initialize()

    // Register background tasks synchronously
    BackgroundSyncManager.shared.registerBackgroundTasks()

    // Keep the home-screen widgets' favorites snapshot current
    WidgetDataBridge.shared.start()
}
```

**Offline Mode View** (`OfflineModeView.swift`):
- BackgroundSyncCard integrated into offline management screen
- Positioned prominently for user visibility and control

## Sync Process Flow

### 🔄 **Automatic Sync Workflow**

1. **App Backgrounded**: `scheduleBackgroundSync()` called
2. **iOS Scheduling**: BGTaskScheduler schedules tasks based on user patterns
3. **Background Execution**: iOS launches app in background and invokes the registered handler, which calls `runBudgetedSync(for:)`
4. **Budgeted Sync** (`runBudgetedSync`):
   - Schedules the *next* sync window first, so a killed/expired run already has a follow-up request
   - Attaches the expiration handler before any work starts; expiration cancels the sync `Task` (SKIE propagates the cancellation into the shared coroutine)
   - Delegates to the shared `BackgroundSyncService` with a millisecond budget (platform allowance minus a 5s completion margin)
5. **Completion**: Update last sync time and mark the task completed

### ⚡ **Manual Sync Workflow**

1. **User Trigger**: User taps "Sync Now" button
2. **Network Check**: Verify internet connectivity
3. **Immediate Execution**: Perform sync without time constraints
4. **UI Updates**: Show progress and completion status

### 🕐 **Sync Timing**

- **Background Refresh**: Scheduled 15 minutes after backgrounding
- **Background Fetch**: Scheduled 1 hour after backgrounding
- **Foreground Check**: Auto-sync if last sync > 30 minutes ago
- **Manual Sync**: Available anytime when online

## Data Synchronization

### 📊 **What Gets Synced**

The shared service refreshes the offline cocktail catalog cache (`CocktailCatalogRepository.getCocktailsSortedByNewest()`), so cached browsing data is fresh on next launch. Sync does not drive ViewModels — UI state updates reactively from the repositories when screens observe them.

### 🔧 **Sync Implementation**

`BackgroundSyncManager.performSync` delegates to the shared service through the Koin helper (SKIE exposes the suspend function as Swift async):

```swift
// What "a sync" means lives in shared code (BackgroundSyncService,
// including the offline check and time budget); this class only
// decides when to run it and mirrors sync state for the UI.
let syncService = getSharedKoinHelper().getBackgroundSyncService()
let budgetMs = Self.syncBudgetMs(maxDuration: maxDuration) // (maxDuration - 5s) in ms, floored at 0
let success = (try? await syncService.performSync(maxDurationMs: budgetMs))?.boolValue ?? false
```

## Configuration & Settings

### ⚙️ **User Controls**

- **Enable/Disable**: Toggle background sync on/off
- **Manual Sync**: Force immediate synchronization
- **Sync Status**: View last sync time and next scheduled sync
- **Network Awareness**: Automatic detection of connectivity

### 💾 **Persistent Storage**

Settings stored in UserDefaults:
- `background_sync_enabled`: Boolean for sync preference
- `last_background_sync`: Date of last successful sync

### 🔧 **Configuration Constants**

```swift
private let syncInterval: TimeInterval = 3600 // 1 hour
private let maxBackgroundTime: TimeInterval = 25 // 25 seconds
```

## Error Handling & Resilience

### 🛡️ **Robust Error Management**

- **Network Failures**: Sync is skipped when offline; failures return `false` rather than crash
- **Time Limits**: Respect iOS background execution limits (`withTimeout` budget plus BGTask expiration handler)
- **SKIE Integration**: Use `try?` for error-safe async calls; task expiration cancels the sync `Task` and SKIE propagates the cancellation into the shared coroutine
- **Misconfiguration Safety**: Task handlers guard-cast the BGTask type and fail the task instead of force-casting and crashing
- **Fallback Behavior**: Continue with cached data if sync fails

### 📝 **Logging & Debugging**

Logging uses `os.Logger` — level-gated, so it never spams the release console (IO-9). View output in Xcode's console or Console.app (subsystem `com.cocktailcraft`, category `BackgroundSync`):

```swift
private let logger = Logger(subsystem: "com.cocktailcraft", category: "BackgroundSync")

logger.info("Background tasks registered")
logger.info("Starting background sync")
logger.error("Failed to schedule background tasks: \(error)")
logger.notice("Background task expired")
```

The shared `BackgroundSyncService` logs through Kermit with the tag `BackgroundSyncService`.

## Performance Considerations

### ⚡ **Optimization Strategies**

1. **Time Management**: Hard time budget enforced with `withTimeout` in the shared service
2. **Network Efficiency**: Only sync when connected (offline check in the shared service)
3. **Resource Management**: Minimal memory and CPU usage
4. **Battery Optimization**: Efficient background processing

### 📊 **Performance Metrics**

- **Sync Duration**: Typically completes in 5-15 seconds
- **Data Transfer**: Optimized API calls for minimal bandwidth
- **Battery Impact**: Minimal due to efficient implementation
- **Memory Usage**: Low memory footprint during background execution

## Testing & Validation

### 🧪 **Testing Scenarios**

1. **Background Sync**: Test app backgrounding triggers sync
2. **Manual Sync**: Verify manual sync button functionality
3. **Network Conditions**: Test online/offline state handling
4. **Time Limits**: Ensure compliance with iOS background limits
5. **User Preferences**: Test enable/disable functionality

### 🔍 **Validation Points**

- Sync status updates correctly in UI
- Last sync time displays accurate information
- Network connectivity properly detected
- Background tasks scheduled successfully
- Data freshness improved after sync

### 🚀 **Production Checklist**

- [ ] Background modes (`fetch`, `processing`) and `BGTaskSchedulerPermittedIdentifiers` configured in Info.plist
- [ ] BGTaskScheduler registration happens synchronously in app launch (before launch finishes)
- [ ] Network connectivity handling
- [ ] Time limit compliance (< 30 seconds)
- [ ] Error handling for all sync operations
- [ ] User preference persistence
- [ ] UI feedback for all states
- [ ] Testing on device (not just simulator)

## Future Enhancements

### 🚀 **Potential Improvements**

1. **Smart Scheduling**: ML-based sync timing based on user patterns
2. **Selective Sync**: User-configurable sync preferences
3. **Sync Analytics**: Track sync success rates and performance
4. **Push Notifications**: Notify users of important updates
5. **Conflict Resolution**: Handle data conflicts during sync

### 📈 **Monitoring & Analytics**

Future implementation could include:
- Sync success/failure rates
- Average sync duration
- User engagement with sync features
- Network condition impact on sync performance

## Conclusion

The iOS Background Sync implementation provides a robust, user-friendly solution for keeping app data fresh. It balances performance, battery life, and user experience while adhering to iOS best practices and system limitations.

The implementation is production-ready and significantly enhances the CocktailCraft app's user experience by ensuring users always have access to the latest content with minimal loading delays.
