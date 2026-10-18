package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_retry
import com.cocktailcraft.resources.error_apply_filters_failed
import com.cocktailcraft.resources.error_load_cocktails_failed
import com.cocktailcraft.resources.error_update_favorites_failed
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.util.runCatchingNonCancellation
import com.cocktailcraft.util.runCatchingResult
import com.cocktailcraft.viewmodel.state.CocktailRatingSummary
import com.cocktailcraft.viewmodel.state.HomeSortOption
import com.cocktailcraft.viewmodel.state.HomeUiState
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
 *
 * Every request that replaces the listing — a category, a search, filters,
 * a retry — runs as the one tracked listing load: starting another cancels
 * it, so the listing always shows the latest request, however the earlier
 * one's network calls finish. The initial load starts here, at init;
 * screens only report what the user selects.
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
    // distinctUntilChanged for free; a superseded search is cancelled as the
    // listing load it started is replaced.
    private val searchQueryFlow = MutableStateFlow<String?>(null)

    // True when the currently shown list was produced while the network was
    // down (cache fallback). Lets the restore handler refresh content that a
    // normal reconnect would otherwise leave stale.
    private var lastLoadWasOffline = false

    // The listing load in flight (see the class notes): a category load or
    // a search. Replaced, and cancelled, by the next one.
    private var listingJob: Job? = null

    // Observes the selected category's stored page while the category
    // listing is on screen (not search results), so a refresh — or a
    // detail lookup upgrading a placeholder — reaches the list through the
    // database flow. Restarted per load; stopped while search/filters show.
    private var categoryJob: Job? = null

    // The selected category's whole stored page, as last loaded or
    // observed. The listing shows its first currentPage * PAGE_SIZE
    // cocktails; paging further just shows more of it.
    private var categoryPage: List<Cocktail> = emptyList()

    // Whether the listing is the selected category's (loaded or loading) —
    // not search results, and not a failed load with nothing to show. A
    // screen re-selecting the same category then has nothing to reload.
    private var showsCategory = false

    // The listing on screen in its source order — the stored category page
    // (up to the current page), search results as returned, or the offline
    // fallback — before [HomeUiState.sortOption] is applied. Every listing
    // change goes through [publishListing], so the selected sort holds
    // across category changes, searches and database re-emissions, and
    // DEFAULT can restore the source order.
    private var listingSource: List<Cocktail> = emptyList()

    private var recentlyViewedJob: Job? = null

    // The filter option lists, once both have loaded
    private var filterOptionsLoaded = false
    private var filterOptionsJob: Job? = null

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
                .collect { query ->
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

        // The one initial load: the default listing ("All"). No arbitrary
        // delay: the cache suspends inside its own ensureLoaded() when first
        // touched (SH-7).
        loadCategory(category = null, forceRefresh = false)
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
     * Show [category]'s cocktails (null = "All", the default list). Platforms
     * call this whenever their category selection is (re)applied — a chip
     * tap, a screen coming back, a restored selection — so re-selecting the
     * category already shown (or loading), or the one under an active
     * search, does nothing: no spinner, no list reset, no refetch.
     */
    fun selectCategory(category: String?) {
        val state = _uiState.value
        val searching = state.searchQuery.isNotBlank() || state.searchFilters.hasActiveFilters()
        if (category == state.selectedCategory && (showsCategory || searching)) return
        loadCategory(category, forceRefresh = false)
    }

    /**
     * Refetch what the listing shows — the search or the category, even a
     * page that is still fresh — and return once it is on screen, so a
     * pull-to-refresh spinner lasts exactly as long.
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun refresh() {
        reload().join()
    }

    /** [refresh] without waiting: the Retry buttons and network restore. */
    fun retry() {
        reload()
    }

    private fun reload(): Job {
        val state = _uiState.value
        return if (state.searchQuery.isNotBlank() || state.searchFilters.hasActiveFilters()) {
            runSearch(state.searchFilters.copy(query = state.searchQuery))
        } else {
            loadCategory(state.selectedCategory, forceRefresh = true)
        }
    }

    /**
     * Start [load] as the listing load, cancelling the one it replaces (and
     * its error reporting) so a slower earlier request can never overwrite
     * a later one.
     */
    private fun launchListing(load: suspend () -> Unit): Job {
        listingJob?.cancel()
        return viewModelScope.launch { load() }.also { listingJob = it }
    }

    /**
     * Shows the stored page of [category] right away and refreshes it from
     * the network: when stale, or always with [forceRefresh] (retry, network
     * restore). A failed refresh keeps the stored page; the error only
     * surfaces when there is nothing to show.
     */
    private fun loadCategory(category: String?, forceRefresh: Boolean): Job {
        val targetCategory = category ?: CocktailCategories.DEFAULT

        // Offline-ness of this load is decided when the fetch STARTS — the
        // cache fallback serves a snapshot from that moment. Sample the
        // monitor, not uiState: the collector that mirrors it into uiState
        // may lag behind the actual network state.
        val startedOffline = !networkMonitor.isOnline.value

        showsCategory = true
        categoryPage = emptyList()
        _uiState.update { it.copy(
            selectedCategory = category,
            isLoading = true,
            currentPage = 1,
            hasMoreData = true
        ) }
        clearError()
        observeCategory(targetCategory)

        return launchListing {
            val result = runCatchingResult("Failed to load category") {
                loadCocktailsByCategoryUseCase(targetCategory, forceRefresh)
            }
            // A load replaced while its request was out never publishes
            currentCoroutineContext().ensureActive()
            when (result) {
                is Result.Success -> {
                    categoryPage = result.data
                    val firstPage = result.data.take(PAGE_SIZE)
                    publishListing(firstPage) {
                        it.copy(hasMoreData = firstPage.size < result.data.size, isLoading = false)
                    }
                    clearError()
                    settleOfflineLoad(startedOffline)
                }
                is Result.Error -> showCachedOrReport(targetCategory, result, startedOffline)
            }
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
                if (stored.isNotEmpty()) {
                    categoryPage = stored
                    publishCategoryPage()
                }
            }
        }
    }

    private fun stopObservingCategory() {
        categoryJob?.cancel()
        categoryJob = null
    }

    /** The first currentPage pages of [categoryPage] as the listing. */
    private fun publishCategoryPage() {
        val visible = categoryPage.take(_uiState.value.currentPage * PAGE_SIZE)
        val source = if (listingSource.mapTo(HashSet()) { it.id } == visible.mapTo(HashSet()) { it.id }) {
            val latest = visible.associateBy { it.id }
            listingSource.map { latest.getValue(it.id) }
        } else {
            visible
        }
        publishListing(source) { it.copy(hasMoreData = visible.size < categoryPage.size) }
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
     * Search cocktails by name, composed with any active advanced-search
     * filters: the explicit submit, never debounced.
     */
    fun searchCocktails(query: String) {
        val filters = _uiState.value.searchFilters.copy(query = query)
        _uiState.update { it.copy(searchQuery = query, isSearchActive = query.isNotBlank()) }

        if (query.isBlank() && !filters.hasActiveFilters()) {
            // Nothing left to search — fall back to the current category listing.
            _uiState.update { it.copy(searchFilters = filters) }
            selectCategory(_uiState.value.selectedCategory)
        } else {
            applyFilters(filters)
        }
    }

    /**
     * Show the next page of the selected category. The whole category is
     * already stored (and observed), so this only shows more of it — no
     * request, offline too. Search results are complete and don't page.
     */
    fun loadMoreCocktails() {
        val state = _uiState.value
        if (!state.hasMoreData || !showsCategory || categoryPage.isEmpty()) return
        // While a search or filters are active the list holds complete
        // search results — paging would splice category items into them.
        if (state.searchQuery.isNotBlank() || state.searchFilters.hasActiveFilters()) return

        _uiState.update { it.copy(currentPage = it.currentPage + 1) }
        publishCategoryPage()
    }

    /**
     * Apply the advanced-search [filters] and refresh the cocktail list.
     * Stores [filters] into state so the active-filter chips reflect them.
     */
    fun applyFilters(filters: SearchFilters) {
        _uiState.update { it.copy(searchFilters = filters) }
        runSearch(filters)
    }

    private fun runSearch(filters: SearchFilters): Job {
        // Search results replace the category listing; its stored page must
        // no longer write into the list.
        stopObservingCategory()
        showsCategory = false
        _uiState.update { it.copy(isLoading = true) }
        clearError()

        return launchListing {
            val result = runCatchingResult("Failed to search") { searchCocktailsUseCase.search(filters) }
            currentCoroutineContext().ensureActive()
            when (result) {
                // Search results are a complete, unpaginated set: clear the
                // flag left over from category pagination so clients don't
                // offer "load more" on top of search results.
                is Result.Success -> publishListing(result.data) {
                    it.copy(isLoading = false, hasMoreData = false, currentPage = 1)
                }
                is Result.Error -> {
                    result.getOrReport(Unit, MR.strings.error_apply_filters_failed)
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    /**
     * Load the option lists (categories / ingredients) that back the
     * advanced-search filter UI on both platforms, via [CocktailCatalogRepository]'s
     * list.php endpoints — once: platforms call this whenever the filter
     * UI opens, and only a failed load is tried again.
     */
    fun loadFilterOptions() {
        if (filterOptionsLoaded || filterOptionsJob?.isActive == true) return
        filterOptionsJob = viewModelScope.launch {
            val categories = runCatchingResult("Failed to load categories") { catalogRepository.getCategories() }
            val ingredients = runCatchingResult("Failed to load ingredients") { catalogRepository.getIngredients() }
            filterOptionsLoaded = categories is Result.Success && ingredients is Result.Success
            // Non-fatal: a failed list keeps the options it would replace
            _uiState.update {
                it.copy(
                    filterCategories = categories.getOrDefault(it.filterCategories),
                    filterIngredients = ingredients.getOrDefault(it.filterIngredients)
                )
            }
        }
    }

    /**
     * Toggle favorite status for a cocktail. State updates arrive via the
     * observed favorites flow (SH-4); a failed toggle is reported.
     */
    fun toggleFavorite(cocktail: Cocktail) {
        clearError()
        viewModelScope.launch {
            runCatchingResult("Failed to toggle favorite") { manageFavoritesUseCase.toggle(cocktail) }
                .getOrReport(false, MR.strings.error_update_favorites_failed)
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

    /** Canonical curated category list — single source of truth for category chips on both platforms. */
    val curatedCategories: List<String>
        get() = CocktailCategories.CURATED

    /** Clear only the query text; any active advanced-search filters are kept. */
    fun clearSearch() {
        searchQueryFlow.value = null // cancel any pending debounced search
        _uiState.update { it.copy(isSearchActive = false) }
        searchCocktails("")
    }

    fun toggleSearchMode(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active) }
        if (!active) {
            searchQueryFlow.value = null // cancel any pending debounced search
            _uiState.update { it.copy(searchQuery = "", searchFilters = SearchFilters()) }
            selectCategory(_uiState.value.selectedCategory)
        }
    }

    /** Open or close the advanced-search sheet; opening loads its option lists. */
    fun toggleAdvancedSearchMode(active: Boolean) {
        _uiState.update { it.copy(isAdvancedSearchActive = active) }
        if (active) loadFilterOptions()
    }

    /** Reset all advanced-search filters (and the query) and reload the default list. */
    fun clearSearchFilters() {
        searchQueryFlow.value = null // cancel any pending debounced search
        _uiState.update { it.copy(searchQuery = "", searchFilters = SearchFilters()) }
        selectCategory(_uiState.value.selectedCategory)
    }

    // Private helper functions

    /**
     * After a failed category load: the offline fallback (recently viewed
     * cocktails of [category]) when there is one, else the error, with Retry.
     */
    private suspend fun showCachedOrReport(category: String, failure: Result.Error, startedOffline: Boolean) {
        val cachedCocktails = runCatchingNonCancellation(onFailure = { emptyList() }) {
            offlineRepository.getRecentlyViewedCocktails().getOrDefault(emptyList())
        }.filter { category == CocktailCategories.DEFAULT || it.category == category }
        currentCoroutineContext().ensureActive()

        if (cachedCocktails.isNotEmpty()) {
            publishListing(cachedCocktails) { it.copy(isLoading = false) }
            clearError()
            settleOfflineLoad(startedOffline)
        } else {
            showsCategory = false
            failure.getOrReport(
                Unit,
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
