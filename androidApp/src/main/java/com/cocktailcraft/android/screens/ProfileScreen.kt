package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.ui.components.AnimatedThemeToggleRow
import com.cocktailcraft.android.BuildConfig
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.components.SignInDialog
import com.cocktailcraft.android.ui.components.SignUpDialog
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.viewmodel.SharedProfileViewModel
import com.cocktailcraft.viewmodel.SharedThemeViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
    navigationManager: NavigationManager,
    profileViewModel: SharedProfileViewModel = koinInject(),
    themeViewModel: SharedThemeViewModel = koinInject()
) {
    val scope = rememberCoroutineScope()

    // Get user data from ViewModel
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = profileState.isLoading
    val user = profileState.user
    val isLoggedIn = profileState.isLoggedIn
    val error by profileViewModel.error.collectAsStateWithLifecycle()

    // Get theme data from ThemeViewModel
    val themeState by themeViewModel.uiState.collectAsStateWithLifecycle()
    val isDarkMode = themeState.isDarkMode
    val isSystemTheme = themeState.isSystemTheme

    // Dialog states
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showSignInDialog by rememberSaveable { mutableStateOf(false) }
    var showSignUpDialog by rememberSaveable { mutableStateOf(false) }

    // Sample profile data (in a real app, this would come from a ViewModel)
    val userName = user?.name ?: stringResource(R.string.profile_guest_user)
    val userEmail = user?.email ?: stringResource(R.string.profile_guest_email)

    // Sign in dialog state
    if (showSignInDialog) {
        SignInDialog(
            onDismiss = { showSignInDialog = false },
            onSignIn = { email, password ->
                scope.launch { profileViewModel.signIn(email, password) }
                showSignInDialog = false
            }
        )
    }

    // Sign up dialog state
    if (showSignUpDialog) {
        SignUpDialog(
            onDismiss = { showSignUpDialog = false },
            onSignUp = { name, email, password ->
                scope.launch { profileViewModel.signUp(name, email, password) }
                showSignUpDialog = false
            }
        )
    }

    // Logout dialog state
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout)) },
            text = { Text(stringResource(R.string.logout_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        // Navigate only after sign-out completes — leaving the
                        // screen would cancel this composition-scoped coroutine.
                        scope.launch {
                            profileViewModel.signOut()
                            navigationManager.navigateToHome()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    )
                ) {
                    Text(stringResource(R.string.logout))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
    ) {
        // Profile header
        ProfileHeaderCard(
            userName = userName,
            userEmail = userEmail,
            isLoggedIn = isLoggedIn,
            onSignInClick = { showSignInDialog = true },
            onSignUpClick = { showSignUpDialog = true }
        )

        // Only show account settings and logout option if signed in
        if (isLoggedIn) {
            // Account settings
            AccountSettingsCard()
        }

        // App settings
        AppSettingsCard(
            isLoggedIn = isLoggedIn,
            isDarkMode = isDarkMode,
            isSystemTheme = isSystemTheme,
            onOrderHistoryClick = { navigationManager.navigateToOrderList() },
            onOfflineModeClick = { navigationManager.navigateToOfflineMode() },
            onToggleFollowSystemTheme = { scope.launch { themeViewModel.setFollowSystemTheme(!isSystemTheme) } },
            onToggleDarkMode = {
                if (!isSystemTheme) scope.launch { themeViewModel.setDarkMode(!isDarkMode) }
            },
            onLogoutClick = { showLogoutDialog = true }
        )

        // App information
        AboutCard()
    }

    // Show loading indicator if needed
    if (isLoading) {
        LoadingOverlay()
    }

    // Show error message if needed
    error?.let { errorInfo ->
        AlertDialog(
            onDismissRequest = { profileViewModel.clearError() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(errorInfo.message) },
            confirmButton = {
                Button(
                    onClick = { profileViewModel.clearError() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    )
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    textColor: Color = AppColors.TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(Spacing.lg))

        Text(
            text = title,
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = AppColors.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ProfileHeaderCard(
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
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.lg)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(AppColors.Primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // User name
            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // User email
            Text(
                text = userEmail,
                fontSize = 14.sp,
                color = AppColors.TextSecondary
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
        text = stringResource(R.string.profile_sign_in_prompt),
        fontSize = 16.sp,
        color = AppColors.TextSecondary,
        modifier = Modifier.padding(bottom = Spacing.lg)
    )

    Button(
        onClick = onSignInClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary
        ),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = Spacing.xs)
    ) {
        Text(stringResource(R.string.sign_in))
    }

    Spacer(modifier = Modifier.height(Spacing.sm))

    OutlinedButton(
        onClick = onSignUpClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = Spacing.xs)
    ) {
        Text(stringResource(R.string.create_account))
    }
}

@Composable
private fun AccountSettingsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.lg),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(R.string.profile_account_settings),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            SettingsItem(
                icon = Icons.Default.Person,
                title = stringResource(R.string.edit_profile),
                onClick = { /* Handle edit profile */ }
            )

            SettingsItem(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.change_password),
                onClick = { /* Handle change password */ }
            )

            SettingsItem(
                icon = Icons.Default.Email,
                title = stringResource(R.string.email_preferences),
                onClick = { /* Handle email preferences */ }
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.notification_settings),
                onClick = { /* Handle notification settings */ }
            )
        }
    }
}

