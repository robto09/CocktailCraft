package com.cocktailcraft.testutil

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
import kotlinx.coroutines.delay
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
    /** When set, getCocktailById throws it (e.g. a CancellationException). */
    var throwOnLookup: Exception? = null

    override suspend fun getCocktailById(id: String): Result<Cocktail?> {
        throwOnLookup?.let { throw it }
        return Result.Success(byId[id])
    }
    override suspend fun refreshCocktailById(id: String): Result<Cocktail?> = Result.Success(byId[id])
    override suspend fun getRandomCocktail(): Result<Cocktail?> = Result.Success(byId.values.firstOrNull())
    override fun getCocktailImageUrl(cocktail: Cocktail): String = cocktail.imageUrl.orEmpty()
}

class FakeFavoritesRepository : CocktailFavoritesRepository {
    private val favorites = MutableStateFlow<List<Cocktail>>(emptyList())

    /** Snapshot of currently stored favorites (kept for existing assertions). */
    val stored: List<Cocktail> get() = favorites.value

    /** How often the full list was loaded — point lookups must not need it. */
    var getFavoriteCocktailsCalls = 0
        private set

    override fun observeFavorites(): Flow<List<Cocktail>> = favorites

    override suspend fun getFavoriteCocktails(): Result<List<Cocktail>> {
        getFavoriteCocktailsCalls++
        return Result.Success(favorites.value)
    }

    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> {
        // Mirrors the real impl: a re-add of an existing id upgrades that
        // entry in place (persist-then-hydrate), new ids append.
        favorites.value = if (favorites.value.any { it.id == cocktail.id }) {
            favorites.value.map { if (it.id == cocktail.id) cocktail else it }
        } else {
            favorites.value + cocktail
        }
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

    /**
     * When > 0, filterByCategory decides its result at call time and then
     * suspends this long before returning — models the real fetcher's cache
     * IO / timing-out network attempt, for in-flight race tests.
     */
    var filterDelayMs: Long = 0

    override suspend fun filterByCategory(category: String): Result<List<Cocktail>> {
        errorMessage?.let { return Result.Error(it) }
        val result = all.filter { it.category == category } // snapshot at call time
        if (filterDelayMs > 0) delay(filterDelayMs)
        return Result.Success(result)
    }
}

class FakeCatalogRepository(var all: List<Cocktail> = emptyList()) : CocktailCatalogRepository {
    override suspend fun getCategories(): Result<List<String>> =
        Result.Success(all.mapNotNull { it.category }.distinct())

    override suspend fun getIngredients(): Result<List<String>> = Result.Success(emptyList())
    override suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>> = Result.Success(all)
}

class FakeOfflineRepository : CocktailOfflineRepository {
    var offline = false
    var recentlyViewed: List<Cocktail> = emptyList()

    override suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>> = Result.Success(recentlyViewed)

    override suspend fun isOffline(): Boolean = offline
}
