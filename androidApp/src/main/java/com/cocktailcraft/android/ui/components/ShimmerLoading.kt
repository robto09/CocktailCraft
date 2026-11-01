package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.loading
import com.revenuecat.placeholder.PlaceholderSurface
import com.revenuecat.placeholder.placeholder

// Skeleton blocks are drawn with Modifier.placeholder; their fill, shimmer and
// Reduce Motion behavior come from the app theme (AnimatedCocktailBarTheme).
// Wrap a loading container in PlaceholderSurface so every block beneath it
// shares one animation clock and shimmers in unison.

/**
 * A shimmer loading placeholder for a cocktail item. Place it inside a
 * [PlaceholderSurface] (as [CocktailLoadingShimmer] does).
 */
@Composable
fun CocktailItemShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .placeholder(shape = RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Content placeholders
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .placeholder(shape = RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .placeholder(shape = RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price placeholder
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .placeholder(shape = RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.weight(1f))

                // Action buttons placeholders
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .placeholder(shape = CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .placeholder(shape = CircleShape)
                )
            }
        }
    }
}

/**
 * A shimmer loading placeholder for a cocktail detail
 */
@Composable
fun CocktailDetailShimmer(
    modifier: Modifier = Modifier
) {
    val loadingLabel = stringResource(MR.strings.loading)

    // Screen-level loading container: one animation clock for all placeholders
    PlaceholderSurface {
        // The placeholders mean nothing to TalkBack: read "Loading…" instead
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .clearAndSetSemantics { contentDescription = loadingLabel }
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .placeholder(shape = RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(24.dp)
                    .placeholder(shape = RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rating placeholder
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                        .placeholder(shape = RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(20.dp)
                        .placeholder(shape = RoundedCornerShape(4.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chips row
            Row {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(32.dp)
                        .placeholder(shape = RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(32.dp)
                        .placeholder(shape = RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description placeholder
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .placeholder(shape = RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ingredients title
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .placeholder(shape = RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ingredients list
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .placeholder(shape = RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * A shimmer loading placeholder shaped like a [CartItemCard] row: image,
 * name with favorite toggle, unit price, then the quantity stepper and line
 * total. Place it inside a [PlaceholderSurface] (as [CartLoadingShimmer] does).
 */
@Composable
fun CartItemShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .placeholder(shape = RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            // Name and favorite toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .placeholder(shape = RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .placeholder(shape = CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Unit price
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(14.dp)
                    .placeholder(shape = RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quantity stepper and line total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .placeholder(shape = CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .placeholder(shape = RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .placeholder(shape = CircleShape)
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .placeholder(shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

/**
 * A shimmer loading placeholder shaped like an [OrderCard]: order number and
 * date, line items, total and status. Place it inside a [PlaceholderSurface]
 * (as [OrderListLoadingShimmer] does).
 */
@Composable
fun OrderCardShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg)
    ) {
        // Order number and date
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(16.dp)
                    .placeholder(shape = RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
                    .placeholder(shape = RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Line items
        repeat(2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .placeholder(shape = RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(14.dp)
                        .placeholder(shape = RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(16.dp)
                    .placeholder(shape = RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(16.dp)
                    .placeholder(shape = RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Status
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .placeholder(shape = CircleShape)
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(14.dp)
                    .placeholder(shape = RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Cart loading state: a few [CartItemShimmer] rows sharing one animation clock.
 */
@Composable
fun CartLoadingShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 3
) {
    val loadingLabel = stringResource(MR.strings.loading)

    PlaceholderSurface {
        // The placeholders mean nothing to TalkBack: read "Loading…" instead
        Column(
            modifier = modifier
                .fillMaxSize()
                .clearAndSetSemantics { contentDescription = loadingLabel },
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(itemCount) {
                CartItemShimmer()
            }
        }
    }
}

/**
 * Orders loading state: a few [OrderCardShimmer] cards sharing one animation
 * clock, padded like the order list.
 */
@Composable
fun OrderListLoadingShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 3
) {
    val loadingLabel = stringResource(MR.strings.loading)

    PlaceholderSurface {
        // The placeholders mean nothing to TalkBack: read "Loading…" instead
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(Spacing.lg)
                .clearAndSetSemantics { contentDescription = loadingLabel },
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            repeat(itemCount) {
                OrderCardShimmer()
            }
        }
    }
}
