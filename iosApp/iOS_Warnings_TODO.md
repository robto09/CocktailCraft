# iOS Build Warnings - TODO List

## Status: ✅ Build Successful
The iOS app builds and runs successfully! These warnings are **non-blocking** but should be addressed in future iterations for code quality and best practices.

## Remaining Warnings to Fix

### 1. Unreachable Catch Blocks
**Files affected:**
- `iosApp/CocktailCraft/CocktailCraftApp.swift` (lines 50, 60, 70, 80)

**Issue:** 
```swift
} catch {
  ^
warning: 'catch' block is unreachable because no errors are thrown in 'do' block
```

**Solution:** Remove unnecessary do-catch blocks or add proper error handling where needed.

### 2. Deprecated onChange Usage
**Files affected:**
- `iosApp/CocktailCraft/Components/OfflineBanner.swift` (line 35)

**Issue:**
```swift
.onChange(of: networkMonitor.isConnected) { _ in
 ^
warning: 'onChange(of:perform:)' was deprecated in iOS 17.0: Use `onChange` with a two or zero parameter action closure instead.
```

**Solution:** Update to use the new onChange syntax:
```swift
// Old (deprecated)
.onChange(of: networkMonitor.isConnected) { _ in
    // handle change
}

// New (iOS 17+)
.onChange(of: networkMonitor.isConnected) {
    // handle change
}
```

### 3. TLS Version Warning
**Files affected:**
- `iosApp/CocktailCraft/Info.plist`

**Issue:**
```
warning: The value "TLSv1.0" for 'NSAppTransportSecurity' => 'NSExceptionDomains' => 'thecocktaildb.com' => 'NSExceptionMinimumTLSVersion' has been deprecated starting in iOS 15.0, use TLSv1.2 or TLSv1.3 instead.
```

**Solution:** Update Info.plist to use TLSv1.2 or TLSv1.3:
```xml
<key>NSExceptionMinimumTLSVersion</key>
<string>TLSv1.2</string>
```

### 4. Unused Variable Warnings
**Files affected:**
- `iosApp/CocktailCraft/ViewModels/CartViewModel.swift` (line 24)

**Issue:**
```swift
guard let cartRepository = cartRepository else {
      ~~~~^~~~~~~~~~~~~~~~~
warning: value 'cartRepository' was defined but never used; consider replacing with boolean test
```

**Solution:** Replace with boolean test:
```swift
// Old
guard let cartRepository = cartRepository else {
    return
}

// New
guard cartRepository != nil else {
    return
}
```

### 5. Async Function Suggestions
**Files affected:**
- `iosApp/CocktailCraft/ViewModels/OrderViewModel.swift` (line 73)

**Issue:**
```swift
ordersFlow.collect(collector: collector) { error in
           ^
warning: consider using asynchronous alternative function
```

**Solution:** Consider using async/await pattern where appropriate for better Swift concurrency integration.

## Priority Levels

### High Priority (Security/Performance)
1. **TLS Version Warning** - Security concern, should be fixed soon
2. **Async Function Usage** - Performance and modern Swift practices

### Medium Priority (Code Quality)
1. **Deprecated onChange Usage** - Will become errors in future iOS versions
2. **Unused Variable Warnings** - Code cleanliness

### Low Priority (Cleanup)
1. **Unreachable Catch Blocks** - Code cleanliness, no functional impact

## Implementation Timeline

### Phase 1 (Next Sprint)
- [ ] Fix TLS version warning
- [ ] Update deprecated onChange usage

### Phase 2 (Future Sprint)
- [ ] Clean up unused variables
- [ ] Remove unreachable catch blocks
- [ ] Evaluate async/await migration

### Phase 3 (Long-term)
- [ ] Complete async/await migration
- [ ] Add comprehensive error handling
- [ ] Performance optimization

## Notes
- All warnings are non-blocking and don't prevent the app from building or running
- The app is fully functional despite these warnings
- These are primarily code quality and future-proofing improvements
- Some warnings may be resolved automatically when updating to newer iOS deployment targets
