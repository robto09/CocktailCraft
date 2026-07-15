import SwiftUI

struct ChangePasswordView: View {
    @Environment(\.isDarkMode) private var isDarkMode
    @State private var currentPassword = ""
    @State private var newPassword = ""
    @State private var isCurrentPasswordVisible = false
    @State private var isNewPasswordVisible = false
    @State private var isLoading = false

    let onDismiss: () -> Void
    // Async so the button can hold isLoading across the real change-password
    // call (same contract as SignInView/SignUpView).
    let onChangePassword: (String, String) async -> Void
    // Shared-validator bridges (ProfileViewModelSKIE) so the form gate and
    // strength meter can never drift from the policy changePassword enforces.
    let getPasswordStrength: (String) -> Int
    let meetsPasswordPolicy: (String) -> Bool

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                // Header
                VStack(spacing: 8) {
                    Image(systemName: "lock.rotation")
                        .font(.system(size: 60))
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))

                    Text("Enter your current password and choose a new one.")
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 32)

                // Form
                VStack(spacing: 16) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Current Password")
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        HStack {
                            if isCurrentPasswordVisible {
                                TextField("Enter your current password", text: $currentPassword)
                                    .accessibilityLabel("Current password")
                                    .accessibilityIdentifier("changePassword.currentField")
                            } else {
                                SecureField("Enter your current password", text: $currentPassword)
                                    .accessibilityLabel("Current password")
                                    .accessibilityIdentifier("changePassword.currentField")
                            }

                            Button(action: {
                                isCurrentPasswordVisible.toggle()
                            }) {
                                Image(systemName: isCurrentPasswordVisible ? "eye.slash" : "eye")
                                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                                    .minimumHitTarget()
                            }
                            .accessibilityLabel(isCurrentPasswordVisible ? "Hide password" : "Show password")
                            .accessibilityIdentifier("changePassword.toggleCurrentVisibility")
                        }
                        .textFieldStyle(CustomTextFieldStyle())
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("New Password")
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        HStack {
                            if isNewPasswordVisible {
                                TextField("Enter your new password", text: $newPassword)
                                    .accessibilityLabel("New password")
                                    .accessibilityIdentifier("changePassword.newField")
                            } else {
                                SecureField("Enter your new password", text: $newPassword)
                                    .accessibilityLabel("New password")
                                    .accessibilityIdentifier("changePassword.newField")
                            }

                            Button(action: {
                                isNewPasswordVisible.toggle()
                            }) {
                                Image(systemName: isNewPasswordVisible ? "eye.slash" : "eye")
                                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                                    .minimumHitTarget()
                            }
                            .accessibilityLabel(isNewPasswordVisible ? "Hide password" : "Show password")
                            .accessibilityIdentifier("changePassword.toggleNewVisibility")
                        }
                        .textFieldStyle(CustomTextFieldStyle())

                        if !newPassword.isEmpty {
                            PasswordStrengthMeter(strength: getPasswordStrength(newPassword))
                                .padding(.top, 4)
                        }
                    }
                }
                .padding(.horizontal)

                // Change Password Button
                PrimaryButton(
                    title: String(localized: "Change Password"),
                    isLoading: isLoading,
                    isDisabled: !isFormValid,
                    action: {
                        guard !isLoading else { return }
                        isLoading = true
                        Task {
                            await onChangePassword(currentPassword, newPassword)
                            isLoading = false
                        }
                    }
                )
                .padding(.horizontal)
                .accessibilityLabel("Change password")
                .accessibilityIdentifier("changePassword.submitButton")

                Spacer()
            }
            .background(AppColors.background(isDarkMode: isDarkMode))
            .navigationTitle("Change Password")
            .brandedNavigationBar()
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        onDismiss()
                    }
                    .accessibilityLabel("Cancel")
                    .accessibilityIdentifier("changePassword.cancelButton")
                    .tint(.white)
                }
            }
        }
    }

    private var isFormValid: Bool {
        !currentPassword.isEmpty && meetsPasswordPolicy(newPassword) && newPassword != currentPassword
    }
}
