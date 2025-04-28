package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors
import java.text.NumberFormat
import java.util.Locale

/**
 * A reusable order summary card component.
 *
 * @param subtotal The subtotal amount
 * @param deliveryFee The delivery fee amount
 * @param total The total amount (if null, calculated as subtotal + deliveryFee)
 * @param modifier The modifier for the component
 * @param currencyFormatter The formatter for currency values
 * @param showDeliveryFee Whether to show the delivery fee row
 * @param additionalItems Optional list of additional items to display
 */
@Composable
fun OrderSummaryCard(
    subtotal: Double,
    deliveryFee: Double = 5.99,
    total: Double? = null,
    modifier: Modifier = Modifier,
    currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US),
    showDeliveryFee: Boolean = true,
    additionalItems: List<Pair<String, Double>>? = null
) {
    val calculatedTotal = total ?: (subtotal + deliveryFee)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtotal row
            SummaryRow(
                label = "Subtotal",
                value = currencyFormatter.format(subtotal)
            )
            
            // Delivery fee row
            if (showDeliveryFee) {
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(
                    label = "Delivery Fee",
                    value = currencyFormatter.format(deliveryFee)
                )
            }
            
            // Additional items
            additionalItems?.forEach { (label, value) ->
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(
                    label = label,
                    value = currencyFormatter.format(value)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = AppColors.LightGray)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Total row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = currencyFormatter.format(calculatedTotal),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.Primary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = AppColors.TextSecondary
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = AppColors.TextPrimary
        )
    }
}
