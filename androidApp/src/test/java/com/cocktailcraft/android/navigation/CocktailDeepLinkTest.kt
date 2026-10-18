package com.cocktailcraft.android.navigation

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CocktailDeepLinkTest {

    @Test
    fun `numeric TheCocktailDB ids are accepted`() {
        assertTrue(isValidCocktailId("11007"))
        assertTrue(isValidCocktailId("1"))
        assertTrue(isValidCocktailId("1234567890"))
    }

    @Test
    fun `empty, overlong and non-numeric ids are rejected`() {
        listOf(
            "",
            "12345678901", // 11 digits
            "abc",
            "11007a",
            " 11007",
            "11007\n",
            "../../profile",
            "11007/extra",
            "-1",
            "１２３", // full-width digits
            "%31%31"
        ).forEach { id ->
            assertFalse(isValidCocktailId(id), "expected \"$id\" to be rejected")
        }
    }
}
