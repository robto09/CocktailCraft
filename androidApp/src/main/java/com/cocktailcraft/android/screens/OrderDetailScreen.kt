package com.cocktailcraft.android.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.ui.components.AppTopBar
import com.cocktailcraft.android.ui.components.ConfirmationDialog
import com.cocktailcraft.android.ui.components.DetailInfoCard
import com.cocktailcraft.android.ui.components.ErrorStateView
import com.cocktailcraft.android.ui.components.ErrorToastEffect
import com.cocktailcraft.android.ui.components.LocalToastHostState
import com.cocktailcraft.android.ui.components.OrderListLoadingShimmer
import com.cocktailcraft.android.ui.components.OrderStatusIndicator
import com.cocktailcraft.android.ui.components.OrderSummaryCard
import com.cocktailcraft.android.ui.components.PrimaryButton
import com.cocktailcraft.android.ui.components.ToastSnackbarHost
import com.cocktailcraft.android.ui.components.UiEventToasts
import com.cocktailcraft.android.ui.components.formatPrice
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.order_cancel
import com.cocktailcraft.resources.order_cancel_confirm_message
import com.cocktailcraft.resources.order_cancel_confirm_title
import com.cocktailcraft.resources.order_cancel_keep
import com.cocktailcraft.resources.order_detail_delivery
import com.cocktailcraft.resources.order_detail_item_each
import com.cocktailcraft.resources.order_detail_items
import com.cocktailcraft.resources.order_detail_not_found_message
import com.cocktailcraft.resources.order_detail_not_found_title
import com.cocktailcraft.resources.order_detail_placed_on
import com.cocktailcraft.resources.order_detail_status
import com.cocktailcraft.resources.order_detail_title
import com.cocktailcraft.resources.order_item_quantity_name
import com.cocktailcraft.resources.order_number
import com.cocktailcraft.resources.order_reorder
import com.cocktailcraft.resources.order_reorder_unavailable
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import com.cocktailcraft.viewmodel.state.OrderUiState
import kotlinx.coroutines.launch

/**
 * One order: status, delivery estimate, items and totals, with Reorder (its
 * items back into the cart) and "Cancel Order" (behind a confirmation)
 * while the order is still cancellable.
 * Pushed above the Orders tab with its own top bar and toast host, like the
 * cocktail detail screen.
 */
@Composable
fun OrderDetailScreen(
    orderId: String,
    orderViewModel: SharedOrderViewModel,
    onBackClick: () -> Unit
) {
    val state by orderViewModel.uiState.collectAsStateWithLifecycle()
    val error by orderViewModel.error.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    // Distinguishes "load not started yet" from "loaded but not found"
    var hasLoaded by remember(orderId) { mutableStateOf(false) }
    LaunchedEffect(orderId) {
        orderViewModel.loadOrderDetail(orderId)
        hasLoaded = true
    }

    val toastHostState = remember { SnackbarHostState() }
    // The order ViewModel is a Koin single: never render another order
    // left selected by an earlier visit
    val order = state.selectedOrder?.takeIf { it.id == orderId }

    CompositionLocalProvider(LocalToastHostState provides toastHostState) {
        // "Order cancelled", "Added 3 items to your cart" and a failed
        // reorder — the only collector of the order events
        UiEventToasts(orderViewModel.events)
        // With the order on screen a failed cancel is a toast; a failed load
        // is the full-screen error state instead
        ErrorToastEffect(
            error = error.takeIf { order != null },
            onConsumed = { orderViewModel.clearError() }
        )

        OrderDetailContent(
            state = state.copy(selectedOrder = order),
            isLoading = order == null && (!hasLoaded || state.isLoadingOrderDetail),
            error = error,
            onBackClick = onBackClick,
            onRetry = { scope.launch { orderViewModel.loadOrderDetail(orderId) } },
            onConfirmCancel = { scope.launch { orderViewModel.cancelOrder(orderId) } },
            onReorder = { scope.launch { orderViewModel.reorder(orderId) } },
            toastHostState = toastHostState
        )
    }
}

/**
 * Stateless order detail content: renders [state]'s selected order and
 * reports every intent upward, so it can be previewed/screenshot-tested
 * without a ViewModel. Owns only the cancel confirmation.
 */
