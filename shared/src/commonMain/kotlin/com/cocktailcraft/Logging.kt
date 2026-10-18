package com.cocktailcraft

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import kotlin.native.HiddenFromObjC

/**
 * Sets the process-wide Kermit floor for every shared module (they all log
 * through the global [Logger]). Debug builds keep every level; release builds
 * keep only warnings and errors, so debug traces such as retried request URLs
 * (which carry search queries) and order IDs never reach logcat or the
 * unified log. Kermit's lambda overloads skip building the message entirely
 * below the floor.
 *
 * Runs first thing in [initKoin]; each app passes its own build flag
 * (BuildConfig.DEBUG on Android, `#if DEBUG` on iOS).
 */
@HiddenFromObjC // Kotlin-side wiring; Swift only calls doInitKoin()/KoinHelper (AR-12)
fun configureLogging(isDebug: Boolean) {
    Logger.setMinSeverity(minLogSeverity(isDebug))
}

internal fun minLogSeverity(isDebug: Boolean): Severity =
    if (isDebug) Severity.Verbose else Severity.Warn
