package com.cocktailcraft.android.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.UiEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The snackbar host every screen posts its toasts to: the tabs share
 * MainScreen's, the detail screen (which has no tab chrome) provides its own.
 * Read by [UiEventToasts] and [ErrorToastEffect].
 */
val LocalToastHostState = staticCompositionLocalOf { SnackbarHostState() }

/** Snackbar visuals for the app's toasts: green success, or red error with an optional action. */
private class ToastVisuals(
    override val message: String,
    val isError: Boolean,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration
) : SnackbarVisuals {
    override val withDismissAction: Boolean = false
}

/** A success toast, shown for 2 seconds like the iOS toast. */
suspend fun SnackbarHostState.showSuccessToast(message: String) {
    // Cancelling an indefinite snackbar dismisses it: a 2s toast
    withTimeoutOrNull(SUCCESS_TOAST_DURATION_MS) {
        showSnackbar(ToastVisuals(message, isError = false, duration = SnackbarDuration.Indefinite))
    }
}

/**
 * A success toast with an action (Undo). It stays up for the long snackbar
 * duration, which SnackbarHost stretches further to the user's accessibility
 * timeout, so the action can be reached with TalkBack too.
 */
suspend fun SnackbarHostState.showSuccessToast(message: String, actionLabel: String): SnackbarResult =
    showSnackbar(
        ToastVisuals(
            message = message,
            isError = false,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long
        )
    )

/** An error toast; one with an action stays up longer, so it can be reached. */
suspend fun SnackbarHostState.showErrorToast(message: String, actionLabel: String? = null): SnackbarResult =
    showSnackbar(
        ToastVisuals(
            message = message,
            isError = true,
            actionLabel = actionLabel,
            duration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short
        )
    )

/** Renders the host's snackbars as the app's success/error toasts. */
@Composable
fun ToastSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data ->
        val visuals = data.visuals
        if (visuals is ToastVisuals && visuals.isError) {
            ErrorToast(
                message = visuals.message,
                actionLabel = visuals.actionLabel,
                onAction = { data.performAction() }
            )
        } else {
            SuccessToast(
                message = visuals.message,
                actionLabel = visuals.actionLabel,
                onAction = { data.performAction() }
            )
        }
    }
}

/**
 * Shows a shared ViewModel's one-shot [UiEvent]s as toasts while this is in
 * the composition, running an event's action (Undo) when its button is
 * tapped. Collect each ViewModel's events in one place only: the screen
 * that owns the ViewModel.
 */
@Composable
fun UiEventToasts(events: Flow<UiEvent>) {
    val hostState = LocalToastHostState.current
    val context = LocalContext.current
    LaunchedEffect(events, hostState) {
        events.collect { event ->
            // A newer removal's Undo replaces whatever toast is up, rather
            // than queueing behind a long one (as Material snackbars do)
            if (event.action != null) hostState.currentSnackbarData?.dismiss()
            // Don't hold up collection while a toast is on screen; the host queues them
            launch { hostState.showUiEvent(event, context) }
        }
    }
}

/**
 * Toasts one [UiEvent] — an event, or the outcome an action returned (e.g.
 * add-to-cart) — running its action (Undo) if its button is tapped.
 * Suspends while the toast is up.
 */
suspend fun SnackbarHostState.showUiEvent(event: UiEvent, context: Context) {
    val message = event.message.toString(context)
    val action = event.action
    val actionLabel = action?.label?.toString(context)
    val result = when (event) {
        is UiEvent.Success ->
            if (actionLabel != null) {
                showSuccessToast(message, actionLabel)
            } else {
                showSuccessToast(message)
                null
            }

        is UiEvent.Failure -> showErrorToast(message, actionLabel)
    }
    if (result == SnackbarResult.ActionPerformed) action?.perform()
}

/**
 * Shows [error] — a failure on a screen whose content is still visible —
 * as an error toast, offering its recovery action (e.g. Retry) when it has
 * one, and consumes it via [onConsumed] so it's shown exactly once.
 * Full-screen failures with nothing else to show use [ErrorStateView].
 */
@Composable
fun ErrorToastEffect(error: ErrorHandler.UserFriendlyError?, onConsumed: () -> Unit) {
    val hostState = LocalToastHostState.current
    val context = LocalContext.current
    // Outlives the effect, which restarts as soon as the error is consumed
    val scope = rememberCoroutineScope()
    LaunchedEffect(error) {
        val shown = error ?: return@LaunchedEffect
        onConsumed()
        scope.launch {
            val recovery = shown.recoveryAction
            val result = hostState.showErrorToast(
                message = shown.message.toString(context),
                actionLabel = recovery?.actionLabel?.toString(context)
            )
            if (result == SnackbarResult.ActionPerformed) recovery?.action?.invoke()
        }
    }
}

/**
 * Failure counterpart of [SuccessToast]: same shape, in the error color,
 * with an optional action (e.g. Retry) at the end.
 */
@Composable
fun ErrorToast(
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
            .background(MaterialTheme.colorScheme.error, shape)
            .padding(start = Spacing.lg, end = if (actionLabel != null) Spacing.sm else Spacing.lg)
            .padding(vertical = if (actionLabel != null) Spacing.xs else Spacing.lg)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
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
            modifier = Modifier.weight(1f)
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

// Matches the iOS toast duration
private const val SUCCESS_TOAST_DURATION_MS = 2_000L
