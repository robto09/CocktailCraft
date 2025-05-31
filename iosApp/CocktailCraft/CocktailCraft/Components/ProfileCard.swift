import SwiftUI

struct ProfileCard: View {
    let userName: String
    let userEmail: String
    let isSignedIn: Bool
    let onSignIn: () -> Void
    let onSignUp: () -> Void
    
    private var userInitial: String {
        userName.prefix(1).uppercased()
    }
    
    var body: some View {
        VStack(spacing: 16) {
            // Profile picture
            ZStack {
                Circle()
                    .fill(Color.blue.opacity(0.2))
                    .frame(width: 80, height: 80)
                
                Text(userInitial)
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.blue)
            }
            
            // User name
            Text(userName)
                .font(.system(size: 20, weight: .bold))
            
            // User email
            Text(userEmail)
                .font(.system(size: 14))
                .foregroundColor(.secondary)
            
            // Sign in/up buttons if not signed in
            if !isSignedIn {
                Text("Sign in to access your profile")
                    .font(.system(size: 16))
                    .foregroundColor(.secondary)
                    .padding(.top, 8)
                
                VStack(spacing: 12) {
                    Button(action: onSignIn) {
                        Text("Sign In")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.blue)
                            .cornerRadius(8)
                    }
                    
                    Button(action: onSignUp) {
                        Text("Create Account")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.blue)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color.blue, lineWidth: 1)
                            )
                    }
                }
                .padding(.horizontal, 32)
            }
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(16)
    }
}