# CocktailCraft App

## Overview
CocktailCraft is a Kotlin Multiplatform project for a feature-rich cocktail ordering and discovery application, designed to run on both Android and iOS platforms. The app allows users to browse, search, and order various cocktails, manage their shopping cart, track orders, and maintain a profile. It leverages modern mobile app development practices including MVVM architecture, reactive programming, and dependency injection.

### Platform Status
- **Android**: ✅ Fully functional and production-ready
- **iOS**: ✅ Fully functional — SwiftUI on the shared ViewModels, bridged to Swift async/await by SKIE

<p align="center">
  <img src="docs/images/Screenshot_20250419_014459.png" alt="Android home screen" width="45%">
  <img src="docs/images/ios-home-v0.3.png" alt="iOS home screen" width="45%">
</p>

## Features
- **Cocktail Discovery**: Browse and search for cocktails with detailed information
- **Advanced Search & Filtering**: Combine a name query with category, ingredient, and alcoholic/non-alcoholic filters
- **Shopping Cart**: Add cocktails to cart, update quantities, and checkout
- **User Authentication**: Register, sign in, and manage user profiles — accounts are device-local (stored and verified on the device; there is no server or sync)
- **Order Management**: Place orders, track order history, open an order's details, cancel an order and reorder a past one
- **Favorites**: Save and manage favorite cocktails
- **Reviews**: Read and submit reviews for cocktails; every review on the device is shown to everyone, and only its author can edit or delete it
- **Per-user data**: Each account on the device, and the guest while signed out, has its own cart, favorites, recently viewed list and orders; signing in adds the guest's cart, favorites and views to the account
- **Home Sort**: Order the Home list by name, price, rating or popularity
- **Recently Viewed**: A row of the cocktails you opened last, at the top of Home
- **Undo**: Removing a cart item or a favorite shows a toast with an Undo action
- **Related Cocktails**: "You might also like" picks on the detail screen — a random selection from the same category
- **Dark Mode Support**: Adaptive UI that supports both light and dark themes with smooth transitions
- **Offline Mode**: The whole cached catalog stays browsable without a connection — every category page and cocktail already stored in the database (`cached_cocktail` + `category_page`), plus favorites and recently viewed
- **Home Screen Widgets**: Android and iOS (WidgetKit) widgets with deep links into the app; on Android, WorkManager refreshes them periodically (there is no other background sync on either platform)
- **Localization**: One shared set of UI strings for both platforms (moko-resources in `:core:resources`); prices are US dollars only for now
- **Robust Error Handling**: User-friendly error messages with recovery options
- **Polished Animations**: Enhanced user experience with smooth animations and micro-interactions
- **Cross-Platform**: Same codebase for both Android and iOS platforms with platform-specific UI components
- **Native iOS UI**: SwiftUI screens and reusable components; SKIE exposes the shared Flows and suspend functions to Swift as AsyncSequence and async

## Architecture
The application follows the **Clean Architecture** pattern with **MVVM** (Model-View-ViewModel) for presentation, reactive state management, and a modular **Dependency Injection** system.

### Module Structure
The shared Kotlin code is split into Gradle modules, one per layer, so the
Clean Architecture dependency rules are enforced by the build: a layer can
only use what its module depends on.

