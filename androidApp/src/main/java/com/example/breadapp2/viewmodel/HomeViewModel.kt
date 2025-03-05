package com.example.breadapp2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffee.store.domain.model.Cocktail
import com.coffee.store.domain.repository.CocktailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel : ViewModel(), KoinComponent {
    private val cocktailRepository: CocktailRepository by inject()
    
    private val _cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val cocktails: StateFlow<List<Cocktail>> = _cocktails.asStateFlow()
    
    private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())
    val favorites: StateFlow<List<Cocktail>> = _favorites.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String>("")
    val error: StateFlow<String> = _error.asStateFlow()
    
    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Search active state
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()
    
    init {
        loadCocktails()
        loadFavorites()
    }
    
    fun loadCocktails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""
            
            try {
                cocktailRepository.getCocktailsSortedByNewest().collect { cocktailList ->
                    _cocktails.value = cocktailList
                }
            } catch (e: Exception) {
                _error.value = "Failed to load cocktails: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                cocktailRepository.getFavoriteCocktails().collect { cocktailList ->
                    _favorites.value = cocktailList
                }
            } catch (e: Exception) {
                _error.value = "Failed to load favorites: ${e.message}"
            }
        }
    }
    
    // New function to search cocktails by name
    fun searchCocktails(query: String) {
        _searchQuery.value = query
        
        // Automatically activate search mode when query is not empty
        if (query.isNotEmpty() && !_isSearchActive.value) {
            _isSearchActive.value = true
        }
        
        if (query.isBlank()) {
            loadCocktails() // Reset to all cocktails if query is empty
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""
            
            try {
                cocktailRepository.searchCocktailsByName(query).collect { cocktailList ->
                    _cocktails.value = cocktailList
                }
            } catch (e: Exception) {
                _error.value = "Failed to search cocktails: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Toggle search mode
    fun toggleSearchMode(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            loadCocktails() // Reset to all cocktails when search is deactivated
        }
    }
    
    fun addToFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                cocktailRepository.addToFavorites(cocktail)
                loadFavorites() // Refresh favorites after adding
            } catch (e: Exception) {
                _error.value = "Failed to add to favorites: ${e.message}"
            }
        }
    }
    
    fun removeFromFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                cocktailRepository.removeFromFavorites(cocktail)
                loadFavorites() // Refresh favorites after removing
            } catch (e: Exception) {
                _error.value = "Failed to remove from favorites: ${e.message}"
            }
        }
    }
    
    fun toggleFavorite(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                val isFav = _favorites.value.any { it.id == cocktail.id }
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
    
    fun getCocktailImageUrl(cocktail: Cocktail): String {
        return cocktailRepository.getCocktailImageUrl(cocktail)
    }
    
    fun getCocktailById(id: String): StateFlow<Cocktail?> {
        val cocktailFlow = MutableStateFlow<Cocktail?>(null)
        
        viewModelScope.launch {
            try {
                cocktailRepository.getCocktailById(id).collect { cocktail ->
                    cocktailFlow.value = cocktail
                }
            } catch (e: Exception) {
                _error.value = "Failed to get cocktail details: ${e.message}"
            }
        }
        
        return cocktailFlow
    }
} 