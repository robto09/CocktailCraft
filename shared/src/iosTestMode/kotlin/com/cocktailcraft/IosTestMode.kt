package com.cocktailcraft

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.di.secureSettingsQualifier
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/**
 * Starts Koin for the iOS unit and UI tests: the app's real graph, with its
 * storage and network bindings replaced so a test run can neither read nor
 * modify the simulator's real app data, nor depend on TheCocktailDB:
 *  - an in-memory database (cart, orders, favorites, reviews, offline cache),
 *    empty on every launch,
 *  - plain and "secure" Settings in throwaway NSUserDefaults suites, wiped on
 *    every launch (theme settings, auth, the Keychain install marker),
 *  - an HttpClient over a MockEngine that serves [TestModeFixtures] in
 *    TheCocktailDB's response format.
 *
 * Everything above the bindings (repositories, use cases, ViewModels) is the
 * production code. Swift calls this instead of doInitKoin() when the
 * process runs with COCKTAILCRAFT_TEST_MODE (see KoinInitializer).
 */
// Test mode only exists in Debug builds, so it keeps Debug logging.
fun doInitKoinForTesting(): KoinApplication =
    initKoin(isDebug = true, overrides = listOf(iosTestModeModule()))

internal fun iosTestModeModule(): Module = module {
    single<Settings> { freshUserDefaults(TEST_SETTINGS_SUITE) }
    single<Settings>(secureSettingsQualifier) { freshUserDefaults(TEST_SECURE_SETTINGS_SUITE) }
    single<SqlDriver> { inMemoryDriver(CocktailCraftDatabase.Schema) }
    single<HttpClient> { testModeHttpClient(json = get()) }
}

private const val TEST_SETTINGS_SUITE = "com.cocktailcraft.testmode"
private const val TEST_SECURE_SETTINGS_SUITE = "com.cocktailcraft.testmode.secure"

/** An NSUserDefaults suite emptied first, so every test launch starts clean. */
private fun freshUserDefaults(suiteName: String): Settings {
    NSUserDefaults.standardUserDefaults.removePersistentDomainForName(suiteName)
    return NSUserDefaultsSettings(NSUserDefaults(suiteName = suiteName))
}

/**
 * Same response contract the API layer relies on (JSON bodies, non-2xx
 * throws), without the production client's retry/timeout/logging plugins:
 * the fixtures answer instantly and never fail transiently.
 */
private fun testModeHttpClient(json: Json): HttpClient =
    HttpClient(MockEngine { request -> respondFixture(request) }) {
        install(ContentNegotiation) { json(json) }
        expectSuccess = true
    }

