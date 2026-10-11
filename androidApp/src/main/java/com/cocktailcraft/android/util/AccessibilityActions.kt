package com.cocktailcraft.android.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics

/**
 * Exposes pull-to-refresh — otherwise a swipe-only gesture — as a TalkBack
 * custom action on the refreshable container, so screen-reader users can
 * refresh from the actions menu. Pass the shared "Refresh" copy as [label].
 */
fun Modifier.refreshAccessibilityAction(label: String, onRefresh: () -> Unit): Modifier =
    semantics {
        customActions = listOf(
            CustomAccessibilityAction(label) {
                onRefresh()
                true
            }
        )
    }
