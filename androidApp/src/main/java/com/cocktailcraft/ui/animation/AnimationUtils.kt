package com.cocktailcraft.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

/**
 * Utility object containing reusable animations for the app
 */
object AnimationUtils {
    // Duration constants
    const val ANIMATION_DURATION_SHORT = 150
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 500

    // Spring constants
    val SPRING_STIFFNESS_LOW = Spring.StiffnessLow
    val SPRING_STIFFNESS_MEDIUM = Spring.StiffnessMedium
    val SPRING_STIFFNESS_HIGH = Spring.StiffnessHigh

    val SPRING_DAMPENING_LOW_BOUNCY = Spring.DampingRatioLowBouncy
    val SPRING_DAMPENING_MEDIUM_BOUNCY = Spring.DampingRatioMediumBouncy
    val SPRING_DAMPENING_HIGH_BOUNCY = Spring.DampingRatioHighBouncy
    val SPRING_DAMPENING_NO_BOUNCE = Spring.DampingRatioNoBouncy

    // Fade animations
    val fadeInMedium = fadeIn(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MEDIUM,
            easing = LinearOutSlowInEasing
        )
    )

    val fadeOutMedium = fadeOut(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        )
    )

    // Scale animations
    val scaleInMedium = scaleIn(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MEDIUM,
            easing = LinearOutSlowInEasing
        ),
        initialScale = 0.9f
    )

    val scaleOutMedium = scaleOut(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        ),
        targetScale = 0.9f
    )

    // Bounce scale animation
    val bounceScaleIn = scaleIn(
        animationSpec = spring(
            dampingRatio = SPRING_DAMPENING_MEDIUM_BOUNCY,
            stiffness = SPRING_STIFFNESS_MEDIUM
        ),
        initialScale = 0.8f
    )

    // Slide animations
    val slideInFromBottom = slideInVertically(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MEDIUM,
            easing = LinearOutSlowInEasing
        ),
        initialOffsetY = { it }
    )

    val slideOutToBottom = slideOutVertically(
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MEDIUM,
            easing = FastOutSlowInEasing
        ),
        targetOffsetY = { it }
    )

    // Combined animations
    val enterWithFadeAndScale: EnterTransition = fadeInMedium + scaleInMedium
    val exitWithFadeAndScale: ExitTransition = fadeOutMedium + scaleOutMedium

    val enterWithFadeAndSlideFromBottom: EnterTransition = fadeInMedium + slideInFromBottom
    val exitWithFadeAndSlideToBottom: ExitTransition = fadeOutMedium + slideOutToBottom

    // Content transform for animated content
    val fadeAndSlideContentTransform: ContentTransform = fadeInMedium.togetherWith(fadeOutMedium)
    val scaleAndFadeContentTransform: ContentTransform = (fadeInMedium + scaleInMedium).togetherWith(fadeOutMedium + scaleOutMedium)

    // Navigation transitions
    fun slideIntoContainerFromRight(): EnterTransition {
        return slideInHorizontally(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MEDIUM,
                easing = LinearOutSlowInEasing
            ),
            initialOffsetX = { fullWidth -> fullWidth }
        ) + fadeIn(
            animationSpec = tween(ANIMATION_DURATION_MEDIUM)
        )
    }

    fun slideOutOfContainerToLeft(): ExitTransition {
        return slideOutHorizontally(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MEDIUM,
                easing = FastOutSlowInEasing
            ),
            targetOffsetX = { fullWidth -> -fullWidth }
        ) + fadeOut(
            animationSpec = tween(ANIMATION_DURATION_MEDIUM)
        )
    }

    fun slideIntoContainerFromLeft(): EnterTransition {
        return slideInHorizontally(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MEDIUM,
                easing = LinearOutSlowInEasing
            ),
            initialOffsetX = { fullWidth -> -fullWidth }
        ) + fadeIn(
            animationSpec = tween(ANIMATION_DURATION_MEDIUM)
        )
    }

    fun slideOutOfContainerToRight(): ExitTransition {
        return slideOutHorizontally(
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MEDIUM,
                easing = FastOutSlowInEasing
            ),
            targetOffsetX = { fullWidth -> fullWidth }
        ) + fadeOut(
            animationSpec = tween(ANIMATION_DURATION_MEDIUM)
        )
    }

    // Function to create staggered animation delay based on index
    fun calculateStaggeredDelay(index: Int, baseDelay: Int = 50): Int {
        return index * baseDelay
    }
}
