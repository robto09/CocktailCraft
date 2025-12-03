package com.cocktailcraft.android.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

/**
 * Utility object for haptic feedback in Compose UI.
 * 
 * Provides consistent haptic feedback across the app with different
 * intensity levels for various user interactions.
 */
object HapticUtils {
    
    /**
     * Performs a light click haptic feedback.
     * Best for: toggle actions, button presses, selections
     */
    fun View.performClickHaptic() {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    
    /**
     * Performs a medium/confirm haptic feedback.
     * Best for: successful actions, adding to favorites
     */
    fun View.performConfirmHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    /**
     * Performs a reject/error haptic feedback.
     * Best for: errors, failed actions, removing from favorites
     */
    fun View.performRejectHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
    
    /**
     * Performs a long press haptic feedback.
     * Best for: heavy confirmation actions
     */
    fun View.performLongPressHaptic() {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

/**
 * A composable-friendly haptic feedback handler.
 * 
 * Usage:
 * ```
 * val haptic = rememberHapticHandler()
 * 
 * IconButton(onClick = {
 *     haptic.performToggleFavorite(isCurrentlyFavorite)
 *     onToggleFavorite()
 * })
 * ```
 */
class HapticHandler(
    private val view: View,
    private val hapticFeedback: HapticFeedback
) {
    /**
     * Performs haptic feedback for favorite toggle action.
     * Uses confirm feedback when adding, reject when removing.
     * 
     * @param isCurrentlyFavorite The current favorite state before toggle
     */
    fun performToggleFavorite(isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            // Removing from favorites - lighter feedback
            with(HapticUtils) { view.performRejectHaptic() }
        } else {
            // Adding to favorites - satisfying confirmation feedback
            with(HapticUtils) { view.performConfirmHaptic() }
        }
    }
    
    /**
     * Performs a light click haptic feedback.
     */
    fun performClick() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Performs a long press haptic feedback.
     */
    fun performLongPress() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    /**
     * Performs a confirm haptic feedback (for successful actions).
     */
    fun performConfirm() {
        with(HapticUtils) { view.performConfirmHaptic() }
    }
    
    /**
     * Performs a reject haptic feedback (for errors or removals).
     */
    fun performReject() {
        with(HapticUtils) { view.performRejectHaptic() }
    }
}

/**
 * Remember a HapticHandler for use in composables.
 * 
 * @return A HapticHandler that can be used to trigger haptic feedback
 */
@Composable
fun rememberHapticHandler(): HapticHandler {
    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current
    return remember(view, hapticFeedback) {
        HapticHandler(view, hapticFeedback)
    }
}

