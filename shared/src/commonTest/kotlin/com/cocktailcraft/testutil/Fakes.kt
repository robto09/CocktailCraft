package com.cocktailcraft.testutil

import com.cocktailcraft.data.remote.CategoryDto
import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.data.remote.IngredientDto
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeNetworkMonitor(initiallyOnline: Boolean = true) : NetworkMonitor {
    private val _online = MutableStateFlow(initiallyOnline)
    override val isOnline: StateFlow<Boolean> = _online.asStateFlow()

    // SH-2 regression counters: monitoring lifecycle belongs to app init,
    // so ViewModels must never call these.
    var startCalls = 0
        private set
    var stopCalls = 0
        private set

    override fun startMonitoring() {
        startCalls++
    }

    override fun stopMonitoring() {
        stopCalls++
    }

    fun setOnline(online: Boolean) {
        _online.value = online
    }
}

class FakeCartRepository : CartRepository {
    private val items = MutableStateFlow<List<CocktailCartItem>>(emptyList())

    /** When true, the next mutating call returns Result.Error (then resets). */
    var failNextMutation = false
    private fun consumeFailFlag() = failNextMutation.also { failNextMutation = false }

    override fun observeCartItems(): Flow<List<CocktailCartItem>> = items
    override suspend fun getCartItems(): Result<List<CocktailCartItem>> = Result.Success(items.value)

    override suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit> {
        if (consumeFailFlag()) return Result.Error("add failed")
        val existing = items.value.find { it.cocktail.id == cartItem.cocktail.id }
        items.value = if (existing != null) {
            items.value.map {
                if (it.cocktail.id == cartItem.cocktail.id) it.copy(quantity = it.quantity + cartItem.quantity) else it
            }
        } else {
            items.value + cartItem
        }
        return Result.Success(Unit)
    }

    override suspend fun removeFromCart(cocktailId: String): Result<Unit> {
        if (consumeFailFlag()) return Result.Error("remove failed")
        items.value = items.value.filterNot { it.cocktail.id == cocktailId }
        return Result.Success(Unit)
    }

    override suspend fun updateQuantity(cocktailId: String, quantity: Int): Result<Unit> {
        if (consumeFailFlag()) return Result.Error("update failed")
        items.value = items.value.map {
            if (it.cocktail.id == cocktailId) it.copy(quantity = quantity) else it
        }
        return Result.Success(Unit)
    }

    override suspend fun clearCart(): Result<Unit> {
        items.value = emptyList()
        return Result.Success(Unit)
    }

    override suspend fun getCartTotal(): Result<Double> =
        Result.Success(items.value.sumOf { it.cocktail.price * it.quantity })
}

class FakeDetailRepository(
    val byId: MutableMap<String, Cocktail> = mutableMapOf()
) : CocktailDetailRepository {
    override suspend fun getCocktailById(id: String): Result<Cocktail?> = Result.Success(byId[id])
    override suspend fun refreshCocktailById(id: String): Result<Cocktail?> = Result.Success(byId[id])
    override suspend fun getRandomCocktail(): Result<Cocktail?> = Result.Success(byId.values.firstOrNull())
    override fun getCocktailImageUrl(cocktail: Cocktail): String = cocktail.imageUrl.orEmpty()
}

class FakeFavoritesRepository : CocktailFavoritesRepository {
    private val favorites = MutableStateFlow<List<Cocktail>>(emptyList())

    /** Snapshot of currently stored favorites (kept for existing assertions). */
    val stored: List<Cocktail> get() = favorites.value

    override fun observeFavorites(): Flow<List<Cocktail>> = favorites

    override suspend fun getFavoriteCocktails(): Result<List<Cocktail>> = Result.Success(favorites.value)

    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> {
        if (favorites.value.none { it.id == cocktail.id }) favorites.value += cocktail
        return Result.Success(Unit)
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> {
        favorites.value = favorites.value.filterNot { it.id == cocktail.id }
        return Result.Success(Unit)
    }

    override suspend fun isCocktailFavorite(id: String): Result<Boolean> =
        Result.Success(favorites.value.any { it.id == id })
}

class FakeSearchRepository(var all: List<Cocktail> = emptyList()) : CocktailSearchRepository {
    /** Every advancedSearch invocation's filters, in order — lets tests assert debouncing (SH-13). */
    val advancedSearchCalls = mutableListOf<SearchFilters>()

    /** When set, every search/filter method returns Result.Error with this message (simulates API failure). */
    var errorMessage: String? = null

    override suspend fun searchCocktailsByName(name: String): Result<List<Cocktail>> {
        errorMessage?.let { return Result.Error(it) }
        return Result.Success(all.filter { it.name.contains(name, ignoreCase = true) })
    }

