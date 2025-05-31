import SwiftUI

struct ErrorDialog: View {
    let errorTitle: String
    let errorMessage: String
    let errorIcon: String
    let iconColor: Color
    let showRecoveryAction: Bool
    let recoveryActionLabel: String?
    let onDismiss: () -> Void
    let onRetry: (() -> Void)?
    let onRecoveryAction: (() -> Void)?
    
    init(
        errorTitle: String,
        errorMessage: String,
        errorIcon: String = "exclamationmark.triangle.fill",
        iconColor: Color = .red,
        showRecoveryAction: Bool = true,
        recoveryActionLabel: String? = nil,
        onDismiss: @escaping () -> Void,
        onRetry: (() -> Void)? = nil,
        onRecoveryAction: (() -> Void)? = nil
    ) {
        self.errorTitle = errorTitle
        self.errorMessage = errorMessage
        self.errorIcon = errorIcon
        self.iconColor = iconColor
        self.showRecoveryAction = showRecoveryAction
        self.recoveryActionLabel = recoveryActionLabel
        self.onDismiss = onDismiss
        self.onRetry = onRetry
        self.onRecoveryAction = onRecoveryAction
    }
    
    var body: some View {
        VStack(spacing: 16) {
            // Error icon
            Image(systemName: errorIcon)
                .font(.system(size: 48))
                .foregroundColor(iconColor)
            
            // Error title
            Text(errorTitle)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.primary)
            
            // Error message
            Text(errorMessage)
                .font(.system(size: 16))
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 8)
            
            // Action buttons
            HStack {
                Button("Dismiss") {
                    onDismiss()
                }
                .buttonStyle(.plain)
                .foregroundColor(.secondary)
                
                Spacer()
                
                if showRecoveryAction {
                    if let recoveryActionLabel = recoveryActionLabel, let onRecoveryAction = onRecoveryAction {
                        Button(recoveryActionLabel) {
                            onRecoveryAction()
                            onDismiss()
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(.orange)
                    } else if let onRetry = onRetry {
                        Button("Try Again") {
                            onRetry()
                            onDismiss()
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(.orange)
                    }
                }
            }
            .padding(.top, 8)
        }
        .padding(24)
        .background(Color(UIColor.systemBackground))
        .cornerRadius(16)
        .shadow(radius: 10)
    }
}

struct ErrorBanner: View {
    let errorTitle: String
    let errorMessage: String
    let errorIcon: String
    let backgroundColor: Color
    let actionLabel: String?
    let onDismiss: () -> Void
    let onAction: (() -> Void)?
    
    init(
        errorTitle: String,
        errorMessage: String,
        errorIcon: String = "exclamationmark.triangle.fill",
        backgroundColor: Color = .red.opacity(0.9),
        actionLabel: String? = nil,
        onDismiss: @escaping () -> Void,
        onAction: (() -> Void)? = nil
    ) {
        self.errorTitle = errorTitle
        self.errorMessage = errorMessage
        self.errorIcon = errorIcon
        self.backgroundColor = backgroundColor
        self.actionLabel = actionLabel
        self.onDismiss = onDismiss
        self.onAction = onAction
    }
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: errorIcon)
                .font(.system(size: 24))
                .foregroundColor(.white)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(errorTitle)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                
                Text(errorMessage)
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.9))
            }
            
            Spacer()
            
            if let actionLabel = actionLabel, let onAction = onAction {
                Button(actionLabel) {
                    onAction()
                    onDismiss()
                }
                .buttonStyle(.plain)
                .foregroundColor(.white)
                .font(.system(size: 14, weight: .bold))
            }
        }
        .padding(16)
        .background(backgroundColor)
        .cornerRadius(12)
        .shadow(radius: 4)
        .padding(.horizontal, 16)
        .transition(.asymmetric(
            insertion: .move(edge: .top).combined(with: .opacity),
            removal: .move(edge: .top).combined(with: .opacity)
        ))
    }
}

#Preview {
    VStack(spacing: 20) {
        ErrorDialog(
            errorTitle: "Network Error",
            errorMessage: "Unable to connect to the server. Please check your internet connection and try again.",
            errorIcon: "wifi.exclamationmark",
            iconColor: .orange,
            onDismiss: {},
            onRetry: {}
        )
        
        ErrorBanner(
            errorTitle: "Connection Lost",
            errorMessage: "You are currently offline",
            errorIcon: "wifi.slash",
            backgroundColor: .orange.opacity(0.9),
            actionLabel: "Retry",
            onDismiss: {},
            onAction: {}
        )
    }
    .padding()
}