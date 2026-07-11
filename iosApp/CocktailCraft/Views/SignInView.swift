import SwiftUI

struct SignInView: View {
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
                        .foregroundColor(.blue)

                    Text("Sign In")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)

                    Text("Welcome back! Please sign in to your account.")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 32)
                
                // Form
                VStack(spacing: 16) {
                    // Email Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Email")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        TextField("Enter your email", text: $email)
                            .textFieldStyle(CustomTextFieldStyle())
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .accessibilityLabel("Email")
                            .accessibilityIdentifier("signin.emailField")
                    }
                    
                    // Password Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Password")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        HStack {
                            if isPasswordVisible {
                                TextField("Enter your password", text: $password)
                                    .accessibilityLabel("Password")
                                    .accessibilityIdentifier("signin.passwordField")
                            } else {
                                SecureField("Enter your password", text: $password)
                                    .accessibilityLabel("Password")
                                    .accessibilityIdentifier("signin.passwordField")
                            }

                            Button(action: {
                                isPasswordVisible.toggle()
                            }) {
                                Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                                    .foregroundColor(.gray)
                                    .minimumHitTarget()
                            }
                            .accessibilityLabel(isPasswordVisible ? "Hide password" : "Show password")
                            .accessibilityIdentifier("signin.togglePasswordVisibility")
                        }
                        .textFieldStyle(CustomTextFieldStyle())
                    }
                }
                .padding(.horizontal)
                
                // Sign In Button
                Button(action: {
                    guard !isLoading else { return }
                    isLoading = true
                    Task {
                        await onSignIn(email, password)
                        isLoading = false
                    }
                }) {
                    HStack {
                        if isLoading {
                            ProgressView()
                                .scaleEffect(0.8)
                                .foregroundColor(.white)
                        }
                        
                        Text("Sign In")
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
                .accessibilityLabel("Sign In")
                .accessibilityIdentifier("signin.submitButton")
                
                Spacer()
            }
            .background(Color(.systemBackground))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        onDismiss()
                    }
                    .accessibilityLabel("Cancel")
                    .accessibilityIdentifier("signin.cancelButton")
                }
            }
        }
    }
    
    private var isFormValid: Bool {
        !email.isEmpty && !password.isEmpty && email.contains("@")
    }
}

struct CustomTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.gray.opacity(0.3), lineWidth: 1)
            )
    }
}
