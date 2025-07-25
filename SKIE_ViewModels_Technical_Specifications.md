# SKIE ViewModels Technical Specifications

## Overview

This document provides detailed technical specifications for implementing the remaining SKIE ViewModels in the CocktailCraft project. Each specification includes interface definitions, implementation patterns, and SKIE-specific considerations.

---

## 1. SharedOfflineModeViewModel

### Purpose
Manages offline mode functionality, network connectivity monitoring, and cached data access.

### Dependencies
- `NetworkMonitor` - Network connectivity monitoring
- `CocktailRepository` - Data access and offline mode management
- `CocktailCache` - Cache management operations

### State Management

```kotlin
class SharedOfflineModeViewModel : SharedViewModel() {
    
    // Core state - SKIE will convert to Swift AsyncSequence
    private val _isOfflineModeEnabled = MutableStateFlow(false)
    val isOfflineModeEnabled: StateFlow<Boolean> = _isOfflineModeEnabled.asStateFlow()
    
    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
    
    private val _recentlyViewedCocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val recentlyViewedCocktails: StateFlow<List<Cocktail>> = _recentlyViewedCocktails.asStateFlow()
    
    private val _cacheSize = MutableStateFlow(0)
    val cacheSize: StateFlow<Int> = _cacheSize.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()
}
```

### Key Functions

```kotlin
// SKIE will convert these to Swift async functions
suspend fun toggleOfflineMode()
suspend fun setOfflineMode(enabled: Boolean)
suspend fun syncCachedData()
suspend fun clearCache()
suspend fun loadRecentlyViewedCocktails()

// Synchronous helpers - directly callable from Swift
fun getCachedCocktailCount(): Int
fun getNetworkStatus(): String
fun getOfflineModeStatus(): String
```

### Implementation Notes
- Monitor network connectivity in `init` block
- Auto-enable offline mode when network is lost
- Provide cache size and sync status information
- Handle graceful degradation when offline

---

## 2. SharedOrderViewModel

### Purpose
Manages order placement, order history, and order status tracking.

### Dependencies
- `OrderRepository` - Order data management
- `PlaceOrderUseCase` - Business logic for order placement
- `CartRepository` - Cart data for order creation

### State Management

```kotlin
class SharedOrderViewModel : SharedViewModel() {
    
    // Order state - SKIE will convert to Swift AsyncSequence
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    
    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder.asStateFlow()
    
    private val _orderCount = MutableStateFlow(0)
    val orderCount: StateFlow<Int> = _orderCount.asStateFlow()
    
    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()
}
```

### Key Functions

```kotlin
// SKIE will convert these to Swift async functions
suspend fun loadOrders()
suspend fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double): Boolean
suspend fun getOrderById(orderId: String): Order?
suspend fun updateOrderStatus(orderId: String, status: String)
suspend fun cancelOrder(orderId: String): Boolean
suspend fun reorderItems(orderId: String)

// Synchronous helpers
fun getOrdersByStatus(status: String): List<Order>
fun getTotalSpent(): Double
fun getOrderHistory(limit: Int = 10): List<Order>
```

### Implementation Notes
- Generate unique order IDs using timestamp
- Map cart items to order items properly
- Handle order status transitions
- Provide order analytics and history

---

## 3. SharedProfileViewModel

### Purpose
Manages user authentication, profile data, and account settings.

### Dependencies
- `AuthRepository` - Authentication and profile management
- `User` model - User data structure
- `UserPreferences` model - User preference data

### State Management

```kotlin
class SharedProfileViewModel : SharedViewModel() {
    
    // User state - SKIE will convert to Swift AsyncSequence
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()
    
    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()
    
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
}
```

### Key Functions

