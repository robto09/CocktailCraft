package com.cocktailcraft.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.viewmodel.ReviewViewModel
import androidx.compose.ui.graphics.Color

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
                    text = "Write a Review",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
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
                            text = "Your Name",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = userName,
                            onValueChange = {
                                userName = it
                                hasError = false
                            },
                            placeholder = { Text("Enter your name", color = AppColors.Gray) },
                            isError = hasError && userName.isBlank(),
                            supportingText = {
                                if (hasError && userName.isBlank()) {
                                    Text(
                                        text = "Name is required",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.LightGray,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                containerColor = AppColors.Surface
                            )
                        )
                    }

                    // Rating section with enhanced UI
                    Column {
                        Text(
                            text = "Your Rating",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.Surface, RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(5) { index ->
                                IconButton(
                                    onClick = { userRating = index + 1f },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Rate ${index + 1}",
                                        tint = if (index < userRating) AppColors.Secondary else AppColors.LightGray,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        if (hasError && userRating == 0f) {
                            Text(
                                text = "Please select a rating",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Review text with character count
                    Column {
                        Text(
                            text = "Your Review",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = userComment,
                            onValueChange = {
                                if (it.length <= 500) {
                                    userComment = it
                                }
                            },
                            placeholder = { Text("Share your experience (Optional)", color = AppColors.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.LightGray,
                                containerColor = AppColors.Surface
                            )
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${userComment.length}/500",
                                fontSize = 12.sp,
                                color = AppColors.Gray,
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
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Submit Review",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, AppColors.Primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp
        )
    }
}