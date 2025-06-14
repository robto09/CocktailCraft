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
import com.cocktailcraft.util.CocktailDebugLogger

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

    // Advanced search filters state
    private val _searchFilters = MutableStateFlow(com.cocktailcraft.domain.model.SearchFilters())
    val searchFilters: StateFlow<com.cocktailcraft.domain.model.SearchFilters> = _searchFilters.asStateFlow()

    // Advanced search active state
    private val _isAdvancedSearchActive = MutableStateFlow(false)
    val isAdvancedSearchActive: StateFlow<Boolean> = _isAdvancedSearchActive.asStateFlow()

    // Add a shared flow for connectivity status changes
    private val _connectivityStatus = MutableSharedFlow<Boolean>(replay = 1)

    // Keep track of search job for debouncing
    private var searchJob: Job? = null

    // Network error message with retry button
    private val networkErrorMessage = "Unable to connect to the cocktail database. Please check your internet connection and try again."

    init {
        CocktailDebugLogger.log("🎯 HomeViewModel init() called")
        // Initialize offline mode status
        _isOfflineMode.value = repository.isOfflineModeEnabled()
        CocktailDebugLogger.log("   - Initial offline mode: ${_isOfflineMode.value}")

        // Start monitoring network connectivity
        viewModelScope.launch {
            networkMonitor.startMonitoring()
            networkMonitor.isOnline.collectLatest { isOnline ->
                CocktailDebugLogger.log("📡 Network status changed: $isOnline")
                _isNetworkAvailable.value = isOnline

                // If network becomes available and we had an error, retry loading
                if (isOnline && _errorString.value.isNotBlank() && cocktails.value.isEmpty()) {
                    CocktailDebugLogger.log("   🔄 Network restored with error state, retrying...")
                    retry()
                }

                // If network becomes unavailable and offline mode is not enabled,
                // automatically enable it
                if (!isOnline && !_isOfflineMode.value) {
                    CocktailDebugLogger.log("   📴 Network lost, enabling offline mode")
                    setOfflineMode(true)
                }
            }
        }

        // Monitor offline mode changes from repository
        viewModelScope.launch {
            _isOfflineMode.value = repository.isOfflineModeEnabled()
        }

        // Delay initial load slightly to ensure cache is ready
        viewModelScope.launch {
            CocktailDebugLogger.log("   ⏳ Delaying initial load by 100ms...")
            delay(100) // Small delay to ensure cache initialization
            
            // Only load if we don't have cocktails already (ViewModel might be reused)
            if (cocktails.value.isEmpty()) {
                CocktailDebugLogger.log("   🚀 LAZY LOADING: Starting initial load with 'Cocktail' category only")
                // Start with just the "Cocktail" category to reduce initial API calls
                loadCocktailsByCategory("Cocktail")
            } else {
                CocktailDebugLogger.log("   ✅ Skipping initial load - already have ${cocktails.value.size} cocktails")
            }
            
            loadFavorites()
        }
        
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

            // Periodically check connectivity when there's an error AND no data
            while (true) {
                delay(30000) // Check every 30 seconds
                if (_errorString.value.isNotBlank() && cocktails.value.isEmpty()) {
                    val isConnected = checkConnectivity()
                    _connectivityStatus.emit(isConnected)

                    // If connection restored and we had an error with no data, reload
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
        CocktailDebugLogger.log("🔌 HomeViewModel.checkConnectivity() called")
        return try {
            val isConnected = repository.checkApiConnectivity().first()
            CocktailDebugLogger.log("   - API connectivity: $isConnected")
            isConnected
        } catch (e: Exception) {
            CocktailDebugLogger.log("   ❌ Connectivity check failed: ${e.message}")
            false
        }
    }

    fun loadCocktails() {
        CocktailDebugLogger.log("🏠 HomeViewModel.loadCocktails() called")
        CocktailDebugLogger.log("   🎯 LAZY LOADING: Defaulting to 'Cocktail' category")
        
        // When loadCocktails is called without a category, default to "Cocktail"
        // This reduces initial API calls from fetching all categories
        loadCocktailsByCategory("Cocktail")
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
                        CocktailDebugLogger.log("Failed to load favorites: ${e.message}")
                    }
                    .collect { cocktailList ->
                        _favorites.value = cocktailList
                    }
            } catch (e: Exception) {
                CocktailDebugLogger.log("Failed to load favorites: ${e.message}")
            }
        }
    }

    // Updated function with debouncing
    fun searchCocktails(query: String) {
        _searchQuery.value = query

        // Update the search filters with the new query
        _searchFilters.value = _searchFilters.value.copy(query = query)

        // Automatically activate search mode when query is not empty
        if (query.isNotEmpty() && !_isSearchActive.value) {
            _isSearchActive.value = true
        }

        // Cancel previous search job if it exists
        searchJob?.cancel()

        if (query.isBlank() && !_searchFilters.value.hasActiveFilters()) {
            _isSearchActive.value = false
            _isAdvancedSearchActive.value = false
            loadCocktails() // Reset to all cocktails if query is empty and no filters
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

                // If we have active filters, use advanced search
                if (_searchFilters.value.hasActiveFilters() || _isAdvancedSearchActive.value) {
                    performAdvancedSearch()
                } else {
                    // Otherwise use basic search
                    repository.searchCocktailsByName(query)
                        .catch { e ->
                            handleError("Failed to search cocktails", e)
                        }
                        .collect { cocktailList ->
                            _cocktails.value = cocktailList
                            setLoading(false)
                        }
                }
            } catch (e: Exception) {
                handleError("Failed to search cocktails", e)
            }
        }
    }

    /**
     * Perform advanced search with current filters
     */
    private suspend fun performAdvancedSearch() {
        try {
            repository.advancedSearch(_searchFilters.value)
                .catch { e ->
                    handleError("Failed to perform advanced search", e)
                }
                .collect { cocktailList ->
                    _cocktails.value = cocktailList
                    setLoading(false)
                }
        } catch (e: Exception) {
            handleError("Failed to perform advanced search", e)
            setLoading(false)
        }
    }

    /**
     * Update search filters and perform search
     */
    fun updateSearchFilters(filters: com.cocktailcraft.domain.model.SearchFilters) {
        _searchFilters.value = filters
        _searchQuery.value = filters.query

        // Activate advanced search mode
        _isAdvancedSearchActive.value = filters.hasActiveFilters()

        // If we have a query or active filters, activate search mode
        if (filters.hasBasicSearch() || filters.hasActiveFilters()) {
            _isSearchActive.value = true

            // Cancel previous search job if it exists
            searchJob?.cancel()

            // Create a new search job with debounce
            searchJob = viewModelScope.launch {
                delay(300) // Debounce for 300ms
                setLoading(true)
                clearError() // Clear base class error
                _errorString.value = "" // Clear legacy error string

                performAdvancedSearch()
            }
        } else {
            // If no query and no filters, reset to all cocktails
            _isSearchActive.value = false
            _isAdvancedSearchActive.value = false
            loadCocktails()
        }
    }

    /**
     * Clear all search filters
     */
    fun clearSearchFilters() {
        val clearedFilters = _searchFilters.value.clearAllFilters()
        updateSearchFilters(clearedFilters)
    }

    /**
     * Toggle advanced search mode
     */
    fun toggleAdvancedSearchMode(active: Boolean) {
        _isAdvancedSearchActive.value = active

        if (!active) {
            // Clear all filters except query when disabling advanced search
            val query = _searchQuery.value
            _searchFilters.value = com.cocktailcraft.domain.model.SearchFilters(query = query)

            // If we still have a query, perform basic search
            if (query.isNotBlank()) {
                searchCocktails(query)
            } else {
                // Otherwise reset to all cocktails
                _isSearchActive.value = false
                loadCocktails()
            }
        }
    }

    // Helper function to handle errors consistently
    private fun handleError(baseMessage: String, e: Throwable) {
        CocktailDebugLogger.log("❌ HomeViewModel.handleError() called")
        CocktailDebugLogger.log("   - Base message: $baseMessage")
        CocktailDebugLogger.log("   - Error message: ${e.message}")
        CocktailDebugLogger.log("   - Error type: ${e::class.simpleName}")
        
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
        CocktailDebugLogger.log("   - Error category: $category")

        // Format the error message
        val errorMessage = when {
            e.message?.contains("timeout") == true ->
                "$baseMessage: The request timed out. Please try again."
            e.message?.contains("connection") == true ||
            e.message?.contains("network") == true ->
                networkErrorMessage
            else -> "$baseMessage: ${e.message}"
        }
        CocktailDebugLogger.log("   - Formatted error: $errorMessage")

        // Set the error using the base class method
        setError(
            title = if (category == ErrorUtils.ErrorCategory.NETWORK) "Network Error" else "Error",
            message = errorMessage,
            category = category,
            recoveryAction = recoveryAction
        )

        // Also update the legacy error string for backward compatibility
        _errorString.value = errorMessage
        CocktailDebugLogger.log("   - Error string set: ${_errorString.value}")

        // Log the error
        CocktailDebugLogger.log("❌ Error set: $baseMessage: ${e.message}")
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
        CocktailDebugLogger.log("🏷️ HomeViewModel.loadCocktailsByCategory() called with category: $category")
        
        // Track the current category to prevent duplicate loads
        val currentCategory = category ?: "Cocktail" // Default to "Cocktail" if null
        
        viewModelScope.launch {
            setLoading(true)
            clearError() // Clear base class error
            _errorString.value = "" // Clear legacy error string
            _currentPage.value = 1 // Reset to first page
            _hasMoreData.value = true // Reset pagination state

            try {
                CocktailDebugLogger.log("   🏷️ Loading cocktails for category: $currentCategory")
                repository.filterByCategory(currentCategory)
                    .catch { e ->
                        CocktailDebugLogger.log("   ❌ Flow error caught: ${e.message}")
                        // Check if we have cached data to use
                        val isOffline = _isOfflineMode.value || !_isNetworkAvailable.value
                        if (isOffline) {
                            CocktailDebugLogger.log("   📴 Offline mode, suppressing error")
                            setLoading(false)
                        } else {
                            handleError("Failed to filter cocktails", e)
                        }
                    }
                    .collect { cocktailList ->
                        CocktailDebugLogger.log("   ✅ Loaded ${cocktailList.size} cocktails for category: $currentCategory")
                        // Apply pagination
                        val paginatedList = cocktailList.take(PAGE_SIZE)
                        _cocktails.value = paginatedList
                        _hasMoreData.value = paginatedList.size < cocktailList.size
                        setLoading(false)
                        
                        // Clear any errors when we successfully get data
                        clearError() // Clear base class error
                        _errorString.value = "" // Clear legacy error string
                        CocktailDebugLogger.log("   ✅ Successfully loaded cocktails, errors cleared")
                    }
            } catch (e: Exception) {
                CocktailDebugLogger.log("   ❌ Exception in loadCocktailsByCategory: ${e.message}")
                CocktailDebugLogger.log("   Exception type: ${e::class.simpleName}")
                
                // Try to use cached data before showing error
                try {
                    val cachedCocktails = repository.getRecentlyViewedCocktails().first()
                        .filter { category == null || it.category == currentCategory }
                    
                    if (cachedCocktails.isNotEmpty()) {
                        _cocktails.value = cachedCocktails
                        clearError()
                        _errorString.value = ""
                        setLoading(false)
                        CocktailDebugLogger.log("   ✅ Using ${cachedCocktails.size} cached cocktails")
                        return@launch
                    }
                } catch (cacheException: Exception) {
                    CocktailDebugLogger.log("   ❌ Cache access failed: ${cacheException.message}")
                }
                
                // Show error if no cached data
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
                CocktailDebugLogger.log("Failed to add to favorites: ${e.message}")
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
                CocktailDebugLogger.log("Failed to remove from favorites: ${e.message}")
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
                CocktailDebugLogger.log("Failed to load recommendations: ${e.message}")
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