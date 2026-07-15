import SwiftUI

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

extension View {
    func toast(isShowing: Binding<Bool>, message: String, type: Toast.ToastType = .info, duration: TimeInterval = 3) -> some View {
        modifier(ToastModifier(isShowing: isShowing, message: message, type: type, duration: duration))
    }
}