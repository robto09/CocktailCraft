package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.usecase.GetCocktailsUseCase
import com.cocktailcraft.domain.usecase.NetworkStatusUseCase
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * HomeViewModel that uses use cases instead of directly accessing repositories.
 * This ViewModel is responsible for the home screen, including cocktail listing, searching, and filtering.
 *
 * Following MVVM + Clean Architecture principles, this ViewModel:
 * - Depends on use cases, not repositories
 * - Manages UI state
 * - Handles user interactions
 * - Provides a clean API for the UI layer
 * - Implements the IHomeViewModel interface for cross-platform compatibility
 */
class HomeViewModel(
    private val getCocktailsUseCase: GetCocktailsUseCase,
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase
) : BaseViewModel(), IHomeViewModel {

    // Cocktails state
    private val _cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    override val cocktails: StateFlow<List<Cocktail>> = _cocktails.asStateFlow()

    // Pagination state
    private val _currentPage = MutableStateFlow(1)
    private val _hasMoreData = MutableStateFlow(true)
    override val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()
    private val _isLoadingMore = MutableStateFlow(false)
    override val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    private val PAGE_SIZE = 10

    // Search state
    private val _searchQuery = MutableStateFlow("")
    override val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _isSearchActive = MutableStateFlow(false)
    override val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()
    private val _searchFilters = MutableStateFlow(SearchFilters())
    override val searchFilters: StateFlow<SearchFilters> = _searchFilters.asStateFlow()
    private val _isAdvancedSearchActive = MutableStateFlow(false)
    override val isAdvancedSearchActive: StateFlow<Boolean> = _isAdvancedSearchActive.asStateFlow()
    
    // Advanced search state
    private val _isAdvancedSearchLoading = MutableStateFlow(false)
    val isAdvancedSearchLoading: StateFlow<Boolean> = _isAdvancedSearchLoading.asStateFlow()
    private val _advancedSearchResultCount = MutableStateFlow(0)
    val advancedSearchResultCount: StateFlow<Int> = _advancedSearchResultCount.asStateFlow()

    // Network state
    private val _isOfflineMode = MutableStateFlow(false)
    override val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    private val _isNetworkAvailable = MutableStateFlow(true)
    override val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    // Legacy error string for backward compatibility
    private val _errorString = MutableStateFlow<String>("")
    val errorString: StateFlow<String> = _errorString.asStateFlow()

    // Search job for debouncing
    private var searchJob: Job? = null

    init {
        // Initialize offline mode status
        _isOfflineMode.value = networkStatusUseCase.isOfflineModeEnabled()

        // Start monitoring network connectivity
        viewModelScope.launch {
            networkStatusUseCase.startMonitoring()
            networkStatusUseCase.getNetworkAvailability().collectLatest { isOnline ->
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

        loadCocktails()
    }

    /**
     * Load cocktails with the newest first.
     */
    override fun loadCocktails() {
        _currentPage.value = 1 // Reset to first page
        _hasMoreData.value = true // Reset pagination state

        viewModelScope.launch {
            handleResultFlow(
                flow = getCocktailsUseCase(),
                onSuccess = { cocktails ->
                    val paginatedList = cocktails.take(PAGE_SIZE)
                    _cocktails.value = paginatedList
                    _hasMoreData.value = paginatedList.size < cocktails.size

                    // If we got data while offline, clear any error
                    if (_isOfflineMode.value && cocktails.isNotEmpty()) {
                        clearError() // Clear base class error
                        _errorString.value = "" // Clear legacy error string
                    }
                },
                onError = { error ->
                    _errorString.value = error.message
                },
                defaultErrorMessage = "Failed to load cocktails. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { retry() }
            )
        }
    }

    /**
     * Load more cocktails for pagination.
     */
    override fun loadMoreCocktails() {
        // Don't load more if already loading or no more data
        if (_isLoadingMore.value || !_hasMoreData.value) return

        _isLoadingMore.value = true

        // Increment page
        val nextPage = _currentPage.value + 1

        viewModelScope.launch {
            handleResultFlow(
                flow = getCocktailsUseCase(),
                onSuccess = { cocktails ->
                    // Calculate the range for the next page
                    val startIndex = _currentPage.value * PAGE_SIZE
                    val endIndex = startIndex + PAGE_SIZE

                    // Get items for the next page
                    val newItems = cocktails.drop(startIndex).take(PAGE_SIZE)

                    if (newItems.isNotEmpty()) {
                        // Append new items to existing list
                        _cocktails.value = _cocktails.value + newItems
                        _currentPage.value = nextPage

                        // Check if we have more data
                        _hasMoreData.value = endIndex < cocktails.size
                    } else {
                        // No more items to load
                        _hasMoreData.value = false
                    }

                    _isLoadingMore.value = false
                },
                onError = { error ->
                    _errorString.value = error.message
                    _isLoadingMore.value = false
                },
                onLoading = {
                    // No additional loading action needed
                },
                defaultErrorMessage = "Failed to load more cocktails",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadMoreCocktails() }
            )
        }
    }

    /**
     * Search cocktails by name.
     */
    override fun searchCocktails(query: String) {
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

            // Check if we have active filters
            val searchFlow = if (_searchFilters.value.hasActiveFilters() || _isAdvancedSearchActive.value) {
                searchCocktailsUseCase.advanced(_searchFilters.value)
            } else {
                searchCocktailsUseCase(query)
            }

            handleResultFlow(
                flow = searchFlow,
                onSuccess = { cocktails ->
                    _cocktails.value = cocktails
                },
                onError = { error ->
                    _errorString.value = error.message
                },
                defaultErrorMessage = "Failed to search cocktails. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { searchCocktails(query) }
            )
        }
    }

    /**
     * Update search filters and perform search.
     */
    override fun updateSearchFilters(filters: SearchFilters) {
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
                
                _isAdvancedSearchLoading.value = true

                handleResultFlow(
                    flow = searchCocktailsUseCase.advanced(filters),
                    onSuccess = { cocktails ->
                        _cocktails.value = cocktails
                        _advancedSearchResultCount.value = cocktails.size
                        _isAdvancedSearchLoading.value = false
                    },
                    onError = { error ->
                        _errorString.value = error.message
                        _isAdvancedSearchLoading.value = false
                    },
                    defaultErrorMessage = "Failed to apply filters. Please try again.",
                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { updateSearchFilters(filters) }
                )
            }
        } else {
            // If no query and no filters, reset to all cocktails
            _isSearchActive.value = false
            _isAdvancedSearchActive.value = false
            _advancedSearchResultCount.value = 0
            loadCocktails()
        }
    }

    /**
     * Clear all search filters.
     */
    override fun clearSearchFilters() {
        val clearedFilters = _searchFilters.value.clearAllFilters()
        updateSearchFilters(clearedFilters)
    }

    /**
     * Toggle advanced search mode.
     */
    override fun toggleAdvancedSearchMode(active: Boolean) {
        _isAdvancedSearchActive.value = active

        if (!active) {
            // Clear all filters except query when disabling advanced search
            val query = _searchQuery.value
            _searchFilters.value = SearchFilters(query = query)

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

    /**
     * Toggle search mode.
     */
    override fun toggleSearchMode(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            loadCocktails() // Reset to all cocktails when search is deactivated
        }
    }

    /**
     * Load cocktails filtered by category.
     */
    override fun loadCocktailsByCategory(category: String?) {
        viewModelScope.launch {
            val flow = if (category == null) {
                getCocktailsUseCase()
            } else {
                getCocktailsUseCase.byCategory(category)
            }
            
            handleResultFlow(
                flow = flow,
                onSuccess = { cocktails ->
                    _cocktails.value = cocktails
                },
                onError = { error ->
                    _errorString.value = error.message
                },
                defaultErrorMessage = "Failed to filter cocktails. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCocktailsByCategory(category) }
            )
        }
    }

    /**
     * Sort cocktails by price.
     */
    override fun sortByPrice(ascending: Boolean) {
        viewModelScope.launch {
            handleResultFlow(
                flow = getCocktailsUseCase.sortedByPrice(ascending),
                onSuccess = { cocktails ->
                    _cocktails.value = cocktails
                },
                onError = { error ->
                    _errorString.value = error.message
                },
                defaultErrorMessage = "Failed to sort cocktails. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { sortByPrice(ascending) }
            )
        }
    }

    /**
     * Sort cocktails by popularity.
     */
    override fun sortByPopularity() {
        viewModelScope.launch {
            handleResultFlow(
                flow = getCocktailsUseCase.sortedByPopularity(),
                onSuccess = { cocktails ->
                    _cocktails.value = cocktails
                },
                onError = { error ->
                    _errorString.value = error.message
                },
                defaultErrorMessage = "Failed to sort cocktails. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { sortByPopularity() }
            )
        }
    }

    /**
     * Set offline mode to a specific value.
     */
    override  fun setOfflineMode(enabled: Boolean) {
        _isOfflineMode.value = enabled
        networkStatusUseCase.setOfflineMode(enabled)

        // If switching to online mode, reload data
        if (!enabled && _isNetworkAvailable.value) {
            retry()
        }
    }

    /**
     * Retry the current operation.
     */
    override fun retry() {
        if (_isSearchActive.value && _searchQuery.value.isNotEmpty()) {
            // If in search mode, retry the search
            searchCocktails(_searchQuery.value)
        } else if (_isSearchActive.value && _searchQuery.value.isEmpty()) {
            // If search is active but query is empty, reset to normal mode
            toggleSearchMode(false)
        } else {
            // Otherwise retry loading
            loadCocktails()
        }
    }

    /**
     * Clear legacy error.
     */
    fun clearLegacyError() {
        clearError() // Call the base class method
        _errorString.value = "" // Also clear the legacy error string
    }

    /**
     * Get a cocktail by ID.
     * This is used by the CocktailDetailScreen.
     */
    override fun getCocktailById(id: String): Flow<Cocktail?> {
        return executeWithErrorHandlingFlow(
            operation = {
                getCocktailsUseCase.getById(id)
            },
            defaultErrorMessage = "Failed to get cocktail details. Please try again."
        )
    }

    /**
     * Force refresh cocktail details.
     * This is used by the CocktailDetailScreen.
     */
    fun forceRefreshCocktailDetails(id: String) {
        viewModelScope.launch {
            handleResultFlow(
                flow = getCocktailsUseCase.refreshCocktailDetails(id),
                onSuccess = { _ ->
                    // No need to update state, just log success
                    println("Successfully refreshed cocktail details for $id")
                },
                onError = { error ->
                    _errorString.value = error.message
                },
                defaultErrorMessage = "Failed to refresh cocktail details. Please try again."
            )
        }
    }

    /**
     * Get cocktails by category with a limit.
     * This is used by the CocktailDetailScreen.
     */
    fun getCocktailsByCategory(category: String, limit: Int): Flow<List<Cocktail>> {
        return flow {
            // Emit the current cocktails filtered by category and limited to the specified number
            val filteredCocktails = _cocktails.value.filter {
                it.category?.equals(category, ignoreCase = true) == true
            }.take(limit)
            emit(filteredCocktails)
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkStatusUseCase.stopMonitoring()
    }
}
