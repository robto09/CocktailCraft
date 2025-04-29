package com.cocktailcraft.viewmodel.android

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.usecase.*
import com.cocktailcraft.viewmodel.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

/**
 * Android-specific implementation of IHomeViewModel.
 * This class directly implements the interface using the provided use cases.
 */
class HomeViewModel(
    private val getCocktailsUseCase: GetCocktailsUseCase,
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase
) : IHomeViewModel {
    // State
    override val cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    override val hasMoreData = MutableStateFlow(true)
    override val isLoadingMore = MutableStateFlow(false)
    override val searchQuery = MutableStateFlow("")
    override val isSearchActive = MutableStateFlow(false)
    override val searchFilters = MutableStateFlow(SearchFilters())
    override val isAdvancedSearchActive = MutableStateFlow(false)
    override val isOfflineMode = MutableStateFlow(false)
    override val isNetworkAvailable = MutableStateFlow(true)
    override val isLoading = MutableStateFlow(false)

    // Actions
    override fun loadCocktails() {
        // Implementation would use getCocktailsUseCase
    }

    override fun loadMoreCocktails() {
        // Implementation would use getCocktailsUseCase
    }

    override fun searchCocktails(query: String) {
        // Implementation would use searchCocktailsUseCase
    }

    override fun updateSearchFilters(filters: SearchFilters) {
        searchFilters.value = filters
    }

    override fun clearSearchFilters() {
        searchFilters.value = SearchFilters()
    }

    override fun toggleAdvancedSearchMode(active: Boolean) {
        isAdvancedSearchActive.value = active
    }

    override fun toggleSearchMode(active: Boolean) {
        isSearchActive.value = active
    }

    override fun loadCocktailsByCategory(category: String?) {
        // Implementation would use getCocktailsUseCase
    }

    override fun sortByPrice(ascending: Boolean) {
        // Implementation would sort cocktails
    }

    override fun sortByPopularity() {
        // Implementation would sort cocktails
    }

    override fun setOfflineMode(enabled: Boolean) {
        isOfflineMode.value = enabled
    }

    override fun retry() {
        loadCocktails()
    }

    override fun getCocktailById(id: String): Flow<Cocktail?> {
        // Implementation would use getCocktailsUseCase
        return MutableStateFlow(null)
    }
}

/**
 * Android-specific implementation of ICocktailDetailViewModel.
 */
class CocktailDetailViewModel(
    private val getCocktailDetailsUseCase: GetCocktailDetailsUseCase,
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val manageFavoritesUseCase: ManageFavoritesUseCase
) : ICocktailDetailViewModel {
    // State
    override val cocktail = MutableStateFlow<Cocktail?>(null)
    override val isFavorite = MutableStateFlow(false)
    override val recommendations = MutableStateFlow<List<Cocktail>>(emptyList())
    override val isLoading = MutableStateFlow(false)

    // Actions
    override fun loadCocktailDetails(id: String) {
        // Implementation would use getCocktailDetailsUseCase
    }

    override fun toggleFavorite() {
        // Implementation would use manageFavoritesUseCase
        isFavorite.value = !isFavorite.value
    }

    override fun loadRandomCocktail() {
        // Implementation would use getCocktailDetailsUseCase
    }
}

/**
 * Android-specific implementation of ICartViewModel.
 */
class CartViewModel(
    private val manageCartUseCase: ManageCartUseCase
) : ICartViewModel, KoinComponent {
    // Get the actual Android ViewModel from Koin
    private val viewModel: com.cocktailcraft.viewmodel.CartViewModel = get()

    // State
    override val cartItems: StateFlow<List<CocktailCartItem>> get() = viewModel.cartItems
    override val totalPrice: StateFlow<Double> get() = viewModel.totalPrice
    override val isLoading: StateFlow<Boolean> get() = viewModel.isLoading

    // Actions
    override fun loadCartItems() = viewModel.loadCartItems()
    override fun addToCart(cocktail: Cocktail, quantity: Int) = viewModel.addToCart(cocktail, quantity)
    override fun removeFromCart(cocktailId: String) = viewModel.removeFromCart(cocktailId)
    override fun updateQuantity(cocktailId: String, quantity: Int) = viewModel.updateQuantity(cocktailId, quantity)
    override fun clearCart() = viewModel.clearCart()
}

/**
 * Android-specific implementation of IFavoritesViewModel.
 */
class FavoritesViewModel(
    private val manageFavoritesUseCase: ManageFavoritesUseCase
) : IFavoritesViewModel, KoinComponent {
    // Get the actual Android ViewModel from Koin
    private val viewModel: com.cocktailcraft.viewmodel.FavoritesViewModel = get()

    // State
    override val favorites: StateFlow<List<Cocktail>> get() = viewModel.favorites
    override val isLoading: StateFlow<Boolean> get() = viewModel.isLoading

    // Actions
    override fun loadFavorites() = viewModel.loadFavorites()
    override fun addToFavorites(cocktail: Cocktail) = viewModel.addToFavorites(cocktail)
    override fun removeFromFavorites(cocktail: Cocktail) = viewModel.removeFromFavorites(cocktail)
    override fun toggleFavorite(cocktail: Cocktail) = viewModel.toggleFavorite(cocktail)
    override fun isFavorite(id: String): Boolean = viewModel.isFavorite(id)
}

/**
 * Android-specific implementation of IThemeViewModel.
 *
 * Note: The actual initialization happens in the Android ViewModel's init block,
 * which includes:
 * - Loading theme preferences
 * - Setting up initial dark mode state
 * - Setting up system theme following
 */
