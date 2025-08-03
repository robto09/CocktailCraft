# iOS UI Components Documentation

This document provides comprehensive documentation for all reusable UI components in the CocktailCraft iOS app. These components follow the app's design system and provide consistent styling and behavior across all screens.

## Table of Contents

1. [Design System Components](#design-system-components)
2. [Cocktail-Specific Components](#cocktail-specific-components)
3. [Cart Components](#cart-components)
4. [Profile Components](#profile-components)
5. [Order Components](#order-components)
6. [Offline Mode Components](#offline-mode-components)
7. [Common UI Components](#common-ui-components)
8. [Usage Guidelines](#usage-guidelines)

## Design System Components

### AppColors
Provides consistent color theming across the app with support for both light and dark modes.

```swift
// Usage examples
AppColors.primary(isDarkMode: isDarkMode)
AppColors.textPrimary(isDarkMode: isDarkMode)
AppColors.background(isDarkMode: isDarkMode)
```

### AppTheme
Contains typography, spacing, corner radius, shadows, and animation constants.

```swift
// Typography
AppTheme.Typography.headline
AppTheme.Typography.body

// Spacing
AppTheme.Spacing.lg
AppTheme.Spacing.md

// Corner Radius
AppTheme.CornerRadius.card
AppTheme.CornerRadius.button
```

## Cocktail-Specific Components

### CocktailImageView
Displays cocktail images with loading states and customizable dimensions.

```swift
CocktailImageView(
    imageUrl: cocktail.imageUrl,
    height: 300,
    cornerRadius: 12
)
```

**Properties:**
- `imageUrl: String?` - URL of the cocktail image
- `height: CGFloat` - Height of the image (default: 300)
- `cornerRadius: CGFloat` - Corner radius (default: 0)

### CocktailInfoCard
Displays cocktail information including name, category, price, and instructions.

```swift
CocktailInfoCard(cocktail: cocktail)
```

**Properties:**
- `cocktail: Cocktail` - The cocktail object containing all information

### IngredientsListView
Shows a formatted list of cocktail ingredients with measurements.

```swift
IngredientsListView(ingredients: cocktail.ingredients)
```

**Properties:**
- `ingredients: [CocktailIngredient]` - Array of cocktail ingredients

### ActionButtonsRow
Provides favorite and add-to-cart action buttons for cocktails.

```swift
ActionButtonsRow(
    isFavorite: isFavorite,
    onFavoriteToggle: { toggleFavorite() },
    onAddToCart: { addToCart() }
)
```

**Properties:**
- `isFavorite: Bool` - Whether the cocktail is favorited
- `onFavoriteToggle: () -> Void` - Callback for favorite toggle
- `onAddToCart: () -> Void` - Callback for add to cart action

## Cart Components

### CartItemCard
Displays a cart item with image, details, quantity controls, and remove option.

```swift
CartItemCard(
    item: cartItem,
    onIncrementQuantity: { /* increment logic */ },
    onDecrementQuantity: { /* decrement logic */ },
    onRemoveFromCart: { /* remove logic */ },
    onToggleFavorite: { /* favorite logic */ }
)
```

**Properties:**
- `item: CartItem` - The cart item to display
- `onIncrementQuantity: () -> Void` - Callback to increment quantity
- `onDecrementQuantity: () -> Void` - Callback to decrement quantity
- `onRemoveFromCart: () -> Void` - Callback to remove item
- `onToggleFavorite: (() -> Void)?` - Optional favorite toggle callback

### CartSummaryCard
Shows order summary with subtotal, delivery fee, and total.

```swift
CartSummaryCard(
    subtotal: 25.98,
    deliveryFee: 3.99,
    total: 29.97
)
```

**Properties:**
- `subtotal: Double` - Subtotal amount
- `deliveryFee: Double` - Delivery fee amount
- `total: Double` - Total amount

### CheckoutButton
Primary action button for checkout with loading states.

```swift
CheckoutButton(
    title: "Place Order",
    isEnabled: true,
    isLoading: false,
    action: { /* checkout logic */ }
)
```

**Properties:**
- `title: String` - Button text (default: "Place Order")
- `isEnabled: Bool` - Whether button is enabled (default: true)
- `isLoading: Bool` - Whether to show loading state (default: false)
- `action: () -> Void` - Callback for button tap

## Profile Components

### ProfileHeaderCard
Displays user profile information with avatar and authentication buttons.

```swift
ProfileHeaderCard(
    userName: "John Doe",
    userEmail: "john@example.com",
    isLoggedIn: true,
    onSignIn: { /* sign in logic */ },
    onSignUp: { /* sign up logic */ }
)
```

**Properties:**
- `userName: String` - User's display name
- `userEmail: String` - User's email address
- `isLoggedIn: Bool` - Whether user is authenticated
- `onSignIn: () -> Void` - Callback for sign in action
- `onSignUp: () -> Void` - Callback for sign up action

### SettingsCard
Displays a group of settings options with consistent styling.

```swift
SettingsCard(
    title: "Account Settings",
    items: [
        .action(ActionSettingsItem(icon: "person", title: "Edit Profile", action: {})),
        .toggle(ToggleSettingsItem(title: "Dark Mode", subtitle: "On", icon: "moon.fill", isChecked: true, onToggle: {}))
    ]
)
```

**Properties:**
- `title: String` - Card title
- `items: [SettingsItem]` - Array of settings items (actions or toggles)

### MenuItemRow
Individual menu item with icon, title, and optional chevron.

```swift
MenuItemRow(
    icon: "person",
    title: "Edit Profile",
    action: { /* action logic */ },
    textColor: .primary,
    showChevron: true
)
```

**Properties:**
- `icon: String` - SF Symbol icon name
- `title: String` - Menu item title
- `action: () -> Void` - Callback for item tap
- `textColor: Color` - Text color (default: .primary)
- `showChevron: Bool` - Whether to show chevron (default: true)

## Order Components

### OrderCard
Displays order information including items, status, and total.

```swift
OrderCard(order: order)
```

**Properties:**
- `order: Order` - The order object to display

### OrderStatusBadge
Shows order status with appropriate color coding.

```swift
OrderStatusBadge(status: "Processing")
```

**Properties:**
- `status: String` - Order status text

## Offline Mode Components

### NetworkStatusCard
Displays current network connectivity status.

```swift
NetworkStatusCard(isNetworkAvailable: true)
```

**Properties:**
- `isNetworkAvailable: Bool` - Whether network is available

### CacheInfoCard
Shows cache information and statistics.

```swift
CacheInfoCard(
    cacheSize: "25 cocktails",
    lastSyncTime: "2 hours ago",
    networkStatus: "Connected",
    offlineModeStatus: "Enabled",
    networkStatusColor: .green,
    offlineModeColor: .blue
)
```

### ToggleCard
Card with toggle functionality and optional recommendations.

```swift
ToggleCard(
    title: "Offline Mode Settings",
    toggleTitle: "Enable Offline Mode",
    toggleSubtitle: "Access cached cocktails when offline",
    isToggled: true,
    onToggle: { /* toggle logic */ },
    showRecommendation: true,
    recommendationText: "Recommended when network is unavailable"
)
```

## Common UI Components

### LoadingStateView
Consistent loading indicator with customizable message.

```swift
LoadingStateView(message: "Loading cocktail details...")
```

### EmptyStateView
Standard empty state with icon, title, and message.

```swift
EmptyStateView(
    icon: "heart",
    title: "No favorites yet",
    message: "Start adding cocktails to your favorites"
)
```

## Usage Guidelines

### 1. Consistent Theming
Always use `@Environment(\.isDarkMode)` to access the current theme state and apply appropriate colors using `AppColors` functions.

### 2. Spacing and Layout
Use `AppTheme.Spacing` constants for consistent spacing throughout the app.

### 3. Typography
Apply `AppTheme.Typography` styles for consistent text appearance.

### 4. Card Styling
Use the `.cardStyle()` modifier for consistent card appearance across components.

### 5. Animation
Use `AppTheme.Animation` constants for consistent animation timing.

### 6. Accessibility
All components include proper accessibility labels and support for dynamic type sizing.

### 7. Preview Support
Each component includes SwiftUI previews for easy development and testing.

## Best Practices

1. **Reusability**: Always prefer using existing components over creating new ones
2. **Consistency**: Follow the established design patterns and color schemes
3. **Performance**: Use lazy loading where appropriate (LazyVStack, LazyHStack)
4. **Accessibility**: Ensure all interactive elements have proper accessibility support
5. **Testing**: Include preview configurations for different states and themes
6. **Documentation**: Keep component documentation up to date with any changes

## Component Dependencies

Most components depend on:
- `shared` module for data models
- `AppColors` for theming
- `AppTheme` for styling constants
- SwiftUI environment values for theme state

Make sure these dependencies are properly imported when using the components.