```
  androidApp (Compose)                   iosApp (SwiftUI: `import shared`)
        │ implementation                        │ links the static framework
        ▼                                       ▼
  ┌────────────────────────────────────────────────────────┐
  │ :shared — umbrella: builds the `shared` iOS framework  │
  │ (SKIE), export()s the API modules, assembles Koin      │
  │ (initKoin, appModule, platformModule(), KoinHelper)    │
  └────────────────────────────────────────────────────────┘
        │ api + export()                             │ implementation
        │ (also :core:domain, :core:common,          │ (not exported,
        │  :core:resources, :core:designsystem)      │  not visible to the apps)
        ▼                                            ▼
  ┌────────────────────┐                       ┌────────────────────┐
  │ :presentation      │                       │ :core:data         │
  │ shared ViewModels, │                       │ repository impls,  │
  │ UiState,           │                       │ Ktor, SQLDelight,  │
  │ ErrorHandler       │                       │ Settings, drivers  │
  └────────────────────┘                       └────────────────────┘
     │ api        │ api                                │ api
     │            └─────────────┐   ┌──────────────────┘
     ▼                          ▼   ▼
  ┌────────────────────┐  ┌──────────────────────────┐
  │ :core:resources    │  │ :core:domain             │
  │ moko strings (MR)  │  │ models, repository       │
  └────────────────────┘  │ interfaces, use cases,   │
                          │ validators               │
                          └──────────────────────────┘
                                     │ api
                                     ▼
                          ┌──────────────────────────┐
                          │ :core:common             │
                          │ Result/ErrorCode,        │
                          │ ErrorClassifier,         │
                          │ NetworkMonitor, UUID     │
                          └──────────────────────────┘

  User-facing copy is chosen only in :presentation (and the apps): the
  layers below report typed codes and values, never strings.

  :core:testing — test-only fakes, test data, MainDispatcherTest (api :core:domain)
```

| Module | Contents | Depends on |
|--------|----------|------------|
| `:core:resources` | moko-resources strings/plurals → `com.cocktailcraft.resources.MR` | moko-resources (api) |
| `:core:designsystem` | `DesignTokens` (colors, spacing, typography) | — |
| `:core:common` | `Result`/`ErrorCode`/`DomainException`, `ErrorClassifier`, the `NetworkMonitor` contract, `UUID` | Ktor core (implementation — only for exception types) |
| `:core:domain` | models, repository interfaces, use cases, `AppConfig`, `DeliveryPolicy`, validators | common (api) |
| `:core:data` | repository implementations, Ktor remote + DTOs, SQLDelight database, Settings/secure storage, platform `NetworkMonitor`s and drivers, `networkModule`/`dataModule`/`dataPlatformModule()` | domain (api); Ktor, SQLDelight, Settings (implementation) |
| `:presentation` | shared ViewModels + UiState, `ErrorHandler`, display formatting (`format/`), `domainModule`/`presentationModule` | domain (api), resources (api), androidx ViewModel (api), designsystem (implementation) |
| `:shared` | umbrella: iOS framework, Koin assembly, `KoinHelper` | presentation/domain/common/resources/designsystem (api + `export`), data (implementation) |
| `:core:testing` | test doubles shared across suites (only ever a test dependency) | domain (api) |

**Dependency rules**
- `:presentation` → `:core:domain` ← `:core:data`. The domain depends on
  nothing app-specific (no Ktor, SQLDelight, Settings or platform drivers);
  `:presentation` cannot see `:core:data` — repositories reach the ViewModels
  through Koin as domain interfaces.
- `api` only where a module's public API exposes the dependency's types;
  everything else is `implementation`, so e.g. Ktor stays off the domain and
  presentation compile classpaths.
- Only `:shared` depends on `:core:data`, with `implementation`: androidApp
  and Swift never see data-layer types, and the framework doesn't export them.
  Each module exposes its own Koin module; `:shared` assembles them (see
  [Dependency Injection](docs/DependencyInjection.md)).
- Swift sees exactly the `export()`ed modules (`:presentation`,
  `:core:domain`, `:core:common`, `:core:resources`, `:core:designsystem`);
  helpers that must be public for another Kotlin module but aren't Swift API
  are `@HiddenFromObjC`.
- Package names did not change with the split (e.g. `Result` is still
  `com.cocktailcraft.domain.util.Result`, in `:core:common`).
- The KMP setup (targets, toolchain, SDK levels, opt-ins, host tests) comes
  from the `cocktailcraft.kmp.library` convention plugin in `build-logic/`,
  so each module's build file only lists its dependencies.

Each platform renders native UI over the same shared ViewModels: Jetpack Compose on Android ([UI Components](docs/UI_Components.md)) and SwiftUI on iOS ([iOS UI Components](docs/iOS_UI_Components.md)).
ViewModels expose one `StateFlow` of UI state plus an error channel; failures are classified by `ErrorHandler` (`:presentation`) into user-friendly, localized messages.
For diagrams of the catalog flow and order placement, see the [Documentation Index](docs/README.md).

