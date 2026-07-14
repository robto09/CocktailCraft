# Enhancement Tracker

Findings from the full-app review (2026-07-10) across **Architecture**, **Shared (KMP)**, **Android**, and **iOS**.
Re-verified against `dev` after the phase-4 merge (2ee7b44) — all items confirmed still applicable.

> **Batch status (2026-07-10, branch `enhancements/tracker-2026-07`):** every item
> below is implemented as its own commit on this branch, with two deliberate
> exceptions — **AR-1 (CI/CD) was omitted from this batch per scope**, and
> **AN-5 is phase 1 only** (dead theme deleted; the ~320-call-site
> `AppColors.*` → `MaterialTheme.colorScheme` migration remains a follow-up).
> Verified by: `:shared:testAndroidHostTest`, `:shared:iosSimulatorArm64Test`,
> `:androidApp:testDebugUnitTest`, `:androidApp:lintDebug`, and
> `xcodebuild test` (CocktailCraftTests) on the iPhone 16 simulator.

**Status legend:** 🔲 Not Started · 🟡 In Progress · ✅ Done

## Quick Status Board

| ID | Area | Title | Severity | Status |
|----|------|-------|----------|--------|
| [AR-1](#ar-1-stand-up-cicd) | Architecture | Stand up CI/CD | High | 🔲 |
| [AR-2](#ar-2-typed-error-codes-at-the-repository-boundary) | Architecture | Typed error codes at the repository boundary | High | ✅ |
| [AR-3](#ar-3-reconcile-docs-with-post-refactor-code) | Architecture | Reconcile docs with post-refactor code | Medium | ✅ |
| [AR-4](#ar-4-environment-aware-build-configuration) | Architecture | Environment-aware build configuration | Medium | ✅ |
| [AR-5](#ar-5-test-the-networking-and-use-case-layers) | Architecture | Test the networking and use-case layers | Medium | ✅ |
| [AR-6](#ar-6-harden-the-ios-koinhelper-service-locator) | Architecture | Harden the iOS `KoinHelper` service locator | Medium | ✅ |
| [AR-7](#ar-7-centralize-offlinecaching-policy) | Architecture | Centralize offline/caching policy | Medium | ✅ |
| [AR-8](#ar-8-decide-a-convention-for-pass-through-use-cases) | Architecture | Decide a convention for pass-through use cases | Med/Low | ✅ |
| [AR-9](#ar-9-single-source-the-app-version) | Architecture | Single-source the app version | Low/Med | ✅ |
| [AR-10](#ar-10-extract-shared-build-convention-plugin) | Architecture | Extract shared build convention plugin | Low/Med | ✅ |
| [AR-11](#ar-11-repo-hygiene-committed-artifacts) | Architecture | Repo hygiene: committed artifacts | Low | ✅ |
| [AR-12](#ar-12-trim-the-exported-objc-framework-surface) | Architecture | Trim the exported ObjC framework surface | Low | ✅ |
| [SH-1](#sh-1-stop-swallowing-category-fetch-errors-into-empty-success) | Shared | Stop swallowing category-fetch errors into empty success | High | ✅ |
| [SH-2](#sh-2-fix-networkmonitor-double-registration--shared-teardown) | Shared | Fix `NetworkMonitor` double-registration / shared teardown | High | ✅ |
| [SH-3](#sh-3-make-clear-cache-actually-clear-the-cache) | Shared | Make "Clear Cache" actually clear the cache | Med/High | ✅ |
| [SH-4](#sh-4-make-favorites-reactive-and-offline-safe) | Shared | Make favorites reactive and offline-safe | Medium | ✅ |
| [SH-5](#sh-5-fix-ios-connectivity-false-positives) | Shared | Fix iOS connectivity false-positives | Medium | ✅ |
| [SH-6](#sh-6-harden-json-parsing-against-thecocktaildb-quirks) | Shared | Harden JSON parsing against TheCocktailDB quirks | Medium | ✅ |
| [SH-7](#sh-7-consolidate-retryrate-limit-layers) | Shared | Consolidate retry/rate-limit layers | Medium | ✅ |
| [SH-8](#sh-8-clean-up-getcocktailbyid-no-op-catch--magic-timeout) | Shared | Clean up `getCocktailById` (no-op catch + magic timeout) | Low/Med | ✅ |
| [SH-9](#sh-9-document--fix-singleton-viewmodel-lifecycle) | Shared | Document / fix singleton ViewModel lifecycle | Low/Med | ✅ |
| [SH-10](#sh-10-replace-shuffled-in-getcocktailsbycategory) | Shared | Replace `shuffled()` in `getCocktailsByCategory` | Low | ✅ |
| [SH-11](#sh-11-unify-the-two-cache-layers) | Shared | Unify the two cache layers | Low | ✅ |
| [SH-12](#sh-12-trim-errorhandler-string-matching-expand-turbine-tests) | Shared | Trim ErrorHandler string-matching; expand Turbine tests | Low | ✅ |
| [SH-13](#sh-13-add-debounce-to-the-shared-search-flow) | Shared | Add debounce to the shared search flow | High | ✅ |
| [AN-1](#an-1-make-domain-models-compose-stable) | Android | Make domain models Compose-stable | High | ✅ |
| [AN-2](#an-2-schedule-or-delete-widgetupdateworker) | Android | Schedule (or delete) `WidgetUpdateWorker` | High | ✅ |
| [AN-3](#an-3-eliminate-on²-favorites-lookup-in-lists) | Android | Eliminate O(n²) favorites lookup in lists | Med/High | ✅ |
| [AN-4](#an-4-process-death--savedstatehandle-strategy) | Android | Process-death / SavedStateHandle strategy | Medium | ✅ |
| [AN-5](#an-5-consolidate-theme-systems-remove-global-mutable-appcolors) | Android | Consolidate theme systems; remove global mutable `AppColors` | Medium | 🟡 |
| [AN-6](#an-6-fix-edge-to-edge-insets-on-the-app-bar) | Android | Fix edge-to-edge insets on the app bar | Medium | ✅ |
| [AN-7](#an-7-migrate-deprecated-compose-material-apis) | Android | Migrate deprecated Compose Material APIs | Medium | ✅ |
| [AN-8](#an-8-widget-strings-sentinel-state-and-dead-image-code) | Android | Widget strings, sentinel state, and dead image code | Medium | ✅ |
| [AN-9](#an-9-tighten-homescreen-effects--search-launch) | Android | Tighten HomeScreen effects & search launch | Low/Med | ✅ |
| [AN-10](#an-10-workmanager-koin-workerfactory) | Android | WorkManager: Koin `WorkerFactory` | Low | ✅ |
| [AN-11](#an-11-defer-non-critical-startup-work) | Android | Defer non-critical startup work | Low | ✅ |
| [AN-12](#an-12-automate-versioncodeversionname) | Android | Automate versionCode/versionName | Low | ✅ |
| [AN-13](#an-13-deepen-the-baseline-profile-journey) | Android | Deepen the baseline profile journey | Low | ✅ |
| [IO-1](#io-1-debounce--cancel-search-on-ios) | iOS | Debounce + cancel search on iOS | High | ✅ |
| [IO-2](#io-2-remove-artificial-pull-to-refresh-delays) | iOS | Remove artificial pull-to-refresh delays | Medium | ✅ |
| [IO-3](#io-3-delete-dead-platformerrorhandler) | iOS | Delete dead `PlatformErrorHandler` | Medium | ✅ |
| [IO-4](#io-4-fix-phantom-scenedelegate-in-infoplist) | iOS | Fix phantom `SceneDelegate` in Info.plist | Medium | ✅ |
| [IO-5](#io-5-remove-stale-armv7-device-capability) | iOS | Remove stale `armv7` device capability | Medium | ✅ |
| [IO-6](#io-6-introduce-localization--locale-aware-prices) | iOS | Introduce localization + locale-aware prices | Medium | ✅ |
| [IO-7](#io-7-grow-native-test-coverage) | iOS | Grow native test coverage | Medium | ✅ |
| [IO-8](#io-8-pick-one-project-source-of-truth-xcodegen-vs-pbxproj) | iOS | Pick one project source of truth (XcodeGen vs pbxproj) | Low/Med | ✅ |
| [IO-9](#io-9-gate-release-logging) | iOS | Gate release logging | Low | ✅ |
| [IO-10](#io-10-remove-bgtask-force-casts) | iOS | Remove `BGTask` force-casts | Low | ✅ |
| [IO-11](#io-11-reset-nav-stack-on-widget-deep-link) | iOS | Reset nav stack on widget deep link | Low | ✅ |
| [IO-12](#io-12-audit-preconcurrency-import-shared) | iOS | Audit `@preconcurrency import shared` | Low | ✅ |
| [IO-13](#io-13-verify-state-viewmodel-churn-on-detail) | iOS | Verify `@State` ViewModel churn on detail | Low | ✅ |

---

## Architecture

### AR-1. Stand up CI/CD
**Status:** 🔲 Not Started · **Severity:** High

**Description:** No `.github/`, fastlane, or any pipeline exists, yet the project has test suites on all three surfaces, a baseline-profile module, R8 minification, and release signing already written to read CI env vars (`ANDROID_RELEASE_*`). Nothing runs automatically — regressions reach `dev`/`main` unguarded.

**Where to look:** `androidApp/build.gradle.kts:11-19,43-74` (env-var signing), `baselineprofile/build.gradle.kts:40-45` (managed-device task), `shared/src/commonTest/`, `iosApp/CocktailCraftTests/`, repo root (no `.github/`).

**Approach:** Add a GitHub Actions workflow: (1) `./gradlew :shared:allTests :androidApp:testDebugUnitTest lint`; (2) an iOS lane running `xcodegen generate && pod install && xcodebuild test`; (3) a scheduled `:baselineprofile` generation job. Gate PRs to `dev`/`main` on it.

### AR-2. Typed error codes at the repository boundary
**Status:** ✅ Done · **Severity:** High

**Description:** The app has a full typed error pipeline (`ErrorCode` → `Result.Error` → `ErrorHandler` → `UserFriendlyError`), but repositories build `Result.Error(e.message ?: "...")` without a code, so nearly every failure arrives as `UNKNOWN`, collapsing the category/recovery taxonomy. A deprecated string-matching fallback in `ErrorHandler` partially papers over this.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/OrderRepositoryImpl.kt:85,94,108,120,144` (and sibling repositories), `shared/src/commonMain/kotlin/com/cocktailcraft/domain/util/Result.kt:5`, `shared/src/commonMain/kotlin/com/cocktailcraft/util/ErrorHandler.kt:84-89,189-222`.

**Approach:** Create a shared `runCatchingResult { }` helper that maps exceptions to `ErrorCode` via the existing type-based classifier and attaches the code to `Result.Error`. Adopt it in every repository. Then delete the string-matching fallback (see SH-12) and consider making `ErrorHandler` injectable instead of a global `object`.

### AR-3. Reconcile docs with post-refactor code
**Status:** ✅ Done (CI doc-lint pairs with AR-1, omitted from this batch) · **Severity:** Medium

**Description:** `CocktailRepositoryImpl` was split into seven focused repositories, but the docs, diagrams, and test catalog still describe the monolith. `TEST_CASES.md` lists tests that don't exist (e.g. `PlaceOrderUseCaseTest`, `CartViewModelTest`).

**Where to look:** `docs/DependencyInjection.md`, `docs/REPOSITORY_METHODS.md`, `docs/RecommendationSystem.md`, `docs/AdvancedSearch.md`, `docs/Shared_ViewModel_Strategy.md`, `README.md`, `docs/images/mermaid/*`, `docs/IMPROVEMENT_PLAN.md`, `TEST_CASES.md`.

**Approach:** Regenerate mermaid diagrams from current source; sweep docs for `CocktailRepositoryImpl`/`CocktailRepository` references; regenerate `TEST_CASES.md` from the actual `commonTest` tree or delete it. Optionally add a CI doc-lint that greps docs for removed symbols (pairs with AR-1).

### AR-4. Environment-aware build configuration
**Status:** ✅ Done · **Severity:** Medium

**Description:** `AppConfigImpl` is a compile-time constant class — enabling network logging requires editing source and rebuilding. There are no dev/staging/prod variants. Its declared `initialNetworkTimeoutMs`/`maxNetworkTimeoutMs`/`maxRetries` fields are dead: `NetworkModule` uses a single timeout and a hardcoded `maxRetries = 3`.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/config/AppConfigImpl.kt:10-40`, `shared/src/commonMain/kotlin/com/cocktailcraft/di/NetworkModule.kt:32-56`.

**Approach:** Drive `AppConfig` from `BuildConfig` (Android) / xcconfig (iOS) via the platform DI modules; gate `verboseNetworkLogging` on debug builds automatically. Either wire the declared retry/timeout fields into `NetworkModule` or delete them (coordinates with SH-7).

### AR-5. Test the networking and use-case layers
**Status:** ✅ Done · **Severity:** Medium

**Description:** ViewModels, repositories, cache, and the DI graph are well tested, but `data/remote/` has zero tests (DTO→domain mapping, the 429/backoff state machine) and no use-case tests exist despite 13 use cases. Ktor `MockEngine` isn't on the test classpath.

**Where to look:** `shared/src/commonTest/` (no `data/remote` tests), `shared/src/commonMain/kotlin/com/cocktailcraft/data/remote/CocktailRemoteDataSource.kt:44-114`, `shared/build.gradle.kts:120-128`, `shared/src/commonMain/kotlin/com/cocktailcraft/domain/usecase/`.

**Approach:** Add `ktor-client-mock` to `commonTest`; test `CocktailApiImpl`/`CocktailRemoteDataSource` (mapping, 429 backoff, timeout classification, and the SH-6 fixtures). Add use-case tests where use cases hold real logic.

### AR-6. Harden the iOS `KoinHelper` service locator
**Status:** ✅ Done · **Severity:** Medium

**Description:** Every shared dependency reachable from iOS must be hand-added as a getter on a single `KoinComponent` — a manual choke point; a missing getter silently hides a binding from Swift.

**Where to look:** `shared/src/iosMain/kotlin/com/cocktailcraft/KoinIOS.kt:28-69`.

**Approach:** Keep the pattern (it's a reasonable answer to no-reified-generics), but add a test asserting each `KoinHelper` getter resolves (mirror of `KoinDependencyGraphTest`), group getters by feature, and document that new shared bindings must be surfaced here.

### AR-7. Centralize offline/caching policy
**Status:** ✅ Done · **Severity:** Medium

**Description:** Offline-mode decisions live in three places (the cache, `CocktailOfflineRepositoryImpl`, `ManageOfflineModeUseCase`), and `CocktailCache` reaches directly into `Settings` and `NetworkMonitor` to decide expiry — the cache encodes policy, not just storage. `CocktailOfflineRepository` also breaks the uniform `Result<T>` contract with raw `Unit`/`Boolean` returns.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/cache/CocktailCache.kt:146-149`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CocktailOfflineRepositoryImpl.kt`, `shared/src/commonMain/kotlin/com/cocktailcraft/domain/usecase/ManageOfflineModeUseCase.kt`.

**Approach:** Make the offline repository the single owner of offline-mode state; have the cache receive an injected `applyExpiry` decision instead of reading settings itself. Normalize offline repo signatures onto `Result<T>`. (Overlaps SH-11.)

### AR-8. Decide a convention for pass-through use cases
**Status:** ✅ Done · **Severity:** Medium/Low

**Description:** Several use cases are 1:1 delegations to a single repository method (`ManageOrdersUseCase` is pure pass-through), adding indirection and DI wiring without behavior. Others (`SortCocktailsUseCase`, `AnalyzeCocktailUseCase`) genuinely hold logic.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/domain/usecase/ManageOrdersUseCase.kt:12-28`, `shared/src/commonMain/kotlin/com/cocktailcraft/di/DomainModule.kt:27-38`.

**Approach:** Pick and document one convention: either ViewModels depend on repository interfaces directly for pure pass-throughs, or keep the use-case layer but add the validation/orchestration that justifies it.

### AR-9. Single-source the app version
**Status:** ✅ Done (CI bump step intentionally omitted per batch scope) · **Severity:** Low/Medium

**Description:** The marketing/build version is hand-maintained independently on both platforms; nothing keeps them in sync.

**Where to look:** `androidApp/build.gradle.kts:29-30`, `iosApp/project.yml:34-35,95-96`.

**Approach:** Define the version once (in `libraries.toml [versions]` or root `gradle.properties`), inject it into the Android DSL and the generated xcconfig/project.yml, and bump it in CI. (Pairs with AN-12.)

### AR-10. Extract shared build convention plugin
**Status:** ✅ Done (version-catalog-backed values — the tracker's lighter option; a buildSrc plugin stays unnecessary at 3 modules) · **Severity:** Low/Medium

**Description:** `compileSdk`, `minSdk`, and the Java/Kotlin 17 target blocks are copy-pasted across three build files; a redundant `repositories {}` block sits in the shared module even though resolution is centralized in settings.

**Where to look:** `shared/build.gradle.kts:41,148-152`, `androidApp/build.gradle.kts:27,77-85`, `baselineprofile/build.gradle.kts:11,19-27`, `settings.gradle.kts:13-17`, root `build.gradle.kts:10-15`.

**Approach:** Add a `build-logic`/`buildSrc` convention plugin (or version-catalog-backed values) for SDK levels and JVM target; delete the redundant `repositories` block in `shared/build.gradle.kts`.

### AR-11. Repo hygiene: committed artifacts
**Status:** ✅ Done · **Severity:** Low

**Description:** A ~995 KB generated codebase dump and a ~726 KB screenshot are tracked at the repo root; `keystore.properties` exists at root (only the `.example` should be tracked).

**Where to look:** `repomix-output-tree-dev.md`, `Screenshot_20250419_003122.png`, `keystore.properties`, `.gitignore`.

**Approach:** Gitignore + remove generated dumps and stray screenshots; verify `keystore.properties`/`local.properties` are untracked (`git ls-files`).

### AR-12. Trim the exported ObjC framework surface
**Status:** ✅ Done (helpers hidden; api(viewmodel) and the SKIE feature set documented as deliberate) · **Severity:** Low

**Description:** `api(androidx.lifecycle.viewmodel)` leaks from commonMain, all SKIE features are enabled, and there's no exported-symbol control — every public shared symbol becomes ObjC API, inflating the framework and compile times.

**Where to look:** `shared/build.gradle.kts:10-27,95`.

**Approach:** Add `@HiddenFromObjC` to internal-but-public helpers; review whether the full SKIE feature set is required. Deliberate trade-off — low priority.

---

## Shared (KMP)

### SH-1. Stop swallowing category-fetch errors into empty success
**Status:** ✅ Done · **Severity:** High

**Description:** `CocktailCategoryFetcher.fetchCocktailsByCategory` catches everything and returns `emptyList()`; the search repository wraps that in `Result.Success`. A real failure (API down, rate-limit, parse error) becomes an empty success, so `SharedHomeViewModel`'s `getOrThrow()` never throws — the user sees an empty list with **no error, no retry, no cached-data fallback**.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CocktailCategoryFetcher.kt:102-105`, `CocktailSearchRepositoryImpl.kt:51-57`, `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedHomeViewModel.kt:110,118-132,390-409`.

**Approach:** Let `fetchCocktailsByCategory` throw (or return `Result`) on genuine failure; return empty only for genuinely empty categories. Keep the cached-fallback branches but re-throw the original exception when no cache exists so `tryLoadCachedData()` works as designed. Add a regression test.

### SH-2. Fix `NetworkMonitor` double-registration / shared teardown
**Status:** ✅ Done · **Severity:** High

**Description:** `NetworkMonitor` is a Koin `single`, and both `SharedHomeViewModel` and `SharedOfflineModeViewModel` (also singles) call `startMonitoring()` in `init`. On Android, registering the same `NetworkCallback` twice throws `IllegalArgumentException`. Additionally, `SharedOfflineModeViewModel.onCleared()` calls `stopMonitoring()` on the shared instance, tearing down monitoring for everyone.

**Where to look:** `shared/src/androidMain/kotlin/com/cocktailcraft/util/NetworkMonitor.kt:45-54`, `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedHomeViewModel.kt:60`, `SharedOfflineModeViewModel.kt:42,186-189`, both `PlatformModule.kt` files, `di/DomainModule.kt:53-69`.

**Approach:** Make `startMonitoring()`/`stopMonitoring()` idempotent and reference-counted, or (better) start monitoring once at app init (KoinInit / platform startup) and remove start/stop calls from ViewModels entirely.

### SH-3. Make "Clear Cache" actually clear the cache
**Status:** ✅ Done · **Severity:** Medium/High

**Description:** `SharedOfflineModeViewModel.clearCache()` only resets UI state; `CocktailCache.clearCache()` (which exists and clears memory + Settings) is never called. The persisted entries survive and the count reappears on next refresh — the feature silently lies to the user.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedOfflineModeViewModel.kt:109-117`, `shared/src/commonMain/kotlin/com/cocktailcraft/domain/usecase/ManageOfflineModeUseCase.kt`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/cache/CocktailCache.kt`.

**Approach:** Add `clearCache()` to `CocktailOfflineRepository` + `ManageOfflineModeUseCase` delegating to `CocktailCache.clearCache()`; call it from the ViewModel, then refresh state. Ensure both cache layers are purged (see SH-11).

### SH-4. Make favorites reactive and offline-safe
**Status:** ✅ Done · **Severity:** Medium

**Description:** Unlike cart/orders/reviews, `CocktailFavoritesRepository` has no `observe*()` flow — screens manually re-pull after each mutation and can drift out of sync. `addToFavorites(cocktail)` persists only the id and never caches the object, so a favorited item can silently vanish when offline.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/domain/repository/CocktailFavoritesRepository.kt:11-14`, `data/repository/CocktailFavoritesRepositoryImpl.kt:44-48,55-65`, `SharedHomeViewModel.kt:226-233`.

**Approach:** Back favorites with a `MutableStateFlow` and expose `observeFavorites()` (mirroring `CartRepository`); call `cocktailCache.cacheCocktail(cocktail)` at add-time. Migrate consumers off manual `loadFavorites()` re-pulls.

### SH-5. Fix iOS connectivity false-positives
**Status:** ✅ Done · **Severity:** Medium

**Description:** `IOSNetworkMonitor` treats `nw_path_status_satisfiable` ("could connect, but no path now") as online, and `BaseNetworkMonitor` seeds `_isOnline = true` before the first callback — both produce false-positive connectivity and defeat offline auto-switch. `NSLog` also fires unconditionally in release.

**Where to look:** `shared/src/iosMain/kotlin/com/cocktailcraft/util/NetworkMonitor.kt:24,35-36`, `shared/src/commonMain/kotlin/com/cocktailcraft/util/NetworkMonitor.kt:20`.

**Approach:** Map only `satisfied` → online; seed `_isOnline` from the monitor's initial path; gate `NSLog` behind `AppConfig.verboseNetworkLogging`/debug.

### SH-6. Harden JSON parsing against TheCocktailDB quirks
**Status:** ✅ Done · **Severity:** Medium

**Description:** `CocktailDto.id`/`name` are non-nullable with no default — one malformed record fails the entire response decode. TheCocktailDB is also known to return the literal string `"no data found"` for `drinks` on some endpoints, which would throw a `SerializationException` instead of yielding an empty list.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/remote/CocktailDto.kt:8-19`, `shared/src/commonMain/kotlin/com/cocktailcraft/di/DataModule.kt:37-42`.

**Approach:** Add `coerceInputValues = true`; make `id`/`name` nullable and filter invalid records post-parse (or use a custom list serializer); add test fixtures for a malformed record and the `"no data found"` payload (pairs with AR-5).

### SH-7. Consolidate retry/rate-limit layers
**Status:** ✅ Done · **Severity:** Medium

**Description:** Three overlapping resilience mechanisms: Ktor `HttpRequestRetry` (3× exponential, doesn't cover 429), manual backoff/min-interval in `CocktailRemoteDataSource`, and scattered `delay(100/200)` timing hacks. Layers can compound (Ktor retries a timeout 3×, then the manual layer backs off again). The `delay(100)` "for cache initialization" races the cache's own `ensureLoaded()`.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/di/NetworkModule.kt:49-56`, `data/remote/CocktailRemoteDataSource.kt:44-108`, `data/repository/CocktailDetailRepositoryImpl.kt:48,72`, `SharedHomeViewModel.kt:78`.

**Approach:** Pick one strategy: configure `HttpRequestRetry` to also retry 429 honoring `Retry-After` and delete the manual backoff, **or** keep the manual layer and disable Ktor retry. Remove the arbitrary `delay()` calls and rely on `ensureLoaded()` suspension.

### SH-8. Clean up `getCocktailById` (no-op catch + magic timeout)
**Status:** ✅ Done · **Severity:** Low/Medium

**Description:** `try { ... } catch (e) { throw e }` is a pointless rethrow, and a per-call `requestTimeoutMillis = 10000` hardcodes a value that duplicates/diverges from `AppConfig` — no other endpoint sets one.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/remote/CocktailApi.kt:41-60`, `data/config/AppConfigImpl.kt:22-26`.

**Approach:** Delete the no-op try/catch; drive the timeout from `AppConfig` or drop it in favor of the global `HttpTimeout` plugin (coordinates with AR-4).

### SH-9. Document / fix singleton ViewModel lifecycle
**Status:** ✅ Done · **Severity:** Low/Medium

**Description:** Several shared ViewModels are Koin `single`s extending `androidx.lifecycle.ViewModel` — `onCleared()` effectively never runs, their `init`-launched infinite collectors live for the whole process, and the `viewModel {}` vs `single {}` scoping split is undocumented.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/di/DomainModule.kt:53-70`, `viewmodel/SharedOfflineModeViewModel.kt:186-189`.

**Approach:** Document the scoping rationale per ViewModel; stop relying on `onCleared` for singleton teardown (moves with SH-2); consider a dedicated app-lifecycle owner for process-wide flows. (See also AN-4 for the Android-side consequence.)

### SH-10. Replace `shuffled()` in `getCocktailsByCategory`
**Status:** ✅ Done · **Severity:** Low

**Description:** A plain getter calls `.shuffled().take(limit)`, returning a different list every call — unnecessary recomposition/diffing on Android and unstable results for iOS.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedHomeViewModel.kt:302-317`.

**Approach:** Seed the selection by cocktail id (like the deterministic demo price/rating derivation in `CocktailRemoteDataSource.kt:121-144`) or memoize per category.

### SH-11. Unify the two cache layers
**Status:** ✅ Done · **Severity:** Low

**Description:** `CocktailCacheManager` (in-memory, 5-min validity, rate-limit state) and `CocktailCache` (persistent LRU, 24h TTL, offline-aware) hold overlapping data with independent expiry; clearing one doesn't clear the other; fetchers write to both.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/cache/CocktailCacheManager.kt`, `data/cache/CocktailCache.kt`, `data/repository/CocktailCategoryFetcher.kt:80-81`.

**Approach:** Longer-term, one cache abstraction with one TTL policy and one invalidation entry point. Short-term: document the eviction matrix and make every `clearCache` path purge both (pairs with SH-3, AR-7).

### SH-12. Trim ErrorHandler string-matching; expand Turbine tests
**Status:** ✅ Done · **Severity:** Low

**Description:** The typed exception classification is good, but the fallback branch pattern-matches message substrings (`"timeout"`, `"404"`, `"connect"`) — locale/format-fragile and can misclassify. Test-side, Turbine is used in only one test; most flow-bearing ViewModels assert final `uiState.value` snapshots instead of emission sequences.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/util/ErrorHandler.kt:167-213`, `shared/src/commonTest/.../SharedCartViewModelTest.kt`.

**Approach:** After AR-2 lands, reduce the fallback to a single generic case. Add Turbine-based emission tests for Home network-restore retry, Cart optimistic revert, and the Order/Review observers.

### SH-13. Add debounce to the shared search flow
**Status:** ✅ Done · **Severity:** High

**Description:** Neither platform debounces search — both fire a request per keystroke (see AN-9, IO-1). The fix belongs in shared so both platforms benefit and stale results can't win races.

**Where to look:** `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedHomeViewModel.kt` (search entry point), plus the platform call sites in AN-9/IO-1.

**Approach:** Model the query as a `MutableStateFlow<String>` inside the shared ViewModel; apply `debounce(300)` + `distinctUntilChanged()` + `flatMapLatest { search(it) }` so superseded searches are cancelled. Platforms just push query text. Handle the empty-query case by clearing results.

---

## Android

### AN-1. Make domain models Compose-stable
**Status:** ✅ Done · **Severity:** High

**Description:** `Cocktail` contains `ingredients: List<Ingredient>`, making it unstable to the Compose compiler — `AnimatedCocktailItem`/`CocktailItemContent` are non-skippable, so every parent recomposition (scroll, favorite toggle, pagination append) re-executes all visible rows. This undermines the perf story of the main scrolling surface.

**Where to look:** `androidApp/.../ui/components/AnimatedCocktailList.kt:116-133`, `ui/components/AnimatedCocktailItem.kt:62,114,202-210`, `shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/Cocktail.kt`.

**Approach:** Either mark `Cocktail`/`Ingredient` `@Immutable` in shared (or switch to `kotlinx.collections.immutable.ImmutableList`), or add a Compose **stability configuration file** listing `com.cocktailcraft.domain.model.*`. Verify with compose compiler metrics reports that the item composables become skippable.

### AN-2. Schedule (or delete) `WidgetUpdateWorker`
**Status:** ✅ Done · **Severity:** High

**Description:** `WidgetUpdateWorker.schedule()` has zero callers — only `BackgroundSyncWorker` is scheduled at startup. The Favorites widget has no refresh path at all; the Random widget only updates on manual tap. Widget data goes stale indefinitely.

**Where to look:** `androidApp/.../widget/WidgetUpdateWorker.kt:106`, `di/WidgetModule.kt:4,11-14`, `CocktailCraftApplication.kt:25`.

**Approach:** Call `WidgetUpdateWorker.schedule(context)` from `Application.onCreate` or from widget receivers' `onEnabled`/`onUpdate`, and cancel in `onDisabled`. If the worker was intentionally abandoned, delete it and the unused import.

### AN-3. Eliminate O(n²) favorites lookup in lists
**Status:** ✅ Done · **Severity:** Medium/High

**Description:** `favorites.any { it.id == cocktail.id }` runs per row inside lazy item scopes across three screens — O(favorites × visible items) per pass, compounding AN-1 because `favorites` is itself an unstable `List` param.

**Where to look:** `AnimatedCocktailList.kt:129`, `ui/screens/CartScreen.kt:126`, `ui/screens/CocktailDetailScreen.kt:151`.

**Approach:** Hoist `remember(favorites) { favorites.mapTo(HashSet()) { it.id } }` once per screen and pass `isFavorite = id in favoriteIds` — or better, expose a `Set<String>` of favorite ids from the shared ViewModel (pairs with SH-4).

### AN-4. Process-death / SavedStateHandle strategy
**Status:** ✅ Done (documented limitation + search-query recovery; full koinViewModel migration deliberately out of scope) · **Severity:** Medium

**Description:** All shared VMs are resolved via `koinInject()` as app singletons; no `SavedStateHandle` exists anywhere in `androidApp`. State survives config changes but is lost on process death — the user returns to a reset Home/search. `onCleared` never runs on the singletons.

**Where to look:** `ui/screens/MainScreen.kt:109-113`, `ui/screens/ProfileScreen.kt:86-87`, `ui/screens/HomeScreen.kt:117-139`.

**Approach:** Either migrate to lifecycle `ViewModel`s (`koinViewModel()`) with `SavedStateHandle` persisting query/filters/selected category, or add explicit state persistence in shared. At minimum, document that process-death restoration is unsupported. (Coordinates with SH-9.)

### AN-5. Consolidate theme systems; remove global mutable `AppColors`
**Status:** 🟡 In Progress — dead `CocktailBarTheme` (incl. deprecated `window.statusBarColor`) deleted; the ~320-call-site `AppColors.*` → `MaterialTheme.colorScheme` migration and removal of the mutable global remain a follow-up · **Severity:** Medium

**Description:** Dead `CocktailBarTheme` duplicates the actually-used `AnimatedCocktailBarTheme`; `AppColors` exposes a process-global `var isDarkTheme by mutableStateOf(false)` with ~300 direct call sites bypassing `MaterialTheme.colorScheme`. Blocks dynamic color; three color definitions must be hand-synced. `CocktailBarTheme` also uses the deprecated `window.statusBarColor`.

**Where to look:** `ui/theme/Theme.kt:50,112-116`, `ui/theme/AnimatedTheme.kt:70-73`, `MainActivity.kt:41`.

**Approach:** Delete `CocktailBarTheme`; incrementally migrate `AppColors.*` reads to `MaterialTheme.colorScheme` (mechanical but wide); remove the mutable global last. Consider enabling dynamic color once migrated.

### AN-6. Fix edge-to-edge insets on the app bar
**Status:** ✅ Done · **Severity:** Medium

**Description:** `enableEdgeToEdge()` is on, but the `topBar` slot wraps `OfflineModeIndicator` *above* `TopAppBar` in a `Column` — the indicator can render behind the status bar, or the inset padding lands between indicator and title.

**Where to look:** `MainActivity.kt:23`, `ui/screens/MainScreen.kt:131-183`, `ui/screens/OfflineModeScreen.kt`.

**Approach:** Apply `Modifier.statusBarsPadding()` to the wrapping `Column` and set the `TopAppBar`'s `windowInsets = WindowInsets(0)` so the inset is owned in exactly one place.

### AN-7. Migrate deprecated Compose Material APIs
**Status:** ✅ Done · **Severity:** Medium

**Description:** Material2 pull-refresh (`androidx.compose.material.pullrefresh.*`) is mixed into a Material3 app; `Divider` and non-auto-mirrored `Icons.Filled.ArrowBack` are used in several screens — the latter is an RTL bug given `supportsRtl="true"`.

**Where to look:** `ui/screens/HomeScreen.kt:45-47,404-408,454`, `ui/screens/MainScreen.kt:15,167,186`, plus `OrderSummaryCard`, `SearchFilterComponents`, `OrderListScreen`, `CocktailDetailScreen`, `OfflineModeScreen`.

**Approach:** Adopt `PullToRefreshBox` (Material3), `HorizontalDivider`, and `Icons.AutoMirrored.Filled.ArrowBack` across the listed files; drop the Material2 dependency surface if nothing else uses it.

### AN-8. Widget strings, sentinel state, and dead image code
**Status:** ✅ Done · **Severity:** Medium

**Description:** `"Tap to load"`, `"No cocktail found"`, `"Failed to load"` are hardcoded (Compose lint rule only covers XML), and `cocktailName == "Tap to load"` is used as a state sentinel — breaks under localization. Unused `Bitmap`/`BitmapFactory`/`URL` imports and a stored-but-never-rendered `IMAGE_URL`.

**Where to look:** `widget/RandomCocktailWidget.kt:52-54,75,147,315,318,324`, `widget/WidgetUpdateWorker.kt`, `androidApp/build.gradle.kts:106-111`.

**Approach:** Move strings to `strings.xml`; replace the sentinel with an explicit state enum/flag in widget state; either render the cached image via Glance `ImageProvider` or delete `IMAGE_URL` and the dead imports.

### AN-9. Tighten HomeScreen effects & search launch
**Status:** ✅ Done · **Severity:** Low/Medium

**Description:** `LaunchedEffect(cocktails)` re-runs on every list mutation just to conditionally `clearError()`; a hand-rolled `isFirstComposition` flag duplicates `NavigationManager` restore behavior; search launches a coroutine per keystroke with no debounce at this layer.

**Where to look:** `ui/screens/HomeScreen.kt:147-164,176-183,198`.

**Approach:** Key the error-clear effect on `errorMessage` instead of the whole list; remove the first-composition flag if `NavigationManager` already covers it; rely on SH-13 (shared debounce) rather than adding one here.

### AN-10. WorkManager: Koin `WorkerFactory`
**Status:** ✅ Done · **Severity:** Low

**Description:** `BackgroundSyncWorker` is a `KoinComponent` using global `by inject()` — dependent on default startup init ordering and hard to constructor-test; no `Configuration.Provider` exists.

**Where to look:** `worker/BackgroundSyncWorker.kt:24-26`, `CocktailCraftApplication.kt`.

**Approach:** Add a Koin-backed `WorkerFactory` (`Configuration.Provider` on the Application) and constructor-inject `BackgroundSyncService`.

### AN-11. Defer non-critical startup work
**Status:** ✅ Done · **Severity:** Low

**Description:** `Application.onCreate` runs Koin startup then WorkManager scheduling inline on the main thread — WorkManager DB touch on the cold-start critical path.

**Where to look:** `CocktailCraftApplication.kt:14-26`.

**Approach:** Keep Koin early; move sync/widget scheduling off the critical path (post-first-frame dispatch or `androidx.startup` initializer).

### AN-12. Automate versionCode/versionName
**Status:** ✅ Done (folded into AR-9) · **Severity:** Low

**Description:** `versionCode = 1` / `versionName = "1.0"` hardcoded with no automation. (Signing/R8/shrink config is otherwise solid; keystore correctly gitignored.)

**Where to look:** `androidApp/build.gradle.kts:29-30`.

**Approach:** Derive from git tag / CI build number; fold into AR-9's single-source version.

### AN-13. Deepen the baseline profile journey
**Status:** ✅ Done · **Severity:** Low

**Description:** The generator covers cold start + tab switches only — it never scrolls the list, opens detail, or searches (the hottest composition paths), and matches literal English text, which breaks on string changes or non-en locales.

**Where to look:** `baselineprofile/src/main/java/.../BaselineProfileGenerator.kt:32-38`.

**Approach:** Add scroll + open-detail + search interactions; match by `testTag`/resource-id instead of display text.

---

## iOS

### IO-1. Debounce + cancel search on iOS
**Status:** ✅ Done · **Severity:** High

**Description:** `onChange(of: searchText)` launches a new unstructured `Task` per keystroke — typing "margarita" fires 9 concurrent calls; the last-*completed* (not last-typed) query wins. Deleting back to empty does nothing, leaving stale results (unlike the explicit clear button).

**Where to look:** `iosApp/CocktailCraft/Views/HomeViewSKIE.swift:48-54,75-78`.

**Approach:** Preferred: land SH-13 (shared debounce) and just push query text. iOS-side stopgap: `.task(id: searchText)` with a `Task.sleep`-based debounce so superseded searches auto-cancel; explicitly clear results on empty query.

### IO-2. Remove artificial pull-to-refresh delays
**Status:** ✅ Done · **Severity:** Medium

**Description:** Hardcoded `Task.sleep` calls ("Simulate refresh with delay") add 1s (Home) / 0.5s (Favorites) of dead time to every pull-to-refresh — placeholder scaffolding that shipped.

**Where to look:** `iosApp/CocktailCraft/ViewModels/HomeViewModelSKIE.swift:127`, `FavoritesViewModelSKIE.swift:88`.

**Approach:** Delete the sleeps and call the load directly. If a minimum spinner duration is wanted for polish, implement it as an explicit minimum-duration wrapper, not a blanket sleep.

### IO-3. Delete dead `PlatformErrorHandler`
**Status:** ✅ Done · **Severity:** Medium

**Description:** ~60 lines with zero call sites; reimplements NSURLError → `UserFriendlyError` mapping the shared error flow already handles. Presents a false picture of how iOS errors are bridged.

**Where to look:** `iosApp/CocktailCraft/Utils/PlatformErrorHandler.swift` (whole file).

**Approach:** Delete it. If iOS-specific `NSError` mapping is ever genuinely needed, wire it into the one place raw `NSError`s can surface.

### IO-4. Fix phantom `SceneDelegate` in Info.plist
**Status:** ✅ Done · **Severity:** Medium

**Description:** `UISceneDelegateClassName` points at `$(PRODUCT_MODULE_NAME).SceneDelegate`, which doesn't exist (the app is pure SwiftUI `@main`). Meanwhile `project.yml` sets `UIApplicationSceneManifest_Generation: true`, so the manifest has two sources of truth.

**Where to look:** `iosApp/CocktailCraft/Info.plist:34-35`, `iosApp/project.yml:41`, `CocktailCraftApp.swift`.

**Approach:** Remove the manual `UIApplicationSceneManifest`/`UISceneDelegateClassName` block from Info.plist and let SwiftUI/XcodeGen generation own it. Verify launch behavior after `xcodegen generate && pod install`.

### IO-5. Remove stale `armv7` device capability
**Status:** ✅ Done · **Severity:** Medium

**Description:** `UIRequiredDeviceCapabilities = armv7` is 32-bit boilerplate on an iOS 17 (arm64-only) app — misrepresents the requirement and can trip App Store validation.

**Where to look:** `iosApp/CocktailCraft/Info.plist:46`.

**Approach:** Change to `arm64` or delete the key (deployment target 17.0 already implies 64-bit).

### IO-6. Introduce localization + locale-aware prices
**Status:** ✅ Done (String Catalogs seeded for app+widgets; shared-KMP price formatting remains the cross-platform follow-up) · **Severity:** Medium

**Description:** No `Localizable.strings`/`.xcstrings` and zero `String(localized:)` anywhere in app or widgets — every label is a literal. Widget prices hardcode USD via `String(format: "$%.2f", ...)`; price formatting is duplicated between app (`.asPrice` from shared) and widget (which doesn't link the framework) and can diverge.

**Where to look:** `iosApp/CocktailCraftWidgets/FavoritesWidget.swift:81-84,125`, `RandomCocktailWidget` price code, `CocktailDetailViewModelSKIE.swift:93,106-110`.

**Approach:** Add a String Catalog and route user copy through it; use `NumberFormatter`/`Decimal` currency style in the widget. Longer-term, move price formatting into shared (cross-cutting theme with Android).

### IO-7. Grow native test coverage
**Status:** ✅ Done · **Severity:** Medium

**Description:** `CoreUnitTests.swift` covers only trivial `AppRouter` props, `DeliveryPolicy` bridge, and `OrderStatus` mapping. Untested iOS-specific logic: `WidgetDeepLink` URL parsing (deep-link critical path), `WidgetSnapshotStore` encode/decode, `SharedViewModelWrapper` state-seeding/mirroring/cancellation, `BackgroundSyncManager` budget math. No UI test covers widget deep-link → detail (the headline Phase-4 feature).

**Where to look:** `iosApp/CocktailCraftTests/CoreUnitTests.swift`, `iosApp/CocktailCraft/Utils/WidgetBridge.swift:20-32`, `SharedViewModelWrapper.swift`, `Utils/BackgroundSyncManager.swift:136-146`, `iosApp/CocktailCraftUITests/`.

**Approach:** Add pure-Swift unit tests for `WidgetDeepLink`, `WidgetSnapshotStore` round-trip, wrapper lifecycle, and sync budget math; add a deep-link launch UI test (open the `cocktailcraft://` URL and assert detail appears).

### IO-8. Pick one project source of truth (XcodeGen vs pbxproj)
**Status:** ✅ Done (project.yml declared authoritative; pbxproj stays committed as generated output — gitignoring it is a repo-layout decision deferred until CI exists) · **Severity:** Low/Medium

**Description:** Both the XcodeGen spec and the generated `project.pbxproj` are committed, with CocoaPods layered on top — they can silently diverge and reviewers can't tell which is authoritative (they're in sync today).

**Where to look:** `iosApp/project.yml`, `iosApp/CocktailCraft.xcodeproj/project.pbxproj`, `iosApp/Podfile`, `iosApp/CocktailCraftTests/TestSetup.swift:11-13`.

**Approach:** Either gitignore the `.xcodeproj` and document `xcodegen generate && pod install` as bootstrap (add to CI, pairs with AR-1), or drop XcodeGen. Document the CocoaPods ordering either way.

### IO-9. Gate release logging
**Status:** ✅ Done · **Severity:** Low

**Description:** ~15 unconditional `print()` calls in `BackgroundSyncManager` (plus `WidgetDataBridge`, `KoinInitializer`) log in release builds; other files already use the `#if DEBUG` pattern correctly — it's just inconsistent.

**Where to look:** `iosApp/CocktailCraft/Utils/BackgroundSyncManager.swift:66,95-97,168,190,213,222`, `WidgetDataBridge.swift`, `KoinInitializer.swift`; good examples at `Utils/WidgetBridge.swift:66-70`, `SharedViewModelWrapper.swift:80-84`.

**Approach:** Route through `os.Logger` (preferred) or wrap remaining `print`s in `#if DEBUG`.

### IO-10. Remove `BGTask` force-casts
**Status:** ✅ Done · **Severity:** Low

**Description:** `task as! BGAppRefreshTask` / `as! BGProcessingTask` crash on identifier/type misconfig; the identifiers are duplicated as string literals in three places, so a future edit could desync them.

**Where to look:** `iosApp/CocktailCraft/Utils/BackgroundSyncManager.swift:18-19,55,63`, `Info.plist:79-80`.

**Approach:** `guard let refresh = task as? BGAppRefreshTask else { task.setTaskCompleted(success: false); return }`; centralize the identifiers as constants referenced by the manager.

### IO-11. Reset nav stack on widget deep link
**Status:** ✅ Done · **Severity:** Low

**Description:** `openCocktailDetail(id:)` appends onto the existing Home path — tapping a widget while deep in the Home stack pushes on top; repeated taps stack duplicate detail screens.

**Where to look:** `iosApp/CocktailCraft/Utils/AppRouter.swift:21-24`.

**Approach:** Reset the path before appending (`homePath = NavigationPath(); homePath.append(id)`), or no-op if the top of stack already equals `id`.

### IO-12. Audit `@preconcurrency import shared`
**Status:** ✅ Done (audited: single occurrence; near-no-op under Swift 5.10 minimal checking; documented as tech debt with the strict-concurrency trigger) · **Severity:** Low

**Description:** Whole-module suppression of Sendable/actor-isolation diagnostics from the KMP framework hides exactly the main-actor issues that matter when SKIE types cross threads; will need revisiting for Swift 6 strict concurrency.

**Where to look:** `iosApp/CocktailCraft/Views/CocktailDetailView.swift:2`.

**Approach:** Track as tech debt; audit which SKIE types genuinely need it and scope the suppression narrowly rather than at the import.

### IO-13. Verify `@State` ViewModel churn on detail
**Status:** ✅ Done (DEBUG instrumentation added per the "instrument first" approach; restructuring only if the logs show frequent churn) · **Severity:** Low

**Description:** `@State private var viewModel = CocktailDetailViewModelSKIE()` evaluates its initializer on every parent re-render; each instance is a Koin *factory* whose `deinit` calls `onCleared()` — discarded throwaway instances spin up and immediately tear down a shared coroutine scope. Functionally safe (self-documented) but potentially wasteful.

**Where to look:** `iosApp/CocktailCraft/Views/CocktailDetailView.swift:10`, `HomeViewModelSKIE.swift:19-21`, detail VM `deinit` at `CocktailDetailViewModelSKIE.swift:33-38`.

**Approach:** Instrument how often throwaway instances are created; if frequent, lazily resolve the shared VM inside `.task` or use a lighter factory.

---

## Suggested execution order

1. **SH-1, SH-2, SH-3** — bugs that hide failures or can crash (shared, user-visible).
2. **SH-13 + IO-1 + AN-9** — one shared debounce fix closes the per-keystroke problem on both platforms.
3. **AR-1** — CI, so everything after this is guarded.
4. **AR-2 + SH-12** — typed error codes end-to-end.
5. **AN-1, AN-2, AN-3** — Android perf + broken widget refresh.
6. **IO-2, IO-3, IO-4, IO-5** — quick iOS cleanups (scaffolding, dead code, plist).
7. Remaining medium/low items opportunistically, alongside the area being touched.
