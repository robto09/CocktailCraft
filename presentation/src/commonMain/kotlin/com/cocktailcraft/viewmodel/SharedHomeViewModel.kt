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
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
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
import com.cocktailcraft.resources.error_update_favorites_failed
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.util.runCatchingNonCancellation
import com.cocktailcraft.viewmodel.state.CocktailRatingSummary
import com.cocktailcraft.viewmodel.state.HomeSortOption
import com.cocktailcraft.viewmodel.state.HomeUiState
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
// Home aggregates the catalog, search, favorites, reviews, recently viewed
// and connectivity; each collaborator is a single-purpose use case/repository
@Suppress("LongParameterList")
class SharedHomeViewModel internal constructor(
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val loadCocktailsByCategoryUseCase: LoadCocktailsByCategoryUseCase,
    private val sortCocktailsUseCase: SortCocktailsUseCase,
    private val manageFavoritesUseCase: ManageFavoritesUseCase,
    private val offlineRepository: CocktailOfflineRepository,
    private val getCocktailDetailUseCase: GetCocktailDetailUseCase,
    private val catalogRepository: CocktailCatalogRepository,
    private val networkMonitor: NetworkMonitor,
    private val manageReviewsUseCase: ManageReviewsUseCase
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

    // Observes the selected category's stored page while the category
    // listing is on screen (not search results), so a refresh — or a
    // detail lookup upgrading a placeholder — reaches the list through the
    // database flow. Restarted per load; stopped while search/filters show.
    private var categoryJob: Job? = null

    // The listing on screen in its source order — the stored category page
    // (plus loaded pages), search results as returned, or the offline
    // fallback — before [HomeUiState.sortOption] is applied. Every listing
    // change goes through [publishListing], so the selected sort holds
    // across category changes, searches and database re-emissions, and
    // DEFAULT can restore the source order.
    private var listingSource: List<Cocktail> = emptyList()

    private var recentlyViewedJob: Job? = null

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

        // Review averages, observed like favorites. They rank the Rating
        // sort and show on the cards; a failing flow just leaves them empty.
        viewModelScope.launch {
            manageReviewsUseCase.observeReviews()
                .catch { /* non-fatal: no review averages */ }
                .collect { reviews ->
                    val summaries = reviews.groupBy { it.cocktailId }.mapValues { (_, cocktailReviews) ->
                        CocktailRatingSummary(
                            rating = manageReviewsUseCase.computeAverageRating(cocktailReviews),
                            count = cocktailReviews.size
                        )
                    }
                    _uiState.update {
                        it.copy(
                            ratingSummaries = summaries,
                            cocktails = sortListing(listingSource, it.sortOption, summaries)
                        )
                    }
                }
        }

        refreshRecentlyViewed()

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
        loadCategory(category, forceRefresh = false)
    }

    /**
     * Shows the stored page of [category] right away and refreshes it from
     * the network: when stale, or always with [forceRefresh] (retry, network
     * restore). A failed refresh keeps the stored page; the error only
     * surfaces when there is nothing to show.
     */
    private suspend fun loadCategory(category: String?, forceRefresh: Boolean) {
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
        observeCategory(targetCategory)

        try {
            val cocktailList = loadCocktailsByCategoryUseCase(targetCategory, forceRefresh).getOrThrow()
            val paginatedList = cocktailList.take(PAGE_SIZE)
            publishListing(paginatedList) {
                it.copy(
                    hasMoreData = paginatedList.size < cocktailList.size,
                    isLoading = false
                )
            }
            clearError()
            settleOfflineLoad(startedOffline)
        } catch (e: Exception) {
            val isOffline = !_uiState.value.isNetworkAvailable
            if (!isOffline) {
                handleException(
                    e,
                    MR.strings.error_load_cocktails_failed,
                    recoveryAction = ErrorHandler.RecoveryAction(MR.strings.common_retry.desc()) {
                        viewModelScope.launch { loadCategory(category, forceRefresh = true) }
                    }
                )
            }
            // Try cached data before showing error
            tryLoadCachedData(targetCategory, e, startedOffline)
        }
    }

    /**
     * (Re)start observing [category]'s stored page. Emissions update the
     * listing in place: while the page lists the same cocktails they keep
     * their place (with the stored copies swapped in); once a refresh changes
     * which cocktails it lists, the stored order replaces it. Either way the
     * selected sort is applied on top. Empty emissions are skipped — an
     * empty or failed load is published, with its error or offline fallback,
     * by [loadCategory] itself.
     */
    private fun observeCategory(category: String) {
        categoryJob?.cancel()
        categoryJob = viewModelScope.launch {
            loadCocktailsByCategoryUseCase.observe(category).collect { stored ->
                if (stored.isNotEmpty()) publishCategoryPage(stored)
            }
        }
    }

    private fun stopObservingCategory() {
        categoryJob?.cancel()
        categoryJob = null
    }

    private fun publishCategoryPage(stored: List<Cocktail>) {
        val visible = stored.take(_uiState.value.currentPage * PAGE_SIZE)
        val source = if (listingSource.mapTo(HashSet()) { it.id } == visible.mapTo(HashSet()) { it.id }) {
            val latest = visible.associateBy { it.id }
            listingSource.map { latest.getValue(it.id) }
        } else {
            visible
        }
        publishListing(source) { it.copy(hasMoreData = visible.size < stored.size) }
    }

    /**
     * Show [source] as the listing, ordered by the selected sort, together
     * with the other state changes in [transform] — one atomic update.
     */
    private fun publishListing(source: List<Cocktail>, transform: (HomeUiState) -> HomeUiState = { it }) {
        listingSource = source
        _uiState.update { state ->
            val next = transform(state)
            next.copy(cocktails = sortListing(source, next.sortOption, next.ratingSummaries))
        }
    }

    /**
     * [source] in [option]'s order. Rating ranks by the user's review
     * average where there is one, and the demo [Cocktail.rating] otherwise.
     */
    private fun sortListing(
        source: List<Cocktail>,
        option: HomeSortOption,
        ratings: Map<String, CocktailRatingSummary>
    ): List<Cocktail> {
        val sortType = when (option) {
            HomeSortOption.DEFAULT -> return source
            HomeSortOption.NAME_ASC -> SortCocktailsUseCase.SortType.NAME_ASC
            HomeSortOption.PRICE_ASC -> SortCocktailsUseCase.SortType.PRICE_ASC
            HomeSortOption.PRICE_DESC -> SortCocktailsUseCase.SortType.PRICE_DESC
            HomeSortOption.RATING -> SortCocktailsUseCase.SortType.RATING
            HomeSortOption.POPULARITY -> SortCocktailsUseCase.SortType.POPULARITY
        }
        return sortCocktailsUseCase(source, sortType) { ratings[it.id]?.rating ?: it.rating }
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

        clearError()
        _uiState.update { it.copy(isLoadingMore = true) }

        try {
            val nextPage = state.currentPage + 1
            val category = state.selectedCategory ?: CocktailCategories.DEFAULT
            val newItems = loadCocktailsByCategoryUseCase
                .loadMore(category, state.currentPage, PAGE_SIZE)
                .getOrThrow()

            if (newItems.isNotEmpty()) {
                // The new page joins the listing and the whole of it is
                // re-sorted, so a sorted list stays in order as it grows
                publishListing(listingSource + newItems) { cur ->
                    cur.copy(
                        currentPage = nextPage,
                        hasMoreData = newItems.size >= PAGE_SIZE,
                        isLoadingMore = false
                    )
                }
            } else {
                _uiState.update { it.copy(hasMoreData = false, isLoadingMore = false) }
            }
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
        // Search results replace the category listing; its stored page must
        // no longer write into the list.
        stopObservingCategory()
        _uiState.update { it.copy(searchFilters = filters, isLoading = true) }
        clearError()

        try {
            val filtered = searchCocktailsUseCase.search(filters).getOrThrow()
            // Search results are a complete, unpaginated set: clear the flag
            // left over from category pagination so clients don't offer
            // "load more" on top of search results.
            publishListing(filtered) {
                it.copy(
                    isLoading = false,
                    hasMoreData = false,
                    currentPage = 1
                )
            }
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
     * observed favorites flow (SH-4); a failed toggle is reported.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleFavorite(cocktail: Cocktail) {
        clearError()
        try {
            manageFavoritesUseCase.toggle(cocktail)
                .getOrReport(false, MR.strings.error_update_favorites_failed)
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_favorites_failed)
        }
    }

    /**
     * Order the listing by [option]. The choice is kept in
     * [HomeUiState.sortOption] and re-applied to every later listing —
     * another category, a search, a page loaded on scroll, or a refresh
     * landing through the database — until changed; [HomeSortOption.DEFAULT]
     * restores the catalog's own order.
     */
    fun setSortOption(option: HomeSortOption) {
        _uiState.update {
            it.copy(
                sortOption = option,
                cocktails = sortListing(listingSource, option, it.ratingSummaries)
            )
        }
    }

    /**
     * Reload the "Recently viewed" row (newest first, at most
     * [RECENTLY_VIEWED_LIMIT]). The store offers no change flow, so
     * platforms call this whenever Home comes back on screen (e.g. after a
     * cocktail's detail page was opened). A failed read keeps the row as is.
     */
    fun refreshRecentlyViewed() {
        recentlyViewedJob?.cancel()
        recentlyViewedJob = viewModelScope.launch {
            val recent = runCatchingNonCancellation(onFailure = { null }) {
                offlineRepository.getRecentlyViewedCocktails().getOrNull()
            } ?: return@launch
            _uiState.update { it.copy(recentlyViewed = recent.distinctBy { c -> c.id }.take(RECENTLY_VIEWED_LIMIT)) }
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
                // An explicit retry (or network restore) refetches even a
                // page that is still fresh.
                loadCategory(state.selectedCategory ?: CocktailCategories.DEFAULT, forceRefresh = true)
            }
        }
    }
    
    // Private helper functions

    private suspend fun tryLoadCachedData(category: String, originalError: Exception, startedOffline: Boolean) {
        try {
            val cachedCocktails = offlineRepository.getRecentlyViewedCocktails().getOrDefault(emptyList())
                .filter { category == CocktailCategories.DEFAULT || it.category == category }

            if (cachedCocktails.isNotEmpty()) {
                publishListing(cachedCocktails) { it.copy(isLoading = false) }
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

    private companion object {
        /** How many cocktails the "Recently viewed" row shows. */
        const val RECENTLY_VIEWED_LIMIT = 10
    }
}
