# Dependency Injection in CocktailCraft

This document outlines the dependency injection (DI) approach used in the CocktailCraft app.

## Overview

CocktailCraft uses Koin for dependency injection. Each Gradle module that owns bindings exposes its own Koin module, and the `:shared` umbrella assembles them into a single `appModule` list, plus a platform module supplied per platform. Because a module's implementation classes can stay `internal` while its Koin module is public, the wiring lives next to the code it wires:

| Koin module | Gradle module | Source |
|---|---|---|
| `networkModule`, `dataModule` | `:core:data` | `core/data/src/commonMain/kotlin/com/cocktailcraft/di/` |
| `dataPlatformModule()` (expect/actual) | `:core:data` | `core/data/src/{commonMain,androidMain,iosMain}/kotlin/com/cocktailcraft/di/DataPlatformModule.kt` |
| `domainModule` | `:presentation` (wires `:core:domain`'s use cases) | `presentation/src/commonMain/kotlin/com/cocktailcraft/di/DomainModule.kt` |
| `presentationModule` | `:presentation` | `presentation/src/commonMain/kotlin/com/cocktailcraft/di/PresentationModule.kt` |
| `appModule`, `widgetDataModule`, `platformModule()`, `initKoin()` | `:shared` | `shared/src/commonMain/kotlin/com/cocktailcraft/` (`di/AppModule.kt`, `di/PlatformModule.kt`, `KoinInit.kt`) |

Those source files are the source of truth; the excerpts below are representative, not exhaustive. All of these declarations are `@HiddenFromObjC`: Swift only calls `doInitKoin()` and `KoinHelper`.

## Module Structure

### 1. App Module (`:shared`, `AppModule.kt`)

Combines the common modules. Only `:shared` depends on every layer, so it is the one place the full graph is assembled:

```kotlin
val appModule = listOf(
    networkModule,      // :core:data
    dataModule,         // :core:data
    domainModule,       // :presentation (the use cases of :core:domain)
    presentationModule, // :presentation
    widgetDataModule    // :shared (WidgetDataSource, below)
)
// platformModule() is added separately at Koin startup (see PlatformModule.kt)
```

Both apps start Koin through `initKoin(isDebug)` (`KoinInit.kt`), which also sets the Kermit log floor (Warn in release) and starts the NetworkMonitor. androidApp calls it with `BuildConfig.DEBUG` (the widgets' `WidgetDataSource` comes from `appModule`'s `widgetDataModule`); iOS calls `doInitKoin(isDebug:)` (`KoinIOS.kt`) with its `#if DEBUG` flag.

### 2. Network Module (`:core:data`, `NetworkModule.kt`)

HTTP client, retry policy, and the API implementation. The module is public so `:shared` can assemble it; `buildHttpClient` and the API classes stay `internal`:

```kotlin
val networkModule = module {
    // HTTP client. Android binds an OkHttp engine over the app-wide
    // OkHttpClient (dataPlatformModule); elsewhere Ktor picks the default.
    single { buildHttpClient(json = get(), config = get(), engine = getOrNull()) }

    // API
    single<CocktailApi> { CocktailApiImpl(client = get(), appConfig = get(), json = get()) }
}

internal fun buildHttpClient(json: Json, config: AppConfig, engine: HttpClientEngine? = null): HttpClient {
    // ContentNegotiation, Logging (verbose only when AppConfig says so),
    // HttpTimeout, and the single retry layer: one quick retry of a 5xx,
    // a 429 or a dropped connection; timeouts, an unreachable host and
    // other 4xx are not retried.
    // expectSuccess = true. See the source for the full configuration.
}
```

### 3. Data Module (`:core:data`, `DataModule.kt`)

JSON, the dispatchers and the app scope, the offline policy, the database, who owns the user's data, and one binding per focused repository. Every implementation is `internal` to `:core:data` and bound to its domain interface here:

```kotlin
val dataModule = module {
    single { Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true } }

    // Database and Settings I/O, JSON (de)serialization
    single<CoroutineDispatcher>(named("ioDispatcher")) { Dispatchers.IO }
    // CPU work off the main thread: decoding and mapping API responses
    single<CoroutineDispatcher>(named("defaultDispatcher")) { Dispatchers.Default }
    // Lives as long as the process: shared fetches (InFlightRequests) and the
    // shared favorites stream run here, so they don't die with a screen
    single<CoroutineScope>(named("appScope")) {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(named("defaultDispatcher")))
    }

    single { OfflineModePolicy(networkMonitor = get()) }  // "is the app offline"
    single { CocktailCraftDatabase(get<SqlDriver>()) }    // over the platform's driver

    // The local cocktail catalog (cached_cocktail, cocktail_access, category_page)
    single { CocktailCache(/* database (lazy), settings, json, appConfig, currentOwner = UserSession, ioDispatcher, offlineModePolicy */) }
    // The user's data tables, scoped to the current owner; the first open
    // imports (once) what older versions kept as JSON in Settings
    single { LocalDatabase(/* database (lazy), LegacySettingsImport(settings, json), owner = UserSession, ioDispatcher */) }

    single { ApiRateLimiter() }  // the API's min-interval throttle
    single { CocktailRemoteDataSource(api = get(), rateLimiter = get(), defaultDispatcher = get(named("defaultDispatcher"))) }

    single<CocktailOfflineRepository> { CocktailOfflineRepositoryImpl(offlineModePolicy = get(), cocktailCache = get()) }
    single<CocktailFavoritesRepository> { CocktailFavoritesRepositoryImpl(/* localDatabase, json, cocktailCache, remote, offlineRepository, appScope, ioDispatcher */) }
    // Category pages shared by browsing and the search's category filter
    single { CocktailCategoryFetcher(remote = get(), cocktailCache = get(), offlineRepository = get(), appScope = get(named("appScope"))) }
    single<CocktailSearchRepository> { CocktailSearchRepositoryImpl(/* remote, cocktailCache, offlineRepository, categoryFetcher */) }
    single<CocktailDetailRepository> { CocktailDetailRepositoryImpl(/* remote, cocktailCache, offlineRepository, appScope */) }
    single<CocktailCatalogRepository> { CocktailCatalogRepositoryImpl(remote = get(), categoryFetcher = get()) }
    single<CartRepository> { CartRepositoryImpl(/* localDatabase, cocktailCache, json, ioDispatcher */) }

    // The account store; who owns the user's data (the signed-in user, else
    // the guest); and the app's AuthRepository, which switches that owner
    // on sign-in, sign-up and sign-out
    single { AuthRepositoryImpl(get(secureSettingsQualifier), get(), get(named("ioDispatcher"))) }
    single { UserSession(authRepository = get<AuthRepositoryImpl>()) }
    single<AuthRepository> {
        SessionAwareAuthRepository(delegate = get<AuthRepositoryImpl>(), session = get(), localDatabase = get())
    }

    single<OrderRepository> { OrderRepositoryImpl(localDatabase = get(), ioDispatcher = get(named("ioDispatcher"))) }
    single<ReviewRepository> { ReviewRepositoryImpl(localDatabase = get(), ioDispatcher = get(named("ioDispatcher"))) }
}
```

- `UserSession` is the current owner of the user's data: it reads who is signed in from the account store once, then changes only when `SessionAwareAuthRepository` switches it after a successful sign-in, sign-up or sign-out (merging the guest's data into the account first; see [Data layer](#data-layer)). Every other `AuthRepository` call, `changePassword` included, goes straight to `AuthRepositoryImpl`.
- `AuthRepositoryImpl` takes the `secureSettingsQualifier` `Settings`: auth data lives in the platform's encrypted store (see [Auth storage](#auth-storage)), registered by the platform module (`SettingsQualifiers.kt` in `:core:data`).

### 4. Domain Module (`:presentation`, `DomainModule.kt`)

The use cases (all `factory`). It lives in `:presentation`, next to `presentationModule`, so `:core:domain` itself has no DI (or logging) dependency. The `AppConfig` binding lives in each platform module — the platform knows whether this is a debug build; commonMain doesn't. A use case must earn its indirection: pure pass-throughs are collapsed onto the repository interface instead, and ViewModels call that repository directly (`ManageOrdersUseCase` and `ManageProfileUseCase` were removed this way).

```kotlin
val domainModule = module {
    // Use cases, constructor-injected with the focused repository interfaces
    factory { SearchCocktailsUseCase(searchRepository = get()) }
    factory { LoadCocktailsByCategoryUseCase(catalogRepository = get()) }
    factory { SortCocktailsUseCase() }
    factory { GetCocktailDetailUseCase(detailRepository = get(), favoritesRepository = get(), catalogRepository = get()) }
    factory { ManageFavoritesUseCase(favoritesRepository = get(), detailRepository = get()) }
    factory { ManageCartUseCase(cartRepository = get(), detailRepository = get()) }
    factory { ManageReviewsUseCase(repository = get()) }
    factory { PlaceOrderUseCase(orderRepository = get()) }
}
```

The use-case classes are public (`:presentation` constructs its ViewModels with them) but `@HiddenFromObjC`, so they never reach Swift.

### 5. Presentation Module (`:presentation`, `PresentationModule.kt`)

The shared ViewModels. Most take use cases from `domainModule`; the ones with no logic to wrap take a repository interface or `NetworkMonitor` directly, bound by `dataModule`/the platform module — `:presentation` never sees the data layer.

```kotlin
val presentationModule = module {
    // Screen-scoped ViewModels — new instance per screen (Android's
    // koinViewModel() scopes them to the nav back-stack entry)
    viewModel { SharedCocktailDetailViewModel(getCocktailDetailUseCase = get(), manageFavoritesUseCase = get(), manageCartUseCase = get()) }
    viewModel { SharedReviewViewModel(manageReviewsUseCase = get()) }
    viewModel { SharedOrderDetailViewModel(orderRepository = get(), manageCartUseCase = get()) }

    // Global-state ViewModels — `single`, shared across screens and platforms.
    // A Koin single is never routed through a ViewModelStore, so onCleared()
    // never runs on them; see PresentationModule.kt for the lifecycle notes
    // (including the process-death caveat).
    single {
        SharedHomeViewModel(
            searchCocktailsUseCase = get(), loadCocktailsByCategoryUseCase = get(), sortCocktailsUseCase = get(),
            manageFavoritesUseCase = get(), offlineRepository = get(), catalogRepository = get(),
            networkMonitor = get(), manageReviewsUseCase = get()
        )
    }
    single { SharedCartViewModel(manageCartUseCase = get()) }
    single { SharedFavoritesViewModel(manageFavoritesUseCase = get()) }
    single { SharedOrderViewModel(orderRepository = get(), placeOrderUseCase = get()) }
    single { SharedProfileViewModel(authRepository = get()) }
    single { SharedThemeViewModel(authRepository = get()) }
}
```

### 6. Widget Module (`:shared`, `AppModule.kt`)

`widgetDataModule` binds `WidgetDataSource` (`shared/src/commonMain/.../widget/`), the one source of what the home-screen widgets show on both platforms: a pool of a fresh random cocktail (online only) followed by the recently viewed ones, and the favorites. Android's widgets resolve it directly; iOS reaches it through `KoinHelper.getRandomCocktailWidgetPool`.

### 7. Platform Module (`platformModule()`)

- `expect fun dataPlatformModule(): Module` in `:core:data`'s commonMain; the Android and iOS actuals provide the platform-specific bindings (Settings instances including the secure/encrypted one, `NetworkMonitor`, the SQLDelight `SqlDriver`, the debug-aware `AppConfig`). They are all data-layer adapters, so they live in `core/data/src/androidMain/.../di/DataPlatformModule.kt` and `core/data/src/iosMain/.../di/DataPlatformModule.kt` (named apart from `:shared`'s `PlatformModule.kt`: same package, so equal file names would clash as JVM classes in the app).
- `:shared` re-exposes it as `platformModule()` (`shared/src/commonMain/.../di/PlatformModule.kt`) — the name androidApp and `initKoin()` use, since neither app can see `:core:data`.

## Data layer

The nine repository interfaces live in `core/domain/src/commonMain/kotlin/com/cocktailcraft/domain/repository/`; their `internal` implementations (plus the `CocktailCategoryFetcher` helper shared by the search and catalog repositories, and `SessionAwareAuthRepository`) are in `core/data/src/commonMain/kotlin/com/cocktailcraft/data/repository/`. The KDoc on the interfaces is the source of truth for their methods.

**Conventions**
- One-shot operations are `suspend` functions returning `Result<T>` (`com.cocktailcraft.domain.util.Result`).
- Data the UI watches is exposed as an `observe*()` `Flow` (cart, orders, favorites, reviews, a catalog category page, a single cocktail): a SQLDelight query flow that emits what is stored on collection and again after every write to its tables, from any repository, and switches to the new owner's rows when the owner changes.
- Implementations confine database and Settings I/O and JSON (de)serialization to the injected IO dispatcher, decode API responses on the default dispatcher, and run multi-statement writes in one transaction.

### Storage

Everything except auth lives in one SQLite database (`cocktailcraft_cache.db`, schema in `core/data/src/commonMain/sqldelight/`; each `.sq` file documents its table):

| Table | Holds |
|-------|-------|
| `cached_cocktail` | The single copy of every stored cocktail, as JSON, with a `full_details` flag (0 for a list endpoint's partial record: id, name and thumbnail only). Everything else refers to these rows by id, so one detail refresh updates every screen. Expires after 24h and is evicted least-recently-used, except rows something refers to (`CocktailCache`) |
| `cocktail_access` | The last-used stamp of each `cached_cocktail` row, for LRU eviction; kept apart so a read doesn't count as a write to `cached_cocktail` |
| `category_page` | Which cocktails make up each category, in order, and when the page was fetched |
| `favorite_cocktail` | Favorites: `owner_id`, `cocktail_id` (→ `cached_cocktail`), position |
| `cart_item` | Cart lines: `owner_id`, `cocktail_id` (→ `cached_cocktail`), quantity, and `price_cents`, the unit price when the line was added, which the line keeps charging |
| `recently_viewed_cocktail` | `owner_id`, `cocktail_id` (→ `cached_cocktail`), when it was viewed; capped per owner |
| `customer_order`, `customer_order_item` | Orders with `owner_id`; each item keeps its own name, quantity and price. Placing an order saves it and clears the cart in one transaction (`OrderRepositoryImpl.placeOrderAndClearCart`) |
| `cocktail_review` | Reviews with `owner_id` |

- **Owners.** Every user-data row carries an `owner_id`: the signed-in user's id, or `guest` while signed out (`UserSession`). The cart, favorites, recently viewed list and orders are read and written for the current owner only. Reviews are readable by everyone on the device, and only their owner can edit or delete them (`Review.isMine` marks the reader's own). Signing in or up from the guest merges the guest's cart (quantities add up), favorites and recently viewed list into the account; orders and reviews stay with the guest. Switching between accounts merges nothing. Rows from before owners existed go to whoever is signed in at the first launch after the upgrade. The rules, and the code that applies them, are in `core/data/src/commonMain/kotlin/com/cocktailcraft/data/local/UserDataOwnership.kt`.
- Every schema change needs a numbered migration in `core/data/src/commonMain/sqldelight/migrations/` and a regenerated snapshot in `databases/`; `verifySqlDelightMigration` (part of `check`) fails otherwise.

### Auth storage

Accounts are device-local: accounts, the session, sign-in throttling and guest preferences stay out of the database, in the platform's encrypted `Settings` store (`secureSettingsQualifier`, registered by the platform module and opened lazily through `LazySettings`):

- **Android**: an encrypted file store (`EncryptedFileSettings` in `no_backup/`), encrypted with a Tink AES-256-GCM keyset that is itself wrapped by an Android Keystore key (`AuthStore.kt`). The `EncryptedSharedPreferences` file earlier versions used is read once on first open to migrate its data (`LegacySecurePrefs.kt`), then deleted. Neither the store nor the keyset is backed up.
- **iOS**: the Keychain (`KeychainSettings`).

## ViewModel Dependency Injection

ViewModels do **not** implement `KoinComponent` and do not use field injection. All shared ViewModels extend `SharedViewModel` (the multiplatform base class on androidx `ViewModel`) and receive their dependencies — use cases, occasionally a repository or `NetworkMonitor` — through their (`internal`) constructors. Koin wires them up in `presentationModule`:

- **Screen-scoped** (`viewModel { ... }`): resolved on Android with `koinViewModel<SharedCocktailDetailViewModel>()` inside the destination composable; on iOS via `KoinHelper`.
- **Global-state** (`single { ... }`): the same instance is shared across screens and handed to iOS through `KoinHelper`.

Constructor injection keeps ViewModels constructible without a Koin container, which is how the `:presentation` tests build them.

## Testing with Koin

There is no mock-based `testModule`. Each module's `commonTest` suite uses:

- **Hand-written fakes** in `:core:testing` (`core/testing/src/commonMain/kotlin/com/cocktailcraft/testutil/Fakes.kt`, a test-only module), passed directly to constructors — most tests never start Koin. Data-layer doubles that need `:core:data` internals (`FakeCocktailApi`, the `LocalDatabase` helpers) stay in `core/data/src/commonTest/.../testutil/`.
- **`KoinDependencyGraphTest`** (`shared/src/commonTest/kotlin/com/cocktailcraft/di/`), which builds the full production graph (`appModule` + a test platform module) and resolves every repository and ViewModel, so DI wiring breaks fail the test run. Its iOS mirror, `KoinHelperTest` (`shared/src/iosTest`), resolves every `KoinHelper` getter.
- **`DataModuleGraphTest`** (`core/data/src/commonTest/.../di/`), which checks the data layer's internal singletons (cache, `LocalDatabase`) that the umbrella-level test cannot name.
- **`DataLayerTest`** (`presentation/src/commonTest/.../testutil/DataLayer.kt`): the Profile/Theme/Review ViewModel tests run against the real repositories, resolved through `dataModule` in an isolated `koinApplication` (`:core:data` is a test-only dependency of `:presentation`). `dataModule` is one `Module` object whose singles cache their instance until the graph closes, so the base class closes every graph after each test.

## Best Practices

1. **Separation of Concerns**: Keep modules focused on specific layers or functionality — each Gradle module owns the Koin module for its own classes
2. **Constructor Injection**: All ViewModels and use cases take dependencies via constructor — no service-locator lookups in business code
3. **Interface-Based Injection**: Inject the focused repository interfaces (`CocktailSearchRepository`, `CartRepository`, ...), never implementations — the implementations are `internal` to `:core:data`
4. **Minimal Dependencies**: Keep dependencies minimal and focused
5. **Documentation**: Document non-obvious dependencies and injection patterns (the lifecycle notes in `PresentationModule.kt` are the model)
6. **Testability**: Prefer constructor-passed fakes over container-level mocking; keep the DI-graph tests green
