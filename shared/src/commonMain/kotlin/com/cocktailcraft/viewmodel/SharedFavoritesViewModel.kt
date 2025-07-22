package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.model.Cocktail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for favorites functionality
 * 
 * This ViewModel demonstrates SKIE's capabilities for sharing ViewModels between platforms:
 * - StateFlow automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - Type safety preserved across platforms
 * - Lifecycle managed through SharedViewModel base class
 */
class SharedFavoritesViewModel : SharedViewModel() {
    
    private val repository: CocktailRepository by inject()
    
    // State flows that will be automatically converted to Swift AsyncSequence by SKIE
    private val _favoriteCocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val favoriteCocktails: StateFlow<List<Cocktail>> = _favoriteCocktails.asStateFlow()
    
    private val _favoriteCount = MutableStateFlow(0)
    val favoriteCount: StateFlow<Int> = _favoriteCount.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    /**
     * Load favorite cocktails - will be converted to Swift async function by SKIE
     */
    fun loadFavorites() {
        viewModelScope.launch {
            try {
                setLoading(true)
                repository.getFavoriteCocktails()
                    .catch { exception ->
                        handleException(exception, "Failed to load favorite cocktails")
                    }
                    .collect { favorites ->
                        _favoriteCocktails.value = favorites
                        _favoriteCount.value = favorites.size
                    }
            } catch (e: Exception) {
                handleException(e, "Failed to load favorite cocktails")
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Toggle favorite status for a cocktail - SKIE converts this to Swift async function
     */
    suspend fun toggleFavorite(cocktail: Cocktail) {
        try {
            setLoading(true)
            val isFavorite = _favoriteCocktails.value.any { it.id == cocktail.id }
            
            if (isFavorite) {
                repository.removeFromFavorites(cocktail)
            } else {
                repository.addToFavorites(cocktail)
            }
            
            // Reload favorites to get updated list
            loadFavorites()
            
        } catch (e: Exception) {
            handleException(e, "Failed to update favorite status")
        } finally {
            setLoading(false)
        }
    }
    
    /**
     * Check if a cocktail is favorite - simple property access
     */
    fun isFavorite(cocktailId: String): Boolean {
        return _favoriteCocktails.value.any { it.id == cocktailId }
    }
    
    /**
     * Get favorite cocktails count
     */
    fun getFavoriteCount(): Int = _favoriteCount.value
    
    /**
     * Check if has any favorites
     */
    fun hasFavorites(): Boolean = _favoriteCocktails.value.isNotEmpty()
    
    /**
     * Clear all favorites - SKIE converts this to Swift async function
     */
    suspend fun clearAllFavorites() {
        try {
            setLoading(true)
            val currentFavorites = _favoriteCocktails.value
            
            currentFavorites.forEach { cocktail ->
                repository.removeFromFavorites(cocktail)
            }
            
            // Reload to get updated list
            loadFavorites()
            
        } catch (e: Exception) {
            handleException(e, "Failed to clear favorites")
        } finally {
            setLoading(false)
        }
    }
    
    /**
     * Refresh favorites - SKIE converts this to Swift async function
     */
    fun refresh() {
        loadFavorites()
    }
}
