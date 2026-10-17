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

                    Text(L(\.change_password_subtitle))
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 32)

                // Form
                VStack(spacing: 16) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(L(\.change_password_current_label))
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        HStack {
                            if isCurrentPasswordVisible {
                                TextField(L(\.change_password_current_placeholder), text: $currentPassword)
                                    .accessibilityLabel(L(\.change_password_current_a11y))
                                    .accessibilityIdentifier("changePassword.currentField")
                            } else {
                                SecureField(L(\.change_password_current_placeholder), text: $currentPassword)
                                    .accessibilityLabel(L(\.change_password_current_a11y))
                                    .accessibilityIdentifier("changePassword.currentField")
                            }

                            Button(action: {
                                isCurrentPasswordVisible.toggle()
                            }) {
                                Image(systemName: isCurrentPasswordVisible ? "eye.slash" : "eye")
                                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                                    .minimumHitTarget()
                            }
                            .accessibilityLabel(isCurrentPasswordVisible ? L(\.hide_password) : L(\.show_password))
                            .accessibilityIdentifier("changePassword.toggleCurrentVisibility")
                        }
                        .textFieldStyle(CustomTextFieldStyle())
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text(L(\.change_password_new_label))
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        HStack {
                            if isNewPasswordVisible {
                                TextField(L(\.change_password_new_placeholder), text: $newPassword)
                                    .accessibilityLabel(L(\.change_password_new_a11y))
                                    .accessibilityIdentifier("changePassword.newField")
                            } else {
                                SecureField(L(\.change_password_new_placeholder), text: $newPassword)
                                    .accessibilityLabel(L(\.change_password_new_a11y))
                                    .accessibilityIdentifier("changePassword.newField")
                            }

                            Button(action: {
                                isNewPasswordVisible.toggle()
                            }) {
                                Image(systemName: isNewPasswordVisible ? "eye.slash" : "eye")
                                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                                    .minimumHitTarget()
                            }
                            .accessibilityLabel(isNewPasswordVisible ? L(\.hide_password) : L(\.show_password))
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
                    title: L(\.change_password),
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
                .accessibilityLabel(L(\.change_password_a11y))
                .accessibilityIdentifier("changePassword.submitButton")

                Spacer()
            }
            .background(AppColors.background(isDarkMode: isDarkMode))
            .navigationTitle(L(\.change_password))
            .brandedNavigationBar()
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(L(\.cancel)) {
                        onDismiss()
                    }
                    .accessibilityLabel(L(\.cancel))
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
