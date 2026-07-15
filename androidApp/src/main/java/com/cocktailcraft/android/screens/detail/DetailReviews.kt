package com.cocktailcraft.android.screens.detail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.components.DetailInfoCard
import com.cocktailcraft.android.ui.components.RatingBar
import com.cocktailcraft.android.ui.components.SectionHeader
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.domain.model.Review

// Reviews header with write-review action and the first few reviews
@Composable
internal fun DetailReviewsSection(
    reviews: List<Review>,
    onWriteReviewClick: () -> Unit
) {
    DetailInfoCard(
        title = "",
        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = stringResource(R.string.reviews_title, reviews.size),
                    modifier = Modifier.weight(1f),
                    fontSize = 18
                )

                TextButton(
                    onClick = onWriteReviewClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppColors.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(stringResource(R.string.write_review))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            if (reviews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xxl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.detail_be_first_review),
                        color = AppColors.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                reviews.take(3).forEachIndexed { index, review ->
                    CocktailReviewItem(review = review)

                    if (index < reviews.take(3).size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Spacing.md),
                            color = AppColors.LightGray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        },
        elevation = 0
    )
}

@Composable
internal fun CocktailReviewItem(review: Review) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User initial in circle - safely handle potential null/empty username
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AppColors.Primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (review.userName.takeIf { it.isNotBlank() } ?: stringResource(R.string.detail_review_anonymous_initial)).take(1).uppercase(),
                    color = AppColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column {
                Text(
                    text = review.userName.takeIf { it.isNotBlank() } ?: stringResource(R.string.detail_review_anonymous),
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    fontSize = 16.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingBar(
                        rating = review.rating.coerceIn(0f, 5f),
                        modifier = Modifier.padding(end = Spacing.sm, top = Spacing.xs),
                        stars = 5,
                        starsColor = AppColors.Secondary
                    )

                    Text(
                        text = review.date.takeIf { it.isNotBlank() } ?: stringResource(R.string.detail_unknown_date),
                        fontSize = 12.sp,
                        color = AppColors.Gray
                    )
                }
            }
        }

        if (review.comment.isNotBlank()) {
            Text(
                text = review.comment,
                modifier = Modifier.padding(start = 52.dp, top = Spacing.sm),
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// ---- Design-time previews ----

@Preview(name = "Reviews section", showBackground = true)
@Composable
private fun DetailReviewsSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        DetailReviewsSection(
            reviews = PreviewData.reviews,
            onWriteReviewClick = {}
        )
    }
}

@Preview(name = "Reviews section (dark)", showBackground = true)
@Composable
private fun DetailReviewsSectionDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        DetailReviewsSection(
            reviews = PreviewData.reviews,
            onWriteReviewClick = {}
        )
    }
}
