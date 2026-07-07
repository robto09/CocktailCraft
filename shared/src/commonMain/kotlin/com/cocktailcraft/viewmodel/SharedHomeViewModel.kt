package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageOfflineModeUseCase
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.viewmodel.state.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Home screen functionality.
 * Designed for full SKIE interoperability with iOS.
 *
 * Uses consolidated [HomeUiState] for atomic state updates.
 * iOS SKIE wrappers observe the single [uiState] flow.
 * Individual StateFlow properties are derived for backward compatibility with Android.
 */
class SharedHomeViewModel internal constructor(
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val loadCocktailsByCategoryUseCase: LoadCocktailsByCategoryUseCase,
    private val sortCocktailsUseCase: SortCocktailsUseCase,
    private val manageFavoritesUseCase: ManageFavoritesUseCase,
    private val manageOfflineModeUseCase: ManageOfflineModeUseCase,
    private val getCocktailDetailUseCase: GetCocktailDetailUseCase,
    private val catalogRepository: CocktailCatalogRepository,
    private val networkMonitor: NetworkMonitor
) : SharedViewModel() {

    // Consolidated UI State — single source of truth
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
                loadCocktailsByCategory(CocktailCategories.DEFAULT)
            }
            loadFavorites()
        }
    }
    
    /**
     * Load cocktails for the currently selected category.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktails(category: String? = null) {
        loadCocktailsByCategory(category ?: _uiState.value.selectedCategory ?: CocktailCategories.DEFAULT)
    }
    
    /**
     * Load cocktails by category.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktailsByCategory(category: String?) {
        val targetCategory = category ?: CocktailCategories.DEFAULT

        _uiState.update { it.copy(
            selectedCategory = category,
            isLoading = true,
            currentPage = 1,
            hasMoreData = true
        ) }
        clearError()

        try {
            val cocktailList = loadCocktailsByCategoryUseCase(targetCategory).getOrThrow()
            val paginatedList = cocktailList.take(PAGE_SIZE)
            _uiState.update { it.copy(
                cocktails = paginatedList,
                hasMoreData = paginatedList.size < cocktailList.size,
                isLoading = false
            ) }
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
     * Search cocktails by name, composed with any active advanced-search filters.
     * SKIE will convert this to Swift async function.
     */
    suspend fun searchCocktails(query: String) {
        val filters = _uiState.value.searchFilters.copy(query = query)
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isBlank() && !filters.hasActiveFilters()) {
            // Nothing left to search — fall back to the current category listing.
            _uiState.update { it.copy(searchFilters = filters) }
            loadCocktails()
        } else {
            applyFilters(filters)
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
            val category = state.selectedCategory ?: "Cocktail"
            val newItems = loadCocktailsByCategoryUseCase.loadMore(category, state.currentPage, PAGE_SIZE)

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
     * Apply the advanced-search [filters] and refresh the cocktail list.
     * Stores [filters] into state so the active-filter chips reflect them.
     * SKIE will convert this to Swift async function.
     */
    suspend fun applyFilters(filters: SearchFilters) {
        _uiState.update { it.copy(searchFilters = filters, isLoading = true) }
        clearError()

        try {
            val filtered = searchCocktailsUseCase.search(filters).getOrThrow()
            _uiState.update { it.copy(cocktails = filtered, isLoading = false) }
        } catch (e: Exception) {
            handleException(e, "Failed to apply filters")
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Load the option lists (categories / ingredients) that back the
     * advanced-search filter UI on both platforms, via [CocktailCatalogRepository]'s
     * list.php endpoints. Failures leave the current options untouched.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadFilterOptions() {
        try {
            val categories = catalogRepository.getCategories().getOrDefault(emptyList())
            val ingredients = catalogRepository.getIngredients().getOrDefault(emptyList())
            _uiState.update { it.copy(
                filterCategories = categories,
                filterIngredients = ingredients
            ) }
        } catch (e: Exception) {
            // Non-fatal: the filter UI simply has no options to offer.
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
        try {
            val sortType = if (ascending) SortCocktailsUseCase.SortType.PRICE_ASC else SortCocktailsUseCase.SortType.PRICE_DESC
            _uiState.update { it.copy(cocktails = sortCocktailsUseCase(it.cocktails, sortType)) }
        } catch (e: Exception) {
            handleException(e, "Failed to sort cocktails")
        } finally {
        }
    }

    /**
     * Sort cocktails by rating.
     */
    suspend fun sortByRating() {
        try {
            _uiState.update { it.copy(cocktails = sortCocktailsUseCase(it.cocktails, SortCocktailsUseCase.SortType.RATING)) }
        } catch (e: Exception) {
            handleException(e, "Failed to sort cocktails")
        } finally {
        }
    }

    /**
     * Sort cocktails by popularity.
     */
    suspend fun sortByPopularity() {
        try {
            _uiState.update { it.copy(cocktails = sortCocktailsUseCase(it.cocktails, SortCocktailsUseCase.SortType.POPULARITY)) }
        } catch (e: Exception) {
            handleException(e, "Failed to sort cocktails")
        } finally {
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
            getCocktailDetailUseCase.refresh(id)
        } catch (e: Exception) {
            handleException(e, "Failed to refresh cocktail details")
        } finally {
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

    /** Canonical curated category list — single source of truth for category chips on both platforms. */
    val curatedCategories: List<String>
        get() = CocktailCategories.CURATED

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

    /** Clear only the query text; any active advanced-search filters are kept. */
    fun clearSearch() {
        _uiState.update { it.copy(isSearchActive = false) }
        viewModelScope.launch { searchCocktails("") }
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

    /** Reset all advanced-search filters (and the query) and reload the default list. */
    fun clearSearchFilters() {
        _uiState.update { it.copy(searchQuery = "", searchFilters = SearchFilters()) }
        viewModelScope.launch { loadCocktails() }
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
                .filter { category == CocktailCategories.DEFAULT || it.category == category }

            if (cachedCocktails.isNotEmpty()) {
                _uiState.update { it.copy(cocktails = cachedCocktails, isLoading = false) }
                clearError()
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
        }
    }
}