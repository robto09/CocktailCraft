package com.cocktailcraft.di

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailApiImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.util.NetworkMonitor
import io.ktor.client.*
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Retry predicate for the single retry/backoff layer (SH-7): transient server
 * errors and TheCocktailDB's 429 rate limit are retryable; ordinary client
 * errors are not.
 */
internal fun shouldRetryResponse(statusCode: Int): Boolean =
    statusCode in 500..599 || statusCode == 429

/**
 * Retry predicate for transport failures. Retried: a dropped or reset
 * connection, which a second attempt often gets through. Not retried:
 * - timeouts, which already used up their HttpTimeout budget;
 * - an unresolvable or unreachable host (no network, DNS failure, refused
 *   connection; see [isUnreachableHostError]), which fails the same way a
 *   moment later — retrying only delays the error or the cached fallback.
 */
internal fun shouldRetryException(cause: Throwable): Boolean = when {
    cause is ResponseException -> shouldRetryResponse(cause.response.status.value)
    cause is CancellationException -> false
    cause is HttpRequestTimeoutException ||
        cause is ConnectTimeoutException ||
        cause is SocketTimeoutException -> false
    cause is UnresolvedAddressException || isUnreachableHostError(cause) -> false
    else -> true
}

/**
 * True for the platform engine's "no route to the server" failures: unknown
 * host, refused connection, no network (OkHttp's java.net exceptions on
 * Android, NSURLError codes on iOS).
 */
internal expect fun isUnreachableHostError(cause: Throwable): Boolean

/** Upper bound for establishing a connection (TCP + TLS). */
internal const val CONNECT_TIMEOUT_MS = 5_000L

/**
 * Backoff before a retry: [BASE_DELAY_MS] doubling per attempt (capped at
 * [MAX_DELAY_MS]) plus up to [JITTER_MS] of jitter. With
 * AppConfig.maxRetries = 1 the only retry waits 300-500 ms. HttpTimeout's
 * request budget (AppConfig.networkTimeoutMs, 10 seconds) spans every
 * attempt and wait of a call, so a failing request surfaces within it.
 */
internal object RetryBackoff {
    const val BASE_DELAY_MS = 300L
    const val JITTER_MS = 200L
    const val MAX_DELAY_MS = 1_000L
}

/**
 * Builds the shared [HttpClient] with the full production plugin stack.
 * Extracted from the Koin single (AR-5) so tests can inject a MockEngine while
 * exercising the exact same configuration; [engine] = null (production) lets
 * Ktor pick the platform default engine.
 */
internal fun buildHttpClient(
    json: Json,
    config: AppConfig,
    engine: HttpClientEngine? = null
): HttpClient {
    val configuration: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            // Verbose wire logging is opt-in via AppConfig, not always-on
            level = if (config.verboseNetworkLogging) LogLevel.ALL else LogLevel.NONE
        }

        install(io.ktor.client.plugins.HttpTimeout) {
            // Fail fast on an unreachable host; the whole request and each
            // socket read get AppConfig's budget.
            connectTimeoutMillis = minOf(CONNECT_TIMEOUT_MS, config.networkTimeoutMs)
            requestTimeoutMillis = config.networkTimeoutMs
            socketTimeoutMillis = config.networkTimeoutMs
        }

        install(io.ktor.client.plugins.DefaultRequest) {
            // Add default headers for consistency
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        // The single retry/backoff layer (SH-7): the manual per-endpoint
        // exponential backoff in CocktailRemoteDataSource was deleted in
        // its favor. Covers transport exceptions, 5xx, and 429 —
        // exponentialDelay() honors a 429's Retry-After header. Because
        // expectSuccess=true can surface retryable statuses as
        // ResponseExceptions, both predicates apply the same status rule.
        install(io.ktor.client.plugins.HttpRequestRetry) {
            val maxRetries = config.maxRetries
            retryIf(maxRetries = maxRetries) { _, response ->
                shouldRetryResponse(response.status.value)
            }
            retryOnExceptionIf(maxRetries = maxRetries) { _, cause -> shouldRetryException(cause) }
            exponentialDelay(
                baseDelayMs = RetryBackoff.BASE_DELAY_MS,
                maxDelayMs = RetryBackoff.MAX_DELAY_MS,
                randomizationMs = RetryBackoff.JITTER_MS
            )
            modifyRequest { request ->
                // Log retry attempts - access retryCount from the context
                Logger.d { "Retrying request to ${request.url} (attempt #${retryCount})" }
            }
        }

        // Non-2xx responses throw Ktor's typed exceptions
        // (ClientRequestException for 4xx, ServerResponseException for 5xx),
        // which ErrorHandler classifies into user-friendly errors.
        expectSuccess = true

        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                // Ktor's messages embed the full request URL (search
                // queries), so release builds — whose floor is Warn — get
                // only the status or exception type; the detail is debug-only.
                val status = (exception as? ResponseException)?.response?.status?.value
                Logger.w { "Network error: ${status?.let { "HTTP $it" } ?: exception::class.simpleName}" }
                Logger.d { "Network error detail: ${exception.message}" }
            }
        }
    }
    return engine?.let { HttpClient(it, configuration) } ?: HttpClient(configuration)
}

/**
 * Koin module for network-related dependencies. Public (was internal) so
 * :shared can assemble it into appModule; its builders stay internal.
 */
val networkModule = module {
    // HTTP Client
    single {
        // Android binds an OkHttp engine over the app-wide OkHttpClient
        // (dataPlatformModule); elsewhere Ktor picks the platform default.
        buildHttpClient(json = get(), config = get(), engine = getOrNull())
    }

    // API
    single<CocktailApi> { CocktailApiImpl(client = get(), appConfig = get(), json = get()) }
}
