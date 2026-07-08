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
        assertTrue(AuthInputValidator.isValidEmail("user+tag@sub.domain.org"))
        assertTrue(AuthInputValidator.isValidEmail("x_1%y@my-host.io"))
        assertFalse(AuthInputValidator.isValidEmail(""))
        assertFalse(AuthInputValidator.isValidEmail("   "))
        assertFalse(AuthInputValidator.isValidEmail("no-at-sign.com"))
        assertFalse(AuthInputValidator.isValidEmail("no@dots"))
        assertFalse(AuthInputValidator.isValidEmail("a@b."))
        assertFalse(AuthInputValidator.isValidEmail("a@.b"))          // empty domain label
        assertFalse(AuthInputValidator.isValidEmail("@b.co"))         // missing local part
        assertFalse(AuthInputValidator.isValidEmail("a@"))            // missing domain
        assertFalse(AuthInputValidator.isValidEmail("a b@c.de"))      // whitespace
        assertFalse(AuthInputValidator.isValidEmail("a@b..co"))       // empty middle label
        assertFalse(AuthInputValidator.isValidEmail("a@-b.co"))       // label starts with hyphen
        assertFalse(AuthInputValidator.isValidEmail("a@b.c"))         // one-char TLD
        assertFalse(AuthInputValidator.isValidEmail("a@b.co "))       // trailing space
    }

    @Test
    fun passwordLengthBoundary() {
        assertFalse(AuthInputValidator.isValidPassword("1234567"))
        assertTrue(AuthInputValidator.isValidPassword("12345678"))
    }

    @Test
    fun passwordPolicyRequiresLengthAndStrength() {
        // Long enough but a single character class (strength 2) fails.
        assertFalse(AuthInputValidator.meetsPasswordPolicy("12345678"))
        assertFalse(AuthInputValidator.meetsPasswordPolicy("abcdefgh"))
        // Strong but short fails.
        assertFalse(AuthInputValidator.meetsPasswordPolicy("Ab1!"))
        // Length plus two character classes passes.
        assertTrue(AuthInputValidator.meetsPasswordPolicy("Abcdefgh"))
        assertTrue(AuthInputValidator.meetsPasswordPolicy("abcd1234"))
        assertTrue(AuthInputValidator.meetsPasswordPolicy("Abcdef1!"))
    }

    @Test
    fun passwordStrengthScoring() {
        assertEquals(0, AuthInputValidator.passwordStrength(""))
        assertEquals(2, AuthInputValidator.passwordStrength("abc1"))       // lower + digit
        assertEquals(3, AuthInputValidator.passwordStrength("abcdefg1"))   // + length
        assertEquals(5, AuthInputValidator.passwordStrength("Abcdef1!"))   // all criteria
    }
}
