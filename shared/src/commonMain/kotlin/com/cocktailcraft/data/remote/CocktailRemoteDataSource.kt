package com.cocktailcraft.data.remote

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.time.Clock

/**
 * Remote access to TheCocktailDB: DTO→domain mapping plus API rate-limit
 * bookkeeping (state shared via [CocktailCacheManager]). Extracted from the
 * former CocktailRepositoryImpl god class.
 */
internal class CocktailRemoteDataSource(
    private val api: CocktailApi,
    private val cacheManager: CocktailCacheManager
) {

    suspend fun searchByName(name: String): List<Cocktail> =
        api.searchCocktailsByName(name).map(::mapToDomain)

    suspend fun getById(id: String): Cocktail? = api.getCocktailById(id)?.let(::mapToDomain)

    suspend fun getRandom(): Cocktail? = api.getRandomCocktail()?.let(::mapToDomain)

    suspend fun filterByIngredient(ingredient: String): List<Cocktail> =
        api.filterByIngredient(ingredient).map(::mapToDomain)

    suspend fun filterByAlcoholic(alcoholic: Boolean): List<Cocktail> =
        api.filterByAlcoholic(alcoholic).map(::mapToDomain)

    /** Raw DTOs so the catalog layer can decide between mapping and cache enrichment. */
    suspend fun filterByCategoryRaw(category: String): List<CocktailDto> = api.filterByCategory(category)

    suspend fun getCategories(): List<String> = api.getCategories().map { it.name }
    suspend fun getIngredients(): List<String> = api.getIngredients().map { it.name }

    // --- Rate limiting / connectivity ---
    // Only the proactive min-interval throttle lives here (request shaping);
    // retry and 429 backoff are owned solely by Ktor's HttpRequestRetry (SH-7).

    /** Wait out the minimum interval between API calls. */
    suspend fun awaitRateLimitWindow() {
        val timeSinceLastCall = Clock.System.now().toEpochMilliseconds() - cacheManager.getLastApiCallTime()
        if (timeSinceLastCall < CocktailCacheManager.MIN_API_CALL_INTERVAL_MS) {
            val waitTime = CocktailCacheManager.MIN_API_CALL_INTERVAL_MS - timeSinceLastCall
            Logger.d { "Rate limiting: waiting ${waitTime}ms before API call" }
            delay(waitTime)
        }
    }

    suspend fun noteApiCall() {
        cacheManager.setLastApiCallTime(Clock.System.now().toEpochMilliseconds())
    }

    /**
     * Ping with rate-limit awareness. Callers are expected to have handled
     * offline mode already.
     */
    suspend fun ping(): Boolean {
        return try {
            val sinceLastCall = Clock.System.now().toEpochMilliseconds() - cacheManager.getLastApiCallTime()
            if (sinceLastCall < CocktailCacheManager.MIN_API_CALL_INTERVAL_MS) {
                Logger.d { "Skipping ping to avoid rate limit (last call ${sinceLastCall}ms ago)" }
                return true // Assume API is reachable
            }

            noteApiCall()
            api.pingApi()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Map a DTO to the domain model. Price/rating/popularity are demo values
     * TheCocktailDB doesn't provide — derived deterministically from the
     * cocktail id so the same drink always shows the same values.
     */
    fun mapToDomain(dto: CocktailDto): Cocktail {
        val seed = dto.id.hashCode().absoluteValue
        return Cocktail(
            id = dto.id,
            name = dto.name,
            instructions = dto.instructions ?: Cocktail.PLACEHOLDER_INSTRUCTIONS,
            imageUrl = dto.imageUrl,
            price = demoPrice(seed),
            ingredients = dto.getIngredients().ifEmpty {
                // If no ingredients (from filter endpoint), add placeholder
                listOf(CocktailIngredient(Cocktail.PLACEHOLDER_INGREDIENT_NAME, ""))
            },
            rating = demoRating(seed),
            category = dto.category,
            glass = dto.glass,
            alcoholic = dto.alcoholic ?: "Unknown",
            popularity = demoPopularity(seed)
        )
    }

    // Demo values: price $5.00-$15.00, rating 3.0-5.0, popularity 1-100
    private fun demoPrice(seed: Int): Double = 5.0 + (seed % 1001) / 100.0
    private fun demoRating(seed: Int): Float = 3.0f + (seed % 21) / 10.0f
    private fun demoPopularity(seed: Int): Int = seed % 100 + 1
}
