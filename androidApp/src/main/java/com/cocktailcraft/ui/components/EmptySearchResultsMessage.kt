package com.cocktailcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A visually appealing message displayed when search results are empty
 */
@Composable
fun EmptySearchResultsMessage(
    searchQuery: String,
    selectedCategory: String? = null,
    onClearSearch: () -> Unit,
    onClearCategory: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Animation states
    val visibleState = remember { MutableTransitionState(false) }

    // Set visible to trigger the animation
    LaunchedEffect(Unit) {
        visibleState.targetState = true
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 400)
            ) + slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                initialOffsetY = { it / 4 }
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp)
            ) {
                // Icon based on context
                val icon = when {
                    selectedCategory != null -> Icons.Outlined.Category
                    searchQuery.isNotBlank() -> Icons.Outlined.SearchOff
                    else -> Icons.Filled.Search
                }

                EmptyStateIcon(icon)

                Spacer(modifier = Modifier.height(24.dp))

                // Main message
                Text(
                    text = when {
                        selectedCategory != null && searchQuery.isNotBlank() ->
                            "No cocktails found matching \"$searchQuery\" in \"$selectedCategory\""
                        selectedCategory != null ->
                            "No cocktails found in category \"$selectedCategory\""
                        searchQuery.isNotBlank() ->
                            "No cocktails found matching \"$searchQuery\""
                        else ->
                            "No cocktails found"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle with suggestions
                Text(
                    text = when {
                        selectedCategory != null && searchQuery.isNotBlank() ->
                            "Try a different search term or category"
                        selectedCategory != null ->
                            "This category doesn't have any cocktails yet"
                        searchQuery.isNotBlank() ->
                            "Try a different search term or check your spelling"
                        else ->
                            "We couldn't find any cocktails"
                    },
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                when {
                    selectedCategory != null && searchQuery.isNotBlank() -> {
                        // Both category and search are active
                        Button(
                            onClick = onClearSearch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary
                            )
                        ) {
                            Text("Clear Search")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onClearCategory?.invoke() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.Primary
                            )
                        ) {
                            Text("Clear Category Filter")
                        }
                    }
                    selectedCategory != null -> {
                        // Only category is active
                        Button(
                            onClick = { onClearCategory?.invoke() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary
                            )
                        ) {
                            Text("Browse All Cocktails")
                        }
                    }
                    searchQuery.isNotBlank() -> {
                        // Only search is active
                        Button(
                            onClick = onClearSearch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary
                            )
                        ) {
                            Text("Clear Search")
                        }
                    }
                    else -> {
                        // Neither is active
                        Button(
                            onClick = onClearSearch, // This will refresh the list
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary
                            )
                        ) {
                            Text("Refresh")
                        }
                    }
                }


            }
        }
    }
}

@Composable
private fun EmptyStateIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "No results",
            tint = AppColors.Primary.copy(alpha = 0.2f),
            modifier = Modifier.size(80.dp)
        )

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.Primary,
            modifier = Modifier.size(40.dp)
        )
    }
}
