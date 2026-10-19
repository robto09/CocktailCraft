# Shared ViewModel Strategy

The screens of both apps are driven by ViewModels written once in Kotlin, in
the `:presentation` module. Android's Compose screens use them directly; the
iOS app wraps each one in a thin Swift adapter that SKIE makes possible. This
document describes that contract and how each platform consumes it.

## ViewModel inventory

### Shared ViewModels (Kotlin)

**Location**: `presentation/src/commonMain/kotlin/com/cocktailcraft/viewmodel/`
(`:presentation`, re-exported to Android and Swift by the `:shared` umbrella).
Their state classes are in `viewmodel/state/`.

Nine ViewModels on the `SharedViewModel` base class, registered in
`presentation/.../di/PresentationModule.kt` (the use cases they take come from
`DomainModule.kt`, which also lives in `:presentation`):

| ViewModel | Scope | Screen |
|-----------|-------|--------|
| `SharedHomeViewModel` | `single` | Home: catalog by category, debounced and advanced search, sort, recently viewed, review averages, offline fallback |
| `SharedCartViewModel` | `single` | Cart, plus add-to-cart from Home and Favorites |
| `SharedFavoritesViewModel` | `single` | Favorites |
| `SharedOrderViewModel` | `single` | Order list and placing an order |
| `SharedProfileViewModel` | `single` | Profile: sign-in, sign-up, sign-out, profile edit, password change |
| `SharedThemeViewModel` | `single` | Theme and accessibility settings |
| `SharedCocktailDetailViewModel` | `viewModel {}` (screen) | Cocktail detail |
| `SharedReviewViewModel` | `viewModel {}` (screen) | A cocktail's reviews and the review sheet |
| `SharedOrderDetailViewModel` | `viewModel {}` (screen) | Order detail: cancel and reorder |

The `single`s are shared by every screen and live for the whole process: a
Koin `single` never goes through a `ViewModelStore`, so `onCleared()` never
runs on them (see the scoping notes in `PresentationModule.kt`). The
screen-scoped ones are a new instance per screen; anything that belongs to
one screen (its loading flags, the item it shows) lives in one of those,
never in a singleton.

### iOS wrappers (Swift)

**Location**: `iosApp/CocktailCraft/ViewModels/`. One wrapper per shared
ViewModel the app uses, on the `SharedViewModelWrapper` base class:
`HomeViewModelSKIE`, `CartViewModelSKIE`, `FavoritesViewModelSKIE`,
`OrderViewModelSKIE`, `ProfileViewModelSKIE`, `ThemeViewModelSKIE`,
`CocktailDetailViewModelSKIE`, `ReviewViewModelSKIE` and
`OrderDetailViewModelSKIE`.

### Android: no wrapper layer

The shared ViewModels extend the multiplatform androidx `ViewModel`, so
Compose screens use them as they are.

## The contract

Every shared ViewModel exposes the same surface, and both platforms use only
that surface.

### State: one `uiState`

- Each ViewModel has exactly one `val uiState: StateFlow<XxxUiState>`, backed
  by a private `MutableStateFlow` and changed with `update { it.copy(...) }`.
- The `XxxUiState` classes are immutable, val-only data classes, declared
  stable for Compose in `androidApp/compose-stability-config.conf`.
- Values derived from state live on the state, as getters or as fields set
  in the same update as their source (`CartUiState.finalTotal`,
  `DetailUiState.shareableText`, `ThemeUiState.isDarkMode`), so every
  emission is self-consistent and every read is observed on both platforms.
- There are no per-field `StateFlow`s and no getters on the ViewModel that
  read `_uiState.value`: Compose and SwiftUI Observation can't see such a
  read, so it renders stale data. A ViewModel may still have constant option
  lists (`curatedCategories`) and pure helpers that take their input as a
  parameter (`isValidEmail(email)`).

### Errors: the consumable `error`

`SharedViewModel` has one error channel for every subclass:
`val error: StateFlow<UserFriendlyError?>` plus `clearError()`. It is a
`StateFlow`, not an event, on purpose: an error raised while nothing collects
(a configuration change, a wrapper being recreated) is still there when the
UI observes again, and the UI clears it once it has shown it. Every load or
action clears it when it starts. Failures are mapped to a user-facing error
in one place, `ErrorHandler`.

### Events: `events`, collected by the owning screen

`SharedViewModel.events` is a `SharedFlow<UiEvent>` with no replay for
one-off outcomes a screen toasts: "Removed … from cart" with an Undo action,
"Order cancelled", "Password changed". Only the screen that owns the
ViewModel collects them, never an app-wide host:

| Screen | Android (`UiEventToasts`) | iOS (`.sharedEventToast`) |
|--------|---------------------------|---------------------------|
| Cart | `CartScreen` | `CartView` |
| Favorites | `FavoritesScreen` | `FavoritesView` |
| Profile | `ProfileScreen` | `ProfileView` |
| Cocktail detail | `CocktailDetailScreen` | `CocktailDetailView` |
| Order detail | `OrderDetailScreen` | `OrderDetailView` |

An action that runs from another screen returns its outcome to that caller
instead: `SharedCartViewModel.addToCart` is called from Home and Favorites
and returns the `UiEvent` the caller toasts, so a failure never surfaces
later on the Cart tab. (The detail screen adds to the cart through its own
ViewModel, whose `events` it already collects.)

### Actions run in `viewModelScope`

- Actions are plain public methods named for what they do (`selectCategory`,
  `toggleFavorite`, `removeFromCart`, `setHighContrast`); there is no
  `Intent`/`onEvent` dispatcher.
