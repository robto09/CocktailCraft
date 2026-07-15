package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.domain.model.Order

/**
 * Order summary card: order number + date header, line items, total, and a
 * colored status indicator. Extracted from OrderListScreen so the screen
 * stays a thin state/list shell.
 */
@Composable
fun OrderCard(
    order: Order,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            // Order header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.order_number, order.id.takeLast(5)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                Text(
                    text = order.date,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Order items
            Column {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.order_item_quantity_name, item.quantity, item.name),
                            fontSize = 14.sp,
                            color = AppColors.TextPrimary
                        )

                        Text(
                            text = stringResource(R.string.order_price_format, item.price * item.quantity),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextPrimary
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.md),
                color = AppColors.LightGray
            )

            // Total row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                Text(
                    text = stringResource(R.string.order_price_format, order.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Order status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusColor = when (order.status) {
                    "Completed" -> AppColors.Success
                    "In Progress" -> AppColors.Warning
                    else -> AppColors.Gray
                }

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor, CircleShape)
                )

                Spacer(modifier = Modifier.width(Spacing.sm))

                Text(
                    text = order.status,
                    fontSize = 14.sp,
                    color = statusColor
                )
            }
        }
    }
}
