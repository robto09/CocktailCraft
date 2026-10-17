package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.cancel
import com.cocktailcraft.resources.review_character_count
import com.cocktailcraft.resources.review_enter_your_name
import com.cocktailcraft.resources.review_name_required
import com.cocktailcraft.resources.review_rate_star
import com.cocktailcraft.resources.review_select_rating_error
import com.cocktailcraft.resources.review_share_experience_placeholder
import com.cocktailcraft.resources.review_submit
import com.cocktailcraft.resources.review_your_name
import com.cocktailcraft.resources.review_your_rating
import com.cocktailcraft.resources.review_your_review
import com.cocktailcraft.resources.write_review

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (userName: String, rating: Float, comment: String) -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var userRating by remember { mutableStateOf(0f) }
    var userComment by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(MR.strings.write_review),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Name input with validation
                    Column {
                        Text(
                            text = stringResource(MR.strings.review_your_name),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = userName,
                            onValueChange = {
                                userName = it
                                hasError = false
                            },
                            placeholder = { Text(stringResource(MR.strings.review_enter_your_name), color = MaterialTheme.colorScheme.outline) },
                            isError = hasError && userName.isBlank(),
                            supportingText = {
                                if (hasError && userName.isBlank()) {
                                    Text(
                                        text = stringResource(MR.strings.review_name_required),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = CocktailCraftTheme.extendedColors.neutralSubtle,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    // Rating section with enhanced UI
                    Column {
                        Text(
                            text = stringResource(MR.strings.review_your_rating),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // A single-choice group: TalkBack reads "Rate 4, selected"
                        // for the chosen star
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                                .selectableGroup(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(5) { index ->
                                IconButton(
                                    onClick = { userRating = index + 1f },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .semantics { selected = userRating.toInt() == index + 1 }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = stringResource(MR.strings.review_rate_star, index + 1),
                                        tint = if (index < userRating) CocktailCraftTheme.extendedColors.rating else CocktailCraftTheme.extendedColors.neutralSubtle,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        if (hasError && userRating == 0f) {
                            Text(
                                text = stringResource(MR.strings.review_select_rating_error),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    // Announced when validation fails on submit
                                    .semantics { liveRegion = LiveRegionMode.Polite }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Review text with character count
                    Column {
                        Text(
                            text = stringResource(MR.strings.review_your_review),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = userComment,
                            onValueChange = {
                                if (it.length <= MAX_COMMENT_LENGTH) {
                                    userComment = it
                                }
                            },
                            placeholder = { Text(stringResource(MR.strings.review_share_experience_placeholder), color = MaterialTheme.colorScheme.outline) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = CocktailCraftTheme.extendedColors.neutralSubtle,
                                focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(MR.strings.review_character_count, userComment.length, MAX_COMMENT_LENGTH),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userName.isBlank() || userRating == 0f) {
                            hasError = true
                        } else {
                            onSubmit(userName, userRating, userComment)
                            // Reset fields
                            userName = ""
                            userRating = 0f
                            userComment = ""
                            hasError = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = stringResource(MR.strings.review_submit),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(MR.strings.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp
        )
    }
}

// Mirrors the shared review rule (ManageReviewsUseCase.validateReview)
private const val MAX_COMMENT_LENGTH = 500
