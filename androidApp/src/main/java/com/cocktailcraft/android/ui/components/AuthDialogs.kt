package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.cancel
import com.cocktailcraft.resources.change_password
import com.cocktailcraft.resources.change_password_current_label
import com.cocktailcraft.resources.change_password_new_label
import com.cocktailcraft.resources.change_password_subtitle
import com.cocktailcraft.resources.create_account
import com.cocktailcraft.resources.edit_profile
import com.cocktailcraft.resources.edit_profile_save_changes
import com.cocktailcraft.resources.edit_profile_subtitle
import com.cocktailcraft.resources.email
import com.cocktailcraft.resources.hide_password
import com.cocktailcraft.resources.name
import com.cocktailcraft.resources.password
import com.cocktailcraft.resources.password_hidden
import com.cocktailcraft.resources.password_shown
import com.cocktailcraft.resources.show_password
import com.cocktailcraft.resources.sign_in

/**
 * Sign-in, sign-up, edit-profile and change-password dialogs, extracted
 * from ProfileScreen. Form state is rememberSaveable so half-typed input
 * survives configuration changes. Each stays open while its request runs
 * ([isSubmitting] shows progress on the confirm button) and shows the
 * failure inline ([errorMessage]); the caller closes it on success.
 */
@Composable
fun SignInDialog(
    onDismiss: () -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(MR.strings.sign_in)) },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(MR.strings.email)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                PasswordField(
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )

                DialogErrorText(errorMessage)
            }
        },
        confirmButton = {
            SubmitButton(
                text = stringResource(MR.strings.sign_in),
                enabled = email.isNotBlank() && password.isNotBlank(),
                isSubmitting = isSubmitting,
                onClick = { onSignIn(email, password) }
            )
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(MR.strings.cancel))
            }
        }
    )
}

@Composable
fun SignUpDialog(
    onDismiss: () -> Unit,
    onSignUp: (name: String, email: String, password: String) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(MR.strings.create_account)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(MR.strings.name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(MR.strings.email)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                PasswordField(
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )

                DialogErrorText(errorMessage)
            }
        },
        confirmButton = {
            SubmitButton(
                text = stringResource(MR.strings.create_account),
                enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                isSubmitting = isSubmitting,
                onClick = { onSignUp(name, email, password) }
            )
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(MR.strings.cancel))
            }
        }
    )
}

/**
 * Edit name and email, prefilled with the current values. The form gate
 * uses the shared validator ([isValidEmail]), as iOS's EditProfileView does.
 */
@Composable
fun EditProfileDialog(
    currentName: String,
    currentEmail: String,
    isValidEmail: (String) -> Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    var name by rememberSaveable { mutableStateOf(currentName) }
    var email by rememberSaveable { mutableStateOf(currentEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(MR.strings.edit_profile)) },
        text = {
            Column {
                Text(
                    text = stringResource(MR.strings.edit_profile_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(MR.strings.name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(MR.strings.email)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                DialogErrorText(errorMessage)
            }
        },
        confirmButton = {
            SubmitButton(
                text = stringResource(MR.strings.edit_profile_save_changes),
                enabled = name.isNotBlank() && isValidEmail(email),
                isSubmitting = isSubmitting,
                onClick = { onSave(name, email) }
            )
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(MR.strings.cancel))
            }
        }
    )
}

/**
 * Current + new password. Like iOS's ChangePasswordView, the new one must
 * meet the shared policy ([meetsPasswordPolicy]) and differ from the current.
 */
@Composable
fun ChangePasswordDialog(
    meetsPasswordPolicy: (String) -> Boolean,
    onDismiss: () -> Unit,
    onChangePassword: (currentPassword: String, newPassword: String) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var currentVisible by rememberSaveable { mutableStateOf(false) }
    var newVisible by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(MR.strings.change_password)) },
        text = {
            Column {
                Text(
                    text = stringResource(MR.strings.change_password_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PasswordField(
                    password = currentPassword,
                    onPasswordChange = { currentPassword = it },
                    passwordVisible = currentVisible,
                    onToggleVisibility = { currentVisible = !currentVisible },
                    label = stringResource(MR.strings.change_password_current_label)
                )

                PasswordField(
                    password = newPassword,
                    onPasswordChange = { newPassword = it },
                    passwordVisible = newVisible,
                    onToggleVisibility = { newVisible = !newVisible },
                    label = stringResource(MR.strings.change_password_new_label)
                )

                DialogErrorText(errorMessage)
            }
        },
        confirmButton = {
            SubmitButton(
                text = stringResource(MR.strings.change_password),
                enabled = currentPassword.isNotEmpty() &&
                    meetsPasswordPolicy(newPassword) &&
                    newPassword != currentPassword,
                isSubmitting = isSubmitting,
                onClick = { onChangePassword(currentPassword, newPassword) }
            )
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(MR.strings.cancel))
            }
        }
    )
}

/** A dialog's inline failure message, announced politely to TalkBack. */
@Composable
private fun DialogErrorText(errorMessage: String?) {
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
    }
}

/** Confirm button that swaps its label for a spinner (and takes no taps) while submitting. */
@Composable
private fun SubmitButton(
    text: String,
    enabled: Boolean,
    isSubmitting: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isSubmitting,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isSubmitting) {
            // TalkBack still hears what the (busy, disabled) button is
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(20.dp)
                    .semantics { contentDescription = text }
            )
        } else {
            Text(text)
        }
    }
}

@Composable
private fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    label: String = stringResource(MR.strings.password)
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        trailingIcon = {
            // Announce purpose (action) and current state to TalkBack — an
            // icon-only toggle is otherwise just "button"
            val stateDesc = stringResource(
                if (passwordVisible) MR.strings.password_shown else MR.strings.password_hidden
            )
            IconButton(
                onClick = onToggleVisibility,
                modifier = Modifier.semantics { stateDescription = stateDesc }
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = stringResource(
                        if (passwordVisible) MR.strings.hide_password else MR.strings.show_password
                    )
                )
            }
        }
    )
}
