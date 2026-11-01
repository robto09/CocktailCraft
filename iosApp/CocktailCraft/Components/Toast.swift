import SwiftUI
import shared

struct Toast: View {
    let message: String
    let type: ToastType
    /// Optional button at the end of the toast (e.g. Undo)
    var actionLabel: String? = nil
    var onAction: (() -> Void)? = nil

    enum ToastType {
        case success
        case error
        case info

        var backgroundColor: Color {
            switch self {
            case .success: return AppColors.success
            case .error: return AppColors.error
            case .info: return AppColors.primaryLight
            }
        }

        var icon: String {
            switch self {
            case .success: return "checkmark.circle.fill"
            case .error: return "xmark.circle.fill"
            case .info: return "info.circle.fill"
            }
        }
    }

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: type.icon)
                .foregroundColor(.white)
                .accessibilityHidden(true)

            Text(message)
                .font(.subheadline)
                .foregroundColor(.white)
                .lineLimit(2)

            Spacer()

            if let actionLabel, let onAction {
                // A real button (VoiceOver reaches it by swiping), at least
                // 44pt, white like Android's toast action
                Button(action: onAction) {
                    Text(actionLabel)
                        .font(.subheadline.weight(.bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, AppTheme.Spacing.sm)
                        .minimumHitTarget()
                }
                .accessibilityIdentifier("toast.actionButton")
            }
        }
        .padding(.leading)
        .padding(.trailing, actionLabel == nil ? nil : AppTheme.Spacing.sm)
        .padding(.vertical, actionLabel == nil ? nil : AppTheme.Spacing.xs)
        .background(type.backgroundColor)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.2), radius: 8, x: 0, y: 4)
        .padding(.horizontal)
        // The action is also offered on the message itself, from the
        // VoiceOver actions rotor
        .accessibilityElement(children: .contain)
        .modifier(ToastAccessibilityAction(label: actionLabel, action: onAction))
    }
}

/// Adds the toast's action (if any) as a named accessibility action.
private struct ToastAccessibilityAction: ViewModifier {
    let label: String?
    let action: (() -> Void)?

    func body(content: Content) -> some View {
        if let label, let action {
            content.accessibilityAction(named: Text(label), action)
        } else {
            content
        }
    }
}

// Toast modifier
struct ToastModifier: ViewModifier {
    @Binding var isShowing: Bool
    let message: String
    let type: Toast.ToastType
    let duration: TimeInterval

    func body(content: Content) -> some View {
        ZStack {
            content

            VStack {
                Spacer()

                if isShowing {
                    Toast(message: message, type: type)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                        .onAppear {
                            // Toasts never take focus: VoiceOver would miss them
                            AccessibilityNotification.Announcement(message).post()
                            DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
                                withAnimation {
                                    isShowing = false
                                }
                            }
                        }
                }
            }
            .padding(.bottom, 50) // Above tab bar
        }
        .animation(.spring(), value: isShowing)
    }
}

/// Shows a shared ViewModel's mirrored one-shot event (`UiEvent`) as a toast
/// — green for a success, red for a failure — and marks it shown. The
/// message is captured when the event arrives, so it doesn't blank out while
/// the toast animates away.
///
/// An event with an action (Undo after a removal) gets the action as a
/// button, stays up longer — longer still under VoiceOver, so the button can
/// be reached — and replaces whatever toast is showing.
struct SharedEventToast: ViewModifier {
    let event: UiEvent?
    let clearEvent: () -> Void

    /// One shown toast; its id keeps a replaced toast's timer from
    /// dismissing the one that replaced it.
    private struct Shown: Equatable {
        let id = Foundation.UUID()
        let message: String
        let type: Toast.ToastType
        let actionLabel: String?
        let action: UiEventAction?

        static func == (lhs: Shown, rhs: Shown) -> Bool { lhs.id == rhs.id }
    }

    @State private var shown: Shown?

    func body(content: Content) -> some View {
        ZStack {
            content

            VStack {
                Spacer()

                if let shown {
                    Toast(
                        message: shown.message,
                        type: shown.type,
                        actionLabel: shown.actionLabel,
                        onAction: shown.action.map { action in
                            {
                                action.perform()
                                dismiss(shown.id)
                            }
                        }
                    )
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .id(shown.id)
                    .onAppear {
                        // Toasts never take focus: VoiceOver would miss them
                        AccessibilityNotification.Announcement(shown.message).post()
                        let duration = Self.duration(hasAction: shown.action != nil)
                        DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
                            dismiss(shown.id)
                        }
                    }
                }
            }
            .padding(.bottom, 50) // Above tab bar
        }
        .animation(.spring(), value: shown)
        .onChange(of: event) { _, newEvent in
            guard let newEvent else { return }
            shown = Shown(
                message: newEvent.message.localized(),
                type: newEvent is UiEvent.Failure ? .error : .success,
                actionLabel: newEvent.action?.label.localized(),
                action: newEvent.action
            )
            clearEvent()
        }
    }

    private func dismiss(_ id: Foundation.UUID) {
        guard shown?.id == id else { return }
        withAnimation { shown = nil }
    }

    /// 2s like every toast; one with an action stays long enough to reach it.
    static func duration(hasAction: Bool) -> TimeInterval {
        guard hasAction else { return 2 }
        return UIAccessibility.isVoiceOverRunning ? 10 : 5
    }
}

extension View {
    func toast(isShowing: Binding<Bool>, message: String, type: Toast.ToastType = .info, duration: TimeInterval = 3) -> some View {
        modifier(ToastModifier(isShowing: isShowing, message: message, type: type, duration: duration))
    }

    /// Attach to a screen to toast its shared ViewModel's one-shot events.
    func sharedEventToast(_ event: UiEvent?, clear: @escaping () -> Void) -> some View {
        modifier(SharedEventToast(event: event, clearEvent: clear))
    }
}
