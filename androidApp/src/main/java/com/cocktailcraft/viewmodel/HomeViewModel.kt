package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel(
    private val cocktailRepository: CocktailRepository? = null
) : ViewModel(), KoinComponent {
    
    // Use injected repository if not provided in constructor (for production)
    private val injectedCocktailRepository: CocktailRepository by inject()
    
    // Use the provided repository or the injected one
    private val repository: CocktailRepository
        get() = cocktailRepository ?: injectedCocktailRepository
    
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
    
    // Add a shared flow for connectivity status changes
    private val _connectivityStatus = MutableSharedFlow<Boolean>(replay = 1)
    
    // Keep track of search job for debouncing
    private var searchJob: Job? = null
    
    // Network error message with retry button
    private val networkErrorMessage = "Unable to connect to the cocktail database. Please check your internet connection and try again."
    
    init {
        loadCocktails()
        loadFavorites()
        monitorConnectivity()
    }
    
    private fun monitorConnectivity() {
        viewModelScope.launch {
            _connectivityStatus.emit(checkConnectivity())
            
            // Periodically check connectivity when there's an error
            while (true) {
                delay(30000) // Check every 30 seconds
                if (_error.value.isNotBlank()) {
                    val isConnected = checkConnectivity()
                    _connectivityStatus.emit(isConnected)
                    
                    // If connection restored and we had an error, reload data
                    if (isConnected && _error.value == networkErrorMessage) {
                        _error.value = ""
                        loadCocktails()
                    }
                }
            }
        }
    }
    
    private suspend fun checkConnectivity(): Boolean {
        return try {
            repository.checkApiConnectivity().first()
        } catch (e: Exception) {
            false
        }
    }
    
    fun loadCocktails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""
            
            try {
                // First check API connectivity
                if (!checkConnectivity()) {
                    _error.value = networkErrorMessage
                    _isLoading.value = false
                    return@launch
                }
                
                repository.getCocktailsSortedByNewest()
                    .catch { e -> 
                        handleError("Failed to load cocktails", e)
                    }
                    .collect { cocktailList ->
                        _cocktails.value = cocktailList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                handleError("Failed to load cocktails", e)
            }
        }
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                repository.getFavoriteCocktails()
                    .catch { e ->
                        // Don't show error UI for favorites loading, just log
                        println("Failed to load favorites: ${e.message}")
                    }
                    .collect { cocktailList ->
                        _favorites.value = cocktailList
                    }
            } catch (e: Exception) {
                println("Failed to load favorites: ${e.message}")
            }
        }
    }
    
    // Updated function with debouncing
    fun searchCocktails(query: String) {
        _searchQuery.value = query
        
        // Automatically activate search mode when query is not empty
        if (query.isNotEmpty() && !_isSearchActive.value) {
            _isSearchActive.value = true
        }
        
        // Cancel previous search job if it exists
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _isSearchActive.value = false
            loadCocktails() // Reset to all cocktails if query is empty
            return
        }
        
        // Create a new search job with debounce
        searchJob = viewModelScope.launch {
            delay(300) // Debounce for 300ms
            _isLoading.value = true
            _error.value = ""
            
            try {
                // Check connectivity first
                if (!checkConnectivity()) {
                    _error.value = networkErrorMessage
                    _isLoading.value = false
                    return@launch
                }
                
                repository.searchCocktailsByName(query)
                    .catch { e ->
                        handleError("Failed to search cocktails", e)
                    }
                    .collect { cocktailList ->
                        _cocktails.value = cocktailList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                handleError("Failed to search cocktails", e)
            }
        }
    }
    
    // Helper function to handle errors consistently
    private fun handleError(baseMessage: String, e: Throwable) {
        println("$baseMessage: ${e.message}")
        
        val userMessage = when {
            e.message?.contains("timeout") == true -> 
                "$baseMessage: The request timed out. Please try again."
            e.message?.contains("connection") == true || 
            e.message?.contains("network") == true -> 
                networkErrorMessage
            else -> "$baseMessage: ${e.message}"
        }
        
        _error.value = userMessage
        _isLoading.value = false
    }
    
    // Toggle search mode
    fun toggleSearchMode(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            loadCocktails() // Reset to all cocktails when search is deactivated
        }
    }
    
    // Load cocktails filtered by category - add this new method
    fun loadCocktailsByCategory(category: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""
            
            try {
                // Check connectivity first
                if (!checkConnectivity()) {
                    _error.value = networkErrorMessage
                    _isLoading.value = false
                    return@launch
                }
                
                if (category == null) {
                    loadCocktails() // Load all if category is null
                    return@launch
                }
                
                repository.filterByCategory(category)
                    .catch { e ->
                        handleError("Failed to filter cocktails", e)
                    }
                    .collect { cocktailList ->
                        _cocktails.value = cocktailList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                handleError("Failed to filter cocktails", e)
            }
        }
    }
    
    fun addToFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                repository.addToFavorites(cocktail)
                loadFavorites() // Refresh favorites after adding
            } catch (e: Exception) {
                println("Failed to add to favorites: ${e.message}")
                // Don't show UI error for favorite operations
            }
        }
    }
    
    // Add retry functionality to reload data based on current state
    fun retry() {
        if (_isSearchActive.value && _searchQuery.value.isNotEmpty()) {
            // If in search mode, retry the search
            searchCocktails(_searchQuery.value)
        } else if (_isSearchActive.value && _searchQuery.value.isEmpty()) {
            // If search is active but query is empty, reset to normal mode
            toggleSearchMode(false)
        } else {
            // Otherwise retry loading based on selected category
            // This will be handled by the LaunchedEffect in the UI
            loadCocktails()
        }
    }
    
    fun removeFromFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                repository.removeFromFavorites(cocktail)
                loadFavorites() // Refresh favorites after removing
            } catch (e: Exception) {
                println("Failed to remove from favorites: ${e.message}")
                // Don't show UI error for favorite operations
            }
        }
    }
    
    fun isFavorite(cocktailId: String): Boolean {
        return favorites.value.any { it.id == cocktailId }
    }
    
    fun toggleFavorite(cocktail: Cocktail) {
        if (isFavorite(cocktail.id)) {
            removeFromFavorites(cocktail)
        } else {
            addToFavorites(cocktail)
        }
    }
    
    fun sortByPrice(ascending: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val sortedList = if (ascending) {
                    _cocktails.value.sortedBy { it.price }
                } else {
                    _cocktails.value.sortedByDescending { it.price }
                }
                _cocktails.value = sortedList
            } catch (e: Exception) {
                handleError("Failed to sort cocktails", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sortByRating() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val sortedList = _cocktails.value.sortedByDescending { it.rating }
                _cocktails.value = sortedList
            } catch (e: Exception) {
                handleError("Failed to sort cocktails", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sortByPopularity() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val sortedList = _cocktails.value.sortedByDescending { it.popularity }
                _cocktails.value = sortedList
            } catch (e: Exception) {
                handleError("Failed to sort cocktails", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getCategories(): List<String> {
        return _cocktails.value
            .mapNotNull { it.category }
            .distinct()
            .filterNot { it.isBlank() }
            .sorted()
    }
    
    fun clearError() {
        _error.value = ""
    }
    
    // Add method to get cocktail by ID
    fun getCocktailById(id: String): kotlinx.coroutines.flow.Flow<Cocktail?> {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""
        }
        
        return kotlinx.coroutines.flow.flow {
            try {
                repository.getCocktailById(id).collect { cocktail ->
                    emit(cocktail)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                handleError("Failed to load cocktail details", e)
                emit(null)
            }
        }
    }
    
    // Add method to force refresh cocktail details
    fun forceRefreshCocktailDetails(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""
            
            try {
                // Clear any cached data for this cocktail
                _cocktails.value = _cocktails.value.filter { it.id != id }
                
                // Force a new API call
                repository.getCocktailById(id).collect { cocktail ->
                    if (cocktail != null) {
                        // Add the refreshed cocktail to the list
                        _cocktails.value = _cocktails.value + cocktail
                    } else {
                        _error.value = "Could not refresh cocktail details. Please try again."
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                handleError("Failed to refresh cocktail details", e)
                _isLoading.value = false
            }
        }
    }
}