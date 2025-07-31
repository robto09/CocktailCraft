# CocktailCraft Android App - Repository Methods Documentation

## Overview

This document provides comprehensive documentation for all repository methods available in the CocktailCraft Android application. The repositories are implemented using Kotlin Multiplatform and are located in the shared module.

## Repository Architecture

The application follows a clean architecture pattern with:
- **Interfaces**: Located in `shared/src/commonMain/kotlin/com/cocktailcraft/domain/repository/`
- **Implementations**: Located in `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/`
- **Platform-agnostic**: All repositories work across Android and iOS

## Repository Interfaces

### 1. CocktailRepository

Main repository for cocktail data management and search functionality.

#### Core Search Methods

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `searchCocktailsByName` | `Flow<List<Cocktail>>` | `name: String` | Search cocktails by name with fuzzy matching |
| `searchCocktailsByFirstLetter` | `Flow<List<Cocktail>>` | `letter: Char` | Browse cocktails alphabetically |
| `getCocktailById` | `Flow<Cocktail?>` | `id: String` | Get detailed cocktail information |
| `refreshCocktailById` | `Cocktail?` | `id: String` | Force refresh cocktail data from API |
| `getRandomCocktail` | `Flow<Cocktail?>` | None | Get a random cocktail for discovery |

#### Filter Methods

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `filterByIngredient` | `Flow<List<Cocktail>>` | `ingredient: String` | Filter cocktails containing specific ingredient |
| `filterByAlcoholic` | `Flow<List<Cocktail>>` | `alcoholic: Boolean` | Filter alcoholic/non-alcoholic cocktails |
| `filterByCategory` | `Flow<List<Cocktail>>` | `category: String` | Filter by cocktail category (e.g., "Ordinary Drink") |
| `filterByGlass` | `Flow<List<Cocktail>>` | `glass: String` | Filter by glass type (e.g., "Highball glass") |

#### Metadata Methods

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `getCategories` | `Flow<List<String>>` | None | Get all available cocktail categories |
| `getGlasses` | `Flow<List<String>>` | None | Get all glass types |
| `getIngredients` | `Flow<List<String>>` | None | Get all available ingredients |
| `getAlcoholicFilters` | `Flow<List<String>>` | None | Get alcoholic filter options |

#### Favorites Management

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `getFavoriteCocktails` | `Flow<List<Cocktail>>` | None | Get user's favorite cocktails |
| `addToFavorites` | `Unit` | `cocktail: Cocktail` | Add cocktail to favorites |
| `removeFromFavorites` | `Unit` | `cocktail: Cocktail` | Remove cocktail from favorites |
| `isCocktailFavorite` | `Flow<Boolean>` | `id: String` | Check if cocktail is favorited |

#### Sorting and Pagination

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `getCocktailsSortedByNewest` | `Flow<List<Cocktail>>` | None | Get cocktails sorted by date added |
| `getCocktailsSortedByPriceLowToHigh` | `Flow<List<Cocktail>>` | None | Sort cocktails by price ascending |
| `getCocktailsSortedByPriceHighToLow` | `Flow<List<Cocktail>>` | None | Sort cocktails by price descending |
| `getCocktailsSortedByPopularity` | `Flow<List<Cocktail>>` | None | Sort cocktails by popularity/rating |
| `getCocktailsByPriceRange` | `Flow<List<Cocktail>>` | `minPrice: Double, maxPrice: Double` | Filter cocktails by price range |

#### Advanced Search

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `advancedSearch` | `Flow<List<Cocktail>>` | `filters: SearchFilters` | Complex multi-filter search with taste profiles, complexity, etc. |

#### Utility Methods

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `getCocktailImageUrl` | `String` | `cocktail: Cocktail` | Get optimized image URL with fallbacks |
| `checkApiConnectivity` | `Flow<Boolean>` | None | Check API connection status |

#### Offline Mode Support

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `getRecentlyViewedCocktails` | `Flow<List<Cocktail>>` | None | Get recently viewed cocktails |
| `setOfflineMode` | `Unit` | `enabled: Boolean` | Enable/disable offline mode |
| `isOfflineModeEnabled` | `Boolean` | None | Check offline mode status |

#### Recommendation Methods

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `getCocktailsByCategory` | `List<Cocktail>` | `category: String` | Get cocktails for recommendations |
| `getCocktailsByIngredient` | `List<Cocktail>` | `ingredient: String` | Get cocktails for ingredient-based recommendations |
| `getCocktailsByAlcoholicFilter` | `List<Cocktail>` | `alcoholicFilter: String` | Get cocktails for alcoholic preference recommendations |

### 2. AuthRepository

Repository for user authentication and profile management.

#### Authentication Methods

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `signUp` | `Flow<Boolean>` | `email: String, password: String` | Register new user account |
| `signIn` | `Flow<Boolean>` | `email: String, password: String` | Authenticate user login |
| `signOut` | `Flow<Boolean>` | None | Sign out current user |
| `resetPassword` | `Flow<Boolean>` | `email: String` | Send password reset email |
| `changePassword` | `Flow<Boolean>` | `oldPassword: String, newPassword: String` | Change user password |
| `isUserSignedIn` | `Flow<Boolean>` | None | Check if user is authenticated |
| `getCurrentUser` | `Flow<User?>` | None | Get current user data |

#### Profile Management

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `updateUserName` | `Flow<Boolean>` | `name: String` | Update user display name |
| `updateUserEmail` | `Flow<Boolean>` | `email: String, password: String` | Update user email address |
| `updateUserAddress` | `Flow<Boolean>` | `address: Address` | Update user shipping address |

