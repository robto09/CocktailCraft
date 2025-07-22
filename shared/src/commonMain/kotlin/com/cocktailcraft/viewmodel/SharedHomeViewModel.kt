package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Home screen functionality.
 * Designed for full SKIE interoperability with iOS.
 * 
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 */
class SharedHomeViewModel : SharedViewModel() {
    
    private val repository: CocktailRepository by inject()
    private val networkMonitor: NetworkMonitor by inject()
    
    // UI State - SKIE will convert these to Swift AsyncSequence
    private val _cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val cocktails: StateFlow<List<Cocktail>> = _cocktails.asStateFlow()
    
    private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())
    val favorites: StateFlow<List<Cocktail>> = _favorites.asStateFlow()
    
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    
    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
    
    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()
    
    private val _searchFilters = MutableStateFlow(SearchFilters())
    val searchFilters: StateFlow<SearchFilters> = _searchFilters.asStateFlow()
    
    // Pagination state
    private val _currentPage = MutableStateFlow(1)
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    // Currently selected category
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    private var searchJob: Job? = null
    private val PAGE_SIZE = 10
    
    init {
        initialize()
    }
    
    private fun initialize() {
        // Initialize offline mode
        _isOfflineMode.value = repository.isOfflineModeEnabled()
        
        // Monitor network connectivity
        viewModelScope.launch {
            networkMonitor.startMonitoring()
            networkMonitor.isOnline.collectLatest { isOnline ->
                _isNetworkAvailable.value = isOnline
                
                // Auto-retry on network restoration
                if (isOnline && error.value != null && cocktails.value.isEmpty()) {
                    retry()
                }
                
                // Auto-enable offline mode when network lost
                if (!isOnline && !_isOfflineMode.value) {
                    setOfflineMode(true)
                }
            }
        }
        
        // Initial load
        viewModelScope.launch {
            delay(100) // Small delay for cache initialization
            if (cocktails.value.isEmpty()) {
                loadCocktailsByCategory("Cocktail") // Default category
            }
            loadFavorites()
        }
    }
    
    /**
     * Load cocktails for the currently selected category.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktails() {
        loadCocktailsByCategory(_selectedCategory.value ?: "Cocktail")
    }
    
    /**
     * Load cocktails by category.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktailsByCategory(category: String?) {
        _selectedCategory.value = category
        val targetCategory = category ?: "Cocktail"
        
        setLoading(true)
        clearError()
        _currentPage.value = 1
        _hasMoreData.value = true
        
        try {
            repository.filterByCategory(targetCategory)
                .catch { e ->
                    val isOffline = _isOfflineMode.value || !_isNetworkAvailable.value
                    if (!isOffline) {
                        handleException(
                            e,
                            "Failed to load cocktails",
                            recoveryAction = ErrorHandler.RecoveryAction("Retry") { 
                                viewModelScope.launch { loadCocktailsByCategory(category) }
                            }
                        )
                    }
                }
                .collect { cocktailList ->
                    val paginatedList = cocktailList.take(PAGE_SIZE)
                    _cocktails.value = paginatedList
                    _hasMoreData.value = paginatedList.size < cocktailList.size
                    setLoading(false)
                    clearError()
                }
        } catch (e: Exception) {
            // Try cached data before showing error
            tryLoadCachedData(targetCategory, e)
        }
    }
    
    /**
     * Search cocktails with debouncing.
     * SKIE will convert this to Swift async function.
     */
    suspend fun searchCocktails(query: String) {
        _searchQuery.value = query
        _searchFilters.value = _searchFilters.value.copy(query = query)
        
        if (query.isNotEmpty() && !_isSearchActive.value) {
            _isSearchActive.value = true
        }
        
        searchJob?.cancel()
        
        if (query.isBlank() && !_searchFilters.value.hasActiveFilters()) {
            _isSearchActive.value = false
            loadCocktails()
            return
        }
        
        // Debounce search
        searchJob = viewModelScope.launch {
            delay(300)
            performSearch()
        }
    }
    
    /**
     * Load more cocktails for pagination.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadMoreCocktails() {
        if (_isLoadingMore.value || !_hasMoreData.value) return
        
        _isLoadingMore.value = true
        
        try {
            val nextPage = _currentPage.value + 1
            
            repository.getCocktailsSortedByNewest()
                .catch { e ->
                    handleException(e, "Failed to load more cocktails")
                    _isLoadingMore.value = false
                }
                .collect { allCocktails ->
                    val startIndex = _currentPage.value * PAGE_SIZE
                    val newItems = allCocktails.drop(startIndex).take(PAGE_SIZE)
                    
                    if (newItems.isNotEmpty()) {
                        _cocktails.value = _cocktails.value + newItems
                        _currentPage.value = nextPage
                        _hasMoreData.value = startIndex + PAGE_SIZE < allCocktails.size
                    } else {
                        _hasMoreData.value = false
                    }
                    
                    _isLoadingMore.value = false
                }
        } catch (e: Exception) {
            handleException(e, "Failed to load more cocktails")
            _isLoadingMore.value = false
        }
    }
    
    /**
     * Update search filters and perform search.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateSearchFilters(filters: SearchFilters) {
        _searchFilters.value = filters
        _searchQuery.value = filters.query
        _isSearchActive.value = filters.hasBasicSearch() || filters.hasActiveFilters()
        
        if (_isSearchActive.value) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(300)
                performSearch()
            }
        } else {
            loadCocktails()
        }
    }
    
    /**
     * Toggle favorite status for a cocktail.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleFavorite(cocktail: Cocktail) {
        try {
            if (isFavorite(cocktail.id)) {
                repository.removeFromFavorites(cocktail)
            } else {
                repository.addToFavorites(cocktail)
            }
            loadFavorites()
        } catch (e: Exception) {
            handleException(e, "Failed to update favorites", showAsEvent = true)
        }
    }
    
    /**
     * Sort cocktails by price.
     * SKIE will convert this to Swift async function.
     */
    suspend fun sortByPrice(ascending: Boolean) {
        setLoading(true)
        try {
            val sortedList = if (ascending) {
                _cocktails.value.sortedBy { it.price }
            } else {
                _cocktails.value.sortedByDescending { it.price }
            }
            _cocktails.value = sortedList
        } catch (e: Exception) {
            handleException(e, "Failed to sort cocktails", showAsEvent = true)
        } finally {
            setLoading(false)
        }
    }
    
    /**
     * Sort cocktails by rating.
     * SKIE will convert this to Swift async function.
     */
    suspend fun sortByRating() {
        setLoading(true)
        try {
            _cocktails.value = _cocktails.value.sortedByDescending { it.rating }
        } catch (e: Exception) {
            handleException(e, "Failed to sort cocktails", showAsEvent = true)
        } finally {
            setLoading(false)
        }
    }
    
    /**
     * Sort cocktails by popularity.
     * SKIE will convert this to Swift async function.
     */
    suspend fun sortByPopularity() {
        setLoading(true)
        try {
            _cocktails.value = _cocktails.value.sortedByDescending { it.popularity }
        } catch (e: Exception) {
            handleException(e, "Failed to sort cocktails", showAsEvent = true)
        } finally {
            setLoading(false)
        }
    }
    
    /**
     * Get cocktail by ID.
     * SKIE will convert this to Swift async function returning optional.
     */
    suspend fun getCocktailById(id: String): Cocktail? {
        return try {
            repository.getCocktailById(id).first()
        } catch (e: Exception) {
            handleException(e, "Failed to load cocktail details", showAsEvent = true)
            null
        }
    }
    
    /**
     * Get cocktails by category with limit.
     * SKIE will handle the List return type perfectly.
     */
    fun getCocktailsByCategory(category: String, limit: Int = 3): List<Cocktail> {
        val fromCurrentList = _cocktails.value
            .filter { it.category == category && !it.imageUrl.isNullOrBlank() }
            .shuffled()
            .take(limit)
        
        return if (fromCurrentList.size >= limit) {
            fromCurrentList
        } else {
            // Return any available cocktails as fallback
            _cocktails.value
                .filter { it.category != null && !it.imageUrl.isNullOrBlank() }
                .shuffled()
                .take(limit)
        }
    }
    
    /**
     * Get available categories.
     * SKIE will handle the List<String> return type perfectly.
     */
    fun getCategories(): List<String> {
        return _cocktails.value
            .mapNotNull { it.category }
            .distinct()
            .filterNot { it.isBlank() }
            .sorted()
    }
    
    /**
     * Set offline mode.
     */
    fun setOfflineMode(enabled: Boolean) {
        _isOfflineMode.value = enabled
        repository.setOfflineMode(enabled)
        
        if (!enabled && _isNetworkAvailable.value) {
            retry()
        }
    }
    
    /**
     * Clear search and filters.
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchFilters.value = SearchFilters()
        _isSearchActive.value = false
        viewModelScope.launch { loadCocktails() }
    }
    
    /**
     * Check if a cocktail is favorited.
     */
    fun isFavorite(cocktailId: String): Boolean {
        return _favorites.value.any { it.id == cocktailId }
    }
    
    /**
     * Retry last operation.
     */
    fun retry() {
        viewModelScope.launch {
            if (_isSearchActive.value && _searchQuery.value.isNotEmpty()) {
                searchCocktails(_searchQuery.value)
            } else {
                loadCocktails()
            }
        }
    }
    
    // Private helper functions
    
    private suspend fun performSearch() {
        setLoading(true)
        clearError()
        
        try {
            if (_searchFilters.value.hasActiveFilters()) {
                repository.advancedSearch(_searchFilters.value)
            } else {
                repository.searchCocktailsByName(_searchQuery.value)
            }
                .catch { e -> handleException(e, "Failed to search cocktails") }
                .collect { cocktailList ->
                    _cocktails.value = cocktailList
                    setLoading(false)
                }
        } catch (e: Exception) {
            handleException(e, "Failed to search cocktails")
            setLoading(false)
        }
    }
    
    private suspend fun loadFavorites() {
        try {
            repository.getFavoriteCocktails()
                .catch { /* Silent fail for favorites */ }
                .collect { _favorites.value = it }
        } catch (e: Exception) {
            // Don't show error for favorites
        }
    }
    
    private suspend fun tryLoadCachedData(category: String, originalError: Exception) {
        try {
            val cachedCocktails = repository.getRecentlyViewedCocktails().first()
                .filter { category == "Cocktail" || it.category == category }
            
            if (cachedCocktails.isNotEmpty()) {
                _cocktails.value = cachedCocktails
                clearError()
                setLoading(false)
            } else {
                throw originalError
            }
        } catch (e: Exception) {
            handleException(
                originalError,
                "Failed to load cocktails",
                recoveryAction = ErrorHandler.RecoveryAction("Retry") { retry() }
            )
            setLoading(false)
        }
    }
}