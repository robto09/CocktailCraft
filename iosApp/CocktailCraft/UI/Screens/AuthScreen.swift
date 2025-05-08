import SwiftUI
import shared

struct AuthScreen: View {
    @StateObject private var viewModel = DependencyContainer.shared.makeAuthViewModel()
    @State private var email = ""
    @State private var password = ""
    @State private var name = ""
    @State private var isRegistering = false
    @State private var showingResetPassword = false
    @State private var showingError = false
    @State private var showingBiometricPrompt = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Logo and Welcome Message
                    VStack(spacing: 8) {
                        Image("logo")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 120, height: 120)
                        
                        Text(isRegistering ? "Create Account" : "Welcome Back")
                            .font(.title)
                            .bold()
                        
                        Text(isRegistering ? "Sign up to start ordering" : "Sign in to continue")
                            .foregroundColor(.secondary)
                    }
                    .padding(.top, 40)
                    
                    // Form Fields
                    VStack(spacing: 16) {
                        if isRegistering {
                            FormField(
                                title: "Name",
                                text: $name,
                                placeholder: "Enter your name",
                                icon: "person.fill"
                            )
                        }
                        
                        FormField(
                            title: "Email",
                            text: $email,
                            placeholder: "Enter your email",
                            icon: "envelope.fill",
                            keyboardType: .emailAddress,
                            autocapitalization: .none
                        )
                        
                        FormField(
                            title: "Password",
                            text: $password,
                            placeholder: "Enter your password",
                            icon: "lock.fill",
                            isSecure: true
                        )
                    }
                    .padding(.horizontal)
                    
                    // Action Buttons
                    VStack(spacing: 16) {
                        Button(action: handleAuthAction) {
                            HStack {
                                if viewModel.isLoading {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                } else {
                                    Text(isRegistering ? "Sign Up" : "Sign In")
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.accentColor)
                            .foregroundColor(.white)
                            .cornerRadius(12)
                        }
                        .disabled(viewModel.isLoading || !isValidForm)
                        
                        if !isRegistering {
                            Button("Forgot Password?") {
                                showingResetPassword = true
                            }
                            .foregroundColor(.accentColor)
                        }
                        
                        // Toggle between login and register
                        Button(action: {
                            withAnimation {
                                isRegistering.toggle()
                                resetForm()
                            }
                        }) {
                            Text(isRegistering ? "Already have an account? Sign In" : "Don't have an account? Sign Up")
                                .foregroundColor(.accentColor)
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom, 40)
            }
            .alert("Error", isPresented: $showingError, presenting: viewModel.error) { _ in
                Button("OK") {}
            } message: { error in
                Text(error)
            }
            .sheet(isPresented: $showingResetPassword) {
                ResetPasswordSheet(onSubmit: { email in
                    viewModel.resetPassword(email: email)
                })
            }
            .alert("Use Biometric Login?", isPresented: $showingBiometricPrompt) {
                Button("Enable") {
                    viewModel.enableBiometricAuth()
                }
                Button("Not Now", role: .cancel) {}
            } message: {
                Text("Would you like to enable biometric authentication for faster login?")
            }
        }
    }
    
    private var isValidForm: Bool {
        let isValidEmail = email.contains("@") && email.contains(".")
        let isValidPassword = password.count >= 8
        
        if isRegistering {
            return isValidEmail && isValidPassword && !name.isEmpty
        } else {
            return isValidEmail && isValidPassword
        }
    }
    
    private func handleAuthAction() {
        if isRegistering {
            viewModel.register(email: email, password: password, name: name)
        } else {
            viewModel.login(email: email, password: password)
        }
    }
    
    private func resetForm() {
        email = ""
        password = ""
        name = ""
    }
}

private struct FormField: View {
    let title: String
    @Binding var text: String
    let placeholder: String
    let icon: String
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default
    var autocapitalization: TextInputAutocapitalization = .sentences
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .foregroundColor(.secondary)
            
            HStack {
                Image(systemName: icon)
                    .foregroundColor(.gray)
                
                if isSecure {
                    SecureField(placeholder, text: $text)
                } else {
                    TextField(placeholder, text: $text)
                        .keyboardType(keyboardType)
                        .textInputAutocapitalization(autocapitalization)
                }
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
        }
    }
}

private struct ResetPasswordSheet: View {
    let onSubmit: (String) -> Void
    
    @Environment(\.dismiss) private var dismiss
    @State private var email = ""
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Enter your email address")) {
                    TextField("Email", text: $email)
                        .keyboardType(.emailAddress)
                        .textInputAutocapitalization(.none)
                }
                
                Section {
                    Button(action: {
                        onSubmit(email)
                        dismiss()
                    }) {
                        Text("Reset Password")
                            .frame(maxWidth: .infinity)
                            .multilineTextAlignment(.center)
                    }
                    .disabled(!email.contains("@"))
                }
            }
            .navigationTitle("Reset Password")
            .navigationBarItems(
                leading: Button("Cancel") {
                    dismiss()
                }
            )
        }
    }
}