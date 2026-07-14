import SwiftUI

/// Standard full-frame loading indicator. Use this instead of ad-hoc
/// ProgressView arrangements so loading looks the same on every screen
/// (Home's redacted skeleton list is the deliberate exception).
struct LoadingStateView: View {
    var message: String? = nil

    var body: some View {
        VStack(spacing: 12) {
            ProgressView()
            if let message {
                Text(message)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
