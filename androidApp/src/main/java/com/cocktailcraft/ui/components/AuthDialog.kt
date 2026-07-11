package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cocktailcraft.ui.theme.AppColors

/**
 * The type of authentication dialog to display.
 */
enum class AuthDialogType {
    SIGN_IN, SIGN_UP
}

/**
 * A reusable authentication dialog component that can be used for sign-in or sign-up.
 *
 * @param type The type of authentication dialog to display
 * @param onDismiss The callback for when the dialog is dismissed
 * @param onSubmit The callback for when the form is submitted
 * @param signInButtonText The text for the sign-in button
 * @param signUpButtonText The text for the sign-up button
 * @param cancelButtonText The text for the cancel button
 * @param nameLabel The label for the name field
 * @param emailLabel The label for the email field
 * @param passwordLabel The label for the password field
 */
@Composable
fun AuthDialog(
    type: AuthDialogType,
    onDismiss: () -> Unit,
    onSubmit: (name: String?, email: String, password: String) -> Unit,
    signInButtonText: String = "Sign In",
    signUpButtonText: String = "Create Account",
    cancelButtonText: String = "Cancel",
    nameLabel: String = "Name",
    emailLabel: String = "Email",
    passwordLabel: String = "Password"
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isSignUp = type == AuthDialogType.SIGN_UP
    val title = if (isSignUp) "Create Account" else "Sign In"
    val confirmButtonText = if (isSignUp) signUpButtonText else signInButtonText
    val isFormValid = if (isSignUp) {
        name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
    } else {
        email.isNotBlank() && password.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (isSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(nameLabel) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(emailLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(passwordLabel) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onSubmit(if (isSignUp) name else null, email, password) 
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary
                )
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(cancelButtonText)
            }
        }
    )
}
