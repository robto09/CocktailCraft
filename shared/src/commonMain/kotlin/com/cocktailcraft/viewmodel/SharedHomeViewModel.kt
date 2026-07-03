package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import com.cocktailcraft.domain.usecase.FilterCocktailsUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageOfflineModeUseCase
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.viewmodel.state.HomeUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Home screen functionality.
 * Designed for full SKIE interoperability with iOS.
 *
 * Uses consolidated [HomeUiState] for atomic state updates.
 * iOS SKIE wrappers observe the single [uiState] flow.
 * Individual StateFlow properties are derived for backward compatibility with Android.
 */
class SharedHomeViewModel : SharedViewModel() {

    private val searchCocktailsUseCase: SearchCocktailsUseCase by inject()
    private val loadCocktailsByCategoryUseCase: LoadCocktailsByCategoryUseCase by inject()
    private val sortCocktailsUseCase: SortCocktailsUseCase by inject()
    private val filterCocktailsUseCase: FilterCocktailsUseCase by inject()
    private val manageFavoritesUseCase: ManageFavoritesUseCase by inject()
    private val manageOfflineModeUseCase: ManageOfflineModeUseCase by inject()
    private val getCocktailDetailUseCase: GetCocktailDetailUseCase by inject()
    private val networkMonitor: NetworkMonitor by inject()

