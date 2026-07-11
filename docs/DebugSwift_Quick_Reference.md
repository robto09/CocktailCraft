# DebugSwift Quick Reference for CocktailCraft

## Quick Start

### Accessing DebugSwift
1. **Run DEBUG build** on device or simulator
2. **Shake device** to toggle DebugSwift interface
3. **Automatic appearance** - Shows on app launch in DEBUG mode

### Main Features

#### 🌐 Network Inspector
- **Location**: Network tab in DebugSwift interface
- **Purpose**: Monitor TheCocktailDB API calls
- **Features**:
  - Real-time request/response monitoring
  - JSON formatting with syntax highlighting
  - Request filtering (only TheCocktailDB URLs)
  - Response time tracking

#### ⚡ Performance Monitor
- **Location**: Performance tab
- **Metrics**:
  - CPU usage (real-time)
  - Memory consumption
  - FPS (frames per second)
  - Memory leak detection

#### 📱 App Tools
- **Location**: App tab
- **Features**:
  - Crash reports with stack traces
  - Console logs (real-time)
  - Device information
  - Custom CocktailCraft debug actions

#### 🎨 Interface Tools
- **Location**: Interface tab
- **Features**:
  - 3D view hierarchy inspector
  - Touch indicators
  - Animation speed control
  - View boundary highlighting

#### 📁 Resources
- **Location**: Resources tab
- **Features**:
  - File browser (app sandbox)
  - UserDefaults viewer/editor
  - Keychain inspector
  - Database browser

## CocktailCraft Custom Actions

### Available Actions
Access via: **App Tab → Custom Actions → CocktailCraft Debug Tools**

1. **Clear Favorites Cache**
   - Removes all saved favorite cocktails
   - Useful for testing favorites functionality

2. **Clear Cart Data**
   - Empties the shopping cart
   - Useful for testing cart operations

3. **Reset App State**
   - Clears favorites, cart, and search history
   - Complete app data reset for fresh testing

4. **Force Network Error**
   - Simulates network error conditions
   - Useful for testing error handling

### App Information
Access via: **App Tab → App Info → CocktailCraft Info**

- App Version: 1.0
- API: TheCocktailDB (Free Tier)
- Platform: iOS (Kotlin Multiplatform)
- Architecture: MVVM + Clean Architecture
- DI Framework: Koin
- UI Framework: SwiftUI

## Common QA Testing Scenarios

### Testing Network Connectivity
1. Open **Network tab**
2. Navigate through app (search cocktails, view details)
3. Monitor API calls to TheCocktailDB
4. Check response times and status codes
5. Verify JSON data structure

### Testing Performance
1. Open **Performance tab**
2. Navigate through different screens
3. Monitor CPU and memory usage
4. Check for memory leaks
5. Verify smooth FPS (60fps target)

### Testing Error Handling
1. Use **Force Network Error** action
2. Navigate through app features
3. Verify error messages display correctly
4. Check app doesn't crash

### Testing Data Persistence
1. Add items to favorites and cart
2. Use **Clear Favorites Cache** or **Clear Cart Data**
3. Verify app handles empty states correctly
4. Test data restoration

### Testing UI Components
1. Open **Interface tab**
2. Use **View Hierarchy** to inspect UI structure
3. Enable **Touch Indicators** to verify touch targets
4. Use **View Borders** to check layout issues

## Troubleshooting

### DebugSwift Not Appearing
- Ensure running DEBUG build (not Release)
- Try shaking device again
- Check console for DebugSwift initialization logs

### Network Monitoring Not Working
- Verify making API calls to TheCocktailDB
- Check if URLs match configured filters
- Look for network requests in Network tab

### Performance Issues
- DebugSwift itself uses resources
- Performance impact is normal in DEBUG builds
- Disable specific monitoring if needed

### Shake Gesture Not Working
- Ensure device supports motion events
- Try more vigorous shake motion
- Use manual toggle: `CocktailCraftApp.debugSwift.toggle()`

## Best Practices

### For QA Testing
1. **Start with Network tab** - Monitor API behavior first
2. **Check Performance regularly** - Ensure app runs smoothly
3. **Use Custom Actions** - Test edge cases with data clearing
4. **Document Issues** - Screenshot DebugSwift data for bug reports

### For Development
1. **Monitor Memory Usage** - Watch for leaks during development
2. **Check API Responses** - Verify data structure and content
3. **Test Error Scenarios** - Use network error simulation
4. **Validate UI** - Use view hierarchy for layout debugging

## Integration Status

✅ **Fully Integrated** - Ready for QA use  
✅ **DEBUG Only** - No impact on release builds  
✅ **Custom Configured** - Tailored for CocktailCraft  
✅ **API Monitoring** - TheCocktailDB specific  
✅ **Shake Enabled** - Easy access during testing  

## Support

- **Installation Guide**: `docs/DebugSwift_Complete_Installation_Guide.md`
- **Detailed Documentation**: `iosApp/DebugSwift_Integration.md`
- **DebugSwift GitHub**: https://github.com/DebugSwift/DebugSwift

---

*This tool is designed specifically for QA debugging and development purposes. It provides comprehensive insights into CocktailCraft's behavior, performance, and network activity.*
