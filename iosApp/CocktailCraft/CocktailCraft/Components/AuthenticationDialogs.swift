import SwiftUI

struct SignInDialog: View {
    @Binding var isPresented: Bool
    let onSignIn: (String, String) -> Void
    
    @State private var email = ""
    @State private var password = ""
    @State private var isPasswordVisible = false
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("Email", text: $email)
                        .textContentType(.emailAddress)
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                    
                    HStack {
                        if isPasswordVisible {
                            TextField("Password", text: $password)
                                .textContentType(.password)
                        } else {
                            SecureField("Password", text: $password)
                                .textContentType(.password)
                        }
                        
                        Button(action: { isPasswordVisible.toggle() }) {
                            Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                                .foregroundColor(.gray)
                        }
                    }
                }
            }
            .navigationTitle("Sign In")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        isPresented = false
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Sign In") {
                        onSignIn(email, password)
                        isPresented = false
                    }
                    .disabled(email.isEmpty || password.isEmpty)
                }
            }
        }
    }
}

struct SignUpDialog: View {
    @Binding var isPresented: Bool
    let onSignUp: (String, String, String) -> Void
    
    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var isPasswordVisible = false
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("Name", text: $name)
                        .textContentType(.name)
                    
                    TextField("Email", text: $email)
                        .textContentType(.emailAddress)
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                    
                    HStack {
                        if isPasswordVisible {
                            TextField("Password", text: $password)
                                .textContentType(.newPassword)
                        } else {
                            SecureField("Password", text: $password)
                                .textContentType(.newPassword)
                        }
                        
                        Button(action: { isPasswordVisible.toggle() }) {
                            Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                                .foregroundColor(.gray)
                        }
                    }
                }
            }
            .navigationTitle("Create Account")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        isPresented = false
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Create") {
                        onSignUp(name, email, password)
                        isPresented = false
                    }
                    .disabled(name.isEmpty || email.isEmpty || password.isEmpty)
                }
            }
        }
    }
}