```kotlin
// SKIE will convert these to Swift async functions
suspend fun signIn(email: String, password: String): Boolean
suspend fun signUp(name: String, email: String, password: String): Boolean
suspend fun signOut(): Boolean
suspend fun resetPassword(email: String): Boolean
suspend fun updateProfile(name: String, email: String): Boolean
suspend fun updateAddress(address: Address): Boolean
suspend fun changePassword(oldPassword: String, newPassword: String): Boolean

// Synchronous helpers
fun isEmailValid(email: String): Boolean
fun isPasswordValid(password: String): Boolean
fun getDisplayName(): String
fun getInitials(): String
```

### Implementation Notes
- Implement comprehensive input validation
- Handle authentication errors gracefully
- Provide user-friendly error messages
- Support profile picture management
- Manage session persistence

---

## 4. SharedThemeViewModel

### Purpose
Manages application theme preferences and system theme integration.

### Dependencies
- `AuthRepository` - For preference persistence
- `UserPreferences` model - Theme preference data

### State Management

```kotlin
class SharedThemeViewModel : SharedViewModel() {
    
    // Theme state - SKIE will convert to Swift AsyncSequence
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _followSystemTheme = MutableStateFlow(true)
    val followSystemTheme: StateFlow<Boolean> = _followSystemTheme.asStateFlow()
    
    private val _currentTheme = MutableStateFlow("system")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()
    
    private val _isSystemInDarkMode = MutableStateFlow(false)
    val isSystemInDarkMode: StateFlow<Boolean> = _isSystemInDarkMode.asStateFlow()
}
```

### Key Functions

```kotlin
// SKIE will convert these to Swift async functions
suspend fun setDarkMode(enabled: Boolean)
suspend fun toggleDarkMode()
suspend fun setFollowSystemTheme(enabled: Boolean)
suspend fun updateSystemDarkMode(isDark: Boolean)
suspend fun resetToDefaults()

// Synchronous helpers
fun getThemeDisplayName(): String
fun shouldUseDarkMode(): Boolean
fun getAvailableThemes(): List<String>
```

### Implementation Notes
- React to system theme changes automatically
- Persist theme preferences
- Provide smooth theme transitions
- Support custom theme options in future

---

## 5. SharedReviewViewModel

### Purpose
Manages cocktail reviews, ratings, and review submission.

### Dependencies
- `Review` model - Review data structure
- Potential `ReviewRepository` - Review data management (may need to be created)

### State Management

```kotlin
class SharedReviewViewModel : SharedViewModel() {
    
    // Review state - SKIE will convert to Swift AsyncSequence
    private val _reviews = MutableStateFlow<Map<String, List<Review>>>(emptyMap())
    val reviews: StateFlow<Map<String, List<Review>>> = _reviews.asStateFlow()
    
    private val _currentCocktailReviews = MutableStateFlow<List<Review>>(emptyList())
    val currentCocktailReviews: StateFlow<List<Review>> = _currentCocktailReviews.asStateFlow()
    
    private val _averageRating = MutableStateFlow(0.0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()
    
    private val _reviewCount = MutableStateFlow(0)
    val reviewCount: StateFlow<Int> = _reviewCount.asStateFlow()
}
```

### Key Functions

```kotlin
// SKIE will convert these to Swift async functions
suspend fun loadReviewsForCocktail(cocktailId: String)
suspend fun submitReview(cocktailId: String, rating: Float, comment: String, userName: String): Boolean
suspend fun updateReview(reviewId: String, rating: Float, comment: String): Boolean
suspend fun deleteReview(reviewId: String): Boolean
suspend fun loadAllReviews()

// Synchronous helpers
fun getAverageRating(cocktailId: String): Float
fun getReviewCount(cocktailId: String): Int
fun getReviewsForCocktail(cocktailId: String): List<Review>
fun validateReview(rating: Float, comment: String): Boolean
```

### Implementation Notes
- Implement review validation (rating 1-5, comment length)
- Calculate average ratings efficiently
- Support review editing and deletion
- Handle anonymous vs authenticated reviews
- Provide review sorting options

---

