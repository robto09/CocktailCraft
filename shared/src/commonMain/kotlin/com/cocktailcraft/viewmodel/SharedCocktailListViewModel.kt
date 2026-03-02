package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Proof of Concept: Shared ViewModel for cocktail list functionality
 * 
 * This ViewModel demonstrates SKIE's capabilities for sharing ViewModels between platforms:
 * - StateFlow automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - Type safety preserved across platforms
 * - Lifecycle managed through SharedViewModel base class
 */
class SharedCocktailListViewModel : SharedViewModel() {
    
    private val loadCocktailsByCategoryUseCase: LoadCocktailsByCategoryUseCase by inject()
    private val searchCocktailsUseCase: SearchCocktailsUseCase by inject()
    
    // State flows that will be automatically converted to Swift AsyncSequence by SKIE
    private val _cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val cocktails: StateFlow<List<Cocktail>> = _cocktails.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    init {
        loadInitialCocktails()
    }
    
    /**
     * Load initial cocktails - will be converted to Swift async function by SKIE
     */
    fun loadInitialCocktails() {
        viewModelScope.launch {
            try {
                setLoading(true)
                _cocktails.value = loadCocktailsByCategoryUseCase("Cocktail").getOrDefault(emptyList())
            } catch (e: Exception) {
                handleException(e, "Failed to load cocktails")
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Search cocktails by name - SKIE converts this to Swift async function
     */
    fun searchCocktails(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            loadInitialCocktails()
            return
        }

        viewModelScope.launch {
            try {
                setLoading(true)
                _cocktails.value = searchCocktailsUseCase(query).getOrDefault(emptyList())
            } catch (e: Exception) {
                handleException(e, "Failed to search cocktails")
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Load cocktails by category - SKIE converts this to Swift async function
     */
    fun loadCocktailsByCategory(category: String) {
        _selectedCategory.value = category

        viewModelScope.launch {
            try {
                setLoading(true)
                _cocktails.value = loadCocktailsByCategoryUseCase(category).getOrDefault(emptyList())
            } catch (e: Exception) {
                handleException(e, "Failed to load cocktails for category: $category")
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Refresh cocktails - SKIE converts this to Swift async function
     */
    fun refresh() {
        val currentCategory = _selectedCategory.value
        if (currentCategory != null) {
            loadCocktailsByCategory(currentCategory)
        } else {
            loadInitialCocktails()
        }
    }
    
    /**
     * Clear search and reset to initial state
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        loadInitialCocktails()
    }
    
    /**
     * Get current cocktail count - simple property access
     */
    fun getCocktailCount(): Int = _cocktails.value.size
    
    /**
     * Check if currently searching
     */
    fun isSearching(): Boolean = _searchQuery.value.isNotBlank()
    
    /**
     * Check if category is selected
     */
    fun hasSelectedCategory(): Boolean = _selectedCategory.value != null
}
