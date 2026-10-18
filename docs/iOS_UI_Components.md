# iOS UI Components Documentation

This document describes the reusable UI components in the CocktailCraft iOS app. Components live in `iosApp/CocktailCraft/Components/`, the design system in `iosApp/CocktailCraft/Theme/`. It is the iOS counterpart to [UI_Components.md](UI_Components.md) (Android).

Scope note: the iOS app extracts fewer components than Android. Several screens keep their sections as private computed views inside the screen file (e.g. `ProfileView`'s `profileHeaderCard`, `accountSettingsCard`, `appSettingsCard`, `aboutCard`) rather than as shared components — those inline views are not documented individually here.

## Table of Contents

1. [Design System (Theme)](#design-system-theme)
2. [Cocktail & Home Components](#cocktail--home-components)
3. [Cart Components](#cart-components)
4. [Order Components](#order-components)
5. [Review Components](#review-components)
6. [Profile & Auth Components](#profile--auth-components)
7. [Offline Components](#offline-components)
8. [Feedback & State Components](#feedback--state-components)
9. [Usage Guidelines](#usage-guidelines)

## Design System (Theme)

### AppColors
**File**: `Theme/AppColors.swift`

Color palette mapped from the shared design tokens (`ColorTokens`). Theme-dependent colors are functions: those that also follow the accent or high-contrast setting take the whole `AppAppearance` (`primary(_:)`, `textSecondary(_:)`, `topBar(_:)`), the rest take `isDarkMode:`. Static utility colors (`success`, `error`, `warning`, `gray`, `lightGray`, `chipBackground`) are theme-independent.

```swift
AppColors.primary(appearance)                                  // follows the accent
AppColors.primary(isDarkMode: true, accentName: accentName)    // a specific variant, e.g. a theme-switch preview
AppColors.surface(isDarkMode: isDarkMode)
AppColors.textSecondary(appearance)                            // high-contrast aware
```

Always pair these functions with `@Environment(\.appearance)` or `@Environment(\.isDarkMode)`. The old trait-collection-driven static vars were removed on purpose: they followed the *system* appearance while the app follows its own theme preference (`ThemeViewModelSKIE`), so call sites could visually desync. Also provides a `Color(hex:)` initializer.

### BrandColorComponents
**File**: `Utils/WidgetBridge.swift`

Cross-target brand color tokens: `primary` (#EB6A43 coral) and `secondary` (#FFC84D gold) as raw RGB components. `WidgetBridge.swift` is compiled into both the app and the widget extension (which does not link the shared framework), so both targets render the brand colors from one definition. `AppColors.primaryLight`/`secondaryLight` are built from these tokens.

### AppTheme
**File**: `Theme/AppTheme.swift`

Design constants matching Android: `Typography` (Dynamic Type-anchored fonts), `Spacing` (xs...xxxl), `CornerRadius`, `Shadow`, and `Animation` (standard/quick). Also defines:

- `AppAppearance` (dark mode, accent name, high contrast), published by `ContentView` from the shared theme state as the `appearance` environment value (`AppAppearanceKey`), plus the `isDarkMode` shorthand for `appearance.isDarkMode`
- `.cardStyle()` — surface background, card corner radius, and shadow
- `.brandedNavigationBar(title:)` — the coral navigation bar with a white title, used by every screen
- `.brandedTabBar()` — the opaque surface tab bar matching Android's bottom navigation (applied in `ContentView`)
- `.minimumHitTarget()` — expands icon-only controls to Apple's 44 pt HIG minimum

```swift
Text("Title").font(AppTheme.Typography.headline)
VStack { ... }.cardStyle()
```

## Cocktail & Home Components

### CocktailCard
**Purpose**: The cocktail row for the Home and Favorites lists, laid out with the same metrics as Android's `CocktailItem` (12 padding, 100 pt image, heart and filled cart 48 pt apart). Add to Cart is disabled for a cocktail marked out of stock (never the case with API data). On Home it also shows the user's own review average (`reviewRating`). VoiceOver reads the whole card (name, category, ingredients, price, stock) in one stop on the title; the heart ("Favorite", selected when on) and "Add <name> to cart" buttons stay separate elements. At accessibility text sizes the image stacks above the text.

```swift
CocktailCard(
    cocktail: cocktail,
    isFavorite: isFavorite,
    reviewRating: summary,          // optional, Home only
    onFavoriteToggle: { ... },
    onAddToCart: { ... },
    onCardTap: { ... }
)
```

**Used in**: `HomeViewSKIE`, `FavoritesView`. (The detail screen's related cocktails use the smaller `RelatedCocktailCard` in `DetailSections.swift`.)

### CocktailImageView
**Purpose**: Kingfisher-backed cocktail image with disk+memory caching, retry (3 attempts), a shimmering placeholder block while loading, and a static fallback when there is no URL.

**Parameters**: `imageUrl: String?`, `height: CGFloat = 300`, `cornerRadius: CGFloat = 0`, `width: CGFloat? = nil` (nil keeps flexible-width hero behavior).

**Used in**: `CocktailCard`, `CartItemCard`, `RecentlyViewedRow`, `CocktailDetailView` and its `DetailSections`.

### HomeSearchBar
**Purpose**: Home search field with clear button and the advanced-search toggle button.

**Parameters**: `searchText: Binding<String>`, `onSubmit`, `onClear`, `onAdvancedSearch`, `isFilterActive: Bool = false` (fills the filter button while filters are applied).

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
**Purpose**: Shimmering placeholder list matching the cocktail card layout, shown while the first page loads. Built from `SkeletonBlock`s and `.shimmering()`.

**Parameters**: `rowCount: Int = 6`.

**Used in**: `HomeViewSKIE`.

### SearchEmptyStateView
**File**: `Components/EmptyStateView.swift`

**Purpose**: Context-aware no-results state for the Home search, mirroring Android's `EmptySearchResultsMessage`: icon, title, subtitle and actions vary with the active query, category and advanced-search filters.

**Parameters**: `searchQuery`, `selectedCategory: String?`, `hasActiveFilters`, `onClearSearch`, `onClearCategory`, `onClearFilters`, `onRefresh`.

**Used in**: `HomeViewSKIE`.

### ActiveFiltersCard
**File**: `Components/HomeSearchBar.swift`

**Purpose**: Card of the active advanced-search filters, mirroring Android's `SearchFilterChips`: a header row (filter icon, "Active Filters", Clear All) above wrapping chips. Tapping a chip applies the reduced `SearchFilters`; Clear All resets everything.

**Parameters**: `filters: SearchFilters`, `onApply: (SearchFilters) -> Void`, `onClearAll`.

**Used in**: `HomeViewSKIE`.

### HomeSortMenu
**Purpose**: Home's sort control in the navigation bar, the counterpart of Android's `HomeSortButton`: a menu of every shared `HomeSortOption` with the current one checked; the icon fills in while a non-default sort is applied.

**Parameters**: `selection: HomeSortOption`, `onSelect: (HomeSortOption) -> Void`.

**Used in**: `HomeViewSKIE`.

### RecentlyViewedRow
**Purpose**: Home's "Recently viewed" heading and horizontally scrolling row of compact cards, newest first, as Android's `RecentlyViewedRow`. Each card pushes the cocktail's detail page through the enclosing stack's `navigationDestination(for: String.self)`.

**Parameters**: `cocktails: [Cocktail]`, `horizontalBleed: CGFloat = 0` (lets the cards scroll to the screen edges while the first one lines up with the heading).

**Used in**: `HomeViewSKIE`.

## Cart Components

### CartItemCard
**Purpose**: Cart row with image, name/category, quantity stepper, remove, and an optional favorite toggle. Stepper and remove buttons are labeled with the item name and quantity changes are announced; the line total and remove button move below the stepper when the row doesn't fit (large Dynamic Type).

**Parameters**: `item: CocktailCartItem`, `onIncrementQuantity`, `onDecrementQuantity`, `onRemoveFromCart`, `onToggleFavorite: (() -> Void)?`.

**Used in**: `CartView`.

### CartSummaryCard
**Purpose**: Order summary showing subtotal, delivery fee, and total (file-local `SummaryRow` renders each line).

**Parameters**: `subtotal: Money`, `deliveryFee: Money`, `total: Money` (shared `Money`, formatted by the shared `Money.formatted()`).

**Used in**: `CartView`, `OrderDetailContent` (order detail screen).

### CheckoutButton
**Purpose**: Primary checkout action button with loading and disabled states.

**Parameters**: `title: String = L(\.cart_place_order)`, `isEnabled: Bool = true`, `isLoading: Bool = false`, `action`.

**Used in**: `CartView`.

## Order Components

### OrderComponents.swift
- **OrderStatusIndicator** — a colored dot and the status's localized label; `OrderStatus.tint` gives the status color (as Android's `orderStatusColor`). Used in `OrderListView` and the order detail.
- **OrderDetailContent** — the stateless body of the order detail screen, laid out like Android's `OrderDetailContent`: number/date/status/delivery card, items card, `CartSummaryCard`, Reorder, then Cancel Order while the order is cancellable. Parameters: `order`, `subtotal: Money`, `deliveryFee: Money`, `deliveryEstimate`, `canCancel`, `isCancelling`, `onCancel`, `canReorder`, `isReordering`, `onReorder` (values come from the shared `OrderUiState`).

**Used in**: `OrderDetailView`, `OrderListView`.

## Review Components

### ReviewComponents.swift
- **StarRatingView** — read-only row of stars mirroring Android's `RatingBar` (`rating: Float`, `maxRating = 5`, `starSize = 12`, `spacing = 2`). Used by `CocktailCard` and `ReviewRow`.
- **ReviewRow** — one review in the detail screen's Reviews card, mirroring Android's `CocktailReviewItem`: initial avatar, name, stars and date, then the comment. Used in `DetailSections`.

## Profile & Auth Components

### ProfileSupportViews.swift
A small collection of profile-screen building blocks:

- **SettingsRow** — tappable menu row (`icon`, `title`, `action`, `textColor = .primary`) with chevron, full-width hit testing, and the accessibility identifier `profile.row.<title>`
- **ThemeToggleRow** — toggle row (`title`, `subtitle`, `icon`, `isChecked`, `onToggle: (Bool) -> Void`, `enabled = true`); the callback receives the toggle's target value so duplicate firings are idempotent. VoiceOver sees one switch per row (label, On/Off value, toggle trait)
- **AnimatedThemeSwitch** — animated sun/moon switch (currently unused by any screen)
- **PrimaryButtonStyle** / **SecondaryButtonStyle** — `ButtonStyle`s for the sign-in/sign-up buttons

**Used in**: `ProfileView`.

### PrimaryButton
**Purpose**: The reusable primary call to action, matching Android's `PrimaryButton`: a full-width rounded coral button with a bold label, an optional leading SF Symbol, and a spinner that replaces the label while loading. `isDestructive` swaps in the error color.

**Parameters**: `title`, `icon: String? = nil`, `isLoading = false`, `isDisabled = false`, `isDestructive = false`, `action`.

**Used in**: the auth and profile forms (`SignInView`, `SignUpView`, `EditProfileView`, `ChangePasswordView`), `WriteReviewView`, `EmptyStateView`'s action, `DetailSections`, `OrderDetailContent`.

### PasswordStrengthMeter
**Purpose**: Five-segment meter for the shared `AuthInputValidator`'s 0-5 password strength scale, with matching color and label. Shared by both password forms so the visualization cannot drift between them.

**Parameters**: `strength: Int`.

**Used in**: `SignUpView`, `ChangePasswordView`.

## Offline Components

### OfflineBanner
**Purpose**: "You're offline — showing saved cocktails." banner that animates in/out while the network is down, driven by `NetworkMonitor.shared`. Takes no parameters. Cached content keeps rendering underneath it.

**Used in**: `ContentView` (overlaid on the tab navigation, app-wide).

## Feedback & State Components

### SharedErrorAlert
**Purpose**: Standard error surfacing for screens backed by a shared KMP ViewModel. Presents the ViewModel's mirrored error channel as an alert, offers the error's recovery action when present, and clears the error on the shared side on dismissal so stale state cannot re-present. Applied via the `View` extension:

```swift
.sharedErrorAlert(viewModel.error) { viewModel.clearError() }
```

**Used in**: nearly every screen (`HomeViewSKIE`, `FavoritesView`, `CartView`, `OrderListView`, `OrderDetailView`, `ProfileView`, `CocktailDetailView`).

### ErrorView
**Purpose**: Full-frame error state for a shared `ErrorHandler.UserFriendlyError`, with a category-specific icon/color and a recovery-action or retry button. The error's title, message and action label are shared `StringDesc`s, shown via `.localized()`.

**Parameters**: `error: ErrorHandler.UserFriendlyError`, `onRetry: (() -> Void)?`.

**Used in**: `CocktailDetailView`, `OrderDetailView`.

### EmptyStateView
**Purpose**: Generic empty state (SF Symbol icon, title, message). The title carries the accessibility identifier `emptyState.title` as a stable hook for UI tests.

**Parameters**: `icon: String`, `title: String`, `message: String`, `actionTitle: String? = nil`, `action: (() -> Void)? = nil` (an optional `PrimaryButton`).

**Used in**: `CartView`, `FavoritesView`, `OrderListView`, `HomeViewSKIE`.

### Shimmer / SkeletonBlock
**File**: `Components/Shimmer.swift`

**Purpose**: The loading-skeleton building blocks, the iOS counterpart of Android's `Modifier.placeholder` skeletons. `SkeletonBlock(width:height:cornerRadius:)` draws one shape in the `neutralSubtle` fill (a nil width fills the row); `.shimmering()` sweeps a surface-colored highlight across the shapes of the view it is applied to. Apply `.shimmering()` once to a group of blocks (not to each block) so they shimmer as one wave, and apply it before a card's background so only the blocks shimmer. Reduce Motion (in-app or system) turns the sweep off through ContentView's app-wide transaction.

```swift
VStack(alignment: .leading, spacing: 8) {
    SkeletonBlock(width: 120, height: 20)
    SkeletonBlock(height: 16)
}
.shimmering()
```

### CocktailDetailSkeleton / OrderListSkeleton
**File**: `Components/LoadingSkeletons.swift`

**Purpose**: Full-screen skeletons laid out like the screens they stand in for; VoiceOver reads the screen's loading message instead of the blocks. `DetailSections`' `relatedCocktailsLoadingSection` shows three shimmering cards while related cocktails load.

**Parameters**: `OrderListSkeleton(cardCount: Int = 3)`.

**Used in**: `CocktailDetailView`, `OrderListView`, `OrderDetailView`.

### Toast
**Purpose**: Transient banner with `.success`/`.error`/`.info` styles and an optional action button (`actionLabel`/`onAction`, e.g. Undo; the action is also exposed to VoiceOver). Two modifiers apply it:

```swift
// A screen's own toast
.toast(isShowing: $showingToast, message: toastMessage, type: .success, duration: 2)

// A shared ViewModel's one-shot UiEvents, including Undo after a removal
.sharedEventToast(viewModel.event, clear: { viewModel.clearEvent() })
```

A toast with an action stays up longer (5 s, 10 s under VoiceOver) so it can be reached.

**Used in**: `.toast` in `HomeViewSKIE` and `FavoritesView` (add-to-cart confirmation); `.sharedEventToast` in `CartView`, `FavoritesView`, `CocktailDetailView`, `OrderDetailView` and `ProfileView`.

## Usage Guidelines

1. **Theming**: Read the theme with `@Environment(\.appearance)` / `@Environment(\.isDarkMode)` and resolve colors through the `AppColors` functions — never through system appearance. Give screens the shared chrome with `.brandedNavigationBar(title:)`.
2. **Spacing, typography, radii**: Use the `AppTheme` constants rather than literals.
3. **Cards**: Use `.cardStyle()` for consistent container styling.
4. **Touch targets**: Apply `.minimumHitTarget()` to icon-only buttons.
5. **Errors**: Screens backed by a shared ViewModel surface errors with `.sharedErrorAlert`; use `ErrorView` for full-frame error states.
6. **Loading and empty states**: Show a skeleton shaped like the content (`SkeletonBlock` + `.shimmering()`) rather than a spinner, and use `EmptyStateView` for empty states. Keep `ProgressView` for in-place work: buttons, pull-to-refresh and loading the next page.
7. **Reuse first**: Check this list before writing a new component, and document new components here.
8. **Accessibility**: Hide decorative images (`CocktailImageView` already is), mark section titles `.isHeader`, give toggles one stable label plus `.isSelected`/`.isToggle`, and let rows wrap or stack at accessibility text sizes — see README "Accessibility" for the conventions and the automated audit.
9. **Copy**: Never hardcode user-facing text (labels, placeholders, accessibility labels/hints). Add it to the shared strings and use `L(\.key)` from `Utils/Strings.swift`; pass the resulting `String` to `Text`, `Button`, `Label`, `.navigationTitle`, `.accessibilityLabel`, … (see README "Localization"). Component parameters for copy are `String`, not `LocalizedStringKey`.