### Animations and Transitions
The app features a focused set of animations to enhance the user experience:
- **Micro-interactions**: Press-scale animations on buttons, icon buttons, and FABs, with optional haptic feedback
- **List Animations**: Item entrance and placement animation via Compose's built-in `animateItem()` on identity-keyed list items
- **Loading States**: Shimmer loading placeholders driven by a single shared transition per loading container
- **Theme Transitions**: Animated color transitions when switching between light and dark modes, plus a custom animated theme switch

For detailed information about the animations implementation, see the [Animations section of the UI Components documentation](docs/UI_Components.md#animations).

### Dependency Injection
The app uses Koin for dependency injection with a modular approach:
- **Modular Structure**: Each Gradle module exposes its own Koin module (network, data, domain, presentation, platform); `:shared` assembles them into `appModule`
- **Testability**: Constructor injection throughout, so tests pass fakes directly (no mock Koin module)
- **ViewModel Integration**: Standardized pattern for ViewModel dependency injection

For detailed information about the dependency injection implementation, see:
- [Dependency Injection Documentation](docs/DependencyInjection.md) - Overview, module structure, and best practices

## Libraries

Every dependency, with its version and purpose, is listed in [docs/Libraries.md](docs/Libraries.md); versions live in the `gradle/libs.versions.toml` version catalog.

## Package Structure

### Domain Layer (`core/domain`)
- **Models**: Data classes representing core business entities like `Cocktail`, `User`, `Order`, etc.
- **Repositories**: Interfaces defining data access contracts
- **UseCases**: Business logic operations and workflows

### Data Layer (`core/data`)
- **Repository Implementations**: Concrete implementations of repository interfaces
- **Remote Data Sources**: API clients and network-related code
- **Local Data Sources**: SQLDelight database (`data/local/LocalDatabase`, `data/cache/CocktailCache`), with each user's data kept under its owner (`data/local/UserDataOwnership.kt`), and an encrypted store for auth: a Tink-encrypted file with a Keystore-wrapped key on Android, the Keychain on iOS (see [Data layer](docs/DependencyInjection.md#data-layer))

### Presentation Layer
- **Shared ViewModels** (`presentation`): Manage UI state and handle user interactions on both platforms
- **Screens**: Compose UI components representing app screens (`androidApp`) and SwiftUI views (`iosApp`)
- **UI Elements**: Reusable UI components

## Key Features Implementation

### Cart Functionality
- **SharedCartViewModel**: Manages cart state and operations like adding/removing items and checkout
- **CartRepository**: Persists the cart in SQLite and streams changes; checkout (`OrderRepository.placeOrderAndClearCart`) stores the order and empties the cart in one transaction
- **CartScreen**: UI for displaying and managing cart items

### User Authentication
- **AuthRepository**: Handles user registration, login, and session management; accounts and sign-in throttling stay on the device, and signing in or out switches whose cart, favorites and orders the app shows
- **SharedProfileViewModel**: Manages user profile data and settings
- **ProfileScreen**: UI for user authentication and profile management

### Cocktail Discovery
- **SharedHomeViewModel**: Manages cocktail listings, categories, and search
- **CocktailSearchRepository / CocktailCatalogRepository / CocktailDetailRepository**: Focused repositories providing cocktail data from remote and cached sources
- **HomeScreen & CocktailDetailScreen**: UI for browsing and viewing cocktail details

### Recommendation System
- **GetCocktailDetailUseCase.getRelatedCocktails**: Client-side, category-based related-cocktail suggestions
- **Detail screen sections**: "You might also like" carousels on both platforms, fed by `relatedCocktails` in the shared detail state
- **Offline Support**: Works with cached data when no internet connection is available

### Dark Mode Support
- **SharedThemeViewModel**: Manages theme state and preferences
- **AnimatedCocktailBarTheme** (Android) / **AppTheme** (iOS): Provide theme-specific colors and typography from the shared design tokens. On Android every color is read from `MaterialTheme.colorScheme` or `CocktailCraftTheme.extendedColors` (see [UI_Components.md](docs/UI_Components.md#theming-and-colors))
- **Smooth Transitions**: Animated transitions between light and dark modes
- **System Integration**: Option to follow system theme settings

### Offline Mode
- **NetworkMonitor**: Detects and monitors network connectivity
- **CocktailOfflineRepository**: Recently viewed cocktails (`getRecentlyViewedCocktails()`) and the connectivity check (`isOffline()`)
- **CocktailCache**: The local catalog (`cached_cocktail` + `category_page`) — the single source of truth the catalog, search and detail repositories read and refresh into, so stored data simply keeps showing when offline
- **OfflineModePolicy**: Answers only "is the app offline" (`isOffline()`), for the cache and the offline repository
- **UI Indicators**: Clear indicators of offline status and available actions

### Error Handling
- **ErrorHandler** (`:presentation`): Turns failures into user-friendly errors with recovery actions; exception types are classified by `ErrorClassifier` (`:core:common`)
- **ErrorUtils** (Android): Maps error categories to Android UI resources (icons, colors)
- **SharedViewModel**: Common error handling for all ViewModels
- **ErrorDialog**: User-friendly error display component
- **Recovery Actions**: Actionable error messages with recovery options
- **Localized copy**: Error titles, messages and actions are shared `StringDesc`s, resolved for the user's locale by each platform (see [Localization](#localization))

### Localization
The app's user-facing strings have a single source in the `:core:resources`
module, managed by [moko-resources](https://github.com/icerockdev/moko-resources):

```
core/resources/src/commonMain/moko-resources/
├── base/strings.xml    # English (base) — every UI, widget and error string
├── base/plurals.xml    # count-dependent copy
└── <lang>/strings.xml  # a translation (none ship yet)
```

At build time moko-resources generates the `MR.strings` / `MR.plurals`
accessors (package `com.cocktailcraft.resources`), Android string resources in
that module, and an iOS `Localizable.strings`/`.stringsdict` bundle that the
`shared` framework build collects and the iOS app's "Compile Kotlin Framework"
phase copies into the app (the shared framework is static, so it cannot carry
the bundle itself). `:presentation` and `:shared` apply the moko plugin without
resources of their own, only so their iOS test executables get the bundle.

| Where | How to use a string |
|-------|---------------------|
| Shared Kotlin (`:presentation` only: ViewModels, `ErrorHandler`, `format/`) | Return/emit a `StringDesc` — `MR.strings.key.desc()`, `MR.strings.key.format(arg)`, or `StringDesc.Raw(text)` for untranslated data-layer text. Never resolve it in shared code: `UserFriendlyError.title/message`, `RecoveryAction.actionLabel` and the theme/order display names are `StringDesc`s. Below `:presentation`, return a typed value (e.g. `ReviewValidationError`, `Result.Error.retryAfterMs`) and word it in `format/`. |
| Android Compose | `stringResource(MR.strings.key)`, `stringResource(MR.strings.key, arg)`, `pluralStringResource(MR.plurals.key, count, count)` and `desc.resolve()` from `androidApp/.../util/SharedStrings.kt` |
| Android Glance widgets / non-Compose | `context.getString(MR.strings.key, …)` (same file) |
| iOS SwiftUI | `L(\.key)`, `L(\.key, arg1, arg2)`, `L(\.key, count: n)` from `iosApp/CocktailCraft/Utils/Strings.swift`; `desc.localized()` for a shared `StringDesc` |

**Adding a string**: add it to `base/strings.xml` (or `plurals.xml`) with a
readable `snake_case` key grouped by screen, escaped like an Android resource
(`\'`, `\"`, `&amp;`), with positional format arguments (`%1$s`, `%2$d`) so a
translation can reorder them. Build once (or run `./gradlew
:core:resources:generateMRcommonMain`) and use it from either platform as above. On
iOS, pass Swift `Int`s/`String`s as the arguments — every specifier is
formatted as an object, so avoid precision specifiers like `%.2f` in strings
iOS uses (prices are formatted separately, see below). Keys ending in `_ios`
hold copy that currently differs from the Android wording of the same element;
unifying such a pair is a visual change that needs the screenshot baselines
re-recorded.

**Adding a language**: create `core/resources/src/commonMain/moko-resources/<lang>/strings.xml`
(and `plurals.xml` with that language's plural quantities) translating the
base keys — untranslated keys fall back to English — then add the language
code to `CFBundleLocalizations` in `iosApp/CocktailCraft/Info.plist` so iOS
offers it. Android needs nothing else (moko generates `values-<lang>`).
Translate the native leftovers below as well.

**Still native, and why**:
- **iOS widget extension** (`iosApp/CocktailCraftWidgets/Localizable.xcstrings`):
  the extension does not link the shared framework — it renders App Group
  snapshots written by the app, and pulling the Kotlin/Native framework and
  runtime into a memory-capped extension for ten strings isn't worth it — so
  it cannot read the shared bundle.
- **Android OS-read metadata** (`androidApp/src/main/res/values/strings.xml`):
  the launcher label, the widget picker names/descriptions and the static
  widget preview layouts, which the system reads from the app's own resources
  (their iOS counterparts are the widget's `configurationDisplayName`s).
- The Debug-only DebugSwift menu labels, which never ship.

Prices are US dollars only for now, held as `Money` (whole cents, in
`:core:domain`) and turned into text in one place, the shared
`Money.formatted()`: USD in the device locale's number style ("$12.50" in
en-US, "12,50 $" in de-DE) — `java.text`'s currency format for the
configuration locale on Android, `NSNumberFormatter` with a plain "$" on iOS.
No string hard-codes `$` or a number format: copy that shows a price takes the
formatted amount as a `%1$s` argument. The iOS widget extension, which doesn't
link the framework, gets prices already formatted in its snapshot.

## Setup & Running the Project
1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd CocktailCraft
   ```

2. **Open the Project**:
   - Open the project in Android Studio.

3. **Sync the Project**:
   - Allow Gradle to sync and download all necessary dependencies.

4. **Run on Android**:
   - Select the `androidApp` configuration
   - Choose an emulator or device
   - Click Run

5. **Run on iOS** (macOS only):
   - Navigate to `iosApp` directory: `cd iosApp`
   - Generate the Xcode project from the spec: `xcodegen generate` (`brew install xcodegen`)
   - Open `CocktailCraft.xcodeproj` in Xcode — no CocoaPods: Swift packages resolve automatically, and the shared Kotlin framework is built by the app's "Compile Kotlin Framework" build phase
   - Select a simulator and run the app
   - See [iOS Setup Instructions](iosApp/iOS_Setup_Instructions.md) for detailed setup

### TheCocktailDB API key
Both apps call [TheCocktailDB](https://www.thecocktaildb.com/api.php). By default they use its public test key, `1`, so the project builds and runs without any setup. Per TheCocktailDB's API page, that key is for development and education; a published app needs a premium key.

To build with your own key, set `cocktailDbApiKey` in one of these places (the first one found wins):

1. On the command line: `./gradlew assembleRelease -PcocktailDbApiKey=YOUR_KEY`
2. In the environment: `COCKTAILDB_API_KEY=YOUR_KEY` (use this in CI, from a secret)
3. In `local.properties` (untracked): `cocktailDbApiKey=YOUR_KEY`

The key must be letters and digits only. `:core:data` compiles it into a generated constant, so it applies to both platforms. The iOS app's "Compile Kotlin Framework" build phase runs this same Gradle build, so `local.properties` works for Xcode builds too. The environment variable works for `xcodebuild` run from a shell, but not for the Xcode app. Never commit a real key. Anything compiled into an app can be extracted from it, so treat the key as identifying the app, not as a secret.

## Development Setup
- **Android Studio**: a release that supports AGP 9.2 (the version in `gradle/libs.versions.toml`) — see the [AGP/Android Studio compatibility table](https://developer.android.com/build/releases/gradle-plugin#android_gradle_plugin_and_android_studio_compatibility)
- **JDK**: Version 17 or higher
- **Xcode**: Latest version (for iOS development), plus [XcodeGen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`)
- **Mac**: Apple silicon for iOS work — the Kotlin framework is built for arm64 simulators and devices only (no Intel `x86_64` simulator)
- **Gradle**: Managed by the project
- **Git**: For version control

## Troubleshooting
- **Build Issues**: Try cleaning and rebuilding the project
- **KMP Plugin Issues**: Make sure the Kotlin Multiplatform plugin is up to date
- **iOS Builds**: See [iOS Setup Instructions](iosApp/iOS_Setup_Instructions.md) and the [SKIE configuration](docs/Shared_ViewModel_Strategy.md#skie-configuration) for details
- **Dependency Resolution**: Check Gradle settings and the `gradle/libs.versions.toml` version catalog

## Documentation

See the [Documentation Index](docs/README.md) for the architecture, feature, UI component, library, debugging and iOS setup docs.

## Static analysis

CI (`.github/workflows/ci.yml`) runs these checks and the tests below on
every pull request and push to `dev` and `main`. Findings that predate a tool are
recorded in baselines, so the commands below pass today and report only new
issues.

```bash
./gradlew detekt            # detekt 2 + ktlint (ktlint wrapper) + Compose rules, every module
./gradlew :core:data:detekt # one module
./gradlew detektBaseline    # re-record the baselines (only to accept existing findings)
./gradlew detekt -Pdetekt.autoCorrect=true  # apply ktlint formatting fixes (rewrites sources)
./gradlew :core:data:detektMainAndroid      # opt-in: type-resolved analysis (Android compilation)
cd iosApp && swiftlint lint # SwiftLint (brew install swiftlint)
```

- **Kotlin**: detekt 2.0.0-alpha.6 (the 1.23 line can't parse Kotlin 2.3),
  applied to every module by the `cocktailcraft.detekt` convention
  plugin in `build-logic/`, with the shared config in `config/detekt/detekt.yml`
  and a `detekt-baseline.xml` per module; HTML reports land in
  `<module>/build/reports/detekt/`. `detekt` is also part of `./gradlew check`.
  It is detekt's light (syntax-only) pass over all of `src/`, iOS source sets
  included, so type-aware rules don't run there. The type-resolved tasks
  (`detektMainAndroid`/`detektHostTestAndroid` in KMP modules,
  `detektMain`/`detektDebug`/... in `:androidApp`) cover the Android
  compilation only, aren't part of `check`, and in KMP modules still report
  spurious expect/actual compiler errors (detekt 2 alpha limitation). By
  default nothing rewrites code (ktlint runs check-only; formatting follows
  `.editorconfig`); `-Pdetekt.autoCorrect=true` opts in to ktlint's fixes,
  then re-run `detektBaseline` to drop the fixed entries.
- **Swift**: `iosApp/.swiftlint.yml`. The app target has an advisory SwiftLint
  build phase that runs only when SwiftLint is installed and reports findings
  as warnings, never failing the build. To see only new findings, record a
  baseline (SwiftLint 0.55+): `swiftlint lint --write-baseline .swiftlint-baseline.json`,
  then `swiftlint lint --baseline .swiftlint-baseline.json`.

### Continuous integration

`.github/workflows/ci.yml` runs on every pull request and on pushes to `dev`
and `main`, in three jobs. No secrets are used; release builds are unsigned.

- **Linux** (Android and shared Kotlin): `detekt`, `testAndroidHostTest`,
  `:androidApp:testDebugUnitTest` (unit and Robolectric; screenshots are not
  compared on Linux),
  `:core:data:verifySqlDelightMigration`, `:androidApp:lintRelease` and
  `:androidApp:assembleRelease`.
- **Linux, emulator**: `:androidApp:connectedDebugAndroidTest` (the
  instrumented Compose tests, against fake repositories) on an API 34 ATD
  emulator.
- **macOS** (Xcode 26 on `macos-26`): `:androidApp:verifyRoborazziDebug`
  (the Android screenshot references were recorded on macOS);
  `iosSimulatorArm64Test`; checks the committed Xcode
  project matches `xcodegen generate`; builds the app and runs the
  `CocktailCraftTests` unit and snapshot tests (snapshots are verified only on
  an iPhone 16 Pro iOS 18.5 simulator, skipped otherwise); builds the Release
  configuration and fails if it contains the Debug-only test mode or QA
  hooks; and runs the `CocktailCraftUITests` XCUITest flows in the app's test
  mode.

## Versioning

`gradle.properties` (`appVersionName`/`appVersionCode`) is the single source of
the app version, currently 0.4.0 (build 2). Bump it with `scripts/bump-version.sh <versionName> <versionCode>`
(e.g. `scripts/bump-version.sh 1.1 2`), which also updates both iOS targets in
`iosApp/project.yml` and the committed Xcode project, and fails if any of
those edits did not land.

## Contributing
Contributions are welcome! Please follow the standard GitHub flow:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request


## Testing

The test suites live alongside the code they cover — the tests themselves are the up-to-date catalog:

- **Shared (KMP)**: each module's `src/commonTest` (plus `androidHostTest`/`iosTest`), next to the code it covers — `presentation` (ViewModels), `core/domain` (use cases, policies, validators), `core/data` (repositories, cache, API, database, data DI), `core/common` (`ErrorHandler`), and `shared` (full DI graph, `KoinHelper`, Android string resolution).
- **`core/testing`**: a test-only module (only ever a test dependency) with hand-written fakes (`Fakes.kt`, `FakeOrderRepository`, `FakeReviewRepository`), test data, the `MainDispatcherTest` base and an in-memory SQLite driver (`createInMemorySqlDriver`, plus `CountingSqlDriver`). Tests construct ViewModels and use cases directly through their constructors; there is no mock-based Koin test module. Some `presentation` tests run against the real repositories on the in-memory database (`:core:data` is a test-only dependency there).
- **Android**: `androidApp/src/test` (JVM unit, Robolectric and screenshot tests) and `androidApp/src/androidTest` (instrumented Compose tests against fake repositories, e.g. `MainScreenSharedVmTest`)
- **iOS**: `iosApp/CocktailCraftTests` (unit and snapshot tests) and `iosApp/CocktailCraftUITests` (XCUITest flows and the accessibility audit)

Run them with:

```bash
./gradlew testAndroidHostTest               # Shared (KMP) tests, every module, on the JVM
./gradlew iosSimulatorArm64Test             # the same suites on the iOS simulator
./gradlew :androidApp:testDebugUnitTest     # Android unit tests (incl. screenshot verify)
./gradlew :androidApp:connectedDebugAndroidTest  # Android instrumented tests (device/emulator)
# iOS (from iosApp/, after xcodegen generate):
xcodebuild test -project CocktailCraft.xcodeproj -scheme CocktailCraft \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.5'
```

### Screenshot tests

Both platforms snapshot the design-system components and the stateless parts
of the main screens in light and dark mode, plus high-contrast, large-font
(including Android's 2x maximum and iOS's largest accessibility size, AX5) and
accent-color variants of a few key components and screens. Reference images are committed;
a visual change fails the test until the references are re-recorded and the
new images reviewed in the diff. Sample data never carries image URLs, so no
test touches the network.

**Android** — [Roborazzi](https://github.com/takahirom/roborazzi) on
Robolectric (JVM, no emulator). Tests live in
`androidApp/src/test/java/com/cocktailcraft/android/screenshot`, references in
`androidApp/src/test/screenshots`. A plain `testDebugUnitTest` run verifies
(`roborazzi.test.verify=true` in `gradle.properties`).

```bash
./gradlew :androidApp:recordRoborazziDebug    # re-record references after an intended change
./gradlew :androidApp:verifyRoborazziDebug    # verify only (same as testDebugUnitTest)
./gradlew :androidApp:compareRoborazziDebug   # write *_compare.png diffs without failing
```

Failed comparisons write `*_compare.png` / `*_actual.png` to
`androidApp/build/outputs/roborazzi`. Robolectric is pinned to SDK 34 and a
360x640dp xhdpi window (`androidApp/src/test/resources/robolectric.properties`);
references were recorded on macOS and Linux renders text differently, so CI
compares them in its macOS job.

**iOS** — [swift-snapshot-testing](https://github.com/pointfreeco/swift-snapshot-testing)
(Swift package, `CocktailCraftTests` target only). Tests live in
`iosApp/CocktailCraftTests/Snapshots`, references in its `__Snapshots__`
folder. Renders use a fixed width, @3x, phone traits, pinned Dynamic Type and
the scheme's `en`/`US` locale. **References must be recorded on the iPhone 16
Pro simulator running iOS 18.5**; on any other iOS runtime the snapshot tests
skip themselves (text rasterization differs between runtimes).

```bash
# from iosApp/ — verify (missing references are recorded and fail once)
xcodebuild test -project CocktailCraft.xcodeproj -scheme CocktailCraft \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.5' \
  -only-testing:CocktailCraftTests
# re-record every reference (the run reports failures; re-run to verify)
TEST_RUNNER_SNAPSHOT_TESTING_RECORD=all xcodebuild test -project CocktailCraft.xcodeproj \
  -scheme CocktailCraft -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.5' \
  -only-testing:CocktailCraftTests
```

### Accessibility

Screen-reader semantics (labels, toggle/selected state, headings, grouping,
live regions/announcements, custom actions), touch targets, contrast,
Dynamic Type / font scale and reduced motion are checked automatically:

| Check | Where | Runs in |
|-------|-------|---------|
| Google's Accessibility Test Framework over Home, filters, detail, cart and profile content at 1x and 2x font scale (via Roborazzi's ATF integration; fails on warnings and errors) | `androidApp/src/test/.../accessibility/AccessibilityChecksTest.kt` | `./gradlew :androidApp:testDebugUnitTest` |
| Semantics assertions for the labels, states, headings, live regions and custom actions | `androidApp/src/test/.../accessibility/AccessibilitySemanticsTest.kt` | `./gradlew :androidApp:testDebugUnitTest` |
| Compose `enableAccessibilityChecks()` on the real `MainScreen`, walking every tab | `androidApp/src/androidTest/.../AccessibilityChecksTest.kt` | `./gradlew :androidApp:connectedDebugAndroidTest` (device/emulator) |
| Xcode accessibility audit (`performAccessibilityAudit`) on every tab and the detail screen, plus Home/Cart/Profile at AX5 | `iosApp/CocktailCraftUITests/AccessibilityAuditTests.swift` | the full iOS scheme (`xcodebuild test ...`) |
| Font-setting / Dynamic Type mapping and spoken ratings | `iosApp/CocktailCraftTests/AccessibilityTests.swift` | the full iOS scheme |
| Large-font and high-contrast screenshots (see above) | Android: `LargeFontScreenshotTest`, `AccessibilityScreenshotTest`; iOS: `AccessibilitySnapshotTests.swift`, `ScreenSnapshotTests.swift` | the screenshot runs above |

The iOS audit is network-tolerant: it audits whatever state renders (content,
loading, offline, error), dismissing an error alert first, and hides the
Debug-only DebugSwift overlay (`HIDE_DEBUG_OVERLAY`), which never ships.

**Known exclusion — contrast.** The brand coral (`#EB6A43`, 3.2:1 on white),
the default secondary gray (`#8E8E93`, 3.3:1) and the success green are below
the 4.5:1 WCAG AA ratio for small text, so contrast checks are suppressed in
the ATF and Xcode audits rather than failing on a design decision. The iOS
in-app **High Contrast** setting strengthens secondary text (Android has no
in-app toggle; its theme still applies the shared setting, including the gray
outline role in light theme); changing the brand palette itself is out of
scope for these checks.

Conventions for new UI:
- Every user-facing label, hint, value and announcement is a shared string
  (see [Localization](#localization)); accessibility-only copy uses an `_a11y`
  key suffix.
- Toggles keep one stable label and expose their state (Compose
  `IconToggleButton`/`toggleable`, SwiftUI `.isSelected`/`.isToggle` traits)
  instead of a label that flips between "Add" and "Remove".
- Decorative images and icons next to text are hidden from screen readers;
  composite rows read as one element; section titles are headings.
- Touch targets are at least 48dp / 44pt; layouts wrap or stack at large
  text sizes instead of truncating.
- On iOS the in-app Font Size steps relative to the system Dynamic Type size
  (so accessibility sizes apply) and Reduce Motion follows the in-app setting
  or the system one. Android has no in-app Appearance section and follows the
  system font scale.