@Composable
internal fun OrderDetailContent(
    state: OrderUiState,
    onBackClick: () -> Unit,
    onConfirmCancel: () -> Unit,
    isLoading: Boolean = false,
    error: ErrorHandler.UserFriendlyError? = null,
    onRetry: () -> Unit = {},
    onReorder: () -> Unit = {},
    toastHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val order = state.selectedOrder
    val loadError = error.takeIf { order == null && !isLoading }
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }

    if (order != null) {
        ConfirmationDialog(
            showDialog = showCancelDialog,
            title = stringResource(MR.strings.order_cancel_confirm_title),
            message = stringResource(MR.strings.order_cancel_confirm_message, order.id.takeLast(5)),
            confirmButtonText = stringResource(MR.strings.order_cancel),
            dismissButtonText = stringResource(MR.strings.order_cancel_keep),
            confirmButtonColor = MaterialTheme.colorScheme.error,
            onConfirm = {
                showCancelDialog = false
                onConfirmCancel()
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.testTag("order_detail_screen"),
        topBar = {
            AppTopBar(
                title = stringResource(MR.strings.order_detail_title),
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        snackbarHost = { ToastSnackbarHost(toastHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
        when {
            isLoading -> Column(modifier = contentModifier) {
                OrderListLoadingShimmer(itemCount = 1)
            }
            loadError != null -> ErrorStateView(error = loadError, onRetry = onRetry, modifier = contentModifier)
            order == null -> ErrorStateView(
                title = stringResource(MR.strings.order_detail_not_found_title),
                message = stringResource(MR.strings.order_detail_not_found_message),
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                onRetry = null,
                modifier = contentModifier
            )
            else -> OrderDetailBody(
                order = order,
                state = state,
                onCancelClick = { showCancelDialog = true },
                onReorderClick = onReorder,
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun OrderDetailBody(
    order: Order,
    state: OrderUiState,
    onCancelClick: () -> Unit,
    onReorderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        OrderStatusCard(order = order, deliveryEstimate = state.selectedOrderDeliveryEstimate.resolve())

        DetailInfoCard(
            title = stringResource(MR.strings.order_detail_items),
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    order.items.forEach { item -> OrderItemRow(item) }
                }
            }
        )

        OrderSummaryCard(
            subtotal = state.selectedOrderSubtotal,
            deliveryFee = state.selectedOrderDeliveryFee,
            total = order.total
        )

        ReorderButton(
            canReorder = state.canReorderSelectedOrder,
            isReordering = state.isReordering,
            onClick = onReorderClick
        )

        if (state.canCancelSelectedOrder) {
            CancelOrderButton(isCancelling = state.isCancellingOrder, onClick = onCancelClick)
        }

        Spacer(modifier = Modifier.height(Spacing.sm))
    }
}

/** Order number, placement date, status and delivery estimate. */
@Composable
private fun OrderStatusCard(order: Order, deliveryEstimate: String) {
    DetailInfoCard(
        title = stringResource(MR.strings.order_number, order.id.takeLast(5)),
        titleFontSize = 20,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    text = stringResource(MR.strings.order_detail_placed_on, order.date),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LabeledRow(label = stringResource(MR.strings.order_detail_status)) {
                    OrderStatusIndicator(status = order.status)
                }
                LabeledRow(label = stringResource(MR.strings.order_detail_delivery)) {
                    Text(
                        text = deliveryEstimate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

/** "Status   ● Being Prepared" — one TalkBack stop per row. */
@Composable
private fun LabeledRow(label: String, value: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        value()
    }
}

/** "2x Margarita" with its line total, and the unit price underneath. */
@Composable
private fun OrderItemRow(item: OrderItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(MR.strings.order_item_quantity_name, item.quantity, item.name),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatPrice(item.lineTotal),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = stringResource(
                MR.strings.order_detail_item_each,
                formatPrice(item.price)
            ),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Puts the order's items back in the cart; shows a spinner while that runs.
 * Orders placed before items recorded their cocktail can't be reordered:
 * the button is disabled with the reason underneath, and TalkBack reads the
 * reason as the disabled button's state rather than as a separate stop.
 */
@Composable
private fun ReorderButton(canReorder: Boolean, isReordering: Boolean, onClick: () -> Unit) {
    val unavailable = stringResource(MR.strings.order_reorder_unavailable)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        PrimaryButton(
            text = stringResource(MR.strings.order_reorder),
            onClick = onClick,
            enabled = canReorder,
            isLoading = isReordering,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_detail_reorder")
                .then(if (canReorder) Modifier else Modifier.semantics { stateDescription = unavailable })
        )
        if (!canReorder) {
            Text(
                text = unavailable,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics {}
            )
        }
    }
}

/** Outlined in the error color; shows a spinner while the cancel is in flight. */
@Composable
private fun CancelOrderButton(isCancelling: Boolean, onClick: () -> Unit) {
    val label = stringResource(MR.strings.order_cancel)
    OutlinedButton(
        onClick = onClick,
        enabled = !isCancelling,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .testTag("order_detail_cancel")
    ) {
        if (isCancelling) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.error,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = label }
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
