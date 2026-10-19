import SwiftUI

struct OfflineBanner: View {
    private let connectivity = ConnectivityObserver.shared
    @State private var showBanner = false
    
    var body: some View {
        ZStack(alignment: .top) {
            if showBanner && !connectivity.isConnected {
                HStack {
                    Image(systemName: "wifi.slash")
                        .foregroundColor(.white)
                        .accessibilityHidden(true)

                    Text(L(\.offline_you_are_offline))
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(.white)

                    Spacer()
                }
                .padding(.horizontal)
                .padding(.vertical, 12)
                .background(AppColors.warning)
                .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showBanner)
        .onAppear {
            showBanner = !connectivity.isConnected
        }
        .onChange(of: connectivity.isConnected) {
            withAnimation {
                showBanner = !connectivity.isConnected
            }
            // The banner doesn't take focus, so speak it when it appears
            if !connectivity.isConnected {
                AccessibilityNotification.Announcement(L(\.offline_you_are_offline)).post()
            }
        }
    }
}