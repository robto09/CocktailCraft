# Dependency Injection in CocktailCraft

This document outlines the dependency injection (DI) approach used in the CocktailCraft app.

## Overview

CocktailCraft uses Koin for dependency injection. The DI setup is split into small, focused modules that are combined into a single `appModule` list, plus a platform module supplied per platform. The module source files in `shared/src/commonMain/kotlin/com/cocktailcraft/di/` are the source of truth; the excerpts below are representative, not exhaustive.

## Module Structure

### 1. App Module (`AppModule.kt`)

Combines the common modules:

```kotlin
val appModule = listOf(
    networkModule,
    dataModule,
    domainModule
)
// platformModule() is added separately at Koin startup (see PlatformModule.kt)
```

### 2. Network Module (`NetworkModule.kt`)

HTTP client, retry policy, and the API implementation:

```kotlin
internal val networkModule = module {
    // HTTP Client
    single {
        HttpClient {
            install(ContentNegotiation) { json(get<Json>()) }
            install(Logging) {
                // Verbose wire logging is opt-in via AppConfig, not always-on
                level = if (get<AppConfig>().verboseNetworkLogging) LogLevel.ALL else LogLevel.NONE
            }
            install(io.ktor.client.plugins.HttpTimeout) { /* timeouts from AppConfig */ }
            // The single retry/backoff layer (SH-7): retries 5xx and 429
            // with exponential delay; timeouts and 4xx are not retried.
            install(io.ktor.client.plugins.HttpRequestRetry) { /* see source */ }
            expectSuccess = true
        }
    }

    // API
    single<CocktailApi> { CocktailApiImpl(client = get(), appConfig = get()) }
}
```

### 3. Data Module (`DataModule.kt`)

JSON, the IO dispatcher, caching, the offline policy, and one binding per focused repository. The former monolithic `CocktailRepository`/`CocktailRepositoryImpl` was split into nine single-concern repositories:

```kotlin
val dataModule = module {
    // JSON
    single { Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true } }

    // Background dispatcher for Settings I/O and JSON (de)serialization
    single<CoroutineDispatcher>(named("ioDispatcher")) { Dispatchers.IO }

    // Offline decision — single source of truth for "is the app offline" (AR-7)
    single { OfflineModePolicy(settings = get(), appConfig = get(), networkMonitor = get(),
        ioDispatcher = get(named("ioDispatcher"))) }

    // Caches
    single { CocktailCache(/* settings, json, appConfig, ioDispatcher, offlineModePolicy */) }
    single { CocktailCacheManager() }

    // Remote data source: API access, DTO mapping, rate-limit bookkeeping
    single { CocktailRemoteDataSource(api = get(), cacheManager = get()) }

    // Focused repositories, each owning one concern
    single<CocktailOfflineRepository> { CocktailOfflineRepositoryImpl(/* ... */) }
    single<CocktailFavoritesRepository> { CocktailFavoritesRepositoryImpl(/* ... */) }

    // Category fetch-and-cache path shared by the search and catalog repositories
    single { CocktailCategoryFetcher(remote = get(), cocktailCache = get(),
        cacheManager = get(), offlineRepository = get()) }

    single<CocktailSearchRepository> { CocktailSearchRepositoryImpl(/* ... */) }
    single<CocktailDetailRepository> { CocktailDetailRepositoryImpl(/* ... */) }
    single<CocktailCatalogRepository> { CocktailCatalogRepositoryImpl(remote = get(), categoryFetcher = get()) }

    single<CartRepository> { CartRepositoryImpl(get(), get(), get(named("ioDispatcher"))) }
    single<AuthRepository> { AuthRepositoryImpl(get(secureSettingsQualifier), get(), get(named("ioDispatcher"))) }
    single<OrderRepository> { OrderRepositoryImpl(/* settings, json, appConfig, ioDispatcher */) }
    single<ReviewRepository> { ReviewRepositoryImpl(/* settings, json, appConfig, ioDispatcher */) }
}
```

Note the `secureSettingsQualifier` for `AuthRepositoryImpl`: auth data uses the platform-secure `Settings` instance (EncryptedSharedPreferences / Keychain) registered by the platform module (see `SettingsQualifiers.kt`).

### 4. Domain Module (`DomainModule.kt`)

Use cases (all `factory`), the shared ViewModels, and the background sync service. The `AppConfig` binding lives in each `platformModule()` (AR-4) — the platform knows whether this is a debug build; commonMain doesn't. A use case must earn its indirection (AR-8): pure 1:1 pass-throughs with a single consumer are collapsed onto the repository interface instead (`ManageOrdersUseCase` was removed this way).

