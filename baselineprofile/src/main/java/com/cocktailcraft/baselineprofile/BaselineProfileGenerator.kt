package com.cocktailcraft.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates androidApp's baseline profile (run via
 * `./gradlew :androidApp:generateBaselineProfile`).
 *
 * The journey covers cold startup plus the bottom tabs so the classes on
 * those paths get ahead-of-time compiled on install.
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
            device.wait(Until.hasObject(By.text("My Bar")), 10_000)

            // Visit each bottom tab to profile their composition paths
            for (tab in listOf("Cart", "Favorites", "Orders", "Profile", "Home")) {
                device.findObject(By.text(tab))?.click()
                device.waitForIdle()
            }
        }
    }
}
