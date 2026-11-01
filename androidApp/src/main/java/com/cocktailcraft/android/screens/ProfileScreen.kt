package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.screens.profile.AboutCard
import com.cocktailcraft.android.screens.profile.AccountSettingsCard
import com.cocktailcraft.android.screens.profile.AppSettingsCard
import com.cocktailcraft.android.screens.profile.ProfileHeaderCard
import com.cocktailcraft.android.ui.components.ChangePasswordDialog
import com.cocktailcraft.android.ui.components.EditProfileDialog
import com.cocktailcraft.android.ui.components.ErrorDialog
import com.cocktailcraft.android.ui.components.SignInDialog
import com.cocktailcraft.android.ui.components.SignUpDialog
import com.cocktailcraft.android.ui.components.UiEventToasts
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.cancel
import com.cocktailcraft.resources.logout
import com.cocktailcraft.resources.logout_confirm
import com.cocktailcraft.resources.profile_guest_email
import com.cocktailcraft.resources.profile_guest_user
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
