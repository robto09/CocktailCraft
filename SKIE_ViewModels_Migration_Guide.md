# SKIE ViewModels Migration Guide

## Overview

This guide provides step-by-step instructions for migrating from platform-specific ViewModels to shared SKIE ViewModels in the CocktailCraft project. Follow this guide to ensure a smooth transition while maintaining functionality across both iOS and Android platforms.

---

## Prerequisites

Before starting the migration, ensure you have:

1. **Development Environment Setup**
   - Kotlin Multiplatform project configured
   - SKIE plugin properly integrated
   - Xcode and Android Studio set up
   - Koin dependency injection working

2. **Understanding of Current Architecture**
   - Familiarity with existing ViewModels
   - Understanding of SKIE integration patterns
   - Knowledge of the shared module structure

3. **Testing Environment**
   - Unit testing framework set up
   - Ability to run tests on both platforms
   - Mock repositories available for testing

---

## Phase 1: Foundation & Core Dependencies

### Step 1.1: Audit Existing SKIE ViewModels

**Objective**: Validate that existing SKIE ViewModels are properly implemented and functioning.

**Actions**:

1. **Verify Compilation**
   ```bash
   # Build shared module
   ./gradlew :shared:build
   
   # Build iOS framework
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```

2. **Check SKIE Integration**
   - Verify StateFlows are exposed correctly
   - Confirm suspend functions are marked for async conversion
   - Test iOS wrapper classes functionality

3. **Validation Checklist**
   - [ ] [`SharedHomeViewModel.kt`](shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedHomeViewModel.kt) compiles
   - [ ] [`SharedCartViewModel.kt`](shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedCartViewModel.kt) compiles
   - [ ] [`SharedCocktailDetailViewModel.kt`](shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedCocktailDetailViewModel.kt) compiles
   - [ ] [`SharedFavoritesViewModel.kt`](shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedFavoritesViewModel.kt) compiles
   - [ ] iOS wrappers work correctly
   - [ ] Android integration functions properly

### Step 1.2: Create SharedOfflineModeViewModel

**Objective**: Implement offline mode functionality with SKIE integration.

**Implementation Steps**:

1. **Create the ViewModel File**
   ```kotlin
   // File: shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedOfflineModeViewModel.kt
   package com.cocktailcraft.viewmodel
   
   import com.cocktailcraft.domain.model.Cocktail
   import com.cocktailcraft.domain.repository.CocktailRepository
   import com.cocktailcraft.util.NetworkMonitor
   import kotlinx.coroutines.flow.*
   import kotlinx.coroutines.launch
   import org.koin.core.component.inject
   
   class SharedOfflineModeViewModel : SharedViewModel() {
       // Implementation following technical specifications
   }
   ```

2. **Implement Core Functionality**
   - Network monitoring
   - Offline mode toggle
   - Cache management
   - Recently viewed cocktails

3. **Testing**
   ```kotlin
   // Create unit tests
   class SharedOfflineModeViewModelTest : BaseKoinTest() {
       // Test implementation
   }
   ```

### Step 1.3: Create SharedOrderViewModel

**Objective**: Implement order management functionality with SKIE integration.

**Implementation Steps**:

1. **Create the ViewModel File**
   ```kotlin
   // File: shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedOrderViewModel.kt
   package com.cocktailcraft.viewmodel
   
   import com.cocktailcraft.domain.model.Order
   import com.cocktailcraft.domain.model.CocktailCartItem
   import com.cocktailcraft.domain.repository.OrderRepository
   import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
   import kotlinx.coroutines.flow.*
   import kotlinx.coroutines.launch
   import org.koin.core.component.inject
   
   class SharedOrderViewModel : SharedViewModel() {
       // Implementation following technical specifications
   }
   ```

2. **Implement Core Functionality**
   - Order placement
   - Order history management
   - Order status tracking
   - Order cancellation

3. **Testing**
   ```kotlin
   class SharedOrderViewModelTest : BaseKoinTest() {
       // Test implementation
   }
   ```

### Step 1.4: Update DomainModule

**Objective**: Register new ViewModels in the dependency injection container.

**Implementation Steps**:

1. **Update DomainModule.kt**
   ```kotlin
   // File: shared/src/commonMain/kotlin/com/cocktailcraft/di/DomainModule.kt
   val domainModule = module {
       // Existing registrations...
       
       // Add new ViewModels
       factory { com.cocktailcraft.viewmodel.SharedOfflineModeViewModel() }
       factory { com.cocktailcraft.viewmodel.SharedOrderViewModel() }
   }
   ```

