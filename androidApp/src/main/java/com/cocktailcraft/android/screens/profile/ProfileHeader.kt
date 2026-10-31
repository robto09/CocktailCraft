package com.cocktailcraft.android.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.components.PrimaryButton
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.create_account
import com.cocktailcraft.resources.profile_sign_in_prompt
import com.cocktailcraft.resources.sign_in

// The Profile screen's header: the signed-in user's card, or the sign-in
// prompt for a guest.

@Composable
internal fun ProfileHeaderCard(
    userName: String,
    userEmail: String,
    isLoggedIn: Boolean,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.lg),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.lg)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture (decorative initial: the name follows)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clearAndSetSemantics {}
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // User name
            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // User email
            Text(
                text = userEmail,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Show login/signup buttons if not signed in
            if (!isLoggedIn) {
                SignInPromptSection(
                    onSignInClick = onSignInClick,
                    onSignUpClick = onSignUpClick
                )
            }
        }
    }
}

@Composable
private fun SignInPromptSection(
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(Spacing.xxl))

    Text(
        text = stringResource(MR.strings.profile_sign_in_prompt),
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = Spacing.lg)
    )

    PrimaryButton(
        text = stringResource(MR.strings.sign_in),
        onClick = onSignInClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = Spacing.xs)
    )

    Spacer(modifier = Modifier.height(Spacing.sm))

    // Same shape and height as Sign In (PrimaryButton), not the
    // OutlinedButton default pill
    OutlinedButton(
        onClick = onSignUpClick,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = Spacing.xs)
            .heightIn(min = 54.dp)
    ) {
        // Primary-colored bold label like iOS, same type as Sign In
        Text(
            text = stringResource(MR.strings.create_account),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(name = "Profile header — logged out", showBackground = true)
@Composable
private fun ProfileHeaderCardLoggedOutPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        ProfileHeaderCard(
            userName = "Guest User",
            userEmail = "guest@example.com",
            isLoggedIn = false,
            onSignInClick = {},
            onSignUpClick = {}
        )
    }
}

@Preview(name = "Profile header — logged out (dark)", showBackground = true)
@Composable
private fun ProfileHeaderCardLoggedOutDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        ProfileHeaderCard(
            userName = "Guest User",
            userEmail = "guest@example.com",
            isLoggedIn = false,
            onSignInClick = {},
            onSignUpClick = {}
        )
    }
}

@Preview(name = "Profile header — logged in", showBackground = true)
@Composable
private fun ProfileHeaderCardLoggedInPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        ProfileHeaderCard(
            userName = "Alex",
            userEmail = "alex@example.com",
            isLoggedIn = true,
            onSignInClick = {},
            onSignUpClick = {}
        )
    }
}