class ThemeViewModel(
    private val themeUseCase: ThemeUseCase
) : IThemeViewModel, KoinComponent {
    // Get the actual Android ViewModel from Koin
    private val viewModel: com.cocktailcraft.viewmodel.ThemeViewModel = get()

    // State
    override val isDarkMode: StateFlow<Boolean> get() = viewModel.isDarkMode
    override val followSystemTheme: StateFlow<Boolean> get() = viewModel.followSystemTheme

    // Actions
    override fun updateSystemDarkMode(isDark: Boolean) = viewModel.updateSystemDarkMode(isDark)
    override fun toggleDarkMode() = viewModel.toggleDarkMode()
    override fun setDarkMode(enabled: Boolean) = viewModel.setDarkMode(enabled)
    override fun toggleFollowSystemTheme() = viewModel.toggleFollowSystemTheme()
}

/**
 * Android-specific implementation of IOfflineModeViewModel.
 *
 * Note: The actual initialization happens in the Android ViewModel's init block,
 * which includes:
 * - Setting up offline mode status
 * - Starting network monitoring
 * - Loading recently viewed cocktails
 *
 * The Android ViewModel also handles cleanup in onCleared() by stopping network monitoring.
 */
class OfflineModeViewModel(
    private val offlineModeUseCase: OfflineModeUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase
) : IOfflineModeViewModel, KoinComponent {
    // Get the actual Android ViewModel from Koin
    private val viewModel: com.cocktailcraft.viewmodel.OfflineModeViewModel = get()

    // State
    override val isOfflineModeEnabled: StateFlow<Boolean> get() = viewModel.isOfflineModeEnabled
    override val isNetworkAvailable: StateFlow<Boolean> get() = viewModel.isNetworkAvailable
    override val recentlyViewedCocktails: StateFlow<List<Cocktail>> get() = viewModel.recentlyViewedCocktails
    override val isLoading: StateFlow<Boolean> get() = viewModel.isLoading

    // Actions
    override fun toggleOfflineMode() = viewModel.toggleOfflineMode()
    override fun setOfflineMode(enabled: Boolean) = viewModel.setOfflineMode(enabled)
    override fun loadRecentlyViewedCocktails() = viewModel.loadRecentlyViewedCocktails()
    override fun clearCache() = viewModel.clearCache()
    override fun getCachedCocktailCount(): Int = viewModel.getCachedCocktailCount()
}

/**
 * Android-specific implementation of IOrderViewModel.
 */
class OrderViewModel(
    private val manageOrdersUseCase: ManageOrdersUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase
) : IOrderViewModel, KoinComponent {
    // Get the actual Android ViewModel from Koin
    private val viewModel: com.cocktailcraft.viewmodel.OrderViewModel = get()

    // State
    override val orders: StateFlow<List<Order>> get() = viewModel.orders
    override val isLoading: StateFlow<Boolean> get() = viewModel.isLoading

    // Actions
    override fun loadOrders() = viewModel.loadOrders()
    override fun addOrder(order: Order) = viewModel.addOrder(order)
    override fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double) = viewModel.placeOrder(cartItems, totalPrice)
    override fun updateOrderStatus(orderId: String, status: String) = viewModel.updateOrderStatus(orderId, status)
    override fun cancelOrder(orderId: String) = viewModel.cancelOrder(orderId)
    override fun getOrderById(orderId: String): Flow<Order?> = viewModel.getOrderById(orderId)
}

/**
 * Android-specific implementation of IProfileViewModel.
 */
class ProfileViewModel(
    private val authUseCase: AuthUseCase
) : IProfileViewModel {
    // State
    override val user = MutableStateFlow<User?>(null)
    override val isSignedIn = MutableStateFlow(false)
    override val isLoading = MutableStateFlow(false)

    // Actions
    override fun signIn(email: String, password: String) {
        // Implementation would use authUseCase
    }

    override fun signUp(name: String, email: String, password: String) {
        // Implementation would use authUseCase
    }

    override fun signOut() {
        // Implementation would use authUseCase
        user.value = null
        isSignedIn.value = false
    }

    override fun updateUserName(name: String) {
        // Implementation would use authUseCase
        user.value?.let { currentUser ->
            user.value = currentUser.copy(name = name)
        }
    }
}

/**
 * Android-specific implementation of IReviewViewModel.
 */
class ReviewViewModel(
    private val manageReviewsUseCase: ManageReviewsUseCase
) : IReviewViewModel {
    // State
    override val reviews = MutableStateFlow<Map<String, List<Review>>>(emptyMap())
    override val isLoading = MutableStateFlow(false)

    // Actions
    override fun getReviewsForCocktail(cocktailId: String): List<Review> {
        // Implementation would use manageReviewsUseCase
        return reviews.value[cocktailId] ?: emptyList()
    }

    override fun addReview(review: Review) {
        // Implementation would use manageReviewsUseCase
    }

    override fun getAverageRating(cocktailId: String): Float {
        // Implementation would use manageReviewsUseCase
        val cocktailReviews = reviews.value[cocktailId] ?: return 0f
        if (cocktailReviews.isEmpty()) return 0f
        return cocktailReviews.map { it.rating }.average().toFloat()
    }

    override fun createAndAddReview(cocktailId: String, userName: String, rating: Float, comment: String) {
        // Implementation would use manageReviewsUseCase
    }
}
