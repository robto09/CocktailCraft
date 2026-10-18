package com.cocktailcraft

import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals

class LoggingTest {

    @Test
    fun releaseBuildsKeepOnlyWarningsAndErrors() {
        assertEquals(Severity.Warn, minLogSeverity(isDebug = false))
    }

    @Test
    fun debugBuildsKeepEveryLevel() {
        assertEquals(Severity.Verbose, minLogSeverity(isDebug = true))
    }
}
