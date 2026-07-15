package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.R
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.LoadingStateComponent
import com.cocktailcraft.android.ui.components.OrderCard
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    orderViewModel: SharedOrderViewModel,
    navigationManager: NavigationManager
) {
    val state by orderViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = state.isLoading
    val error by orderViewModel.error.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Load orders when the screen is first displayed
    LaunchedEffect(Unit) {
        orderViewModel.loadOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        LoadingStateComponent(isLoading = isLoading)

        if (!isLoading && error != null) {
            EmptyStateComponent(
                title = stringResource(R.string.error),
                message = error?.message ?: stringResource(R.string.order_unknown_error),
                actionButtonText = stringResource(R.string.cart_try_again),
                onActionButtonClick = { scope.launch { orderViewModel.loadOrders() } }
            )
        } else if (!isLoading && state.orders.isEmpty()) {
            EmptyStateComponent(
                title = stringResource(R.string.order_list_empty_title),
                message = stringResource(R.string.order_list_empty_message),
                actionButtonText = stringResource(R.string.browse_cocktails),
                onActionButtonClick = { navigationManager.navigateToHome() },
                icon = Icons.AutoMirrored.Filled.List
            )
        } else if (!isLoading) {
            // Pull-to-refresh, parity with iOS Orders
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { scope.launch { orderViewModel.loadOrders() } },
                modifier = Modifier.fillMaxSize(),
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isLoading,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = Color.White,
                        color = AppColors.Primary
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
