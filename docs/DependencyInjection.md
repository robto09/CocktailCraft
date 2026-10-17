# Dependency Injection in CocktailCraft

This document outlines the dependency injection (DI) approach used in the CocktailCraft app.

## Overview

CocktailCraft uses Koin for dependency injection. Each Gradle module that owns bindings exposes its own Koin module, and the `:shared` umbrella assembles them into a single `appModule` list, plus a platform module supplied per platform. Because a module's implementation classes can stay `internal` while its Koin module is public, the wiring lives next to the code it wires:

| Koin module | Gradle module | Source |
|---|---|---|
| `networkModule`, `dataModule` | `:core:data` | `core/data/src/commonMain/kotlin/com/cocktailcraft/di/` |
| `dataPlatformModule()` (expect/actual) | `:core:data` | `core/data/src/{commonMain,androidMain,iosMain}/kotlin/com/cocktailcraft/di/DataPlatformModule.kt` |
| `domainModule` | `:core:domain` | `core/domain/src/commonMain/kotlin/com/cocktailcraft/di/DomainModule.kt` |
| `presentationModule` | `:presentation` | `presentation/src/commonMain/kotlin/com/cocktailcraft/di/PresentationModule.kt` |
| `appModule`, `platformModule()`, `initKoin()` | `:shared` | `shared/src/commonMain/kotlin/com/cocktailcraft/` (`di/AppModule.kt`, `di/PlatformModule.kt`, `KoinInit.kt`) |

Those source files are the source of truth; the excerpts below are representative, not exhaustive. All of these declarations are `@HiddenFromObjC` (AR-12): Swift only calls `doInitKoin()` and `KoinHelper`.

## Module Structure

### 1. App Module (`:shared`, `AppModule.kt`)

Combines the common modules. Only `:shared` depends on every layer, so it is the one place the full graph is assembled:

```kotlin
val appModule = listOf(
    networkModule,      // :core:data
    dataModule,         // :core:data
    domainModule,       // :core:domain
    presentationModule  // :presentation
)
// platformModule() is added separately at Koin startup (see PlatformModule.kt)
```

androidApp starts Koin with `appModule + platformModule() + widgetModule`; iOS calls `doInitKoin()` (`KoinIOS.kt`), which runs `initKoin()` with `appModule + platformModule()`.

### 2. Network Module (`:core:data`, `NetworkModule.kt`)

HTTP client, retry policy, and the API implementation. The module is public so `:shared` can assemble it; `buildHttpClient` and the API classes stay `internal`:

```kotlin
val networkModule = module {
    // HTTP Client — buildHttpClient() installs the full plugin stack
    single { buildHttpClient(json = get(), config = get()) }

    // API
    single<CocktailApi> { CocktailApiImpl(client = get(), appConfig = get()) }
}

internal fun buildHttpClient(json: Json, config: AppConfig, engine: HttpClientEngine? = null): HttpClient {
    // ContentNegotiation, Logging (verbose only when AppConfig says so),
    // HttpTimeout, and the single retry/backoff layer (SH-7): retries 5xx
    // and 429 with exponential delay; timeouts and 4xx are not retried.
    // expectSuccess = true. See the source for the full configuration.
}
```

### 3. Data Module (`:core:data`, `DataModule.kt`)

JSON, the IO dispatcher, caching, the offline policy, and one binding per focused repository. The former monolithic `CocktailRepository`/`CocktailRepositoryImpl` was split into nine single-concern repositories; every implementation is `internal` to `:core:data` and bound to its domain interface here:

```kotlin
val dataModule = module {
    // JSON
    single { Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true } }

    // Background dispatcher for database and Settings I/O and JSON (de)serialization
    single<CoroutineDispatcher>(named("ioDispatcher")) { Dispatchers.IO }

    // Offline decision — single source of truth for "is the app offline" (AR-7)
    single { OfflineModePolicy(networkMonitor = get()) }

    // App database over the platform's SqlDriver (cache + user data)
    single { CocktailCraftDatabase(get<SqlDriver>()) }

    // Caches
    single { CocktailCache(/* database (lazy), settings, json, appConfig, ioDispatcher, offlineModePolicy */) }

    // Database access for cart/orders/favorites/reviews; the first open
    // imports (once) what older versions kept as JSON in Settings
    single { LocalDatabase(/* database (lazy), LegacySettingsImport(settings, json), ioDispatcher */) }
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

    single<CartRepository> { CartRepositoryImpl(/* localDatabase, json, ioDispatcher */) }
    single<AuthRepository> { AuthRepositoryImpl(get(secureSettingsQualifier), get(), get(named("ioDispatcher"))) }
    single<OrderRepository> { OrderRepositoryImpl(/* localDatabase, ioDispatcher */) }
    single<ReviewRepository> { ReviewRepositoryImpl(/* localDatabase, ioDispatcher */) }
}
```

Note the `secureSettingsQualifier` for `AuthRepositoryImpl`: auth data uses the platform-secure `Settings` instance (EncryptedSharedPreferences / Keychain) registered by the platform module (see `SettingsQualifiers.kt` in `:core:data`).

### 4. Domain Module (`:core:domain`, `DomainModule.kt`)

The use cases (all `factory`). The `AppConfig` binding lives in each platform module (AR-4) — the platform knows whether this is a debug build; commonMain doesn't. A use case must earn its indirection (AR-8): pure 1:1 pass-throughs with a single consumer are collapsed onto the repository interface instead (`ManageOrdersUseCase` was removed this way).