    // Consolidated UI State — single source of truth
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Derived StateFlows for backward compatibility (Android wrappers reference these)
    val cocktails: StateFlow<List<Cocktail>> = _uiState
        .map { it.cocktails }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val favorites: StateFlow<List<Cocktail>> = _uiState
        .map { it.favorites }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val isOfflineMode: StateFlow<Boolean> = _uiState
        .map { it.isOfflineMode }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isNetworkAvailable: StateFlow<Boolean> = _uiState
        .map { it.isNetworkAvailable }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val searchQuery: StateFlow<String> = _uiState
        .map { it.searchQuery }.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val isSearchActive: StateFlow<Boolean> = _uiState
        .map { it.isSearchActive }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val searchFilters: StateFlow<SearchFilters> = _uiState
        .map { it.searchFilters }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchFilters())
    val isAdvancedSearchActive: StateFlow<Boolean> = _uiState
        .map { it.isAdvancedSearchActive }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val hasMoreData: StateFlow<Boolean> = _uiState
        .map { it.hasMoreData }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val isLoadingMore: StateFlow<Boolean> = _uiState
        .map { it.isLoadingMore }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val selectedCategory: StateFlow<String?> = _uiState
        .map { it.selectedCategory }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var searchJob: Job? = null
    private val PAGE_SIZE = 10
    
    init {
        initialize()
    }
    
    private fun initialize() {
        // Initialize offline mode
        _uiState.update { it.copy(isOfflineMode = manageOfflineModeUseCase.isOfflineModeEnabled()) }

        // Monitor network connectivity
        viewModelScope.launch {
            networkMonitor.startMonitoring()
            networkMonitor.isOnline.collectLatest { isOnline ->
                _uiState.update { it.copy(isNetworkAvailable = isOnline) }

                // Auto-retry on network restoration
                if (isOnline && error.value != null && _uiState.value.cocktails.isEmpty()) {
                    retry()
                }

                // Auto-enable offline mode when network lost
                if (!isOnline && !_uiState.value.isOfflineMode) {
                    setOfflineMode(true)
                }
            }
        }

        // Initial load
        viewModelScope.launch {
            delay(100) // Small delay for cache initialization
            if (_uiState.value.cocktails.isEmpty()) {
                loadCocktailsByCategory("Cocktail") // Default category
            }
            loadFavorites()
        }
    }
    
    /**
     * Load cocktails for the currently selected category.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktails(category: String? = null) {
        loadCocktailsByCategory(category ?: _uiState.value.selectedCategory ?: "Cocktail")
    }
    
    /**
     * Load cocktails by category.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktailsByCategory(category: String?) {
        val targetCategory = category ?: "Cocktail"

        _uiState.update { it.copy(
            selectedCategory = category,
            isLoading = true,
            currentPage = 1,
            hasMoreData = true
        ) }
        setLoading(true)
        clearError()

        try {
            val cocktailList = loadCocktailsByCategoryUseCase(targetCategory).getOrDefault(emptyList())
            val paginatedList = cocktailList.take(PAGE_SIZE)
            _uiState.update { it.copy(
                cocktails = paginatedList,
                hasMoreData = paginatedList.size < cocktailList.size,
                isLoading = false
            ) }
            setLoading(false)
            clearError()
        } catch (e: Exception) {
            val state = _uiState.value
            val isOffline = state.isOfflineMode || !state.isNetworkAvailable
            if (!isOffline) {
                handleException(
                    e,
                    "Failed to load cocktails",
                    recoveryAction = ErrorHandler.RecoveryAction("Retry") {
                        viewModelScope.launch { loadCocktailsByCategory(category) }
                    }
                )
            }
            // Try cached data before showing error
            tryLoadCachedData(targetCategory, e)
        }
    }
    
    /**
     * Search cocktails with debouncing.
     * SKIE will convert this to Swift async function.
     */
    suspend fun searchCocktails(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            loadCocktails()
        } else {
            _uiState.update { it.copy(isLoading = true) }
            setLoading(true)
            clearError()

            try {
                val cocktailList = searchCocktailsUseCase(query).getOrDefault(emptyList())
                _uiState.update { it.copy(cocktails = cocktailList, isLoading = false) }
                setLoading(false)
            } catch (e: Exception) {
                handleException(e, "Failed to search cocktails")
                _uiState.update { it.copy(isLoading = false) }
                setLoading(false)
            }
        }
    }
    
    /**
     * Load more cocktails for pagination.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadMoreCocktails() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMoreData) return

        _uiState.update { it.copy(isLoadingMore = true) }

        try {
            val nextPage = state.currentPage + 1
            val newItems = loadCocktailsByCategoryUseCase.loadMore(emptyList(), state.currentPage, PAGE_SIZE)

            _uiState.update { cur ->
                if (newItems.isNotEmpty()) {
                    cur.copy(
                        cocktails = cur.cocktails + newItems,
                        currentPage = nextPage,
                        hasMoreData = newItems.size >= PAGE_SIZE,
                        isLoadingMore = false
                    )
                } else {
                    cur.copy(hasMoreData = false, isLoadingMore = false)
                }
            }
        } catch (e: Exception) {
            handleException(e, "Failed to load more cocktails")
            _uiState.update { it.copy(isLoadingMore = false) }
        }
    }
    
    /**
     * Update search filters and perform search.
     * SKIE will convert this to Swift async function.
     */
    suspend fun applyFilters(category: String? = null, ingredient: String? = null) {
        _uiState.update { it.copy(isLoading = true) }
        setLoading(true)
        clearError()

        try {
            val filtered = filterCocktailsUseCase(category, ingredient).getOrDefault(emptyList())
            _uiState.update { it.copy(cocktails = filtered, isLoading = false) }
            setLoading(false)
        } catch (e: Exception) {
            handleException(e, "Failed to apply filters")
            _uiState.update { it.copy(isLoading = false) }
            setLoading(false)
        }
    }
    
    /**
     * Toggle favorite status for a cocktail.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleFavorite(cocktail: Cocktail) {
        try {
            manageFavoritesUseCase.toggle(cocktail)
            loadFavorites()
        } catch (e: Exception) {
            handleException(e, "Failed to update favorites")
        }
    }

    /**
     * Sort cocktails by price.
     * SKIE will convert this to Swift async function.
     */
    suspend fun sortByPrice(ascending: Boolean) {
        setLoading(true)
        try {
            val sortType = if (ascending) SortCocktailsUseCase.SortType.PRICE_ASC else SortCocktailsUseCase.SortType.PRICE_DESC
            _uiState.update { it.copy(cocktails = sortCocktailsUseCase(it.cocktails, sortType)) }
        } catch (e: Exception) {
            handleException(e, "Failed to sort cocktails")
        } finally {
            setLoading(false)
        }
    }

    /**
     * Sort cocktails by rating.
     */
    suspend fun sortByRating() {
        setLoading(true)
        try {
            _uiState.update { it.copy(cocktails = sortCocktailsUseCase(it.cocktails, SortCocktailsUseCase.SortType.RATING)) }
        } catch (e: Exception) {
            handleException(e, "Failed to sort cocktails")
        } finally {
            setLoading(false)
        }
    }

    /**
     * Sort cocktails by popularity.
     */
    suspend fun sortByPopularity() {
        setLoading(true)
        try {
            _uiState.update { it.copy(cocktails = sortCocktailsUseCase(it.cocktails, SortCocktailsUseCase.SortType.POPULARITY)) }
        } catch (e: Exception) {
            handleException(e, "Failed to sort cocktails")
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
            getCocktailDetailUseCase(id).getOrNull()
        } catch (e: Exception) {
            handleException(e, "Failed to load cocktail details")
            null
        }
    }



    /**
     * Force refresh cocktail details.
     */
    suspend fun refreshCocktailDetails(id: String) {
        try {
            setLoading(true)
            getCocktailDetailUseCase.refresh(id)
        } catch (e: Exception) {
            handleException(e, "Failed to refresh cocktail details")
        } finally {
            setLoading(false)
        }
    }
    
    /**
     * Get cocktails by category with limit.
     * SKIE will handle the List return type perfectly.
     */
    fun getCocktailsByCategory(category: String, limit: Int = 3): List<Cocktail> {
        val currentCocktails = _uiState.value.cocktails
        val fromCurrentList = currentCocktails
            .filter { it.category == category && !it.imageUrl.isNullOrBlank() }
            .shuffled()
            .take(limit)

        return if (fromCurrentList.size >= limit) {
            fromCurrentList
        } else {
            currentCocktails
                .filter { it.category != null && !it.imageUrl.isNullOrBlank() }
                .shuffled()
                .take(limit)
        }
    }

    fun getCategories(): List<String> {
        return _uiState.value.cocktails
            .mapNotNull { it.category }
            .distinct()
            .filterNot { it.isBlank() }
            .sorted()
    }

    fun setOfflineMode(enabled: Boolean) {
        _uiState.update { it.copy(isOfflineMode = enabled) }
        manageOfflineModeUseCase.setOfflineMode(enabled)

        if (!enabled && _uiState.value.isNetworkAvailable) {
            retry()
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchFilters = SearchFilters(), isSearchActive = false) }
        viewModelScope.launch { loadCocktails() }
    }

    fun toggleSearchMode(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active) }
        if (!active) {
            _uiState.update { it.copy(searchQuery = "", searchFilters = SearchFilters()) }
            viewModelScope.launch { loadCocktails() }
        }
    }

    fun toggleAdvancedSearchMode(active: Boolean) {
        _uiState.update { it.copy(isAdvancedSearchActive = active) }
    }

    fun clearSearchFilters() {
        _uiState.update { it.copy(searchFilters = SearchFilters()) }
        viewModelScope.launch { searchCocktails(_uiState.value.searchQuery) }
    }

    fun isFavorite(cocktailId: String): Boolean {
        return _uiState.value.favorites.any { it.id == cocktailId }
    }

    fun retry() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isSearchActive && state.searchQuery.isNotEmpty()) {
                searchCocktails(state.searchQuery)
            } else {
                loadCocktails()
            }
        }
    }
    
    // Private helper functions
    
    private suspend fun performSearch() {
        _uiState.update { it.copy(isLoading = true) }
        setLoading(true)
        clearError()

        try {
            val state = _uiState.value
            val cocktailList = if (state.searchFilters.hasActiveFilters()) {
                searchCocktailsUseCase.advancedSearch(state.searchFilters).getOrDefault(emptyList())
            } else {
                searchCocktailsUseCase(state.searchQuery).getOrDefault(emptyList())
            }
            _uiState.update { it.copy(cocktails = cocktailList, isLoading = false) }
            setLoading(false)
        } catch (e: Exception) {
            handleException(e, "Failed to search cocktails")
            _uiState.update { it.copy(isLoading = false) }
            setLoading(false)
        }
    }

    private suspend fun loadFavorites() {
        try {
            val favs = manageFavoritesUseCase.loadFavorites().getOrDefault(emptyList())
            _uiState.update { it.copy(favorites = favs) }
        } catch (e: Exception) {
            // Don't show error for favorites
        }
    }

    private suspend fun tryLoadCachedData(category: String, originalError: Exception) {
        try {
            val cachedCocktails = manageOfflineModeUseCase.getRecentlyViewedCocktails().getOrDefault(emptyList())
                .filter { category == "Cocktail" || it.category == category }

            if (cachedCocktails.isNotEmpty()) {
                _uiState.update { it.copy(cocktails = cachedCocktails, isLoading = false) }
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
            _uiState.update { it.copy(isLoading = false) }
            setLoading(false)
        }
    }
}