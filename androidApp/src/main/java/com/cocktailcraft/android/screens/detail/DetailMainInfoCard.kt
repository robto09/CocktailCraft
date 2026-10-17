package com.cocktailcraft.android.screens.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.add_to_cart
import com.cocktailcraft.resources.detail_no_instructions
import com.cocktailcraft.resources.detail_price_format
import com.cocktailcraft.resources.detail_refresh_details
import com.cocktailcraft.resources.detail_update_cart
import com.cocktailcraft.resources.how_to_prepare

// Main info card, laid out like the iOS one: name, alcoholic/category line,
// price, add-to-cart button and preparation. Its rounded top overlaps the
// header image by 20dp. Favorite and share live in the top bar; no stock or
// rating, which the API doesn't provide.
@Composable
internal fun DetailMainInfoCard(
    cocktail: Cocktail,
    isInCart: Boolean,
    onAddToCartClick: () -> Unit,
    onRefreshDetails: () -> Unit
) {
    val overlap = 20.dp
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Pull the card up over the image and give the 20dp back to the
            // list, so the next section keeps the usual 16dp gap
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val shift = overlap.roundToPx()
                layout(placeable.width, placeable.height - shift) {
                    placeable.place(0, -shift)
                }
            }
            .padding(horizontal = Spacing.lg),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = cocktail.name,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // "Alcoholic • Cocktail" from the API values that are present
            val subtitle = listOfNotNull(cocktail.knownAlcoholic, cocktail.category).joinToString(" • ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
            }

            Text(
                text = stringResource(MR.strings.detail_price_format, cocktail.price),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Add to cart button, same height as the app's other main buttons
            Button(
                onClick = onAddToCartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = cocktail.stockCount > 0
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(Spacing.sm))

                Text(
                    text = if (isInCart) stringResource(MR.strings.detail_update_cart) else stringResource(MR.strings.add_to_cart),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Preparation: plain text in the card, as on iOS
            Text(
                text = stringResource(MR.strings.how_to_prepare),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            val instructionsText = cocktail.instructions ?: ""
            if (instructionsText.isNotBlank()) {
                Text(
                    text = instructionsText,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            } else {
                Text(
                    text = stringResource(MR.strings.detail_no_instructions),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                OutlinedButton(
                    onClick = onRefreshDetails,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(MR.strings.detail_refresh_details),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ---- Design-time previews ----

@Preview(name = "Main info card", showBackground = true)
@Composable
private fun DetailMainInfoCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        DetailMainInfoCard(
            cocktail = PreviewData.cocktail,
            isInCart = false,
            onAddToCartClick = {},
            onRefreshDetails = {}
        )
    }
}

@Preview(name = "Main info card (dark)", showBackground = true)
@Composable
private fun DetailMainInfoCardDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        DetailMainInfoCard(
            cocktail = PreviewData.cocktail,
            isInCart = false,
            onAddToCartClick = {},
            onRefreshDetails = {}
        )
    }
}

@Preview(name = "Main info card (large font)", showBackground = true, fontScale = 1.5f)
@Composable
private fun DetailMainInfoCardLargeFontPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        DetailMainInfoCard(
            cocktail = PreviewData.cocktail,
            isInCart = false,
            onAddToCartClick = {},
            onRefreshDetails = {}
        )
    }
}
