package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.format.displayName
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.order_item_quantity_name
import com.cocktailcraft.resources.order_number
import com.cocktailcraft.resources.order_view_details_a11y
import com.cocktailcraft.resources.total

/**
 * Order summary card: order number + date header, line items, total, and a
 * colored status indicator. Extracted from OrderListScreen so the screen
 * stays a thin state/list shell. With [onClick] the whole card opens the
 * order's details (TalkBack: "View order details").
 */
@Composable
fun OrderCard(
    order: Order,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val clickLabel = stringResource(MR.strings.order_view_details_a11y)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Clickable inside the card, so the ripple is clipped to its shape
        // without also clipping the card's shadow
        Column(
            modifier = Modifier
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClickLabel = clickLabel, role = Role.Button, onClick = onClick)
                    } else {
                        Modifier
                    }
                )
                .padding(Spacing.lg)
        ) {
            // Order header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(MR.strings.order_number, order.id.takeLast(5)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )

                Text(
                    text = order.date,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Order items
            Column {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs)
                            .semantics(mergeDescendants = true) {},
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(MR.strings.order_item_quantity_name, item.quantity, item.name),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = formatPrice(item.lineTotal),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.md),
                color = CocktailCraftTheme.extendedColors.neutralSubtle
            )

            // Total row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(MR.strings.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatPrice(order.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Order status
            OrderStatusIndicator(status = order.status)
        }
    }
}

/** The color an order status is shown in, on the order list and the detail screen. */
@Composable
internal fun orderStatusColor(status: OrderStatus): Color = when (status) {
    OrderStatus.DELIVERED -> CocktailCraftTheme.extendedColors.success

    OrderStatus.PENDING,
    OrderStatus.PROCESSING,
    OrderStatus.SHIPPED -> CocktailCraftTheme.extendedColors.warning

    OrderStatus.CANCELLED -> MaterialTheme.colorScheme.outline
}

/** A colored dot and the status's localized label ("Being Prepared"). */
@Composable
internal fun OrderStatusIndicator(status: OrderStatus, modifier: Modifier = Modifier) {
    val statusColor = orderStatusColor(status)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(statusColor, CircleShape)
        )

        Spacer(modifier = Modifier.width(Spacing.sm))

        Text(
            text = status.displayName.resolve(),
            fontSize = 14.sp,
            color = statusColor
        )
    }
}
