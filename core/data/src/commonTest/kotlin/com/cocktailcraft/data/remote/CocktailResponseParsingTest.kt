package com.cocktailcraft.data.remote

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Locks in the SH-6 hardening against TheCocktailDB response quirks.
 */
class CocktailResponseParsingTest {

    // Mirrors the DataModule Json configuration.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun noDataFoundStringDecodesToEmptyList() {
        // Some empty-result endpoints return the literal string "no data
        // found" for drinks instead of null/[] — must not throw.
        val response = json.decodeFromString<CocktailResponse>("""{"drinks":"no data found"}""")

        assertEquals(emptyList(), response.drinks)
    }

    @Test
    fun malformedRecordIsDroppedWithoutFailingTheWholeResponse() {
        val payload = """
            {"drinks":[
                {"idDrink":"1","strDrink":"Margarita"},
                {"strDrink":"missing id"},
                {"idDrink":null,"strDrink":"null id"},
                {"idDrink":"2","strDrink":"Mojito"}
            ]}
        """.trimIndent()

        val response = json.decodeFromString<CocktailResponse>(payload)

        assertEquals(listOf("1", "2"), response.drinks?.map { it.id })
        assertEquals(listOf("Margarita", "Mojito"), response.drinks?.map { it.name })
    }

    @Test
    fun nullDrinksStaysNullForCallSiteFallbacks() {
        val response = json.decodeFromString<CocktailResponse>("""{"drinks":null}""")

        assertNull(response.drinks)
    }

    @Test
    fun wellFormedPayloadStillParsesCompletely() {
        val payload = """
            {"drinks":[{
                "idDrink":"11007","strDrink":"Margarita","strCategory":"Ordinary Drink",
                "strAlcoholic":"Alcoholic","strGlass":"Cocktail glass",
                "strInstructions":"Shake with ice.","strDrinkThumb":"https://example.com/m.jpg",
                "strIngredient1":"Tequila","strMeasure1":"1 1/2 oz"
            }]}
        """.trimIndent()

        val drink = json.decodeFromString<CocktailResponse>(payload).drinks!!.single()

        assertEquals("11007", drink.id)
        assertEquals("Margarita", drink.name)
        assertEquals("Tequila", drink.getIngredients().single().name)
    }
}
