# CocktailCraft — Improvement Plan

> Generated 2026-07-07 from a full-codebase audit (shared KMP module, Android app, iOS app).
> Priorities: **P0** = broken behavior / must fix · **P1** = security & data integrity · **P2** = quality, consistency, performance · **P3** = polish & backlog.
> Effort: **S** = < half day · **M** = 1–2 days · **L** = multi-day.

## Summary scoreboard

| Area | P0 | P1 | P2 | P3 | Total |
|---|---|---|---|---|---|
| Shared (KMP) | 2 | 1 | 8 | 4 | 15 |
| Security | 0 | 5 | 3 | 3 | 11 |
| Android | 3 | 1 | 8 | 3 | 15 |
| iOS | 4 | 1 | 7 | 4 | 16 |

---

# 1. Shared module (KMP)

## P0 — Broken behavior

### 1.1 Pagination loads the wrong category ("load more" bug) — ✅ Done (branch `shared/1.1-pagination-category`, commit `9d8983d`)
- [x] **Severity:** High · **Effort:** S–M
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedHomeViewModel.kt:154-180`, `shared/src/commonMain/kotlin/com/cocktailcraft/domain/usecase/LoadCocktailsByCategoryUseCase.kt:22-26`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CocktailRepositoryImpl.kt:239-245`
- **Issue:** `SharedHomeViewModel.loadMoreCocktails()` calls `LoadCocktailsByCategoryUseCase.loadMore(emptyList(), page, PAGE_SIZE)`. The use case ignores its `allCocktails` parameter and unconditionally calls `catalogRepository.getCocktailsSortedByNewest()`, which is hardcoded to fetch the `"Cocktail"` category. Paging past page 1 while browsing **any other category** returns items from the wrong category (or repeats).
- **Fix:** Thread the active category (or the already-fetched full result list) through `loadMore(category, page, pageSize)`. Have the use case page over `filterByCategory(category)` results instead of the hardcoded default. Add a unit test in `commonTest` that pages page 2 of a non-default category and asserts category membership.

