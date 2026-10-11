import SwiftUI

struct OfflineBanner: View {
    private let networkMonitor = NetworkMonitor.shared
    @State private var showBanner = false
    
    var body: some View {
        ZStack(alignment: .top) {
            if showBanner && !networkMonitor.isConnected {
                HStack {
                    Image(systemName: "wifi.slash")
                        .foregroundColor(.white)

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
            showBanner = !networkMonitor.isConnected
        }
        .onChange(of: networkMonitor.isConnected) {
            withAnimation {
                showBanner = !networkMonitor.isConnected
            }
        }
    }
}