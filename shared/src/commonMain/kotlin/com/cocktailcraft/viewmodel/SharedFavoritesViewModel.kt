package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
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

    private val repository: CocktailRepository by inject()

    // UI State - SKIE will convert these to Swift AsyncSequence
    private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())
    val favorites: StateFlow<List<Cocktail>> = _favorites.asStateFlow()
    
    private val _favoriteCount = MutableStateFlow(0)
    val favoriteCount: StateFlow<Int> = _favoriteCount.asStateFlow()

    // Computed properties
    val isEmpty: Boolean
        get() = _favorites.value.isEmpty()
    
    val hasItems: Boolean
        get() = _favorites.value.isNotEmpty()

    init {
        viewModelScope.launch {
            loadFavorites()
        }
    }

    /**
     * Load favorites from repository.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadFavorites() {
        setLoading(true)
        try {
            repository.getFavoriteCocktails()
                .catch { /* Ignore errors */ }
                .collect { favoriteCocktails ->
                    _favorites.value = favoriteCocktails
                    _favoriteCount.value = favoriteCocktails.size
                    setLoading(false)
                }
        } catch (e: Exception) {
            setLoading(false)
        }
    }

    /**
     * Toggle favorite status for a cocktail.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleFavorite(cocktail: Cocktail) {
        try {
            if (isFavorite(cocktail.id)) {
                repository.removeFromFavorites(cocktail)
            } else {
                repository.addToFavorites(cocktail)
            }
            loadFavorites()
        } catch (e: Exception) {
            handleException(e, "Failed to update favorites", showAsEvent = true)
        }
    }

    /**
     * Clear all favorites.
     * SKIE will convert this to Swift async function.
     */
    suspend fun clearAllFavorites() {
        try {
            // Remove all favorites one by one since clearFavorites doesn't exist
            val currentFavorites = _favorites.value
            for (cocktail in currentFavorites) {
                repository.removeFromFavorites(cocktail)
            }
            _favorites.value = emptyList()
            _favoriteCount.value = 0
        } catch (e: Exception) {
            handleException(e, "Failed to clear favorites", showAsEvent = true)
        }
    }

    // MARK: - Synchronous Helper Methods

    /**
     * Check if a cocktail is favorite.
     */
    fun isFavorite(cocktailId: String): Boolean {
        return _favorites.value.any { it.id == cocktailId }
    }
    
    /**
     * Get favorites by category.
     */
    fun getFavoritesByCategory(category: String): List<Cocktail> {
        return _favorites.value.filter { it.category == category }
    }
    
    /**
     * Get all favorite categories.
     */
    fun getFavoriteCategories(): List<String> {
        return _favorites.value
            .mapNotNull { it.category }
            .distinct()
            .sorted()
    }
    
    /**
     * Search favorites by name.
     */
    fun searchFavorites(query: String): List<Cocktail> {
        if (query.isBlank()) return _favorites.value
        
        return _favorites.value.filter { cocktail ->
            cocktail.name.contains(query, ignoreCase = true) ||
            cocktail.ingredients.any { it.name.contains(query, ignoreCase = true) }
        }
    }
    
    /**
     * Get favorite cocktails sorted by name.
     */
    fun getFavoritesSortedByName(): List<Cocktail> {
        return _favorites.value.sortedBy { it.name }
    }
    
    /**
     * Get favorite cocktails sorted by date added.
     */
    fun getFavoritesSortedByDate(): List<Cocktail> {
        return _favorites.value.sortedByDescending { it.dateAdded }
    }
    
    /**
     * Get favorite cocktails sorted by rating.
     */
    fun getFavoritesSortedByRating(): List<Cocktail> {
        return _favorites.value.sortedByDescending { it.rating }
    }
    
    /**
     * Refresh favorites data.
     */
    fun refresh() {
        viewModelScope.launch {
            loadFavorites()
        }
    }
}
