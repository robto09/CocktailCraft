package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_load_favorites_failed
import com.cocktailcraft.resources.error_update_favorites_failed
import com.cocktailcraft.resources.favorite_removed
import com.cocktailcraft.viewmodel.state.FavoritesUiState
import dev.icerock.moko.resources.format
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
     * Unfavorite [cocktail] from a list of favorites (the Favorites screen,
     * where every heart is filled), announcing it on [events] with an Undo
     * action that favorites it again. State updates arrive via the observed
     * flow (SH-4); a failed removal is reported and offers no Undo.
     * SKIE will convert this to Swift async function.
     */
    suspend fun removeFavorite(cocktail: Cocktail) {
        clearError()
        try {
            val result = manageFavoritesUseCase.removeFromFavorites(cocktail)
            if (result is Result.Error) {
                result.getOrReport(Unit, MR.strings.error_update_favorites_failed)
                return
            }
            sendEvent(
                UiEvent.Success(
                    message = MR.strings.favorite_removed.format(cocktail.name),
                    action = undoAction { restore(listOf(cocktail)) }
                )
            )
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_favorites_failed)
        }
    }

    /**
     * Undo for [removeFavorite]: favorites [cocktails] again, in order. The
     * repository appends re-added favorites, so the cocktail returns at the
     * end of the list. Stops at, and reports, the first failure.
     */
    private suspend fun restore(cocktails: List<Cocktail>) {
        clearError()
        try {
            for (cocktail in cocktails) {
                val result = manageFavoritesUseCase.addToFavorites(cocktail)
                if (result is Result.Error) {
                    result.getOrReport(Unit, MR.strings.error_update_favorites_failed)
                    return
                }
            }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_favorites_failed)
        }
    }

    /** Fire-and-forget [loadFavorites] for callers without a coroutine scope. */
    fun refresh() {
        viewModelScope.launch { loadFavorites() }
    }
}
