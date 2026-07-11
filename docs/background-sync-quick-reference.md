# Background Sync Quick Reference

## 🚀 Quick Start

### Enable Background Sync
```swift
// Background sync is automatically initialized in CocktailCraftApp.swift
// Users can toggle it in the OfflineModeView
```

### Manual Sync
```swift
// Trigger immediate sync
await BackgroundSyncManager.shared.performImmediateSync()
```

### Check Sync Status
```swift
let manager = BackgroundSyncManager.shared
print("Last sync: \(manager.timeSinceLastSync)")
print("Next sync: \(manager.nextScheduledSync)")
print("Sync enabled: \(manager.backgroundSyncEnabled)")
```

## 📁 File Locations

| Component | File Path |
|-----------|-----------|
| **BackgroundSyncManager** | `iosApp/CocktailCraft/Utils/BackgroundSyncManager.swift` |
| **BackgroundSyncCard** | `iosApp/CocktailCraft/Components/BackgroundSyncCard.swift` |
| **App Integration** | `iosApp/CocktailCraft/CocktailCraftApp.swift` |
| **UI Integration** | `iosApp/CocktailCraft/Views/OfflineModeView.swift` |
| **Background Modes** | `iosApp/CocktailCraft/Info.plist` |

## 🔧 Key Configuration

### Background Task IDs
```swift
private static let backgroundRefreshTaskId = "com.cocktailcraft.background-refresh"
private static let backgroundFetchTaskId = "com.cocktailcraft.background-fetch"
```

### Timing Configuration
```swift
private let syncInterval: TimeInterval = 3600 // 1 hour
private let maxBackgroundTime: TimeInterval = 25 // 25 seconds
```

### UserDefaults Keys
```swift
private let lastSyncKey = "last_background_sync"
private let syncEnabledKey = "background_sync_enabled"
```

## 🎯 Main Methods

### BackgroundSyncManager

| Method | Purpose | When to Use |
|--------|---------|-------------|
| `registerBackgroundTasks()` | Register with iOS BGTaskScheduler | App launch |
| `scheduleBackgroundSync()` | Schedule next background sync | App backgrounded |
| `performImmediateSync()` | Manual sync trigger | User action |
| `toggleBackgroundSync()` | Enable/disable sync | User preference |

### BackgroundSyncCard

| Property | Type | Description |
|----------|------|-------------|
| `backgroundSyncEnabled` | `Bool` | Sync enabled state |
| `lastBackgroundSync` | `Date?` | Last sync timestamp |
| `syncInProgress` | `Bool` | Current sync status |
| `timeSinceLastSync` | `String` | Human-readable last sync |
| `nextScheduledSync` | `String` | Next sync countdown |

## 🔄 Sync Flow

### Automatic Background Sync
1. App enters background → `scheduleBackgroundSync()`
2. iOS triggers background task → `handleBackgroundRefresh()` or `handleBackgroundFetch()`
3. Sync execution → `performSync(isBackground: true)`
4. Complete and schedule next sync

### Manual Sync
1. User taps "Sync Now" → `performImmediateSync()`
2. Network check → Proceed if online
3. Sync execution → `performSync(isBackground: false)`
4. UI updates with completion status

## 📊 Sync Data Priority

| Priority | Data Type | ViewModel Method |
|----------|-----------|------------------|
| **High** | Cocktail Data | `homeViewModel.loadCocktails()` |
| **Medium** | Cached Data | `offlineViewModel.syncCachedData()` |

## 🛠️ Debugging

### Enable Logging
```swift
// Logs are automatically printed to console
// Look for "BackgroundSyncManager:" prefix
```

### Common Log Messages
```
BackgroundSyncManager: Background tasks registered
BackgroundSyncManager: Starting background sync
BackgroundSyncManager: Sync completed successfully in 12.3s
BackgroundSyncManager: Time limit approaching, stopping sync
BackgroundSyncManager: Background sync disabled
```

### Testing Background Sync
1. Enable background app refresh in iOS Settings
2. Background the app
3. Wait 15+ minutes or use Xcode background app refresh simulation
4. Check console logs for sync activity

## ⚠️ Important Notes

### iOS Limitations
- Background execution limited to ~30 seconds
- iOS may deny background execution based on user patterns
- Requires "Background App Refresh" enabled in Settings

### Network Requirements
- Sync only occurs when network is available
- Automatically detects connectivity changes
- Gracefully handles network failures

### Error Handling
- Uses `try?` for SKIE method calls
- Continues execution even if individual sync steps fail
- Logs errors for debugging

## 🔧 Customization

### Modify Sync Interval
```swift
// In BackgroundSyncManager.swift
private let syncInterval: TimeInterval = 1800 // 30 minutes
```

### Add New Sync Data
```swift
// In performSync() method
// Add new sync operations with time checks
let timeElapsed = Date().timeIntervalSince(startTime)
guard timeElapsed < maxDuration - 5 else { return }

// Your new sync operation here
try? await newViewModel.syncNewData()
```

### Customize UI
```swift
// Modify BackgroundSyncCard.swift
// Add new status indicators, buttons, or information displays
```

## 📱 User Experience

### UI States
- **Enabled + Online**: Shows sync controls and status
- **Enabled + Offline**: Shows "Waiting for network"
- **Disabled**: Shows enable prompt
- **Syncing**: Shows progress indicator

### User Controls
- **Toggle Switch**: Enable/disable background sync
- **Sync Now Button**: Manual sync trigger (when online)
- **Status Display**: Last sync time and next sync info

## 🚀 Production Checklist

- [ ] Background modes configured in Info.plist
- [ ] BGTaskScheduler registration in app launch
- [ ] Network connectivity handling
- [ ] Time limit compliance (< 30 seconds)
- [ ] Error handling for all sync operations
- [ ] User preference persistence
- [ ] UI feedback for all states
- [ ] Testing on device (not just simulator)

## 📚 Related Documentation

- [iOS Background Sync Implementation](./ios-background-sync.md) - Complete technical documentation
- [Apple BGTaskScheduler Documentation](https://developer.apple.com/documentation/backgroundtasks/bgtaskscheduler)
- [iOS Background Execution Guide](https://developer.apple.com/documentation/backgroundtasks)
