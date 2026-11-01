package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_message_network
import com.cocktailcraft.resources.error_update_favorites_failed
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.util.ErrorHandler
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * getOrReport and handleException report through one mapping
 * (ErrorHandler.errorFromResult), so a failure reads the same whichever
 * path a ViewModel takes — and raw data-layer text never reaches the user.
 */
class SharedViewModelErrorReportingTest : MainDispatcherTest() {

    private class ReportingViewModel : SharedViewModel() {
        fun report(result: Result<Unit>) {
            result.getOrReport(Unit, MR.strings.error_update_favorites_failed)
        }

        fun handle(exception: Exception) {
            handleException(exception, MR.strings.error_update_favorites_failed)
        }
    }

    private fun reported(result: Result.Error): ErrorHandler.UserFriendlyError {
        val vm = ReportingViewModel()
        vm.report(result)
        return assertNotNull(vm.error.value)
    }

    private fun handled(result: Result.Error): ErrorHandler.UserFriendlyError {
        val vm = ReportingViewModel()
        vm.handle(DomainException(result.code, result.message, result.retryAfterMs))
        return assertNotNull(vm.error.value)
    }

    @Test
    fun getOrReportShowsTheLocalizedFallbackNotTheRawDataLayerMessage() {
        val error = reported(Result.Error("Failed to update favorites"))

        assertEquals(MR.strings.error_update_favorites_failed.desc(), error.message)
    }

    @Test
    fun getOrReportWordsALockoutFromItsRetryAfter() {
        val error = reported(Result.Error("locked out", ErrorCode.FORBIDDEN, retryAfterMs = 30_000))

        assertEquals(ErrorHandler.lockoutMessage(30_000), error.message)
        assertEquals(ErrorCode.FORBIDDEN, error.errorCode)
    }

    @Test
    fun getOrReportShowsSharedCopyForATransportFailure() {
        val ktorText = "Unable to resolve host [url=https://example.com/search.php?s=gin]"

        val error = reported(Result.Error(ktorText, ErrorCode.NETWORK))

        assertEquals(MR.strings.error_message_network.desc(), error.message)
    }

    @Test
    fun getOrReportAndHandleExceptionAgree() {
        val cases = listOf(
            Result.Error("raw"),
            Result.Error("raw", ErrorCode.TIMEOUT),
            Result.Error("raw", ErrorCode.FORBIDDEN, retryAfterMs = 90_000)
        )

        cases.forEach { result ->
            val viaReport = reported(result)
            val viaException = handled(result)
            assertEquals(viaException.message, viaReport.message)
            assertEquals(viaException.title, viaReport.title)
            assertEquals(viaException.errorCode, viaReport.errorCode)
        }
    }
}
