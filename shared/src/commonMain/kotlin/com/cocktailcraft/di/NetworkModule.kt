package com.cocktailcraft.di

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailApiImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.util.NetworkMonitor
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Koin module for network-related dependencies.
 */
internal val networkModule = module {
    // HTTP Client
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get<Json>())
            }
            install(Logging) {
                // Verbose wire logging is opt-in via AppConfig, not always-on
                level = if (get<AppConfig>().verboseNetworkLogging) LogLevel.ALL else LogLevel.NONE
            }

            install(io.ktor.client.plugins.HttpTimeout) {
                val config = get<AppConfig>()
                connectTimeoutMillis = config.networkTimeoutMs
                requestTimeoutMillis = config.networkTimeoutMs
                socketTimeoutMillis = config.networkTimeoutMs
            }

            install(io.ktor.client.plugins.DefaultRequest) {
                // Add default headers for consistency
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }

            // Add HttpRequestRetry for automatic retries
            install(io.ktor.client.plugins.HttpRequestRetry) {
                retryOnExceptionOrServerErrors(maxRetries = 3)
                exponentialDelay()
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
                    Logger.w { "Network error: ${exception.message}" }
                }
            }

            engine {
                // Engine-specific config
            }
        }
    }

    // API
    single<CocktailApi> { CocktailApiImpl(client = get(), appConfig = get()) }
}
