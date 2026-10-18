package com.cocktailcraft.android.ui.components

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.order_delivery_fee
import com.cocktailcraft.resources.order_subtotal
import com.cocktailcraft.resources.order_summary_title
import com.cocktailcraft.resources.total

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
    subtotal: Money,
    deliveryFee: Money = DeliveryPolicy.STANDARD_FEE,
    total: Money? = null,
    modifier: Modifier = Modifier,
    currencyFormatter: CurrencyFormatter = rememberCurrencyFormatter(),
    showDeliveryFee: Boolean = true,
    additionalItems: List<Pair<String, Money>>? = null
) {
    val calculatedTotal = total ?: (subtotal + deliveryFee)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(MR.strings.order_summary_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtotal row
            SummaryRow(
                label = stringResource(MR.strings.order_subtotal),
                value = currencyFormatter.format(subtotal)
            )
            
            // Delivery fee row
            if (showDeliveryFee) {
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(
                    label = stringResource(MR.strings.order_delivery_fee),
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
            
            HorizontalDivider(color = CocktailCraftTheme.extendedColors.neutralSubtle)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Total row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(MR.strings.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currencyFormatter.format(calculatedTotal),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
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
    // One TalkBack stop per line: "Subtotal, $25.00"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