```kotlin
val domainModule = module {
    // Use Cases — constructor-injected with the focused repository interfaces
    factory { SearchCocktailsUseCase(searchRepository = get()) }
    factory { LoadCocktailsByCategoryUseCase(searchRepository = get()) }
    factory { SortCocktailsUseCase() }
    factory { GetCocktailDetailUseCase(detailRepository = get(), favoritesRepository = get(), searchRepository = get()) }
    factory { ManageFavoritesUseCase(favoritesRepository = get(), detailRepository = get()) }
    factory { ManageCartUseCase(cartRepository = get(), detailRepository = get()) }
    factory { ManageOfflineModeUseCase(offlineRepository = get(), catalogRepository = get()) }
    factory { ManageProfileUseCase(authRepository = get()) }
    factory { ManageReviewsUseCase(repository = get()) }
    factory { AnalyzeCocktailUseCase() }
    factory { PlaceOrderUseCase(orderRepository = get()) }

    // Screen-scoped ViewModels — new instance per screen (Android's
    // koinViewModel() scopes them to the nav back-stack entry)
    viewModel { SharedCocktailDetailViewModel(/* use cases */) }
    viewModel { SharedReviewViewModel(manageReviewsUseCase = get()) }

    // Global-state ViewModels — `single`, shared across screens and platforms.
    // Because a Koin single is never routed through a ViewModelStore,
    // onCleared() never runs on them (SH-9); see DomainModule.kt for the
    // full lifecycle notes (including the AN-4 process-death caveat).
    single { SharedHomeViewModel(/* use cases + catalogRepository + networkMonitor */) }
    single { SharedCartViewModel(manageCartUseCase = get()) }
    single { SharedFavoritesViewModel(manageFavoritesUseCase = get()) }
    single { SharedOrderViewModel(orderRepository = get(), placeOrderUseCase = get()) }
    single { SharedProfileViewModel(manageProfileUseCase = get()) }
    single { SharedOfflineModeViewModel(manageOfflineModeUseCase = get(), networkMonitor = get()) }
    single { SharedThemeViewModel(manageProfileUseCase = get()) }

    // Cross-platform sync orchestration (schedulers are platform-side)
    single { BackgroundSyncService(catalogRepository = get(), networkMonitor = get()) }
}
```

### 5. Platform Module (`platformModule()`)

- `expect fun platformModule(): Module` in commonMain
- Android and iOS actuals provide platform-specific dependencies (Settings instances including the secure/encrypted one, `NetworkMonitor`, etc.)
- Located in `shared/src/androidMain/.../di/PlatformModule.kt` and `shared/src/iosMain/.../di/PlatformModule.kt`

## ViewModel Dependency Injection

ViewModels do **not** implement `KoinComponent` and do not use field injection. All shared ViewModels extend `SharedViewModel` (the multiplatform base class on androidx `ViewModel`) and receive their dependencies — use cases, occasionally a repository or `NetworkMonitor` — through their constructors. Koin wires them up in `DomainModule`:

- **Screen-scoped** (`viewModel { ... }`): resolved on Android with `koinViewModel<SharedCocktailDetailViewModel>()` inside the destination composable; on iOS via `KoinHelper`.
- **Global-state** (`single { ... }`): the same instance is shared across screens and handed to iOS through `KoinHelper`.

Constructor injection keeps ViewModels constructible without a Koin container, which is how the shared tests build them.

## Testing with Koin

There is no mock-based `testModule`. The shared test suite (`shared/src/commonTest`) uses:

- **Hand-written fakes** in `shared/src/commonTest/kotlin/com/cocktailcraft/testutil/Fakes.kt`, passed directly to constructors — most tests never start Koin.
- **`KoinDependencyGraphTest`** (`shared/src/commonTest/kotlin/com/cocktailcraft/di/`), which verifies the real module graph resolves, so DI wiring breaks fail in CI.

## Best Practices

1. **Separation of Concerns**: Keep modules focused on specific layers or functionality
2. **Constructor Injection**: All ViewModels and use cases take dependencies via constructor — no service-locator lookups in business code
3. **Interface-Based Injection**: Inject the focused repository interfaces (`CocktailSearchRepository`, `CartRepository`, ...), never implementations
4. **Minimal Dependencies**: Keep dependencies minimal and focused
5. **Documentation**: Document non-obvious dependencies and injection patterns (the lifecycle notes in `DomainModule.kt` are the model)
6. **Testability**: Prefer constructor-passed fakes over container-level mocking; keep the DI-graph test green
