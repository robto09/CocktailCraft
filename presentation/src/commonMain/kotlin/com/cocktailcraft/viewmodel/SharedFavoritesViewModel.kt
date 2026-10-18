package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_clear_favorites_failed
import com.cocktailcraft.resources.error_load_favorites_failed
import com.cocktailcraft.resources.error_update_favorites_failed
import com.cocktailcraft.viewmodel.state.FavoritesUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Favorites functionality.
 * Designed for full SKIE interoperability with iOS.
 *
 * [uiState] is the only public state surface: platforms render the
 * favorites list from it (and derive id sets for membership checks) rather
 * than calling snapshot getters on the ViewModel.
 *
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 */
class SharedFavoritesViewModel internal constructor(
    private val manageFavoritesUseCase: ManageFavoritesUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        // Reactive favorites (SH-4): the repository publishes after every
        // mutation from any screen, so this collector is the single way
        // favorites reach the UI state — no manual re-pulls.
        viewModelScope.launch {
            manageFavoritesUseCase.observeFavorites().collect { favs ->
                _uiState.update { it.copy(favorites = favs, favoriteCount = favs.size, isLoading = false) }
            }
        }
    }

    /** Manual refresh (pull-to-refresh); routine updates arrive via the observed flow. */
    suspend fun loadFavorites() {
        clearError()
        _uiState.update { it.copy(isLoading = true) }
        try {
            manageFavoritesUseCase.loadFavorites().getOrThrow()
            _uiState.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_load_favorites_failed)
            _uiState.update { it.copy(isLoading = false) }
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
     * Clear all favorites. State updates arrive via the observed flow (SH-4).
     * Every removal is attempted; if any fails, the first failure is reported
     * (the ones that succeeded stay removed).
     * SKIE will convert this to Swift async function.
     */
    suspend fun clearAllFavorites() {
        clearError()
        try {
            // Remove all favorites one by one since clearFavorites doesn't exist
            val currentFavorites = _uiState.value.favorites
            val firstFailure = currentFavorites
                .map { manageFavoritesUseCase.removeFromFavorites(it) }
                .firstOrNull { it is Result.Error }
            firstFailure?.getOrReport(Unit, MR.strings.error_clear_favorites_failed)
        } catch (e: Exception) {
            handleException(e, MR.strings.error_clear_favorites_failed)
        }
    }

    /** Fire-and-forget [loadFavorites] for callers without a coroutine scope. */
    fun refresh() {
        viewModelScope.launch { loadFavorites() }
    }
}
