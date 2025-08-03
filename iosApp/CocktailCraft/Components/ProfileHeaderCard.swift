import SwiftUI

struct ProfileHeaderCard: View {
    let userName: String
    let userEmail: String
    let isLoggedIn: Bool
    let onSignIn: () -> Void
    let onSignUp: () -> Void
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(spacing: AppTheme.Spacing.lg) {
            // Profile Picture
            ProfileAvatarView(userName: userName)

            // User Info
            VStack(spacing: 4) {
                Text(userName)
                    .font(AppTheme.Typography.title2)
                    .fontWeight(.bold)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                Text(userEmail)
                    .font(AppTheme.Typography.subheadline)
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
            }

            // Sign In/Sign Up Buttons (if not logged in)
            if !isLoggedIn {
                VStack(spacing: AppTheme.Spacing.md) {
                    Text("Sign in to access your profile")
                        .font(AppTheme.Typography.body)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .padding(.top, AppTheme.Spacing.sm)

                    Button("Sign In", action: onSignIn)
                        .buttonStyle(PrimaryButtonStyle())

                    Button("Create Account", action: onSignUp)
                        .buttonStyle(SecondaryButtonStyle())
                }
            }
        }
        .padding(AppTheme.Spacing.lg)
        .cardStyle()
    }
}

struct ProfileAvatarView: View {
    let userName: String
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        ZStack {
            Circle()
                .fill(AppColors.primary(isDarkMode: isDarkMode).opacity(0.2))
                .frame(width: 80, height: 80)

            Text(userName.prefix(1).uppercased())
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
        }
    }
}

struct PrimaryButtonStyle: ButtonStyle {
    @Environment(\.isDarkMode) var isDarkMode

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(AppTheme.Typography.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(AppTheme.Spacing.lg)
            .background(AppColors.primary(isDarkMode: isDarkMode))
            .cornerRadius(AppTheme.CornerRadius.button)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(AppTheme.Animation.quick, value: configuration.isPressed)
    }
}

struct SecondaryButtonStyle: ButtonStyle {
    @Environment(\.isDarkMode) var isDarkMode

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(AppTheme.Typography.headline)
            .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
            .frame(maxWidth: .infinity)
            .padding(AppTheme.Spacing.lg)
            .background(Color.clear)
            .overlay(
                RoundedRectangle(cornerRadius: AppTheme.CornerRadius.button)
                    .stroke(AppColors.primary(isDarkMode: isDarkMode), lineWidth: 1)
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(AppTheme.Animation.quick, value: configuration.isPressed)
    }
}

#Preview {
    VStack(spacing: 20) {
        ProfileHeaderCard(
            userName: "John Doe",
            userEmail: "john.doe@example.com",
            isLoggedIn: true,
            onSignIn: {},
            onSignUp: {}
        )
        
        ProfileHeaderCard(
            userName: "Guest",
            userEmail: "Not signed in",
            isLoggedIn: false,
            onSignIn: {},
            onSignUp: {}
        )
    }
    .environment(\.isDarkMode, false)
    .padding()
}
