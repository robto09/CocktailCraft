# DebugSwift Integration for CocktailCraft iOS

## Overview

DebugSwift has been integrated into the CocktailCraft iOS app to provide comprehensive debugging capabilities for QA and development purposes. This integration is **DEBUG-only** and will not be included in release builds.

## Features Available

### 🌐 Network Inspector
- **HTTP Monitoring**: Captures all API requests/responses to TheCocktailDB
- **Request Filtering**: Configured to monitor only TheCocktailDB API calls
- **JSON Formatting**: Automatic formatting with syntax highlighting
- **Response Analysis**: Detailed request/response inspection

### ⚡ Performance Monitoring
- **Real-time Metrics**: CPU, memory, and FPS monitoring
- **Memory Leak Detection**: Automatic detection of leaked ViewControllers
- **Thread Checker**: Main thread violation detection
- **Performance Widget**: Live performance stats overlay

### 📱 App Tools
- **Crash Reports**: Detailed crash analysis with stack traces
- **Console Logs**: Real-time console output monitoring
- **Device Info**: App version, build, device details
- **Custom Actions**: CocktailCraft-specific debugging actions

### 🎨 Interface Tools
- **View Hierarchy**: 3D interactive view hierarchy inspector
- **Touch Indicators**: Visual feedback for touch interactions
- **Animation Control**: Slow down animations for debugging
- **View Borders**: Highlight view boundaries

### 📁 Resources
- **File Browser**: Navigate app sandbox
- **UserDefaults**: View and modify app preferences
- **Database Browser**: Inspect local data storage

## How to Use

### Accessing DebugSwift

1. **Automatic Launch**: DebugSwift interface appears automatically in DEBUG builds
2. **Shake to Toggle**: Shake your device to show/hide the DebugSwift interface
3. **Manual Toggle**: Use `CocktailCraftApp.debugSwift.toggle()` in code

### Custom Debug Actions

The following custom actions are available in the DebugSwift interface:

- **Clear Favorites Cache**: Removes all saved favorites
- **Clear Cart Data**: Empties the shopping cart
- **Reset App State**: Clears all app data (favorites, cart, search history)
- **Force Network Error**: Simulates network errors for testing

### Network Monitoring

- Automatically monitors all requests to TheCocktailDB API
- Filters out non-essential network traffic
- Provides detailed request/response analysis
- Shows API response times and status codes

## Configuration

### Network Filtering
```swift
// Only monitor TheCocktailDB API calls
DebugSwift.Network.shared.onlyURLs = [
    "https://www.thecocktaildb.com",
    "http://www.thecocktaildb.com"
]
```

### Custom Actions
Custom debugging actions are configured in `CocktailCraftApp.swift` and include:
- Cache management
- Data reset functionality
- Network error simulation

### App Information
Displays relevant app information including:
- App version and build
- API details (TheCocktailDB Free Tier)
- Architecture information (MVVM + Clean Architecture)
- Technology stack details

## Requirements

- **iOS**: 14.0+
- **Swift**: 5.10+ (configured for compatibility)
- **Xcode**: 16.0+
- **Build Configuration**: DEBUG only

## Installation

DebugSwift is automatically included via CocoaPods in DEBUG builds:

```ruby
pod 'DebugSwift', :git => 'https://github.com/DebugSwift/DebugSwift.git', :tag => '1.7.13.1', :configurations => ['Debug']
```

## Important Notes

- ⚠️ **DEBUG ONLY**: DebugSwift is only available in debug builds
- 🔒 **No Release Impact**: Zero impact on release builds or performance
- 🧪 **QA Tool**: Designed specifically for QA testing and debugging
- 📱 **Device Testing**: Best used on physical devices for comprehensive testing

## Troubleshooting

### DebugSwift Not Appearing
1. Ensure you're running a DEBUG build
2. Check that the device supports shake gestures
3. Verify Swift version is 6.0+

### Swift Concurrency Compatibility
The project is configured for optimal compatibility:
1. Main app uses Swift 5.10 (avoids Swift 6 strict concurrency issues)
2. DebugSwift runs with Swift 5.0 and minimal concurrency checking
3. This configuration provides stability while maintaining modern Swift features
4. Perfect for Kotlin Multiplatform projects with shared ViewModels

### Network Monitoring Issues
1. Check that URLs are correctly configured in `onlyURLs`
2. Verify API calls are being made to TheCocktailDB
3. Check console logs for DebugSwift initialization

### Performance Impact
- DebugSwift may impact app performance during debugging
- This is expected and only affects DEBUG builds
- Performance monitoring itself uses system resources

## Support

For DebugSwift-specific issues, refer to:
- [DebugSwift GitHub Repository](https://github.com/DebugSwift/DebugSwift)
- [DebugSwift Documentation](https://github.com/DebugSwift/DebugSwift#readme)

For CocktailCraft-specific integration issues, check the implementation in `CocktailCraftApp.swift`.