- Every action runs its work in `viewModelScope`, never in the caller's
  coroutine, so a screen that goes away (a composition left, a Swift `Task`
  cancelled) can't stop a write or a refresh halfway. Most actions are plain
  `fun`s that launch their work and report back through `uiState`, `error`
  and `events`; both platforms call them without a coroutine or `Task`.
- An action is `suspend` only when its caller has to wait for it: a result
  to branch on (`placeOrder`, `signIn`, `signUp`, `submitReview` return
  `Boolean`; `addToCart` returns its `UiEvent`) or the end of a
  pull-to-refresh (`refresh`, `loadFavorites`, `loadOrders`). It runs its
  body through `runInViewModelScope`, so cancelling the caller only stops the
  waiting.
- Every Swift-facing `suspend` function is `@Throws(Exception::class)`:
  without it, an exception that reaches Swift terminates the app instead of
  becoming a Swift error.
- Every `catch` rethrows `CancellationException`
  (`runCatchingNonCancellation`, `runCatchingResult`): cancellation is
  teardown, not a failure to report.
- Changes are value-based where a callback could fire twice:
  `setHighContrast(enabled)`, not `toggleHighContrast()`.

## Android

Screen-scoped ViewModels are resolved with `koinViewModel()` inside their
navigation destination, scoped to its back-stack entry. The `single`s are
resolved with `koinInject()`; `MainScreen` resolves only the cart and
favorites ViewModels its chrome needs on every tab, and Home and Orders are
resolved by the destinations that use them.

Each screen is a stateful entry point plus a stateless `internal`
`*Content` composable. The entry point resolves the ViewModel, collects
`uiState` and `error` once with `collectAsStateWithLifecycle()`, collects its
own `events`, and passes state and action lambdas down:

```kotlin
@Composable
fun CartScreen(viewModel: SharedCartViewModel, /* ... */) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    UiEventToasts(viewModel.events)

    CartContent(
        state = state,
        error = error,
        onRemove = { item -> viewModel.removeFromCart(item.cocktail.id) },
        /* ... */
    )
}
```

The same split exists for Home, Favorites, Orders, Order detail, Cocktail
detail and Profile, which is what the previews and Roborazzi screenshot
tests render.

## iOS

SwiftUI can't observe a Kotlin `StateFlow`, so each shared ViewModel gets a
wrapper. `SharedViewModelWrapper` is `@MainActor @Observable` and does
everything the wrappers have in common:

- seeds `state` synchronously from `uiState.value`, so the first frame shows
  current data, then mirrors `uiState`, `error` and `events` into the
  Observation-tracked `state`, `error` and `event` properties through SKIE's
  `AsyncSequence` bridging;
- forwards `clearError()` and offers `clearEvent()` for the mirrored event;
- cancels its observation tasks on deinit and, for the screen-scoped
  ViewModels it owns (`ownsViewModel: true`), clears the shared instance
  through `KoinHelper.clearScreenViewModel`, the way Android's
  `ViewModelStore` would;
- runs forwarded `suspend` calls through `run { }`, the one place a failure
  is logged (the user sees it through `error`).

Views read `viewModel.state.<field>`. Subclasses only forward actions and add
Swift-only formatting; they never re-publish individual fields:

```swift
final class CartViewModelSKIE: SharedViewModelWrapper<CartUiState> {
    private let sharedViewModel: SharedCartViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedCartViewModel()
        self.sharedViewModel = viewModel
        super.init(viewModel: viewModel, uiState: viewModel.uiState)
    }

    // Runs on the shared viewModelScope; the outcome arrives through state/events
    func removeFromCart(_ cocktailId: String) {
        sharedViewModel.removeFromCart(cocktailId: cocktailId)
    }
}
```

Views hold their wrapper through `ViewModelHolder`, so a parent re-render
doesn't build (and resolve from Koin) a throwaway wrapper each time.
`KoinHelper` (`shared/src/iosMain/.../KoinIOS.kt`) has one getter per
ViewModel; `KoinHelperTest` checks each one resolves.

## SKIE configuration

The SKIE version lives in the version catalog and the plugin is applied
through its alias:

```toml
# libraries.toml
[versions]
skie = "0.10.13"

[plugins]
skie = { id = "co.touchlab.skie", version.ref = "skie" }
```

```kotlin
// shared/build.gradle.kts
plugins {
    alias(libs.plugins.skie)
}

// Flows, suspend functions, enums and sealed classes are on by default;
// default arguments are not.
skie {
    features {
        group {
            co.touchlab.skie.configuration.DefaultArgumentInterop.Enabled(true)
        }
        // moko-resources reaches the framework through :core:resources; no
        // default-argument overloads for its classes
        group("dev.icerock.moko") {
            co.touchlab.skie.configuration.DefaultArgumentInterop.Enabled(false)
        }
    }
}
```

SKIE runs only in `:shared`, the umbrella that builds the framework, over the
modules it `export()`s (`:presentation`, `:core:domain`, `:core:common`,
`:core:resources`, `:core:designsystem`). What the iOS code relies on:

- **Flows** (default): `Flow`/`StateFlow` arrive in Swift as `AsyncSequence`,
  with `.value` on a `StateFlow`; the wrappers' mirroring is built on this.
- **Suspend functions** (default): `suspend` functions become Swift `async`
  functions, and cancelling the Swift `Task` cancels the Kotlin call.
- **Enums and sealed classes** (default): Kotlin enums become Swift enums,
  and sealed hierarchies such as `UiEvent` get `onEnum(of:)` for exhaustive
  `switch`es.
- **Default arguments** (enabled above): Kotlin default parameter values
  produce Swift overloads that leave those parameters out.

If Xcode doesn't see a new Kotlin API, rebuild the framework (the app's
"Compile Kotlin Framework" phase) before suspecting SKIE. For other options
see [skie.touchlab.co](https://skie.touchlab.co).
