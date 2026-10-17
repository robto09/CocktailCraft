package com.cocktailcraft.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CocktailTest {

    private fun cocktail(alcoholic: String?) = Cocktail(id = "1", name = "Margarita", alcoholic = alcoholic)

    @Test
    fun knownAlcoholicKeepsApiValues() {
        assertEquals("Alcoholic", cocktail("Alcoholic").knownAlcoholic)
        assertEquals("Non alcoholic", cocktail("Non alcoholic").knownAlcoholic)
    }

    @Test
    fun knownAlcoholicDropsMissingAndListPlaceholderValues() {
        assertNull(cocktail(null).knownAlcoholic)
        assertNull(cocktail("").knownAlcoholic)
        assertNull(cocktail(Cocktail.ALCOHOLIC_UNKNOWN).knownAlcoholic)
    }
}
