package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.ui.theme.Spacing
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
import kotlinx.coroutines.launch

/**
 * Write-a-review bottom sheet, laid out like the iOS sheet (and like the
 * Advanced Search sheets): a Cancel / title header, name, a 1-5 star picker,
 * the comment with a 0/500 counter, then a full-width Submit Review button.
 *
 * Name and rating are checked here; the comment rules live in the shared
 * ReviewViewModel. [onSubmit] returns whether the review was saved: the sheet
 * closes only then, and otherwise stays open with [submissionError] shown
 * inline, so nothing typed is lost.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewSheet(
    submissionError: String?,
    onDismiss: () -> Unit,
    onClearError: () -> Unit,
    onSubmit: suspend (userName: String, rating: Float, comment: String) -> Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("") }
    var userRating by remember { mutableIntStateOf(0) }
    var userComment by remember { mutableStateOf("") }
    var showFieldErrors by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    fun animateClose() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xl)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Cancel | title, with an invisible Cancel on the right
            // so the title stays centered
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { animateClose() }) {
                    Text(stringResource(MR.strings.cancel))
                }
                Text(
                    text = stringResource(MR.strings.write_review),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                TextButton(onClick = {}, enabled = false, modifier = Modifier.alpha(0f)) {
                    Text(stringResource(MR.strings.cancel))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Name
            FieldLabel(stringResource(MR.strings.review_your_name))
            OutlinedTextField(
                value = userName,
                onValueChange = {
                    userName = it
                    onClearError()
                },
                placeholder = {
                    Text(
                        stringResource(MR.strings.review_enter_your_name),
                        color = MaterialTheme.colorScheme.outline
                    )
                },
                isError = showFieldErrors && userName.isBlank(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = reviewFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            if (showFieldErrors && userName.isBlank()) {
                ErrorText(stringResource(MR.strings.review_name_required))
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Rating: a single-choice group, TalkBack reads "Rate 4, selected"
            FieldLabel(stringResource(MR.strings.review_your_rating))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { index ->
                    val star = index + 1
                    IconButton(
                        onClick = {
                            userRating = star
                            onClearError()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { selected = userRating == star }
                    ) {
                        Icon(
                            imageVector = if (star <= userRating) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = stringResource(MR.strings.review_rate_star, star),
                            tint = if (star <=
                                userRating
                            ) {
                                CocktailCraftTheme.extendedColors.rating
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            if (showFieldErrors && userRating == 0) {
                ErrorText(stringResource(MR.strings.review_select_rating_error))
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Comment, capped like the shared rule
            FieldLabel(stringResource(MR.strings.review_your_review))
            OutlinedTextField(
                value = userComment,
                onValueChange = {
                    if (it.length <= MAX_COMMENT_LENGTH) {
                        userComment = it
                        onClearError()
                    }
                },
                placeholder = {
                    Text(
                        stringResource(MR.strings.review_share_experience_placeholder),
                        color = MaterialTheme.colorScheme.outline
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = reviewFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Text(
                text = stringResource(MR.strings.review_character_count, userComment.length, MAX_COMMENT_LENGTH),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xs)
            )

            // Rejection from the shared ViewModel (comment rules or save failure)
            submissionError?.let {
                Spacer(modifier = Modifier.height(Spacing.sm))
                ErrorText(it)
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            PrimaryButton(
                text = stringResource(MR.strings.review_submit),
                isLoading = isSubmitting,
                onClick = {
                    if (userName.isBlank() || userRating == 0) {
                        showFieldErrors = true
                    } else if (!isSubmitting) {
                        showFieldErrors = false
                        isSubmitting = true
                        scope.launch {
                            val saved = onSubmit(userName, userRating.toFloat(), userComment)
                            isSubmitting = false
                            if (saved) animateClose()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = Spacing.sm)
    )
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp,
        modifier = Modifier
            .padding(top = Spacing.xs)
            // Announced when validation fails on submit
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
}

@Composable
private fun reviewFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = CocktailCraftTheme.extendedColors.neutralSubtle,
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)

// Mirrors the shared review rule (ManageReviewsUseCase.validateReview)
private const val MAX_COMMENT_LENGTH = 500
