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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.BuildConfig
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.ui.components.AccentColorPicker
import com.cocktailcraft.android.ui.components.AnimatedThemeToggleRow
import com.cocktailcraft.android.ui.components.PrimaryButton
import com.cocktailcraft.android.ui.components.SettingsItem
import com.cocktailcraft.android.ui.components.SignInDialog
import com.cocktailcraft.android.ui.components.SignUpDialog
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.accent_color
import com.cocktailcraft.resources.cancel
import com.cocktailcraft.resources.change_password
import com.cocktailcraft.resources.create_account
import com.cocktailcraft.resources.dark_mode
import com.cocktailcraft.resources.edit_profile
import com.cocktailcraft.resources.email_preferences
import com.cocktailcraft.resources.error
import com.cocktailcraft.resources.follow_system_theme
import com.cocktailcraft.resources.font_size
import com.cocktailcraft.resources.help_support
import com.cocktailcraft.resources.high_contrast
import com.cocktailcraft.resources.logout
import com.cocktailcraft.resources.logout_confirm
import com.cocktailcraft.resources.notification_settings
import com.cocktailcraft.resources.ok
import com.cocktailcraft.resources.order_history
import com.cocktailcraft.resources.profile_about
import com.cocktailcraft.resources.profile_about_app_name
import com.cocktailcraft.resources.profile_account_settings
import com.cocktailcraft.resources.profile_app_settings
import com.cocktailcraft.resources.profile_appearance
import com.cocktailcraft.resources.profile_controlled_by_system
import com.cocktailcraft.resources.profile_copyright
import com.cocktailcraft.resources.profile_guest_email
import com.cocktailcraft.resources.profile_guest_user
import com.cocktailcraft.resources.profile_sign_in_prompt
import com.cocktailcraft.resources.profile_toggle_off
import com.cocktailcraft.resources.profile_toggle_on
import com.cocktailcraft.resources.profile_version_format
import com.cocktailcraft.resources.reduced_motion
import com.cocktailcraft.resources.sign_in
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.SharedProfileViewModel
import com.cocktailcraft.viewmodel.SharedThemeViewModel
import com.cocktailcraft.viewmodel.state.ProfileUiState
import com.cocktailcraft.viewmodel.state.ThemeUiState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
    navigationManager: NavigationManager,
    profileViewModel: SharedProfileViewModel = koinInject(),
    themeViewModel: SharedThemeViewModel = koinInject()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Get user data from ViewModel
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val error by profileViewModel.error.collectAsStateWithLifecycle()

    // Get theme data from ThemeViewModel
    val themeState by themeViewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        profileState = profileState,
        themeState = themeState,
        error = error,
        accentOptions = themeViewModel.availableAccentColors,
        fontSizeOptions = themeViewModel.availableFontSizes,
        fontSizeLabel = { themeViewModel.getFontSizeDisplayName(it).toString(context) },
        onSignIn = { email, password -> scope.launch { profileViewModel.signIn(email, password) } },
        onSignUp = { name, email, password -> scope.launch { profileViewModel.signUp(name, email, password) } },
        onConfirmLogout = {
            // Navigate only after sign-out completes — leaving the
            // screen would cancel this composition-scoped coroutine.
            scope.launch {
                profileViewModel.signOut()
                navigationManager.navigateToHome()
            }
        },
        onOrderHistoryClick = { navigationManager.navigateToOrderList() },
        onSetFollowSystemTheme = { enabled -> scope.launch { themeViewModel.setFollowSystemTheme(enabled) } },
        onSetDarkMode = { enabled -> scope.launch { themeViewModel.setDarkMode(enabled) } },
        onSelectAccent = { scope.launch { themeViewModel.setAccentColor(it) } },
        onSelectFontSize = { scope.launch { themeViewModel.setFontSize(it) } },
        onSetHighContrast = { enabled -> scope.launch { themeViewModel.setHighContrast(enabled) } },
        onSetReducedMotion = { enabled -> scope.launch { themeViewModel.setReducedMotion(enabled) } },
        onDismissError = { profileViewModel.clearError() }
    )
}

/**
 * Stateless profile content: renders the profile and theme state and reports
 * every intent upward, so it can be previewed/screenshot-tested without
 * ViewModels. Owns only UI-local state (which dialog is showing).
 */
