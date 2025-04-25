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

```kotlin
// Example usage
AnimatedCocktailItem(
    cocktail = cocktail,
    onClick = { /* action */ },
    onAddToCart = { /* action */ },
    isFavorite = true,
    onToggleFavorite = { /* action */ },
    index = index // For staggered animation
)
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

## Best Practices

1. **Performance Considerations**
   - Use lightweight animations for frequently updated components
   - Avoid animating large components or many items simultaneously
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
