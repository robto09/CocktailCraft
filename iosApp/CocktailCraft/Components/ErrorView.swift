import SwiftUI

import shared

struct ErrorView: View {
    let error: ErrorHandler.UserFriendlyError
    let onRetry: (() -> Void)?
    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.appearance) private var appearance
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: getErrorIcon(for: error.category))
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 60, height: 60)
                .foregroundColor(getErrorColor(for: error.category))
                .accessibilityHidden(true)

            Text(error.title.localized())
                .font(.title3)
                .fontWeight(.semibold)
                .accessibilityAddTraits(.isHeader)
            
            Text(error.message.localized())
                .font(.body)
                .foregroundColor(AppColors.textSecondary(appearance))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            if let recoveryAction = error.recoveryAction {
                Button(action: {
                    recoveryAction.action()
                }) {
                    Text(recoveryAction.actionLabel.localized())
                        .font(.headline)
                        .foregroundStyle(AppColors.onPrimary(isDarkMode: isDarkMode))
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(AppColors.primary(appearance), in: RoundedRectangle(cornerRadius: 8))
                }
                .buttonStyle(.plain)
            } else if let onRetry = onRetry {
                Button(action: onRetry) {
                    Text(L(\.dialog_try_again))
                        .font(.headline)
                        .foregroundStyle(AppColors.onPrimary(isDarkMode: isDarkMode))
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(AppColors.primary(appearance), in: RoundedRectangle(cornerRadius: 8))
                }
                .buttonStyle(.plain)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private func getErrorIcon(for category: ErrorHandler.ErrorCategory) -> String {
        switch category {
        case .network:
            return "wifi.slash"
        case .server:
            return "exclamationmark.triangle"
        case .authentication:
            return "lock.slash"
        case .data:
            return "exclamationmark.circle"
        default:
            return "xmark.circle"
        }
    }
    
    private func getErrorColor(for category: ErrorHandler.ErrorCategory) -> Color {
        switch category {
        case .network:
            return AppColors.warning
        case .server:
            return AppColors.error
        case .authentication:
            return AppColors.primary(appearance)
        case .data:
            return AppColors.primary(appearance)
        default:
            return AppColors.gray
        }
    }
}