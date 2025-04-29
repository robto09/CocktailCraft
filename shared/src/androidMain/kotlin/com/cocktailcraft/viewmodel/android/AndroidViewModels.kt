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
import org.koin.core.component.get
import org.koin.core.component.inject

/**
 * Android-specific implementation of IHomeViewModel.
 * This class uses lazy loading to get the actual Android ViewModel from Koin.
 * This approach improves performance by only initializing the ViewModel when it's first accessed.
 */
class HomeViewModel(
    private val getCocktailsUseCase: GetCocktailsUseCase,
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase
) : IHomeViewModel, KoinComponent {
    // Lazy-loaded Android ViewModel
    private val viewModel by lazy { get<com.cocktailcraft.viewmodel.HomeViewModel>() }

    // State - delegated to the Android ViewModel
    override val cocktails: StateFlow<List<Cocktail>> get() = viewModel.cocktails
    override val hasMoreData: StateFlow<Boolean> get() = viewModel.hasMoreData
    override val isLoadingMore: StateFlow<Boolean> get() = viewModel.isLoadingMore
    override val searchQuery: StateFlow<String> get() = viewModel.searchQuery
    override val isSearchActive: StateFlow<Boolean> get() = viewModel.isSearchActive
    override val searchFilters: StateFlow<SearchFilters> get() = viewModel.searchFilters
    override val isAdvancedSearchActive: StateFlow<Boolean> get() = viewModel.isAdvancedSearchActive
    override val isOfflineMode: StateFlow<Boolean> get() = viewModel.isOfflineMode
    override val isNetworkAvailable: StateFlow<Boolean> get() = viewModel.isNetworkAvailable
    override val isLoading: StateFlow<Boolean> get() = viewModel.isLoading

    // Actions - delegated to the Android ViewModel
    override fun loadCocktails() = viewModel.loadCocktails()
    override fun loadMoreCocktails() = viewModel.loadMoreCocktails()
    override fun searchCocktails(query: String) = viewModel.searchCocktails(query)
    override fun updateSearchFilters(filters: SearchFilters) = viewModel.updateSearchFilters(filters)
    override fun clearSearchFilters() = viewModel.clearSearchFilters()
    override fun toggleAdvancedSearchMode(active: Boolean) = viewModel.toggleAdvancedSearchMode(active)
    override fun toggleSearchMode(active: Boolean) = viewModel.toggleSearchMode(active)
    override fun loadCocktailsByCategory(category: String?) = viewModel.loadCocktailsByCategory(category)
    override fun sortByPrice(ascending: Boolean) = viewModel.sortByPrice(ascending)
    override fun sortByPopularity() = viewModel.sortByPopularity()
    override fun setOfflineMode(enabled: Boolean) = viewModel.setOfflineMode(enabled)
    override fun retry() = viewModel.retry()
    override fun getCocktailById(id: String): Flow<Cocktail?> = viewModel.getCocktailById(id)
}

/**
 * Android-specific implementation of ICocktailDetailViewModel.
 * This implementation uses the provided use cases to fetch and manage cocktail details.
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

    // Current cocktail ID
    private var currentCocktailId: String? = null

    // Actions
    override fun loadCocktailDetails(id: String) {
        isLoading.value = true
        currentCocktailId = id

        // Use the GetCocktailDetailsUseCase to fetch the cocktail details
        getCocktailDetailsUseCase.getCocktailById(id) { result ->
            isLoading.value = false

            if (result.isSuccess) {
                val fetchedCocktail = result.getOrNull()
                cocktail.value = fetchedCocktail

                // Check if this cocktail is a favorite
                fetchedCocktail?.let {
                    isFavorite.value = manageFavoritesUseCase.isFavorite(it.id)

                    // Load recommendations for this cocktail
                    loadRecommendations(it)
                }
            } else {
                // Handle error case
                // In a real implementation, you might want to emit an error state
            }
        }
    }

    private fun loadRecommendations(cocktail: Cocktail) {
        getRecommendationsUseCase.getRecommendations(cocktail) { result ->
            if (result.isSuccess) {
                recommendations.value = result.getOrNull() ?: emptyList()
            } else {
                // Handle error case
                recommendations.value = emptyList()
            }
        }
    }

    override fun toggleFavorite() {
        val currentCocktail = cocktail.value ?: return

        if (isFavorite.value) {
            // Remove from favorites
            manageFavoritesUseCase.removeFromFavorites(currentCocktail)
        } else {
            // Add to favorites
            manageFavoritesUseCase.addToFavorites(currentCocktail)
        }

        // Update the favorite status
        isFavorite.value = !isFavorite.value
    }

    override fun loadRandomCocktail() {
        isLoading.value = true

        // Use the GetCocktailDetailsUseCase to fetch a random cocktail
        getCocktailDetailsUseCase.getRandomCocktail { result ->
            isLoading.value = false

            if (result.isSuccess) {
                val randomCocktail = result.getOrNull()
                cocktail.value = randomCocktail
                currentCocktailId = randomCocktail?.id

                // Check if this cocktail is a favorite
                randomCocktail?.let {
                    isFavorite.value = manageFavoritesUseCase.isFavorite(it.id)

                    // Load recommendations for this cocktail
                    loadRecommendations(it)
                }
            } else {
                // Handle error case
            }
        }
    }
}

/**
 * Android-specific implementation of ICartViewModel.
 * This implementation includes initialization logic to load cart items
 * and calculate the total price.
 */
