# ViewModels Architecture Explanation

## Current ViewModels Structure

### 📱 **Shared ViewModels (Kotlin Multiplatform) - KEEP ALL**
**Location**: `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/`

**11 ViewModels** - These contain the **business logic** and are shared between platforms:
- SharedCartViewModel.kt
- SharedCocktailDetailViewModel.kt  
- SharedCocktailListViewModel.kt
- SharedFavoritesViewModel.kt
- SharedHomeViewModel.kt
- SharedOfflineModeViewModel.kt
- SharedOrderViewModel.kt
- SharedProfileViewModel.kt
- SharedReviewViewModel.kt
- SharedThemeViewModel.kt
- SharedViewModel.kt (base class)

**Why we need these**: These contain all the business logic, state management, and data operations. They're written once and used by both platforms.

### 🍎 **iOS SKIE Wrapper ViewModels - KEEP ALL**
**Location**: `iosApp/CocktailCraft/ViewModels/`

**9 ViewModels** - These are **platform adapters** for SwiftUI:
- CartViewModelSKIE.swift
- CocktailDetailViewModelSKIE.swift
- FavoritesViewModelSKIE.swift
- HomeViewModelSKIE.swift
- OfflineModeViewModelSKIE.swift
- OrderViewModelSKIE.swift
- ProfileViewModelSKIE.swift
- ReviewViewModelSKIE.swift
- ThemeViewModelSKIE.swift

**Why we need these**: They wrap the shared ViewModels to make them work with SwiftUI's `@ObservableObject` and `@Published` patterns. They handle:
- StateFlow → AsyncSequence conversion
- MainActor threading for UI updates
- SwiftUI lifecycle management
- Native Swift async/await patterns

### 🤖 **Android SKIE Wrapper ViewModels - KEEP ALL**
**Location**: `androidApp/src/main/java/com/cocktailcraft/viewmodel/`

**9 ViewModels** - These are **platform adapters** for Android:
- CartViewModelSKIE.kt
- CocktailDetailViewModelSKIE.kt
- FavoritesViewModelSKIE.kt
- HomeViewModelSKIE.kt
- OfflineModeViewModelSKIE.kt
- OrderViewModelSKIE.kt
- ProfileViewModelSKIE.kt
- ReviewViewModelSKIE.kt
- ThemeViewModelSKIE.kt

**Why we need these**: They wrap the shared ViewModels to work with Android's lifecycle and Compose. They handle:
- AndroidX ViewModel lifecycle
- StateFlow scoping with `viewModelScope`
- Compose state management
- Android-specific lifecycle events

## Architecture Pattern: **Shared Business Logic + Platform Adapters**

```
┌─────────────────────────────────────────────────────────┐
│           Shared ViewModels (Business Logic)            │
│                    Kotlin Multiplatform                 │
├─────────────────────────────────────────────────────────┤
│  SharedHomeViewModel                                     │
│  ├── StateFlow<List<Cocktail>> cocktails               │
│  ├── suspend fun loadCocktails()                       │
│  └── fun searchCocktails(query: String)                │
└─────────────────────────────────────────────────────────┘
                     │                    │
                     ▼                    ▼
      ┌──────────────────────┐  ┌──────────────────────┐
      │   iOS SKIE Wrapper    │  │ Android SKIE Wrapper │
      ├──────────────────────┤  ├──────────────────────┤
      │ HomeViewModelSKIE     │  │ HomeViewModelSKIE    │
      │ @ObservableObject     │  │ : ViewModel()        │
      │ @Published cocktails  │  │ val cocktails: SF    │
      │ async loadCocktails() │  │ fun loadCocktails()  │
      └──────────────────────┘  └──────────────────────┘
```

## Why This Architecture?