@Composable
internal fun ProfileContent(
    profileState: ProfileUiState,
    themeState: ThemeUiState,
    error: ErrorHandler.UserFriendlyError?,
    accentOptions: List<String>,
    fontSizeOptions: List<String>,
    fontSizeLabel: (String) -> String,
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, password: String) -> Unit,
    onConfirmLogout: () -> Unit,
    onOrderHistoryClick: () -> Unit,
    onSetFollowSystemTheme: (Boolean) -> Unit,
    onSetDarkMode: (Boolean) -> Unit,
    onSelectAccent: (String) -> Unit,
    onSelectFontSize: (String) -> Unit,
    onSetHighContrast: (Boolean) -> Unit,
    onSetReducedMotion: (Boolean) -> Unit,
    onDismissError: () -> Unit
) {
    val isLoading = profileState.isLoading
    val user = profileState.user
    val isLoggedIn = profileState.isLoggedIn
    val isDarkMode = themeState.isDarkMode
    val isSystemTheme = themeState.isSystemTheme

    // Dialog states
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showSignInDialog by rememberSaveable { mutableStateOf(false) }
    var showSignUpDialog by rememberSaveable { mutableStateOf(false) }

    // Sample profile data (in a real app, this would come from a ViewModel)
    val userName = user?.name ?: stringResource(MR.strings.profile_guest_user)
    val userEmail = user?.email ?: stringResource(MR.strings.profile_guest_email)

    // Sign in dialog state
    if (showSignInDialog) {
        SignInDialog(
            onDismiss = { showSignInDialog = false },
            onSignIn = { email, password ->
                onSignIn(email, password)
                showSignInDialog = false
            }
        )
    }

    // Sign up dialog state
    if (showSignUpDialog) {
        SignUpDialog(
            onDismiss = { showSignUpDialog = false },
            onSignUp = { name, email, password ->
                onSignUp(name, email, password)
                showSignUpDialog = false
            }
        )
    }

    // Logout dialog state
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(MR.strings.logout)) },
            text = { Text(stringResource(MR.strings.logout_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onConfirmLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(MR.strings.logout))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(stringResource(MR.strings.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
            onOrderHistoryClick = onOrderHistoryClick,
            onToggleFollowSystemTheme = { onSetFollowSystemTheme(!isSystemTheme) },
            onToggleDarkMode = {
                if (!isSystemTheme) onSetDarkMode(!isDarkMode)
            },
            onLogoutClick = { showLogoutDialog = true }
        )

        // Appearance / accessibility settings (accent, font size, contrast,
        // motion) — parity with the iOS Settings sheet
        AppearanceSettingsCard(
            accentColor = themeState.accentColor,
            accentOptions = accentOptions,
            fontSize = themeState.fontSize,
            fontSizeOptions = fontSizeOptions,
            fontSizeLabel = fontSizeLabel,
            isHighContrast = themeState.isHighContrast,
            isReducedMotion = themeState.isReducedMotion,
            onSelectAccent = onSelectAccent,
            onSelectFontSize = onSelectFontSize,
            onToggleHighContrast = { onSetHighContrast(!themeState.isHighContrast) },
            onToggleReducedMotion = { onSetReducedMotion(!themeState.isReducedMotion) }
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
            onDismissRequest = onDismissError,
            title = { Text(stringResource(MR.strings.error)) },
            text = { Text(errorInfo.message.resolve()) },
            confirmButton = {
                Button(
                    onClick = onDismissError,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(MR.strings.ok))
                }
            }
        )
    }
}

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
            // Profile picture
            Box(
                modifier = Modifier
                    .size(80.dp)
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

    OutlinedButton(
        onClick = onSignUpClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = Spacing.xs)
    ) {
        Text(stringResource(MR.strings.create_account))
    }
}

