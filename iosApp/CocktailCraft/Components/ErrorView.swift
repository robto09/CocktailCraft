import SwiftUI

import shared

struct ErrorView: View {
    let error: ErrorHandler.UserFriendlyError
    let onRetry: (() -> Void)?
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: getErrorIcon(for: error.category as! shared.__Bridge__ErrorHandler_ErrorCategory))
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 60, height: 60)
                .foregroundColor(getErrorColor(for: error.category as! shared.__Bridge__ErrorHandler_ErrorCategory))
            
            Text(error.title)
                .font(.title3)
                .fontWeight(.semibold)
            
            Text(error.message)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            if let recoveryAction = error.recoveryAction {
                Button(action: {
                    recoveryAction.action()
                }) {
                    Text(recoveryAction.actionLabel)
                        .font(.headline)
                        .foregroundStyle(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(Color.blue, in: RoundedRectangle(cornerRadius: 8))
                }
                .buttonStyle(.plain)
            } else if let onRetry = onRetry {
                Button(action: onRetry) {
                    Text("Try Again")
                        .font(.headline)
                        .foregroundStyle(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(Color.blue, in: RoundedRectangle(cornerRadius: 8))
                }
                .buttonStyle(.plain)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private func getErrorIcon(for category: shared.__Bridge__ErrorHandler_ErrorCategory) -> String {
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
    
    private func getErrorColor(for category: shared.__Bridge__ErrorHandler_ErrorCategory) -> Color {
        switch category {
        case .network:
            return .blue
        case .server:
            return .orange
        case .authentication:
            return .red
        case .data:
            return .purple
        default:
            return .red
        }
    }
}