```kotlin
val domainModule = module {
    // Use Cases — constructor-injected with the focused repository interfaces
    factory { SearchCocktailsUseCase(searchRepository = get()) }
    factory { LoadCocktailsByCategoryUseCase(searchRepository = get()) }
    factory { SortCocktailsUseCase() }
    factory { GetCocktailDetailUseCase(detailRepository = get(), favoritesRepository = get(), searchRepository = get()) }
    factory { ManageFavoritesUseCase(favoritesRepository = get(), detailRepository = get()) }
    factory { ManageCartUseCase(cartRepository = get(), detailRepository = get()) }
    factory { ManageProfileUseCase(authRepository = get()) }
    factory { ManageReviewsUseCase(repository = get()) }
    factory { PlaceOrderUseCase(orderRepository = get()) }
}
```

The use-case classes are public (`:presentation` constructs its ViewModels with them) but `@HiddenFromObjC`, so they never reach Swift.

### 5. Presentation Module (`:presentation`, `PresentationModule.kt`)

The shared ViewModels. Their use cases come from `domainModule`; the few that take a repository or `NetworkMonitor` directly get the interface, bound by `dataModule`/the platform module — `:presentation` never sees the data layer.

```kotlin
val presentationModule = module {
    // Screen-scoped ViewModels — new instance per screen (Android's
    // koinViewModel() scopes them to the nav back-stack entry)
    viewModel { SharedCocktailDetailViewModel(/* use cases */) }
    viewModel { SharedReviewViewModel(manageReviewsUseCase = get()) }

    // Global-state ViewModels — `single`, shared across screens and platforms.
    // Because a Koin single is never routed through a ViewModelStore,
    // onCleared() never runs on them (SH-9); see PresentationModule.kt for
    // the full lifecycle notes (including the AN-4 process-death caveat).
    single { SharedHomeViewModel(/* use cases + catalogRepository + networkMonitor */) }
    single { SharedCartViewModel(manageCartUseCase = get()) }
    single { SharedFavoritesViewModel(manageFavoritesUseCase = get()) }
    single { SharedOrderViewModel(orderRepository = get(), placeOrderUseCase = get()) }
    single { SharedProfileViewModel(manageProfileUseCase = get()) }
    single { SharedThemeViewModel(manageProfileUseCase = get()) }
}
```

### 6. Platform Module (`platformModule()`)

- `expect fun dataPlatformModule(): Module` in `:core:data`'s commonMain; the Android and iOS actuals provide the platform-specific bindings (Settings instances including the secure/encrypted one, `NetworkMonitor`, the SQLDelight `SqlDriver`, the debug-aware `AppConfig`). They are all data-layer adapters, so they live in `core/data/src/androidMain/.../di/DataPlatformModule.kt` and `core/data/src/iosMain/.../di/DataPlatformModule.kt` (named apart from `:shared`'s `PlatformModule.kt`: same package, so equal file names would clash as JVM classes in the app).
- `:shared` re-exposes it as `platformModule()` (`shared/src/commonMain/.../di/PlatformModule.kt`) — the name androidApp and `initKoin()` use, since neither app can see `:core:data`.

## ViewModel Dependency Injection

ViewModels do **not** implement `KoinComponent` and do not use field injection. All shared ViewModels extend `SharedViewModel` (the multiplatform base class on androidx `ViewModel`) and receive their dependencies — use cases, occasionally a repository or `NetworkMonitor` — through their (`internal`) constructors. Koin wires them up in `presentationModule`:

- **Screen-scoped** (`viewModel { ... }`): resolved on Android with `koinViewModel<SharedCocktailDetailViewModel>()` inside the destination composable; on iOS via `KoinHelper`.
- **Global-state** (`single { ... }`): the same instance is shared across screens and handed to iOS through `KoinHelper`.

Constructor injection keeps ViewModels constructible without a Koin container, which is how the `:presentation` tests build them.

## Testing with Koin

There is no mock-based `testModule`. Each module's `commonTest` suite uses:

- **Hand-written fakes** in `:core:testing` (`core/testing/src/commonMain/kotlin/com/cocktailcraft/testutil/Fakes.kt`, a test-only module), passed directly to constructors — most tests never start Koin. Data-layer doubles that need `:core:data` internals (`FakeCocktailApi`, the `LocalDatabase` helpers) stay in `core/data/src/commonTest/.../testutil/`.
- **`KoinDependencyGraphTest`** (`shared/src/commonTest/kotlin/com/cocktailcraft/di/`), which builds the full production graph (`appModule` + a test platform module) and resolves every repository and ViewModel, so DI wiring breaks fail in CI. Its iOS mirror, `KoinHelperTest` (`shared/src/iosTest`), resolves every `KoinHelper` getter (AR-6).
- **`DataModuleGraphTest`** (`core/data/src/commonTest/.../di/`), which checks the data layer's internal singletons (cache, `LocalDatabase`) that the umbrella-level test cannot name.
- **`DataLayerTest`** (`presentation/src/commonTest/.../testutil/DataLayer.kt`): the Profile/Theme/Review ViewModel tests run against the real repositories, resolved through `dataModule` in an isolated `koinApplication` (`:core:data` is a test-only dependency of `:presentation`). `dataModule` is one `Module` object whose singles cache their instance until the graph closes, so the base class closes every graph after each test.

## Best Practices

1. **Separation of Concerns**: Keep modules focused on specific layers or functionality — each Gradle module owns the Koin module for its own classes
2. **Constructor Injection**: All ViewModels and use cases take dependencies via constructor — no service-locator lookups in business code
3. **Interface-Based Injection**: Inject the focused repository interfaces (`CocktailSearchRepository`, `CartRepository`, ...), never implementations — the implementations are `internal` to `:core:data`
4. **Minimal Dependencies**: Keep dependencies minimal and focused
5. **Documentation**: Document non-obvious dependencies and injection patterns (the lifecycle notes in `PresentationModule.kt` are the model)
6. **Testability**: Prefer constructor-passed fakes over container-level mocking; keep the DI-graph tests green
