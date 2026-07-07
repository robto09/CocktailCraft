package com.cocktailcraft.domain.model

/**
 * Search filters for cocktails, limited to the dimensions TheCocktailDB free
 * tier actually supports (name, category, single ingredient, alcoholic, glass).
 */
data class SearchFilters(
    val query: String = "",
    val category: String? = null,
    val ingredient: String? = null,   // single ingredient
    val alcoholic: Boolean? = null,   // null = any
    val glass: String? = null,
) {
    // Helper function to check if any filters are active
    fun hasActiveFilters(): Boolean =
        category != null || ingredient != null || alcoholic != null || glass != null
}