    /** Mirrors the real intersection logic: AND the 4 supported fields over [all]. */
    override suspend fun advancedSearch(filters: SearchFilters): Result<List<Cocktail>> {
        advancedSearchCalls.add(filters)
        errorMessage?.let { return Result.Error(it) }
        var result = all
        if (filters.query.isNotBlank()) {
            result = result.filter { it.name.contains(filters.query, ignoreCase = true) }
        }
        filters.category?.let { cat -> result = result.filter { it.category == cat } }
        filters.ingredient?.let { ing ->
            result = result.filter { c -> c.ingredients.any { it.name.contains(ing, ignoreCase = true) } }
        }
        filters.alcoholic?.let { alc ->
            result = result.filter {
                val value = it.alcoholic?.replace('_', ' ')
                if (alc) value.equals("Alcoholic", ignoreCase = true)
                else value.equals("Non alcoholic", ignoreCase = true)
            }
        }
        return Result.Success(result)
    }

    override suspend fun filterByIngredient(ingredient: String): Result<List<Cocktail>> {
        errorMessage?.let { return Result.Error(it) }
        return Result.Success(all)
    }

    override suspend fun filterByAlcoholic(alcoholic: Boolean): Result<List<Cocktail>> {
        errorMessage?.let { return Result.Error(it) }
        return Result.Success(all)
    }

    override suspend fun filterByCategory(category: String): Result<List<Cocktail>> {
        errorMessage?.let { return Result.Error(it) }
        return Result.Success(all.filter { it.category == category })
    }
}

class FakeCatalogRepository(var all: List<Cocktail> = emptyList()) : CocktailCatalogRepository {
    override suspend fun getCategories(): Result<List<String>> =
        Result.Success(all.mapNotNull { it.category }.distinct())

    override suspend fun getIngredients(): Result<List<String>> = Result.Success(emptyList())
    override suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>> = Result.Success(all)
}

class FakeOfflineRepository : CocktailOfflineRepository {
    var offlineModeEnabled = false
    var recentlyViewed: List<Cocktail> = emptyList()
    var clearCacheCalls = 0
        private set

    override suspend fun checkApiConnectivity(): Result<Boolean> = Result.Success(true)
    override suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>> = Result.Success(recentlyViewed)
    override suspend fun setOfflineMode(enabled: Boolean) {
        offlineModeEnabled = enabled
    }

    override suspend fun isOfflineModeEnabled(): Boolean = offlineModeEnabled

    override suspend fun isOffline(): Boolean = offlineModeEnabled

    override suspend fun clearCache(): Result<Unit> {
        clearCacheCalls++
        recentlyViewed = emptyList()
        return Result.Success(Unit)
    }
}

internal class FakeCocktailApi(var drinks: List<CocktailDto> = emptyList()) : CocktailApi {
    /** When set, name-search and filter.php endpoints throw it (to simulate an API failure). */
    var endpointError: Exception? = null

    override suspend fun searchCocktailsByName(name: String): List<CocktailDto> {
        endpointError?.let { throw it }
        return drinks.filter { it.name.contains(name, ignoreCase = true) }
    }

    override suspend fun getCocktailById(id: String): CocktailDto? = drinks.find { it.id == id }
    override suspend fun getRandomCocktail(): CocktailDto? = drinks.firstOrNull()

    override suspend fun filterByIngredient(ingredient: String): List<CocktailDto> {
        endpointError?.let { throw it }
        return drinks.filter { dto -> dto.getIngredients().any { it.name.contains(ingredient, ignoreCase = true) } }
    }

    override suspend fun filterByAlcoholic(alcoholic: Boolean): List<CocktailDto> {
        endpointError?.let { throw it }
        // The real server matches ?a=Non_Alcoholic against records whose
        // strAlcoholic is "Non alcoholic" — replicate that tolerance here.
        val target = if (alcoholic) "Alcoholic" else "Non alcoholic"
        return drinks.filter { it.alcoholic?.replace('_', ' ').equals(target, ignoreCase = true) }
    }

    /** When set, only filter.php?c= throws it — kept separate from [endpointError] so intersection tests can fail a later filter while category succeeds. */
    var categoryEndpointError: Exception? = null

    override suspend fun filterByCategory(category: String): List<CocktailDto> {
        categoryEndpointError?.let { throw it }
        return drinks.filter { it.category == category }
    }

    /** list.php?c=list payload — seeded by catalog tests, empty by default. */
    var categories: List<CategoryDto> = emptyList()

    /** list.php?i=list payload — seeded by catalog tests, empty by default. */
    var ingredients: List<IngredientDto> = emptyList()

    override suspend fun getCategories(): List<CategoryDto> = categories
    override suspend fun getIngredients(): List<IngredientDto> = ingredients
    override suspend fun pingApi(): Boolean = true
}
