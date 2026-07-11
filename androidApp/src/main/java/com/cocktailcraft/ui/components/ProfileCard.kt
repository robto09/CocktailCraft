package com.cocktailcraft.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable profile card component that displays user information and sign-in/sign-up buttons if not signed in.
 *
 * @param userName The name of the user
 * @param userEmail The email of the user
 * @param isSignedIn Whether the user is signed in
 * @param onSignIn The callback for when the sign-in button is clicked
 * @param onSignUp The callback for when the sign-up button is clicked
 * @param modifier The modifier for the component
 * @param avatarSize The size of the avatar
 * @param avatarBackgroundColor The background color of the avatar
 * @param avatarTextColor The color of the avatar text
 * @param nameTextSize The font size of the name text
 * @param emailTextSize The font size of the email text
 * @param signInButtonText The text for the sign-in button
 * @param signUpButtonText The text for the sign-up button
 * @param notSignedInMessage The message to display when not signed in
 */
@Composable
fun ProfileCard(
    userName: String,
    userEmail: String,
    isSignedIn: Boolean,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: Int = 80,
    avatarBackgroundColor: androidx.compose.ui.graphics.Color = AppColors.Primary.copy(alpha = 0.2f),
    avatarTextColor: androidx.compose.ui.graphics.Color = AppColors.Primary,
    nameTextSize: Int = 20,
    emailTextSize: Int = 14,
    signInButtonText: String = "Sign In",
    signUpButtonText: String = "Create Account",
    notSignedInMessage: String = "Sign in to access your profile"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture
            Box(
                modifier = Modifier
                    .size(avatarSize.dp)
                    .background(avatarBackgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    fontSize = (avatarSize / 2.5).sp,
                    fontWeight = FontWeight.Bold,
                    color = avatarTextColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User name
            Text(
                text = userName,
                fontSize = nameTextSize.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // User email
            Text(
                text = userEmail,
                fontSize = emailTextSize.sp,
                color = AppColors.TextSecondary
            )

            // Show login/signup buttons if not signed in
            if (!isSignedIn) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = notSignedInMessage,
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = onSignIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 4.dp)
                ) {
                    Text(signInButtonText)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onSignUp,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 4.dp)
                ) {
                    Text(signUpButtonText)
                }
            }
        }
    }
}
