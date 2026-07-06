package com.cocktailcraft.domain.util

/**
 * Pure validation rules for credential input, shared by every screen that
 * collects them. Lives outside the ViewModels so the rules are testable on
 * their own and stay identical across platforms.
 */
object AuthInputValidator {

    const val MIN_PASSWORD_LENGTH = 6

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() &&
               email.contains("@") &&
               email.contains(".") &&
               email.length >= 5
    }

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    /** 0–5 score: length ≥ 8, uppercase, lowercase, digit, symbol. */
    fun passwordStrength(password: String): Int {
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score
    }
}