class CartViewModel(
    private val manageCartUseCase: ManageCartUseCase
) : ICartViewModel {
    // State
    override val cartItems = MutableStateFlow<List<CocktailCartItem>>(emptyList())
    override val totalPrice = MutableStateFlow(0.0)
    override val isLoading = MutableStateFlow(false)

    init {
        // Load cart items when the ViewModel is created
        loadCartItems()
    }

    /**
     * Calculate the total price of all items in the cart.
     */
    private fun calculateTotalPrice() {
        val total = cartItems.value.sumOf { it.cocktail.price * it.quantity }
        totalPrice.value = total
    }

    // Actions
    override fun loadCartItems() {
        isLoading.value = true

        manageCartUseCase.getCartItems { result ->
            isLoading.value = false

            if (result.isSuccess) {
                cartItems.value = result.getOrNull() ?: emptyList()
                calculateTotalPrice()
            } else {
                // Handle error case
                cartItems.value = emptyList()
                totalPrice.value = 0.0
            }
        }
    }

    override fun addToCart(cocktail: Cocktail, quantity: Int) {
        isLoading.value = true

        manageCartUseCase.addToCart(cocktail, quantity) { success ->
            isLoading.value = false

            if (success) {
                // Reload cart items to reflect the changes
                loadCartItems()
            } else {
                // Handle error case
            }
        }
    }

    override fun removeFromCart(cocktailId: String) {
        isLoading.value = true

        manageCartUseCase.removeFromCart(cocktailId) { success ->
            isLoading.value = false

            if (success) {
                // Reload cart items to reflect the changes
                loadCartItems()
            } else {
                // Handle error case
            }
        }
    }

    override fun updateQuantity(cocktailId: String, quantity: Int) {
        isLoading.value = true

        manageCartUseCase.updateQuantity(cocktailId, quantity) { success ->
            isLoading.value = false

            if (success) {
                // Reload cart items to reflect the changes
                loadCartItems()
            } else {
                // Handle error case
            }
        }
    }

    override fun clearCart() {
        isLoading.value = true

        manageCartUseCase.clearCart { success ->
            isLoading.value = false

            if (success) {
                cartItems.value = emptyList()
                totalPrice.value = 0.0
            } else {
                // Handle error case
            }
        }
    }
}

/**
 * Android-specific implementation of IFavoritesViewModel.
 */
class FavoritesViewModel(
    private val manageFavoritesUseCase: ManageFavoritesUseCase
) : IFavoritesViewModel {
    // State
    override val favorites = MutableStateFlow<List<Cocktail>>(emptyList())
    override val isLoading = MutableStateFlow(false)

    // Actions
    override fun loadFavorites() {
        // Implementation would use manageFavoritesUseCase
    }

    override fun addToFavorites(cocktail: Cocktail) {
        // Implementation would use manageFavoritesUseCase
    }

    override fun removeFromFavorites(cocktail: Cocktail) {
        // Implementation would use manageFavoritesUseCase
    }

    override fun toggleFavorite(cocktail: Cocktail) {
        // Implementation would use manageFavoritesUseCase
    }

    override fun isFavorite(id: String): Boolean {
        // Implementation would use manageFavoritesUseCase
        return favorites.value.any { it.id == id }
    }
}

/**
 * Android-specific implementation of IThemeViewModel.
 * This implementation includes initialization logic to load theme preferences.
 */
