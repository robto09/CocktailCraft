package com.cocktailcraft.android.ui.theme

import android.app.Application
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.cocktailcraft.android.R
import com.cocktailcraft.designsystem.ColorTokens
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/**
 * res/values/colors.xml copies the brand colors by hand for the pre-Compose
 * window (cold start, status bar), which cannot read the shared
 * [ColorTokens]. Pins every copy to its token so a brand color change can't
 * miss it. The iOS widget's copy has the same check (BrandColorParityTests).
 */
@RunWith(RobolectricTestRunner::class)
// Plain Application: the real one starts Koin, network monitoring and WorkManager
@Config(application = Application::class)
class BrandColorParityTest {

    private val mirrors: Map<String, Pair<Int, Long>> = mapOf(
        "cocktail_primary" to (R.color.cocktail_primary to ColorTokens.primaryLight),
        "cocktail_primary_dark" to (R.color.cocktail_primary_dark to ColorTokens.primaryDark),
        "cocktail_secondary" to (R.color.cocktail_secondary to ColorTokens.secondaryLight),
        "cocktail_secondary_dark" to (R.color.cocktail_secondary_dark to ColorTokens.secondaryDark),
        "background_light" to (R.color.background_light to ColorTokens.backgroundLight),
        "background_dark" to (R.color.background_dark to ColorTokens.backgroundDark),
        "text_primary_light" to (R.color.text_primary_light to ColorTokens.textPrimaryLight),
        "text_primary_dark" to (R.color.text_primary_dark to ColorTokens.textPrimaryDark),
        "text_secondary_light" to (R.color.text_secondary_light to ColorTokens.textSecondaryLight),
        "text_secondary_dark" to (R.color.text_secondary_dark to ColorTokens.textSecondaryDark)
    )

    @Test
    fun everyColorResourceMatchesItsDesignToken() {
        mirrors.forEach { (name, mirror) ->
            val (resource, token) = mirror
            assertEquals(hex(token.toInt()), hex(color(resource)), "R.color.$name drifted from its ColorTokens value")
        }
    }

    private fun color(@ColorRes resource: Int): Int =
        ContextCompat.getColor(RuntimeEnvironment.getApplication(), resource)

    private fun hex(argb: Int): String = "#%08X".format(argb)
}
