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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Refactored HomeViewModel that uses use cases instead of directly accessing repositories.
 * This ViewModel is responsible for the home screen, including cocktail listing, searching, and filtering.
 */
class RefactoredHomeViewModel : BaseViewModel() {

    // Use cases
    private val getCocktailsUseCase: GetCocktailsUseCase by inject()
    private val searchCocktailsUseCase: SearchCocktailsUseCase by inject()
    private val networkStatusUseCase: NetworkStatusUseCase by inject()

    // Cocktails state
    private val _cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val cocktails: StateFlow<List<Cocktail>> = _cocktails.asStateFlow()

    // Pagination state
    private val _currentPage = MutableStateFlow(1)
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    private val PAGE_SIZE = 10

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()
    private val _searchFilters = MutableStateFlow(SearchFilters())
    val searchFilters: StateFlow<SearchFilters> = _searchFilters.asStateFlow()
    private val _isAdvancedSearchActive = MutableStateFlow(false)
    val isAdvancedSearchActive: StateFlow<Boolean> = _isAdvancedSearchActive.asStateFlow()

    // Network state
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

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
    fun loadCocktails() {
        executeWithErrorHandling(
            operation = {
                _currentPage.value = 1 // Reset to first page
                _hasMoreData.value = true // Reset pagination state
                getCocktailsUseCase()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success<List<Cocktail>> -> {
                                val paginatedList = result.data.take(PAGE_SIZE)
                                _cocktails.value = paginatedList
                                _hasMoreData.value = paginatedList.size < result.data.size

                                // If we got data while offline, clear any error
                                if (_isOfflineMode.value && result.data.isNotEmpty()) {
                                    clearError() // Clear base class error
                                    _errorString.value = "" // Clear legacy error string
                                }
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Load Cocktails",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA,
                                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { retry() }
                                )
                                _errorString.value = result.message
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to load cocktails. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Retry") { retry() }
        )
    }

    /**
     * Load more cocktails for pagination.
     */
    fun loadMoreCocktails() {
        // Don't load more if already loading or no more data
        if (_isLoadingMore.value || !_hasMoreData.value) return

        viewModelScope.launch {
            try {
                _isLoadingMore.value = true

                // Increment page
                val nextPage = _currentPage.value + 1

                getCocktailsUseCase().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            // Calculate the range for the next page
                            val startIndex = _currentPage.value * PAGE_SIZE
                            val endIndex = startIndex + PAGE_SIZE

                            // Get items for the next page
                            val newItems = result.data.drop(startIndex).take(PAGE_SIZE)

                            if (newItems.isNotEmpty()) {
                                // Append new items to existing list
                                _cocktails.value = _cocktails.value + newItems
                                _currentPage.value = nextPage

                                // Check if we have more data
                                _hasMoreData.value = endIndex < result.data.size
                            } else {
                                // No more items to load
                                _hasMoreData.value = false
                            }
                        }
                        is Result.Error -> {
                            setError(
                                title = "Failed to Load More",
                                message = result.message,
                                category = ErrorUtils.ErrorCategory.DATA,
                                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadMoreCocktails() }
                            )
                            _errorString.value = result.message
                        }
                        is Result.Loading -> {
                            // No action needed
                        }
                    }
                    _isLoadingMore.value = false
                }
            } catch (e: Exception) {
                handleException(
                    exception = e,
                    defaultMessage = "Failed to load more cocktails",
                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadMoreCocktails() }
                )
                _errorString.value = "Failed to load more cocktails: ${e.message}"
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * Search cocktails by name.
     */
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

            executeWithErrorHandling(
                operation = {
                    // Check if we have active filters
                    if (_searchFilters.value.hasActiveFilters() || _isAdvancedSearchActive.value) {
                        searchCocktailsUseCase.advanced(_searchFilters.value)
                    } else {
                        searchCocktailsUseCase(query)
                    }
                },
                onSuccess = { resultFlow ->
                    viewModelScope.launch {
                        resultFlow.collect { result ->
                            when (result) {
                                is Result.Success<List<Cocktail>> -> {
                                    _cocktails.value = result.data
                                }
                                is Result.Error -> {
                                    setError(
                                        title = "Search Failed",
                                        message = result.message,
                                        category = ErrorUtils.ErrorCategory.DATA,
                                        recoveryAction = ErrorUtils.RecoveryAction("Retry") { searchCocktails(query) }
                                    )
                                    _errorString.value = result.message
                                }
                                is Result.Loading -> {
                                    // Already handled by executeWithErrorHandling
                                }
                            }
                        }
                    }
                },
                defaultErrorMessage = "Failed to search cocktails. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { searchCocktails(query) }
            )
        }
    }

    /**
     * Update search filters and perform search.
     */
    fun updateSearchFilters(filters: SearchFilters) {
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

                executeWithErrorHandling(
                    operation = {
                        searchCocktailsUseCase.advanced(filters)
                    },
                    onSuccess = { resultFlow ->
                        viewModelScope.launch {
                            resultFlow.collect { result ->
                                when (result) {
                                    is Result.Success<List<Cocktail>> -> {
                                        _cocktails.value = result.data
                                    }
                                    is Result.Error -> {
                                        setError(
                                            title = "Filter Failed",
                                            message = result.message,
                                            category = ErrorUtils.ErrorCategory.DATA,
                                            recoveryAction = ErrorUtils.RecoveryAction("Retry") { updateSearchFilters(filters) }
                                        )
                                        _errorString.value = result.message
                                    }
                                    is Result.Loading -> {
                                        // Already handled by executeWithErrorHandling
                                    }
                                }
                            }
                        }
                    },
                    defaultErrorMessage = "Failed to apply filters. Please try again.",
                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { updateSearchFilters(filters) }
                )
            }
        } else {
            // If no query and no filters, reset to all cocktails
            _isSearchActive.value = false
            _isAdvancedSearchActive.value = false
            loadCocktails()
        }
    }

    /**
     * Clear all search filters.
     */
    fun clearSearchFilters() {
        val clearedFilters = _searchFilters.value.clearAllFilters()
        updateSearchFilters(clearedFilters)
    }

    /**
     * Toggle advanced search mode.
     */
    fun toggleAdvancedSearchMode(active: Boolean) {
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
    fun toggleSearchMode(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            loadCocktails() // Reset to all cocktails when search is deactivated
        }
    }

    /**
     * Load cocktails filtered by category.
     */
    fun loadCocktailsByCategory(category: String?) {
        executeWithErrorHandling(
            operation = {
                if (category == null) {
                    getCocktailsUseCase()
                } else {
                    getCocktailsUseCase.byCategory(category)
                }
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success<List<Cocktail>> -> {
                                _cocktails.value = result.data
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Filter",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA,
                                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCocktailsByCategory(category) }
                                )
                                _errorString.value = result.message
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to filter cocktails. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCocktailsByCategory(category) }
        )
    }

    /**
     * Sort cocktails by price.
     */
    fun sortByPrice(ascending: Boolean) {
        executeWithErrorHandling(
            operation = {
                getCocktailsUseCase.sortedByPrice(ascending)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success<List<Cocktail>> -> {
                                _cocktails.value = result.data
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Sort",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA,
                                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { sortByPrice(ascending) }
                                )
                                _errorString.value = result.message
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to sort cocktails. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Retry") { sortByPrice(ascending) }
        )
    }

    /**
     * Sort cocktails by popularity.
     */
    fun sortByPopularity() {
        executeWithErrorHandling(
            operation = {
                getCocktailsUseCase.sortedByPopularity()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success<List<Cocktail>> -> {
                                _cocktails.value = result.data
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Sort",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA,
                                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { sortByPopularity() }
                                )
                                _errorString.value = result.message
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to sort cocktails. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Retry") { sortByPopularity() }
        )
    }

    /**
     * Set offline mode to a specific value.
     */
    fun setOfflineMode(enabled: Boolean) {
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
    fun retry() {
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

    override fun onCleared() {
        super.onCleared()
        networkStatusUseCase.stopMonitoring()
    }
}
