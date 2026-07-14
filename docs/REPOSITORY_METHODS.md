# CocktailCraft — Repository Interfaces

## Overview

The former monolithic `CocktailRepository` was split into focused, single-concern repositories. There are **nine repository interfaces**, all in `shared/src/commonMain/kotlin/com/cocktailcraft/domain/repository/`, with implementations in `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/` (plus the `CocktailCategoryFetcher` helper shared by the search and catalog implementations). All repositories are Kotlin Multiplatform and behave identically on Android and iOS.

**The KDoc on the interfaces is the source of truth** — this page is a quick index, kept deliberately brief.

## Conventions

- Operations are `suspend` functions returning `Result<T>` (`com.cocktailcraft.domain.util.Result`), not `Flow`s.
- Mutable stores (favorites, cart, orders, reviews) additionally expose a hot `observe*(): Flow<List<T>>` stream, seeded on first collection and re-published after every mutation.
- Implementations confine Settings I/O and JSON (de)serialization to an injected IO dispatcher.

## Cocktail data (the split of the old monolith)

### CocktailSearchRepository
Search and single-dimension filtering against TheCocktailDB.

```kotlin
suspend fun searchCocktailsByName(name: String): Result<List<Cocktail>>
suspend fun advancedSearch(filters: SearchFilters): Result<List<Cocktail>>   // multi-filter ID-set intersection, see AdvancedSearch.md
suspend fun filterByIngredient(ingredient: String): Result<List<Cocktail>>
suspend fun filterByAlcoholic(alcoholic: Boolean): Result<List<Cocktail>>
suspend fun filterByCategory(category: String): Result<List<Cocktail>>
```

### CocktailDetailRepository
Single-cocktail lookups and image URL resolution.

```kotlin
suspend fun getCocktailById(id: String): Result<Cocktail?>
suspend fun refreshCocktailById(id: String): Result<Cocktail?>   // bypasses cache
suspend fun getRandomCocktail(): Result<Cocktail?>
fun getCocktailImageUrl(cocktail: Cocktail): String
```

### CocktailCatalogRepository
Catalog/browse metadata and the default listing.

```kotlin
suspend fun getCategories(): Result<List<String>>
suspend fun getIngredients(): Result<List<String>>
suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>>
```

### CocktailFavoritesRepository
Favorites persistence and observation.

```kotlin
fun observeFavorites(): Flow<List<Cocktail>>   // hot stream, re-published after every mutation (SH-4)
suspend fun getFavoriteCocktails(): Result<List<Cocktail>>
suspend fun addToFavorites(cocktail: Cocktail): Result<Unit>
suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit>
suspend fun isCocktailFavorite(id: String): Result<Boolean>
```

### CocktailOfflineRepository
Offline mode, connectivity, recently viewed, and cache clearing.

```kotlin
suspend fun checkApiConnectivity(): Result<Boolean>
suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>>
suspend fun setOfflineMode(enabled: Boolean)
suspend fun isOfflineModeEnabled(): Boolean
suspend fun isOffline(): Boolean   // user forced offline OR network currently down
suspend fun clearCache(): Result<Unit>
```

## Commerce and account

### CartRepository
Shopping cart persistence and observation.

```kotlin
fun observeCartItems(): Flow<List<CocktailCartItem>>
suspend fun getCartItems(): Result<List<CocktailCartItem>>
suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit>
suspend fun removeFromCart(cocktailId: String): Result<Unit>
suspend fun updateQuantity(cocktailId: String, quantity: Int): Result<Unit>
suspend fun clearCart(): Result<Unit>
suspend fun getCartTotal(): Result<Double>
```

### OrderRepository
Order placement, history, and status.

```kotlin
fun observeOrders(): Flow<List<Order>>
suspend fun getOrders(): Result<List<Order>>
suspend fun placeOrder(order: Order): Result<Unit>
suspend fun getOrderById(id: String): Result<Order?>
suspend fun updateOrderStatus(id: String, status: String): Result<Unit>
suspend fun deleteOrder(id: String): Result<Unit>
suspend fun cancelOrder(orderId: String): Result<Boolean>
```

### AuthRepository
Authentication, profile, and preferences. Backed by the platform-secure `Settings` store (EncryptedSharedPreferences / Keychain).

```kotlin
// Authentication
suspend fun signUp(email: String, password: String): Result<Boolean>
suspend fun signIn(email: String, password: String): Result<Boolean>
suspend fun signOut(): Result<Boolean>
suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean>
suspend fun isUserSignedIn(): Result<Boolean>
suspend fun getCurrentUser(): Result<User?>

// Profile
suspend fun updateUserName(name: String): Result<Boolean>
suspend fun updateUserEmail(email: String, password: String): Result<Boolean>
suspend fun updateUserAddress(address: Address): Result<Boolean>

// Preferences
suspend fun updateUserPreferences(preferences: UserPreferences): Result<Boolean>
suspend fun getUserPreferences(): Result<UserPreferences>
```

### ReviewRepository
Review storage and observation.

```kotlin
fun observeReviews(): Flow<List<Review>>
suspend fun getReviews(): Result<List<Review>>
suspend fun addReview(review: Review): Result<Unit>
suspend fun updateReview(reviewId: String, rating: Float, comment: String, date: String): Result<Boolean>   // false when reviewId not found
suspend fun deleteReview(reviewId: String): Result<Boolean>                                                 // false when reviewId not found
```

## Where things are wired

- Koin bindings: `shared/src/commonMain/kotlin/com/cocktailcraft/di/DataModule.kt` (see [DependencyInjection.md](DependencyInjection.md))
- Consumers inject the interfaces (mostly via use cases in `domain/usecase/`), never the `*Impl` classes.