@Composable
internal fun AccountSettingsCard() {
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
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(MR.strings.profile_account_settings),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            SettingsItem(
                icon = Icons.Default.Person,
                title = stringResource(MR.strings.edit_profile),
                onClick = { /* Handle edit profile */ }
            )

            SettingsItem(
                icon = Icons.Default.Lock,
                title = stringResource(MR.strings.change_password),
                onClick = { /* Handle change password */ }
            )

            SettingsItem(
                icon = Icons.Default.Email,
                title = stringResource(MR.strings.email_preferences),
                onClick = { /* Handle email preferences */ }
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = stringResource(MR.strings.notification_settings),
                onClick = { /* Handle notification settings */ }
            )
        }
    }
}

@Composable
internal fun AppSettingsCard(
    isLoggedIn: Boolean,
    isDarkMode: Boolean,
    isSystemTheme: Boolean,
    onOrderHistoryClick: () -> Unit,
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(MR.strings.profile_app_settings),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            SettingsItem(
                icon = Icons.Default.DateRange,
                title = stringResource(MR.strings.order_history),
                onClick = onOrderHistoryClick
            )

            SettingsItem(
                icon = Icons.Default.Help,
                title = stringResource(MR.strings.help_support),
                onClick = { /* Handle help & support */ }
            )

            // Follow System Theme Toggle with animated switch
            AnimatedThemeToggleRow(
                title = stringResource(MR.strings.follow_system_theme),
                subtitle = if (isSystemTheme) stringResource(MR.strings.profile_toggle_on) else stringResource(MR.strings.profile_toggle_off),
                icon = Icons.Default.DateRange,
                isChecked = isSystemTheme,
                onToggle = onToggleFollowSystemTheme,
                modifier = Modifier.fillMaxWidth()
            )

            // Dark Mode Toggle with animated switch (only enabled if not following system theme)
            AnimatedThemeToggleRow(
                title = stringResource(MR.strings.dark_mode),
                subtitle = if (isSystemTheme)
                    stringResource(MR.strings.profile_controlled_by_system)
                else
                    if (isDarkMode) stringResource(MR.strings.profile_toggle_on) else stringResource(MR.strings.profile_toggle_off),
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
                    title = stringResource(MR.strings.logout),
                    onClick = onLogoutClick,
                    textColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
internal fun AppearanceSettingsCard(
    accentColor: String,
    accentOptions: List<String>,
    fontSize: String,
    fontSizeOptions: List<String>,
    fontSizeLabel: (String) -> String,
    isHighContrast: Boolean,
    isReducedMotion: Boolean,
    onSelectAccent: (String) -> Unit,
    onSelectFontSize: (String) -> Unit,
    onToggleHighContrast: () -> Unit,
    onToggleReducedMotion: () -> Unit
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
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(MR.strings.profile_appearance),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            // Accent color
            Text(
                text = stringResource(MR.strings.accent_color),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            AccentColorPicker(
                selected = accentColor,
                options = accentOptions,
                onSelect = onSelectAccent
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Font size
            Text(
                text = stringResource(MR.strings.font_size),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                fontSizeOptions.forEach { option ->
                    val selected = option.equals(fontSize, ignoreCase = true)
                    Text(
                        text = fontSizeLabel(option),
                        fontSize = 14.sp,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .background(
                                color = if (selected) MaterialTheme.colorScheme.primary else CocktailCraftTheme.extendedColors.neutralSubtle.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectFontSize(option) }
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                    )
                }
            }

            // High contrast
            AnimatedThemeToggleRow(
                title = stringResource(MR.strings.high_contrast),
                subtitle = if (isHighContrast) stringResource(MR.strings.profile_toggle_on) else stringResource(MR.strings.profile_toggle_off),
                icon = Icons.Default.Visibility,
                isChecked = isHighContrast,
                onToggle = onToggleHighContrast,
                modifier = Modifier.fillMaxWidth()
            )

            // Reduce motion
            AnimatedThemeToggleRow(
                title = stringResource(MR.strings.reduced_motion),
                subtitle = if (isReducedMotion) stringResource(MR.strings.profile_toggle_on) else stringResource(MR.strings.profile_toggle_off),
                icon = Icons.Default.Speed,
                isChecked = isReducedMotion,
                onToggle = onToggleReducedMotion,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(MR.strings.profile_about),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            Text(
                text = stringResource(MR.strings.profile_about_app_name),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = stringResource(MR.strings.profile_version_format, BuildConfig.VERSION_NAME),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = stringResource(MR.strings.profile_copyright),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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