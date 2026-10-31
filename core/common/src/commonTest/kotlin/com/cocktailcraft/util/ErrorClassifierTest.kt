package com.cocktailcraft.util

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.ErrorCode
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorClassifierTest {

    @Test
    fun classifiesByExceptionType() {
        assertEquals(ErrorClassifier.Kind.NETWORK, ErrorClassifier.classify(IOException("socket closed")))
        assertEquals(
            ErrorClassifier.Kind.TIMEOUT,
            ErrorClassifier.classify(HttpRequestTimeoutException("https://example.com/api", 5_000L))
        )
        assertEquals(ErrorClassifier.Kind.INVALID_PAYLOAD, ErrorClassifier.classify(SerializationException("bad")))
        assertEquals(ErrorClassifier.Kind.INVALID_DATA, ErrorClassifier.classify(IllegalArgumentException("bad id")))
        assertEquals(ErrorClassifier.Kind.SECURITY, ErrorClassifier.classify(RuntimeException("Permission denied")))
        assertEquals(ErrorClassifier.Kind.UNKNOWN, ErrorClassifier.classify(RuntimeException("boom")))
    }

    @Test
    fun doesNotGuessFromMessageSubstrings() {
        assertEquals(
            ErrorClassifier.Kind.UNKNOWN,
            ErrorClassifier.classify(RuntimeException("Connection timeout while talking to server 500"))
        )
    }

    @Test
    fun codeForKeepsADomainExceptionsOwnCode() {
        assertEquals(ErrorCode.NOT_FOUND, ErrorClassifier.codeFor(DomainException(ErrorCode.NOT_FOUND, "gone")))
        assertEquals(ErrorCode.INVALID_DATA, ErrorClassifier.codeFor(SerializationException("bad")))
    }
}
