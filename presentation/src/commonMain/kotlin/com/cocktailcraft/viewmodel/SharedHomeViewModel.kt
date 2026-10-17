package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_retry
import com.cocktailcraft.resources.error_apply_filters_failed
import com.cocktailcraft.resources.error_load_cocktail_details_failed
import com.cocktailcraft.resources.error_load_cocktails_failed
import com.cocktailcraft.resources.error_load_more_cocktails_failed
import com.cocktailcraft.resources.error_refresh_cocktail_details_failed
import com.cocktailcraft.resources.error_sort_cocktails_failed
import com.cocktailcraft.resources.error_update_favorites_failed
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.viewmodel.state.HomeUiState
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Home screen functionality.
 * Designed for full SKIE interoperability with iOS.
 *
 * Uses consolidated [HomeUiState] for atomic state updates. [uiState] is the
 * only public state surface — both the iOS SKIE wrapper and the Android
 * screen render from it (favorites included); there are no per-field flows
 * or snapshot getters.
 */
@OptIn(FlowPreview::class)
class SharedHomeViewModel internal constructor(
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val loadCocktailsByCategoryUseCase: LoadCocktailsByCategoryUseCase,
    private val sortCocktailsUseCase: SortCocktailsUseCase,
    private val manageFavoritesUseCase: ManageFavoritesUseCase,
    private val offlineRepository: CocktailOfflineRepository,
    private val getCocktailDetailUseCase: GetCocktailDetailUseCase,
    private val catalogRepository: CocktailCatalogRepository,
    private val networkMonitor: NetworkMonitor
) : SharedViewModel() {

    // Consolidated UI State — single source of truth
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val PAGE_SIZE = 10
    private val SEARCH_DEBOUNCE_MS = 300L

    // Debounced search pipeline (SH-13): platforms push raw query text via
    // [updateSearchQuery]; a null value cancels any pending search (used by
    // the clear paths). StateFlow's value-equality conflation provides
    // distinctUntilChanged for free, and collectLatest cancels a superseded
    // in-flight search so stale results can never win the race.
    private val searchQueryFlow = MutableStateFlow<String?>(null)

    // True when the currently shown list was produced while the network was
    // down (cache fallback). Lets the restore handler refresh content that a
    // normal reconnect would otherwise leave stale.
    private var lastLoadWasOffline = false

    init {
        initialize()
    }
    
    private fun initialize() {
        // Observe network connectivity. Monitoring itself is started once at
        // app init (initKoin / Application.onCreate), not per-ViewModel (SH-2).
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                _uiState.update { it.copy(isNetworkAvailable = isOnline) }

                // Auto-retry on network restoration: recover from an errored
                // empty screen, and refresh content that was served from the
                // offline cache fallback.
                if (isOnline && ((error.value != null && _uiState.value.cocktails.isEmpty()) || lastLoadWasOffline)) {
                    retry()
                }
            }
        }

        // Debounced search (SH-13): one request per typing pause, not per keystroke.
        viewModelScope.launch {
            searchQueryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .collectLatest { query ->
                    if (query != null) searchCocktails(query)
                }
        }

        // Reactive favorites (SH-4): mutations from any screen publish
        // through the repository flow; no manual re-pulls after toggles.
        viewModelScope.launch {
            manageFavoritesUseCase.observeFavorites().collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
            }
        }

        // Initial load. No arbitrary delay: the cache suspends inside its own
        // ensureLoaded() when first touched (SH-7).
        viewModelScope.launch {
            if (_uiState.value.cocktails.isEmpty()) {
                loadCocktailsByCategory(CocktailCategories.DEFAULT)
            }
        }
    }

    /**
     * Push a search-query text change from the platform search field. The
     * search itself runs through the debounced pipeline, so fast typing fires
     * one request per pause instead of one per keystroke, and superseded
     * searches are cancelled. An empty query falls back to the category
     * listing (clearing stale results).
     * Platforms call this per keystroke; [searchCocktails] stays available
     * for explicit submit/retry, which must not be debounced.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearchActive = query.isNotBlank()) }
        searchQueryFlow.value = query
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

        // Offline-ness of this load is decided when the fetch STARTS — the
        // cache fallback serves a snapshot from that moment. Sample the
        // monitor, not uiState: the collector that mirrors it into uiState
        // may lag behind the actual network state.
        val startedOffline = !networkMonitor.isOnline.value

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
            settleOfflineLoad(startedOffline)
        } catch (e: Exception) {
            val isOffline = !_uiState.value.isNetworkAvailable
            if (!isOffline) {
                handleException(
                    e,
                    MR.strings.error_load_cocktails_failed,
                    recoveryAction = ErrorHandler.RecoveryAction(MR.strings.common_retry.desc()) {
                        viewModelScope.launch { loadCocktailsByCategory(category) }
                    }
                )
            }
            // Try cached data before showing error
            tryLoadCachedData(targetCategory, e, startedOffline)
        }
    }

    /**
     * Bookkeeping after content was published by a load that may have been
     * served from the offline cache fallback. If connectivity came back while
     * that load was still in flight, the restore handler found no flag to act
     * on — refresh immediately instead of leaving the stale snapshot visible.
     */
    private fun settleOfflineLoad(startedOffline: Boolean) {
        if (startedOffline && networkMonitor.isOnline.value) {
            lastLoadWasOffline = false
            retry()
        } else {
            lastLoadWasOffline = startedOffline
        }
    }
    
    /**
     * Search cocktails by name, composed with any active advanced-search filters.
     * SKIE will convert this to Swift async function.
     */
    suspend fun searchCocktails(query: String) {
        val filters = _uiState.value.searchFilters.copy(query = query)
        _uiState.update { it.copy(searchQuery = query, isSearchActive = query.isNotBlank()) }

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
        // Pagination pages the category listing. While a search or filters
        // are active the list holds complete search results — there is no
        // next page, and paging would splice category items into them.
        if (state.searchQuery.isNotBlank() || state.searchFilters.hasActiveFilters()) return
        // Offline: the next page can't be fetched — the list simply ends at
        // the cached data. hasMoreData stays true so pagination resumes once
        // connectivity returns.
        if (!state.isNetworkAvailable) return

        _uiState.update { it.copy(isLoadingMore = true) }

        try {
            val nextPage = state.currentPage + 1
            val category = state.selectedCategory ?: CocktailCategories.DEFAULT
            val newItems = loadCocktailsByCategoryUseCase
                .loadMore(category, state.currentPage, PAGE_SIZE)
                .getOrThrow()

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
            clearError()
        } catch (e: Exception) {
            // hasMoreData stays true: a failed page fetch must surface as a
            // retryable error, never masquerade as the end of the list.
            handleException(
                e,
                MR.strings.error_load_more_cocktails_failed,
                recoveryAction = ErrorHandler.RecoveryAction(MR.strings.common_retry.desc()) {
                    viewModelScope.launch { loadMoreCocktails() }
                }
            )
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
            // Search results are a complete, unpaginated set: clear the flag
            // left over from category pagination so clients don't offer
            // "load more" on top of search results.
            _uiState.update { it.copy(
                cocktails = filtered,
                isLoading = false,
                hasMoreData = false,
                currentPage = 1
            ) }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_apply_filters_failed)
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
     * Toggle favorite status for a cocktail. State updates arrive via the
     * observed favorites flow (SH-4).
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleFavorite(cocktail: Cocktail) {
        try {
            manageFavoritesUseCase.toggle(cocktail)
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_favorites_failed)
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
            handleException(e, MR.strings.error_sort_cocktails_failed)
        }
    }

    /**
     * Sort cocktails by rating.
     */
    suspend fun sortByRating() {
        try {
            _uiState.update { it.copy(cocktails = sortCocktailsUseCase(it.cocktails, SortCocktailsUseCase.SortType.RATING)) }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_sort_cocktails_failed)
        }
    }

    /**
     * Sort cocktails by popularity.
     */
    suspend fun sortByPopularity() {
        try {
            _uiState.update { it.copy(cocktails = sortCocktailsUseCase(it.cocktails, SortCocktailsUseCase.SortType.POPULARITY)) }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_sort_cocktails_failed)
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
            handleException(e, MR.strings.error_load_cocktail_details_failed)
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
            handleException(e, MR.strings.error_refresh_cocktail_details_failed)
        }
    }
    
    /** Canonical curated category list — single source of truth for category chips on both platforms. */
    val curatedCategories: List<String>
        get() = CocktailCategories.CURATED

    /** Clear only the query text; any active advanced-search filters are kept. */
    fun clearSearch() {
        searchQueryFlow.value = null // cancel any pending debounced search
        _uiState.update { it.copy(isSearchActive = false) }
        viewModelScope.launch { searchCocktails("") }
    }

    fun toggleSearchMode(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active) }
        if (!active) {
            searchQueryFlow.value = null // cancel any pending debounced search
            _uiState.update { it.copy(searchQuery = "", searchFilters = SearchFilters()) }
            viewModelScope.launch { loadCocktails() }
        }
    }

    fun toggleAdvancedSearchMode(active: Boolean) {
        _uiState.update { it.copy(isAdvancedSearchActive = active) }
    }

    /** Reset all advanced-search filters (and the query) and reload the default list. */
    fun clearSearchFilters() {
        searchQueryFlow.value = null // cancel any pending debounced search
        _uiState.update { it.copy(searchQuery = "", searchFilters = SearchFilters()) }
        viewModelScope.launch { loadCocktails() }
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

    private suspend fun tryLoadCachedData(category: String, originalError: Exception, startedOffline: Boolean) {
        try {
            val cachedCocktails = offlineRepository.getRecentlyViewedCocktails().getOrDefault(emptyList())
                .filter { category == CocktailCategories.DEFAULT || it.category == category }

            if (cachedCocktails.isNotEmpty()) {
                _uiState.update { it.copy(cocktails = cachedCocktails, isLoading = false) }
                clearError()
                settleOfflineLoad(startedOffline)
            } else {
                throw originalError
            }
        } catch (e: Exception) {
            handleException(
                originalError,
                MR.strings.error_load_cocktails_failed,
                recoveryAction = ErrorHandler.RecoveryAction(MR.strings.common_retry.desc()) { retry() }
            )
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}