# iOS UI Components Documentation

This document describes the reusable UI components in the CocktailCraft iOS app. Components live in `iosApp/CocktailCraft/Components/`, the design system in `iosApp/CocktailCraft/Theme/`. It is the iOS counterpart to [UI_Components.md](UI_Components.md) (Android).

Scope note: the iOS app extracts fewer components than Android. Several screens keep their sections as private computed views inside the screen file (e.g. `ProfileView`'s `profileHeaderCard`, `accountSettingsCard`, `appSettingsCard`, `aboutCard`) rather than as shared components — those inline views are not documented individually here.

## Table of Contents

1. [Design System (Theme)](#design-system-theme)
2. [Cocktail & Home Components](#cocktail--home-components)
3. [Cart Components](#cart-components)
4. [Profile & Auth Components](#profile--auth-components)
5. [Offline & Sync Components](#offline--sync-components)
6. [Feedback & State Components](#feedback--state-components)
7. [Usage Guidelines](#usage-guidelines)

## Design System (Theme)

### AppColors
**File**: `Theme/AppColors.swift`

Color palette matching the Android design. Theme-dependent colors are exposed as `isDarkMode:`-parameterized functions; static utility colors (`success`, `error`, `warning`, `gray`, `lightGray`, `chipBackground`) are theme-independent.

```swift
AppColors.primary(isDarkMode: isDarkMode)
AppColors.surface(isDarkMode: isDarkMode)
AppColors.textPrimary(isDarkMode: isDarkMode)
```

Always pair these functions with `@Environment(\.isDarkMode)`. The old trait-collection-driven static vars were removed on purpose: they followed the *system* appearance while the app follows its own theme preference (`ThemeViewModelSKIE`), so call sites could visually desync. Also provides a `Color(hex:)` initializer.

### BrandColorComponents
**File**: `Utils/WidgetBridge.swift`

Cross-target brand color tokens: `primary` (#EB6A43 coral) and `secondary` (#FFC84D gold) as raw RGB components. `WidgetBridge.swift` is compiled into both the app and the widget extension (which does not link the shared framework), so both targets render the brand colors from one definition. `AppColors.primaryLight`/`secondaryLight` are built from these tokens.

### AppTheme
**File**: `Theme/AppTheme.swift`

Design constants matching Android: `Typography` (Dynamic Type-anchored fonts), `Spacing` (xs...xxxl plus card-specific values), `CornerRadius`, `Shadow`, and `Animation` (standard/quick/slow). Also defines:

- The `isDarkMode` SwiftUI environment value (`ThemeEnvironmentKey`)
- `.cardStyle()` — surface background, card corner radius, and shadow
- `.chipStyle(isSelected:)` — filter-chip styling
- `.minimumHitTarget()` — expands icon-only controls to Apple's 44 pt HIG minimum

```swift
Text("Title").font(AppTheme.Typography.headline)
VStack { ... }.cardStyle()
```

## Cocktail & Home Components

### CocktailCard
**Purpose**: The main cocktail card, with `.horizontal` (Home list) and `.vertical` (Favorites grid) layouts. Its out-of-stock rule intentionally mirrors `CocktailDetailViewModelSKIE.canAddToCart()`.

```swift
CocktailCard(
    cocktail: cocktail,
    isFavorite: isFavorite,
    onFavoriteToggle: { ... },
    onAddToCart: { ... },
    onCardTap: { ... },
    layout: .horizontal
)
```

**Used in**: `HomeViewSKIE`, `FavoritesView`, `CocktailDetailView` (recommendations).

### CocktailImageView
**Purpose**: Kingfisher-backed cocktail image with disk+memory caching, retry (3 attempts), a spinner placeholder while loading, and a static fallback when there is no URL.

**Parameters**: `imageUrl: String?`, `height: CGFloat = 300`, `cornerRadius: CGFloat = 0`, `width: CGFloat? = nil` (nil keeps flexible-width hero behavior).

**Used in**: `CocktailCard`, `CartItemCard`, `CocktailDetailView`, `OfflineModeView`.

### HomeSearchBar
**Purpose**: Home search field with clear button and the advanced-search toggle button.

**Parameters**: `searchText: Binding<String>`, `onSubmit`, `onClear`, `onAdvancedSearch`.

**Used in**: `HomeViewSKIE`.

### AdvancedSearchSheet
**Purpose**: Advanced-search filter sheet driven by the shared HomeViewModel, styled identically to Android's `AdvancedSearchBottomSheet`: Clear / title / Apply header row and three collapsible sections — Category (dropdown menu), Ingredient (native searchable list sheet with checkmark selection), Alcoholic (tri-state chips). Option lists come from the shared API-backed data; Apply forwards a `SearchFilters` to the shared `applyFilters`, Clear resets them.

**Parameters**: `viewModel: HomeViewModelSKIE`.

**Used in**: `HomeViewSKIE` (presented as a sheet).

### CategoryChipRow
**Purpose**: Horizontal category filter chips ("All" plus the curated categories).

**Parameters**: `categories: [String]`, `selectedCategory: String?`, `onSelectAll`, `onSelect: (String) -> Void`.

**Used in**: `HomeViewSKIE`.

### CocktailListSkeleton
**Purpose**: Redacted placeholder list matching the cocktail card layout, shown while the first page loads (the deliberate exception to `LoadingStateView`).

**Parameters**: `rowCount: Int = 6`.

**Used in**: `HomeViewSKIE`.

### HomeEmptyStateView
**Purpose**: Home-specific empty state with an optional action button (e.g. clear search).

**Parameters**: `icon`, `title`, `subtitle`, `actionTitle: String? = nil`, `action: (() -> Void)? = nil`.

**Used in**: `HomeViewSKIE`.

## Cart Components

### CartItemCard
**Purpose**: Cart row with image, name/category, quantity stepper, remove, and an optional favorite toggle.

**Parameters**: `item: CocktailCartItem`, `onIncrementQuantity`, `onDecrementQuantity`, `onRemoveFromCart`, `onToggleFavorite: (() -> Void)?`.

**Used in**: `CartView`.

### CartSummaryCard
**Purpose**: Order summary showing subtotal, delivery fee, and total (file-local `SummaryRow` renders each line).

**Parameters**: `subtotal: Double`, `deliveryFee: Double`, `total: Double`.

**Used in**: `CartView`.

### CheckoutButton
**Purpose**: Primary checkout action button with loading and disabled states.

**Parameters**: `title: String = "Place Order"`, `isEnabled: Bool = true`, `isLoading: Bool = false`, `action`.

**Used in**: `CartView`.

## Profile & Auth Components

### ProfileSupportViews.swift
A small collection of profile-screen building blocks:

- **SettingsRow** — tappable menu row (`icon`, `title`, `action`, `textColor = .primary`) with chevron, full-width hit testing, and the accessibility identifier `profile.row.<title>`
- **ThemeToggleRow** — toggle row (`title`, `subtitle`, `icon`, `isChecked`, `onToggle: (Bool) -> Void`, `enabled = true`); the callback receives the toggle's target value so duplicate firings are idempotent
- **AnimatedThemeSwitch** — animated sun/moon switch (currently unused by any screen)
- **PrimaryButtonStyle** / **SecondaryButtonStyle** — `ButtonStyle`s for the sign-in/sign-up buttons

**Used in**: `ProfileView`.

### PasswordStrengthMeter
**Purpose**: Five-segment meter for the shared `AuthInputValidator`'s 0-5 password strength scale, with matching color and label. Shared by both password forms so the visualization cannot drift between them.

**Parameters**: `strength: Int`.

**Used in**: `SignUpView`, `ChangePasswordView`.

## Offline & Sync Components

### OfflineBanner
**Purpose**: Orange "You're offline" banner that animates in/out, driven by `NetworkMonitor.shared`. Takes no parameters.

**Used in**: `ContentView` (overlaid on the tab navigation, app-wide).

### BackgroundSyncCard
**Purpose**: Background-sync status and controls: enable/disable toggle, last-sync and next-sync display, and a manual "Sync Now" button. Reads `BackgroundSyncManager.shared` directly; takes no parameters (file-local `SyncInfoRow` renders the status lines).

**Used in**: `OfflineModeView`.

## Feedback & State Components

### SharedErrorAlert
**Purpose**: Standard error surfacing for screens backed by a shared KMP ViewModel. Presents the ViewModel's mirrored error channel as an alert, offers the error's recovery action when present, and clears the error on the shared side on dismissal so stale state cannot re-present. Applied via the `View` extension:

```swift
.sharedErrorAlert(viewModel.error) { viewModel.clearError() }
```

**Used in**: nearly every screen (`HomeViewSKIE`, `FavoritesView`, `CartView`, `OrderListView`, `ProfileView`, `CocktailDetailView`, ...).

### ErrorView
**Purpose**: Full-frame error state for a shared `ErrorHandler.UserFriendlyError`, with a category-specific icon/color and a recovery-action or retry button.

**Parameters**: `error: ErrorHandler.UserFriendlyError`, `onRetry: (() -> Void)?`.

**Used in**: `CocktailDetailView`.

### EmptyStateView
**Purpose**: Generic empty state (SF Symbol icon, title, message). The title carries the accessibility identifier `emptyState.title` as a stable hook for UI tests.

**Parameters**: `icon: String`, `title: String`, `message: String`.

**Used in**: `CartView`, `FavoritesView`.

### LoadingStateView
**Purpose**: Standard full-frame loading indicator with an optional message. Use this instead of ad-hoc `ProgressView` arrangements so loading looks the same on every screen (Home's skeleton list is the deliberate exception).

**Parameters**: `message: String? = nil`.

**Used in**: `OrderListView`, `CocktailDetailView`.

### Toast
**Purpose**: Transient confirmation banner with `.success`/`.error`/`.info` styles, applied via the modifier:

```swift
.toast(isShowing: $showingToast, message: toastMessage, type: .success, duration: 2)
```

**Used in**: `HomeViewSKIE` (add-to-cart confirmation).

### LoadingOverlay
**Purpose**: Modal dark overlay with spinner and message, plus a `.loadingOverlay(isShowing:message:)` modifier. Currently not used by any screen.

### ShimmerEffect / CocktailCardPlaceholder
**Purpose**: Animated shimmer gradient and a shimmering card placeholder built on it. Currently not used by any screen (`CocktailListSkeleton` covers the Home loading state).

## Usage Guidelines

1. **Theming**: Read the theme with `@Environment(\.isDarkMode)` and resolve colors through the `AppColors` functions — never through system appearance.
2. **Spacing, typography, radii**: Use the `AppTheme` constants rather than literals.
3. **Cards and chips**: Use `.cardStyle()` and `.chipStyle(isSelected:)` for consistent container styling.
4. **Touch targets**: Apply `.minimumHitTarget()` to icon-only buttons.
5. **Errors**: Screens backed by a shared ViewModel surface errors with `.sharedErrorAlert`; use `ErrorView` for full-frame error states.
6. **Loading and empty states**: Prefer `LoadingStateView` and `EmptyStateView` over ad-hoc arrangements.
7. **Reuse first**: Check this list before writing a new component, and document new components here.
