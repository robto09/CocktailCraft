package com.example.breadapp2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffee.store.domain.model.Cocktail
import com.coffee.store.domain.repository.CocktailRepository
import com.coffee.store.domain.usecase.ToggleFavoriteUseCase
import com.coffee.store.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FavoritesViewModel : ViewModel(), KoinComponent {
    private val cocktailRepository: CocktailRepository by inject()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase by inject()
    
    private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())
    val favorites: StateFlow<List<Cocktail>> = _favorites.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                cocktailRepository.getFavoriteCocktails().collect { cocktails ->
                    _favorites.value = cocktails
                }
            } catch (e: Exception) {
                _error.value = "Failed to load favorites: ${e.message}"
            } finally {
                _isLoading.value = false
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
                            _error.value = "Failed to add to favorites: ${result.message}"
                        }
                        is Result.Loading -> {
                            // No action needed
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to add to favorites: ${e.message}"
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
                            _error.value = "Failed to remove from favorites: ${result.message}"
                        }
                        is Result.Loading -> {
                            // No action needed
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to remove from favorites: ${e.message}"
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
                _error.value = "Failed to check favorite status: ${e.message}"
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
                _error.value = "Failed to toggle favorite: ${e.message}"
            }
        }
    }
} 