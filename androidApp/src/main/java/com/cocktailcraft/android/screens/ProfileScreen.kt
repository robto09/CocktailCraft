package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.BuildConfig
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.ui.components.AnimatedThemeToggleRow
import com.cocktailcraft.android.ui.components.ChangePasswordDialog
import com.cocktailcraft.android.ui.components.EditProfileDialog
import com.cocktailcraft.android.ui.components.ErrorDialog
import com.cocktailcraft.android.ui.components.PrimaryButton
import com.cocktailcraft.android.ui.components.SettingsItem
import com.cocktailcraft.android.ui.components.SignInDialog
import com.cocktailcraft.android.ui.components.SignUpDialog
import com.cocktailcraft.android.ui.components.UiEventToasts
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.cancel
import com.cocktailcraft.resources.change_password
import com.cocktailcraft.resources.create_account
import com.cocktailcraft.resources.dark_mode
import com.cocktailcraft.resources.edit_profile
import com.cocktailcraft.resources.email_preferences
import com.cocktailcraft.resources.follow_system_theme
import com.cocktailcraft.resources.help_support
import com.cocktailcraft.resources.logout
import com.cocktailcraft.resources.logout_confirm
import com.cocktailcraft.resources.notification_settings
import com.cocktailcraft.resources.order_history
import com.cocktailcraft.resources.profile_about
import com.cocktailcraft.resources.profile_about_app_name
import com.cocktailcraft.resources.profile_account_settings
import com.cocktailcraft.resources.profile_app_settings
import com.cocktailcraft.resources.profile_controlled_by_system
import com.cocktailcraft.resources.profile_copyright
import com.cocktailcraft.resources.profile_guest_email
import com.cocktailcraft.resources.profile_guest_user
import com.cocktailcraft.resources.profile_sign_in_prompt
import com.cocktailcraft.resources.profile_toggle_off
import com.cocktailcraft.resources.profile_toggle_on
import com.cocktailcraft.resources.profile_version_format
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

    // Get user data from ViewModel
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val error by profileViewModel.error.collectAsStateWithLifecycle()

    // Get theme data from ThemeViewModel
    val themeState by themeViewModel.uiState.collectAsStateWithLifecycle()

    // One-off outcomes, e.g. "Your password has been updated successfully"
    UiEventToasts(profileViewModel.events)

    ProfileContent(
        profileState = profileState,
        themeState = themeState,
        error = error,
        onSignIn = { email, password -> profileViewModel.signIn(email, password) },
        onSignUp = { name, email, password -> profileViewModel.signUp(name, email, password) },
        onUpdateProfile = { name, email -> profileViewModel.updateProfile(name, email) },
        onChangePassword = { current, new -> profileViewModel.changePassword(current, new) },
        isValidEmail = profileViewModel::isValidEmail,
        meetsPasswordPolicy = profileViewModel::meetsPasswordPolicy,
        onConfirmLogout = {
            // Navigate only after sign-out completes (the sign-out itself
            // runs on the profile ViewModel's scope)
            scope.launch {
                profileViewModel.signOut()
                navigationManager.navigateToHome()
            }
        },
        onOrderHistoryClick = { navigationManager.navigateToOrderList() },
        onSetFollowSystemTheme = { enabled -> themeViewModel.setFollowSystemTheme(enabled) },
        onSetDarkMode = { enabled -> themeViewModel.setDarkMode(enabled) },
        onDismissError = { profileViewModel.clearError() }
    )
}

/**
 * Stateless profile content: renders the profile and theme state and reports
 * every intent upward, so it can be previewed/screenshot-tested without
 * ViewModels. Owns only UI-local state (which dialog is showing, whether its
 * request is in flight). The form actions return whether they succeeded: a
 * form dialog closes only on success and otherwise shows [error] inline.
 */