2. **Verify Registration**
   - Test dependency injection works
   - Ensure no circular dependencies
   - Validate all dependencies are available

### Step 1.5: Create iOS Wrapper Classes

**Objective**: Create iOS-compatible wrapper classes for new ViewModels.

**Implementation Steps**:

1. **Create OfflineModeViewModelWrapper.swift**
   ```swift
   // File: iosApp/CocktailCraft/ViewModels/OfflineModeViewModelWrapper.swift
   import Foundation
   import shared
   
   @MainActor
   class OfflineModeViewModelWrapper: ObservableObject {
       private let sharedViewModel: SharedOfflineModeViewModel
       
       @Published var isOfflineModeEnabled: Bool = false
       @Published var isNetworkAvailable: Bool = true
       @Published var recentlyViewedCocktails: [Cocktail] = []
       @Published var isLoading: Bool = false
       @Published var error: UserFriendlyError?
       
       init() {
           self.sharedViewModel = KoinHelper.shared.getSharedOfflineModeViewModel()
           observeStateChanges()
       }
       
       private func observeStateChanges() {
           // Implement state observation using SKIE AsyncSequence
       }
       
       func toggleOfflineMode() {
           Task {
               await sharedViewModel.toggleOfflineMode()
           }
       }
   }
   ```

2. **Create OrderViewModelWrapper.swift**
   ```swift
   // File: iosApp/CocktailCraft/ViewModels/OrderViewModelWrapper.swift
   // Similar implementation pattern
   ```

3. **Update KoinHelper**
   ```swift
   // Add methods to retrieve new ViewModels
   func getSharedOfflineModeViewModel() -> SharedOfflineModeViewModel {
       return koin.get()
   }
   
   func getSharedOrderViewModel() -> SharedOrderViewModel {
       return koin.get()
   }
   ```

---

## Phase 2: User Experience Features

### Step 2.1: Create SharedProfileViewModel

**Implementation Steps**:

1. **Create ViewModel**
   ```kotlin
   // File: shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedProfileViewModel.kt
   class SharedProfileViewModel : SharedViewModel() {
       // Implementation with authentication logic
   }
   ```

2. **Handle Authentication Complexity**
   - Implement comprehensive error handling
   - Add input validation
   - Manage session state
   - Handle password security

3. **Create iOS Wrapper**
   ```swift
   // File: iosApp/CocktailCraft/ViewModels/ProfileViewModelWrapper.swift
   @MainActor
   class ProfileViewModelWrapper: ObservableObject {
       // Implementation
   }
   ```

### Step 2.2: Create SharedThemeViewModel

**Implementation Steps**:

1. **Create ViewModel**
   ```kotlin
   // File: shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedThemeViewModel.kt
   class SharedThemeViewModel : SharedViewModel() {
       // Implementation with theme management
   }
   ```

2. **Handle System Theme Integration**
   - Monitor system theme changes
   - Persist user preferences
   - Provide smooth transitions

### Step 2.3: Create SharedReviewViewModel

**Implementation Steps**:

1. **Create ViewModel**
   ```kotlin
   // File: shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedReviewViewModel.kt
   class SharedReviewViewModel : SharedViewModel() {
       // Implementation with review management
   }
   ```

2. **Implement Review Logic**
   - Review validation
   - Rating calculations
   - Review persistence

### Step 2.4: Update Android SKIE Wrappers

**Implementation Steps**:

1. **Create Android Wrapper Classes**
   ```kotlin
   // File: androidApp/src/main/java/com/cocktailcraft/viewmodel/OfflineModeViewModelSKIE.kt
   class OfflineModeViewModelSKIE : BaseViewModel() {
       private val sharedViewModel: SharedOfflineModeViewModel by inject()
       
       // Expose shared ViewModel functionality
   }
   ```

2. **Update Existing Wrappers**
   - Follow established patterns
   - Ensure consistency across all wrappers

---

## Phase 3: Integration & Optimization

### Step 3.1: Update Android Screens

**Migration Strategy**:

1. **Gradual Migration Approach**
   ```kotlin
   // Before
   class HomeScreen {
       private val viewModel: HomeViewModel by viewModel()
   }
   
   // After
   class HomeScreen {
       private val viewModel: HomeViewModelSKIE by viewModel()
   }
   ```

