package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android ViewModel wrapper for SharedHomeViewModel.
 * This demonstrates how to use shared ViewModels on Android while maintaining
 * compatibility with Android's ViewModel architecture.
 * 
 * Benefits:
 * - Single source of truth for business logic
 * - Android-specific lifecycle handling
 * - Seamless integration with Android UI components
 */
class HomeViewModelWrapper : ViewModel(), KoinComponent {
    
    // Get the shared ViewModel from Koin
    private val sharedViewModel: com.cocktailcraft.viewmodel.SharedHomeViewModel by inject()
    
    // Expose StateFlows from shared ViewModel
    // Convert to hot StateFlow for Android lifecycle awareness
    val cocktails: StateFlow<List<Cocktail>> = sharedViewModel.cocktails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val favorites: StateFlow<List<Cocktail>> = sharedViewModel.favorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val isLoading: StateFlow<Boolean> = sharedViewModel.isLoading
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    val error: StateFlow<ErrorHandler.UserFriendlyError?> = sharedViewModel.error
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    val isOfflineMode: StateFlow<Boolean> = sharedViewModel.isOfflineMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    val isNetworkAvailable: StateFlow<Boolean> = sharedViewModel.isNetworkAvailable
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val searchQuery: StateFlow<String> = sharedViewModel.searchQuery
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    
    val isSearchActive: StateFlow<Boolean> = sharedViewModel.isSearchActive
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    val selectedCategory: StateFlow<String?> = sharedViewModel.selectedCategory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    val hasMoreData: StateFlow<Boolean> = sharedViewModel.hasMoreData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val isLoadingMore: StateFlow<Boolean> = sharedViewModel.isLoadingMore
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    // Delegate all actions to shared ViewModel
    
    fun loadCocktails() {
        viewModelScope.launch {
            sharedViewModel.loadCocktails()
        }
    }
    
    fun loadCocktailsByCategory(category: String?) {
        viewModelScope.launch {
            sharedViewModel.loadCocktailsByCategory(category)
        }
    }
    
    fun searchCocktails(query: String) {
        viewModelScope.launch {
            sharedViewModel.searchCocktails(query)
        }
    }
    
    fun loadMoreCocktails() {
        viewModelScope.launch {
            sharedViewModel.loadMoreCocktails()
        }
    }
    
    fun updateSearchFilters(filters: SearchFilters) {
        viewModelScope.launch {
            sharedViewModel.applyFilters(filters.category, filters.ingredient)
        }
    }
    
    fun toggleFavorite(cocktail: Cocktail) {
        viewModelScope.launch {
            sharedViewModel.toggleFavorite(cocktail)
        }
    }
    
    fun sortByPrice(ascending: Boolean) {
        viewModelScope.launch {
            sharedViewModel.sortByPrice(ascending)
        }
    }
    
    fun sortByRating() {
        viewModelScope.launch {
            sharedViewModel.sortByRating()
        }
    }
    
    fun sortByPopularity() {
        viewModelScope.launch {
            sharedViewModel.sortByPopularity()
        }
    }
    
    fun getCocktailById(id: String) {
        viewModelScope.launch {
            sharedViewModel.getCocktailById(id)
        }
    }
    
    // Synchronous methods can be called directly
    
    fun isFavorite(cocktailId: String): Boolean {
        return sharedViewModel.isFavorite(cocktailId)
    }
    
    fun getCategories(): List<String> {
        return sharedViewModel.getCategories()
    }
    
    fun getCocktailsByCategory(category: String, limit: Int = 3): List<Cocktail> {
        return sharedViewModel.getCocktailsByCategory(category, limit)
    }
    
    fun setOfflineMode(enabled: Boolean) {
        sharedViewModel.setOfflineMode(enabled)
    }
    
    fun clearSearch() {
        sharedViewModel.clearSearch()
    }
    
    fun clearError() {
        sharedViewModel.clearError()
    }
    
    fun retry() {
        sharedViewModel.retry()
    }
    
    // Android-specific lifecycle handling
    
    override fun onCleared() {
        super.onCleared()
        // The shared ViewModel will be cleaned up by Koin
        // No need to manually clean it up here
    }
}