package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.util.ErrorUtils
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel(
    private val cocktailRepository: CocktailRepository? = null
) : BaseViewModel(), KoinComponent {

    // Use injected repository if not provided in constructor (for production)
    private val injectedCocktailRepository: CocktailRepository by inject()
    private val networkMonitor: NetworkMonitor by inject()

    // Use the provided repository or the injected one
    val repository: CocktailRepository
        get() = cocktailRepository ?: injectedCocktailRepository

    // Track offline mode status
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    // Track network availability
    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private val _cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val cocktails: StateFlow<List<Cocktail>> = _cocktails.asStateFlow()

    private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())
    val favorites: StateFlow<List<Cocktail>> = _favorites.asStateFlow()

    // Legacy error string for backward compatibility
    private val _errorString = MutableStateFlow<String>("")
    val errorString: StateFlow<String> = _errorString.asStateFlow()

    // Pagination state
    private val _currentPage = MutableStateFlow(1)
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    private val PAGE_SIZE = 10

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Search active state
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    // Add a shared flow for connectivity status changes
    private val _connectivityStatus = MutableSharedFlow<Boolean>(replay = 1)

    // Keep track of search job for debouncing
    private var searchJob: Job? = null

    // Network error message with retry button
    private val networkErrorMessage = "Unable to connect to the cocktail database. Please check your internet connection and try again."

    init {
        // Initialize offline mode status
        _isOfflineMode.value = repository.isOfflineModeEnabled()

        // Start monitoring network connectivity
        viewModelScope.launch {
            networkMonitor.startMonitoring()
            networkMonitor.isOnline.collectLatest { isOnline ->
                _isNetworkAvailable.value = isOnline

                // If network becomes available and we had an error, retry loading
                if (isOnline && _errorString.value.isNotBlank()) {
                    retry()
                }

                // If network becomes unavailable and offline mode is not enabled,
                // automatically enable it
                if (!isOnline && !_isOfflineMode.value) {
                    setOfflineMode(true)
                }
            }
        }

        // Monitor offline mode changes from repository
        viewModelScope.launch {
            _isOfflineMode.value = repository.isOfflineModeEnabled()
        }

        loadCocktails()
        loadFavorites()
        monitorConnectivity()
    }

    /**
     * Set offline mode to a specific value.
     */
    fun setOfflineMode(enabled: Boolean) {
        _isOfflineMode.value = enabled
        repository.setOfflineMode(enabled)

        // If switching to online mode, reload data
        if (!enabled && _isNetworkAvailable.value) {
            retry()
        }
    }

    private fun monitorConnectivity() {
        viewModelScope.launch {
            _connectivityStatus.emit(checkConnectivity())

            // Periodically check connectivity when there's an error
            while (true) {
                delay(30000) // Check every 30 seconds
                if (_errorString.value.isNotBlank()) {
                    val isConnected = checkConnectivity()
                    _connectivityStatus.emit(isConnected)

                    // If connection restored and we had an error, reload data
                    if (isConnected && _errorString.value == networkErrorMessage) {
                        _errorString.value = ""
                        clearError()
                        loadCocktails()
                    }
                }
            }
        }
    }

    private suspend fun checkConnectivity(): Boolean {
        return try {
            repository.checkApiConnectivity().first()
        } catch (e: Exception) {
            false
        }
    }

    fun loadCocktails() {
        viewModelScope.launch {
            setLoading(true)
            clearError() // Clear base class error
            _errorString.value = "" // Clear legacy error string
            _currentPage.value = 1 // Reset to first page
            _hasMoreData.value = true // Reset pagination state

            try {
                // Check if we're in offline mode (either forced or due to network unavailability)
                val isOffline = _isOfflineMode.value || !_isNetworkAvailable.value

                // If we're offline, update the repository's offline mode setting
                if (!_isNetworkAvailable.value && !_isOfflineMode.value) {
                    repository.setOfflineMode(true)
                    _isOfflineMode.value = true
                }

                // The repository will handle returning cached data when in offline mode
                repository.getCocktailsSortedByNewest()
                    .catch { e ->
                        // If we're offline, don't show an error - just show cached data
                        if (isOffline) {
                            setLoading(false)
                        } else {
                            handleError("Failed to load cocktails", e)
                        }
                    }
                    .collect { cocktailList ->
                        // Apply pagination
                        val paginatedList = cocktailList.take(PAGE_SIZE)
                        _cocktails.value = paginatedList
                        _hasMoreData.value = paginatedList.size < cocktailList.size
                        setLoading(false)

                        // If we got data while offline, clear any error
                        if (isOffline && cocktailList.isNotEmpty()) {
                            clearError() // Clear base class error
                            _errorString.value = "" // Clear legacy error string
                        }
                    }
            } catch (e: Exception) {
                // If we're offline, try to get cached data instead of showing an error
                if (_isOfflineMode.value || !_isNetworkAvailable.value) {
                    try {
                        val cachedCocktails = repository.getRecentlyViewedCocktails().first()
                        if (cachedCocktails.isNotEmpty()) {
                            _cocktails.value = cachedCocktails
                            clearError() // Clear base class error
                            _errorString.value = "" // Clear legacy error string
                        } else {
                            // Create a user-friendly error for no cached data
                            setError(
                                title = "No Cached Data",
                                message = "No cached cocktails available offline. Connect to the internet to download cocktails.",
                                category = ErrorUtils.ErrorCategory.DATA,
                                recoveryAction = ErrorUtils.RecoveryAction("Go Online") {
                                    if (_isNetworkAvailable.value) {
                                        setOfflineMode(false)
                                        retry()
                                    }
                                }
                            )
                            _errorString.value = "No cached cocktails available offline."
                        }
                    } catch (cacheException: Exception) {
                        handleError("Failed to load cached cocktails", cacheException)
                    }
                } else {
                    handleError("Failed to load cocktails", e)
                }
                setLoading(false)
            }
        }
    }

    /**
     * Load more cocktails for pagination
     */
    fun loadMoreCocktails() {
        // Don't load more if already loading or no more data
        if (_isLoadingMore.value || !_hasMoreData.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true

            try {
                // Increment page
                val nextPage = _currentPage.value + 1

                repository.getCocktailsSortedByNewest()
                    .catch { e ->
                        handleError("Failed to load more cocktails", e)
                        _isLoadingMore.value = false
                    }
                    .collect { allCocktails ->
                        // Calculate the range for the next page
                        val startIndex = _currentPage.value * PAGE_SIZE
                        val endIndex = startIndex + PAGE_SIZE

                        // Get items for the next page
                        val newItems = allCocktails.drop(startIndex).take(PAGE_SIZE)

                        if (newItems.isNotEmpty()) {
                            // Append new items to existing list
                            _cocktails.value = _cocktails.value + newItems
                            _currentPage.value = nextPage

                            // Check if we have more data
                            _hasMoreData.value = endIndex < allCocktails.size
                        } else {
                            // No more items to load
                            _hasMoreData.value = false
                        }

                        _isLoadingMore.value = false
                    }
            } catch (e: Exception) {
                handleError("Failed to load more cocktails", e)
                _isLoadingMore.value = false
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                repository.getFavoriteCocktails()
                    .catch { e ->
                        // Don't show error UI for favorites loading, just log
                        println("Failed to load favorites: ${e.message}")
                    }
                    .collect { cocktailList ->
                        _favorites.value = cocktailList
                    }
            } catch (e: Exception) {
                println("Failed to load favorites: ${e.message}")
            }
        }
    }

    // Updated function with debouncing
    fun searchCocktails(query: String) {
        _searchQuery.value = query

        // Automatically activate search mode when query is not empty
        if (query.isNotEmpty() && !_isSearchActive.value) {
            _isSearchActive.value = true
        }

        // Cancel previous search job if it exists
        searchJob?.cancel()

        if (query.isBlank()) {
            _isSearchActive.value = false
            loadCocktails() // Reset to all cocktails if query is empty
            return
        }

        // Create a new search job with debounce
        searchJob = viewModelScope.launch {
            delay(300) // Debounce for 300ms
            setLoading(true)
            clearError() // Clear base class error
            _errorString.value = "" // Clear legacy error string

            try {
                // Check connectivity first
                if (!checkConnectivity()) {
                    setError(
                        title = "Network Error",
                        message = networkErrorMessage,
                        category = ErrorUtils.ErrorCategory.NETWORK,
                        recoveryAction = ErrorUtils.RecoveryAction("Retry") { retry() }
                    )
                    _errorString.value = networkErrorMessage
                    setLoading(false)
                    return@launch
                }

                repository.searchCocktailsByName(query)
                    .catch { e ->
                        handleError("Failed to search cocktails", e)
                    }
                    .collect { cocktailList ->
                        _cocktails.value = cocktailList
                        setLoading(false)
                    }
            } catch (e: Exception) {
                handleError("Failed to search cocktails", e)
            }
        }
    }

    // Helper function to handle errors consistently
    private fun handleError(baseMessage: String, e: Throwable) {
        // Create a recovery action based on the error
        val recoveryAction = ErrorUtils.RecoveryAction("Retry") { retry() }

        // Determine error category
        val category = when {
            e.message?.contains("timeout") == true -> ErrorUtils.ErrorCategory.NETWORK
            e.message?.contains("connection") == true ||
            e.message?.contains("network") == true -> ErrorUtils.ErrorCategory.NETWORK
            e.message?.contains("server") == true -> ErrorUtils.ErrorCategory.SERVER
            else -> ErrorUtils.ErrorCategory.UNKNOWN
        }

        // Format the error message
        val errorMessage = when {
            e.message?.contains("timeout") == true ->
                "$baseMessage: The request timed out. Please try again."
            e.message?.contains("connection") == true ||
            e.message?.contains("network") == true ->
                networkErrorMessage
            else -> "$baseMessage: ${e.message}"
        }

        // Set the error using the base class method
        setError(
            title = if (category == ErrorUtils.ErrorCategory.NETWORK) "Network Error" else "Error",
            message = errorMessage,
            category = category,
            recoveryAction = recoveryAction
        )

        // Also update the legacy error string for backward compatibility
        _errorString.value = errorMessage

        // Log the error
        println("$baseMessage: ${e.message}")
    }

    // Toggle search mode
    fun toggleSearchMode(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            loadCocktails() // Reset to all cocktails when search is deactivated
        }
    }

    // Load cocktails filtered by category - add this new method
    fun loadCocktailsByCategory(category: String?) {
        viewModelScope.launch {
            setLoading(true)
            clearError() // Clear base class error
            _errorString.value = "" // Clear legacy error string

            try {
                // Check connectivity first
                if (!checkConnectivity()) {
                    setError(
                        title = "Network Error",
                        message = networkErrorMessage,
                        category = ErrorUtils.ErrorCategory.NETWORK,
                        recoveryAction = ErrorUtils.RecoveryAction("Retry") { retry() }
                    )
                    _errorString.value = networkErrorMessage
                    setLoading(false)
                    return@launch
                }

                if (category == null) {
                    loadCocktails() // Load all if category is null
                    return@launch
                }

                repository.filterByCategory(category)
                    .catch { e ->
                        handleError("Failed to filter cocktails", e)
                    }
                    .collect { cocktailList ->
                        _cocktails.value = cocktailList
                        setLoading(false)
                    }
            } catch (e: Exception) {
                handleError("Failed to filter cocktails", e)
            }
        }
    }

    fun addToFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                repository.addToFavorites(cocktail)
                loadFavorites() // Refresh favorites after adding
            } catch (e: Exception) {
                println("Failed to add to favorites: ${e.message}")
                // Don't show UI error for favorite operations
            }
        }
    }

    // Add retry functionality to reload data based on current state
    fun retry() {
        if (_isSearchActive.value && _searchQuery.value.isNotEmpty()) {
            // If in search mode, retry the search
            searchCocktails(_searchQuery.value)
        } else if (_isSearchActive.value && _searchQuery.value.isEmpty()) {
            // If search is active but query is empty, reset to normal mode
            toggleSearchMode(false)
        } else {
            // Otherwise retry loading based on selected category
            // This will be handled by the LaunchedEffect in the UI
            loadCocktails()
        }
    }

    fun removeFromFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                repository.removeFromFavorites(cocktail)
                loadFavorites() // Refresh favorites after removing
            } catch (e: Exception) {
                println("Failed to remove from favorites: ${e.message}")
                // Don't show UI error for favorite operations
            }
        }
    }

    fun isFavorite(cocktailId: String): Boolean {
        return favorites.value.any { it.id == cocktailId }
    }

    fun toggleFavorite(cocktail: Cocktail) {
        if (isFavorite(cocktail.id)) {
            removeFromFavorites(cocktail)
        } else {
            addToFavorites(cocktail)
        }
    }

    /**
     * Get cocktails by category - improved version for recommendations
     * This method returns a list of cocktails based on the provided category
     * and ensures we always return varied recommendations
     */
    fun getCocktailsByCategory(category: String, limit: Int = 3): List<Cocktail> {
        // First try to get from the current list with exact category match
        val fromCurrentList = _cocktails.value
            .filter { it.category == category && it.imageUrl?.isNotBlank() == true }
            .shuffled() // Add randomness
            .take(limit)

        if (fromCurrentList.size >= limit) {
            return fromCurrentList
        }

        // If we don't have enough from the exact category, try to get any cocktails
        val anyRecommendations = _cocktails.value
            .filter { it.category != null && it.imageUrl?.isNotBlank() == true }
            .shuffled() // Add randomness
            .take(limit)

        if (anyRecommendations.size >= limit) {
            return anyRecommendations
        }

        // If we still don't have enough, trigger a background load
        viewModelScope.launch {
            try {
                repository.getCocktailsSortedByNewest()
                    .collect { cocktailList ->
                        if (cocktailList.isNotEmpty()) {
                            _cocktails.value = cocktailList
                        }
                    }
            } catch (e: Exception) {
                println("Failed to load recommendations: ${e.message}")
            }
        }

        // Return a varied set of fallback cocktails based on the requested category
        val fallbackCocktails = when (category.lowercase()) {
            "cocktail" -> listOf(
                createFallbackCocktail(
                    "11000", "Mojito", "Cocktail",
                    "https://www.thecocktaildb.com/images/media/drink/3z6xdi1589574603.jpg"
                ),
                createFallbackCocktail(
                    "11001", "Old Fashioned", "Cocktail",
                    "https://www.thecocktaildb.com/images/media/drink/vrwquq1478252802.jpg"
                ),
                createFallbackCocktail(
                    "11002", "Long Island Tea", "Cocktail",
                    "https://www.thecocktaildb.com/images/media/drink/nkwr4c1606770558.jpg"
                )
            )
            "ordinary drink" -> listOf(
                createFallbackCocktail(
                    "11007", "Margarita", "Ordinary Drink",
                    "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg"
                ),
                createFallbackCocktail(
                    "11008", "Manhattan", "Ordinary Drink",
                    "https://www.thecocktaildb.com/images/media/drink/yk70e31606771240.jpg"
                ),
                createFallbackCocktail(
                    "11009", "Moscow Mule", "Ordinary Drink",
                    "https://www.thecocktaildb.com/images/media/drink/3pylqc1504370988.jpg"
                )
            )
            "shot" -> listOf(
                createFallbackCocktail(
                    "12127", "Jello shots", "Shot",
                    "https://www.thecocktaildb.com/images/media/drink/l0smzo1504884904.jpg"
                ),
                createFallbackCocktail(
                    "13192", "Kamikaze", "Shot",
                    "https://www.thecocktaildb.com/images/media/drink/d7mo481504889531.jpg"
                ),
                createFallbackCocktail(
                    "14610", "ACID", "Shot",
                    "https://www.thecocktaildb.com/images/media/drink/xuxpxt1479209317.jpg"
                )
            )
            else -> listOf(
                createFallbackCocktail(
                    "11000", "Mojito", "Cocktail",
                    "https://www.thecocktaildb.com/images/media/drink/3z6xdi1589574603.jpg"
                ),
                createFallbackCocktail(
                    "11007", "Margarita", "Ordinary Drink",
                    "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg"
                ),
                createFallbackCocktail(
                    "12127", "Jello shots", "Shot",
                    "https://www.thecocktaildb.com/images/media/drink/l0smzo1504884904.jpg"
                )
            )
        }

        // Combine any real recommendations with fallbacks to reach the limit
        val combined = (fromCurrentList + anyRecommendations + fallbackCocktails)
            .distinctBy { it.id } // Remove duplicates
            .take(limit)

        return combined
    }

    /**
     * Helper method to create fallback cocktails with consistent properties
     */
    private fun createFallbackCocktail(
        id: String,
        name: String,
        category: String,
        imageUrl: String
    ): Cocktail {
        return Cocktail(
            id = id,
            name = name,
            category = category,
            alcoholic = "Alcoholic",
            glass = "Cocktail glass",
            instructions = "Mix ingredients and serve.",
            imageUrl = imageUrl,
            ingredients = emptyList(),
            price = 8.99 + (id.hashCode() % 5), // Varied price based on ID
            stockCount = 10 + (id.hashCode() % 10) // Varied stock based on ID
        )
    }

    fun sortByPrice(ascending: Boolean) {
        viewModelScope.launch {
            setLoading(true)

            try {
                val sortedList = if (ascending) {
                    _cocktails.value.sortedBy { it.price }
                } else {
                    _cocktails.value.sortedByDescending { it.price }
                }
                _cocktails.value = sortedList
            } catch (e: Exception) {
                handleError("Failed to sort cocktails", e)
            } finally {
                setLoading(false)
            }
        }
    }

    fun sortByRating() {
        viewModelScope.launch {
            setLoading(true)

            try {
                val sortedList = _cocktails.value.sortedByDescending { it.rating }
                _cocktails.value = sortedList
            } catch (e: Exception) {
                handleError("Failed to sort cocktails", e)
            } finally {
                setLoading(false)
            }
        }
    }

    fun sortByPopularity() {
        viewModelScope.launch {
            setLoading(true)

            try {
                val sortedList = _cocktails.value.sortedByDescending { it.popularity }
                _cocktails.value = sortedList
            } catch (e: Exception) {
                handleError("Failed to sort cocktails", e)
            } finally {
                setLoading(false)
            }
        }
    }

    fun getCategories(): List<String> {
        return _cocktails.value
            .mapNotNull { it.category }
            .distinct()
            .filterNot { it.isBlank() }
            .sorted()
    }

    // Override the base clearError to also clear our legacy error string
    fun clearLegacyError() {
        clearError() // Call the base class method
        _errorString.value = "" // Also clear the legacy error string
    }

    // Add method to get cocktail by ID
    fun getCocktailById(id: String): kotlinx.coroutines.flow.Flow<Cocktail?> {
        viewModelScope.launch {
            setLoading(true)
            clearError() // Clear base class error
            _errorString.value = "" // Clear legacy error string
        }

        return kotlinx.coroutines.flow.flow {
            try {
                repository.getCocktailById(id).collect { cocktail ->
                    emit(cocktail)
                    setLoading(false)
                }
            } catch (e: Exception) {
                handleError("Failed to load cocktail details", e)
                emit(null)
            }
        }
    }

    // Add method to force refresh cocktail details
    fun forceRefreshCocktailDetails(id: String) {
        viewModelScope.launch {
            setLoading(true)
            clearError() // Clear base class error
            _errorString.value = "" // Clear legacy error string

            try {
                // Clear any cached data for this cocktail
                _cocktails.value = _cocktails.value.filter { it.id != id }

                // Force a new API call
                repository.getCocktailById(id).collect { cocktail ->
                    if (cocktail != null) {
                        // Add the refreshed cocktail to the list
                        _cocktails.value = _cocktails.value + cocktail
                    } else {
                        setError(
                            title = "Refresh Failed",
                            message = "Could not refresh cocktail details. Please try again.",
                            category = ErrorUtils.ErrorCategory.DATA,
                            recoveryAction = ErrorUtils.RecoveryAction("Retry") { forceRefreshCocktailDetails(id) }
                        )
                        _errorString.value = "Could not refresh cocktail details. Please try again."
                    }
                    setLoading(false)
                }
            } catch (e: Exception) {
                handleError("Failed to refresh cocktail details", e)
                setLoading(false)
            }
        }
    }
}