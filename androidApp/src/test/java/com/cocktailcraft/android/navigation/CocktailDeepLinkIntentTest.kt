package com.cocktailcraft.android.navigation

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import com.cocktailcraft.android.MainActivity
import com.cocktailcraft.android.widget.cocktailDetailIntent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * What MainActivity (exported) lets through to Navigation: only well-formed
 * cocktail deep links, rebuilt without the sender's extras and flags.
 */
@RunWith(RobolectricTestRunner::class)
// Plain Application: the real one starts Koin, network monitoring and WorkManager
@Config(application = Application::class)
class CocktailDeepLinkIntentTest {

    private fun viewIntent(uri: String) = Intent(Intent.ACTION_VIEW, uri.toUri())

    @Test
    fun cocktailDeepLinkIsForwardedAsABareViewIntent() {
        val incoming = viewIntent("cocktailcraft://cocktail/11007").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("extra", "value")
        }

        val forwarded = CocktailDeepLink.navigationIntentOrNull(incoming)!!

        assertEquals(Intent.ACTION_VIEW, forwarded.action)
        assertEquals("cocktailcraft://cocktail/11007", forwarded.dataString)
        assertNull(forwarded.extras)
        assertEquals(0, forwarded.flags)
    }

    @Test
    fun widgetIntentsRoundTrip() {
        val widgetIntent = cocktailDetailIntent(RuntimeEnvironment.getApplication(), "11007")

        assertEquals(MainActivity::class.java.name, widgetIntent.component?.className)
        assertEquals(
            "cocktailcraft://cocktail/11007",
            CocktailDeepLink.navigationIntentOrNull(widgetIntent)?.dataString
        )
    }

    @Test
    fun legacyWidgetLinkIsRewrittenToTheCurrentForm() {
        val forwarded = CocktailDeepLink.navigationIntentOrNull(viewIntent("app://cocktailcraft/cocktail/11007"))

        assertEquals("cocktailcraft://cocktail/11007", forwarded?.dataString)
    }

    @Test
    fun navigationInternalExtrasWithoutACocktailLinkAreDropped() {
        // The extras NavDeepLinkBuilder puts on its intents: destination ids
        // to open (here an arbitrary one) and their arguments
        val incoming = Intent(Intent.ACTION_MAIN).apply {
            putExtra("android-support-nav:controller:deepLinkIds", intArrayOf(1, 2))
            putExtra("android-support-nav:controller:deepLinkArgs", arrayListOf(Bundle()))
            putExtra("android-support-nav:controller:deepLinkExtras", Bundle())
        }

        assertNull(CocktailDeepLink.navigationIntentOrNull(incoming))
    }

    @Test
    fun navigationInternalExtrasOnACocktailLinkAreStripped() {
        val incoming = viewIntent("cocktailcraft://cocktail/11007").apply {
            putExtra("android-support-nav:controller:deepLinkIds", intArrayOf(1, 2))
        }

        val forwarded = CocktailDeepLink.navigationIntentOrNull(incoming)!!

        assertTrue(forwarded.extras?.isEmpty ?: true)
    }

    @Test
    fun anythingElseIsNotForwarded() {
        assertNull(CocktailDeepLink.navigationIntentOrNull(null))
        assertNull(CocktailDeepLink.navigationIntentOrNull(Intent(Intent.ACTION_MAIN)))
        listOf(
            "cocktailcraft://cocktail",
            "cocktailcraft://cocktail/",
            "cocktailcraft://cocktail/abc",
            "cocktailcraft://cocktail/12345678901",
            "cocktailcraft://cocktail/11007/extra",
            "cocktailcraft://order/11007",
            "cocktailcraft://orders/1",
            "app://cocktailcraft/order/11007",
            "app://cocktailcraft/11007",
            "app://other/cocktail/11007",
            "https://cocktail/11007",
            "other://cocktail/11007"
        ).forEach { uri ->
            assertNull(CocktailDeepLink.navigationIntentOrNull(viewIntent(uri)), "expected $uri to be dropped")
        }
    }
}
