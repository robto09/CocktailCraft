import SwiftUI

struct SignUpView: View {
    @Environment(\.isDarkMode) private var isDarkMode
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
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))

                    Text("Join us and discover amazing cocktails!")
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 32)
                
                // Form
                VStack(spacing: 16) {
                    // Name Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Full Name")
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        
                        TextField("Enter your full name", text: $name)
                            .textFieldStyle(CustomTextFieldStyle())
                            .autocapitalization(.words)
                            .accessibilityLabel("Full name")
                            .accessibilityIdentifier("signup.nameField")
                    }
                    
                    // Email Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Email")
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        
                        TextField("Enter your email", text: $email)
                            .textFieldStyle(CustomTextFieldStyle())
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .accessibilityLabel("Email")
                            .accessibilityIdentifier("signup.emailField")
                    }
                    
                    // Password Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Password")
                            .font(.headline)
                            .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))
                        
                        HStack {
                            if isPasswordVisible {
                                TextField("Enter your password", text: $password)
                                    .accessibilityLabel("Password")
                                    .accessibilityIdentifier("signup.passwordField")
                            } else {
                                SecureField("Enter your password", text: $password)
                                    .accessibilityLabel("Password")
                                    .accessibilityIdentifier("signup.passwordField")
                            }

                            Button(action: {
                                isPasswordVisible.toggle()
                            }) {
                                Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                                    .minimumHitTarget()
                            }
                            .accessibilityLabel(isPasswordVisible ? "Hide password" : "Show password")
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
                    title: String(localized: "Create Account"),
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
                .accessibilityLabel("Create Account")
                .accessibilityIdentifier("signup.submitButton")
                
                Spacer()
            }
            .background(AppColors.background(isDarkMode: isDarkMode))
            .navigationTitle("Create Account")
            .brandedNavigationBar()
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        onDismiss()
                    }
                    .accessibilityLabel("Cancel")
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
