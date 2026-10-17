import SwiftUI

// MARK: - Supporting Views

struct SettingsRow: View {
    let icon: String
    let title: String
    let action: () -> Void
    let textColor: Color
    /// Icon box grows with Dynamic Type so large symbols don't overflow it
    @ScaledMetric(relativeTo: .body) private var iconSize: CGFloat = 24

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
                    .frame(width: iconSize, height: iconSize)

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
        .accessibilityLabel(title)
        .accessibilityIdentifier("profile.row." + title)
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
    @ScaledMetric(relativeTo: .body) private var iconSize: CGFloat = 24

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
                .frame(width: iconSize, height: iconSize)
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

            AnimatedThemeSwitch(
                isChecked: isChecked,
                enabled: enabled,
                onToggle: { onToggle(!isChecked) }
            )
            .opacity(enabled ? 1.0 : 0.5)
        }
        .padding(.vertical, 12)
        // VoiceOver: one switch per row — "Dark Mode, Off, Switch button,
        // Controlled by system" — instead of three separate stops and an
        // unlabeled switch
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(title)
        .accessibilityValue(isChecked ? L(\.profile_toggle_on) : L(\.profile_toggle_off))
        .accessibilityHint(subtitle == L(\.profile_toggle_on) || subtitle == L(\.profile_toggle_off) ? "" : subtitle)
        .accessibilityAddTraits(.isToggle)
        .accessibilityAction {
            if enabled { onToggle(!isChecked) }
        }
        .disabled(!enabled)
    }
}

/// SwiftUI port of the Android AnimatedThemeSwitch: a capsule track that
/// darkens when checked, with an orange circular thumb carrying a sun/moon
/// icon that springs across.
struct AnimatedThemeSwitch: View {
    let isChecked: Bool
    let enabled: Bool
    let onToggle: () -> Void

    @Environment(\.isDarkMode) private var isDarkMode

    var body: some View {
        ZStack(alignment: .leading) {
            Capsule()
                // The off track stays subtle on dark surfaces
                .fill(isChecked
                    ? Color(hex: "3C4043")
                    : isDarkMode ? AppColors.neutralSubtle(isDarkMode: true) : Color(hex: "E4E4E4"))
                .frame(width: 72, height: 40)

            Circle()
                // Follows the selected accent (dark/light variant by state),
                // matching Android's AnimatedThemeSwitch thumb
                .fill(AppColors.primary(isDarkMode: isChecked))
                .frame(width: 24, height: 24)
                .overlay(
                    Image(systemName: isChecked ? "moon.fill" : "sun.max.fill")
                        .font(.caption)
                        // The switch is fixed-size artwork: keep the glyph
                        // inside its 24 pt thumb at large text sizes
                        .dynamicTypeSize(...DynamicTypeSize.large)
                        .foregroundColor(.white)
                        .accessibilityHidden(true)
                )
                .offset(x: isChecked ? 44 : 4)
        }
        // 72x40 visual; the tappable band extends to the 44 pt HIG minimum
        .frame(minHeight: 44)
        .contentShape(Rectangle())
        .onTapGesture {
            if enabled { onToggle() }
        }
        .animation(.spring(response: 0.4, dampingFraction: 0.65), value: isChecked)
        .accessibilityAddTraits(.isButton)
        .accessibilityValue(isChecked ? L(\.profile_toggle_on) : L(\.profile_toggle_off))
        .accessibilityIdentifier("profile.themeSwitch")
    }
}



// Rounded rectangles (12 corners), the shape of every other main button in
// the app (Place Order, Add to Cart) and of Android's Profile buttons.
struct PrimaryButtonStyle: ButtonStyle {
    @Environment(\.isDarkMode) var isDarkMode

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundColor(AppColors.onPrimary(isDarkMode: isDarkMode))
            .frame(maxWidth: .infinity)
            .padding()
            .background(AppColors.primary(isDarkMode: isDarkMode))
            .clipShape(RoundedRectangle(cornerRadius: AppTheme.CornerRadius.md))
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
            // Gray outline like Android's Material 3 OutlinedButton
            .overlay(
                RoundedRectangle(cornerRadius: AppTheme.CornerRadius.md)
                    .stroke(Color.gray.opacity(0.5), lineWidth: 1)
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}
