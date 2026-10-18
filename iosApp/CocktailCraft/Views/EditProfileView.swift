import SwiftUI

struct EditProfileView: View {
    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.appearance) private var appearance
    @State private var name: String
    @State private var email: String
    @State private var isLoading = false

    let onDismiss: () -> Void
    // Async so the button can hold isLoading across the real update call
    // (same contract as SignInView/SignUpView).
    let onSave: (String, String) async -> Void
    // Shared-validator bridge (ProfileViewModelSKIE) so the form gate matches
    // the policy updateProfile actually enforces.
    let isValidEmail: (String) -> Bool

    init(
        currentName: String,
        currentEmail: String,
        onDismiss: @escaping () -> Void,
        onSave: @escaping (String, String) async -> Void,
        isValidEmail: @escaping (String) -> Bool
    ) {
        _name = State(initialValue: currentName)
        _email = State(initialValue: currentEmail)
        self.onDismiss = onDismiss
        self.onSave = onSave
        self.isValidEmail = isValidEmail
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                // Header
                VStack(spacing: 8) {
                    Image(systemName: "person.crop.circle.badge.checkmark")
                        .font(.system(size: 60))
                        .foregroundColor(AppColors.primary(appearance))

                    Text(L(\.edit_profile_subtitle))
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(appearance))
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 32)

                // Form
                VStack(spacing: 16) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(L(\.auth_full_name))
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        TextField(L(\.auth_enter_full_name), text: $name)
                            .textFieldStyle(CustomTextFieldStyle())
                            .autocapitalization(.words)
                            .accessibilityLabel(L(\.auth_full_name_a11y))
                            .accessibilityIdentifier("editProfile.nameField")
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text(L(\.email))
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        TextField(L(\.auth_enter_email), text: $email)
                            .textFieldStyle(CustomTextFieldStyle())
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .accessibilityLabel(L(\.auth_email_address_a11y))
                            .accessibilityIdentifier("editProfile.emailField")
                    }
                }
                .padding(.horizontal)

                // Save Button
                PrimaryButton(
                    title: L(\.edit_profile_save_changes),
                    isLoading: isLoading,
                    isDisabled: !isFormValid,
                    action: {
                        guard !isLoading else { return }
                        isLoading = true
                        Task {
                            await onSave(name, email)
                            isLoading = false
                        }
                    }
                )
                .padding(.horizontal)
                .accessibilityLabel(L(\.edit_profile_save_changes_a11y))
                .accessibilityIdentifier("editProfile.saveButton")

                Spacer()
            }
            .background(AppColors.background(isDarkMode: isDarkMode))
            .brandedNavigationBar(title: L(\.edit_profile))
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(L(\.cancel)) {
                        onDismiss()
                    }
                    .accessibilityLabel(L(\.cancel))
                    .accessibilityIdentifier("editProfile.cancelButton")
                    .tint(.white)
                }
            }
        }
    }

    private var isFormValid: Bool {
        !name.isEmpty && isValidEmail(email)
    }
}