### ✅ **Benefits of Current Structure**
1. **Single Source of Truth**: Business logic in shared ViewModels
2. **Platform Native**: Each platform gets native UI integration
3. **Type Safety**: SKIE provides compile-time checking
4. **Performance**: Zero overhead with direct interop
5. **Maintainability**: Change business logic once, affects both platforms

### 🎯 **What Each Layer Does**

**Shared ViewModels (Kotlin)**:
- Business logic and state management
- Data operations and API calls
- Error handling and validation
- Cross-platform consistency

**iOS SKIE Wrappers (Swift)**:
- SwiftUI integration (`@ObservableObject`, `@Published`)
- StateFlow → AsyncSequence conversion
- MainActor threading for UI updates
- iOS-specific lifecycle management

**Android SKIE Wrappers (Kotlin)**:
- AndroidX ViewModel lifecycle
- Compose state management
- `viewModelScope` for coroutines
- Android-specific lifecycle events

## Answer: **YES, we need all of them!**

Each layer serves a specific purpose:
- **Shared**: Contains the business logic (write once, use everywhere)
- **iOS Wrappers**: Make shared ViewModels work with SwiftUI
- **Android Wrappers**: Make shared ViewModels work with Compose

This gives us **95% code sharing** for business logic while maintaining **100% native** platform experiences.

The alternative would be duplicating all business logic in each platform, which would mean maintaining the same code in two places and losing the benefits of Kotlin Multiplatform.

---

# SKIE's Value and Why We Still Need Platform Wrappers

## What SKIE Actually Does

### ✅ **With SKIE (Current Implementation)**

**SKIE's Magic**: Automatic conversion of Kotlin types to native platform types
- `StateFlow<List<Cocktail>>` → Swift `AsyncSequence<[Cocktail]>`
- `suspend fun loadCocktails()` → Swift `async func loadCocktails()`
- Kotlin `Flow` → Swift `AsyncSequence`
- Type safety with generics preserved

**iOS Wrapper (Simplified by SKIE)**:
```swift
// SKIE makes this possible - only ~20 lines instead of 80+
@MainActor
class HomeViewModelSKIE: ObservableObject {
    @Published var cocktails: [Cocktail] = []
    private let sharedViewModel: SharedHomeViewModel
    
    init() {
        self.sharedViewModel = KoinInitializer.shared.getSharedHomeViewModel()
        // SKIE magic: StateFlow automatically becomes AsyncSequence
        Task {
            for await cocktailList in sharedViewModel.cocktails {
                self.cocktails = cocktailList
            }
        }
    }
    
    // SKIE magic: suspend function becomes async function
    func loadCocktails() async {
        await sharedViewModel.loadCocktails()
    }
}
```

### ❌ **Without SKIE (Manual Bridge Code)**

**iOS Wrapper (Manual Implementation)**:
```swift
// Without SKIE - would need ~80+ lines of complex bridge code
@MainActor
class HomeViewModelManual: ObservableObject {
    @Published var cocktails: [Cocktail] = []
    private let sharedViewModel: SharedHomeViewModel
    
    init() {
        self.sharedViewModel = KoinInitializer.shared.getSharedHomeViewModel()
        // Manual Flow collection with custom collectors
        let collector = FlowValueCollector<NSArray>()
        Task {
            try await sharedViewModel.cocktails.collect(collector: collector)
            var attempts = 0
            while collector.isLoading && attempts < 50 {
                try await Task.sleep(nanoseconds: 100_000_000)
                attempts += 1
            }
            if let cocktailArray = collector.value as? [Cocktail] {
                self.cocktails = cocktailArray
            }
        }
    }
    
    // Manual async wrapping
    func loadCocktails() {
        Task {
            do {
                try await withCheckedThrowingContinuation { continuation in
                    // Complex manual async bridging code
                }
            } catch {
                // Manual error handling
            }
        }
    }
}
```

## Why We Can't Use Shared ViewModels Directly

### 🚫 **Technical Limitations**

