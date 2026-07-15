package com.cocktailcraft.android.screens.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.components.DetailInfoCard
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.domain.model.Cocktail

// Main info card: price and favorite row, subtitle, stock status,
// add-to-cart button and preparation instructions
@Composable
internal fun DetailMainInfoCard(
    cocktail: Cocktail,
    isFavorite: Boolean,
    isInCart: Boolean,
    onToggleFavorite: () -> Unit,
    onAddToCartClick: () -> Unit,
    onRefreshDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-20).dp)
            .padding(horizontal = Spacing.lg),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl)
        ) {
            // Price and favorite row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.detail_price_format, cocktail.price),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )

                // Favorite button with haptic feedback —
                // default size keeps the 48 dp touch target
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) stringResource(R.string.detail_remove_from_favorites) else stringResource(R.string.detail_add_to_favorites),
                        tint = if (isFavorite) AppColors.Secondary else AppColors.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Category and alcoholic info subtitle
            val unknownAlcoholic = stringResource(R.string.detail_alcoholic_unknown)
            Text(
                text = buildString {
                    append(cocktail.alcoholic ?: unknownAlcoholic)
                    cocktail.category?.let {
                        append(" • ")
                        append(it)
                    }
                },
                fontSize = 16.sp,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Stock status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val inStock = cocktail.stockCount > 0
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (inStock) AppColors.Success else AppColors.Error,
                            CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(Spacing.sm))

                Text(
                    text = if (inStock) stringResource(R.string.detail_in_stock, cocktail.stockCount) else stringResource(R.string.detail_out_of_stock),
                    fontSize = 14.sp,
                    color = if (inStock) AppColors.Success else AppColors.Error,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // Add to cart button
            Button(
                onClick = onAddToCartClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    disabledContainerColor = AppColors.Gray
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
                    text = if (isInCart) stringResource(R.string.detail_update_cart) else stringResource(R.string.add_to_cart),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // Instructions
            DetailInfoCard(
                title = stringResource(R.string.how_to_prepare),
                modifier = Modifier.padding(vertical = Spacing.sm),
                content = {
                    // Add debug print to console to verify instructions are available
                    val instructionsText = cocktail.instructions ?: ""

                    if (instructionsText.isNotBlank()) {
                        Text(
                            text = instructionsText,
                            fontSize = 15.sp,
                            color = AppColors.TextPrimary,
                            lineHeight = 24.sp
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.detail_no_instructions),
                                fontSize = 15.sp,
                                color = AppColors.TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            OutlinedButton(
                                onClick = onRefreshDetails,
                                border = BorderStroke(1.dp, AppColors.Primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.detail_refresh_details),
                                    color = AppColors.Primary
                                )
                            }
                        }
                    }
                },
                backgroundColor = AppColors.Surface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(Spacing.lg))
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
            isFavorite = true,
            isInCart = false,
            onToggleFavorite = {},
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
            isFavorite = true,
            isInCart = false,
            onToggleFavorite = {},
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
            isFavorite = true,
            isInCart = false,
            onToggleFavorite = {},
            onAddToCartClick = {},
            onRefreshDetails = {}
        )
    }
}
