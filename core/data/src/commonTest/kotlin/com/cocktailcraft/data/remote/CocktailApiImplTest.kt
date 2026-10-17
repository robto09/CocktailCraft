package com.cocktailcraft.data.remote

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.di.buildHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises CocktailApiImpl through the REAL production Ktor pipeline built by
 * [buildHttpClient] — ContentNegotiation with the SH-6 quirk shield,
 * HttpRequestRetry, and expectSuccess — with only the engine mocked (AR-5).
 * CocktailResponseParsingTest covers the raw Json decode of the same quirks;
 * here they must survive the full client stack.
 */
class CocktailApiImplTest {

    private val config = AppConfigImpl()

    // Mirrors the DataModule Json configuration.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun api(engine: MockEngine): CocktailApiImpl =
        CocktailApiImpl(client = buildHttpClient(json, config, engine), appConfig = config)

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK
    ): HttpResponseData = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )

    private val margaritaPayload = """
        {"drinks":[{
            "idDrink":"11007","strDrink":"Margarita","strCategory":"Ordinary Drink",
            "strAlcoholic":"Alcoholic","strGlass":"Cocktail glass",
            "strInstructions":"Shake with ice.","strDrinkThumb":"https://example.com/m.jpg",
            "strIngredient1":"Tequila","strMeasure1":"1 1/2 oz"
        }]}
    """.trimIndent()

    @Test
    fun searchCocktailsByNameHitsSearchEndpointAndMapsDtos() = runTest {
        val engine = MockEngine { respondJson(margaritaPayload) }

        val drinks = api(engine).searchCocktailsByName("margarita")

        val request = engine.requestHistory.single()
        assertEquals("www.thecocktaildb.com", request.url.host)
        assertEquals("/api/json/v1/1/search.php", request.url.encodedPath)
        assertEquals("margarita", request.url.parameters["s"])

        val drink = drinks.single()
        assertEquals("11007", drink.id)
        assertEquals("Margarita", drink.name)
        assertEquals("Ordinary Drink", drink.category)
        assertEquals("Tequila", drink.getIngredients().single().name)
        assertEquals("1 1/2 oz", drink.getIngredients().single().measure)
    }

    @Test
    fun getCocktailByIdHitsLookupEndpointAndReturnsFirstDrink() = runTest {
        val payload = """
            {"drinks":[
                {"idDrink":"11007","strDrink":"Margarita"},
                {"idDrink":"11008","strDrink":"Manhattan"}
            ]}
        """.trimIndent()
        val engine = MockEngine { respondJson(payload) }

        val drink = api(engine).getCocktailById("11007")

        val request = engine.requestHistory.single()
        assertEquals("/api/json/v1/1/lookup.php", request.url.encodedPath)
        assertEquals("11007", request.url.parameters["i"])
        assertEquals("11007", drink?.id)
    }

    @Test
    fun getCocktailByIdReturnsNullWhenDrinksIsNull() = runTest {
        val engine = MockEngine { respondJson("""{"drinks":null}""") }

        assertNull(api(engine).getCocktailById("99999"))
    }

    @Test
    fun filterByCategoryPropagatesServerErrorAfterExhaustingRetries() = runTest {
        val engine = MockEngine {
            respondJson("""{"message":"boom"}""", HttpStatusCode.InternalServerError)
        }

        assertFailsWith<ServerResponseException> {
            api(engine).filterByCategory("Cocktail")
        }

        // HttpRequestRetry re-requests retryable 5xx: initial attempt plus
        // AppConfig.maxRetries (3) retries before the failure surfaces.
        assertEquals(config.maxRetries + 1, engine.requestHistory.size)
        assertTrue(engine.requestHistory.all { it.url.parameters["c"] == "Cocktail" })
    }

    @Test
    fun noDataFoundStringBodyDecodesToEmptyListThroughPipeline() = runTest {
        // Some empty-result endpoints return the literal string "no data
        // found" for drinks instead of null/[] — the SH-6 shield must hold
        // through ContentNegotiation, not just raw Json.decodeFromString.
        val engine = MockEngine { respondJson("""{"drinks":"no data found"}""") }

        assertEquals(emptyList(), api(engine).searchCocktailsByName("zzz"))
    }

    @Test
    fun malformedRecordAmongValidOnesIsDroppedThroughPipeline() = runTest {
        val payload = """
            {"drinks":[
                {"idDrink":"1","strDrink":"Margarita"},
                {"strDrink":"missing id"},
                {"idDrink":null,"strDrink":"null id"},
                {"idDrink":"2","strDrink":"Mojito"}
            ]}
        """.trimIndent()
        val engine = MockEngine { respondJson(payload) }

        val drinks = api(engine).searchCocktailsByName("m")

        assertEquals(listOf("1", "2"), drinks.map { it.id })
    }

    @Test
    fun rateLimitedRequestIsRetriedThenParsed() = runTest {
        var calls = 0
        val engine = MockEngine {
            calls++
            if (calls == 1) {
                respond(
                    content = """{"message":"rate limited"}""",
                    status = HttpStatusCode.TooManyRequests,
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        // Keep the retry fast: exponentialDelay honors
                        // Retry-After, and runTest skips the virtual delay.
                        HttpHeaders.RetryAfter to listOf("0")
                    )
                )
            } else {
                respondJson(margaritaPayload)
            }
        }

        val drinks = api(engine).searchCocktailsByName("margarita")

        assertEquals(2, engine.requestHistory.size, "the 429 must be retried exactly once")
        assertEquals("Margarita", drinks.single().name)
    }
}