## SKIE Integration Best Practices

### 1. StateFlow Patterns

```kotlin
// ✅ Correct - SKIE compatible
private val _state = MutableStateFlow(initialValue)
val state: StateFlow<Type> = _state.asStateFlow()

// ❌ Avoid - Not SKIE compatible
val state: Flow<Type> = flow { /* ... */ }
```

### 2. Suspend Function Patterns

```kotlin
// ✅ Correct - Converts to Swift async
suspend fun performAction(): ResultType {
    return try {
        // Implementation
        result
    } catch (e: Exception) {
        handleException(e, "Action failed")
        throw e
    }
}

// ✅ Also correct - Boolean return for success/failure
suspend fun performAction(): Boolean {
    return try {
        // Implementation
        true
    } catch (e: Exception) {
        handleException(e, "Action failed", showAsEvent = true)
        false
    }
}
```

### 3. Error Handling Patterns

```kotlin
// Use inherited error handling from SharedViewModel
try {
    // Operation
    setLoading(false)
} catch (e: Exception) {
    handleException(
        e, 
        "User-friendly message",
        showAsEvent = true, // For one-time errors
        recoveryAction = ErrorHandler.RecoveryAction("Retry") { retry() }
    )
}
```

### 4. Computed Properties

```kotlin
// ✅ Synchronous computed properties work well with SKIE
val isEmpty: Boolean
    get() = _items.value.isEmpty()

val hasItems: Boolean
    get() = _items.value.isNotEmpty()
```

### 5. Collection Handling

```kotlin
// ✅ SKIE handles Kotlin collections well
fun getItemsByCategory(category: String): List<Item> {
    return _items.value.filter { it.category == category }
}

// ✅ Maps are also supported
fun getItemsGroupedByCategory(): Map<String, List<Item>> {
    return _items.value.groupBy { it.category }
}
```

---

## iOS Wrapper Pattern

Each shared ViewModel should have a corresponding iOS wrapper:

```swift
@MainActor
class OfflineModeViewModelWrapper: ObservableObject {
    private let sharedViewModel: SharedOfflineModeViewModel
    
    @Published var isOfflineModeEnabled: Bool = false
    @Published var isNetworkAvailable: Bool = true
    @Published var recentlyViewedCocktails: [Cocktail] = []
    @Published var isLoading: Bool = false
    @Published var error: UserFriendlyError?
    
    init(sharedViewModel: SharedOfflineModeViewModel) {
        self.sharedViewModel = sharedViewModel
        observeStateChanges()
    }
    
    private func observeStateChanges() {
        // Observe StateFlows using SKIE's AsyncSequence conversion
        Task {
            for await value in sharedViewModel.isOfflineModeEnabled {
                self.isOfflineModeEnabled = value
            }
        }
        // ... other observations
    }
    
    func toggleOfflineMode() {
        Task {
            await sharedViewModel.toggleOfflineMode()
        }
    }
}
```

---

## Testing Strategy

### Unit Test Structure

```kotlin
class SharedXxxViewModelTest : BaseKoinTest() {
    
    private lateinit var viewModel: SharedXxxViewModel
    private lateinit var mockRepository: MockRepository
    
    @BeforeEach
    fun setup() {
        mockRepository = MockRepository()
        viewModel = SharedXxxViewModel()
    }
    
    @Test
    fun `test state flow updates correctly`() = runTest {
        // Given
        val expectedValue = "test"
        
        // When
        viewModel.performAction(expectedValue)
        
        // Then
        assertEquals(expectedValue, viewModel.state.value)
    }
    
    @Test
    fun `test error handling`() = runTest {
        // Given
        mockRepository.shouldThrowError = true
        
        // When
        viewModel.performAction()
        
        // Then
        assertNotNull(viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }
}
```

This technical specification provides the detailed guidance needed to implement each ViewModel following SKIE best practices and maintaining consistency with the existing codebase.