#### Preferences Management

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `updateUserPreferences` | `Flow<Boolean>` | `preferences: UserPreferences` | Update user app preferences |
| `getUserPreferences` | `Flow<UserPreferences>` | None | Get user preferences |

### 3. FavoritesRepository

Repository for managing user's favorite cocktails.

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `getFavorites` | `Flow<List<Cocktail>>` | None | Get all favorite cocktails |
| `addToFavorites` | `Unit` | `cocktail: Cocktail` | Add cocktail to favorites |
| `removeFromFavorites` | `Unit` | `cocktail: Cocktail` | Remove cocktail from favorites |
| `isFavorite` | `Flow<Boolean>` | `id: String` | Check if cocktail is favorited |
| `toggleFavorite` | `Unit` | `cocktail: Cocktail` | Toggle favorite status |

### 4. CartRepository

Repository for shopping cart management.

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `getCartItems` | `Flow<List<CocktailCartItem>>` | None | Get all items in cart |
| `addToCart` | `Unit` | `cartItem: CocktailCartItem` | Add item to shopping cart |
| `removeFromCart` | `Unit` | `cocktailId: String` | Remove item from cart |
| `updateQuantity` | `Unit` | `cocktailId: String, quantity: Int` | Update item quantity |
| `clearCart` | `Unit` | None | Remove all items from cart |
| `getCartTotal` | `Flow<Double>` | None | Calculate total cart value |

### 5. OrderRepository

Repository for order management and history.

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| `getOrders` | `Flow<List<Order>>` | None | Get all user orders |
| `addOrder` | `Unit` | `order: Order` | Add new order |
| `getOrderById` | `Flow<Order?>` | `id: String` | Get specific order details |
| `updateOrderStatus` | `Unit` | `id: String, status: String` | Update order status |
| `deleteOrder` | `Unit` | `id: String` | Delete order record |
| `placeOrder` | `Flow<Boolean>` | `order: Order` | Place new order |
| `getOrderHistory` | `Flow<List<Order>>` | None | Get user's order history |
| `cancelOrder` | `Flow<Boolean>` | `orderId: String` | Cancel existing order |

## Data Models

### Core Models

#### Cocktail
Complete cocktail data including:
- Basic info (id, name, description)
- Ingredients and measurements
- Instructions
- Pricing and availability
- Ratings and reviews
- Images and media

#### CocktailCartItem
Shopping cart item containing:
- Cocktail reference
- Quantity
- Price per item
- Total price

#### User
User profile data:
- Email and authentication info
- Display name
- Shipping address
- Preferences and settings

#### Order
Order information:
- Order ID and timestamp
- Items and quantities
- Total price and tax
- Status and tracking
- Shipping information

#### SearchFilters
Advanced search parameters:
- Taste profiles (SWEET, SOUR, BITTER, SPICY, FRUITY, HERBAL, CREAMY)
- Complexity levels (EASY, MEDIUM, COMPLEX)
- Preparation time (QUICK, MEDIUM, LONG)
- Price ranges
- Ingredient requirements

## Data Access Patterns

### Return Types

- **Flow<T>**: Reactive data streams for real-time updates (most read operations)
- **suspend fun**: Coroutine-based async operations for write operations
- **Non-suspend**: Synchronous utility functions

### Implementation Features

- **Caching**: Aggressive local caching for offline support
- **Offline Mode**: Full functionality with cached data when network unavailable
- **Rate Limiting**: API rate limiting with exponential backoff
- **Error Handling**: Comprehensive error handling with graceful fallbacks
- **Reactive Updates**: Flow-based reactive data streams for UI updates

### Storage

- **Local Storage**: Uses Kotlin Multiplatform Settings library
- **JSON Serialization**: Kotlinx.serialization for data persistence
- **In-Memory Cache**: Additional caching layer for performance optimization

## Usage Examples

### Android Activity/Fragment Usage

```kotlin
// Inject repository using dependency injection
class MainActivity : AppCompatActivity() {
    private val cocktailRepository: CocktailRepository by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Search for cocktails
        lifecycleScope.launch {
            cocktailRepository.searchCocktailsByName("Mojito")
                .collect { cocktails ->
                    // Update UI with search results
                }
        }
        
        // Get user favorites
        lifecycleScope.launch {
            cocktailRepository.getFavoriteCocktails()
                .collect { favorites ->
                    // Update favorites UI
                }
        }
    }
}
```

### Compose Usage

```kotlin
@Composable
fun CocktailScreen(
    cocktailRepository: CocktailRepository
) {
    val cocktails by cocktailRepository.searchCocktailsByName("Martini")
        .collectAsState(initial = emptyList())
    
    LazyColumn {
        items(cocktails) { cocktail ->
            CocktailCard(
                cocktail = cocktail,
                onFavoriteClick = { 
                    cocktailRepository.addToFavorites(cocktail)
                }
            )
        }
    }
}
```

## Platform Support

All repositories are implemented using Kotlin Multiplatform and work identically on:
- **Android**: Full support with all Android-specific optimizations
- **iOS**: Full support through Kotlin/Native compilation
- **Shared Logic**: Business logic shared between platforms

## Performance Considerations

- **Caching**: All data is cached locally for fast access
- **Pagination**: Large datasets support pagination for memory efficiency
- **Background Processing**: Heavy operations run on background threads
- **Reactive Updates**: Efficient UI updates through Flow streams
- **Offline Support**: Full functionality without network connectivity