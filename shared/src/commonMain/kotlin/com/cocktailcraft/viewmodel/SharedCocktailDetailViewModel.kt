package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.viewmodel.state.DetailUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Cocktail Detail functionality.
 * Uses consolidated [DetailUiState] for atomic state updates.
 */
class SharedCocktailDetailViewModel : SharedViewModel() {

    private val getCocktailDetailUseCase: GetCocktailDetailUseCase by inject()
    private val manageFavoritesUseCase: ManageFavoritesUseCase by inject()
    private val manageCartUseCase: ManageCartUseCase by inject()

    // Consolidated UI State
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Derived StateFlows for backward compatibility
    val cocktail: StateFlow<Cocktail?> = _uiState
        .map { it.cocktail }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val isFavorite: StateFlow<Boolean> = _uiState
        .map { it.isFavorite }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isInCart: StateFlow<Boolean> = _uiState
        .map { it.isInCart }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val cartQuantity: StateFlow<Int> = _uiState
        .map { it.cartQuantity }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val relatedCocktails: StateFlow<List<Cocktail>> = _uiState
        .map { it.relatedCocktails }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val ingredientsByType: StateFlow<Map<String, List<String>>> = _uiState
        .map { it.ingredientsByType }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /**
     * Load cocktail details by ID.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktail(cocktailId: String) {
        _uiState.update { it.copy(isLoading = true) }
        setLoading(true)
        clearError()

        try {
            val loadedCocktail = getCocktailDetailUseCase(cocktailId).getOrNull()
            _uiState.update { it.copy(cocktail = loadedCocktail) }
            if (loadedCocktail != null) {
                viewModelScope.launch {
                    try {
                        val fav = getCocktailDetailUseCase.isFavorite(cocktailId)
                        _uiState.update { it.copy(isFavorite = fav) }
                    } catch (e: Exception) { /* Silent fail */ }
                }
                updateCartStatus(cocktailId)
                loadRelatedCocktails(loadedCocktail)
                processIngredients(loadedCocktail)
            }
            _uiState.update { it.copy(isLoading = false) }
            setLoading(false)
        } catch (e: Exception) {
            handleException(e, "Failed to load cocktail details")
            _uiState.update { it.copy(isLoading = false) }
            setLoading(false)
        }
    }

    /**
     * Toggle favorite status.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleFavorite() {
        val currentCocktail = _uiState.value.cocktail ?: return
        try {
            manageFavoritesUseCase.toggle(currentCocktail)
            _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        } catch (e: Exception) {
            handleException(e, "Failed to update favorites")
        }
    }

    /**
     * Add to cart or update quantity.
     * SKIE will convert this to Swift async function.
     */
    suspend fun addToCart(quantity: Int = 1) {
        val currentCocktail = _uiState.value.cocktail ?: return
        try {
            manageCartUseCase.addToCart(currentCocktail, quantity)
            updateCartStatus(currentCocktail.id)
        } catch (e: Exception) {
            handleException(e, "Failed to add to cart")
        }
    }

    suspend fun updateCartQuantity(quantity: Int) {
        val currentCocktail = _uiState.value.cocktail ?: return
        try {
            if (quantity > 0) {
                manageCartUseCase.updateQuantity(currentCocktail.id, quantity)
            } else {
                manageCartUseCase.removeFromCart(currentCocktail.id)
            }
            updateCartStatus(currentCocktail.id)
        } catch (e: Exception) {
            handleException(e, "Failed to update cart quantity")
        }
    }

    suspend fun removeFromCart() {
        val currentCocktail = _uiState.value.cocktail ?: return
        try {
            manageCartUseCase.removeFromCart(currentCocktail.id)
            updateCartStatus(currentCocktail.id)
        } catch (e: Exception) {
            handleException(e, "Failed to remove from cart")
        }
    }
    
    // MARK: - Private Helper Methods
    
    private suspend fun updateCartStatus(cocktailId: String) {
        try {
            val cartItems = manageCartUseCase.getCartItems().getOrDefault(emptyList())
            val cartItem = cartItems.find { it.cocktail.id == cocktailId }
            _uiState.update { it.copy(isInCart = cartItem != null, cartQuantity = cartItem?.quantity ?: 0) }
        } catch (e: Exception) {
            // Silent fail for cart status
        }
    }

    private suspend fun loadRelatedCocktails(cocktail: Cocktail) {
        try {
            val related = getCocktailDetailUseCase.getRelatedCocktails(cocktail)
            _uiState.update { it.copy(relatedCocktails = related) }
        } catch (e: Exception) {
            // Silent fail for related cocktails
        }
    }

    private fun processIngredients(cocktail: Cocktail) {
        val grouped = mutableMapOf<String, MutableList<String>>()
        cocktail.ingredients.forEach { ingredient ->
            val type = categorizeIngredient(ingredient.name)
            grouped.getOrPut(type) { mutableListOf() }.add(ingredient.name)
        }
        _uiState.update { it.copy(ingredientsByType = grouped.mapValues { entry -> entry.value.toList() }) }
    }
    
    private fun categorizeIngredient(ingredientName: String): String {
        val name = ingredientName.lowercase()
        return when {
            name.contains("rum") || name.contains("vodka") || name.contains("gin") ||
            name.contains("whiskey") || name.contains("tequila") || name.contains("bourbon") -> "Spirits"
            name.contains("juice") || name.contains("lime") || name.contains("lemon") ||
            name.contains("orange") -> "Citrus & Juices"
            name.contains("syrup") || name.contains("sugar") || name.contains("honey") -> "Sweeteners"
            name.contains("bitters") || name.contains("salt") || name.contains("pepper") -> "Seasonings"
            else -> "Other"
        }
    }
    
    // MARK: - Synchronous Helper Methods
    
    /**
     * Get shareable text representation of the cocktail.
     */
    fun getShareableText(): String {
        val cocktail = _uiState.value.cocktail ?: return ""
        
        val ingredients = cocktail.ingredients.joinToString("\n") {
            "• ${it.measure} ${it.name}".trim()
        }
        
        return """
            🍹 ${cocktail.name}
            
            Ingredients:
            $ingredients
            
            Instructions:
            ${cocktail.instructions}
            
            Glass: ${cocktail.glass}
            Category: ${cocktail.category}
        """.trimIndent()
    }
    
    /**
     * Get nutrition facts (example calculation).
     */
    fun getNutritionFacts(): Map<String, String> {
        val cocktail = _uiState.value.cocktail ?: return emptyMap()
        
        // Simple estimation based on ingredients
        var calories = 0
        var alcohol = 0.0
        
        cocktail.ingredients.forEach { ingredient ->
            val name = ingredient.name.lowercase()
            when {
                name.contains("rum") || name.contains("vodka") || name.contains("gin") ||
                name.contains("whiskey") || name.contains("tequila") -> {
                    calories += 65 // ~65 calories per oz of spirits
                    alcohol += 14.0 // ~14g alcohol per oz
                }
                name.contains("juice") -> calories += 15 // ~15 calories per oz of juice
                name.contains("syrup") || name.contains("sugar") -> calories += 25 // ~25 calories per oz
            }
        }
        
        return mapOf(
            "Calories" to "${calories} kcal",
            "Alcohol" to "${alcohol.toInt()}g",
            "Carbs" to "${(calories * 0.1).toInt()}g",
            "Sugar" to "${(calories * 0.15).toInt()}g"
        )
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
}