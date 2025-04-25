package com.cocktailcraft.di

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailApiImpl
import com.cocktailcraft.data.repository.AuthRepositoryImpl
import com.cocktailcraft.data.repository.CartRepositoryImpl
import com.cocktailcraft.data.repository.CocktailRepositoryImpl
import com.cocktailcraft.data.repository.OrderRepositoryImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.usecase.ToggleFavoriteUseCase
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.Settings
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
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule = module {
    // Config
    single<AppConfig> { AppConfigImpl() }

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

    // JSON
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    // Network monitoring
    single { NetworkMonitor(get()) }

    // Cache
    single { CocktailCache(get(), get(), get()) }

    // Repositories
    single<CocktailRepository> {
        CocktailRepositoryImpl(
            api = get(),
            settings = get(),
            appConfig = get(),
            json = get(),
            networkMonitor = get()
        )
    }

    single<CartRepository> {
        CartRepositoryImpl(get(), get())
    }

    single<AuthRepository> {
        AuthRepositoryImpl(get(), get())
    }

    single<OrderRepository> {
        OrderRepositoryImpl(
            settings = get(),
            json = get(),
            appConfig = get()
        )
    }

    // Use Cases
    factory { PlaceOrderUseCase(orderRepository = get()) }
    factory { ToggleFavoriteUseCase(cocktailRepository = get()) }
}
// The platformModule function is declared in PlatformModule.kt