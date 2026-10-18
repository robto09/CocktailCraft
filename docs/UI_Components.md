# UI Components Documentation

This document provides an overview of the reusable UI components in the CocktailCraft app, their purpose, and guidelines for using them in new features or refactoring existing code.

## Table of Contents

1. [Introduction](#introduction)
2. [Component Usage Guidelines](#component-usage-guidelines)
3. [Available Components](#available-components)
   - [EmptyStateComponent](#emptystatecomponent)
   - [DetailHeaderImage](#detailheaderimage)
   - [SectionHeader](#sectionheader)
   - [OrderSummaryCard](#ordersummarycard)
   - [ConfirmationDialog](#confirmationdialog)
   - [DetailInfoCard](#detailinfocard)
   - [RatingDisplay](#ratingdisplay)
   - [StatusIndicator](#statusindicator)
   - [SettingsCard](#settingscard)
   - [ProfileCard](#profilecard)
   - [AuthDialog](#authdialog)
   - [CocktailItem](#cocktailitem)
   - [AnimatedCocktailItem](#animatedcocktailitem)
   - [CartItemCard](#cartitemcard)
   - [FilterChip](#filterchip)
   - [FavoriteToggleButton](#favoritetogglebutton)
   - [RatingBar](#ratingbar)
   - [OptimizedImage](#optimizedimage)
   - [ErrorDialog and ErrorBanner](#errordialog-and-errorbanner)
   - [AnimatedButtons](#animatedbuttons)
   - [AdvancedSearchBottomSheet](#advancedsearchbottomsheet)
   - [CocktailSearchBar](#cocktailsearchbar)
   - [NetworkErrorStateDisplay](#networkerrordisplay)
   - [AnimatedCocktailList](#animatedcocktaillist)
   - [CategoryFilterRow](#categoryfilterrow)
   - [CocktailLoadingShimmer](#cocktailloadingshimmer)
   - [EndOfListMessage](#endoflistmessage)
4. [Animations](#animations)
5. [Refactoring Process](#refactoring-process)
6. [Best Practices](#best-practices)

## Introduction

The CocktailCraft app uses a component-based architecture for its UI, with reusable components that ensure consistency across the app. These components are located in the `androidApp/src/main/java/com/cocktailcraft/android/ui/components` directory.

Using these reusable components offers several benefits:
- **Consistency**: Ensures a uniform look and feel across the app
- **Maintainability**: Changes to components are reflected everywhere they're used
- **Efficiency**: Reduces development time for new features
- **Readability**: Makes the code more organized and easier to understand

## Component Usage Guidelines

When developing new features or refactoring existing code, follow these guidelines:

1. **Always check for existing components first** before creating new ones
2. **Use the most specific component** that meets your needs
3. **Extend existing components** when you need slight variations rather than creating entirely new ones
4. **Keep components focused** on a single responsibility
5. **Document any new components** you create in this guide

## Available Components

### EmptyStateComponent

**Purpose**: Displays a standardized empty state with an icon/image, title, message, and optional action button. There is no default illustration: pass the screen's icon (error states use `Icons.Filled.Warning`).

**Usage**:
```kotlin
EmptyStateComponent(
    title = stringResource(MR.strings.cart_empty_title),
    message = stringResource(MR.strings.cart_empty_message),
    actionButtonText = stringResource(MR.strings.cart_start_shopping),
    onActionButtonClick = onStartShopping,
    icon = Icons.Filled.ShoppingCart
)
```

**When to use**: For any screen that needs to display an empty state, such as an empty cart, empty favorites list, or no search results.

### DetailHeaderImage

**Purpose**: Displays a header image with a gradient overlay, commonly used in detail screens.

**Usage**:
```kotlin
DetailHeaderImage(
    imageUrl = imageUrl,
    contentDescription = cocktailData.name,
    height = 250,
    targetSize = 800 // Higher resolution for detail view
)
```

**When to use**: For detail screens that display a large header image with a gradient overlay.

### SectionHeader

**Purpose**: Provides a consistent header for sections with an optional action button.

**Usage**:
```kotlin
SectionHeader(
    title = "Your Cart",
    fontSize = 20,
    modifier = Modifier.padding(bottom = 8.dp),
    actionText = "Clear All",
    onActionClick = { /* Clear action */ }
)
```

**When to use**: For any section in a screen that needs a header, especially when you want to maintain consistent styling across the app.

### OrderSummaryCard

**Purpose**: Displays an order summary with subtotal, delivery fee, and total.

**Usage**:
```kotlin
OrderSummaryCard(
    subtotal = totalPrice,
    deliveryFee = 5.99,
    modifier = Modifier.padding(vertical = 8.dp),
    currencyFormatter = currencyFormatter
)
```

**When to use**: In cart or checkout screens where you need to display an order summary.

### ConfirmationDialog

**Purpose**: Displays a confirmation dialog with customizable title, message, and buttons.

**Usage**:
```kotlin
ConfirmationDialog(
    showDialog = showPlaceOrderDialog,
    title = "Confirm Order",
    message = "Are you sure you want to place this order?",
    confirmButtonText = "Confirm",
    dismissButtonText = "Cancel",
    onConfirm = { /* Confirm action */ },
    onDismiss = { /* Dismiss action */ }
)
```

**When to use**: When you need to confirm a user action, such as placing an order, deleting an item, or logging out.

### DetailInfoCard

**Purpose**: Displays detailed information in a card with a consistent style.

**Usage**:
```kotlin
DetailInfoCard(
    title = "How to Prepare",
    modifier = Modifier.padding(vertical = 8.dp),
    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
) {
    // Content goes here
    Text(
        text = instructionsText,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 24.sp
    )
}
```

**When to use**: For displaying detailed information in a card format, especially in detail screens.

### RatingDisplay

**Purpose**: Displays a rating with stars and review count.

**Usage**:
```kotlin
RatingDisplay(
    rating = 4.5f,
    reviewCount = 12,
    starSize = 16,
    useHalfStars = true
)
```

**When to use**: When you need to display a rating with stars and review count, such as in product listings or detail screens.

### StatusIndicator

**Purpose**: Displays a status with a colored dot or icon and text.

**Usage**:
```kotlin
StatusIndicator(
    status = order.status,
    isActive = order.status == "Completed",
    activeColor = Color(0xFF4CAF50),
    icon = Icons.Default.CheckCircle
)
```

**When to use**: When you need to display a status indicator, such as order status, network status, or any other state that can be active or inactive.

### SettingsCard

**Purpose**: Provides a consistent card layout for settings sections with a title and content.

**Usage**:
```kotlin
SettingsCard(
    title = "Account Settings",
    modifier = Modifier.padding(bottom = 16.dp)
) {
    // Content goes here
    SettingsItem(
        icon = Icons.Default.Person,
        title = "Edit Profile",
        onClick = { /* Handle edit profile */ }
    )

    SettingsItem(
        icon = Icons.Default.Lock,
        title = "Change Password",
        onClick = { /* Handle change password */ }
    )
}
```

**When to use**: For settings screens or any section that needs a consistent card layout with a title and content.

### ProfileCard

**Purpose**: Displays user profile information with an avatar, name, email, and sign-in/sign-up buttons if not signed in.

**Usage**:
```kotlin
ProfileCard(
    userName = userName,
    userEmail = userEmail,
    isSignedIn = isSignedIn,
    onSignIn = { showSignInDialog = true },
    onSignUp = { showSignUpDialog = true }
)
```

**When to use**: For profile screens to display user information and authentication options.

### AuthDialog

**Purpose**: Provides a dialog for user authentication (sign-in or sign-up).

**Usage**:
```kotlin
AuthDialog(
    type = AuthDialogType.SIGN_IN,
    onDismiss = { showSignInDialog = false },
    onSubmit = { name, email, password ->
        profileViewModel.signIn(email, password)
        showSignInDialog = false
    }
)
```

**When to use**: When you need to display a sign-in or sign-up dialog for user authentication.

### CocktailItem

**Purpose**: Displays a cocktail item in a list with image, name, price, and actions.

**Usage**:
```kotlin
CocktailItem(
    cocktail = cocktail,
    onClick = { /* Navigate to detail */ },
    onAddToCart = { cocktail ->
        cartViewModel.addToCart(cocktail)
        onAddToCart(cocktail)
    },
    isFavorite = true,
    onToggleFavorite = { cocktail ->
        favoritesViewModel.toggleFavorite(cocktail)
    }
)
```

**When to use**: In lists of cocktails, such as in the home screen or search results.

### AnimatedCocktailItem

**Purpose**: An enhanced version of CocktailItem with animations.

**Usage**:
```kotlin
AnimatedCocktailItem(
    cocktail = cocktail,
    onClick = { /* Navigate to detail */ },
    onAddToCart = { cocktail ->
        cartViewModel.addToCart(cocktail)
        onAddToCart(cocktail)
    },
    isFavorite = true,
    onToggleFavorite = { cocktail ->
        favoritesViewModel.toggleFavorite(cocktail)
    }
)
```

**When to use**: When you want to add animations to cocktail items in a list for a more engaging user experience.

### CartItemCard

**Purpose**: Displays a cart item with image, name, price, quantity controls, and actions.

**Usage**:
```kotlin
CartItemCard(
    item = item,
    onIncreaseQuantity = { viewModel.updateQuantity(item.cocktail.id, item.quantity + 1) },
    onDecreaseQuantity = {
        if (item.quantity > 1) {
            viewModel.updateQuantity(item.cocktail.id, item.quantity - 1)
        } else {
            viewModel.removeFromCart(item.cocktail.id)
        }
    },
    onRemove = { viewModel.removeFromCart(item.cocktail.id) },
    isFavorite = favorites.any { it.id == item.cocktail.id },
    onToggleFavorite = { favoritesViewModel.toggleFavorite(item.cocktail) }
)
```

**When to use**: In the cart screen to display items in the user's cart.

### FilterChip

**Purpose**: Displays a selectable chip for filtering options.

**Usage**:
```kotlin
FilterChip(
    selected = selectedComplexity == complexity,
    onClick = { onComplexitySelected(complexity) },
    label = complexity.toString()
)
```

**When to use**: For filtering options in search or browse screens.

### FavoriteToggleButton

**Purpose**: The heart toggle on cocktail cards, cart rows and the detail screen. An `IconToggleButton` with one stable "Favorite" label, so TalkBack reads (and announces changes to) its on/off state instead of a label that flips between "Add to" and "Remove from" favorites. Keeps the 48dp touch target.

**Usage**:
```kotlin
FavoriteToggleButton(
    isFavorite = isFavorite,
    onToggle = { onToggleFavorite(cocktail) },
    iconSize = 24.dp
)
```

**When to use**: Any favorite on/off control — don't build a heart from a plain `IconButton`.

### RatingBar

**Purpose**: Displays a row of stars representing a rating.

**Usage**:
```kotlin
RatingBar(
    rating = 4.5f,
    stars = 5,
    starsColor = CocktailCraftTheme.extendedColors.rating,
    useHalfStars = true
)
```

**When to use**: When you need to display a rating with stars, such as in product listings or detail screens.

### OptimizedImage

**Purpose**: Loads and displays images with optimization for performance.

**Usage**:
```kotlin
OptimizedImage(
    url = imageUrl,
    contentDescription = cocktailData.name,
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop,
    targetSize = 800 // Higher resolution for detail view
)
```

**When to use**: Whenever you need to display an image from a URL, especially when performance is a concern.

### ErrorDialog and ErrorBanner

**Purpose**: Displays error messages in a dialog or banner format.

**Usage**:
```kotlin
ErrorDialog(
    error = error,
    onDismiss = { /* Dismiss action */ },
    onRetry = { /* Retry action */ }
)

ErrorBanner(
    error = error,
    onDismiss = { /* Dismiss action */ },
    onAction = { /* Action */ }
)
```

**When to use**: When you need to display error messages to the user, either in a modal dialog or a non-modal banner. The error's title, message and action label are shared `StringDesc`s; the components resolve them with `resolve()` (`util/SharedStrings.kt`).

### AnimatedButtons

**Purpose**: Provides buttons with scale animations when pressed.

**Usage**:
```kotlin
AnimatedButton(
    onClick = { /* Action */ },
    modifier = Modifier
) {
    Text("Click Me")
}

AnimatedTextButton(
    text = "Click Me",
    onClick = { /* Action */ }
)
```

**When to use**: When you want to add animations to buttons for a more engaging user experience.

### AdvancedSearchBottomSheet

**Purpose**: Modal bottom sheet for the advanced search filters, mirroring the iOS sheet. Apply and Clear both close the sheet.

**Usage**:
```kotlin
if (isAdvancedSearchActive) {
    AdvancedSearchBottomSheet(
        currentFilters = searchFilters,
        categories = categories,
        ingredients = ingredients,
        onApplyFilters = { filters ->
            scope.launch { viewModel.applyFilters(filters) }
        },
        onClearFilters = {
            viewModel.clearSearchFilters()
        },
        onDismiss = {
            viewModel.toggleAdvancedSearchMode(false)
        }
    )
}
```

**When to use**: When the user opens advanced search from the search bar; the sheet dismisses on Apply, Clear, or swipe-down.

### CocktailSearchBar

**Purpose**: Provides a search bar with an advanced search button for filtering cocktails.

**Usage**:
```kotlin
CocktailSearchBar(
    searchQuery = searchQuery,
    isAdvancedSearchActive = isAdvancedSearchActive,
    hasActiveFilters = searchFilters.hasActiveFilters(),
    onSearchQueryChange = { viewModel.searchCocktails(it) },
    onClearSearch = { viewModel.toggleSearchMode(false) },
    onToggleAdvancedSearch = { viewModel.toggleAdvancedSearchMode(!isAdvancedSearchActive) },
    onShowAdvancedSearchDialog = { showAdvancedSearch = true }
)
```

**When to use**: When you need a search bar with advanced filtering capabilities, such as in the home screen or any screen with searchable content.

### NetworkErrorStateDisplay

**Purpose**: Full-screen error state for load failures, shown only when there is no content to render. With no connection it shows the no-internet state with a Retry action; with a connection it shows the load error itself.

**Usage**:
```kotlin
NetworkErrorStateDisplay(
    errorMessage = errorMessage,
    isNetworkAvailable = isNetworkAvailable,
    onRetry = { viewModel.retry() }
)
```

**When to use**: When a screen that depends on network content has nothing to show and needs a retry affordance. When cached content exists, render it instead — the global offline banner already communicates connectivity.

### AnimatedCocktailList

**Purpose**: Displays a list of cocktails with animations, pagination, and end-of-list messaging.

**Usage**:
```kotlin
AnimatedCocktailList(
    cocktails = cocktails,
    isSearchActive = isSearchActive,
    selectedCategory = selectedCategory,
    isLoadingMore = isLoadingMore,
    hasMoreData = hasMoreData,
    favorites = favorites,
    onCocktailClick = onCocktailClick,
    onAddToCart = onAddToCart,
    onToggleFavorite = { cocktailToToggle ->
        favoritesViewModel.toggleFavorite(cocktailToToggle)
    },
    onLoadMore = {
        viewModel.loadMoreCocktails()
    }
)
```

**When to use**: When you need to display a list of cocktails with animations and pagination, such as in the home screen, favorites screen, or search results.

### CategoryFilterRow

**Purpose**: Displays a horizontal row of category filter chips for filtering content.

**Usage**:
```kotlin
CategoryFilterRow(
    categories = categories,
    selectedCategory = selectedCategory,
    onCategorySelected = onCategorySelected
)
```

**When to use**: When you need to display a horizontal list of categories for filtering content, such as in the home screen or browse screen.

### CocktailLoadingShimmer

**Purpose**: Displays a shimmer loading effect for cocktails while content is loading.

**Usage**:
```kotlin
CocktailLoadingShimmer()                    // Home: section-title bar + rows
CocktailLoadingShimmer(showHeader = false)  // Favorites: rows only
```

**When to use**: When you need to display a loading state for cocktails, providing a better user experience than a simple spinner.

### EndOfListMessage

**Purpose**: Displays an animated message when the user reaches the end of a list.

**Usage**:
```kotlin
EndOfListMessage(
    visible = !hasMoreData && !isSearchActive && cocktails.isNotEmpty(),
    message = "You've reached the end of the list"
)
```

**When to use**: When you need to indicate to the user that they have reached the end of a paginated list.

## Animations

The animation system consists of shared utilities in `androidApp/src/main/java/com/cocktailcraft/android/ui/animation/AnimationUtils.kt` and the animated components in `ui/components/` described above.

### AnimationUtils

`AnimationUtils` is an object providing reusable animation building blocks:

- **Duration constants**: `ANIMATION_DURATION_SHORT` (150ms), `ANIMATION_DURATION_MEDIUM` (300ms), `ANIMATION_DURATION_LONG` (500ms)
- **Spring constants**: stiffness (`SPRING_STIFFNESS_LOW/MEDIUM/HIGH`) and damping (`SPRING_DAMPENING_*`) presets
- **Prebuilt transitions**: `fadeInMedium`/`fadeOutMedium`, `scaleInMedium`/`scaleOutMedium`, `bounceScaleIn`, slide-in/out variants, and combined transitions such as `enterWithFadeAndScale`

```kotlin
// Combined enter transition: fade + scale with medium duration
AnimatedVisibility(
    visible = visible,
    enter = AnimationUtils.enterWithFadeAndScale,
    exit = AnimationUtils.exitWithFadeAndScale
) { /* content */ }
```

### Button Animations

`AnimatedButtons.kt` provides press-scale button variants driven by `interactionSource` and spring animations:

- **`AnimatedButton`**: a `Button` that scales to 0.95 while pressed. Takes a content lambda (there is no `text` parameter):

```kotlin
AnimatedButton(onClick = { /* action */ }) {
    Text("Add to Cart")
}
```

- **`AnimatedTextButton(text, onClick)`**: convenience wrapper around `AnimatedButton` for a plain text label
- **`AnimatedIconButton`**: icon button with press-scale and optional haptic feedback
- **`AnimatedFloatingActionButton`**: FAB with press-scale

### Theming and Colors

`AnimatedCocktailBarTheme` (`ui/theme/AnimatedTheme.kt`) is the app's single
theme. Its Material 3 color scheme is built in `ui/theme/Theme.kt`
(`cocktailColorScheme`) from the shared `ColorTokens`
(`shared/.../designsystem/DesignTokens.kt`) and honours the accent color
(primary) and high-contrast setting (stronger secondary text) from the shared
theme state. Android has no in-app controls for these (the Profile screen has
no Appearance section); iOS sets them in its Settings sheet. Read
colors only through the theme — there is no global color object (AN-5):

- **`MaterialTheme.colorScheme`** for every role Material defines
- **`CocktailCraftTheme.extendedColors`** for brand/semantic colors Material
  has no role for
- **`CocktailCraftTheme.isDarkTheme`** for theme-specific assets
  (`ThemeAssets`)

| Design token | Read it as |
|---|---|
| primary (follows accent) | `colorScheme.primary` |
| secondary (gold) | `colorScheme.secondary` |
| chipBackground (brown) | `colorScheme.tertiary` |
| background / surface | `colorScheme.background` / `surface` |
| textPrimary | `colorScheme.onSurface` |
| textSecondary (high-contrast aware) | `colorScheme.onSurfaceVariant` |
| gray (high-contrast aware in light theme) | `colorScheme.outline` |
| error | `colorScheme.error` |
| success / warning | `extendedColors.success` / `warning` |
| rating stars | `extendedColors.rating` |
| lightGray (dividers, unfocused borders, placeholders) | `extendedColors.neutralSubtle` |

`lightGray` is deliberately not `outlineVariant`: that role feeds Material
defaults (chip and outlined-button borders, default dividers) and remapping
it would restyle them. Code outside composition must not read theme colors —
pass the color in instead. Home-screen widgets are Glance and use their own
`GlanceTheme` color providers (`widget/WidgetTheme.kt`).

Android dynamic color (Material You) is supported by the theme's
`dynamicColor` parameter but stays **off** by default: the app has a brand
palette and a shared accent setting that a wallpaper-derived scheme would
override. Extended colors stay on-brand either way.

```kotlin
Text("In stock", color = CocktailCraftTheme.extendedColors.success)
Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { /* ... */ }
```

### Theme Transitions

- **`AnimatedCocktailBarTheme(darkTheme, accentColor, highContrast, ...)`**: animates the theme-dependent palette roles (and the rating gold) when switching between light and dark modes or accents; Reduce Motion makes the switch instant
- **`AnimatedThemeSwitch(isDarkMode, onToggle)`**: custom switch with animated track/thumb colors, spring-based thumb position, and an animated light/dark icon

```kotlin
AnimatedCocktailBarTheme(darkTheme = isDarkMode) { /* app content */ }

AnimatedThemeSwitch(
    isDarkMode = isDarkMode,
    onToggle = { themeViewModel.toggleDarkMode() }
)
```

### Shimmer Loading

Skeletons use RevenueCat's [placeholder-compose](https://github.com/RevenueCat/placeholder-compose) (`Modifier.placeholder`). `AnimatedCocktailBarTheme` provides the defaults for every placeholder in the app: the `neutralSubtle` fill with a surface-colored shimmer, and under Reduce Motion static blocks with no shimmer or fade. Call sites pass only a shape.

Wrap a loading container in `PlaceholderSurface` so every block beneath it shares one animation clock and shimmers in unison. `OptimizedImage` also shimmers the image area while a photo loads.

```kotlin
// Any skeleton block
Box(modifier = Modifier.size(100.dp).placeholder(shape = RoundedCornerShape(8.dp)))

// Prebuilt placeholders (ShimmerLoading.kt / CocktailLoadingShimmer.kt)
CocktailLoadingShimmer()    // Home / Favorites list
CocktailDetailShimmer()     // cocktail detail screen
CartLoadingShimmer()        // cart rows (CartItemShimmer)
OrderListLoadingShimmer()   // order cards (OrderCardShimmer)
```

### List Animations

`AnimatedCocktailList` delegates item entrance/exit/placement animation to Compose's built-in `LazyItemScope.animateItem()` modifier on identity-keyed items (`key = "cocktail_${cocktail.id}"`). New items fade in and position changes animate when the list shifts. A previous hand-rolled batching/stagger system was removed — it never produced a visible animation (see the comment in `AnimatedCocktailList.kt`).

`AnimatedCocktailItem` adds a spring-based hover-scale effect on the card itself; there is no per-index stagger.

Screen-to-screen navigation uses the `NavHost` defaults — no custom enter/exit transitions are wired.

## Refactoring Process

When refactoring UI code to use reusable components, follow these steps:

1. **Identify patterns**: Look for repeated UI patterns across different screens
2. **Extract components**: Create reusable components for these patterns
3. **Parameterize**: Make components flexible with parameters for variations
4. **Replace instances**: Update all occurrences to use the new components
5. **Test thoroughly**: Ensure the refactored code works as expected

Recent refactoring in the CocktailCraft app focused on:

**First Phase:**
- Creating standardized empty states
- Extracting header images with gradient overlays
- Standardizing section headers
- Creating reusable cards for different types of information
- Implementing consistent loading states

**Second Phase:**
- Creating status indicators for order status and network status
- Extracting settings cards for profile and settings screens
- Extracting profile cards for user information
- Implementing authentication dialogs for sign-in and sign-up

**Third Phase:**
- Creating a reusable search bar with advanced search functionality
- Implementing a network error state display
- Extracting an animated cocktail list with pagination
- Creating a category filter row for content filtering
- Implementing a shimmer loading effect for cocktails
- Creating an end-of-list indicator for pagination
- Extracting filter options loading logic into a utility

## Best Practices

1. **Component Naming**: Use clear, descriptive names that indicate the component's purpose
2. **Documentation**: Add KDoc comments to explain parameters and usage
3. **Default Values**: Provide sensible defaults for optional parameters
4. **Theming**: Read colors from `MaterialTheme.colorScheme` / `CocktailCraftTheme.extendedColors` (never hardcode `Color.White` for surfaces) and use `Spacing` for dimensions
5. **Accessibility**: Ensure components are accessible with proper content descriptions — see README "Accessibility" for the conventions and the automated checks
6. **Composition**: Prefer composition over inheritance for component reuse
7. **Testing**: Write tests for components to ensure they work as expected
8. **Strings**: Never hardcode user-facing copy (Compose literals are invisible to the `HardcodedText` lint check). Add it to the shared strings and read it with `stringResource(MR.strings.key)` / `pluralStringResource(MR.plurals.key, …)` from `util/SharedStrings.kt` — see README "Localization"

By following these guidelines and using the available components, you can maintain a consistent, maintainable, and efficient UI codebase for the CocktailCraft app.