### 1.2 Theme settings silently fail to persist — ✅ Done (branch `shared/1.2-theme-persistence`, commit `fa4dc6b`)
- [x] **Severity:** High (user-visible data loss) · **Effort:** S–M
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedThemeViewModel.kt:90-124, 299-326`, `shared/src/commonMain/kotlin/com/cocktailcraft/domain/model/User.kt:37-41`, `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/state/ThemeUiState.kt:11-19`
- **Issue:** `setAccentColor`, `setFontSize`, `toggleHighContrast`, `toggleReducedMotion` all call `savePreferences()`, but the persisted `UserPreferences` model only contains `darkMode`/`followSystemTheme`. The four other settings silently reset to defaults on every app restart, despite code that looks like it saves them.
- **Fix:** Add `accentColor`, `fontSize`, `isHighContrast`, `isReducedMotion` to `UserPreferences` (with serialization defaults for migration) and round-trip them in `savePreferences()`/load. Alternatively, remove the misleading `savePreferences()` calls if these settings are intentionally session-only. Add a persistence round-trip test for `SharedThemeViewModel`.

## P1 — Data integrity / repo hygiene

### 1.3 Prebuilt `shared.xcframework` (~138 MB) committed to git — ✅ Done (branch `shared/1.3-untrack-xcframework`, commit `55396f8`)
- [x] **Severity:** High (repo health) · **Effort:** S
- **Files:** `shared/shared.xcframework/**` (9 tracked files incl. 57 MB + 81 MB Mach-O binaries), `.gitignore`
- **Issue:** The compiled iOS framework is tracked in git with no ignore rule. Every regeneration re-commits ~138 MB of binary diff, permanently bloating clone size and history.
- **Fix:** Add `shared/shared.xcframework/` (or `*.xcframework`) to `.gitignore`, `git rm -r --cached shared/shared.xcframework`, and produce the framework as a local/CI build artifact (the Podfile/podspec already builds `shared` from source for the app).

## P2 — Architecture & performance

### 1.4 No dispatcher confinement — disk I/O and JSON work on the Main thread — ✅ Done (branch `shared/1.4-io-dispatcher`, commit `5576335`)
- [x] **Severity:** Medium-High · **Effort:** M
- **Files:** all `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/*.kt`, `data/cache/*.kt` (zero `withContext`/`Dispatchers.IO|Default` matches module-wide)
- **Issue:** Repository suspend functions do synchronous `Settings` (SharedPreferences/NSUserDefaults) reads/writes and `Json.encode/decode` of full lists on whatever dispatcher the caller supplies. ViewModels call them from `viewModelScope` (Main), so disk I/O and serialization of potentially 100-item lists run on the Main thread.
- **Fix:** Confine at the repository boundary: inject a `CoroutineDispatcher` (IO on Android, `Dispatchers.Default`/IO equivalent on iOS) via Koin and wrap Settings/JSON work in `withContext(ioDispatcher)`. Injecting the dispatcher keeps repositories testable with a test dispatcher.

### 1.5 Cache persists the entire cache on every single write (O(n²)) — ✅ Done (branch `shared/1.5-cache-batch-persist`, commit `e0d50ba`)
- [x] **Severity:** Medium-High · **Effort:** S–M
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/cache/CocktailCache.kt:137-172`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CocktailRepositoryImpl.kt:195-196`
- **Issue:** `cacheCocktail()` re-serializes **all** cached cocktails (up to 100) to JSON and writes to `Settings` on every call. `fetchCocktailsByCategory()` calls it once per item in a `forEach`, so a 20-item category load triggers ~20 full-cache serialize+write cycles.
- **Fix:** Add a `cacheAll(cocktails: List<Cocktail>)` batch API that mutates memory then persists once. Optionally debounce persistence (e.g., persist on a small delay or at natural sync points). Combine with 1.4 so the single write happens off-Main.

### 1.6 Two near-duplicate favorites stacks — ✅ Done (branch `shared/1.6-dedupe-favorites`, commit `1994657`)
- [x] **Severity:** Medium · **Effort:** S
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/domain/repository/FavoritesRepository.kt`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/FavoritesRepositoryImpl.kt:14-54`, consumer: `androidApp/src/main/java/com/cocktailcraft/android/widget/WidgetDataProvider.kt:21`
- **Issue:** `FavoritesRepository`/`FavoritesRepositoryImpl` duplicates `CocktailFavoritesRepository`/`Impl` with renamed methods. The impl delegates 4 of 5 methods but **reimplements `toggleFavorite()` with its own direct `Settings` access** — duplicated business logic that can drift. Its only consumer is the Android widget.
- **Fix:** Delete `FavoritesRepository`/`FavoritesRepositoryImpl`, point `WidgetDataProvider` at `CocktailFavoritesRepository`, and remove the DI binding.

### 1.7 Finish the repository interface segregation — ✅ Done (branch `shared/1.7-split-cocktail-repository`, commit `c129e69`)
- [x] **Severity:** Medium · **Effort:** M
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CocktailRepositoryImpl.kt:33-342`
- **Issue:** Consumers correctly inject the 5 narrow interfaces (Search/Detail/Catalog/Favorites/Offline), but `CocktailRepositoryImpl` still implements Search + Detail + Catalog in one 342-line class — only Favorites/Offline were extracted. The segregation is real for consumers but cosmetic for the implementation.
- **Fix:** Split into `CocktailSearchRepositoryImpl`, `CocktailDetailRepositoryImpl`, `CocktailCatalogRepositoryImpl`, sharing the remote data source / cache manager via constructor injection. Update `DataModule` bindings; the composite `CocktailRepository` interface can then be deleted if nothing injects it.

### 1.8 DIP violation: dependencies on concrete `CocktailOfflineRepositoryImpl` — ✅ Done (branch `shared/1.8-offline-repo-interface`, commit `f0c4f32`)
- [x] **Severity:** Medium · **Effort:** S
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CocktailRepositoryImpl.kt:38`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CocktailFavoritesRepositoryImpl.kt:21`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/CocktailOfflineRepositoryImpl.kt:33`
- **Issue:** Both classes take the **concrete** `CocktailOfflineRepositoryImpl` (not the interface) because they need its internal `isOffline()` helper — coupling two repositories to an implementation detail and making them harder to fake in tests.
- **Fix:** Add `suspend fun isOffline(): Boolean` to the `CocktailOfflineRepository` interface (or extract a small `OfflineModePolicy` collaborator) and depend on the abstraction.

### 1.9 Dead/disconnected cache configuration — no time-based expiry — ✅ Done (branch `shared/1.9-cache-ttl`, commit `dacf244`)
- [x] **Severity:** Medium · **Effort:** S–M
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/domain/config/AppConfig.kt:70,80`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/config/AppConfigImpl.kt:39-40`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/cache/CocktailCacheManager.kt:36`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/cache/CocktailCache.kt:80-81`
- **Issue:** `AppConfig.cacheExpirationMs` (24 h) and `maxOfflineCocktails` (50) are never referenced anywhere. The enforced values are hardcoded elsewhere (`CACHE_VALIDITY_MS = 5 min` in-memory; `MAX_CACHED_COCKTAILS = 100`). The persistent `CocktailCache` has **no age-based expiration at all** — only LRU size eviction — contradicting the "24 hour cache expiration" comment.
- **Fix:** Wire `cacheExpirationMs` into `CocktailCache` (store a cached-at timestamp per entry, evict stale entries on read/startup) and use `maxOfflineCocktails` for the size cap — or delete both from `AppConfig` and keep the constants where they're enforced. Either way, one source of truth.

### 1.10 `ErrorHandler` has two parallel error-construction APIs — ✅ Done (branch `shared/1.10-errorhandler-consolidation`, commit `44cfebe`)
- [x] **Severity:** Low-Medium · **Effort:** S–M
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/util/ErrorHandler.kt` (392 lines: classification path at 55-224 vs `create*` family at 273-392), `androidApp/src/main/java/com/cocktailcraft/android/util/ErrorUtils.kt`
- **Issue:** The `getErrorFromException`/`errorFromCode` path (used by `SharedViewModel`) and the `createUserFriendlyError`/`createErrorFromException`/`createErrorFromErrorCode` family (only reached via Android's `ErrorUtils` pass-through) duplicate title/message/category logic. Two paths producing the same `UserFriendlyError` will drift.
- **Fix:** Consolidate on the classification path; make `ErrorUtils` (or the Android call sites) call into it; delete the `create*` duplicates.

### 1.11 Test coverage gaps in shared logic — ✅ Done (branch `shared/1.11-test-coverage`, commit `df31f98`)
- [x] **Severity:** Medium · **Effort:** L (incremental)
- **Files:** `shared/src/commonTest/**` (14 files / ~1.4k lines vs commonMain 85 files / ~6.6k lines)
- **Issue:** Untested: `SharedProfileViewModel` (385 lines), `SharedThemeViewModel` (327 — would have caught 1.2), `SharedOrderViewModel`, `SharedOfflineModeViewModel`, `SharedCocktailDetailViewModel`; `CartRepositoryImpl`, `OrderRepositoryImpl`; `CocktailCache`, `CocktailCacheManager`, `CocktailRemoteDataSource`; `ErrorHandler`; most use cases including `LoadCocktailsByCategoryUseCase` (whose bug 1.1 is untested). No Koin `checkModules()` DI-graph verification.
- **Fix:** Prioritize: (1) regression test for 1.1, (2) `SharedThemeViewModel` persistence round-trip for 1.2, (3) `CocktailCache` eviction/expiry, (4) a Koin `checkModules()` test so DI wiring breaks fail in CI, then backfill remaining ViewModels using the existing `testutil/Fakes.kt` pattern.

## P3 — Cleanups

### 1.12 Dead `ToggleFavoriteUseCase` — ✅ Done (branch `shared/1.12-remove-dead-usecase`, commit `fa90145`)
- [x] **Severity:** Low · **Effort:** S
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/domain/usecase/ToggleFavoriteUseCase.kt`, `shared/src/commonTest/kotlin/com/cocktailcraft/domain/usecase/ToggleFavoriteUseCaseTest.kt`
- **Issue:** Duplicates `ManageFavoritesUseCase.toggle()`; never registered in DI; no production call sites — only its own test exercises it.
- **Fix:** Delete the use case and its test (or consolidate the two into one, keeping a single toggle implementation).

### 1.13 `OrderRepository` redundant near-duplicate methods — ✅ Done (branch `shared/1.13-order-repository-api`, commit `641334b`)
- [x] **Severity:** Low · **Effort:** S
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/domain/repository/OrderRepository.kt:8-16`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/OrderRepositoryImpl.kt:47,94-113`
- **Issue:** `getOrders()` and `getOrderHistory()` have identical implementations; `addOrder()` and `placeOrder()` largely overlap (the latter also generates an id). The interface comment "Methods needed by the implementation" signals unresolved API indecision.
- **Fix:** Collapse to one get-all method and one create method (`placeOrder`), update call sites, delete the rest.

### 1.14 Magic `"Cocktail"` default-category literal repeated 7+ times — ✅ Done (branch `shared/1.14-default-category-constant`, commit `18ac23a`)
- [x] **Severity:** Low · **Effort:** S
- **Files:** `SharedHomeViewModel.kt:78,89,97,390`, `CocktailRepositoryImpl.kt:241,277`, `GetCocktailDetailUseCase.kt:30`, canonical list at `domain/model/CocktailCategories.kt:10`
- **Issue:** The default/fallback category string is independently retyped across ViewModel, repository, and use case layers — a typo or rename breaks behavior silently.
- **Fix:** Add `CocktailCategories.DEFAULT = "Cocktail"` and reference it everywhere.

### 1.15 Dead Android DI bindings & inconsistent `createNetworkMonitor` expect/actual — ✅ Done (branch `shared/1.15-dead-di-bindings`, commit `b1a4313`)
- [x] **Severity:** Low · **Effort:** S
- **Files:** `shared/src/androidMain/kotlin/com/cocktailcraft/di/PlatformModule.kt:20-27`, `shared/src/commonMain/kotlin/com/cocktailcraft/util/NetworkMonitor.kt:16`, `shared/src/androidMain/kotlin/com/cocktailcraft/util/NetworkMonitor.kt`
- **Issue:** androidMain registers a `DataStore<Preferences>` + `FlowSettings` nobody injects (the real `Settings` is `SharedPreferencesSettings`). The `createNetworkMonitor()` expect/actual works on iOS but **throws `IllegalStateException` on Android** ("use DI instead") — an inconsistent contract.
- **Fix:** Remove the unused DataStore/FlowSettings bindings (unless migrating to DataStore is planned — then actually migrate). Replace the expect/actual with DI-only construction on both platforms so no platform has a booby-trapped function.

---

## Section 1 integration status

All 15 task branches are merged into **`integration/shared-phase-1`** (verified: 136 shared tests green, Android debug + androidTest compile, iOS Kotlin compile). Follow-up fixes landed on the same branch:
- `293fdbe` — signUp now refreshes auth status (`isLoggedIn` was stale after sign-up)
- `1d1e409` — composite `CocktailRepository` interface/impl/binding and unused `KoinIOS.getCocktailRepository()` deleted
- `8d51448` — cache tests use the shared `ALL_COCKTAILS_KEY` constant instead of a retyped literal
- `f128b73` — androidTest fake implements the new `isOffline()` interface member

Remaining 1.4 caveats CLOSED (`2c69c5b` on the integration branch): Cart/Order/Review repositories now lazy-load behind a mutex-guarded `ensureLoaded()` on the IO dispatcher instead of doing disk I/O in `init {}`, and `setOfflineMode`/`isOfflineModeEnabled` are suspend + IO-confined (ViewModel signatures unchanged). Full Xcode simulator build of the iOS app against the integration branch: **succeeded** (SKIE surface changes verified end-to-end). Section 1 is fully verified and merged into `dev`; the integration branch has been deleted.

---

# 2. Security

## P1 — Fix before any real distribution

### 2.1 Credentials & PII stored in unencrypted platform storage — ✅ Done (branch `security/2.1-encrypted-storage`, commit `7fa7536`)
- [x] **Severity:** High · **Effort:** M
- **Files:** `shared/src/androidMain/kotlin/com/cocktailcraft/di/PlatformModule.kt:29-33` (plain `SharedPreferencesSettings`), `shared/src/iosMain/kotlin/com/cocktailcraft/di/PlatformModule.kt:10-11` (`NSUserDefaultsSettings`), `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/AuthRepositoryImpl.kt:216-229`
- **Issue:** Password hashes (`password_<email>`) and the full `users` record (email, name, phone, physical address as plaintext JSON) live in plain SharedPreferences XML (Android) and an unencrypted NSUserDefaults plist (iOS). Readable via rooted/jailbroken device, backup extraction, or device-migration tooling. Neither Keystore nor Keychain is used anywhere.
- **Fix:** Android: back the auth `Settings` with `androidx.security.crypto.EncryptedSharedPreferences` (Keystore-backed) — multiplatform-settings accepts any `SharedPreferences` instance. iOS: move auth keys to Keychain (`KeychainSettings` from multiplatform-settings, or `kSecClassGenericPassword` with `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`). Migrate existing values on first launch, then delete them from the old store. Coordinate with 2.2.

### 2.2 `android:allowBackup="true"` with no exclusion rules — ✅ Done (branch `security/2.2-backup-rules`, commit `72049fe`)
- [x] **Severity:** Medium (compounds 2.1) · **Effort:** S
- **Files:** `androidApp/src/main/AndroidManifest.xml:9`
- **Issue:** With backup enabled and no rules, `adb backup` / device-to-device transfer / cloud backup can extract `cocktailcraft_prefs.xml` — including credentials and PII — widening the attack surface beyond "attacker has root."
- **Fix:** Either set `allowBackup="false"`, or add `android:dataExtractionRules` (Android 12+) + `android:fullBackupContent` excluding the auth prefs file (`<exclude domain="sharedpref" path="cocktailcraft_prefs.xml"/>`). If 2.1 moves secrets to Keystore-encrypted storage, still exclude the encrypted file (its master key isn't backed up, making restored data unreadable — exclude to avoid restore-time corruption).

### 2.3 Hand-rolled iterated SHA-256 KDF instead of a vetted password-hashing algorithm — ✅ Done (branch `security/2.3-pbkdf2-kdf`, commit `a0234d0`)
- [x] **Severity:** Medium · **Effort:** M
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/security/PasswordHasher.kt:11-46`
- **Issue:** Custom `sha256(salt + password)` iterated 10,000× is fast and GPU-parallelizable — far weaker per unit of compute than PBKDF2-HMAC-SHA256 at OWASP-recommended ≥600k iterations, and much weaker than memory-hard Argon2id/scrypt. Practical risk: offline cracking of an extracted store (see 2.1/2.2).
- **Fix:** Swap the digest for a real KDF via expect/actual: Android `javax.crypto` `PBKDF2WithHmacSHA256`; iOS CommonCrypto `CCKeyDerivationPBKDF` (or a KMP Argon2 library). The existing versioned `v1:` format was designed for this — introduce `v2:` and re-hash transparently on next successful sign-in (same upgrade-on-use pattern already used for legacy plaintext).

### 2.4 Release builds signed with the debug keystore — ✅ Done (branch `security/2.4-release-signing`, commit `2db6ce3`)
- [x] **Severity:** Medium (shipping blocker) · **Effort:** S
- **Files:** `androidApp/build.gradle.kts:36-38`
- **Issue:** The `release` build type uses the world-readable debug keystore (default password `android`). Any such artifact offers no authenticity guarantee, could be re-signed/tampered by anyone, and will be rejected by Play. Comments acknowledge this is a local-dev convenience.
- **Fix:** Before any distribution: generate a dedicated release keystore, keep it out of VCS (`.gitignore` already covers `*.keystore`/`*.jks`), and wire the signing config from Gradle properties / CI secrets. Keep debug-signing behind an explicit local flag if local-installability of release builds is still wanted.

### 2.5 Unnecessary iOS ATS exception permitting insecure HTTP — ✅ Done (branch `security/2.5-remove-ats-exception`, commit `10eafaa`)
- [x] **Severity:** Low-Medium · **Effort:** S
- **Files:** `iosApp/CocktailCraft/Info.plist:77-92`, `shared/src/commonMain/kotlin/com/cocktailcraft/data/config/AppConfigImpl.kt:10,15`
- **Issue:** `NSExceptionAllowsInsecureHTTPLoads = true` for `thecocktaildb.com` even though the app is HTTPS-only. It's also likely a no-op as written (no `NSIncludesSubdomains`, and the real host is `www.thecocktaildb.com`) — but it's a latent downgrade vector and will draw App Review scrutiny.
- **Fix:** Delete the entire `NSExceptionDomains` block. All real traffic is HTTPS; ATS defaults are then correct.

## P2 — Hardening

### 2.6 Weak password policy — 6-char minimum, strength score never enforced — ✅ Done (branch `security/2.6-password-policy`, commit `f6a2776`)
- [x] **Severity:** Low-Medium · **Effort:** S
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/domain/util/AuthInputValidator.kt:10,19,22-30`, enforced at `SharedProfileViewModel.kt:84,140,269`, mirrored at `iosApp/CocktailCraft/Views/SignUpView.swift:142-154`
- **Issue:** Only a length ≥ 6 check; `passwordStrength()` (0–5) is computed and displayed but never gates sign-up. `"123456"` is an acceptable password, which materially helps offline cracking given 2.1–2.3.
- **Fix:** Raise `MIN_PASSWORD_LENGTH` to 8 (NIST SP 800-63B) and require `passwordStrength() >= 3` at sign-up/change-password in the shared validator so both platforms inherit it. Update the iOS client-side mirror.

### 2.7 No sign-in attempt throttling — ✅ Done (branch `security/2.7-signin-throttling`, commit `c35d836`)
- [x] **Severity:** Low · **Effort:** S
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/AuthRepositoryImpl.kt:43-53`
- **Issue:** `signIn` has no attempt counter, delay, or lockout — automated UI-level guessing against a known email is unlimited. Impact bounded (local-only auth), but the app presents itself as a real account system.
- **Fix:** Add a per-email attempt counter with exponential backoff persisted in Settings; reset on success.

### 2.8 Loose email validation — ✅ Done (branch `security/2.8-email-validation`, commit `808393c`)
- [x] **Severity:** Low · **Effort:** S
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/domain/util/AuthInputValidator.kt:12-17`
- **Issue:** Only checks non-blank + contains `@` + contains `.` + length ≥ 5 — accepts `"a@.b"` etc. Email doubles as the account's unique key, so junk values pollute storage keys.
- **Fix:** Use an RFC-5322-lite regex (local-part `@` domain with at least one dot-separated label, no whitespace) in the shared validator.

## P3 — Optional / cleanup

### 2.9 Ungated logging in release builds — ✅ Done (branch `security/2.9-release-logging`, commit `d0631a4`)
- [x] **Severity:** Low · **Effort:** S
- **Files:** `androidApp/src/main/java/com/cocktailcraft/android/screens/HomeScreen.kt:154,158,167,174` (`Log.d`), `androidApp/src/main/java/com/cocktailcraft/android/CocktailCraftApplication.kt:17` (Koin `androidLogger()` always on), `androidApp/src/main/java/com/cocktailcraft/android/util/ImageLoaderSingleton.kt:71-73` (Coil `DebugLogger` always attached)
- **Issue:** Debug log calls and framework loggers run in release builds. Content is low-sensitivity (categories/counts, DI wiring, image URLs) but it's noise and minor internal-state disclosure.
- **Fix:** Gate all three behind `BuildConfig.DEBUG` (Koin: `androidLogger()` only in debug, `Level.ERROR` otherwise).

### 2.10 Remove the legacy plaintext-password acceptance shim (eventually) — ✅ Done (branch `security/2.10-remove-plaintext-shim`, commit `5c21e7a`)
- [x] **Severity:** Low · **Effort:** S
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/data/repository/AuthRepositoryImpl.kt:220-227`
- **Issue:** `verifyCredentials` still accepts a stored plaintext value and upgrades it on first successful sign-in. Well-designed and test-covered, but it's trust surface that should not live forever.
- **Fix:** Once confident no installs predate hashing, delete the shim (and its test), leaving hash-only verification.

### 2.11 (Optional) TLS certificate pinning — ✅ Done (branch `security/2.11-tls-pinning`, commit `6941ea2`)
- [x] **Severity:** Low (informational) · **Effort:** M
- **Files:** `shared/src/commonMain/kotlin/com/cocktailcraft/di/NetworkModule.kt`
- **Issue:** No pinning on the Ktor client. Proportionally fine — TheCocktailDB is a public, unauthenticated API and no credentials transit it.
- **Fix:** Only if desired for defense-in-depth; not a priority relative to 2.1–2.3.

---

# 3. Android app

## P0 — Broken behavior

### 3.1 Bottom navigation destroys per-tab state on every switch
- [ ] **Severity:** High · **Effort:** S
- **Files:** `androidApp/src/main/java/com/cocktailcraft/android/navigation/NavigationManager.kt:78-84` (and the other `navigateTo*` helpers at 34-71)
- **Issue:** `navigateToBottomNavDestination` lacks `saveState = true` on `popUpTo` and `restoreState = true` on `navigate`. Every tab switch destroys the destination's back-stack entry: scroll position and `rememberSaveable` state are lost, and `LaunchedEffect(Unit)` loads re-trigger on each revisit (e.g. `OrderListScreen.kt:42`).
- **Fix:** ```kotlin
  navController.navigate(screen.route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
  }
  ```
  Apply the same to the other helpers.

### 3.2 Stale-closure bug in infinite-scroll helpers
- [ ] **Severity:** High · **Effort:** S
- **Files:** `androidApp/src/main/java/com/cocktailcraft/android/util/ListOptimizations.kt:48-104` (`OnBottomReached`, `OnScrollPastThreshold`), consumer `androidApp/src/main/java/com/cocktailcraft/android/ui/components/AnimatedCocktailList.kt:80-85`
- **Issue:** `LaunchedEffect(shouldLoadMore)` keys on a `remember`ed `State` object whose identity never changes, so the effect launches once and never restarts. The captured `onLoadMore` lambda closes over `isLoadingMore`/`isSearchActive` values from the first composition; later lambda updates are silently dropped. Result: redundant `loadMoreCocktails()` while already loading, or continued load-more after entering search mode.
- **Fix:** Inside the helpers, wrap the callback: `val currentOnLoadMore by rememberUpdatedState(onLoadMore)` and invoke `currentOnLoadMore()` from the collector. Guards then always read current values.

### 3.3 Detail screen fetches the same cocktail twice from two ViewModels
- [ ] **Severity:** Medium-High · **Effort:** S–M
- **Files:** `androidApp/src/main/java/com/cocktailcraft/android/screens/CocktailDetailScreen.kt:129-170,544-545`
- **Issue:** The screen calls `detailViewModel.loadCocktail(cocktailId)` **and** independently `produceState { homeViewModel.getCocktailById(cocktailId) }` — two overlapping fetches for one screen, with two independently-derived loading states that can disagree during a slow load.
- **Fix:** Source the primary `cocktail` and `isLoading` exclusively from `SharedCocktailDetailViewModel.uiState` (already loaded via `loadCocktail`); delete the parallel `produceState` path and the `homeViewModel` dependency.

## P1 — Ship-readiness

### 3.4 Legacy purple XML theme diverges from the real coral/gold palette
- [ ] **Severity:** High (user-visible) · **Effort:** S–M
- **Files:** `androidApp/src/main/res/values/colors.xml:1-23`, `androidApp/src/main/res/values/themes.xml:1-32`, `AndroidManifest.xml:14,18`, live consumers: `res/drawable/widget_background.xml`, `widget_item_background.xml`, `widget_refresh_button.xml`, `res/layout/*_widget_preview.xml`; runtime palette: `ui/theme/Theme.kt:19-56`, `ui/theme/AnimatedTheme.kt:24-49`
- **Issue:** Compose renders coral/gold (`#EB6A43`/`#FFC84D`), but the Activity theme and widget assets use an unrelated purple palette (`#5E2CA5`). Users see a purple cold-start flash, purple status bar pre-Compose, and purple widget previews/backgrounds that never match the app.
- **Fix:** Regenerate `colors.xml` from `AppColors` values (single source of truth), restyle `themes.xml` (window background, status bar) to match, and retint the widget drawables/preview layouts. Add a comment in both files pointing at the counterpart to prevent future drift.

## P2 — Quality & consistency

### 3.5 Systemic hardcoded user-facing strings (~314 literals vs 61 `stringResource` calls)
- [ ] **Severity:** High (localization blocker) · **Effort:** L (mechanical)
- **Files:** most screens — e.g. `CocktailDetailScreen.kt:200,210,386,415,435,567,738,825,842`, `ProfileScreen.kt:418,426` (stale `"© 2023"`, hardcoded `"Version 1.0.0"`), `CartScreen.kt:83,92,156,169-170`, `OfflineModeScreen.kt:170,242,274,376`
- **Issue:** The app cannot be localized; copy changes require code edits; `"Version 1.0.0"` will drift from the real versionName.
- **Fix:** Sweep every `Text("...")`/dialog literal into `strings.xml`; read the version from `BuildConfig.VERSION_NAME`/`PackageInfo`; enable Android Lint `HardcodedText` as error in CI to prevent regressions.

### 3.6 God composables on the four largest screens
- [ ] **Severity:** Medium-High · **Effort:** L (incremental)
- **Files:** `CocktailDetailScreen.kt:113-768` (~650-line composable), `ProfileScreen.kt:79-464`, `OfflineModeScreen.kt:76-412`, `HomeScreen.kt:107-326`
- **Issue:** Each mixes data-loading effects, many unrelated visual sections, and dialogs in one function. Any `uiState` change forces Compose to walk the entire tree — no narrowly-scoped children to skip; hurts readability, testability, previews.
- **Fix:** Extract each visual section (header/pricing card, ingredients, detail chips, recommendations carousel, reviews list; profile header/settings/about cards; etc.) into its own `@Composable` taking only the primitives/lists it needs. Do it opportunistically as these files are touched by other fixes.

### 3.7 Design tokens bypass Material3; no dynamic color
- [ ] **Severity:** Medium-High · **Effort:** M–L
- **Files:** `ui/theme/Theme.kt` (`AppColors` used 303× across 47 files vs `MaterialTheme.colorScheme` 4×), `ui/theme/AnimatedTheme.kt:24-49`, ~471 raw `dp` / ~110 raw `sp` literals, 48 raw `Color(0x...)` hits (e.g. `CocktailDetailScreen.kt:336,346`, `OfflineModeScreen.kt:145`)
- **Issue:** The global `AppColors` object sidesteps `MaterialTheme`, so components don't respond to Material You wallpaper color (no `dynamicColorScheme` anywhere), contrast overrides, or theme-level typography changes; ad-hoc hex colors and raw sp/dp drift independently.
- **Fix:** Map `AppColors` into the `ColorScheme` passed to `MaterialTheme` and migrate call sites to `MaterialTheme.colorScheme.*` / `MaterialTheme.typography.*` incrementally (start with new/touched code). Add `dynamicColor: Boolean = true` to `AnimatedCocktailBarTheme` using `dynamicLight/DarkColorScheme` on API 31+ with the coral/gold scheme as fallback. Introduce a spacing scale object to replace repeated raw dp.

### 3.8 Touch targets below 48 dp on high-frequency controls
- [ ] **Severity:** Medium (accessibility) · **Effort:** S
- **Files:** `ui/components/CartItemCard.kt:125,173,194,231` (32–36 dp), `screens/CocktailDetailScreen.kt:298` (36 dp), `ui/components/WriteReviewDialog.kt:111` (36 dp star targets)
- **Issue:** Explicit `.size(32.dp/36.dp)` on `IconButton`s overrides the 48 dp minimum interactive size — exactly on the cart's increment/decrement/remove controls. Fails Material/WCAG 2.5.5 guidance.
- **Fix:** Size only the inner `Icon`, not the `IconButton`; let the button keep its default ≥48 dp touch target (visuals unchanged).

### 3.9 Password-visibility toggle invisible to screen readers
- [ ] **Severity:** Medium (accessibility, auth flow) · **Effort:** S
- **Files:** `ui/components/AuthDialogs.kt:169-174`
- **Issue:** The icon-only visibility toggle has `contentDescription = null` — TalkBack users get zero indication of purpose or state during sign-in/sign-up.
- **Fix:** State-dependent description (`stringResource(R.string.show_password)` / `hide_password`); optionally `semantics { stateDescription }` for the toggled state. Audit other icon-only buttons while there.

### 3.10 Entrance-animation system is dead code with a stuck stagger counter
- [ ] **Severity:** Medium · **Effort:** S–M
- **Files:** `ui/components/AnimatedCocktailList.kt:88-116,159-176`
- **Issue:** (a) `visibleItemsCount` is `remember`ed without keying on the list, so after a category swap the reveal logic is instantly satisfied and staggering never runs again. (b) `animateFloatAsState(targetValue = 1f)` on first composition initializes **at** the target — no fade/slide is ever visible; the per-item state objects are pure overhead.
- **Fix:** Delete the manual batching and use `Modifier.animateItem()` / `AnimatedVisibility` — or key the counter with `remember(cocktails)` and animate from a real initial value (`Animatable(0f)` + `LaunchedEffect { animateTo(1f) }`).

### 3.11 Duplicate shimmer implementations, one infinite transition per placeholder
- [ ] **Severity:** Medium · **Effort:** S
- **Files:** `ui/components/ShimmerLoading.kt:36-64` (shared) vs local duplicate in `screens/CocktailDetailScreen.kt:773-797` (shadows the import at line 110; used at line 595)
- **Issue:** The local `shimmerEffect()` copy shadows the imported shared one. Both spin `rememberInfiniteTransition` per invocation, so N placeholders run N always-ticking animations concurrently.
- **Fix:** Delete the local copy; hoist a single `rememberInfiniteTransition` at the loading-container level and pass its animated value down to all placeholders.

### 3.12 Widgets can't deep-link to content
- [ ] **Severity:** Medium · **Effort:** M
- **Files:** `widget/FavoritesWidget.kt:220`, `widget/RandomCocktailWidget.kt:83`, `ui/main/MainScreen.kt:209-284` (no `navDeepLink`), `AndroidManifest.xml:19-22` (no `VIEW` intent filter)
- **Issue:** Tapping a specific favorite or the random cocktail in a home-screen widget calls a bare `actionStartActivity<MainActivity>()` — cold-launches to the Home tab instead of that cocktail's detail, defeating the widgets' purpose.
- **Fix:** Add a deep-link scheme (`app://cocktailcraft/cocktail/{id}`) via `navDeepLink` on `CocktailDetailRoute`, an exported `VIEW` intent filter, and pass the cocktail id from the widgets via an explicit Intent.

## P3 — Polish

### 3.13 `NetworkStatusCard` exists but is reimplemented inline
- [ ] **Severity:** Low-Medium · **Effort:** S
- **Files:** `ui/components/NetworkStatusCard.kt` (zero call sites), duplicate inline at `screens/OfflineModeScreen.kt:139-176`
- **Fix:** Replace the inline block with the component, or delete the unused component — remove the duplication either way.

### 3.14 Default image component is the costlier Coil API
- [ ] **Severity:** Low-Medium · **Effort:** S
- **Files:** `ui/components/OptimizedImage.kt:60-98` (`SubcomposeAsyncImage` default, used in scrolling lists e.g. `CocktailDetailScreen.kt:649-657`)
- **Issue:** Subcomposition-based image loading in lazy lists is more expensive than `AsyncImage` per Coil's own guidance.
- **Fix:** Default list/grid thumbnails to `AsyncImage` (with `onState` for load/error UI); keep `SubcomposeAsyncImage` only for single hero images (`DetailHeaderImage`).

### 3.15 Near-zero `@Preview` coverage, and previews use the wrong theme
- [ ] **Severity:** Low-Medium · **Effort:** M (incremental)
- **Files:** `ui/preview/ComponentPreviews.kt` (only file with previews — 5 total, wrapped in the runtime-unused `CocktailBarTheme`)
- **Issue:** No screen-level previews; existing ones don't represent production visuals (real theme is `AnimatedCocktailBarTheme`).
- **Fix:** Add light/dark + large-font previews per screen using representative fake state; standardize on the production theme wrapper.

---

# 4. iOS app

## P0 — Broken behavior

### 4.1 Background sync is likely fully broken (missing permitted identifiers + deferred registration)
- [ ] **Severity:** High · **Effort:** S
- **Files:** `iosApp/CocktailCraft/Info.plist` (has `UIBackgroundModes` but **no `BGTaskSchedulerPermittedIdentifiers`**), `iosApp/CocktailCraft/CocktailCraftApp.swift:18-21`, `iosApp/CocktailCraft/Utils/BackgroundSyncManager.swift:49-67,162-198`
- **Issue:** (a) Every `BGTaskScheduler` identifier must be listed in `BGTaskSchedulerPermittedIdentifiers`; without it, `register` fails at runtime — background refresh/fetch never fire. (b) Registration is wrapped in `Task { @MainActor in ... }`, deferring it past app-launch completion, which Apple forbids. (c) `task.expirationHandler` is assigned *after* the sync work starts.
- **Fix:** Add `BGTaskSchedulerPermittedIdentifiers` with `com.cocktailcraft.background-refresh` and `com.cocktailcraft.background-fetch` to Info.plist (and `project.yml` so XcodeGen preserves it). Call `registerBackgroundTasks()` synchronously in `init()`. Set `expirationHandler` before starting the sync `Task`.

### 4.2 Sign-in/sign-up loading state and double-submit protection are fake
- [ ] **Severity:** High · **Effort:** S
- **Files:** `Views/SignInView.swift:74-78,96`, `Views/SignUpView.swift:103-107,125`, caller: `Views/ProfileView.swift:70-93`
- **Issue:** The button does `isLoading = true; onSignIn(...); isLoading = false` around a non-async closure that internally fires `Task { await viewModel.signIn(...) }`. `isLoading` resets before the work starts: the spinner never shows and `.disabled(... || isLoading)` never prevents double-taps.
- **Fix:** Make `onSignIn`/`onSignUp` `async` closures (`(String, String) async -> Void`); the button wraps the call in its own `Task` and toggles `isLoading` around the genuine `await`.

### 4.3 Out-of-stock check hardcoded off in list/grid cards
- [ ] **Severity:** High · **Effort:** S
- **Files:** `Components/CocktailCard.swift:17-21` vs correct rule in `ViewModels/CocktailDetailViewModelSKIE.swift:121-148`
- **Issue:** `private var isOutOfStock: Bool { return false // cocktail.stockCount <= 0 }` — the real check is commented out, so every Add to Cart button on Home/Favorites is always enabled, contradicting the Detail ViewModel's `canAddToCart()`/`getStockStatus()` logic. Looks like half-finished work, not intent.
- **Fix:** Restore `cocktail.stockCount <= 0` (or `!cocktail.inStock`), matching the Detail ViewModel's rule; show the disabled/out-of-stock style the card already supports.

### 4.4 `SettingsView` fully implemented but permanently unreachable; dead-end taps elsewhere
- [ ] **Severity:** High (dead UI) · **Effort:** S–M
- **Files:** `Views/ProfileView.swift:7,64-66,176,184,192,200,231`, `Views/SettingsView.swift` (120 lines), `Views/OfflineModeView.swift:209-211,216-218`
- **Issue:** `showingSettings` is never set `true` anywhere — the entire Settings sheet (dark mode, accent color, font size, high contrast, reduced motion, reset) has no entry point. Profile rows "Edit Profile", "Change Password", "Email Preferences", "Notification Settings", "Help & Support" are `{ /* Handle X */ }` no-ops. OfflineModeView's recently-viewed thumbnails and "View All" do nothing on tap.
- **Fix:** Add a Settings row in ProfileView that sets `showingSettings = true`. Wire "Change Password"/"Edit Profile" to the `ProfileViewModelSKIE.updateProfile`/`changePassword` methods that already exist; remove (or mark "coming soon") the rows with no backing feature. Make OfflineModeView thumbnails navigate to `CocktailDetailView` like every other card.

## P1 — Platform parity

### 4.5 `CocktailDetailView` is a stub vs its own ViewModel and Android
- [ ] **Severity:** High · **Effort:** L
- **Files:** `Views/CocktailDetailView.swift` (72 lines) vs `ViewModels/CocktailDetailViewModelSKIE.swift` (153 lines, fully implemented) vs Android's `CocktailDetailScreen.kt` (859 lines)
- **Issue:** The view renders only image/name/category/ingredients. It never uses `toggleFavorite()`, `addToCart()`, `updateCartQuantity()`, `shareContent()`, `formatPrice()`, `getStockStatus()`, `relatedCocktails`, `nutritionFacts`, or `ingredientsByType` — all implemented and waiting in the ViewModel. Biggest functional gap in the app.
- **Fix:** Build out the screen against the existing ViewModel surface: price + add-to-cart/quantity controls, favorite toggle in the toolbar, stock badge, share button, related-cocktails carousel, ingredients grouped by type. Mirror Android's section order for design parity; reuse `CocktailImageView`, `SharedErrorAlert`, and existing components.

## P2 — Quality & consistency

### 4.6 High-traffic surfaces bypass the Kingfisher-backed image component
- [ ] **Severity:** Medium (performance) · **Effort:** S
- **Files:** `Components/CocktailCard.swift:39,150`, `Views/OfflineModeView.swift:308` (bare `AsyncImage`) vs `Components/CocktailImageView.swift` (Kingfisher: disk+memory cache, retry, failure image)
- **Issue:** Home rows, Favorites grid, and Offline grid use `AsyncImage`, which has no cross-render cache and refetches/re-decodes on list recycling — avoidable network/CPU churn and flicker. Detail/Cart already use the good component.
- **Fix:** Replace the three `AsyncImage` call sites with `CocktailImageView`.

### 4.7 51 copies of `do/try/catch` boilerplate with inconsistent error policy
- [ ] **Severity:** Medium · **Effort:** S–M
- **Files:** all 9 `ViewModels/*SKIE.swift` (Home 12, Profile 7, Theme 8, Cart 6, Detail 5, OfflineMode 5, Order 5, Favorites 3)
- **Issue:** Every async forwarding method hand-copies `do { try await sharedViewModel.x(...) } catch { print(...) }`; some print, some silently swallow. None add value beyond the mirrored `error` StateFlow that `SharedViewModelWrapper` already surfaces.
- **Fix:** Add to `SharedViewModelWrapper`: `func run(_ op: () async throws -> Void) async { do { try await op() } catch { /* single logging policy */ } }` and collapse all 51 sites to `await run { try await sharedViewModel.x(...) }`.

### 4.8 Zero accessibility labels/identifiers in the entire app target
- [ ] **Severity:** Medium-High (accessibility) · **Effort:** M
- **Files:** whole `CocktailCraft/` tree (0 grep matches for `accessibilityLabel|accessibilityIdentifier|accessibilityHint`); worst offenders: `Components/CocktailCard.swift:110,121`, `Components/CartItemCard.swift:76,100,115`, `Views/SignInView.swift:64`
- **Issue:** Every icon-only control announces as just "button" to VoiceOver. UI tests match display copy instead of identifiers, so they break on copy changes.
- **Fix:** Add `.accessibilityLabel(...)` to all icon-only buttons (favorite, add-to-cart, quantity ±, remove, password show/hide) and `.accessibilityIdentifier(...)` on key controls; migrate `UITestSetup.swift` helpers to identifiers.

### 4.9 Icon-only controls below the 44×44 pt HIG minimum
- [ ] **Severity:** Medium (accessibility) · **Effort:** S
- **Files:** `Components/CocktailCard.swift:226,237` (32×32), `Components/CartItemCard.swift:103,119` (32×32), `Components/ProfileSupportViews.swift:23,68,113`
- **Issue:** Favorite/add-to-cart in the grid card and cart quantity steppers have 32 pt frames — under Apple's 44 pt guidance, and (with 4.8) the hardest controls to hit and identify.
- **Fix:** Keep the icon visual size but expand the hit area to ≥44 pt via a larger frame + `.contentShape(Rectangle())`.

### 4.10 Dual `AppColors` APIs can visually desync the theme
- [ ] **Severity:** Medium · **Effort:** S
- **Files:** `Theme/AppColors.swift:57-92` (legacy trait-driven statics) vs the `isDarkMode:`-parameterized functions; last legacy call site `Views/ProfileView.swift:117`
- **Issue:** The legacy static vars follow the *system* trait; the rest of the app follows the app-controlled theme (`ThemeViewModelSKIE`). With "Follow System Theme" off and an opposite theme chosen, the Profile avatar background and its text color derive from two different sources and mismatch.
- **Fix:** Fix `ProfileView.swift:117` to use `AppColors.primary(isDarkMode:)`, then delete the entire legacy static-var API so it can't be reintroduced.

### 4.11 Fixed-size typography defeats Dynamic Type
- [ ] **Severity:** Medium (accessibility) · **Effort:** S–M
- **Files:** `Theme/AppTheme.swift:31-34` (`Typography.cardTitle/cardSubtitle/cardCaption/price`), plus `.font(.system(size:))` in 8 files (`CocktailCard`, `CartItemCard`, `ProfileSupportViews`, `HomeEmptyStateView`, `SignInView`, `SignUpView`, `HomeViewSKIE`, `ProfileView`)
- **Issue:** Fixed pixel sizes don't scale with the user's Dynamic Type setting, unlike the semantic tokens in the same file.
- **Fix:** Change the custom tokens to `.system(size: N, relativeTo: .headline/.subheadline/...)` and replace ad-hoc `.system(size:)` call sites with the theme tokens.

### 4.12 `HomeViewSKIE` is a 508-line god-view duplicating shared patterns
- [ ] **Severity:** Medium · **Effort:** M
- **Files:** `Views/HomeViewSKIE.swift` (508 lines — search bar, filter chips, 3 loading/empty/content branches, a ~100-line inline `AdvancedSearchSheet`, and a reimplemented error alert at 314-325 despite `Components/SharedErrorAlert.swift` being used by all 5 other screens)
- **Issue:** The one outlier in an otherwise well-decomposed codebase (avg 137 lines/file); the inline alert duplicates an established shared pattern.
- **Fix:** Extract `SearchBar`, `CategoryChipRow`, skeleton list, and `AdvancedSearchSheet` into `Components/`; replace the inline alert with `.sharedErrorAlert(...)`.

## P3 — Housekeeping & backlog

### 4.13 Stale `iOS_Warnings_TODO.md` misrepresents the codebase
- [ ] **Severity:** Low · **Effort:** S
- **Files:** `iosApp/iOS_Warnings_TODO.md`
- **Issue:** References pre-SKIE filenames (`CartViewModel.swift`) and flags issues already fixed (`onChange(of:)` form, TLS 1.2 already set). Actively misleads the next reader (or tooling).
- **Fix:** Regenerate from a current build-warnings capture, or delete it.

### 4.14 Confirm the iOS 18.5 deployment target is intentional
- [ ] **Severity:** Low · **Effort:** S (decision)
- **Files:** `iosApp/project.yml`, `iosApp/Podfile`
- **Issue:** An 18.5 floor excludes every device that hasn't taken a mid-cycle 18.x point release — an unusually aggressive cutoff for a consumer app.
- **Fix:** Decide deliberately; iOS 17.0 would keep the `@Observable`/NavigationStack architecture intact while widening reach. Lower in both files together.

### 4.15 No WidgetKit counterpart to Android's two widgets
- [ ] **Severity:** Low (parity backlog) · **Effort:** L
- **Files:** Android `widget/FavoritesWidget.kt`, `widget/RandomCocktailWidget.kt`; no widget extension target in `project.yml`
- **Issue:** Android ships Favorites + Random Cocktail home-screen widgets; iOS has none.
- **Fix:** Backlog item: add a WidgetKit extension target (XcodeGen) reading shared data via an App Group; pair with 3.12's deep links so both platforms' widgets open the tapped cocktail.

### 4.16 UI tests keyed to display strings
- [ ] **Severity:** Low · **Effort:** S (falls out of 4.8)
- **Files:** `CocktailCraftUITests/UITestSetup.swift:35` and the three test files
- **Issue:** Tests locate elements by visible copy/placeholder text — brittle to copy changes and localization.
- **Fix:** Switch to `accessibilityIdentifier`-based queries once 4.8 lands.

---

# 5. Suggested execution order

| Phase | Contents | Rationale |
|---|---|---|
| **1. Bug-fix pass** | 1.1, 1.2, 3.1, 3.2, 3.3, 4.1, 4.2, 4.3, 4.4 | Small, independent, verifiable diffs; all user-visible defects. |
| **2. Security pass** | 2.1, 2.2, 2.3, 2.5, 2.6 (2.4 before any distribution) | Storage encryption + backup rules + KDF upgrade travel together; the `v1:` hash format already supports migration. |
| **3. Repo hygiene** | 1.3, 1.6, 1.12, 1.13, 1.14, 1.15, 3.13, 4.13, 2.9 | Deletions and untracking — near-zero risk, shrinks the surface for everything after. |
| **4. iOS parity sprint** | 4.5, 4.6, 4.7, 4.10 | Detail screen build-out is the single biggest feature gap; the ViewModel layer is already done. |
| **5. Design-system consolidation** | 3.4, 3.5, 3.7, 4.11, plus a11y: 3.8, 3.9, 4.8, 4.9 | One source of truth for tokens/strings on both platforms; accessibility fixes ride along since they touch the same components. |
| **6. Performance & structure** | 1.4, 1.5, 3.10, 3.11, 3.14, 3.6, 4.12, 1.7, 1.8, 1.9, 1.10 | Dispatcher confinement + cache batching first (measurable), then decomposition and remaining architecture cleanups. |
| **7. Tests & backlog** | 1.11, 3.15, 4.16, 3.12, 4.14, 4.15, 2.7, 2.8, 2.10, 2.11 | Regression tests for every phase-1 fix land with that phase; the rest fill in here. |
