package com.cocktailcraft.domain.util

/**
 * Pure validation rules for credential input, shared by every screen that
 * collects them. Lives outside the ViewModels so the rules are testable on
 * their own and stay identical across platforms.
 */
object AuthInputValidator {

    // NIST SP 800-63B floor. Raising this only affects newly set passwords:
    // sign-in never re-validates stored credentials against current policy.
    const val MIN_PASSWORD_LENGTH = 8

    // Minimum passwordStrength() score for a newly set password. Length ≥ 8
    // contributes one point, so this effectively demands two character
    // classes on top of the length requirement.
    const val MIN_PASSWORD_STRENGTH = 3

    // RFC-5322-lite: dot-atom local part, then dot-separated domain labels
    // that never start or end with a hyphen, ending in an alphabetic TLD of
    // 2+ chars. Whitespace can't match anywhere. Full RFC 5322 is deliberately
    // out of scope — this is a storage-key sanity gate, not an RFC parser.
    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"
    )

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    /** The gate for newly set passwords (sign-up, change password). */
    fun meetsPasswordPolicy(password: String): Boolean =
        isValidPassword(password) && passwordStrength(password) >= MIN_PASSWORD_STRENGTH

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
