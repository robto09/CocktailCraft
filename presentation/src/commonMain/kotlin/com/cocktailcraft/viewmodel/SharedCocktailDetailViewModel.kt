package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_load_cocktail_details_failed
import com.cocktailcraft.resources.error_refresh_cocktail_details_failed
import com.cocktailcraft.resources.error_update_favorites_failed
import com.cocktailcraft.resources.favorite_removed
import com.cocktailcraft.util.runCatchingNonCancellation
import com.cocktailcraft.util.runCatchingResult
import com.cocktailcraft.viewmodel.state.DetailUiState
import dev.icerock.moko.resources.format
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Cocktail Detail functionality.
 * Uses consolidated [DetailUiState] for atomic state updates; [uiState] is the
 * only public state surface (the share text lives on it too).
 *
 * Screen-scoped, so its [events] belong to the detail screen: add-to-cart
 * outcomes and the Undo offered after unfavoriting are toasted there.
 */
class SharedCocktailDetailViewModel internal constructor(
    private val getCocktailDetailUseCase: GetCocktailDetailUseCase,
    private val manageFavoritesUseCase: ManageFavoritesUseCase,
    private val manageCartUseCase: ManageCartUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Observes the shown cocktail's stored copy, so a refresh (background or
    // explicit) reaches the screen through the database flow.
    private var observeJob: Job? = null

    /**
     * Load cocktail details by ID: the stored copy right away when it has
     * full details (fetched otherwise), then revalidated in the background
     * when stale. Returns once the cocktail is shown (or the load failed),
     * so the screen can tell "not loaded yet" from "not found".
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun loadCocktail(cocktailId: String) {
        runInViewModelScope { load(cocktailId) }
    }

    private suspend fun load(cocktailId: String) {
        _uiState.update { it.copy(isLoading = true) }
        clearError()
        observeCocktail(cocktailId)

        runCatchingNonCancellation(onFailure = { handleException(it, MR.strings.error_load_cocktail_details_failed) }) {
            val loadedCocktail = getCocktailDetailUseCase(cocktailId).getOrThrow()
            publishCocktail(loadedCocktail)
            if (loadedCocktail != null) {
                viewModelScope.launch {
                    // Silent: the heart just keeps its last known state
                    runCatchingNonCancellation(onFailure = {}) {
                        val fav = getCocktailDetailUseCase.isFavorite(cocktailId)
                        _uiState.update { it.copy(isFavorite = fav) }
                    }
                }
                // Stale-while-revalidate: the stored copy stays on screen;
                // a failure is silent (the explicit refresh reports errors).
                viewModelScope.launch { getCocktailDetailUseCase.refreshIfStale(cocktailId) }
                loadRelatedCocktails(loadedCocktail)
            }
        }
        _uiState.update { it.copy(isLoading = false) }
    }

    /**
     * Toggle favorite status. The flip is optimistic — the heart must react
     * on tap, not after the persist/hydration round trip — and is reconciled
     * against the toggle result (reverting, with a surfaced error, on failure).
     * Unfavoriting is announced on [events] with an Undo action.
     */
    fun toggleFavorite() {
        val currentCocktail = _uiState.value.cocktail ?: return
        clearError()
        val wasFavorite = _uiState.value.isFavorite
        _uiState.update { it.copy(isFavorite = !wasFavorite) }

        viewModelScope.launch {
            val reconciled = runCatchingResult("Failed to toggle favorite") {
                manageFavoritesUseCase.toggle(currentCocktail)
            }.getOrReport(default = wasFavorite, fallbackMessage = MR.strings.error_update_favorites_failed)
            _uiState.update { it.copy(isFavorite = reconciled) }

            if (wasFavorite && !reconciled) {
                sendEvent(
                    UiEvent.Success(
                        message = MR.strings.favorite_removed.format(currentCocktail.name),
                        action = undoAction { refavorite(currentCocktail) }
                    )
                )
            }
        }
    }

    /** Undo for an unfavorite: optimistic like [toggleFavorite], reverted on failure. */
    private suspend fun refavorite(cocktail: Cocktail) {
        clearError()
        val isShown = _uiState.value.cocktail?.id == cocktail.id
        if (isShown) _uiState.update { it.copy(isFavorite = true) }
        val result = runCatchingResult("Failed to add favorite") { manageFavoritesUseCase.addToFavorites(cocktail) }
        result.getOrReport(Unit, MR.strings.error_update_favorites_failed)
        val added = result !is Result.Error
        if (isShown && !added) _uiState.update { it.copy(isFavorite = false) }
    }

    /**
     * Add the loaded cocktail to the cart. Like add-to-cart on Home and
     * Favorites, the outcome is announced on [events] — "Added <name> to
     * cart" only on success, else the failure. A tap while an add is in
     * flight is ignored.
     */
    fun addToCart() {
        val cocktail = _uiState.value.cocktail
        if (cocktail == null || _uiState.value.isAddingToCart) return
        _uiState.update { it.copy(isAddingToCart = true) }
        viewModelScope.launch {
            sendEvent(manageCartUseCase.addToCartOutcome(cocktail, quantity = 1))
            _uiState.update { it.copy(isAddingToCart = false) }
        }
    }

    // MARK: - Private Helper Methods

    private suspend fun loadRelatedCocktails(cocktail: Cocktail) {
        // Silent fail for related cocktails
        runCatchingNonCancellation(onFailure = {}) {
            val related = getCocktailDetailUseCase.getRelatedCocktails(cocktail)
            _uiState.update { it.copy(relatedCocktails = related) }
        }
    }

    private fun publishCocktail(cocktail: Cocktail?) {
        _uiState.update { it.copy(cocktail = cocktail) }
    }

    /**
     * (Re)start observing [cocktailId]'s stored copy. Emissions only replace
     * the cocktail once [loadCocktail] has published that id, so a stored
     * placeholder never flashes up before its full details are fetched.
     */
    private fun observeCocktail(cocktailId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            getCocktailDetailUseCase.observe(cocktailId).filterNotNull().collect { stored ->
                if (_uiState.value.cocktail?.id == cocktailId) publishCocktail(stored)
            }
        }
    }
    
    /**
     * Refresh cocktail details.
     */
    fun refresh() {
        val currentCocktail = _uiState.value.cocktail
        if (currentCocktail != null) {
            viewModelScope.launch { load(currentCocktail.id) }
        }
    }

    /**
     * Force-refresh the current cocktail from the network, bypassing the
     * repository cache, and update the UI state with the fresh copy.
     */
    fun refreshCocktail() {
        val cocktailId = _uiState.value.cocktail?.id ?: return
        clearError()
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            runCatchingNonCancellation(
                onFailure = { handleException(it, MR.strings.error_refresh_cocktail_details_failed) }
            ) {
                val refreshed = getCocktailDetailUseCase.refresh(cocktailId).getOrThrow()
                if (refreshed != null) {
                    publishCocktail(refreshed)
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
