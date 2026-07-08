import SwiftUI

struct SignUpView: View {
    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var isPasswordVisible = false
    @State private var isLoading = false
    
    let onDismiss: () -> Void
    let onSignUp: (String, String, String) -> Void
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
                        .foregroundColor(.blue)

                    Text("Create Account")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)

                    Text("Join us and discover amazing cocktails!")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 32)
                
                // Form
                VStack(spacing: 16) {
                    // Name Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Full Name")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        TextField("Enter your full name", text: $name)
                            .textFieldStyle(CustomTextFieldStyle())
                            .autocapitalization(.words)
                    }
                    
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
                    }
                    
                    // Password Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Password")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        HStack {
                            if isPasswordVisible {
                                TextField("Enter your password", text: $password)
                            } else {
                                SecureField("Enter your password", text: $password)
                            }
                            
                            Button(action: {
                                isPasswordVisible.toggle()
                            }) {
                                Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                                    .foregroundColor(.gray)
                            }
                        }
                        .textFieldStyle(CustomTextFieldStyle())
                        
                        // Password strength indicator (one segment per point
                        // of the shared 0-5 strength scale)
                        if !password.isEmpty {
                            HStack {
                                ForEach(0..<5, id: \.self) { index in
                                    Rectangle()
                                        .frame(height: 4)
                                        .foregroundColor(index < passwordStrength ? passwordStrengthColor : Color.gray.opacity(0.3))
                                        .cornerRadius(2)
                                }
                            }
                            .padding(.top, 4)
                            
                            Text(passwordStrengthText)
                                .font(.caption)
                                .foregroundColor(passwordStrengthColor)
                        }
                    }
                }
                .padding(.horizontal)
                
                // Create Account Button
                Button(action: {
                    isLoading = true
                    onSignUp(name, email, password)
                    isLoading = false
                }) {
                    HStack {
                        if isLoading {
                            ProgressView()
                                .scaleEffect(0.8)
                                .foregroundColor(.white)
                        }
                        
                        Text("Create Account")
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
                
                Spacer()
            }
            .background(Color(.systemBackground))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        onDismiss()
                    }
                }
            }
        }
    }
    
    private var isFormValid: Bool {
        !name.isEmpty && !email.isEmpty && !password.isEmpty &&
        email.contains("@") && meetsPasswordPolicy(password)
    }

    // The shared AuthInputValidator's 0-5 score, via ProfileViewModelSKIE.
    private var passwordStrength: Int {
        getPasswordStrength(password)
    }

    private var passwordStrengthColor: Color {
        switch passwordStrength {
        case 0...1: return .red
        case 2: return .orange
        case 3: return .yellow
        case 4...5: return .green
        default: return .gray
        }
    }

    private var passwordStrengthText: String {
        switch passwordStrength {
        case 0...1: return "Weak password"
        case 2: return "Fair password"
        case 3: return "Good password"
        case 4...5: return "Strong password"
        default: return ""
        }
    }
}
