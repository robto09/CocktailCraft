import SwiftUI

struct SignInView: View {
    @Environment(\.isDarkMode) private var isDarkMode
    @State private var email = ""
    @State private var password = ""
    @State private var isPasswordVisible = false
    @State private var isLoading = false
    
    let onDismiss: () -> Void
    // Async so the button can hold isLoading across the real network call;
    // a fire-and-forget closure made the spinner and double-tap guard fake.
    let onSignIn: (String, String) async -> Void
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                // Header
                VStack(spacing: 8) {
                    Image(systemName: "person.circle.fill")
                        .font(.system(size: 60))
                        .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))

                    Text(L(\.sign_in_subtitle))
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 32)
                
                // Form
                VStack(spacing: 16) {
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
                            .accessibilityIdentifier("signin.emailField")
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
                                    .accessibilityIdentifier("signin.passwordField")
                            } else {
                                SecureField(L(\.auth_enter_password), text: $password)
                                    .accessibilityLabel(L(\.password))
                                    .accessibilityIdentifier("signin.passwordField")
                            }

                            Button(action: {
                                isPasswordVisible.toggle()
                            }) {
                                Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                                    .minimumHitTarget()
                            }
                            .accessibilityLabel(isPasswordVisible ? L(\.hide_password) : L(\.show_password))
                            .accessibilityIdentifier("signin.togglePasswordVisibility")
                        }
                        .textFieldStyle(CustomTextFieldStyle())
                    }
                }
                .padding(.horizontal)
                
                // Sign In Button
                PrimaryButton(
                    title: L(\.sign_in),
                    isLoading: isLoading,
                    isDisabled: !isFormValid,
                    action: {
                        guard !isLoading else { return }
                        isLoading = true
                        Task {
                            await onSignIn(email, password)
                            isLoading = false
                        }
                    }
                )
                .padding(.horizontal)
                .accessibilityLabel(L(\.sign_in))
                .accessibilityIdentifier("signin.submitButton")
                
                Spacer()
            }
            .background(AppColors.background(isDarkMode: isDarkMode))
            .navigationTitle(L(\.sign_in))
            .brandedNavigationBar()
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(L(\.cancel)) {
                        onDismiss()
                    }
                    .accessibilityLabel(L(\.cancel))
                    .accessibilityIdentifier("signin.cancelButton")
                    .tint(.white)
                }
            }
        }
    }
    
    private var isFormValid: Bool {
        !email.isEmpty && !password.isEmpty && email.contains("@")
    }
}

struct CustomTextFieldStyle: TextFieldStyle {
    @Environment(\.isDarkMode) private var isDarkMode

    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding()
            .background(AppColors.surface(isDarkMode: isDarkMode))
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(AppColors.gray.opacity(0.3), lineWidth: 1)
            )
    }
}