class ThemeViewModel(
    private val themeUseCase: ThemeUseCase
) : IThemeViewModel {
    // State
    override val isDarkMode = MutableStateFlow(false)
    override val followSystemTheme = MutableStateFlow(true)

    init {
        // Load theme preferences when the ViewModel is created
        loadThemePreferences()
    }

    /**
     * Load theme preferences from storage.
     */
    private fun loadThemePreferences() {
        // Get the dark mode preference
        val darkModeEnabled = themeUseCase.isDarkModeEnabled()
        isDarkMode.value = darkModeEnabled

        // Get the system theme following preference
        val followSystem = themeUseCase.isFollowingSystemTheme()
        followSystemTheme.value = followSystem

        // If following system theme, update the dark mode based on the system theme
        if (followSystem) {
            val systemDarkMode = themeUseCase.isSystemInDarkMode()
            isDarkMode.value = systemDarkMode
        }
    }

    // Actions
    override fun updateSystemDarkMode(isDark: Boolean) {
        if (followSystemTheme.value) {
            isDarkMode.value = isDark
            // Save the dark mode preference
            themeUseCase.setDarkMode(isDark)
        }
    }

    override fun toggleDarkMode() {
        val newDarkMode = !isDarkMode.value
        isDarkMode.value = newDarkMode

        // Save the dark mode preference
        themeUseCase.setDarkMode(newDarkMode)
    }

    override fun setDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled

        // Save the dark mode preference
        themeUseCase.setDarkMode(enabled)
    }

    override fun toggleFollowSystemTheme() {
        val newFollowSystem = !followSystemTheme.value
        followSystemTheme.value = newFollowSystem

        // Save the follow system theme preference
        themeUseCase.setFollowSystemTheme(newFollowSystem)

        // If now following system theme, update the dark mode based on the system theme
        if (newFollowSystem) {
            val systemDarkMode = themeUseCase.isSystemInDarkMode()
            isDarkMode.value = systemDarkMode
            themeUseCase.setDarkMode(systemDarkMode)
        }
    }
}

/**
 * Android-specific implementation of IOfflineModeViewModel.
 * This implementation includes initialization logic to set up network monitoring
 * and load cached cocktails.
 */
class OfflineModeViewModel(
    private val offlineModeUseCase: OfflineModeUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase
) : IOfflineModeViewModel {
    // State
    override val isOfflineModeEnabled = MutableStateFlow(false)
    override val isNetworkAvailable = MutableStateFlow(true)
    override val recentlyViewedCocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    override val isLoading = MutableStateFlow(false)

    init {
        // Initialize offline mode status
        initializeOfflineMode()

        // Start network monitoring
        startNetworkMonitoring()

        // Load recently viewed cocktails
        loadRecentlyViewedCocktails()
    }

    /**
     * Initialize offline mode status from preferences.
     */
    private fun initializeOfflineMode() {
        val offlineModeEnabled = offlineModeUseCase.isOfflineModeEnabled()
        isOfflineModeEnabled.value = offlineModeEnabled
    }

    /**
     * Start monitoring network status.
     */
    private fun startNetworkMonitoring() {
        networkStatusUseCase.startMonitoring { available ->
            isNetworkAvailable.value = available
        }
    }

    /**
     * Stop network monitoring.
     * This should be called when the ViewModel is cleared.
     */
    fun stopNetworkMonitoring() {
        networkStatusUseCase.stopMonitoring()
    }

    // Actions
    override fun toggleOfflineMode() {
        val newOfflineMode = !isOfflineModeEnabled.value
        isOfflineModeEnabled.value = newOfflineMode

        // Save the offline mode preference
        offlineModeUseCase.setOfflineMode(newOfflineMode)
    }

    override fun setOfflineMode(enabled: Boolean) {
        isOfflineModeEnabled.value = enabled

        // Save the offline mode preference
        offlineModeUseCase.setOfflineMode(enabled)
    }

    override fun loadRecentlyViewedCocktails() {
        isLoading.value = true

        offlineModeUseCase.getRecentlyViewedCocktails { result ->
            isLoading.value = false

            if (result.isSuccess) {
                recentlyViewedCocktails.value = result.getOrNull() ?: emptyList()
            } else {
                // Handle error case
                recentlyViewedCocktails.value = emptyList()
            }
        }
    }

    override fun clearCache() {
        isLoading.value = true

        offlineModeUseCase.clearCache { success ->
            isLoading.value = false

            if (success) {
                recentlyViewedCocktails.value = emptyList()
            }
            // Otherwise, handle error case
        }
    }

    override fun getCachedCocktailCount(): Int {
        return offlineModeUseCase.getCachedCocktailCount()
    }
}

/**
 * Android-specific implementation of IOrderViewModel.
 */
class OrderViewModel(
    private val manageOrdersUseCase: ManageOrdersUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase
) : IOrderViewModel {
    // State
    override val orders = MutableStateFlow<List<Order>>(emptyList())
    override val isLoading = MutableStateFlow(false)

    // Actions
    override fun loadOrders() {
        // Implementation would use manageOrdersUseCase
    }

    override fun addOrder(order: Order) {
        // Implementation would use manageOrdersUseCase
    }

    override fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double) {
        // Implementation would use placeOrderUseCase
    }

    override fun updateOrderStatus(orderId: String, status: String) {
        // Implementation would use manageOrdersUseCase
    }

    override fun cancelOrder(orderId: String) {
        // Implementation would use manageOrdersUseCase
    }

    override fun getOrderById(orderId: String): Flow<Order?> {
        // Implementation would use manageOrdersUseCase
        return MutableStateFlow(null)
    }
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
