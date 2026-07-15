import SwiftUI

struct ChangePasswordView: View {
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
                        .foregroundColor(.blue)

                    Text("Change Password")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)

                    Text("Enter your current password and choose a new one.")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 32)

                // Form
                VStack(spacing: 16) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Current Password")
                            .font(.headline)
                            .foregroundColor(.primary)

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
                                    .foregroundColor(.gray)
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
                            .foregroundColor(.primary)

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
                                    .foregroundColor(.gray)
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
                Button(action: {
                    guard !isLoading else { return }
                    isLoading = true
                    Task {
                        await onChangePassword(currentPassword, newPassword)
                        isLoading = false
                    }
                }) {
                    HStack {
                        if isLoading {
                            ProgressView()
                                .scaleEffect(0.8)
                                .foregroundColor(.white)
                        }

                        Text("Change Password")
                            .font(.headline)
                            .fontWeight(.medium)
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(isFormValid ? .blue : .gray)
                    .cornerRadius(8)
                }
                .disabled(!isFormValid || isLoading)
                .padding(.horizontal)
                .accessibilityLabel("Change password")
                .accessibilityIdentifier("changePassword.submitButton")

                Spacer()
            }
            .background(Color(.systemBackground))
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
