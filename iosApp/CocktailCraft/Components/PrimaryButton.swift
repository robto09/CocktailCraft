import SwiftUI

/// Reusable primary CTA matching Android's PrimaryButton component: a
/// full-width rounded coral button with a bold white label, an optional
/// leading SF Symbol, and a white spinner that replaces the label while
/// loading. Disabled state renders the background at 50% opacity.
struct PrimaryButton: View {
    let title: String
    var icon: String? = nil
    var isLoading: Bool = false
    var isDisabled: Bool = false
    /// Destructive actions (e.g. Clear Cache) swap the coral primary for the
    /// token-based AppColors.error background; everything else is identical.
    var isDestructive: Bool = false
    let action: () -> Void

    @Environment(\.isDarkMode) private var isDarkMode

    private var backgroundColor: Color {
        isDestructive ? AppColors.error : AppColors.primary(isDarkMode: isDarkMode)
    }

    var body: some View {
        Button(action: action) {
            ZStack {
                if isLoading {
                    ProgressView()
                        .tint(.white)
                } else {
                    HStack(spacing: AppTheme.Spacing.sm) {
                        if let icon {
                            Image(systemName: icon)
                        }
                        Text(title)
                            .fontWeight(.bold)
                    }
                    .font(.headline)
                }
            }
            .foregroundColor(.white)
            .frame(maxWidth: .infinity, minHeight: 54)
            .background(backgroundColor.opacity(isDisabled ? 0.5 : 1.0))
            .cornerRadius(AppTheme.CornerRadius.md)
        }
        .disabled(isDisabled || isLoading)
    }
}
