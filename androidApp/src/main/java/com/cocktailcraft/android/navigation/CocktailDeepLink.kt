package com.cocktailcraft.android.navigation

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

// TheCocktailDB ids are short decimal numbers (e.g. "11007"). The cocktail
// detail deep link can be fired by other apps, so anything else in the id
// slot is dropped before it reaches the detail screen. ASCII digits only
// ([0-9], not \d) to match iOS's WidgetDeepLink exactly.
private val COCKTAIL_ID_PATTERN = Regex("^[0-9]{1,10}$")

/** True when [cocktailId] is a well-formed TheCocktailDB cocktail id. */
fun isValidCocktailId(cocktailId: String): Boolean = COCKTAIL_ID_PATTERN.matches(cocktailId)

/**
 * The cocktail detail deep link, `cocktailcraft://cocktail/{id}` — the same
 * URL the iOS widgets open (WidgetDeepLink in WidgetBridge.swift). The
 * widgets fire it; the VIEW intent filter in AndroidManifest.xml must stay in
 * step with [SCHEME] and [HOST] (the manifest can't reference them).
 */
object CocktailDeepLink {
    const val SCHEME = "cocktailcraft"
    const val HOST = "cocktail"

    /** navDeepLink base for [CocktailDetailRoute]; the id is the final path segment. */
    const val BASE_PATH = "$SCHEME://$HOST"

    /** The deep link that opens [cocktailId]'s detail screen. */
    fun uriFor(cocktailId: String): Uri = "$BASE_PATH/${Uri.encode(cocktailId)}".toUri()

    /**
     * The cocktail id of a cocktail deep link with a well-formed id as its
     * only path segment, or null for any other URI.
     */
    fun cocktailId(uri: Uri?): String? {
        if (uri == null || uri.scheme != SCHEME || uri.host != HOST) return null
        return uri.pathSegments.singleOrNull()?.takeIf(::isValidCocktailId)
    }

    /**
     * What MainActivity lets Navigation see of an incoming [intent].
     *
     * MainActivity is exported, and NavController.handleDeepLink acts not
     * only on the intent's URI but also on Navigation's own internal extras
     * (the destination ids and arguments it puts on the intents it builds),
     * which would let any app open any destination — an order's detail, say.
     * So only a well-formed cocktail deep link gets through, rebuilt as a
     * bare VIEW intent: canonical URI, no extras, no flags (a widget's
     * framework-forced FLAG_ACTIVITY_NEW_TASK would otherwise make
     * handleDeepLink restart the activity instead of navigating in place).
     * Null for everything else, including the launcher's MAIN intent.
     */
    fun navigationIntentOrNull(intent: Intent?): Intent? =
        cocktailId(intent?.data)?.let { Intent(Intent.ACTION_VIEW, uriFor(it)) }
}
