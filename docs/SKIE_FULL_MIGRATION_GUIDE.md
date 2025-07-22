# SKIE Full Migration Guide

## Overview

This guide documents the migration from FlowCollector bridge pattern to full SKIE integration for ViewModels in the CocktailCraft KMP application.

## What is SKIE?

SKIE (Swift/Kotlin Interface Enhancer) by Touchlab provides seamless interoperability between Kotlin Multiplatform and Swift, enabling:

- **Kotlin Flows → Swift AsyncSequence**: Automatic conversion
- **Suspend functions → Swift async/await**: Native async support
- **Sealed classes → Swift enums**: Better pattern matching
- **Default parameters**: Work naturally in Swift

## Migration Benefits

1. **No Bridge Code**: Remove FlowCollector boilerplate
2. **Native Swift Syntax**: Use async/await naturally
3. **Type Safety**: Better compile-time checks
4. **Performance**: Reduced overhead
5. **Maintainability**: Single source of truth

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                   Shared Module (KMP)                    │
├─────────────────────────────────────────────────────────┤
│  SharedViewModel (Base)                                  │
│  ├── SharedHomeViewModel                                 │
│  ├── SharedCartViewModel                                 │
│  ├── SharedCocktailDetailViewModel                       │
│  └── SharedFavoritesViewModel                           │
└─────────────────────────────────────────────────────────┘
                    │                    │
                    ▼                    ▼
     ┌──────────────────────┐  ┌──────────────────────┐
     │        iOS App        │  │     Android App      │
     ├──────────────────────┤  ├──────────────────────┤
     │ HomeViewModelSKIE     │  │ HomeViewModelWrapper │
     │ CartViewModelSKIE     │  │ CartViewModel        │
     │ (Pure SKIE)           │  │ (AndroidX ViewModel) │
     └──────────────────────┘  └──────────────────────┘
```

## Implementation Steps

### 1. Configure SKIE in build.gradle.kts

```kotlin
plugins {
    id("co.touchlab.skie") version "0.6.1"
}

skie {
    features {
        group {
            co.touchlab.skie.configuration.FlowInterop.Enabled(true)
            co.touchlab.skie.configuration.SuspendInterop.Enabled(true)
            co.touchlab.skie.configuration.EnumInterop.Enabled(true)
            co.touchlab.skie.configuration.SealedInterop.Enabled(true)
            co.touchlab.skie.configuration.DefaultArgumentInterop.Enabled(true)
        }
    }
}
```

### 2. Create Shared ViewModels

Example: SharedHomeViewModel

```kotlin
class SharedHomeViewModel : SharedViewModel() {
    private val repository: CocktailRepository by inject()
    
    // StateFlows automatically convert to Swift AsyncSequence
    private val _cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val cocktails: StateFlow<List<Cocktail>> = _cocktails.asStateFlow()
    
    // Suspend functions become Swift async functions
    suspend fun loadCocktails() {
        // Implementation
    }
    
    // Regular functions work normally
    fun isFavorite(cocktailId: String): Boolean {
        return favorites.value.any { it.id == cocktailId }
    }
}
```

### 3. iOS Integration (Pure SKIE)

```swift
@MainActor
class HomeViewModelSKIE: ObservableObject {
    @Published var cocktails: [Cocktail] = []
    private let sharedViewModel: SharedHomeViewModel
    private var observationTasks: [Task<Void, Never>] = []
    
    init() {
        self.sharedViewModel = KoinInitializer.shared.getSharedHomeViewModel()
        startObserving()
    }
    
    private func startObserving() {
        // SKIE converts StateFlow to AsyncSequence
        observationTasks.append(Task {
            for await cocktailList in sharedViewModel.cocktails {
                await MainActor.run {
                    self.cocktails = cocktailList
                }
            }
        })
    }
    
    // Call suspend functions with async/await
    func loadCocktails() async {
        await sharedViewModel.loadCocktails()
    }
}
```

### 4. Android Integration

```kotlin
class HomeViewModelWrapper : ViewModel(), KoinComponent {
    private val sharedViewModel: SharedHomeViewModel by inject()
    
    // Convert to hot StateFlow for Android lifecycle
    val cocktails: StateFlow<List<Cocktail>> = sharedViewModel.cocktails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Delegate to shared ViewModel
    fun loadCocktails() {
        viewModelScope.launch {
            sharedViewModel.loadCocktails()
        }
    }
}
```

## Migration Checklist

- [x] Configure SKIE in shared module
- [x] Create SharedHomeViewModel with SKIE patterns
- [x] Create SharedCartViewModel
- [x] Create SharedCocktailDetailViewModel
- [x] Update KoinHelper to provide shared ViewModels
- [x] Create iOS wrapper ViewModels using pure SKIE
- [x] Create Android wrapper ViewModels
- [x] Create example iOS views using SKIE ViewModels
- [ ] Remove FlowCollector.swift (after full migration)
- [ ] Update all iOS views to use SKIE ViewModels
- [ ] Test on both platforms

## Key Patterns

### StateFlow Observation (iOS)

```swift
// Old way with FlowCollector
let collector = FlowCollector<NSArray> { cocktailArray in
    DispatchQueue.main.async {
        self.cocktails = cocktailArray as? [Cocktail] ?? []
    }
}
try await kotlinFlow.collect(collector: collector)

// New way with SKIE
for await cocktailList in sharedViewModel.cocktails {
    await MainActor.run {
        self.cocktails = cocktailList
    }
}
```

### Calling Suspend Functions (iOS)

```swift
// Old way
// Complex bridge code required

// New way with SKIE
await sharedViewModel.loadCocktails()
await sharedViewModel.toggleFavorite(cocktail: cocktail)
```

### Error Handling

Shared ViewModel provides consistent error handling across platforms:

```kotlin
// In SharedViewModel
handleException(
    exception,
    "Failed to load cocktails",
    recoveryAction = ErrorHandler.RecoveryAction("Retry") { 
        viewModelScope.launch { loadCocktails() }
    }
)
```

## Best Practices

1. **Keep Business Logic in Shared ViewModels**: Platform-specific code should only handle UI concerns
2. **Use StateFlow for Reactive State**: Automatically converts to appropriate platform types
3. **Handle Lifecycle Properly**: Clean up observations in deinit (iOS) or onCleared (Android)
4. **Leverage SKIE Features**: Use suspend functions, default parameters, and sealed classes
5. **Test on Both Platforms**: Ensure consistent behavior

## Troubleshooting

### Common Issues

1. **Xcode not recognizing SKIE functions**
   - Clean and rebuild the shared framework
   - Ensure SKIE plugin is properly configured

2. **Memory leaks in iOS**
   - Always cancel observation tasks in deinit
   - Use weak references where appropriate

3. **StateFlow not updating UI**
   - Ensure observations are on MainActor
   - Check that StateFlow is properly initialized

## Performance Considerations

- SKIE has minimal runtime overhead
- StateFlow observations are efficient
- Suspend function calls are optimized
- No reflection or runtime type checking

## Future Enhancements

1. Add more shared ViewModels (Profile, Order, etc.)
2. Implement shared navigation logic
3. Add shared data validation
4. Create shared UI state models

## Resources

- [SKIE Documentation](https://skie.touchlab.co/)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Swift Async/Await](https://docs.swift.org/swift-book/LanguageGuide/Concurrency.html)