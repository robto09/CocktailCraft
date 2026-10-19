package com.cocktailcraft.data.remote

import co.touchlab.kermit.Logger
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.Money
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
 *
 * Every call runs on [defaultDispatcher]: Ktor decodes the response body and
 * the mapping walks every record in the coroutine that asks for it, which is
 * the main thread for a ViewModel. The rate-limit wait stays on the caller's
 * dispatcher; it only suspends.
 */
internal class CocktailRemoteDataSource(
    private val api: CocktailApi,
    private val rateLimiter: ApiRateLimiter,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    // Injectable for tests: coroutine virtual time does not move Clock.System.
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {

    // search.php, lookup.php and random.php return full records; the
    // filter.php endpoints only partial ones (id, name, thumbnail)

    suspend fun searchByName(name: String): List<Cocktail> =
        offMain { api.searchCocktailsByName(name).map(::mapFull) }

    suspend fun getById(id: String): Cocktail? = offMain { api.getCocktailById(id)?.let(::mapFull) }

    suspend fun getRandom(): Cocktail? = offMain { api.getRandomCocktail()?.let(::mapFull) }

    suspend fun filterByIngredient(ingredient: String): List<Cocktail> {
        acquireRateLimitSlot()
        return offMain { api.filterByIngredient(ingredient).map(::mapPartial) }
    }

    suspend fun filterByAlcoholic(alcoholic: Boolean): List<Cocktail> {
        acquireRateLimitSlot()
        return offMain { api.filterByAlcoholic(alcoholic).map(::mapPartial) }
    }

    /**
     * The cocktails filter.php lists for [category]. The endpoint omits the
     * category, so each is stamped with the one it was fetched by; the
     * offline fallback finds rows by that column.
     */
    suspend fun filterByCategory(category: String): List<Cocktail> {
        acquireRateLimitSlot()
        return offMain {
            api.filterByCategory(category).map { dto ->
                val cocktail = mapPartial(dto)
                if (cocktail.category.isNullOrBlank()) cocktail.copy(category = category) else cocktail
            }
        }
    }

    suspend fun getCategories(): List<String> = offMain { api.getCategories().map { it.name } }
    suspend fun getIngredients(): List<String> = offMain { api.getIngredients().map { it.name } }

    private suspend fun <T> offMain(block: suspend () -> T): T = withContext(defaultDispatcher) { block() }

    // --- Rate limiting ---
    // Only the proactive min-interval throttle lives here (request shaping);
    // retry and 429 backoff are owned solely by Ktor's HttpRequestRetry.

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

    private fun mapFull(dto: CocktailDto): Cocktail = mapToDomain(dto, hasFullDetails = true)

    private fun mapPartial(dto: CocktailDto): Cocktail = mapToDomain(dto, hasFullDetails = false)

    /**
     * Map a DTO to the domain model; [hasFullDetails] says which kind of
     * endpoint returned it. Price/rating/popularity are demo values
     * TheCocktailDB doesn't provide — derived deterministically from the
     * cocktail id so the same drink always shows the same values.
     */
    fun mapToDomain(dto: CocktailDto, hasFullDetails: Boolean): Cocktail {
        val seed = dto.id.hashCode().absoluteValue
        return Cocktail(
            id = dto.id,
            name = dto.name,
            instructions = dto.instructions,
            imageUrl = dto.imageUrl,
            price = demoPrice(seed),
            ingredients = dto.getIngredients(),
            rating = demoRating(seed),
            category = dto.category,
            glass = dto.glass,
            alcoholic = dto.alcoholic ?: Cocktail.ALCOHOLIC_UNKNOWN,
            popularity = demoPopularity(seed),
            hasFullDetails = hasFullDetails
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
