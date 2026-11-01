package com.cocktailcraft.domain.model

/**
 * Every review of one cocktail on the device, whoever wrote it: their
 * average [rating] (1–5) and how many there are ([count] > 0; cocktails
 * without reviews have no summary).
 */
data class CocktailRatingSummary(val rating: Float, val count: Int)