1. **Platform Lifecycle Differences**:
   - **iOS**: Needs `@ObservableObject` and `@Published` for SwiftUI
   - **Android**: Needs `ViewModel()` base class for lifecycle management

2. **UI Framework Integration**:
   - **iOS**: SwiftUI requires `@MainActor` and specific patterns
   - **Android**: Compose requires `viewModelScope` and StateFlow scoping

3. **Memory Management**:
   - **iOS**: ARC with specific cleanup patterns
   - **Android**: GC with different lifecycle events

### 🎯 **What SKIE Actually Saves Us**

**Code Reduction Comparison**:

| Aspect | Without SKIE | With SKIE | Savings |
|--------|--------------|-----------|---------|
| iOS Wrapper Lines | ~80-100 lines | ~20-30 lines | **70% reduction** |
| Type Casting | Manual `as?` everywhere | Automatic | **100% elimination** |
| Flow Collection | Custom collectors | Native `for await` | **Native patterns** |
| Error Handling | Manual try-catch bridging | Native Swift errors | **Native experience** |
| Async Functions | Manual continuation wrapping | Direct `await` | **Native async/await** |

## Alternative Approaches

### 🔄 **Option 1: Direct Shared ViewModels (Not Possible)**
```swift
// This DOESN'T WORK - platform incompatibilities
struct HomeView: View {
    // ❌ Can't use SharedHomeViewModel directly
    // - No @ObservableObject conformance
    // - No @Published properties
    // - No SwiftUI lifecycle integration
    @StateObject var viewModel: SharedHomeViewModel // ❌ Won't compile
}
```

### 🔄 **Option 2: No SKIE (Manual Bridge Code)**
```swift
// Without SKIE - complex manual implementation
class HomeViewModelManual: ObservableObject {
    // 80+ lines of complex Flow collection code
    // Manual type casting everywhere
    // Custom async/await bridging
    // Error-prone and hard to maintain
}
```

### ✅ **Option 3: SKIE + Thin Wrappers (Current Implementation)**
```swift
// With SKIE - clean, simple, maintainable
class HomeViewModelSKIE: ObservableObject {
    // 20 lines of clean code
    // Native Swift patterns
    // Type-safe automatic conversion
    // Easy to maintain and understand
}
```

## SKIE's Real Value

### 🚀 **What SKIE Provides**
1. **70% Code Reduction**: Eliminates complex bridge code
2. **Native Experience**: Kotlin APIs feel like native Swift/Kotlin
3. **Type Safety**: Compile-time checking instead of runtime casting
4. **Performance**: Direct interop without reflection overhead
5. **Maintainability**: Simple, readable wrapper code

### 🎯 **What We Still Need Platform Wrappers For**
1. **UI Framework Integration**: SwiftUI vs Compose requirements
2. **Platform Lifecycle**: Different lifecycle management patterns
3. **Threading Models**: MainActor vs viewModelScope
4. **Memory Management**: ARC vs GC considerations

## Summary

**SKIE doesn't eliminate the need for platform wrappers**, but it makes them **dramatically simpler and more maintainable**.

**Without SKIE**: 80+ lines of complex bridge code per ViewModel
**With SKIE**: 20 lines of clean, native-feeling code per ViewModel

The shared ViewModels contain 95% of the business logic, and the thin platform wrappers (made possible by SKIE) handle the remaining 5% of platform-specific UI integration.

This is the optimal balance between code sharing and platform-native experience.

## Key Takeaways

1. **SKIE doesn't eliminate platform wrappers** - it makes them dramatically simpler
2. **95% business logic sharing** through shared ViewModels
3. **5% platform-specific UI integration** through thin wrappers
4. **70% code reduction** compared to manual bridge implementations
5. **Native experience** on both platforms while sharing core logic

---

**Document Created**: 2025-07-25
**Status**: Complete Architecture Explanation
**Purpose**: Reference guide for ViewModels architecture and SKIE benefits