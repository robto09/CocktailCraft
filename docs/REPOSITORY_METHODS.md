# CocktailCraft — Repository Interfaces

## Overview

The former monolithic `CocktailRepository` was split into focused, single-concern repositories. There are **nine repository interfaces**, all in `core/domain/src/commonMain/kotlin/com/cocktailcraft/domain/repository/` (`:core:domain`), with `internal` implementations in `core/data/src/commonMain/kotlin/com/cocktailcraft/data/repository/` (`:core:data`) (plus the `CocktailCategoryFetcher` helper shared by the search and catalog implementations). All repositories are Kotlin Multiplatform and behave identically on Android and iOS.

**The KDoc on the interfaces is the source of truth** — this page is a quick index, kept deliberately brief.

## Conventions

- Operations are `suspend` functions returning `Result<T>` (`com.cocktailcraft.domain.util.Result`), not `Flow`s.
- Mutable stores (favorites, cart, orders, reviews) additionally expose an `observe*(): Flow<List<T>>` stream: SQLDelight `asFlow().mapToList(...)` over their tables, which emits the stored list on collection and again after every write to those tables (from any repository).
- Implementations confine database and Settings I/O and JSON (de)serialization to an injected IO dispatcher, and multi-statement writes run in one transaction.

## Storage

Everything except auth lives in one SQLite database (`cocktailcraft_cache.db`, schema in `core/data/src/commonMain/sqldelight/`):

| Data | Table(s) | Owner |
|------|----------|-------|
| Offline cocktail cache, recently viewed | `cached_cocktail`, `recently_viewed_cocktail` | `CocktailCache` |
| Cart | `cart_item` (cocktail snapshot as JSON, quantity) | `CartRepositoryImpl` |
| Orders | `customer_order`, `customer_order_item` | `OrderRepositoryImpl` |
| Favorites | `favorite_cocktail` (cocktail snapshot as JSON) | `CocktailFavoritesRepositoryImpl` |
| Reviews | `cocktail_review` | `ReviewRepositoryImpl` |

- The cart, order, favorites and review repositories reach the database through `LocalDatabase` (`data/local/`), which opens it lazily on the IO dispatcher. On the first open after upgrading from a version that kept this data as JSON in `Settings`, `LegacySettingsImport` copies it into the tables (one transaction per store, `INSERT OR IGNORE`, so it is safe to re-run) and then deletes the old keys. Unreadable JSON is dropped entry by entry.
- Every schema change needs a numbered migration in `core/data/src/commonMain/sqldelight/migrations/` and a regenerated snapshot in `databases/`; `verifySqlDelightMigration` (part of `check`) fails otherwise.
- Auth data (accounts, password hashes, session, sign-in throttling, guest preferences) deliberately stays in the platform-secure `Settings` store: it holds credentials and PII, and the database file is not encrypted.

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
Single-cocktail lookups.

```kotlin
suspend fun getCocktailById(id: String): Result<Cocktail?>
suspend fun refreshCocktailById(id: String): Result<Cocktail?>   // bypasses cache
suspend fun getRandomCocktail(): Result<Cocktail?>
```

### CocktailCatalogRepository
Catalog/browse metadata and the default listing.

```kotlin
suspend fun getCategories(): Result<List<String>>
suspend fun getIngredients(): Result<List<String>>
suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>>
```

### CocktailFavoritesRepository
Favorites persistence and observation. Each favorite stores the full cocktail, so favorites work offline regardless of cache eviction (SH-4).

```kotlin
fun observeFavorites(): Flow<List<Cocktail>>   // re-emits after every add/remove/upgrade (SH-4)
suspend fun getFavoriteCocktails(): Result<List<Cocktail>>
suspend fun addToFavorites(cocktail: Cocktail): Result<Unit>
suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit>
suspend fun isCocktailFavorite(id: String): Result<Boolean>
```

### CocktailOfflineRepository
Connectivity state and recently viewed cocktails.

```kotlin
suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>>
suspend fun isOffline(): Boolean   // network currently down
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
```

### OrderRepository
Order placement, history, and status.

```kotlin
fun observeOrders(): Flow<List<Order>>
suspend fun getOrders(): Result<List<Order>>
suspend fun placeOrderAndClearCart(order: Order): Result<Unit>   // checkout: order insert + cart clear in one transaction
suspend fun getOrderById(id: String): Result<Order?>
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

- Koin bindings: `core/data/src/commonMain/kotlin/com/cocktailcraft/di/DataModule.kt` (see [DependencyInjection.md](DependencyInjection.md))
- Consumers inject the interfaces (mostly via use cases in `domain/usecase/`), never the `*Impl` classes.
