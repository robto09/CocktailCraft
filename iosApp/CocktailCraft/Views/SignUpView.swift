import SwiftUI

struct SignUpView: View {
    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.appearance) private var appearance
    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var isPasswordVisible = false
    @State private var isLoading = false
    
    let onDismiss: () -> Void
    // Async so the button can hold isLoading across the real network call;
    // a fire-and-forget closure made the spinner and double-tap guard fake.
    let onSignUp: (String, String, String) async -> Void
    // Shared-validator bridges (ProfileViewModelSKIE) so the form gate and
    // strength meter can never drift from the policy signUp actually enforces.
    let getPasswordStrength: (String) -> Int
    let meetsPasswordPolicy: (String) -> Bool
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                // Header
                VStack(spacing: 8) {
                    Image(systemName: "person.badge.plus")
                        .font(.system(size: 60))
                        .foregroundColor(AppColors.primary(appearance))

                    Text(L(\.sign_up_subtitle))
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(appearance))
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 32)
                
                // Form
                VStack(spacing: 16) {
                    // Name Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text(L(\.auth_full_name))
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        
                        TextField(L(\.auth_enter_full_name), text: $name)
                            .textFieldStyle(CustomTextFieldStyle())
                            .autocapitalization(.words)
                            .accessibilityLabel(L(\.auth_full_name_a11y))
                            .accessibilityIdentifier("signup.nameField")
                    }
                    
                    // Email Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text(L(\.email))
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        
                        TextField(L(\.auth_enter_email), text: $email)
                            .textFieldStyle(CustomTextFieldStyle())
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .accessibilityLabel(L(\.email))
                            .accessibilityIdentifier("signup.emailField")
                    }
                    
                    // Password Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text(L(\.password))
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        
                        HStack {
                            if isPasswordVisible {
                                TextField(L(\.auth_enter_password), text: $password)
                                    .accessibilityLabel(L(\.password))
                                    .accessibilityIdentifier("signup.passwordField")
                            } else {
                                SecureField(L(\.auth_enter_password), text: $password)
                                    .accessibilityLabel(L(\.password))
                                    .accessibilityIdentifier("signup.passwordField")
                            }

                            Button(action: {
                                isPasswordVisible.toggle()
                            }) {
                                Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                                    .foregroundColor(AppColors.textSecondary(appearance))
                                    .minimumHitTarget()
                            }
                            .accessibilityLabel(isPasswordVisible ? L(\.hide_password) : L(\.show_password))
                            .accessibilityIdentifier("signup.togglePasswordVisibility")
                        }
                        .textFieldStyle(CustomTextFieldStyle())
                        
                        if !password.isEmpty {
                            PasswordStrengthMeter(strength: getPasswordStrength(password))
                                .padding(.top, 4)
                        }
                    }
                }
                .padding(.horizontal)
                
                // Create Account Button
                PrimaryButton(
                    title: L(\.create_account),
                    isLoading: isLoading,
                    isDisabled: !isFormValid,
                    action: {
                        guard !isLoading else { return }
                        isLoading = true
                        Task {
                            await onSignUp(name, email, password)
                            isLoading = false
                        }
                    }
                )
                .padding(.horizontal)
                .accessibilityLabel(L(\.create_account))
                .accessibilityIdentifier("signup.submitButton")
                
                Spacer()
            }
            .background(AppColors.background(isDarkMode: isDarkMode))
            .brandedNavigationBar(title: L(\.create_account))
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(L(\.cancel)) {
                        onDismiss()
                    }
                    .accessibilityLabel(L(\.cancel))
                    .accessibilityIdentifier("signup.cancelButton")
                    .tint(.white)
                }
            }
        }
    }
    
    private var isFormValid: Bool {
        !name.isEmpty && !email.isEmpty && !password.isEmpty &&
        email.contains("@") && meetsPasswordPolicy(password)
    }
}
