package com.cocktailcraft.domain

import com.cocktailcraft.domain.util.AuthInputValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthInputValidatorTest {

    @Test
    fun emailValidation() {
        assertTrue(AuthInputValidator.isValidEmail("a@b.co"))
        assertTrue(AuthInputValidator.isValidEmail("robert.torres@example.com"))
        assertFalse(AuthInputValidator.isValidEmail(""))
        assertFalse(AuthInputValidator.isValidEmail("   "))
        assertFalse(AuthInputValidator.isValidEmail("no-at-sign.com"))
        assertFalse(AuthInputValidator.isValidEmail("no@dots"))
        assertFalse(AuthInputValidator.isValidEmail("a@b.")) // shorter than 5 chars
    }

    @Test
    fun passwordLengthBoundary() {
        assertFalse(AuthInputValidator.isValidPassword("12345"))
        assertTrue(AuthInputValidator.isValidPassword("123456"))
    }

    @Test
    fun passwordStrengthScoring() {
        assertEquals(0, AuthInputValidator.passwordStrength(""))
        assertEquals(2, AuthInputValidator.passwordStrength("abc1"))       // lower + digit
        assertEquals(3, AuthInputValidator.passwordStrength("abcdefg1"))   // + length
        assertEquals(5, AuthInputValidator.passwordStrength("Abcdef1!"))   // all criteria
    }
}