2. **Feature Flag Implementation**
   ```kotlin
   // Use feature flags for gradual rollout
   val useSharedViewModels = BuildConfig.USE_SHARED_VIEWMODELS
   
   private val viewModel = if (useSharedViewModels) {
       get<HomeViewModelSKIE>()
   } else {
       get<HomeViewModel>()
   }
   ```

### Step 3.2: Update iOS Views

**Migration Strategy**:

1. **Update View Declarations**
   ```swift
   // Before
   @StateObject private var viewModel = HomeViewModel()
   
   // After
   @StateObject private var viewModel = HomeViewModelWrapper()
   ```

2. **Update State Observations**
   ```swift
   // Update to use @Published properties from wrappers
   ```

### Step 3.3: Testing Strategy

**Comprehensive Testing Approach**:

1. **Unit Tests**
   ```kotlin
   // Test each ViewModel in isolation
   class SharedXxxViewModelTest {
       @Test
       fun `test functionality`() = runTest {
           // Test implementation
       }
   }
   ```

2. **Integration Tests**
   ```kotlin
   // Test ViewModel integration with repositories
   class ViewModelIntegrationTest {
       // Test implementation
   }
   ```

3. **UI Tests**
   ```swift
   // iOS UI tests
   class CocktailCraftUITests: XCTestCase {
       // Test UI integration
   }
   ```

---

## Migration Checklist

### Pre-Migration
- [ ] Development environment set up
- [ ] SKIE plugin configured
- [ ] Existing code compiles successfully
- [ ] Test environment ready

### Phase 1 Completion
- [ ] Existing ViewModels validated
- [ ] SharedOfflineModeViewModel implemented and tested
- [ ] SharedOrderViewModel implemented and tested
- [ ] DomainModule updated
- [ ] iOS wrappers created and tested

### Phase 2 Completion
- [ ] SharedProfileViewModel implemented and tested
- [ ] SharedThemeViewModel implemented and tested
- [ ] SharedReviewViewModel implemented and tested
- [ ] Android SKIE wrappers updated
- [ ] iOS wrappers completed

### Phase 3 Completion
- [ ] Android screens migrated
- [ ] iOS views migrated
- [ ] Comprehensive tests created
- [ ] Performance optimized
- [ ] Documentation completed

### Final Validation
- [ ] End-to-end testing passed
- [ ] Legacy ViewModels removed
- [ ] Code review completed
- [ ] Production deployment ready

---

## Troubleshooting Guide

### Common Issues and Solutions

1. **SKIE Compilation Errors**
   ```
   Error: StateFlow not converted to AsyncSequence
   Solution: Ensure StateFlow is properly exposed with asStateFlow()
   ```

2. **iOS Wrapper Issues**
   ```
   Error: Cannot observe StateFlow changes
   Solution: Use Task with AsyncSequence iteration
   ```

3. **Dependency Injection Problems**
   ```
   Error: ViewModel not found in Koin container
   Solution: Verify registration in DomainModule
   ```

4. **Performance Issues**
   ```
   Issue: Memory leaks in ViewModels
   Solution: Properly cancel coroutines in onCleared()
   ```

### Best Practices

1. **Error Handling**
   - Always use inherited error handling from SharedViewModel
   - Provide user-friendly error messages
   - Implement recovery actions where possible

2. **State Management**
   - Use StateFlow for all observable state
   - Keep state immutable
   - Provide computed properties for derived state

3. **Testing**
   - Test each ViewModel in isolation
   - Mock all dependencies
   - Test error scenarios
   - Verify SKIE integration

4. **Performance**
   - Cancel coroutines properly
   - Avoid memory leaks
   - Optimize state updates
   - Monitor performance metrics

---

## Support and Resources

### Documentation
- [SKIE Documentation](https://skie.touchlab.co/)
- [Kotlin Multiplatform Guide](https://kotlinlang.org/docs/multiplatform.html)
- [Koin Documentation](https://insert-koin.io/)

### Code Examples
- Existing SKIE ViewModels in the project
- Technical specifications document
- Unit test examples

### Getting Help
- Review existing implementations
- Check troubleshooting guide
- Consult team members
- Reference official documentation

This migration guide provides a systematic approach to implementing the SKIE ViewModels integration while minimizing risks and ensuring quality throughout the process.