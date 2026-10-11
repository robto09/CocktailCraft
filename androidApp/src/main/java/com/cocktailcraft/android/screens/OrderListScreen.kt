package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.LoadingStateComponent
import com.cocktailcraft.android.ui.components.OrderCard
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.refreshAccessibilityAction
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.refresh
import com.cocktailcraft.resources.browse_cocktails
import com.cocktailcraft.resources.cart_try_again
import com.cocktailcraft.resources.error
import com.cocktailcraft.resources.order_list_empty_message
import com.cocktailcraft.resources.order_list_empty_title
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import com.cocktailcraft.viewmodel.state.OrderUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    orderViewModel: SharedOrderViewModel,
    navigationManager: NavigationManager
) {
    val state by orderViewModel.uiState.collectAsStateWithLifecycle()
    val error by orderViewModel.error.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Load orders when the screen is first displayed
    LaunchedEffect(Unit) {
        orderViewModel.loadOrders()
    }

    OrderListContent(
        state = state,
        error = error,
        onReload = { scope.launch { orderViewModel.loadOrders() } },
        onBrowseCocktails = { navigationManager.navigateToHome() }
    )
}

/**
 * Stateless order-list content: renders [state] and reports every intent
 * upward, so it can be previewed/screenshot-tested without a ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OrderListContent(
    state: OrderUiState,
    error: ErrorHandler.UserFriendlyError?,
    onReload: () -> Unit,
    onBrowseCocktails: () -> Unit
) {
    val isLoading = state.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LoadingStateComponent(isLoading = isLoading)

        if (!isLoading && error != null) {
            EmptyStateComponent(
                title = stringResource(MR.strings.error),
                message = error.message.resolve(),
                actionButtonText = stringResource(MR.strings.cart_try_again),
                onActionButtonClick = onReload,
                icon = Icons.Filled.Warning
            )
        } else if (!isLoading && state.orders.isEmpty()) {
            EmptyStateComponent(
                title = stringResource(MR.strings.order_list_empty_title),
                message = stringResource(MR.strings.order_list_empty_message),
                actionButtonText = stringResource(MR.strings.browse_cocktails),
                onActionButtonClick = onBrowseCocktails,
                icon = Icons.AutoMirrored.Filled.List
            )
        } else if (!isLoading) {
            // Pull-to-refresh, parity with iOS Orders
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onReload,
                modifier = Modifier
                    .fillMaxSize()
                    .refreshAccessibilityAction(stringResource(MR.strings.refresh), onReload),
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isLoading,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = Color.White,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.lg),
                    contentPadding = PaddingValues(vertical = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    items(state.orders, key = { it.id }) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }
    }
}
