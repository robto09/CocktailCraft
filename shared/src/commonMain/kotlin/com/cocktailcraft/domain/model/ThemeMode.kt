package com.cocktailcraft.domain.model

/**
 * The single source of truth for theme selection.
 * SKIE exports this as a native Swift enum.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromString(value: String): ThemeMode = when (value.lowercase()) {
            "light" -> LIGHT
            "dark" -> DARK
            else -> SYSTEM
        }
    }
}
