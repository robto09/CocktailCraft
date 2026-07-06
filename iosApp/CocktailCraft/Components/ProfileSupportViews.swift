import SwiftUI

// MARK: - Supporting Views

struct SettingsRow: View {
    let icon: String
    let title: String
    let action: () -> Void
    let textColor: Color

    init(icon: String, title: String, action: @escaping () -> Void, textColor: Color = .primary) {
        self.icon = icon
        self.title = title
        self.action = action
        self.textColor = textColor
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .foregroundColor(textColor)
                    .frame(width: 24, height: 24)

                Text(title)
                    .font(.body)
                    .foregroundColor(textColor)

                Spacer()

                Image(systemName: "chevron.right")
                    .foregroundColor(.gray)
                    .font(.caption)
            }
            .padding(.vertical, 12)
            // Without an explicit content shape, PlainButtonStyle only
            // hit-tests opaque pixels — the stretch around the Spacer was
            // a tap dead zone.
            .contentShape(Rectangle())
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct ThemeToggleRow: View {
    let title: String
    let subtitle: String
    let icon: String
    let isChecked: Bool
    // Receives the Toggle's target value so repeated/duplicate firings are
    // idempotent (a computed-at-tap flip can flip twice and undo itself).
    let onToggle: (Bool) -> Void
    let enabled: Bool

    init(title: String, subtitle: String, icon: String, isChecked: Bool, onToggle: @escaping (Bool) -> Void, enabled: Bool = true) {
        self.title = title
        self.subtitle = subtitle
        self.icon = icon
        self.isChecked = isChecked
        self.onToggle = onToggle
        self.enabled = enabled
    }

    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .foregroundColor(enabled ? .primary : .gray)
                .frame(width: 24, height: 24)
                .scaleEffect(enabled ? 1.0 : 0.8)
                .animation(AppTheme.Animation.quick, value: enabled)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.body)
                    .foregroundColor(enabled ? .primary : .gray)

                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .opacity(enabled ? 1.0 : 0.6)
            .animation(AppTheme.Animation.standard, value: enabled)

            Spacer()

            Toggle("", isOn: Binding(
                get: { isChecked },
                set: { newValue in onToggle(newValue) }
            ))
            .disabled(!enabled)
            .opacity(enabled ? 1.0 : 0.5)
        }
        .padding(.vertical, 12)
    }
}



struct PrimaryButtonStyle: ButtonStyle {
    @Environment(\.isDarkMode) var isDarkMode

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(AppColors.primary(isDarkMode: isDarkMode))
            .cornerRadius(8)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}

struct SecondaryButtonStyle: ButtonStyle {
    @Environment(\.isDarkMode) var isDarkMode

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.clear)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(AppColors.primary(isDarkMode: isDarkMode), lineWidth: 1)
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}
