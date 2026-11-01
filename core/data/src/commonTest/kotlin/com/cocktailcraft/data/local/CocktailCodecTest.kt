package com.cocktailcraft.data.local

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.testutil.partialCocktail
import com.cocktailcraft.testutil.testCocktail
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The stored cocktail payload ([CocktailEntity]) and the shapes older
 * versions wrote: payloads with floating-point dollar prices and partial
 * records marked by placeholder text must keep reading back the same.
 */
class CocktailCodecTest {

    // The data layer's configuration (DataModule)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val codec = CocktailCodec(json)

    @Test
    fun cocktailsAreWrittenWithPriceCents() {
        val encoded = json.parseToJsonElement(codec.encode(Cocktail(id = "1", name = "Mojito", price = Money(1249))))

        assertEquals(1249L, encoded.jsonObject.getValue("priceCents").jsonPrimitive.long)
        assertFalse("price" in encoded.jsonObject)
    }

    @Test
    fun fullAndPartialCocktailsRoundTrip() {
        val full = testCocktail("1", price = Money(1249))
        val partial = partialCocktail("2")

        assertEquals(full, codec.decode(codec.encode(full), hasFullDetails = true))
        assertEquals(partial, codec.decode(codec.encode(partial), hasFullDetails = false))
    }

    @Test
    fun legacyCocktailPayloadsReadTheirDollarPriceAsCents() {
        val legacy = """{"id":"11007","name":"Margarita","price":12.29,"inStock":true,"rating":4.5}"""

        assertEquals(Money(1229), codec.decode(legacy).price)
    }

    @Test
    fun legacyPricesRoundToTheNearestCent() {
        // 5.0 + 29 / 100.0 is stored as 5.29 in JSON, but derived prices
        // could carry binary noise like this
        assertEquals(Money(529), codec.decode("""{"id":"1","name":"A","price":5.289999999999999}""").price)
    }

    @Test
    fun legacyCocktailWithoutAPriceGetsTheDefault() {
        assertEquals(Cocktail.DEFAULT_PRICE, codec.decode("""{"id":"1","name":"A"}""").price)
        assertEquals(Cocktail.DEFAULT_PRICE, codec.decode("""{"id":"1","name":"A","price":null}""").price)
    }

    @Test
    fun priceCentsWinsOverALeftoverLegacyPrice() {
        assertEquals(Money(1250), codec.decode("""{"id":"1","name":"A","price":99.0,"priceCents":1250}""").price)
    }

    @Test
    fun legacyDollarsRoundHalfAwayFromZeroToTheNearestCent() {
        assertEquals(1250L, legacyDollarsToCents(12.5))
        assertEquals(599L, legacyDollarsToCents(5.99))
        // Binary doubles a hair below the cent still land on it
        assertEquals(529L, legacyDollarsToCents(5.0 + 29 / 100.0))
        assertEquals(1015L, legacyDollarsToCents(5.0 + 515 / 100.0))
        assertEquals(13L, legacyDollarsToCents(0.125))
        assertEquals(-13L, legacyDollarsToCents(-0.125))
        assertEquals(0L, legacyDollarsToCents(0.004))
        assertEquals(1L, legacyDollarsToCents(0.005))
    }

    @Test
    fun aLegacyPlaceholderPayloadReadsAsAPartialRecordWithoutThePlaceholderText() {
        // What older versions stored for a filter.php record
        val legacy = """
            {"id":"1","name":"Mojito","instructions":"Tap to view recipe",
             "ingredients":[{"name":"Tap to view ingredients","measure":""}],"priceCents":800}
        """.trimIndent()

        val cocktail = codec.decode(legacy)

        assertFalse(cocktail.hasFullDetails)
        assertTrue(cocktail.ingredients.isEmpty())
        assertNull(cocktail.instructions)
    }

    @Test
    fun theStoredFlagWinsOverWhatThePayloadLooksLike() {
        val payload = codec.encode(testCocktail("1"))

        assertTrue(codec.decode(payload).hasFullDetails)
        assertFalse(codec.decode(payload, hasFullDetails = false).hasFullDetails)
        assertEquals(
            listOf(CocktailIngredient("Gin", "50 ml")),
            codec.decode(payload, hasFullDetails = false).ingredients
        )
    }
}
