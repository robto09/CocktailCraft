package com.cocktailcraft.di

import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailApiImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.util.NetworkMonitor
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.header
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Koin module for network-related dependencies.
 */
val networkModule = module {
    // HTTP Client
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get<Json>())
            }
            install(Logging) {
                level = LogLevel.ALL
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
                    println("Retrying request to ${request.url} (attempt #${retryCount})")
                }
            }

            // Add error handling
            HttpResponseValidator {
                validateResponse { response ->
                    val statusCode = response.status.value
                    if (statusCode >= 400) {
                        when (statusCode) {
                            in 400..499 -> throw ConnectTimeoutException(
                                "Client error: ${response.status.description}", Throwable("Client error")
                            )
                            in 500..599 -> throw ServerResponseException(
                                response, "Server error: ${response.status.description}"
                            )
                        }
                    }
                }

                handleResponseExceptionWithRequest { exception, _ ->
                    println("Network error: ${exception.message}")
                }
            }

            engine {
                // Engine-specific config
            }
        }
    }

    // API
    single<CocktailApi> { CocktailApiImpl(get()) }

    // Network monitoring
    single { NetworkMonitor(get()) }
}
