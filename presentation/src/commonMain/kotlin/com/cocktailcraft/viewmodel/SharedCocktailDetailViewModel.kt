package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_load_cocktail_details_failed
import com.cocktailcraft.resources.error_refresh_cocktail_details_failed
import com.cocktailcraft.resources.error_update_favorites_failed
import com.cocktailcraft.viewmodel.state.DetailUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Cocktail Detail functionality.
 * Uses consolidated [DetailUiState] for atomic state updates; [uiState] is the
 * only public state surface (the share text lives on it too).
 */
class SharedCocktailDetailViewModel internal constructor(
    private val getCocktailDetailUseCase: GetCocktailDetailUseCase,
    private val manageFavoritesUseCase: ManageFavoritesUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Load cocktail details by ID.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktail(cocktailId: String) {
        _uiState.update { it.copy(isLoading = true) }
        clearError()

        try {
            val loadedCocktail = getCocktailDetailUseCase(cocktailId).getOrThrow()
            publishCocktail(loadedCocktail)
            if (loadedCocktail != null) {
                viewModelScope.launch {
                    try {
                        val fav = getCocktailDetailUseCase.isFavorite(cocktailId)
                        _uiState.update { it.copy(isFavorite = fav) }
                    } catch (e: Exception) { /* Silent fail */ }
                }
                loadRelatedCocktails(loadedCocktail)
            }
            _uiState.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_load_cocktail_details_failed)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Toggle favorite status. The flip is optimistic — the heart must react
     * on tap, not after the persist/hydration round trip — and is reconciled
     * against the toggle result (reverting, with a surfaced error, on failure).
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleFavorite() {
        val currentCocktail = _uiState.value.cocktail ?: return
        clearError()
        val wasFavorite = _uiState.value.isFavorite
        _uiState.update { it.copy(isFavorite = !wasFavorite) }

        val reconciled = manageFavoritesUseCase.toggle(currentCocktail)
            .getOrReport(default = wasFavorite, fallbackMessage = MR.strings.error_update_favorites_failed)
        _uiState.update { it.copy(isFavorite = reconciled) }
    }

    // MARK: - Private Helper Methods

    private suspend fun loadRelatedCocktails(cocktail: Cocktail) {
        try {
            val related = getCocktailDetailUseCase.getRelatedCocktails(cocktail)
            _uiState.update { it.copy(relatedCocktails = related) }
        } catch (e: Exception) {
            // Silent fail for related cocktails
        }
    }

    private fun publishCocktail(cocktail: Cocktail?) {
        _uiState.update { it.copy(cocktail = cocktail) }
    }
    
    /**
     * Refresh cocktail details.
     */
    fun refresh() {
        val currentCocktail = _uiState.value.cocktail
        if (currentCocktail != null) {
            viewModelScope.launch {
                loadCocktail(currentCocktail.id)
            }
        }
    }

    /**
     * Force-refresh the current cocktail from the network, bypassing the
     * repository cache, and update the UI state with the fresh copy.
     * SKIE will convert this to a Swift async function.
     */
    suspend fun refreshCocktail() {
        val cocktailId = _uiState.value.cocktail?.id ?: return
        clearError()
        _uiState.update { it.copy(isLoading = true) }

        try {
            val refreshed = getCocktailDetailUseCase.refresh(cocktailId).getOrThrow()
            if (refreshed != null) {
                publishCocktail(refreshed)
            }
            _uiState.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_refresh_cocktail_details_failed)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}