package com.cocktailcraft.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates androidApp's baseline profile (run via
 * `./gradlew :androidApp:generateBaselineProfile`).
 *
 * The journey covers cold startup, scrolling the home list, opening a
 * detail screen, searching, and the bottom tabs so the classes on those
 * paths get ahead-of-time compiled on install.
 *
 * All node matching uses testTags surfaced as resource-ids
 * (testTagsAsResourceId in MainActivity) so the journey is stable across
 * copy changes and non-English locales — no display-text matching.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(packageName = "com.cocktailcraft.android") {
            pressHome()
            startActivityAndWait()

            // Home content (network may be unavailable on the managed
            // device — the timeout keeps generation resilient either way)
            val listSelector = By.res(packageName, "home_cocktail_list")
            device.wait(Until.hasObject(listSelector), 10_000)

            // Scroll the list down and back up to profile item
            // composition, image loading and recycling paths
            device.findObject(listSelector)?.let { list ->
                list.setGestureMargin(device.displayWidth / 5)
                list.scroll(Direction.DOWN, 1.0f)
                device.waitForIdle()
                list.scroll(Direction.UP, 1.0f)
                device.waitForIdle()
            }

            // Open a detail screen (any row — they all share the tag),
            // then return home
            device.findObject(By.res(packageName, "cocktail_list_item"))?.let { item ->
                item.click()
                device.wait(
                    Until.hasObject(By.res(packageName, "cocktail_detail_screen")),
                    10_000
                )
                device.pressBack()
                device.wait(Until.hasObject(listSelector), 10_000)
            }

            // Search to profile the query/filter path
            device.findObject(By.res(packageName, "home_search_field"))?.let { field ->
                field.click()
                device.waitForIdle()
                field.text = "Margarita"
                device.waitForIdle()
            }

            // Visit each bottom tab to profile their composition paths
            for (tab in listOf("nav_cart", "nav_favorites", "nav_orders", "nav_profile", "nav_home")) {
                device.findObject(By.res(packageName, tab))?.click()
                device.waitForIdle()
            }
        }
    }
}
