package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Cocktail Detail screen.
 * Designed for full SKIE interoperability with iOS.
 * 
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 */
class SharedCocktailDetailViewModel : SharedViewModel() {
    
    private val cocktailRepository: CocktailRepository by inject()
    private val cartRepository: CartRepository by inject()
    
    // Detail state - SKIE will convert these to Swift AsyncSequence
    private val _cocktail = MutableStateFlow<Cocktail?>(null)
    val cocktail: StateFlow<Cocktail?> = _cocktail.asStateFlow()
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    private val _isInCart = MutableStateFlow(false)
    val isInCart: StateFlow<Boolean> = _isInCart.asStateFlow()
    
    private val _cartQuantity = MutableStateFlow(0)
    val cartQuantity: StateFlow<Int> = _cartQuantity.asStateFlow()
    
    private val _relatedCocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val relatedCocktails: StateFlow<List<Cocktail>> = _relatedCocktails.asStateFlow()
    
    // Ingredients grouped by type
    private val _ingredientsByType = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val ingredientsByType: StateFlow<Map<String, List<String>>> = _ingredientsByType.asStateFlow()
    
    // Currently selected cocktail ID
    private var currentCocktailId: String? = null
    
    /**
     * Load cocktail details by ID.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktail(cocktailId: String) {
        currentCocktailId = cocktailId
        setLoading(true)
        clearError()
        
        try {
            // Load cocktail details
            cocktailRepository.getCocktailById(cocktailId)
                .catch { e ->
                    handleException(
                        e,
                        "Failed to load cocktail details",
                        recoveryAction = ErrorHandler.RecoveryAction("Retry") {
                            viewModelScope.launch { loadCocktail(cocktailId) }
                        }
                    )
                }
                .collect { cocktailData ->
                    _cocktail.value = cocktailData
                    
                    if (cocktailData != null) {
                        // Group ingredients by type
                        groupIngredients(cocktailData)
                        
                        // Check favorite status
                        checkFavoriteStatus(cocktailData)
                        
                        // Check cart status
                        checkCartStatus(cocktailData.id)
                        
                        // Load related cocktails
                        loadRelatedCocktails(cocktailData)
                        
                        // Add to recently viewed (commented out - method may not exist)
                        // addToRecentlyViewed(cocktailData)
                    }
                    
                    setLoading(false)
                }
        } catch (e: Exception) {
            handleException(
                e,
                "Failed to load cocktail details",
                recoveryAction = ErrorHandler.RecoveryAction("Retry") {
                    viewModelScope.launch { loadCocktail(cocktailId) }
                }
            )
            setLoading(false)
        }
    }
    
    /**
     * Toggle favorite status.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleFavorite() {
        val currentCocktail = _cocktail.value ?: return
        
        try {
            if (_isFavorite.value) {
                cocktailRepository.removeFromFavorites(currentCocktail)
                _isFavorite.value = false
                
                setError(
                    title = "Removed from Favorites",
                    message = "${currentCocktail.name} has been removed from your favorites",
                    category = ErrorHandler.ErrorCategory.DATA,
                    showAsEvent = true
                )
            } else {
                cocktailRepository.addToFavorites(currentCocktail)
                _isFavorite.value = true
                
                setError(
                    title = "Added to Favorites",
                    message = "${currentCocktail.name} has been added to your favorites",
                    category = ErrorHandler.ErrorCategory.DATA,
                    showAsEvent = true
                )
            }
        } catch (e: Exception) {
            handleException(
                e,
                "Failed to update favorites",
                showAsEvent = true
            )
            // Revert the state on error
            _isFavorite.value = !_isFavorite.value
        }
    }
    
    /**
     * Add to cart or update quantity.
     * SKIE will convert this to Swift async function.
     */
    suspend fun addToCart(quantity: Int = 1) {
        val currentCocktail = _cocktail.value ?: return
        
        try {
            val cartItem = com.cocktailcraft.domain.model.CocktailCartItem(
                cocktail = currentCocktail,
                quantity = quantity
            )
            cartRepository.addToCart(cartItem)
            
            _isInCart.value = true
            _cartQuantity.value = quantity
            
            setError(
                title = "Added to Cart",
                message = "${currentCocktail.name} has been added to your cart",
                category = ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
        } catch (e: Exception) {
            handleException(
                e,
                "Failed to add to cart",
                showAsEvent = true,
                recoveryAction = ErrorHandler.RecoveryAction("Retry") {
                    viewModelScope.launch { addToCart(quantity) }
                }
            )
        }
    }
    
    /**
     * Update cart quantity.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateCartQuantity(quantity: Int) {
        val currentCocktail = _cocktail.value ?: return
        
        if (quantity <= 0) {
            removeFromCart()
            return
        }
        
        try {
            cartRepository.updateQuantity(currentCocktail.id, quantity)
            _cartQuantity.value = quantity
        } catch (e: Exception) {
            handleException(
                e,
                "Failed to update quantity",
                showAsEvent = true
            )
        }
    }
    
    /**
     * Remove from cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun removeFromCart() {
        val currentCocktail = _cocktail.value ?: return
        
        try {
            cartRepository.removeFromCart(currentCocktail.id)
            _isInCart.value = false
            _cartQuantity.value = 0
            
            setError(
                title = "Removed from Cart",
                message = "${currentCocktail.name} has been removed from your cart",
                category = ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
        } catch (e: Exception) {
            handleException(
                e,
                "Failed to remove from cart",
                showAsEvent = true
            )
        }
    }
    
    /**
     * Share cocktail recipe.
     * Returns a shareable text representation.
     */
    fun getShareableText(): String {
        val currentCocktail = _cocktail.value ?: return ""
        
        val ingredients = currentCocktail.ingredients.joinToString("\n") { ingredient ->
            "• ${ingredient.measure?.trim() ?: ""} ${ingredient.name}".trim()
        }
        
        return """
            🍹 ${currentCocktail.name}
            
            Category: ${currentCocktail.category ?: "Unknown"}
            Glass: ${currentCocktail.glass ?: "Any"}
            
            Ingredients:
            $ingredients
            
            Instructions:
            ${currentCocktail.instructions ?: "No instructions available"}
            
            Shared from CocktailCraft
        """.trimIndent()
    }
    
    /**
     * Get nutrition facts (example calculation).
     */
    fun getNutritionFacts(): Map<String, String> {
        val currentCocktail = _cocktail.value ?: return emptyMap()
        
        // Example calculation based on common ingredients
        val hasVodka = currentCocktail.ingredients.any { 
            it.name.contains("vodka", ignoreCase = true) 
        }
        val hasRum = currentCocktail.ingredients.any { 
            it.name.contains("rum", ignoreCase = true) 
        }
        val hasJuice = currentCocktail.ingredients.any { 
            it.name.contains("juice", ignoreCase = true) 
        }
        
        val calories = when {
            hasVodka || hasRum -> "180-220"
            hasJuice -> "150-180"
            else -> "120-150"
        }
        
        return mapOf(
            "Calories" to "$calories cal",
            "Carbs" to "15-25g",
            "Sugar" to "10-20g",
            "Alcohol" to if (currentCocktail.alcoholic == "Alcoholic") "1.5-2 oz" else "0 oz"
        )
    }
    
    /**
     * Refresh cocktail details.
     */
    fun refresh() {
        currentCocktailId?.let { id ->
            viewModelScope.launch {
                loadCocktail(id)
            }
        }
    }
    
    // Private helper functions
    
    private fun groupIngredients(cocktail: Cocktail) {
        val grouped = cocktail.ingredients.groupBy { ingredient ->
            when {
                ingredient.name.contains("vodka", ignoreCase = true) ||
                ingredient.name.contains("rum", ignoreCase = true) ||
                ingredient.name.contains("gin", ignoreCase = true) ||
                ingredient.name.contains("whiskey", ignoreCase = true) ||
                ingredient.name.contains("tequila", ignoreCase = true) -> "Spirits"
                
                ingredient.name.contains("juice", ignoreCase = true) ||
                ingredient.name.contains("syrup", ignoreCase = true) ||
                ingredient.name.contains("soda", ignoreCase = true) -> "Mixers"
                
                ingredient.name.contains("lime", ignoreCase = true) ||
                ingredient.name.contains("lemon", ignoreCase = true) ||
                ingredient.name.contains("orange", ignoreCase = true) ||
                ingredient.name.contains("cherry", ignoreCase = true) -> "Garnish"
                
                else -> "Other"
            }
        }.mapValues { (_, ingredients) ->
            ingredients.map { "${it.measure?.trim() ?: ""} ${it.name}".trim() }
        }
        
        _ingredientsByType.value = grouped
    }
    
    private suspend fun checkFavoriteStatus(cocktail: Cocktail) {
        try {
            cocktailRepository.getFavoriteCocktails()
                .first()
                .any { it.id == cocktail.id }
                .let { _isFavorite.value = it }
        } catch (e: Exception) {
            // Silent fail for favorite check
        }
    }
    
    private suspend fun checkCartStatus(cocktailId: String) {
        try {
            cartRepository.getCartItems()
                .first()
                .find { it.cocktail.id == cocktailId }
                ?.let { cartItem ->
                    _isInCart.value = true
                    _cartQuantity.value = cartItem.quantity
                } ?: run {
                    _isInCart.value = false
                    _cartQuantity.value = 0
                }
        } catch (e: Exception) {
            // Silent fail for cart check
        }
    }
    
    private suspend fun loadRelatedCocktails(cocktail: Cocktail) {
        try {
            val category = cocktail.category ?: return
            
            cocktailRepository.filterByCategory(category)
                .first()
                .filter { it.id != cocktail.id }
                .shuffled()
                .take(6)
                .let { _relatedCocktails.value = it }
        } catch (e: Exception) {
            // Silent fail for related cocktails
        }
    }
    
    // Recently viewed functionality commented out - method may not exist in repository
    // private suspend fun addToRecentlyViewed(cocktail: Cocktail) {
    //     try {
    //         cocktailRepository.addToRecentlyViewed(cocktail)
    //     } catch (e: Exception) {
    //         // Silent fail for recently viewed
    //     }
    // }
}