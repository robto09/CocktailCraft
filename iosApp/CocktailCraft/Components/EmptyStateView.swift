import SwiftUI

/// Unified empty state (icon / title / message, plus an optional PrimaryButton
/// action) shared by Home, Cart, Favorites, and Orders — mirroring Android's
/// single empty-state component.
struct EmptyStateView: View {
    let icon: String
    let title: String
    let message: String
    var actionTitle: String? = nil
    var action: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: icon)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 80, height: 80)
                .foregroundColor(.gray)

            Text(title)
                .font(.title2)
                .fontWeight(.semibold)
                // Stable hook for UI tests, so they don't key on display copy
                .accessibilityIdentifier("emptyState.title")

            Text(message)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)

            if let actionTitle, let action {
                PrimaryButton(title: actionTitle, action: action)
                    .padding(.horizontal, 40)
                    .padding(.top, 8)
                    .accessibilityIdentifier("emptyState.actionButton")
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
