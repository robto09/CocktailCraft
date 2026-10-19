package com.cocktailcraft.data.local

import com.cocktailcraft.domain.model.Cocktail

/**
 * Decodes the stored cocktails of one observed query, reusing the previous
 * emission's result for every row whose payload and flag haven't changed.
 *
 * Queries that read cached_cocktail (a category page, the favorites, the
 * cart) re-run on every write to it, and a page refresh rewrites a whole
 * page in one go: without this each re-run decoded every payload again,
 * though usually only a few rows changed. Comparing payload strings costs
 * far less than decoding them.
 *
 * One instance per collector, used from one coroutine: it is not thread-safe.
 */
internal class CocktailDecodeMemo(private val codec: CocktailCodec) {

    private class Decoded(val payload: String, val hasFullDetails: Boolean, val cocktail: Cocktail?)

    private var previous = HashMap<String, Decoded>()
    private var current = HashMap<String, Decoded>()

    /** The cocktail stored as [payload] for [id] (null when unreadable). */
    fun decode(id: String, payload: String, hasFullDetails: Boolean): Cocktail? {
        val decoded = previous[id]?.takeIf { it.payload == payload && it.hasFullDetails == hasFullDetails }
            ?: Decoded(payload, hasFullDetails, codec.decodeOrNull(payload, hasFullDetails))
        current[id] = decoded
        return decoded.cocktail
    }

    /** Decodes one emission's rows with [decodeRows], then forgets the rows it no longer lists. */
    fun <R> emission(decodeRows: CocktailDecodeMemo.() -> R): R =
        decodeRows().also {
            previous = current
            current = HashMap()
        }
}
