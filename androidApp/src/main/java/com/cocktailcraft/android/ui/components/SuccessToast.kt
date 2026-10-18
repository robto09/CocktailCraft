package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.ui.theme.Spacing

/**
 * Success confirmation ("Margarita added to cart"), drawn like the iOS Toast:
 * green, 12dp corners, a white check icon and white text, with a shadow.
 * Rendered by the tabs' SnackbarHost so it keeps the snackbar queueing.
 *
 * With an [actionLabel] (Undo after a removal) it ends in a white text
 * button — a 48dp touch target — that TalkBack reaches as a button and also
 * offers as a custom action on the message, so it can be run from the
 * actions menu without hunting for the button.
 */
@Composable
fun SuccessToast(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            .shadow(elevation = 8.dp, shape = shape)
            .background(CocktailCraftTheme.extendedColors.success, shape)
            .padding(start = Spacing.lg, end = if (actionLabel != null) Spacing.sm else Spacing.lg)
            .padding(vertical = if (actionLabel != null) Spacing.xs else Spacing.lg)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                if (actionLabel != null) {
                    customActions = listOf(
                        CustomAccessibilityAction(actionLabel) {
                            onAction()
                            true
                        }
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(Spacing.md))
        Text(
            text = message,
            color = Color.White,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = if (actionLabel != null) Modifier.weight(1f) else Modifier
        )
        if (actionLabel != null) {
            TextButton(
                onClick = onAction,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text(text = actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}
