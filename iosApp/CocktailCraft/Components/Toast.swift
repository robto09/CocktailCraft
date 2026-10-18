import SwiftUI
import shared

struct Toast: View {
    let message: String
    let type: ToastType
    
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
        }
        .padding()
        .background(type.backgroundColor)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.2), radius: 8, x: 0, y: 4)
        .padding(.horizontal)
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
struct SharedEventToast: ViewModifier {
    let event: UiEvent?
    let clearEvent: () -> Void

    @State private var isShowing = false
    @State private var message = ""
    @State private var type: Toast.ToastType = .success

    func body(content: Content) -> some View {
        content
            .toast(isShowing: $isShowing, message: message, type: type, duration: 2)
            .onChange(of: event) { _, newEvent in
                guard let newEvent else { return }
                message = newEvent.message.localized()
                type = newEvent is UiEvent.Failure ? .error : .success
                withAnimation { isShowing = true }
                clearEvent()
            }
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