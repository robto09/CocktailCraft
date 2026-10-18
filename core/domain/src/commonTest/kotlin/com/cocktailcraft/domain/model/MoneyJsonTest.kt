package com.cocktailcraft.domain.model

import kotlinx.serialization.builtins.ListSerializer
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
 * Payloads stored before money moved to cents hold floating-point dollars
 * under the old keys; they must keep decoding to the same amounts.
 */
class MoneyJsonTest {

    // The data layer's configuration (DataModule)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun cocktailsAreWrittenWithPriceCents() {
        val cocktail = Cocktail(id = "1", name = "Mojito", price = Money(1249))

        val encoded = json.encodeToJsonElement(Cocktail.serializer(), cocktail)

        assertEquals(1249L, encoded.jsonObject.getValue("priceCents").jsonPrimitive.long)
        assertFalse("price" in encoded.jsonObject)
    }

    @Test
    fun cocktailsRoundTrip() {
        val cocktail = Cocktail(id = "1", name = "Mojito", price = Money(1249))

        val encoded = json.encodeToString(Cocktail.serializer(), cocktail)

        assertEquals(cocktail, json.decodeFromString(Cocktail.serializer(), encoded))
    }

    @Test
    fun legacyCocktailPayloadsReadTheirDollarPriceAsCents() {
        val legacy = """{"id":"11007","name":"Margarita","price":12.29,"inStock":true,"rating":4.5}"""

        assertEquals(Money(1229), json.decodeFromString(Cocktail.serializer(), legacy).price)
    }

    @Test
    fun legacyPricesRoundToTheNearestCent() {
        // 5.0 + 29 / 100.0 is stored as 5.29 in JSON, but derived prices
        // could carry binary noise like this
        val legacy = """{"id":"1","name":"A","price":5.289999999999999}"""

        assertEquals(Money(529), json.decodeFromString(Cocktail.serializer(), legacy).price)
    }

    @Test
    fun legacyCocktailWithoutAPriceGetsTheDefault() {
        val withoutPrice = """{"id":"1","name":"A"}"""

        assertEquals(Cocktail.DEFAULT_PRICE, json.decodeFromString(Cocktail.serializer(), withoutPrice).price)
        assertEquals(
            Cocktail.DEFAULT_PRICE,
            json.decodeFromString(Cocktail.serializer(), """{"id":"1","name":"A","price":null}""").price
        )
    }

    @Test
    fun priceCentsWinsOverALeftoverLegacyPrice() {
        val mixed = """{"id":"1","name":"A","price":99.0,"priceCents":1250}"""

        assertEquals(Money(1250), json.decodeFromString(Cocktail.serializer(), mixed).price)
    }

    @Test
    fun legacyCartItemsNestTheConvertedCocktail() {
        val legacy = """[{"cocktail":{"id":"1","name":"A","price":8.5},"quantity":3}]"""

        val items = json.decodeFromString(ListSerializer(CocktailCartItem.serializer()), legacy)

        assertEquals(Money(850), items.single().cocktail.price)
        assertEquals(Money(2550), items.single().lineTotal)
    }

    @Test
    fun legacyOrdersReadTheirDollarTotalsAndItemPrices() {
        // The shape Settings held before orders moved to the database
        val legacy = """
            [{"id":"ORD-1","date":"2024-05-01","status":"Delivered","total":30.99,
              "items":[{"name":"Margarita","quantity":2,"price":12.5}]}]
        """.trimIndent()

        val order = json.decodeFromString(ListSerializer(Order.serializer()), legacy).single()

        assertEquals(Money(3099), order.total)
        assertEquals(OrderItem(name = "Margarita", quantity = 2, price = Money(1250)), order.items.single())
        assertNull(order.items.single().cocktailId)
        assertEquals(Money(2500), order.subtotal)
        assertEquals(Money(599), order.deliveryFee)
        assertFalse(order.canReorder)
    }

    @Test
    fun ordersRoundTripWithCentsAndCocktailIds() {
        val order = Order(
            id = "ORD-2",
            date = "2026-10-18",
            items = listOf(OrderItem(name = "Mojito", quantity = 1, price = Money(999), cocktailId = "11000")),
            total = Money(1598),
            status = OrderStatus.PROCESSING
        )

        val encoded = json.encodeToString(Order.serializer(), order)

        assertTrue(encoded.contains("\"totalCents\":1598"), encoded)
        assertEquals(order, json.decodeFromString(Order.serializer(), encoded))
    }
}
