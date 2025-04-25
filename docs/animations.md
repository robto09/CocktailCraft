# Animations and Transitions in CocktailCraft

This document outlines the animations and transitions implemented in the CocktailCraft app to enhance the user experience.

## Animation Types

### 1. UI Component Animations

#### Button Animations
- Scale animations on press/release for buttons
- Custom animated icon buttons with scale effects
- Animated floating action buttons

```kotlin
// Example usage
AnimatedButton(
    text = "Add to Cart",
    onClick = { /* action */ }
)

AnimatedIconButton(
    onClick = { /* action */ },
    icon = Icons.Default.Favorite,
    contentDescription = "Add to favorites"
)
```

#### List Item Animations
- Staggered entry animations for list items
- Scale animations on hover
- Coordinated animations for multiple elements
- Batched loading with smooth animations

```kotlin
// Example usage of animated item
AnimatedCocktailItem(
    cocktail = cocktail,
    onClick = { /* action */ },
    onAddToCart = { /* action */ },
    isFavorite = true,
    onToggleFavorite = { /* action */ },
    index = index // For staggered animation
)

// Example of batched loading implementation
val visibleItemsCount = remember { mutableStateOf(0) }

// Update visible items based on scroll position
LaunchedEffect(listState.firstVisibleItemIndex) {
    val targetVisible = minOf(
        items.size,
        listState.firstVisibleItemIndex + 12 // Current visible + 3 batches ahead
    )

    if (targetVisible > visibleItemsCount.value) {
        // Animate in batches of 3 items
        val batchSize = 3
        val currentBatch = visibleItemsCount.value / batchSize
        val targetBatch = targetVisible / batchSize

        for (batch in currentBatch until targetBatch) {
            val newCount = minOf((batch + 1) * batchSize, items.size)
            visibleItemsCount.value = newCount
            delay(100) // Small delay between batches
        }
    }
}
```

### 2. Loading State Animations

#### Shimmer Loading Effects
- Shimmer loading placeholders for cocktail items
- Shimmer loading placeholders for detail screens
- Custom shimmer effect that can be applied to any component

```kotlin
// Apply shimmer effect to any component
Box(
    modifier = Modifier
        .size(100.dp)
        .shimmerEffect()
)

// Use pre-built shimmer components
CocktailItemShimmer()
CocktailDetailShimmer()
```

### 3. Transition Animations

#### Screen Transitions
- Fade and slide animations for navigation
- Coordinated enter/exit animations

```kotlin
// Example from NavHost configuration
NavHost(
    navController = navController,
    startDestination = Screen.Home.route,
    enterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    },
    exitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    }
)
```

#### Theme Transitions
- Smooth color transitions when switching between light and dark modes
- Animated theme switch with custom effects

```kotlin
// Example usage
AnimatedCocktailBarTheme(
    darkTheme = isDarkMode,
    content = { /* app content */ }
)

AnimatedThemeSwitch(
    isDarkMode = isDarkMode,
    onToggle = { isDarkMode = !isDarkMode }
)
```

## Animation Utilities

The app includes a set of reusable animation utilities in the `AnimationUtils` class:

```kotlin
// Example utilities
val fadeInMedium = fadeIn(
    animationSpec = tween(
        durationMillis = ANIMATION_DURATION_MEDIUM,
        easing = LinearOutSlowInEasing
    )
)

val scaleInMedium = scaleIn(
    animationSpec = tween(
        durationMillis = ANIMATION_DURATION_MEDIUM,
        easing = LinearOutSlowInEasing
    ),
    initialScale = 0.9f
)

// Combined animations
val enterWithFadeAndScale: EnterTransition = fadeInMedium + scaleInMedium
```

## Optimized Scrolling Performance

The app implements several techniques to ensure smooth scrolling with animations:

### Batched Loading Mechanism

Instead of loading and animating all items at once, the app uses a batched loading approach:

1. **Visibility Tracking**: Maintains a count of how many items should be visible based on scroll position
2. **Batch Processing**: Loads items in small batches (3 at a time) with slight delays between batches
3. **Predictive Loading**: Preloads items that will soon be visible (3 batches ahead of current view)
4. **Coordinated Animations**: Items within the same batch animate together for a cohesive effect

### Animation Optimization Techniques

1. **Direct Animation Properties**: Uses `animateFloatAsState` for alpha and offset animations
2. **Simplified Rendering**: Optimizes rendering path for better performance during scrolling
3. **Efficient Composables**: Extracts and reuses content to minimize recomposition
4. **Minimal Layout Changes**: Avoids layout changes during animations to reduce jank

```kotlin
// Example of optimized item rendering with animations
if (isVisible) {
    // Calculate animation parameters based on batch
    val delayMillis = batchIndex * 100
    val animationDuration = 300

    // Animate alpha and offset for entry
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = delayMillis
        )
    )

    val animatedOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = delayMillis
        )
    )

    Box(
        modifier = Modifier
            .alpha(animatedAlpha)
            .offset(y = animatedOffset.dp)
    ) {
        // Render item content
    }
}
```

## Best Practices

1. **Performance Considerations**
   - Use lightweight animations for frequently updated components
   - Implement batched loading for list animations
   - Use direct animation properties instead of complex composables for better performance
   - Consider disabling animations on low-end devices

2. **Accessibility**
   - Provide options to reduce or disable animations for users with motion sensitivity
   - Ensure animations don't interfere with screen readers

3. **Consistency**
   - Use consistent animation durations and easing curves throughout the app
   - Maintain a cohesive animation style that matches the app's design language

## Implementation Guidelines

When adding new animations to the app:

1. Consider whether the animation enhances or distracts from the user experience
2. Use the provided animation utilities for consistency
3. Test animations on both high-end and low-end devices
4. Ensure animations work correctly in both light and dark modes
5. Add appropriate documentation for complex animations