@Composable
internal fun ProfileContent(
    profileState: ProfileUiState,
    themeState: ThemeUiState,
    error: ErrorHandler.UserFriendlyError?,
    onSignIn: suspend (email: String, password: String) -> Boolean,
    onSignUp: suspend (name: String, email: String, password: String) -> Boolean,
    onUpdateProfile: suspend (name: String, email: String) -> Boolean,
    onChangePassword: suspend (currentPassword: String, newPassword: String) -> Boolean,
    isValidEmail: (String) -> Boolean,
    meetsPasswordPolicy: (String) -> Boolean,
    onConfirmLogout: () -> Unit,
    onOrderHistoryClick: () -> Unit,
    onSetFollowSystemTheme: (Boolean) -> Unit,
    onSetDarkMode: (Boolean) -> Unit,
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
    var showEditProfileDialog by rememberSaveable { mutableStateOf(false) }
    var showChangePasswordDialog by rememberSaveable { mutableStateOf(false) }

    // Form submission: the dialog stays open until the result is in. The
    // scope outlives the dialog, so dismissing it mid-request is safe.
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    val submit: (suspend () -> Boolean, () -> Unit) -> Unit = { action, onSuccess ->
        if (!isSubmitting) {
            isSubmitting = true
            scope.launch {
                val succeeded = try {
                    action()
                } finally {
                    isSubmitting = false
                }
                if (succeeded) onSuccess()
            }
        }
    }
    // While a form is open its failure shows inside it, not as a second dialog
    val isFormOpen = showSignInDialog || showSignUpDialog || showEditProfileDialog || showChangePasswordDialog
    val formError = if (isFormOpen) error?.message?.resolve() else null
    val dismissForm: () -> Unit = {
        showSignInDialog = false
        showSignUpDialog = false
        showEditProfileDialog = false
        showChangePasswordDialog = false
        onDismissError()
    }

    // Sample profile data (in a real app, this would come from a ViewModel)
    val userName = user?.name ?: stringResource(MR.strings.profile_guest_user)
    val userEmail = user?.email ?: stringResource(MR.strings.profile_guest_email)

    // Sign in dialog state
    if (showSignInDialog) {
        SignInDialog(
            onDismiss = dismissForm,
            onSignIn = { email, password ->
                submit({ onSignIn(email, password) }) { showSignInDialog = false }
            },
            isSubmitting = isSubmitting,
            errorMessage = formError
        )
    }

    // Sign up dialog state
    if (showSignUpDialog) {
        SignUpDialog(
            onDismiss = dismissForm,
            onSignUp = { name, email, password ->
                submit({ onSignUp(name, email, password) }) { showSignUpDialog = false }
            },
            isSubmitting = isSubmitting,
            errorMessage = formError
        )
    }

    // Edit profile (parity with iOS's EditProfileView)
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = user?.name.orEmpty(),
            currentEmail = user?.email.orEmpty(),
            isValidEmail = isValidEmail,
            onDismiss = dismissForm,
            onSave = { name, email ->
                submit({ onUpdateProfile(name, email) }) { showEditProfileDialog = false }
            },
            isSubmitting = isSubmitting,
            errorMessage = formError
        )
    }

    // Change password (parity with iOS's ChangePasswordView); success is
    // confirmed by a toast from the ViewModel's events
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            meetsPasswordPolicy = meetsPasswordPolicy,
            onDismiss = dismissForm,
            onChangePassword = { current, new ->
                submit({ onChangePassword(current, new) }) { showChangePasswordDialog = false }
            },
            isSubmitting = isSubmitting,
            errorMessage = formError
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
            AccountSettingsCard(
                onEditProfileClick = { showEditProfileDialog = true },
                onChangePasswordClick = { showChangePasswordDialog = true }
            )
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

        // App information
        AboutCard()
    }

    // Show loading indicator if needed
    if (isLoading) {
        LoadingOverlay()
    }

    // Failures outside a form (e.g. sign-out) in the shared error dialog
    if (!isFormOpen) {
        error?.let { errorInfo ->
            ErrorDialog(error = errorInfo, onDismiss = onDismissError)
        }
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

@Composable
internal fun AccountSettingsCard(
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {}
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
                text = stringResource(MR.strings.profile_account_settings),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = Spacing.lg)
                    .semantics { heading() }
            )

            SettingsItem(
                icon = Icons.Default.Person,
                title = stringResource(MR.strings.edit_profile),
                onClick = onEditProfileClick
            )

            SettingsItem(
                icon = Icons.Default.Lock,
                title = stringResource(MR.strings.change_password),
                onClick = onChangePasswordClick
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
                modifier = Modifier
                    .padding(bottom = Spacing.lg)
                    .semantics { heading() }
            )

            SettingsItem(
                icon = Icons.Default.DateRange,
                title = stringResource(MR.strings.order_history),
                onClick = onOrderHistoryClick
            )

            SettingsItem(
                icon = Icons.AutoMirrored.Filled.Help,
                title = stringResource(MR.strings.help_support),
                onClick = { /* Handle help & support */ }
            )

            // Follow System Theme Toggle with animated switch
            AnimatedThemeToggleRow(
                title = stringResource(MR.strings.follow_system_theme),
                subtitle = if (isSystemTheme) {
                    stringResource(
                        MR.strings.profile_toggle_on
                    )
                } else {
                    stringResource(MR.strings.profile_toggle_off)
                },
                icon = Icons.Default.DateRange,
                isChecked = isSystemTheme,
                onToggle = onToggleFollowSystemTheme,
                modifier = Modifier.fillMaxWidth()
            )

            // Dark Mode Toggle with animated switch (only enabled if not following system theme)
            AnimatedThemeToggleRow(
                title = stringResource(MR.strings.dark_mode),
                subtitle = if (isSystemTheme) {
                    stringResource(MR.strings.profile_controlled_by_system)
                } else {
                    if (isDarkMode) {
                        stringResource(
                            MR.strings.profile_toggle_on
                        )
                    } else {
                        stringResource(MR.strings.profile_toggle_off)
                    }
                },
                icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                isChecked = isDarkMode,
                onToggle = onToggleDarkMode,
                enabled = !isSystemTheme,
                modifier = Modifier.fillMaxWidth()
            )

            // Only show logout if signed in
            if (isLoggedIn) {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = stringResource(MR.strings.logout),
                    onClick = onLogoutClick,
                    textColor = MaterialTheme.colorScheme.error
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
                modifier = Modifier
                    .padding(bottom = Spacing.lg)
                    .semantics { heading() }
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