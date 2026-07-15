import SwiftUI

struct EditProfileView: View {
    @Environment(\.isDarkMode) private var isDarkMode
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
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))

                    Text("Update your name and email address.")
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 32)

                // Form
                VStack(spacing: 16) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Full Name")
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        TextField("Enter your full name", text: $name)
                            .textFieldStyle(CustomTextFieldStyle())
                            .autocapitalization(.words)
                            .accessibilityLabel("Full name")
                            .accessibilityIdentifier("editProfile.nameField")
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Email")
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                        TextField("Enter your email", text: $email)
                            .textFieldStyle(CustomTextFieldStyle())
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .accessibilityLabel("Email address")
                            .accessibilityIdentifier("editProfile.emailField")
                    }
                }
                .padding(.horizontal)

                // Save Button
                Button(action: {
                    guard !isLoading else { return }
                    isLoading = true
                    Task {
                        await onSave(name, email)
                        isLoading = false
                    }
                }) {
                    HStack {
                        if isLoading {
                            ProgressView()
                                .scaleEffect(0.8)
                                .foregroundColor(.white)
                        }

                        Text("Save Changes")
                            .font(.headline)
                            .fontWeight(.medium)
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(isFormValid ? AppColors.primary(isDarkMode: isDarkMode) : AppColors.gray)
                    .cornerRadius(8)
                }
                .disabled(!isFormValid || isLoading)
                .padding(.horizontal)
                .accessibilityLabel("Save changes")
                .accessibilityIdentifier("editProfile.saveButton")

                Spacer()
            }
            .background(AppColors.background(isDarkMode: isDarkMode))
            .navigationTitle("Edit Profile")
            .brandedNavigationBar()
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        onDismiss()
                    }
                    .accessibilityLabel("Cancel")
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