@Composable
private fun AppSettingsCard(
    isLoggedIn: Boolean,
    isDarkMode: Boolean,
    isSystemTheme: Boolean,
    onOrderHistoryClick: () -> Unit,
    onOfflineModeClick: () -> Unit,
    onToggleFollowSystemTheme: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.lg),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(R.string.profile_app_settings),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            SettingsItem(
                icon = Icons.Default.DateRange,
                title = stringResource(R.string.order_history),
                onClick = onOrderHistoryClick
            )

            SettingsItem(
                icon = Icons.Default.Help,
                title = stringResource(R.string.help_support),
                onClick = { /* Handle help & support */ }
            )

            SettingsItem(
                icon = Icons.Default.CloudOff,
                title = stringResource(R.string.offline_mode),
                onClick = onOfflineModeClick
            )

            // Follow System Theme Toggle with animated switch
            AnimatedThemeToggleRow(
                title = stringResource(R.string.follow_system_theme),
                subtitle = if (isSystemTheme) stringResource(R.string.profile_toggle_on) else stringResource(R.string.profile_toggle_off),
                icon = Icons.Default.DateRange,
                isChecked = isSystemTheme,
                onToggle = onToggleFollowSystemTheme,
                modifier = Modifier.fillMaxWidth()
            )

            // Dark Mode Toggle with animated switch (only enabled if not following system theme)
            AnimatedThemeToggleRow(
                title = stringResource(R.string.dark_mode),
                subtitle = if (isSystemTheme)
                    stringResource(R.string.profile_controlled_by_system)
                else
                    if (isDarkMode) stringResource(R.string.profile_toggle_on) else stringResource(R.string.profile_toggle_off),
                icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                isChecked = isDarkMode,
                onToggle = onToggleDarkMode,
                enabled = !isSystemTheme,
                modifier = Modifier.fillMaxWidth()
            )

            // Only show logout if signed in
            if (isLoggedIn) {
                SettingsItem(
                    icon = Icons.Default.ExitToApp,
                    title = stringResource(R.string.logout),
                    onClick = onLogoutClick,
                    textColor = AppColors.Error
                )
            }
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(R.string.profile_about),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            Text(
                text = stringResource(R.string.profile_about_app_name),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = stringResource(R.string.profile_version_format, BuildConfig.VERSION_NAME),
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = stringResource(R.string.profile_copyright),
                fontSize = 12.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AppColors.Primary)
    }
}

// ---- Design-time previews ----

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

@Preview(name = "Account settings", showBackground = true)
@Composable
private fun AccountSettingsCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        AccountSettingsCard()
    }
}

@Preview(name = "App settings", showBackground = true)
@Composable
private fun AppSettingsCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        AppSettingsCard(
            isLoggedIn = true,
            isDarkMode = false,
            isSystemTheme = false,
            onOrderHistoryClick = {},
            onOfflineModeClick = {},
            onToggleFollowSystemTheme = {},
            onToggleDarkMode = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "App settings (dark)", showBackground = true)
@Composable
private fun AppSettingsCardDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        AppSettingsCard(
            isLoggedIn = true,
            isDarkMode = true,
            isSystemTheme = false,
            onOrderHistoryClick = {},
            onOfflineModeClick = {},
            onToggleFollowSystemTheme = {},
            onToggleDarkMode = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "App settings — large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun AppSettingsCardLargeFontPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        AppSettingsCard(
            isLoggedIn = true,
            isDarkMode = false,
            isSystemTheme = true,
            onOrderHistoryClick = {},
            onOfflineModeClick = {},
            onToggleFollowSystemTheme = {},
            onToggleDarkMode = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "About", showBackground = true)
@Composable
private fun AboutCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        AboutCard()
    }
}