private fun MockRequestHandleScope.respondFixture(request: HttpRequestData): HttpResponseData {
    val body = TestModeFixtures.responseFor(
        endpoint = request.url.encodedPath.substringAfterLast('/'),
        parameters = request.url.parameters.entries().associate { (key, values) -> key to values.first() }
    ) ?: return respond("", HttpStatusCode.NotFound)
    return respond(
        content = body.toString(),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
}

/**
 * A few real TheCocktailDB drinks (ids and data as the live API returns
 * them, minus images: thumbnails are null so the UI shows its offline
 * placeholder and Kingfisher never hits the network).
 */
internal object TestModeFixtures {

    private class Drink(
        val id: String,
        val name: String,
        val category: String,
        val alcoholic: String,
        val glass: String,
        val instructions: String,
        val ingredients: List<Pair<String, String>>
    )

    private val drinks = listOf(
        Drink(
            id = "11007",
            name = "Margarita",
            category = "Ordinary Drink",
            alcoholic = "Alcoholic",
            glass = "Cocktail glass",
            instructions = "Rub the rim of the glass with the lime slice to make the salt stick to it. " +
                "Shake the other ingredients with ice, then carefully pour into the glass.",
            ingredients = listOf(
                "Tequila" to "1 1/2 oz",
                "Triple sec" to "1/2 oz",
                "Lime juice" to "1 oz",
                "Salt" to ""
            )
        ),
        Drink(
            id = "11000",
            name = "Mojito",
            category = "Cocktail",
            alcoholic = "Alcoholic",
            glass = "Highball glass",
            instructions = "Muddle mint leaves with sugar and lime juice. Add a splash of soda water " +
                "and fill the glass with cracked ice. Pour the rum and top with soda water.",
            ingredients = listOf(
                "Light rum" to "2-3 oz",
                "Lime" to "Juice of 1",
                "Sugar" to "2 tsp",
                "Mint" to "2-4",
                "Soda water" to ""
            )
        ),
        Drink(
            id = "11001",
            name = "Old Fashioned",
            category = "Cocktail",
            alcoholic = "Alcoholic",
            glass = "Old-fashioned glass",
            instructions = "Place sugar cube in old fashioned glass and saturate with bitters, add a " +
                "dash of plain water. Muddle until dissolved. Fill the glass with ice cubes and add whiskey.",
            ingredients = listOf(
                "Bourbon" to "4.5 cL",
                "Angostura bitters" to "2 dashes",
                "Sugar" to "1 cube",
                "Water" to "dash"
            )
        ),
        Drink(
            id = "12560",
            name = "Afterglow",
            category = "Cocktail",
            alcoholic = "Non alcoholic",
            glass = "Highball Glass",
            instructions = "Mix. Serve over ice.",
            ingredients = listOf(
                "Grenadine" to "1 part",
                "Orange juice" to "4 parts",
                "Pineapple juice" to "4 parts"
            )
        )
    )

    /**
     * The JSON body for one TheCocktailDB endpoint call, or null for an
     * endpoint the fixtures don't cover (answered with a 404).
     */
    fun responseFor(endpoint: String, parameters: Map<String, String>): JsonObject? = when (endpoint) {
        "random.php" -> drinksResponse(drinks.take(1))

        "lookup.php" -> drinksResponse(drinks.filter { it.id == parameters["i"] })

        "search.php" -> {
            val query = parameters["s"].orEmpty()
            drinksResponse(drinks.filter { it.name.contains(query, ignoreCase = true) })
        }

        "filter.php" -> drinksResponse(
            when {
                "c" in parameters -> drinks.filter { it.category.equals(parameters["c"], ignoreCase = true) }

                "i" in parameters -> drinks.filter { drink ->
                    drink.ingredients.any { it.first.equals(parameters["i"], ignoreCase = true) }
                }

                "a" in parameters -> drinks.filter {
                    it.alcoholic.replace(' ', '_').equals(parameters["a"], ignoreCase = true)
                }

                else -> emptyList()
            }
        )

        "list.php" -> when {
            "c" in parameters -> listResponse("strCategory", drinks.map { it.category })
            "i" in parameters -> listResponse("strIngredient1", drinks.flatMap { d -> d.ingredients.map { it.first } })
            else -> null
        }

        else -> null
    }

    /** `{"drinks": [...]}`, or `{"drinks": null}` for no matches, as the live API does. */
    private fun drinksResponse(matches: List<Drink>): JsonObject = buildJsonObject {
        put("drinks", if (matches.isEmpty()) JsonNull else JsonArray(matches.map(::drinkJson)))
    }

    private fun listResponse(field: String, values: List<String>): JsonObject = buildJsonObject {
        put("drinks", JsonArray(values.distinct().map { value -> buildJsonObject { put(field, value) } }))
    }

    private fun drinkJson(drink: Drink): JsonObject = buildJsonObject {
        put("idDrink", drink.id)
        put("strDrink", drink.name)
        put("strCategory", drink.category)
        put("strAlcoholic", drink.alcoholic)
        put("strGlass", drink.glass)
        put("strInstructions", drink.instructions)
        put("strDrinkThumb", JsonNull)
        drink.ingredients.forEachIndexed { index, (ingredient, measure) ->
            put("strIngredient${index + 1}", JsonPrimitive(ingredient))
            put("strMeasure${index + 1}", JsonPrimitive(measure))
        }
    }
}
