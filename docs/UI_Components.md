# UI Components Documentation

This document provides an overview of the reusable UI components in the CocktailCraft app, their purpose, and guidelines for using them in new features or refactoring existing code.

## Table of Contents

1. [Introduction](#introduction)
2. [Component Usage Guidelines](#component-usage-guidelines)
3. [Available Components](#available-components)
   - [EmptyStateComponent](#emptystatecomponent)
   - [DetailHeaderImage](#detailheaderimage)
   - [SectionHeader](#sectionheader)
   - [LoadingStateComponent](#loadingstatecomponent)
   - [OrderSummaryCard](#ordersummarycard)
   - [ConfirmationDialog](#confirmationdialog)
   - [DetailInfoCard](#detailinfocard)
   - [RatingDisplay](#ratingdisplay)
   - [CocktailItem](#cocktailitem)
   - [AnimatedCocktailItem](#animatedcocktailitem)
   - [CartItemCard](#cartitemcard)
   - [FilterChip](#filterchip)
   - [RatingBar](#ratingbar)
   - [OptimizedImage](#optimizedimage)
   - [ErrorDialog and ErrorBanner](#errordialog-and-errorbanner)
   - [AnimatedButtons](#animatedbuttons)
4. [Refactoring Process](#refactoring-process)
5. [Best Practices](#best-practices)

## Introduction

The CocktailCraft app uses a component-based architecture for its UI, with reusable components that ensure consistency across the app. These components are located in the `androidApp/src/main/java/com/cocktailcraft/ui/components` directory.

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

**Purpose**: Displays a standardized empty state with an icon/image, title, message, and optional action button.

**Usage**:
```kotlin
EmptyStateComponent(
    title = "Your cart is empty",
    message = "Add some cocktails to your cart and they will appear here",
    actionButtonText = "Start Shopping",
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

### LoadingStateComponent

**Purpose**: Displays a standardized loading indicator with animation.

**Usage**:
```kotlin
LoadingStateComponent(
    isLoading = isLoading,
    paddingValues = paddingValues
)
```

**When to use**: Whenever you need to show a loading state while data is being fetched or processed.

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
    backgroundColor = AppColors.Surface.copy(alpha = 0.8f)
) {
    // Content goes here
    Text(
        text = instructionsText,
        fontSize = 15.sp,
        color = AppColors.TextPrimary,
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
    },
    index = index
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

### RatingBar

**Purpose**: Displays a row of stars representing a rating.

**Usage**:
```kotlin
RatingBar(
    rating = 4.5f,
    stars = 5,
    starsColor = AppColors.Secondary,
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

**When to use**: When you need to display error messages to the user, either in a modal dialog or a non-modal banner.

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

## Refactoring Process

When refactoring UI code to use reusable components, follow these steps:

1. **Identify patterns**: Look for repeated UI patterns across different screens
2. **Extract components**: Create reusable components for these patterns
3. **Parameterize**: Make components flexible with parameters for variations
4. **Replace instances**: Update all occurrences to use the new components
5. **Test thoroughly**: Ensure the refactored code works as expected

Recent refactoring in the CocktailCraft app focused on:
- Creating standardized empty states
- Extracting header images with gradient overlays
- Standardizing section headers
- Creating reusable cards for different types of information
- Implementing consistent loading states

## Best Practices

1. **Component Naming**: Use clear, descriptive names that indicate the component's purpose
2. **Documentation**: Add KDoc comments to explain parameters and usage
3. **Default Values**: Provide sensible defaults for optional parameters
4. **Theming**: Use app theme colors and dimensions for consistency
5. **Accessibility**: Ensure components are accessible with proper content descriptions
6. **Composition**: Prefer composition over inheritance for component reuse
7. **Testing**: Write tests for components to ensure they work as expected

By following these guidelines and using the available components, you can maintain a consistent, maintainable, and efficient UI codebase for the CocktailCraft app.
