package com.cocktailcraft.data.remote

import co.touchlab.kermit.Logger
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.model.Money
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.time.Clock

/**
 * Remote access to TheCocktailDB: DTO→domain mapping plus the API rate
 * limiter (state shared via [ApiRateLimiter]). Extracted from the former
 * CocktailRepositoryImpl god class.
 *
 * The filter endpoints — fired in bursts by category paging and multi-filter
 * searches — each take a rate-limit slot ([acquireRateLimitSlot]) before
 * calling the API, so no caller can forget to. Single user-initiated calls
 * (debounced name search, opening a cocktail) and the one-off
 * list.php/random.php calls are not throttled, so they never wait behind
 * the interval.
 */
internal class CocktailRemoteDataSource(
    private val api: CocktailApi,
    private val rateLimiter: ApiRateLimiter,
    // Injectable for tests: coroutine virtual time does not move Clock.System.
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {

    suspend fun searchByName(name: String): List<Cocktail> =
        api.searchCocktailsByName(name).map(::mapToDomain)

    suspend fun getById(id: String): Cocktail? = api.getCocktailById(id)?.let(::mapToDomain)

    suspend fun getRandom(): Cocktail? = api.getRandomCocktail()?.let(::mapToDomain)

    suspend fun filterByIngredient(ingredient: String): List<Cocktail> {
        acquireRateLimitSlot()
        return api.filterByIngredient(ingredient).map(::mapToDomain)
    }

    suspend fun filterByAlcoholic(alcoholic: Boolean): List<Cocktail> {
        acquireRateLimitSlot()
        return api.filterByAlcoholic(alcoholic).map(::mapToDomain)
    }

    /** Raw DTOs so the catalog layer can decide between mapping and cache enrichment. */
    suspend fun filterByCategoryRaw(category: String): List<CocktailDto> {
        acquireRateLimitSlot()
        return api.filterByCategory(category)
    }

    suspend fun getCategories(): List<String> = api.getCategories().map { it.name }
    suspend fun getIngredients(): List<String> = api.getIngredients().map { it.name }

    // --- Rate limiting ---
    // Only the proactive min-interval throttle lives here (request shaping);
    // retry and 429 backoff are owned solely by Ktor's HttpRequestRetry (SH-7).

    /**
     * Takes the next API call slot and waits until it arrives. Reserving is
     * atomic ([ApiRateLimiter.reserveSlot]), so concurrent callers are spaced
     * [ApiRateLimiter.MIN_API_CALL_INTERVAL_MS] apart instead of all passing
     * one shared "time since last call" check.
     */
    private suspend fun acquireRateLimitSlot() {
        val waitMs = rateLimiter.reserveSlot(nowMs())
        if (waitMs > 0) {
            Logger.d { "Rate limiting: waiting ${waitMs}ms before API call" }
            delay(waitMs)
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
            alcoholic = dto.alcoholic ?: Cocktail.ALCOHOLIC_UNKNOWN,
            popularity = demoPopularity(seed)
        )
    }

    // Demo values: price $5.00-$15.00, rating 3.0-5.0, popularity 1-100.
    // The price is the same as when it was the Double 5.0 + (seed % 1001) / 100.0
    private fun demoPrice(seed: Int): Money = Money(DemoMinPriceCents + seed % DemoPriceSteps)
    private fun demoRating(seed: Int): Float = 3.0f + (seed % 21) / 10.0f
    private fun demoPopularity(seed: Int): Int = seed % 100 + 1
}

/** $5.00, the cheapest demo price. */
private const val DemoMinPriceCents = 500L

/** 1001 one-cent steps: demo prices run $5.00-$15.00. */
private const val DemoPriceSteps = 1001
