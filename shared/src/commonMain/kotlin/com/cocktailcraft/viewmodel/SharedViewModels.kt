package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Shared interfaces for ViewModels that can be implemented on both Android and iOS.
 * These interfaces define the contract that platform-specific ViewModels must implement.
 *
 * This approach allows for:
 * 1. Shared business logic between platforms
 * 2. Platform-specific implementations where needed
 * 3. Clean architecture boundaries between UI and domain layers
 */

/**
 * Interface for the HomeViewModel functionality.
 * This defines the contract that both Android and iOS implementations must follow.
 */
interface IHomeViewModel {
    // State
    val cocktails: StateFlow<List<Cocktail>>
    val hasMoreData: StateFlow<Boolean>
    val isLoadingMore: StateFlow<Boolean>
    val searchQuery: StateFlow<String>
    val isSearchActive: StateFlow<Boolean>
    val searchFilters: StateFlow<SearchFilters>
    val isAdvancedSearchActive: StateFlow<Boolean>
    val isOfflineMode: StateFlow<Boolean>
    val isNetworkAvailable: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>

    // Actions
    fun loadCocktails()
    fun loadMoreCocktails()
    fun searchCocktails(query: String)
    fun updateSearchFilters(filters: SearchFilters)
    fun clearSearchFilters()
    fun toggleAdvancedSearchMode(active: Boolean)
    fun toggleSearchMode(active: Boolean)
    fun loadCocktailsByCategory(category: String?)
    fun sortByPrice(ascending: Boolean)
    fun sortByPopularity()
    fun setOfflineMode(enabled: Boolean)
    fun retry()
    fun getCocktailById(id: String): Flow<Cocktail?>
}

/**
 * Interface for the CocktailDetailViewModel functionality.
 */
interface ICocktailDetailViewModel {
    // State
    val cocktail: StateFlow<Cocktail?>
    val isFavorite: StateFlow<Boolean>
    val recommendations: StateFlow<List<Cocktail>>
    val isLoading: StateFlow<Boolean>

    // Actions
    fun loadCocktailDetails(id: String)
    fun toggleFavorite()
    fun loadRandomCocktail()
}

/**
 * Interface for the CartViewModel functionality.
 */
interface ICartViewModel {
    // State
    val cartItems: StateFlow<List<CocktailCartItem>>
    val totalPrice: StateFlow<Double>
    val isLoading: StateFlow<Boolean>

    // Actions
    fun loadCartItems()
    fun addToCart(cocktail: Cocktail, quantity: Int = 1)
    fun removeFromCart(cocktailId: String)
    fun updateQuantity(cocktailId: String, quantity: Int)
    fun clearCart()
}

/**
 * Interface for the FavoritesViewModel functionality.
 */
interface IFavoritesViewModel {
    // State
    val favorites: StateFlow<List<Cocktail>>
    val isLoading: StateFlow<Boolean>

    // Actions
    fun loadFavorites()
    fun addToFavorites(cocktail: Cocktail)
    fun removeFromFavorites(cocktail: Cocktail)
    fun toggleFavorite(cocktail: Cocktail)
    fun isFavorite(id: String): Boolean
}

/**
 * Interface for the ThemeViewModel functionality.
 */
interface IThemeViewModel {
    // State
    val isDarkMode: StateFlow<Boolean>
    val followSystemTheme: StateFlow<Boolean>

    // Actions
    fun updateSystemDarkMode(isDark: Boolean)
    fun toggleDarkMode()
    fun setDarkMode(enabled: Boolean)
    fun toggleFollowSystemTheme()
}

/**
 * Interface for the OfflineModeViewModel functionality.
 */
interface IOfflineModeViewModel {
    // State
    val isOfflineModeEnabled: StateFlow<Boolean>
    val isNetworkAvailable: StateFlow<Boolean>
    val recentlyViewedCocktails: StateFlow<List<Cocktail>>
    val isLoading: StateFlow<Boolean>

    // Actions
    fun toggleOfflineMode()
    fun setOfflineMode(enabled: Boolean)
    fun loadRecentlyViewedCocktails()
    fun clearCache()
    fun getCachedCocktailCount(): Int
}

/**
 * Interface for the OrderViewModel functionality.
 */
interface IOrderViewModel {
    // State
    val orders: StateFlow<List<Order>>
    val isLoading: StateFlow<Boolean>

    // Actions
    fun loadOrders()
    fun addOrder(order: Order)
    fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double)
    fun updateOrderStatus(orderId: String, status: OrderStatus)
    fun cancelOrder(orderId: String)
    fun getOrderById(orderId: String): Flow<Order?>
}

/**
 * Interface for the ProfileViewModel functionality.
 */
interface IProfileViewModel {
    // State
    val user: StateFlow<User?>
    val isSignedIn: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>

    // Actions
    fun signIn(email: String, password: String)
    fun signUp(name: String, email: String, password: String)
    fun signOut()
    fun updateUserName(name: String)
}

/**
 * Interface for the ReviewViewModel functionality.
 */
interface IReviewViewModel {
    // State
    val reviews: StateFlow<Map<String, List<Review>>>
    val isLoading: StateFlow<Boolean>

    // Actions
    fun getReviewsForCocktail(cocktailId: String): List<Review>
    fun addReview(review: Review)
    fun getAverageRating(cocktailId: String): Float
    fun createAndAddReview(cocktailId: String, userName: String, rating: Float, comment: String)
}
