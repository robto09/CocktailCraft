package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.usecase.ToggleFavoriteUseCase
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FavoritesViewModel : BaseViewModel() {
    private val cocktailRepository: CocktailRepository by inject()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase by inject()
    
    private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())
    val favorites: StateFlow<List<Cocktail>> = _favorites.asStateFlow()
    
    // Keep _errorString for backward compatibility
    private val _errorString = MutableStateFlow<String?>(null)
    val errorString: StateFlow<String?> = _errorString.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    fun loadFavorites() {
        viewModelScope.launch {
            setLoading(true)
            _errorString.value = null
            clearError()
            
            try {
                cocktailRepository.getFavoriteCocktails().collect { cocktails ->
                    _favorites.value = cocktails
                }
            } catch (e: Exception) {
                _errorString.value = "Failed to load favorites: ${e.message}"
                handleException(e, "Failed to load favorites")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun addToFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(cocktail).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            if (result.data) {
                                // Successfully added to favorites
                                loadFavorites()
                            }
                        }
                        is Result.Error -> {
                            _errorString.value = "Failed to add to favorites: ${result.message}"
                            setError("Failed to add favorite", result.message)
                        }
                        is Result.Loading -> {
                            // No action needed
                        }
                    }
                }
            } catch (e: Exception) {
                _errorString.value = "Failed to add to favorites: ${e.message}"
                handleException(e, "Failed to add to favorites")
            }
        }
    }
    
    fun removeFromFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(cocktail).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            if (!result.data) {
                                // Successfully removed from favorites
                                loadFavorites()
                            }
                        }
                        is Result.Error -> {
                            _errorString.value = "Failed to remove from favorites: ${result.message}"
                            setError("Failed to remove favorite", result.message)
                        }
                        is Result.Loading -> {
                            // No action needed
                        }
                    }
                }
            } catch (e: Exception) {
                _errorString.value = "Failed to remove from favorites: ${e.message}"
                handleException(e, "Failed to remove from favorites")
            }
        }
    }
    
    fun isFavorite(id: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                cocktailRepository.isCocktailFavorite(id).collect { isFavorite ->
                    callback(isFavorite)
                }
            } catch (e: Exception) {
                _errorString.value = "Failed to check favorite status: ${e.message}"
                handleException(e, "Failed to check favorite status")
                callback(false)
            }
        }
    }
    
    fun toggleFavorite(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                val isFav = favorites.value.any { it.id == cocktail.id }
                if (isFav) {
                    removeFromFavorites(cocktail)
                } else {
                    addToFavorites(cocktail)
                }
            } catch (e: Exception) {
                _errorString.value = "Failed to toggle favorite: ${e.message}"
                handleException(e, "Failed to toggle favorite")
            }
        }
    }
} 