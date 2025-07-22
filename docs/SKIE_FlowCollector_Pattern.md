# SKIE FlowCollector Bridge Pattern

## Overview

The CocktailCraft iOS app currently uses a **FlowCollector bridge pattern** to integrate with Kotlin Multiplatform flows through SKIE. This document explains why we're using this approach and how it works.

## Current SKIE Integration Status

**Status**: 🔄 **~80% SKIE Integration Complete**  
**Pattern**: FlowCollector Bridge with SKIE async/await  
**Build Status**: ✅ **Working and Functional**

## Why FlowCollector Bridge Pattern?

### 1. **Stability & Reliability**
- Proven pattern that works consistently with SKIE 0.6.1
- Eliminates build issues and runtime crashes
- Provides predictable behavior across all iOS versions

### 2. **Type Safety**
- Maintains Swift type safety while bridging to Kotlin
- Proper generic type handling: `FlowCollector<NSArray>`, `FlowCollector<KotlinBoolean>`
- Compile-time type checking for flow data

### 3. **SKIE Compatibility**
- Works seamlessly with SKIE's async/await patterns
- Uses `try await kotlinFlow.collect(collector: collector)` syntax
- No deprecated `completionHandler` patterns

### 4. **Future Migration Path**
- Can be easily updated to pure SKIE when ready
- Modular design allows gradual migration
- Maintains consistent API across ViewModels

## Technical Implementation

### FlowCollector Bridge Class

```swift
// iosApp/CocktailCraft/Utils/FlowCollector.swift
import shared

class FlowCollector<T>: KotlinSuspendFunction1 {
    let callback: (T) -> Void
    
    init(callback: @escaping (T) -> Void) {
        self.callback = callback
    }
    
    func invoke(p1: Any?) async throws -> Any? {
        if let value = p1 as? T {
            callback(value)
        }
        return KotlinUnit()
    }
}
```

### Usage Pattern in ViewModels

```swift
// Example: HomeViewModel
func loadCocktails() {
    Task {
        do {
            let collector = FlowCollector<NSArray> { cocktails in
                DispatchQueue.main.async {
                    self.cocktails = cocktails as? [Cocktail] ?? []
                    self.isLoading = false
                }
            }
            
            let kotlinFlow = repository.getCocktails()
            try await kotlinFlow.collect(collector: collector)
        } catch {
            DispatchQueue.main.async {
                self.handleError(error)
            }
        }
    }
}
```

## Benefits Over Previous Approaches

### Before: Callback-Based Pattern
```swift
// Old pattern with completionHandler
kotlinFlow.collect(collector: collector, completionHandler: { error in
    if let error = error {
        DispatchQueue.main.async {
            self.handleError(error)
        }
    }
})
```

### After: SKIE async/await with FlowCollector
```swift
// Current pattern with SKIE async/await
try await kotlinFlow.collect(collector: collector)
```

### Improvements:
- ✅ **Cleaner syntax**: No completion handlers
- ✅ **Better error handling**: Standard Swift async/await error patterns
- ✅ **Type safety**: Compile-time type checking
- ✅ **Consistency**: Same pattern across all ViewModels

## Current Implementation Status

### ✅ ViewModels Using FlowCollector Pattern:
- **HomeViewModel**: Cocktail discovery and search flows
- **FavoritesViewModel**: Favorites management flows
- **CartViewModel**: Order placement flows
- **OrderViewModel**: Order history flows
- **ProfileViewModel**: User authentication flows
- **CocktailDetailView**: Individual cocktail loading

### ✅ Features Working:
- iOS app builds successfully on iOS 18.5
- All ViewModels function correctly
- Smooth async/await experience
- Proper error handling
- Type-safe flow collection

## Path to 100% SKIE Integration

### Current: ~80% SKIE Integration
- ✅ Using SKIE async/await patterns
- ✅ No deprecated completion handlers
- 🔄 Using FlowCollector bridge (not pure SKIE)

### Future: 100% Pure SKIE Integration
1. **Research Pure SKIE Patterns**: Investigate eliminating FlowCollector bridge
2. **SKIE Version Updates**: Monitor for SKIE updates with enhanced flow support
3. **Gradual Migration**: Test pure SKIE in isolated ViewModels first
4. **Performance Testing**: Compare FlowCollector vs pure SKIE performance

### Potential Pure SKIE Pattern:
```swift
// Future pure SKIE pattern (when available)
for await cocktails in repository.getCocktails() {
    DispatchQueue.main.async {
        self.cocktails = cocktails
    }
}
```

## Maintenance Guidelines

### When Adding New ViewModels:
1. Use the established FlowCollector pattern
2. Follow the same async/await structure
3. Maintain consistent error handling
4. Use appropriate generic types

### When SKIE Updates:
1. Test new SKIE features in isolated ViewModels
2. Compare performance with current FlowCollector approach
3. Migrate gradually if benefits are clear
4. Update documentation accordingly

## Conclusion

The FlowCollector bridge pattern provides a **stable, working solution** for SKIE integration in the CocktailCraft iOS app. While not 100% pure SKIE, it delivers:

- ✅ **Working iOS app** on iOS 18.5
- ✅ **Native Swift async/await experience**
- ✅ **Type-safe flow handling**
- ✅ **Consistent patterns** across ViewModels
- ✅ **Future migration path** to pure SKIE

**Current Status**: Functional and ready for development  
**Future Goal**: Migrate to 100% pure SKIE when optimal patterns are available

---

**Document Version**: 1.0  
**Created**: 2025-07-22  
**Status**: FlowCollector Bridge Pattern - Working & Functional
