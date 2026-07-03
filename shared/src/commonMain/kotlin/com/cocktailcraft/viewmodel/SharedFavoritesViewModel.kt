package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.viewmodel.state.FavoritesUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Favorites functionality.
 * Designed for full SKIE interoperability with iOS.
 *
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 */
class SharedFavoritesViewModel : SharedViewModel() {

    private val manageFavoritesUseCase: ManageFavoritesUseCase by inject()

    // Consolidated UI State
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    // Derived StateFlows for backward compatibility
    val favorites: StateFlow<List<Cocktail>> = _uiState
        .map { it.favorites }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val favoriteCount: StateFlow<Int> = _uiState
        .map { it.favoriteCount }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val isEmpty: Boolean
        get() = _uiState.value.favorites.isEmpty()
    val hasItems: Boolean
        get() = _uiState.value.favorites.isNotEmpty()

    init {
        viewModelScope.launch { loadFavorites() }
    }

    suspend fun loadFavorites() {
        _uiState.update { it.copy(isLoading = true) }
        setLoading(true)
        try {
            val favs = manageFavoritesUseCase.loadFavorites().getOrDefault(emptyList())
            _uiState.update { it.copy(favorites = favs, favoriteCount = favs.size, isLoading = false) }
            setLoading(false)
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false) }
            setLoading(false)
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
     * Clear all favorites.
     * SKIE will convert this to Swift async function.
     */
    suspend fun clearAllFavorites() {
        try {
            // Remove all favorites one by one since clearFavorites doesn't exist
            val currentFavorites = _uiState.value.favorites
            for (cocktail in currentFavorites) {
                manageFavoritesUseCase.removeFromFavorites(cocktail)
            }
            _uiState.update { it.copy(favorites = emptyList(), favoriteCount = 0) }
        } catch (e: Exception) {
            handleException(e, "Failed to clear favorites")
        }
    }

    // MARK: - Synchronous Helper Methods

    fun isFavorite(cocktailId: String): Boolean =
        _uiState.value.favorites.any { it.id == cocktailId }

    fun getFavoritesByCategory(category: String): List<Cocktail> =
        _uiState.value.favorites.filter { it.category == category }

    fun getFavoriteCategories(): List<String> =
        _uiState.value.favorites.mapNotNull { it.category }.distinct().sorted()

    fun searchFavorites(query: String): List<Cocktail> {
        val favs = _uiState.value.favorites
        if (query.isBlank()) return favs
        return favs.filter { cocktail ->
            cocktail.name.contains(query, ignoreCase = true) ||
            cocktail.ingredients.any { it.name.contains(query, ignoreCase = true) }
        }
    }

    fun getFavoritesSortedByName(): List<Cocktail> =
        _uiState.value.favorites.sortedBy { it.name }

    fun getFavoritesSortedByDate(): List<Cocktail> =
        _uiState.value.favorites.sortedByDescending { it.dateAdded }

    fun getFavoritesSortedByRating(): List<Cocktail> =
        _uiState.value.favorites.sortedByDescending { it.rating }

    fun refresh() {
        viewModelScope.launch { loadFavorites() }
    }
}
